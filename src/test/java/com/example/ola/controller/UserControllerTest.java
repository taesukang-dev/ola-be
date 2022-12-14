package com.example.ola.controller;

import com.example.ola.domain.UserGender;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.UserLoginRequest;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.dto.request.UserUpdateRequest;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.service.AlarmService;
import com.example.ola.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;
    @MockBean AlarmService alarmService;

    @Test
    void ??????_??????_??????() throws Exception {
        // given
        // when then
        mockMvc.perform(get("/api/v1/users").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ????????????() throws Exception {
        // given
        UserRequest param = new UserRequest("test2", "img", "1q2w3e4r!!", "nick", "name", 20L,
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                UserGender.F.getName());
        // when
        when(userService.join(any())).thenReturn(mock(UserDto.class));
        // then
        mockMvc.perform(
                        post("/api/v1/users/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ???????????????_?????????_??????() throws Exception {
        // given
        UserRequest param = new UserRequest("test2", "img", "1q2w3e4r!!", "nick", "name", 20L,
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                UserGender.F.getName());
        // when
        when(userService.join(param)).thenThrow(new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER));
        // then
        mockMvc.perform(
                        post("/api/v1/users/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void ?????????() throws Exception {
        String username = "username";
        String password = "password";
        // given
        when(userService.login(username, password)).thenReturn("test token");
        // when then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ????????????_???????????????_??????_?????????_??????() throws Exception {
        String username = "username";
        String password = "password";
        // given
        when(userService.login(username, password)).thenThrow(new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        // when then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void ????????????_???????????????_??????_??????() throws Exception {
        String username = "username";
        String password = "password";
        // given
        when(userService.login(username, password)).thenThrow(new OlaApplicationException(ErrorCode.INVALID_PASSWORD));
        // when then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserLoginRequest(username, password))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ??????_??????_??????() throws Exception {
        // given
        UserUpdateRequest request = UserUpdateRequest
                .builder()
                .name("update")
                .imgUri("img")
                .nickname("nick")
                .homeGymRequest(
                        HomeGymRequest.builder()
                                .placeName("place")
                                .roadAddressName("road")
                                .categoryName("cate")
                                .x(3.14)
                                .y(3.14)
                                .build())
                .build();
        when(userService.updateUser(anyString(), any()))
                .thenReturn(UserDto.fromUser(Fixture.makeUserFixture("user1", "name1")));

        // when then
        mockMvc.perform(post("/api/v1/users/update").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ??????_??????_?????????_?????????_????????????() throws Exception {
        // given
        UserUpdateRequest request = UserUpdateRequest
                .builder()
                .name("update")
                .imgUri("img")
                .nickname("nick")
                .homeGymRequest(
                        HomeGymRequest.builder()
                                .placeName("place")
                                .roadAddressName("road")
                                .categoryName("cate")
                                .x(3.14)
                                .y(3.14)
                                .build())
                .build();
        doThrow(new OlaApplicationException(ErrorCode.USER_NOT_FOUND))
                .when(userService).updateUser(anyString(), any());

        // when then
        mockMvc.perform(post("/api/v1/users/update").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void Sse_??????() throws Exception {
        // given
        when(alarmService.connectAlarm(any()))
                .thenReturn(new SseEmitter());
        // when then
        mockMvc.perform(get("/api/v1/users/alarm/subscribe?token=temp").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void Sse_?????????_?????????_??????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.ALARM_CONNECT_ERROR))
                .when(alarmService).connectAlarm(any());
        // when then
        mockMvc.perform(get("/api/v1/users/alarm/subscribe?token=temp").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}