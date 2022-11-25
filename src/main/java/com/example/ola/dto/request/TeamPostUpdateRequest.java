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

    public TeamPostUpdateRequest(Long id, String title, String content, String imgUri, HomeGymRequest homeGymRequest, Long limits) {
        super(id, title, content, imgUri);
        this.homeGymRequest = homeGymRequest;
        this.limits = limits;
    }
}
