package com.example.ola.controller;

import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.UserRequest;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;

    @Test
    void 회원가입() throws Exception {
        // given
        UserRequest userRequest = new UserRequest("username", "1q2w3e4r!!", "nick1", "name1", 20L, "none");

        // when
        when(userService.join(userRequest)).thenReturn(mock(UserDto.class));
        // then
        mockMvc.perform(
                        post("/api/v1/users/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userRequest))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 회원가입시_중복인_경우() throws Exception {
        // given
        UserRequest userRequest = new UserRequest("username", "1q2w3e4r!!", "nick1", "name1", 20L, "none");
        // when
        when(userService.join(userRequest)).thenThrow(new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER));
        // then
        mockMvc.perform(
                        post("/api/v1/users/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(userRequest))
                ).andDo(print())
                .andExpect(status().isConflict());
    }
}