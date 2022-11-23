package com.example.ola.dto.request;

import com.example.ola.domain.HomeGym;
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
    private HomeGymRequest homeGymRequest;
    private Long limits;
    private String imgUri;
}
