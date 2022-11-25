package com.example.ola.service;

import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.domain.User;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.TeamPostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
class TeamPostServiceTest {
    @Autowired TeamPostService teamPostService;
    @MockBean TeamPostRepository teamPostRepository;
    @MockBean UserRepository userRepository;
    @MockBean CommentRepository commentRepository;
    private static MockedStatic<Paging> paging;

    @BeforeAll
    public static void beforeAll() {
        paging = mockStatic(Paging.class);
    }

    @AfterAll
    public static void afterAll() {
        paging.close();
    }

    @Test
    void 팀빌딩_게시물_작성() throws Exception {
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        User user = post.getUser();
        TeamPostWriteRequest postWriteRequest = Fixture.makeTeamPostWriteRequest(user.getUsername(), post.getTitle());
        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(teamPostRepository.saveTeamPost(any())).thenReturn(post);
        // then
        TeamPostDto teamPost = teamPostService.writeTeamPost(postWriteRequest, "user1");
        assertThat(teamPost.getUserDto().getUsername()).isEqualTo("user1");
        assertThat(teamPost.getTitle()).isEqualTo("title1");
        assertThat(teamPost.getHomeGymDto().getX()).isEqualTo(3.14);
    }

    @Test
    void 팀빌딩_게시물_작성_작성자와_로그인유저가_다른경우() throws Exception {
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        User user = post.getUser();
        TeamPostWriteRequest postWriteRequest = Fixture.makeTeamPostWriteRequest(user.getUsername(), post.getTitle());

        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(teamPostRepository.saveTeamPost(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> teamPostService.writeTeamPost(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_작성시_유저가_존재하지_않는경우() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        User user = post.getUser();
        TeamPostWriteRequest postWriteRequest = Fixture.makeTeamPostWriteRequest(user.getUsername(), post.getTitle());
        // when
        when(teamPostRepository.saveTeamPost(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> teamPostService.writeTeamPost(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_조회() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        User user = post.getUser();
        TeamPostWriteRequest postWriteRequest = Fixture.makeTeamPostWriteRequest(user.getUsername(), post.getTitle());

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(teamPostRepository.saveTeamPost(any())).thenReturn(post);

        TeamPostDto teamPost = teamPostService.writeTeamPost(postWriteRequest, "user1");
        // when
        when(teamPostRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // then
        TeamPostDto foundedPost = teamPostService.findTeamPostById(post.getId());
        assertThat(foundedPost).isEqualTo(teamPost);
    }

    @Test
    void 팀빌딩_게시물_페이징_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i);
            temp.add(post);
        }
        // when
        when(teamPostRepository.findAllTeamPostsWithPaging(anyInt())).thenReturn(Optional.of(temp));
        when(teamPostRepository.getPostCount(eq("post"))).thenReturn(1L);
        when(Paging.getPageList(anyInt(), anyInt(), anyInt())).thenReturn(List.of(1, 2));
        // then
        MyPageResponse allTeamPostsWithPaging = teamPostService.findAllTeamPostsWithPaging(0, "", "");
        List<TeamPostResponse> contents = (List<TeamPostResponse>) allTeamPostsWithPaging.getContents();
        List<Integer> pageList = allTeamPostsWithPaging.getPageList();
        assertThat(contents.size()).isEqualTo(10);
        assertThat(pageList.size()).isEqualTo(2);
    }

    @Test
    void 팀빌딩_게시물_수정() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");


        TeamPostUpdateRequest param = new TeamPostUpdateRequest(post.getId(), "title", "content", "img",
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                5L);
        // when
        when(teamPostRepository.saveTeamPost(any())).thenReturn(post);
        when(teamPostRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(post.getUser()));
        // then
        TeamPostDto updatedPost = teamPostService.updateTeamPost(param, "user1");
        TeamPostDto result = teamPostService.findTeamPostById(updatedPost.getId());

        assertThat(result.getTitle()).isEqualTo(param.getTitle());
        assertThat(result.getContent()).isEqualTo(param.getContent());
    }

    @Test
    void 팀빌딩_게시물_수정시_게시물이_존재하지_않는경우() throws Exception {
        // given
        // when
        // then
        assertThatThrownBy(() -> teamPostService.updateTeamPost(mock(TeamPostUpdateRequest.class), "user1"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_수정시_권한이_없는경우() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("test1", "title1");

        TeamPostUpdateRequest param = new TeamPostUpdateRequest(post.getId(), "title", "content", "img",
                new HomeGymRequest("place", "address", "category", 3.14, 3.14),
                5L);
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(post.getUser()));
        when(teamPostRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> teamPostService.updateTeamPost(param, "none"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_삭제() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        User user = post.getUser();

        when(teamPostRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // when
        teamPostService.removeTeamPost(post.getId(), user.getUsername());
        // then
        assertThatNoException();
    }

    @Test
    void 팀빌딩_게시물_삭제시_게시물이_없는경우() throws Exception {
        // given
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() -> teamPostService.removeTeamPost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_시물_삭제시_유저가_권한이_없는경우() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1");
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> teamPostService.removeTeamPost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }
}
