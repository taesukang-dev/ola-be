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

    @GetMapping
    public Response<UserResponse> userInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(UserResponse.fromUserPrincipal(userPrincipal));
    }

    @PostMapping("/join")
    public Response<UserResponse> join(@RequestBody @Valid UserRequest userRequest) {
        return Response.success(UserResponse.fromUserDto(userService.join(userRequest)));
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody UserLoginRequest request) {
        return Response.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/update")
    public Response<UserResponse> update(
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(UserResponse.fromUserDto(userService.updateUser(userPrincipal.getUsername(), request)));
    }

    @GetMapping("/alarm/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return alarmService.connectAlarm(userPrincipal.getId());
    }
}
