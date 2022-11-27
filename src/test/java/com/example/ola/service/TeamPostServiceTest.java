package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.TeamPostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Slf4j
@Transactional
@SpringBootTest
class TeamPostServiceTest {
    @Autowired TeamPostService teamPostService;
    @MockBean TeamPostRepository teamPostRepository;
    @MockBean UserRepository userRepository;
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
        TeamBuildingPost post = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);

        mockPostAndTeamMember(post, memberList, Set.of(), Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        User user = post.getUser();
        TeamPostWriteRequest postWriteRequest
                = Fixture.makeTeamPostWriteRequest(user.getUsername(), post.getTitle());

        try (MockedStatic<TeamBuildingPost> teamBuildingPostMockedStatic = mockStatic(TeamBuildingPost.class)){
            when(TeamBuildingPost
                    .of(any(), anyString(), anyString(), anyString(), any(), anyLong()))
                    .thenReturn(post);
            // when
            when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
            when(teamPostRepository.saveTeamPost(any())).thenReturn(post);
            // then
            teamPostService.writeTeamPost(postWriteRequest, "user1");

            verify(memberList, times(1)).add(any());
        }
    }

    @Test
    void 팀빌딩_게시물_작성_작성자와_로그인유저가_다른경우() throws Exception {
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
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
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
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
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
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
        for (int i = 0; i < 9; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i, 3.14, 3.14);
            temp.add(post);
        }
        // when
        when(teamPostRepository.findAllTeamPostsWithPaging(anyInt())).thenReturn(Optional.of(temp));
        when(teamPostRepository.getPostCount(eq("T"))).thenReturn(1L);
        when(Paging.getPageList(anyInt(), anyInt(), anyInt())).thenReturn(List.of(1, 2));
        // then
        MyPageResponse allTeamPostsWithPaging = teamPostService.findAllTeamPostsWithPaging(0, "", "");
        List<TeamPostResponse> contents = (List<TeamPostResponse>) allTeamPostsWithPaging.getContents();
        List<Integer> pageList = allTeamPostsWithPaging.getPageList();
        assertThat(contents.size()).isEqualTo(9);
        assertThat(pageList.size()).isEqualTo(2);
    }

    @Test
    void 팀빌딩_게시물_페이징_제목검색_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i, 3.14, 3.14);
            temp.add(post);
        }
        // when
        when(teamPostRepository.findAllTeamPostsByKeyword(any())).thenReturn(Optional.of(temp));
        when(teamPostRepository.getPostCount(eq("T"))).thenReturn(1L);
        // then
        MyPageResponse allTeamPostsWithPaging = teamPostService.findAllTeamPostsWithPaging(0, "title", "");
        List<TeamPostResponse> contents = (List<TeamPostResponse>) allTeamPostsWithPaging.getContents();
        assertThat(contents.size()).isEqualTo(9);
    }

    @Test
    void 팀빌딩_게시물_페이징_장소검색_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i, 3.14, 3.14);
            temp.add(post);
        }
        // when
        when(teamPostRepository.findAllTeamPostsByPlace(any())).thenReturn(Optional.of(temp));
        when(teamPostRepository.getPostCount(eq("T"))).thenReturn(1L);
        // then
        MyPageResponse allTeamPostsWithPaging = teamPostService.findAllTeamPostsWithPaging(0, "title", "장소");
        List<TeamPostResponse> contents = (List<TeamPostResponse>) allTeamPostsWithPaging.getContents();
        assertThat(contents.size()).isEqualTo(9);
    }

    @Test
    void 팀빌딩_가까운_게시물_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i, 3.14 + i, 3.14 + i);
            temp.add(post);
        }
        // when
        // TODO : 실제로는 x, y 값 기준으로 sorting 되는데, 수기로 return 해주기 때문에 실제 값과 매칭되지 않음
        // TODO : 여쭤보기!
        when(teamPostRepository.findPostsByShortestLocation(3.14, 3.14, 0)).thenReturn(Optional.of(temp));
        when(teamPostRepository.getPostCount(eq("T"))).thenReturn(1L);
        // then
        List<TeamPostDto> teamPostByLocation = teamPostService.findTeamPostByLocation(3.14, 3.14, 0);
        TeamPostDto teamPostDto1 = teamPostByLocation.get(0);
        TeamPostDto teamPostDto2 = teamPostByLocation.get(1);
        TeamPostDto teamPostDto3 = teamPostByLocation.get(2);

        assertThat((int) (teamPostDto2.getHomeGymDto().getX() - teamPostDto1.getHomeGymDto().getX())).isEqualTo(1);
        assertThat((int) (teamPostDto3.getHomeGymDto().getX() - teamPostDto1.getHomeGymDto().getX())).isEqualTo(2);
    }

    @Test
    void 내가_참여한_팀빌딩게시물_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title" + i, 3.14 + i, 3.14 + i);
            temp.add(post);
        }
        // when
        when(teamPostRepository.findJoinedTeamPostByUsername("user")).thenReturn(Optional.of(temp));
        // then
        List<TeamPostDto> user = teamPostService.findTeamPostByUsername("user");
        assertThat(user.size()).isEqualTo(9);
    }

    @Test
    void 팀빌딩_게시물_수정() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);


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
        TeamBuildingPost post = Fixture.makeTeamPostFixture("test1", "title1", 3.14, 3.14);

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
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
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
    void 팀빌딩_게시물_삭제시_유저가_권한이_없는경우() throws Exception {
        // given
        TeamBuildingPost post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> teamPostService.removeTeamPost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_대기열_조회() throws Exception {
        // given
        List<TeamMemberWaitList> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title", 3.14, 3.14);
            list.add(TeamMemberWaitList.of(post, post.getUser()));
        }
        // when
        when(teamPostRepository.findTeamMemberWaitListsById(any())).thenReturn(Optional.of(list));
        // then
        List<UserDto> waitLists = teamPostService.getWaitLists(1L);
        assertThat(waitLists.size()).isEqualTo(10);
    }

    @Test
    void 팀빌딩_게시물_대기열_조회시_대기열멤버가_없는경우() throws Exception {
        // given
        List<TeamMemberWaitList> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TeamBuildingPost post = Fixture.makeTeamPostFixture("user" + i, "title", 3.14, 3.14);
            list.add(TeamMemberWaitList.of(post, post.getUser()));
        }
        // when
        when(teamPostRepository.findTeamMemberWaitListsById(2L)).thenReturn(Optional.of(list));
        // then
        List<UserDto> waitLists = teamPostService.getWaitLists(1L);
        assertThat(waitLists.size()).isEqualTo(0);
    }

    @Test
    void 팀빌딩_게시물_대기열_추가() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitList, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        teamPostService.addWaitLists(1L, "test");
        // then
        verify(waitList).add(any());
        verify(memberList).forEach(any());
        verify(waitList).forEach(any());
    }

    @Test
    void 팀빌딩_게시물_대기열_추가시_이미있는경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        mockPostAndTeamMember(teamPost, memberList, Set.of(TeamMemberWaitList.of(teamPost, user)), Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));

        // then
        assertThatThrownBy(() -> teamPostService.addWaitLists(1L, user.getUsername()))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_대기열_삭제() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        teamPostService.removeWaitListMember(teamPost.getId(), user.getId(), user.getUsername());
        // then
        verify(waitLists).remove(any());
    }

    @Test
    void 팀빌딩_게시물_대기열_작성자가_삭제() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        teamPostService.removeWaitListMember(teamPost.getId(), user.getId(), teamPost.getUser().getUsername());
        // then
        verify(waitLists).remove(any());
    }

    @Test
    void 팀빌딩_게시물_대기열_삭제시_삭제_권한이_없는경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));
        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        // then
        assertThatThrownBy(() -> teamPostService.removeWaitListMember(teamPost.getId(), user.getId(), "none"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_멤버_추가() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitList, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        teamPostService.addMember(teamPost.getId(), user.getId(), teamPost.getUser().getUsername());
        // then
        verify(memberList).add(any());
        verify(waitList).remove(any());
        verify(memberList).forEach(any());
        verify(waitList).forEach(any());

    }

    @Test
    void 팀빌딩_게시물_멤버_추가_작성자가_아닌경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitList, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));

        // then
        assertThatThrownBy(() -> teamPostService.addMember(teamPost.getId(), user.getId(), "none"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_멤버_추가시_이미_참여한_경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, List.of(TeamMember.of(teamPost, user)), waitList, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));

        // then
        assertThatThrownBy(() -> teamPostService.addMember(teamPost.getId(), user.getId(), teamPost.getUser().getUsername()))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_멤버_확정() throws Exception {
        // given
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        User user = Fixture.makeUserFixture("user1", "1q2w3e4r!!");
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, List.of(TeamMember.of(teamPost, user)), waitList, user);

        // when
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        teamPostService.confirmTeam(teamPost.getId(), user.getUsername());
        // then
        verify(teamPost).updateStatus(any());
    }

    @Test
    void 팀빌딩_게시물_멤버_확정시_작성자가_아닌경우() throws Exception {
        // given
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        User user = Fixture.makeUserFixture("user1", "1q2w3e4r!!");
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, List.of(TeamMember.of(teamPost, user)), waitList, user);

        // when
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(Fixture.makeUserFixture("user12", "1q2w3e4r!!")));

        // then
        assertThatThrownBy(() -> teamPostService.confirmTeam(teamPost.getId(), user.getUsername()))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_멤버_확정시_정원이_차지않은경우() throws Exception {
        // given
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        User user = Fixture.makeUserFixture("user1", "1q2w3e4r!!");
        Set<TeamMemberWaitList> waitList = mock(Set.class);
        mockPostAndTeamMember(teamPost, List.of(), waitList, user);

        // when
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> teamPostService.confirmTeam(teamPost.getId(), user.getUsername()))
                .isInstanceOf(OlaApplicationException.class);
    }


    @Test
    void 팀빌딩_게시물_멤버_삭제() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        teamPostService.removeTeamMember(teamPost.getId(), user.getId(), user.getUsername());
        // then
        verify(memberList).remove(any());
    }

    @Test
    void 팀빌딩_게시물_멤버_작성자가_삭제() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));

        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        teamPostService.removeTeamMember(teamPost.getId(), user.getId(), teamPost.getUser().getUsername());
        // then
        verify(memberList).remove(any());
    }

    @Test
    void 팀빌딩_게시물_멤버_삭제시_삭제_권한이_없는경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, Fixture.makeUserFixture("user1", "1q2w3e4r!!"));
        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        // then
        assertThatThrownBy(() -> teamPostService.removeTeamMember(teamPost.getId(), user.getId(), "none"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_멤버_작성자가_빠지는경우() throws Exception {
        // given
        User user = Fixture.makeUserFixture("test", "1q2w3e4r!!");
        TeamBuildingPost teamPost = mock(TeamBuildingPost.class);
        List<TeamMember> memberList = mock(List.class);
        Set<TeamMemberWaitList> waitLists = mock(Set.class);
        mockPostAndTeamMember(teamPost, memberList, waitLists, user);
        // when
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(teamPostRepository.findTeamPostById(any())).thenReturn(Optional.of(teamPost));
        when(teamPostRepository.findTeamMemberByPostIdAndUserId(any(), anyLong())).thenReturn(TeamMember.of(teamPost, user));
        // then
        assertThatThrownBy(() -> teamPostService.removeTeamMember(teamPost.getId(), user.getId(), user.getUsername()))
                .isInstanceOf(OlaApplicationException.class);
    }

    private void mockPostAndTeamMember(TeamBuildingPost post, List<TeamMember> memberList, Set<TeamMemberWaitList> waitList, User user) {
        when(post.getId()).thenReturn(1L);
        when(post.getUser()).thenReturn(user);
        when(post.getTitle()).thenReturn("title1");
        when(post.getContent()).thenReturn("content1");
        when(post.getImgUri()).thenReturn("img1");
        when(post.getHomeGym()).thenReturn(HomeGym.of("place", "raod", "category", 3.14, 3.14));
        when(post.getLimits()).thenReturn(1L);
        when(post.getMembers()).thenReturn(memberList);
        when(post.getWaitLists()).thenReturn(waitList);
        when(post.getTeamBuildingStatus()).thenReturn(TeamBuildingStatus.READY);
    }
}
