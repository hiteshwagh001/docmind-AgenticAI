package com.substring.docmind.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import com.substring.docmind.config.AppProperties;
import com.substring.docmind.dto.ChatRequestDto;
import com.substring.docmind.dto.ChatResponseDto;
import com.substring.docmind.dto.CitationDto;
import com.substring.docmind.dto.SearchRequestDto;
import com.substring.docmind.dto.SearchResultDto;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor

public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;

    private final AppProperties appProperties;

    private final ChatClient chatClient;

    public ChatResponseDto askQuestion(ChatRequestDto request) {

        long startTime = System.currentTimeMillis();
        log.info("Processing Query: '{}', scoped documentID : '{}'", request.getQuestion(), request.getDocumentId());

        List<Document> similarDocuments = this.retrieveReleventDocuments(
                request.getQuestion(), request.getDocumentId(), request.getTopK(), request.getMinSimilarity());

        List<CitationDto> citationDtos = similarDocuments.stream()
                .map(this::mapToCitation)
                .toList();

        String contextText = buildContextString(similarDocuments);

        String prompt = buildPrompt(request.getQuestion(), contextText);

        String answer = this.chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        long responseTime = System.currentTimeMillis() - startTime;
        return new ChatResponseDto(answer, request.getConversationId(), citationDtos, responseTime);

    }

    private String buildPrompt(@NotBlank(message = "Question can not be empty") String question, String contextText) {
        if (contextText != null && !contextText.isBlank()) {
            return String.format(
                    """
                            Document Context:
                            --------------------
                            %s
                            --------------------

                            User Message / Question: %s
                            Instructions:
                            - If the user's question relates to the document above, prioritize answering using that context and reference key sections.
                            - If the user is asking a general question, greeting, or discussing topics beyound the docunment context, respond helpfully  and conversationally using your general knowledge while weaving in relevant document context if applicable.
                            """,
                    contextText, question);

        } else {
            return String.format(
                    """
                            User Message / Question: %s
                            Instructions:
                            - Respond helpfully accurately and conversationally to the user's message using your broad knowledge and general context.
                            """,
                    question);
        }
    }

    public Flux<String> streamQuestionAnswer(ChatRequestDto requestDto) {
        log.info("Streaming query: '{}'", requestDto.getQuestion());
        List<Document> relevantDocuments = retrieveReleventDocuments(
                requestDto.getQuestion(),
                requestDto.getDocumentId(),
                requestDto.getTopK(),
                requestDto.getMinSimilarity());

        String contextText = buildContextString(relevantDocuments);

        String prompt = buildPrompt(requestDto.getQuestion(), contextText);
        return chatClient
                .prompt()
                .user(prompt)
                .stream()
                .content();
    }

    private String buildContextString(List<Document> similarDocuments) {
        if (similarDocuments == null || similarDocuments.isEmpty())
            return "";

        return similarDocuments.stream()
                .map(doc -> {
                    String fileName = (String) doc.getMetadata().getOrDefault("fileName", "Unknown file");
                    Object page = doc.getMetadata().getOrDefault("pageNumber", "N/A");
                    return String.format("[Source: %s | Page: %s]\n%s", fileName, page, doc.getText());
                })
                .collect(Collectors.joining("\n\n--\n\n"));
    }

    public SearchResultDto searchSimilarChunks(SearchRequestDto request) {

        List<Document> matchedDocs = retrieveReleventDocuments(
                request.getQuery(),
                request.getDocumentId(),
                request.getTopK(),
                request.getSimilaritySearch());

        List<CitationDto> citations = matchedDocs.stream()
                .map(this::mapToCitation)
                .toList();

        return SearchResultDto.builder()
                .query(request.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();

    }

    private CitationDto mapToCitation(Document document) {
        Map<String, Object> meta = document.getMetadata();
        UUID docId = null;
        if (meta.get("documentId") != null) {
            try {
                docId = UUID.fromString(meta.get("documentId").toString());
            } catch (Exception e) {
                log.error("Invalid UUID format for documentId: {}", meta.get("documentId"), e);
            }
        }

        Integer chunkIndex = null;
        if (meta.get("chunkIndex") != null) {
            try {
                chunkIndex = Integer.parseInt(meta.get("chunkIndex").toString());
            } catch (NumberFormatException e) {
                log.error("Invalid number format for chunkIndex: {}", meta.get("chunkIndex"), e);
            }
        }
        Integer pageNumber = null;
        if (meta.get("pageNumber") != null) {
            try {
                pageNumber = Integer.parseInt(meta.get("pageNumber").toString());
            } catch (NumberFormatException e) {
                log.error("Invalid number format for pageNumber: {}", meta.get("pageNumber"), e);
            }
        }

        Double score = null;
        if (meta.get("distance") instanceof Number n) {
            score = 1.0 - n.doubleValue();
        }
        return CitationDto.builder()
                .documentId(docId)
                .fileName((String) meta.getOrDefault("fileName", "unknown"))
                .chunkIndex(chunkIndex)
                .pageNumber(pageNumber)
                .snippet(document.getText())
                .similarityScore(score)
                .metadata(meta)
                .build();
    }

    private List<Document> retrieveReleventDocuments(@NotBlank(message = "Query cannot be blank") String query,
            UUID documentId, Integer topK,
            Double similaritySearch) {
        int effectiveTopK = (topK != null && topK > 0) ? topK : appProperties.getRag().getTopK();
        double effectiveSimilarity = (similaritySearch != null) ? similaritySearch
                : appProperties.getRag().getSimilarityThreshold();

        SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                .query(query)
                .topK(effectiveTopK);

        if (effectiveSimilarity > 0.0) {
            searchRequestBuilder.similarityThreshold(effectiveSimilarity);
        }

        if (documentId != null) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            searchRequestBuilder.filterExpression(b.eq("documentId", documentId.toString()).build());
        }

        try {
            List<Document> matchedDocs = vectorStore.similaritySearch(searchRequestBuilder.build());
            log.info("Retrieved {} relevant documents for query: {}, documentId: {}, topK: {}, similaritySearch: {}",
                    matchedDocs.size(), query,
                    documentId, effectiveTopK, effectiveSimilarity);
            return matchedDocs;
        } catch (Exception e) {
            log.error(
                    "Error occurred while retrieving relevant documents for query: {}, documentId: {}, topK: {}, similaritySearch: {}. Error: {}",
                    query, documentId, effectiveTopK, effectiveSimilarity, e.getMessage(), e);
            return Collections.emptyList();
        }

    }

}
