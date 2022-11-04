package com.example.ola.service;

import com.example.ola.domain.User;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDto join(UserRequest userRequest) {
        userRepository.findByUsername(userRequest.getUsername()).ifPresent(it -> {
            throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
        });
        return UserDto.fromUser(
                userRepository.save(
                        User.of(
                                userRequest.getUsername(),
                                userRequest.getPassword(),
                                userRequest.getNickname(),
                                userRequest.getName(),
                                userRequest.getAgeRange(),
                                userRequest.getHomeGym())));
    }

    public UserDto findByUserName(String username) {
        return UserDto.fromUser(userRepository.findByUsername(username).orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)));
    }
}
