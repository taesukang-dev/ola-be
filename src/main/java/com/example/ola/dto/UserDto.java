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
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String name;
    private Long ageRange;
    private String homeGym;
    private String gender;

    public static UserDto fromUser(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(), user.getName(), user.getAgeRange(), user.getHomeGym(), user.getUserGender().getName());
    }
}
