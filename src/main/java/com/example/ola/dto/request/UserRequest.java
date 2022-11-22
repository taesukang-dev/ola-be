package com.example.ola.dto.request;

import com.example.ola.domain.UserGender;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "공백이어서는 안 됩니다.")
    private String username;
    private String imgUri;
    @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "비밀번호는 최소 8자로 하나의 영어 소문자와 하나의 특수문자를 포함해야 합니다.")
    private String password;
    @NotBlank(message = "닉네임을 확인하세요.")
    private String nickname;
    @NotBlank(message = "이름을 확인하세요.")
    private String name;
    private Long ageRange;
    private String homeGym;
    private String gender;
}

