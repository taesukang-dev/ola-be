package com.example.ola.controller;

import com.example.ola.dto.request.UserLoginRequest;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.UserJoinResponse;
import com.example.ola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody @Valid UserRequest userRequest) {
        return Response.success(UserJoinResponse.fromUserDto(userService.join(userRequest)));
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody UserLoginRequest request) {
        return Response.success(userService.login(request.getUsername(), request.getPassword()));
    }

}
