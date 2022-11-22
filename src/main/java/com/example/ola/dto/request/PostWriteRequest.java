package com.example.ola.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostWriteRequest {
    private String title;
    private String content;
    private String username;
    private String imgUri;
}
