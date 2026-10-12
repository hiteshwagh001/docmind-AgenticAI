package com.substring.docmind.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.substring.docmind.exception.DocumentProcessingException;

import jakarta.annotation.Nullable;

@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    public List<Document> parse(MultipartFile file) {

        String fileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "document";

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase()
                : "";

        log.info("Parsing document: {}, size: {}, contentType: {}",
                fileName,
                file.getSize(),
                contentType);

        try {

            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public @Nullable String getFilename() {
                    return fileName;
                }
            };

            if (fileName.toLowerCase().endsWith(".pdf")
                    || contentType.equals("application/pdf")) {
                return parsePdf(resource);
            }

            return parseGenericFile(resource);

        } catch (IOException e) {

            log.error("Failed to read file bytes {}", fileName, e);

            throw new DocumentProcessingException(
                    "Could not read uploaded file: " + fileName,
                    e);
        }
    }

    private List<Document> parsePdf(Resource resource) {

        try {

            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageBottomMargin(0)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource, config);

            return reader.read();

        } catch (Exception e) {

            log.error("Error while parsing PDF: {}",
                    resource.getFilename(), e);

            throw new DocumentProcessingException(
                    "Failed to parse PDF: " + resource.getFilename(),
                    e);
        }
    }

    private List<Document> parseGenericFile(Resource resource) {

        try {

            TikaDocumentReader reader = new TikaDocumentReader(resource);

            return reader.read();

        } catch (Exception e) {

            log.error("Error while parsing file: {}",
                    resource.getFilename(), e);

            throw new DocumentProcessingException(
                    "Failed to parse file: " + resource.getFilename(),
                    e);
        }
    }
}