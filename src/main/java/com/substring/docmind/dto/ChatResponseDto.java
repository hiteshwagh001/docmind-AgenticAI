package com.substring.docmind.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {
    private String answer;
    private String conversationId;
    private List<CitationDto> citations;
    private Long responseTimeMs;
}
