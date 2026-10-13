package com.substring.docmind.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.substring.docmind.dto.DocumentMetadataDto;
import com.substring.docmind.dto.DocumentResponseDto;
import com.substring.docmind.entity.DocumentMetadata;
import com.substring.docmind.entity.DocumentStatus;
import com.substring.docmind.exception.ResourceNotFoundException;
import com.substring.docmind.repository.DocumentMetaDataRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentMetaDataService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetaDataService.class);

    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;
    private final JdbcTemplate jdbcTemplate;
    private final ModelMapper modelMapper;

    public DocumentResponseDto uploadAndProcess(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // document meta data create
        DocumentMetadata documentMetadata = DocumentMetadata.builder()
                .filename(fileName)
                .contentType(contentType)
                .fileSize(file.getSize())
                .status(DocumentStatus.UPLOADING)
                .createdAt(LocalDateTime.now())
                .build();

        // save the document meta data
        documentMetadata = documentMetaDataRepo.save(documentMetadata);

        // parse the file
        List<Document> parseDoc = parserService.parse(file);

        // ingest service
        int chunkCreated = ingestionService.ingest(documentMetadata, parseDoc);

        return DocumentResponseDto.builder()
                .id(documentMetadata.getId())
                .fileName(documentMetadata.getFilename())
                .fileSize(documentMetadata.getFileSize())
                .status(documentMetadata.getStatus())
                .chunkCreated(chunkCreated)
                .message("Document successfully processed and indexed successfully")
                .build();
    }

    public List<DocumentResponseDto> uploadMultipleDocument(List<MultipartFile> files) {
        List<DocumentResponseDto> responseDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            DocumentResponseDto result = this.uploadAndProcess(file);
            responseDtos.add(result);
        }
        return responseDtos;
    }

    public List<DocumentMetadataDto> getAllDocuments() {
        List<DocumentMetadata> allDocuments = documentMetaDataRepo.findAllByOrderByCreatedAtDesc();
        allDocuments.stream()
                .map(documentMetadata -> modelMapper.map(documentMetadata, DocumentMetadataDto.class))
                .toList();
        return null;

    }

    public DocumentMetadataDto getDocumentById(UUID id) {

        DocumentMetadata documentMetadata = documentMetaDataRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));
        return modelMapper.map(documentMetadata, DocumentMetadataDto.class);
    }

    public void deleteDocumentById(UUID id) {

        DocumentMetadata documentMetadata = documentMetaDataRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id " + id));

                // delete vector entries

                try {
                    String deleteVectorSql = "DELETE FROM vector_store WHERE metadata->>'documentId'=?";
                    int deletedCount = jdbcTemplate.update(deleteVectorSql, id.toString());
                    log.info("Deleted {} vector chunks for document id {}", deletedCount,id);
                } catch (Exception e) {
                    log.warn("Could not delete vector  from vector store directly : {}", e.getMessage());
                }
    }

}
