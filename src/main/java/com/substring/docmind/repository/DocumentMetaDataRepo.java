package com.substring.docmind.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.substring.docmind.entity.DocumentMetadata;
import com.substring.docmind.entity.DocumentStatus;

public interface DocumentMetaDataRepo extends JpaRepository<DocumentMetadata, UUID> {

    List<DocumentMetadata> findAllByStatus(DocumentStatus status);
    List<DocumentMetadata> findAllByOrderByCreatedAtDesc();
    
}
