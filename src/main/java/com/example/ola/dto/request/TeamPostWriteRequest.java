package com.example.ola.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamPostWriteRequest {
    private String title;
    private String content;
    private String username;
    private String place;
    private Long limits;
    private String imgUri;
}
