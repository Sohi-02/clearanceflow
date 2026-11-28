
package com.clearanceflow.backend.repository;

import com.clearanceflow.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    // Retrieve all documents uploaded for an application
    List<Document> findByApplicationId(Long applicationId);

    // Find a specific document type within an application
    Optional<Document> findByApplicationIdAndDocumentType(
            Long applicationId,
            String documentType
    );
}
