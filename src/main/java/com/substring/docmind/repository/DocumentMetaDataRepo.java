package com.substring.docmind.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.substring.docmind.entity.DocumentMetadata;

public interface DocumentMetaDataRepo extends JpaRepository<DocumentMetadata, UUID> {
    
}
