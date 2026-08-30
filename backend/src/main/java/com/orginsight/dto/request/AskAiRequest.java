package com.orginsight.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AskAiRequest {

    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must be at most 500 characters")
    private String question;
}
