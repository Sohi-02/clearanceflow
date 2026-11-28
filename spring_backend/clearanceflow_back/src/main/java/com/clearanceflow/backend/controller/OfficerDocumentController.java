
package com.clearanceflow.backend.controller;

import com.clearanceflow.backend.dto.DocumentResponseDTO;
import com.clearanceflow.backend.service.DocumentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer/applications")
public class OfficerDocumentController {

    private final DocumentService documentService;

    public OfficerDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentResponseDTO>> getDocuments(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                documentService.getOfficerApplicationDocuments(id)
        );
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long documentId) {

        return documentService.downloadOfficerDocument(id, documentId);
    }
}
