package com.example.ola.controller;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.TeamPostByLocationRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@AutoConfigureMockMvc
@SpringBootTest
class TeamPostControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TeamPostService teamPostService;

    @Test
    void ?????????_?????????_??????() throws Exception {
        // given
        when(teamPostService.findTeamPostByLocation(anyDouble(), anyDouble(), anyInt())).thenReturn(List.of());
        TeamPostByLocationRequest request = TeamPostByLocationRequest.builder()
                .x(3.14)
                .y(3.14)
                .build();
        // when then
        mockMvc.perform(post("/api/v1/posts/team/location?page=0").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ??????_?????????_?????????_??????() throws Exception {
        // given
        when(teamPostService.findTeamPostByUsername(any())).thenReturn(List.of());
        // when then
        mockMvc.perform(get("/api/v1/posts/team").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_??????() throws Exception {
        // given
        TeamPostWriteRequest param = Fixture.makeTeamPostWriteRequest("test1", "title1");
        TeamBuildingPost post = Fixture.makeTeamPostFixture("test1", "title1", 3.14, 3.14);
        when(teamPostService.writeTeamPost(any(), eq("test1")))
                .thenReturn(TeamPostDto.fromPost(post));
        // when then
        mockMvc.perform(post("/api/v1/posts/team").with(user(UserPrincipal.fromUser(post.getUser())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void ?????????_?????????_?????????_???????????????_??????_??????() throws Exception {
        // given
        TeamPostWriteRequest param = Fixture.makeTeamPostWriteRequest("test1", "title1");
        // when then
        mockMvc.perform(post("/api/v1/posts/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_?????????_???????????????_?????????_??????() throws Exception {
        // given
        // when then
        mockMvc.perform(post("/api/v1/posts/team").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void ?????????_?????????_??????() throws Exception {
        // given
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(1L, "title", "content", "img",
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                5L);

        when(teamPostService.updateTeamPost(any(), any())).thenReturn(TeamPostDto.fromPost(Fixture.makeTeamPostFixture("test1", "title", 3.14, 3.14)));
        // when then
        mockMvc.perform(put("/api/v1/posts/team").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_?????????_?????????_????????????_????????????() throws Exception {
        // given
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(1L, "title", "content", "img",
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                5L);

        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).updateTeamPost(any(), any());
        // when then
        mockMvc.perform(put("/api/v1/posts/team").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_?????????_????????????_????????????() throws Exception {
        // given
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(1L, "title", "content", "img",
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                5L);
        doThrow(new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .when(teamPostService).updateTeamPost(any(), any());
        // when then
        mockMvc.perform(put("/api/v1/posts/team").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(param))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void ?????????_?????????_??????() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/team/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void ?????????_?????????_?????????_???????????????_??????_??????() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/team/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @WithAnonymousUser
    @Test
    void ?????????_?????????_?????????_????????????_??????_??????() throws Exception {
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).removeTeamPost(any(), any());
        mockMvc.perform(delete("/api/v1/posts/team/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_????????????_??????() throws Exception {
        // given
        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/wait").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithAnonymousUser
    @Test
    void ?????????_?????????_????????????_?????????_?????????_????????????() throws Exception {
        // given
        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/wait"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_????????????_?????????_??????_??????_??????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER))
                .when(teamPostService).addWaitLists(anyLong(), any());
        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/wait").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void ?????????_?????????_???????????????_??????() throws Exception {
        // given
        // then when
        mockMvc.perform(delete("/api/v1/posts/team/1/wait/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_???????????????_?????????_?????????_????????????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).removeWaitListMember(anyLong(), anyLong(), any());
        // then when
        mockMvc.perform(delete("/api/v1/posts/team/1/wait/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_?????????_??????() throws Exception {
        // given
        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_?????????_?????????_????????????_????????????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).addMember(anyLong(), anyLong(), any());

        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_?????????_?????????_?????????_???_??????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.BAD_REQUEST))
                .when(teamPostService).addMember(anyLong(), anyLong(), any());

        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void ?????????_?????????_?????????_?????????_??????_?????????_??????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER))
                .when(teamPostService).addMember(anyLong(), anyLong(), any());

        // then when
        mockMvc.perform(post("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void ?????????_?????????_????????????_??????() throws Exception {
        // given
        // then when
        mockMvc.perform(delete("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_????????????_?????????_?????????_????????????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).removeTeamMember(anyLong(), anyLong(), any());
        // then when
        mockMvc.perform(delete("/api/v1/posts/team/1/member/1").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_??????_??????() throws Exception {
        // given
        // then when
        mockMvc.perform(get("/api/v1/posts/team/1/confirm").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void ?????????_?????????_??????_?????????_????????????_????????????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR))
                .when(teamPostService).confirmTeam(anyLong(), any());

        // then when
        mockMvc.perform(get("/api/v1/posts/team/1/confirm").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ?????????_?????????_??????_?????????_?????????_????????????_??????() throws Exception {
        // given
        doThrow(new OlaApplicationException(ErrorCode.MEMBERS_NOT_ENOUGH))
                .when(teamPostService).confirmTeam(anyLong(), any());

        // then when
        mockMvc.perform(get("/api/v1/posts/team/1/confirm").with(user(UserPrincipal.fromUser(Fixture.makeUserFixture("user1", "1q2w3e4r!!")))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}