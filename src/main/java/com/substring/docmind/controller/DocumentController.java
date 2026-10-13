package com.substring.docmind.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.substring.docmind.dto.ApiResponse;
import com.substring.docmind.dto.DocumentMetadataDto;
import com.substring.docmind.dto.DocumentResponseDto;
import com.substring.docmind.service.DocumentMetaDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Document Management APIs")
public class DocumentController {

        private final DocumentMetaDataService documentService;

        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "upload and index a document", description = "This api is used for the upload and indexing of a document")
        public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
                        @RequestParam("file") MultipartFile file) {

                DocumentResponseDto documentResponseDto = documentService.uploadAndProcess(file);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<DocumentResponseDto>builder()
                                                .success(true)
                                                .data(documentResponseDto)
                                                .timestamp(LocalDateTime.now())
                                                .message("Document uploaded and indexed successfully")
                                                .build());
        }

        @Operation(summary = "Upload and index multiple document at a time", description = "This api is used for the upload and indexing of multiple documents at a time")
        @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultipleDocuments(
                        @RequestParam("files") List<MultipartFile> files) {
                List<DocumentResponseDto> responseDtos = documentService.uploadMultipleDocument(files);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<List<DocumentResponseDto>>builder()
                                                .success(true)
                                                .data(responseDtos)
                                                .timestamp(LocalDateTime.now())
                                                .message("Documents uploaded and indexed successfully")
                                                .build());

        }

        // list of all documents
        @GetMapping
        @Operation(summary = "Get all documents", description = "This api is used to get all documents")

        public ResponseEntity<ApiResponse<List<DocumentMetadataDto>>> getAllDocuments() {
                List<DocumentMetadataDto> responseDtos = documentService.getAllDocuments();

                return ResponseEntity.ok(
                                ApiResponse.<List<DocumentMetadataDto>>builder()
                                                .message("Documents fetched successfully")
                                                .success(true)
                                                .data(responseDtos)
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get document by id", description = "This api is used to get document by id")
        public ResponseEntity<ApiResponse<DocumentMetadataDto>> getDocumentById(@PathVariable UUID id) {
                DocumentMetadataDto documentResponseDto = documentService.getDocumentById(id);
                return ResponseEntity.ok(ApiResponse.<DocumentMetadataDto>builder()
                                .message("Document fetched successfully")
                                .success(true)
                                .data(documentResponseDto)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete document by id", description = "This api is used to delete document by id")
        public ResponseEntity<ApiResponse<Void>> deleteDocumentById(@PathVariable UUID id) {
                documentService.deleteDocumentById(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Document deleted successfully")
                                .success(true)
                                .timestamp(LocalDateTime.now())
                                .data(null)
                                .build());
        }

}
