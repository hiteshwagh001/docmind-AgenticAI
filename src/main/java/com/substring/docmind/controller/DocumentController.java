package com.substring.docmind.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.substring.docmind.dto.ApiResponse;
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

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultipleDocuments(
            @RequestParam("files") List<MultipartFile> files) {
        List<DocumentResponseDto > responseDtos = documentService.uploadMultipleDocument(files);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<List<DocumentResponseDto>>builder()
                        .success(true)
                        .data(responseDtos)
                        .timestamp(LocalDateTime.now())
                        .message("Documents uploaded and indexed successfully")
                        .build());

    }
}
