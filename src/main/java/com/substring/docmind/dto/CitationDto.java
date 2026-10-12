package com.substring.docmind.dto;

import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitationDto {

    private UUID documentId;
    private String fileName;
    private Integer chunkIndex;
    private Integer pageNumber;;
    private String snippet;
    private Double similarityScore;
    private Map<String, Object> metadata;

}
