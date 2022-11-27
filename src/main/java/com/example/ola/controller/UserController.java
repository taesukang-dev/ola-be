package com.example.ola.controller;

import com.example.ola.dto.request.UserLoginRequest;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.dto.request.UserUpdateRequest;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.UserResponse;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.AlarmService;
import com.example.ola.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;
    private final AlarmService alarmService;

    /**
     * 유저 정보 조회
     * @param userPrincipal
     * @return Response<UserResponse>
     */
    @GetMapping
    public Response<UserResponse> userInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(UserResponse.fromUserPrincipal(userPrincipal));
    }

    /**
     * 회원 가입
     * @param userRequest
     * @return Response<UserResponse>
     */
    @PostMapping("/join")
    public Response<UserResponse> join(@RequestBody @Valid UserRequest userRequest) {
        return Response.success(UserResponse.fromUserDto(userService.join(userRequest)));
    }

    /**
     * 로그인
     * @param request
     * @return Response<String>
     */
    @PostMapping("/login")
    public Response<String> login(@RequestBody UserLoginRequest request) {
        return Response.success(userService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * 유저 정보 수정
     * @param request
     * @param userPrincipal
     * @return Response<UserResponse>
     */
    @PostMapping("/update")
    public Response<UserResponse> update(
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(UserResponse.fromUserDto(userService.updateUser(userPrincipal.getUsername(), request)));
    }

    /**
     * Sse 구독
     * @param userPrincipal
     * @return SseEmitter
     */
    @GetMapping("/alarm/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return alarmService.connectAlarm(userPrincipal.getId());
    }
}
