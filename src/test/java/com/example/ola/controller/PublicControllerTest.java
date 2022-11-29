package com.example.ola.controller;

import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.fixture.Fixture;
import com.example.ola.service.PostService;
import com.example.ola.service.TeamPostService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class PublicControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PostService postService;
    @MockBean TeamPostService teamPostService;

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
    void 일반_게시글_목록_검색어로_조회() throws Exception {
        // given
        String keyword = "key";
        when(postService.findAllPostsByKeyword(keyword)).thenReturn(mock(MyPageResponse.class));
        // when then
        mockMvc.perform(get("/api/v2/posts?page=0&keyword="+keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 일반_게시글_단건_조회() throws Exception {
        // given
        when(postService.findById(any())).thenReturn(PostDto.fromPost(Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14)));
        // when then
        mockMvc.perform(get("/api/v2/posts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 팀빌딩_게시글_목록_조회() throws Exception {
        // given
        when(teamPostService.findAllTeamPostsWithPaging(0, "", "")).thenReturn(mock(MyPageResponse.class));
        // when then
        mockMvc.perform(get("/api/v2/posts/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 팀빌딩_게시글_목록_키워드로_조회() throws Exception {
        // given
        String keyword = "asd";
        when(teamPostService.findAllTeamPostsByKeyword(eq(keyword))).thenReturn(mock(MyPageResponse.class));
        // when then
        mockMvc.perform(get("/api/v2/posts/team?page=0&keyword=" + keyword +"&place=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 팀빌딩_게시글_목록_장소_조회() throws Exception {
        // given
        String keyword = "asd";
        when(teamPostService.findAllTeamPostsByPlace(eq(keyword))).thenReturn(mock(MyPageResponse.class));
        // when then
        mockMvc.perform(get("/api/v2/posts/team?page=0&keyword=" + keyword +"&place=장소")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 팀빌딩_게시글_단건_조회() throws Exception {
        // given
        when(teamPostService.findTeamPostById(any())).thenReturn(TeamPostDto.fromPost(Fixture.makeTeamPostFixture("test1", "title1", 3.14, 3.14)));
        // when then
        mockMvc.perform(get("/api/v2/posts/team/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void 팀_빌딩_게시물_대기열_조회() throws Exception {
        // given
        when(teamPostService.getWaitLists(any())).thenReturn(List.of());
        // when then
        mockMvc.perform(get("/api/v2/posts/team/1/wait")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

}