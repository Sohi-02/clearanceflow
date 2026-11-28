
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.DocumentResponseDTO;
import com.clearanceflow.backend.entity.Application;
import com.clearanceflow.backend.entity.Document;
import com.clearanceflow.backend.entity.User;
import com.clearanceflow.backend.enums.ApplicationStatus;

import com.clearanceflow.backend.repository.ApplicationRepository;
import com.clearanceflow.backend.repository.DocumentRepository;
import com.clearanceflow.backend.repository.UserRepository;
import com.clearanceflow.backend.repository.ApprovalDocumentRequirementRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApprovalDocumentRequirementRepository requirementRepository;

    private final Path uploadDirectory;

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    // --------------------------------------------------
    // CONSTRUCTOR
    // --------------------------------------------------

    public DocumentService(
            DocumentRepository documentRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            ApprovalDocumentRequirementRepository requirementRepository,
            @Value("${app.upload.dir}") String uploadDir) {

        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.requirementRepository = requirementRepository;

        this.uploadDirectory = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // --------------------------------------------------
    // 1. UPLOAD DOCUMENT (APPLICANT)
    // --------------------------------------------------

    public DocumentResponseDTO uploadDocument(
            Long applicationId,
            String documentType,
            MultipartFile file) {

        // Applicant must own the application.
        // Application must still be DRAFT.
        Application application =
                getOwnedDraftApplication(applicationId);

        // Validate document type format.
        if (documentType == null ||
                !documentType.matches("[A-Z][A-Z0-9_]{0,99}")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid document type"
            );
        }

        // Check document type against approval requirements.
        Long approvalId = application.getApproval().getId();

        boolean allowed = requirementRepository
                .findByApprovalId(approvalId)
                .stream()
                .anyMatch(requirement ->
                        requirement.getDocumentType().equals(documentType)
                );

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type is not allowed for this approval"
            );
        }

        // Prevent duplicate document types.
        if (documentRepository
                .findByApplicationIdAndDocumentType(
                        applicationId,
                        documentType
                ).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Document type already uploaded"
            );
        }

        // Check file existence.
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File cannot be empty"
            );
        }

        // Check file size.
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Maximum file size is 5 MB"
            );
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File name is required"
            );
        }

        // Remove possible directory information.
        String safeName = originalName.replace('\\', '/');
        safeName = safeName.substring(
                safeName.lastIndexOf('/') + 1
        );

        if (safeName.isBlank() || safeName.length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        String extension = getExtension(safeName);

        // Allowed extensions.
        if (!extension.equals("pdf") &&
                !extension.equals("jpg") &&
                !extension.equals("jpeg") &&
                !extension.equals("png")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, JPG, JPEG and PNG files are allowed"
            );
        }

        // Check basic file signatures.
        if (!hasValidSignature(file, extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File content does not match its extension"
            );
        }

        // Generate unique stored filename.
        String storedName = UUID.randomUUID() + "." + extension;

        Path applicationFolder = uploadDirectory
                .resolve(applicationId.toString())
                .normalize();

        Path destination = applicationFolder
                .resolve(storedName)
                .normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid storage path"
            );
        }

        // Save file to disk.
        try {
            Files.createDirectories(applicationFolder);

            // Do not write through symbolic-link folders.
            if (!applicationFolder.toRealPath()
                    .startsWith(uploadDirectory.toRealPath())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Invalid storage directory"
                );
            }

            try (InputStream input = file.getInputStream()) {
                Files.copy(
                        input,
                        destination
                );
            }

        } catch (IOException e) {

            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store document",
                    e
            );
        }

        // Save document metadata.
        try {
            Document document = new Document();

            document.setApplication(application);
            document.setDocumentType(documentType);
            document.setFileName(safeName);
            document.setFilePath(
                    applicationId + "/" + storedName
            );

            Document saved =
                    documentRepository.saveAndFlush(document);

            return mapToResponseDTO(saved);

        } catch (RuntimeException e) {

            // Clean up file if database save fails.
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }

            throw e;
        }
    }

    // --------------------------------------------------
    // 2. LIST DOCUMENTS (APPLICANT)
    // --------------------------------------------------

    public List<DocumentResponseDTO> getApplicationDocuments(
            Long applicationId) {

        getOwnedApplication(applicationId);

        return documentRepository
                .findByApplicationId(applicationId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // --------------------------------------------------
    // 3. DOWNLOAD DOCUMENT (APPLICANT)
    // --------------------------------------------------

    public ResponseEntity<byte[]> downloadDocument(
            Long applicationId,
            Long documentId) {

        // Applicant must own the application.
        getOwnedApplication(applicationId);

        return readDocumentFile(applicationId, documentId);
    }

    // --------------------------------------------------
    // 4. DELETE DOCUMENT (APPLICANT)
    // --------------------------------------------------

    public void deleteDocument(
            Long applicationId,
            Long documentId) {

        // Only the owner of a DRAFT application may delete.
        getOwnedDraftApplication(applicationId);

        Document document = getDocumentInApplication(
                applicationId,
                documentId
        );

        Path filePath = resolveStoredFilePath(document);

        try {
            // Reject symbolic links in the stored path.
            if (Files.isSymbolicLink(filePath)
                    || Files.isSymbolicLink(filePath.getParent())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Invalid document path"
                );
            }

            // Check the real parent directory.
            if (Files.exists(filePath.getParent())
                    && !filePath.getParent().toRealPath()
                    .startsWith(uploadDirectory.toRealPath())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Invalid document path"
                );
            }

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete document file",
                    e
            );
        }

        documentRepository.delete(document);
    }

    // --------------------------------------------------
    // 5. LIST DOCUMENTS (OFFICER)
    // --------------------------------------------------

    public List<DocumentResponseDTO> getOfficerApplicationDocuments(
            Long applicationId) {

        getOfficerAccessibleApplication(applicationId);

        return documentRepository
                .findByApplicationId(applicationId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // --------------------------------------------------
    // 6. DOWNLOAD DOCUMENT (OFFICER)
    // --------------------------------------------------

    public ResponseEntity<byte[]> downloadOfficerDocument(
            Long applicationId,
            Long documentId) {

        // Officers cannot access DRAFT applications.
        getOfficerAccessibleApplication(applicationId);

        return readDocumentFile(applicationId, documentId);
    }

    // --------------------------------------------------
    // 7. SHARED FILE DOWNLOAD LOGIC
    // --------------------------------------------------

    private ResponseEntity<byte[]> readDocumentFile(
            Long applicationId,
            Long documentId) {

        Document document = getDocumentInApplication(
                applicationId,
                documentId
        );

        Path filePath = resolveStoredFilePath(document);

        try {
            Path realUploadDirectory = uploadDirectory.toRealPath();

            Path realFilePath = filePath.toRealPath();

            // Prevent reading files outside upload storage.
            if (!realFilePath.startsWith(realUploadDirectory)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Invalid document path"
                );
            }

            if (!Files.isRegularFile(realFilePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "File not found"
                );
            }

            byte[] fileBytes = Files.readAllBytes(realFilePath);

            String downloadName = document.getFileName()
                    .replace('\\', '/');

            downloadName = downloadName.substring(
                    downloadName.lastIndexOf('/') + 1
            );

            String contentDisposition = ContentDisposition
                    .attachment()
                    .filename(downloadName, StandardCharsets.UTF_8)
                    .build()
                    .toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            "Content-Disposition",
                            contentDisposition
                    )
                    .contentLength(fileBytes.length)
                    .body(fileBytes);

        } catch (NoSuchFileException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Stored document file not found",
                    e
            );

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read document",
                    e
            );
        }
    }

    // --------------------------------------------------
    // 8. VERIFY DOCUMENT BELONGS TO APPLICATION
    // --------------------------------------------------

    private Document getDocumentInApplication(
            Long applicationId,
            Long documentId) {

        Document document = documentRepository
                .findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Document not found"
                ));

        if (!document.getApplication().getId()
                .equals(applicationId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document not found in this application"
            );
        }

        return document;
    }

    // --------------------------------------------------
    // 9. RESOLVE STORED FILE PATH
    // --------------------------------------------------

    private Path resolveStoredFilePath(Document document) {

        Path filePath = uploadDirectory
                .resolve(document.getFilePath())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid document path"
            );
        }

        return filePath;
    }

    // --------------------------------------------------
    // 10. VERIFY APPLICATION OWNERSHIP
    // --------------------------------------------------

    private Application getOwnedApplication(Long applicationId) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        if (user.getCompany() == null
                || !application.getCompany().getId()
                .equals(user.getCompany().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this application's documents"
            );
        }

        return application;
    }

    // --------------------------------------------------
    // 11. VERIFY OWNERSHIP AND DRAFT STATUS
    // --------------------------------------------------

    private Application getOwnedDraftApplication(Long applicationId) {

        Application application =
                getOwnedApplication(applicationId);

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Documents can only be modified in DRAFT status"
            );
        }

        return application;
    }

    // --------------------------------------------------
    // 12. VERIFY OFFICER ACCESS
    // --------------------------------------------------

    private Application getOfficerAccessibleApplication(
            Long applicationId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Application not found"
                ));

        if (application.getStatus() == ApplicationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Officers cannot access draft application documents"
            );
        }

        return application;
    }

    // --------------------------------------------------
    // 13. EXTRACT FILE EXTENSION
    // --------------------------------------------------

    private String getExtension(String fileName) {

        int dot = fileName.lastIndexOf('.');

        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dot + 1)
                .toLowerCase(Locale.ROOT);
    }

    // --------------------------------------------------
    // 14. BASIC FILE SIGNATURE VALIDATION
    // --------------------------------------------------

    private boolean hasValidSignature(
            MultipartFile file,
            String extension) {

        try (InputStream input = file.getInputStream()) {

            byte[] header = input.readNBytes(8);

            if (extension.equals("pdf")) {

                byte[] pdf = "%PDF-".getBytes(
                        StandardCharsets.US_ASCII
                );

                return startsWith(header, pdf);
            }

            if (extension.equals("jpg")
                    || extension.equals("jpeg")) {

                return header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
            }

            if (extension.equals("png")) {

                byte[] png = {
                        (byte) 0x89,
                        0x50,
                        0x4E,
                        0x47,
                        0x0D,
                        0x0A,
                        0x1A,
                        0x0A
                };

                return startsWith(header, png);
            }

            return false;

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded file",
                    e
            );
        }
    }

    private boolean startsWith(
            byte[] data,
            byte[] prefix) {

        return data.length >= prefix.length
                && Arrays.equals(
                Arrays.copyOf(data, prefix.length),
                prefix
        );
    }

    // --------------------------------------------------
    // 15. CONVERT DOCUMENT ENTITY TO DTO
    // --------------------------------------------------

    private DocumentResponseDTO mapToResponseDTO(
            Document document) {

        DocumentResponseDTO response =
                new DocumentResponseDTO();

        response.setId(document.getId());
        response.setApplicationId(
                document.getApplication().getId()
        );
        response.setDocumentType(document.getDocumentType());
        response.setFileName(document.getFileName());
        response.setUploadedAt(document.getUploadedAt());

        return response;
    }
}
