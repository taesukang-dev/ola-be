package com.example.ola.service;

import com.example.ola.domain.User;
import com.example.ola.domain.UserRole;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.dto.request.UserUpdateRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.jwt.JwtTokenProvider;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDto join(UserRequest userRequest) {
        userRepository.findByUsername(userRequest.getUsername()).ifPresent(it -> {
            throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
        });
        return UserDto.fromUser(
                userRepository.save(
                        User.of(
                                userRequest.getUsername(),
                                passwordEncoder.encode(userRequest.getPassword()),
                                userRequest.getNickname(),
                                userRequest.getName(),
                                userRequest.getAgeRange(),
                                userRequest.getHomeGym(),
                                userRequest.getGender())));
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new OlaApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        return jwtTokenProvider.createToken(username, List.of(UserRole.USER.name()));
    }

    @Transactional
    public UserDto updateUser(String username, UserUpdateRequest updateParam) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        user.updateUser(updateParam.getName(), updateParam.getNickname(), updateParam.getHomeGym());
        return UserDto.fromUser(user);
    }

    public UserDto findByUsername(String username) {
        return UserDto.fromUser(userRepository.findByUsername(username).orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)));
    }
}
