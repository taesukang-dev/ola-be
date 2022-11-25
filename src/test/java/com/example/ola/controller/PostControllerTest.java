package com.example.ola.controller;

import com.example.ola.dto.PostDto;
import com.example.ola.dto.request.PostUpdateRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.service.PostService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PostService postService;

    @Test
    void 일반_게시글_목록_조회() throws Exception {
        // given
        when(postService.findAllPostsWithPaging(anyInt(), eq(""))).thenReturn(mock(MyPageResponse.class));
        // when then
        mockMvc.perform(get("/api/v2/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 일반_게시글_단건_조회() throws Exception {
        // given
        when(postService.findById(any())).thenReturn(PostDto.fromPost(Fixture.makeTeamPostFixture("user1", "title1")));
        // when then
        mockMvc.perform(get("/api/v2/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 일반_게시물_작성() throws Exception {
        // given
        PostWriteRequest param = PostWriteRequest.builder()
                .title("title")
                .content("content")
                .username("test1")
                .imgUri("img")
                .build();
        when(postService.write(any(), eq("test1")))
                .thenReturn(PostDto.fromPost(Fixture.makePostFixture("test1", "title1")));
        // when then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void 일반_게시물_작성시_로그인하지_않은_경우() throws Exception {
        // given
        PostWriteRequest param = PostWriteRequest.builder()
                .title("title")
                .content("content")
                .username("test1")
                .imgUri("img")
                .build();
        // when then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 일반_게시물_수정() throws Exception {
        // given
        PostUpdateRequest param = new PostUpdateRequest(1L, "updated", "updated", "testImg");

        when(postService.updatePost(any(), any())).thenReturn(PostDto.fromPost(Fixture.makeTeamPostFixture("test1", "title1")));
        // when then
        mockMvc.perform(put("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 일반_게시물_수정시_자신의_게시글이_아닌경우() throws Exception {
        // given
        PostUpdateRequest param = new PostUpdateRequest(1L, "updated", "updated", "testImg");

        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(postService).updatePost(any(), any());
        // when then
        mockMvc.perform(put("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 일반_게시물_수정시_게시글이_없는경우() throws Exception {
        // given
        PostUpdateRequest param = new PostUpdateRequest(1L, "updated", "updated", "testImg");

        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(postService).updatePost(any(), any());
        // when then
        mockMvc.perform(put("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithUserDetails(value = "test1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void 일반_게시물_삭제() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void 일반_게시물_삭제시_로그인하지_않은_경우() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithAnonymousUser
    @Test
    void 일반_게시물_삭제시_작성자와_다른_경우() throws Exception {
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(postService).delete(any(), any());
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


}