package com.example.ola.dto.request;

import com.example.ola.domain.HomeGym;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    private String name;
    private String imgUri;
    private String nickname;
    private HomeGymRequest homeGymRequest;
}
