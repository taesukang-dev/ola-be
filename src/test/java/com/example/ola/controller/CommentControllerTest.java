package com.example.ola.controller;

import com.example.ola.dto.request.CommentWriteRequest;
import com.example.ola.dto.request.PostType;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.service.CommentService;
import com.example.ola.service.TeamPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CommentService commentService;

    @Test
    void ๋๊ธ_์กฐํ() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(get("/api/v1/posts/1/comments").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void ๋๊ธ_์กฐํ์_๋ก๊ทธ์ธํ์ง_์์_๊ฒฝ์ฐ() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(get("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ๋๊ธ_์์ฑ() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/1/comments").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ๋๋๊ธ_์์ฑ() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/1/comments/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void ๋๊ธ_์์ฑ์_๋ก๊ทธ์ธํ์ง_์์_๊ฒฝ์ฐ() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ๋๊ธ_์์ฑ์_๊ฒ์๊ธ์ด_์๋_๊ฒฝ์ฐ() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(commentService).writeComment(any(), any(),any(), any());
        // when then
        mockMvc.perform(post("/api/v1/posts/1/comments").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void ๋๋๊ธ_์์ฑ์_๋ถ๋ชจ_๋๊ธ์ด_์๋๊ฒฝ์ฐ() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();

        doThrow(new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND))
                .when(commentService).writeComment(any(), any(),any(), any(), any());
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/99/comments/99").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void ๋๊ธ_์ญ์?() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ๋๊ธ_์ญ์?์_๊ฒ์๊ธ์ด_์๋_๊ฒฝ์ฐ() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(commentService).deleteComment(any(), any(),any());
        // when then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void ๋๊ธ_์ญ์?์_์?์?๊ฐ_๋ค๋ฅธ_๊ฒฝ์ฐ() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(commentService).deleteComment(any(), any(),any());
        // when then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }
}