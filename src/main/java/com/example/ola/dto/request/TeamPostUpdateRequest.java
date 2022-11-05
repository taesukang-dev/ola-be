package com.example.ola.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TeamPostUpdateRequest extends PostUpdateRequest{
    public TeamPostUpdateRequest(Long id, String title, String content, String place) {
        super(id, title, content);
        this.place = place;
    }

    @Getter private String place;
}
