package com.example.ola.dto;

import com.example.ola.domain.User;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private String username;
    private String password;
    private String nickname;
    private String name;
    private int ageRange;
    private String homeGym;

    public static UserDto fromUser(User user) {
        return new UserDto(user.getUsername(), user.getPassword(), user.getNickname(), user.getName(), user.getAgeRange(), user.getHomeGym());
    }
}
