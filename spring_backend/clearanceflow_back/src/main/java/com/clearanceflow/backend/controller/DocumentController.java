
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.DocumentResponseDTO;
import com.clearanceflow.backend.service.DocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // UPLOAD DOCUMENT
    @PostMapping(
            value = "/{id}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @PathVariable Long id,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {

        DocumentResponseDTO response =
                documentService.uploadDocument(id, documentType, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // LIST DOCUMENTS
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentResponseDTO>> getApplicationDocuments(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                documentService.getApplicationDocuments(id)
        );
    }
    // DOWNLOAD A DOCUMENT
    @GetMapping("/{id}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long documentId) {

        return documentService.downloadDocument(id, documentId);
    }
    // DELETE A DOCUMENT
    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long documentId) {

        documentService.deleteDocument(id, documentId);

        return ResponseEntity.noContent().build();
    }
}
