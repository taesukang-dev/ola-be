package com.example.ola.service;

import com.example.ola.domain.HomeGym;
import com.example.ola.domain.User;
import com.example.ola.domain.UserRole;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.dto.request.UserUpdateRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.jwt.JwtTokenProvider;
import com.example.ola.repository.HomeGymRepository;
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
    private final HomeGymRepository homeGymRepository;
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
                                userRequest.getImgUri(),
                                passwordEncoder.encode(userRequest.getPassword()),
                                userRequest.getNickname(),
                                userRequest.getName(),
                                userRequest.getAgeRange(),
                                checkDuplicateHomeGymAndGetHomeGym(userRequest.getHomeGymRequest()),
                                userRequest.getGender())));
    }

    @Transactional
    public UserDto updateUser(String username, UserUpdateRequest updateParam) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        user.updateUser(updateParam.getName(), updateParam.getNickname(), checkDuplicateHomeGymAndGetHomeGym(updateParam.getHomeGymRequest()), updateParam.getImgUri());
        return UserDto.fromUser(user);
    }

    private HomeGym checkDuplicateHomeGymAndGetHomeGym(HomeGymRequest updateParam) {
        HomeGym homeGym = homeGymRepository.findByPlaceName(updateParam.getPlaceName())
                .orElseGet(() -> saveAndReturnHomeGym(updateParam));
        // 사명은 같아도 주소가 다른 경우
        if (!homeGym.getRoadAddressName().equals(updateParam.getRoadAddressName())) {
            homeGym = saveAndReturnHomeGym(updateParam);
        }
        return homeGym;
    }

    private HomeGym saveAndReturnHomeGym(HomeGymRequest homeGymRequest) {
        return homeGymRepository.save(
                HomeGym.of(
                        homeGymRequest.getPlaceName(),
                        homeGymRequest.getRoadAddressName(),
                        homeGymRequest.getCategoryName(),
                        homeGymRequest.getX(),
                        homeGymRequest.getY()));
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new OlaApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        return jwtTokenProvider.createToken(username, List.of(UserRole.USER.name()));
    }

    public UserDto findByUsername(String username) {
        return UserDto.fromUser(userRepository.findByUsername(username).orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)));
    }
}
