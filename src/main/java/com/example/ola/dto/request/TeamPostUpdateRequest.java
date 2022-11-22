package com.example.ola.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPostUpdateRequest extends PostUpdateRequest{
    @Getter private String place;
    @Getter private Long limits;
    private String imgUri;
}
