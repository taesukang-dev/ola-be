package com.example.ola.dto.response;

import com.example.ola.domain.HomeGym;
import com.example.ola.dto.HomeGymDto;
import com.example.ola.dto.UserDto;

import com.example.ola.dto.security.UserPrincipal;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String userId;
    private String imgUri;
    private String nickname;
    private String name;
    private Long ageRange;
    private HomeGymDto homeGym;
    private String userGender;

    public static UserResponse fromUserDto(UserDto userDto) {
        return new UserResponse(userDto.getId(), userDto.getUsername(), userDto.getImgUri(), userDto.getNickname(), userDto.getName(), userDto.getAgeRange(), userDto.getHomeGymDto(), userDto.getGender());
    }

    public static UserResponse fromUserPrincipal(UserPrincipal userPrincipal) {
        return new UserResponse(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getImgUri(), userPrincipal.getNickname(), userPrincipal.getName(), userPrincipal.getAgeRange(), userPrincipal.getHomeGym(), userPrincipal.getUserGender());
    }
}
