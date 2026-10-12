package com.substring.docmind.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.substring.docmind.config.AppProperties;
import com.substring.docmind.entity.DocumentMetadata;
import com.substring.docmind.entity.DocumentStatus;
import com.substring.docmind.exception.DocumentProcessingException;
import com.substring.docmind.repository.DocumentMetaDataRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final DocumentMetaDataRepo documentMetadataRepo;
    private final AppProperties appProperties;

    public int ingest(DocumentMetadata metaData, List<Document> parsedDoc) {
        log.info("Ingesting document [id={}, name={}, pages={}]", metaData.getId(), metaData.getFilename(),
                parsedDoc.size());

        try {
            metaData.setStatus(DocumentStatus.PROCESSING);
            metaData.setTotalPages(parsedDoc.size());

            // 1. text chunking using tokentext splitter
            TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunkSize())
                    .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
                    .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
                    .withKeepSeparator(true)
                    .build();

            List<Document> chunks = tokenTextSplitter.apply(parsedDoc);
            if (chunks.isEmpty()) {
                metaData.setStatus(DocumentStatus.FAILED);
                metaData.setErrorMessage("Document appears to be empty or unscannble");
                documentMetadataRepo.save(metaData);
                return 0;
            }

            // 2. metadata enrichment on each chunk
            List<Document> enrichedChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                Map<String, Object> enrichedMetadata = new HashMap<>(chunk.getMetadata());
                enrichedMetadata.put("documentId", metaData.getId());
                enrichedMetadata.put("filename", metaData.getFilename());
                enrichedMetadata.put("chunkIndex", i);
                enrichedMetadata.put("contentType", metaData.getContentType());

                // preserve or calculate parse number if available
                Object pageNumber = chunk.getMetadata().get("page_number");
                if (pageNumber == null) {
                    pageNumber = chunk.getMetadata().get("pageNumber");
                }
                if (pageNumber != null) {
                    enrichedMetadata.put("pageNumber", pageNumber);
                }
                Document enrichedDoc = new Document(chunk.getText(), enrichedMetadata);
                enrichedChunks.add(enrichedDoc);
            }

            // 3. write chunck and embedding to pg vector

            log.info("Writing {} vector to pgVector store for document: {}", enrichedChunks.size(),
                    metaData.getFilename());
            vectorStore.add(enrichedChunks);

            // 4. update document status to index
            metaData.setStatus(DocumentStatus.INDEXED);
            metaData.setTotalChunks(enrichedChunks.size());
            metaData.setErrorMessage(null);
            documentMetadataRepo.save(metaData);
            log.info("Successfully ingested document [id={}, name={}, totalChunks={}]", metaData.getId(),
                    metaData.getFilename(), enrichedChunks.size());
            return enrichedChunks.size();
        } catch (Exception e) {
            log.error("Error occurred while ingesting document [name={}]", metaData.getFilename(), e);
            metaData.setStatus(DocumentStatus.FAILED);
            metaData.setErrorMessage(e.getMessage());
            documentMetadataRepo.save(metaData);
            throw new DocumentProcessingException(
                    "Error occurred while ingesting document [name=" + metaData.getFilename() + "]", e);
        }

    }
}