package com.example.ola.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "공백이어서는 안 됩니다.")
    private String username;
    @Pattern(regexp="[a-zA-Z1-9]{6,12}", message = "비밀번호는 영어와 숫자로 포함해서 6~12자리 이내로 입력해주세요.")
    private String password;
    @NotBlank(message = "닉네임을 확인하세요.")
    private String nickname;
    @NotBlank(message = "이름을 확인하세요.")
    private String name;
    @NotBlank(message = "연령대를 확인하세요")
    private int ageRange;
    private String homeGym;
}
