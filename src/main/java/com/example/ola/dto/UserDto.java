package com.example.ola.dto;

import com.example.ola.domain.HomeGym;
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
    private String imgUri;
    private String password;
    private String nickname;
    private String name;
    private Long ageRange;
    private HomeGymDto homeGymDto;
    private String gender;

    public static UserDto fromUser(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getImgUri(),
                user.getPassword(),
                user.getNickname(),
                user.getName(),
                user.getAgeRange(),
                HomeGymDto.fromHomeGym(user.getHomeGym()),
                user.getUserGender().getName());
    }
}
