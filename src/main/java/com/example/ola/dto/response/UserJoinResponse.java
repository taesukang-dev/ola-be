package com.example.ola.dto.response;

import com.example.ola.dto.UserDto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserJoinResponse {
    private Long id;
    private String username;
    private String nickname;
    private String name;
    private Long ageRange;
    private String homeGym;

    public static UserJoinResponse fromUserDto(UserDto userDto) {
        return new UserJoinResponse(userDto.getId(), userDto.getUsername(), userDto.getNickname(), userDto.getName(), userDto.getAgeRange(), userDto.getHomeGym());
    }
}
