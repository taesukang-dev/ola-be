package com.example.ola.controller;

import com.example.ola.dto.request.CommentWriteRequest;
import com.example.ola.dto.request.PostType;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CommentService commentService;

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_조회() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(get("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void 댓글_조회시_로그인하지_않은_경우() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(get("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_작성() throws Exception {
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
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 대댓글_작성() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void 댓글_작성시_로그인하지_않은_경우() throws Exception {
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

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_작성시_게시글이_없는_경우() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();
        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(commentService).writeComment(any(), any(),any(), any());
        // when then
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 대댓글_작성시_부모_댓글이_없는경우() throws Exception {
        // given
        CommentWriteRequest param = CommentWriteRequest.builder()
                .type(PostType.POST)
                .content("content")
                .build();

        doThrow(new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND))
                .when(commentService).writeComment(any(), any(),any(), any(), any());
        // when
        // then
        mockMvc.perform(post("/api/v1/posts/99/comments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_삭제() throws Exception {
        // given
        // when
        // then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_삭제시_게시글이_없는_경우() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(commentService).deleteComment(any(), any(),any());
        // when then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 댓글_삭제시_유저가_다른_경우() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(commentService).deleteComment(any(), any(),any());
        // when then
        mockMvc.perform(delete("/api/v1/posts/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }
}