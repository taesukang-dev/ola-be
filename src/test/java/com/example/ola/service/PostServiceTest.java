package com.example.ola.service;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.domain.User;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.PostUpdateRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
class PostServiceTest {
    @Autowired PostService postService;
    @MockBean PostRepository postRepository;
    @MockBean UserRepository userRepository;

    @Test
    void 일반게시물_작성() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.save(any())).thenReturn(post);
        // then
        PostDto postDto = postService.write(postWriteRequest, "user1");
        assertThat(postDto.getUserDto().getUsername()).isEqualTo("user1");
        assertThat(postDto.getUserDto().getHomeGym()).isEqualTo("home");
        assertThat(postDto.getUserDto().getAgeRange()).isEqualTo(30L);
    }

    @Test
    void 일반게시물_작성_작성자와_로그인유저가_다른경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.save(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> postService.write(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_작성시_유저가_존재하지_않는경우() throws Exception {
        // given
        User user = new User("user3", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user3");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        // when
        when(postRepository.save(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> postService.write(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_조회() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.save(any())).thenReturn(post);
        PostDto postDto = postService.write(postWriteRequest, "user1");
        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        // then
        PostDto foundedPost = postService.findById(post.getId());
        assertThat(foundedPost).isEqualTo(postDto);
    }

    @Test
    void 일반게시물_페이징_조회() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Post.of(
                    User.of("username" + i, "password", "nickname", "name", 10L, "gym"),
                    "title" + i,
                    "content" + i);
            temp.add(post);
        }
        // when
        when(postRepository.findAllPostsWithPaging(anyInt())).thenReturn(Optional.of(temp));
        // then
        List<PostDto> allPostsWithPaging = postService.findAllPostsWithPaging(0);
        assertThat(allPostsWithPaging.size()).isEqualTo(10);
    }

    @Test
    void 일반게시물_수정() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        PostUpdateRequest param = new PostUpdateRequest(post.getId(), "updated", "updated");
        // when
        when(postRepository.save(any())).thenReturn(post);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        // then
        PostDto updatedPost = postService.updatePost(param, "user1");
        assertThat(updatedPost.getTitle()).isEqualTo(param.getTitle());
        assertThat(updatedPost.getContent()).isEqualTo(param.getContent());
    }

    @Test
    void 일반게시물_수정시_게시물이_존재하지_않는경우() throws Exception {
        // given
        PostUpdateRequest param = new PostUpdateRequest(1L, "updated", "updated");
        // when
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThatThrownBy(() -> postService.updatePost(param, "user1"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_수정시_권한이_없는경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        PostUpdateRequest param = new PostUpdateRequest(post.getId(), "updated", "updated");
        // when
        when(postRepository.save(any())).thenReturn(post);
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        // then
        assertThatThrownBy(() -> postService.updatePost(param, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_삭제() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        // when
        postService.removePost(post.getId(), user.getUsername());
        // then
        assertThatNoException();
    }

    @Test
    void 일반게시물_삭제시_게시물이_없는경우() throws Exception {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() -> postService.removePost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_삭제시_유저가_권한이_없는경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        PostWriteRequest postWriteRequest = new PostWriteRequest("title1", "content1", "user1");
        Post post = Post.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent());
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> postService.removePost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_작성() throws Exception {
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        // then
        TeamPostDto teamPost = postService.writeTeamPost(postWriteRequest, "user1");
        assertThat(teamPost.getUserDto().getUsername()).isEqualTo("user1");
        assertThat(teamPost.getPlace()).isEqualTo("place1");
        assertThat(teamPost.getLimits()).isEqualTo(3L);
    }

    @Test
    void 팀빌딩_게시물_작성_작성자와_로그인유저가_다른경우() throws Exception {
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        // when
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> postService.writeTeamPost(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_작성시_유저가_존재하지_않는경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        // when
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        // then
        assertThatThrownBy(() -> postService.writeTeamPost(postWriteRequest, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_조회() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        TeamPostDto teamPost = postService.writeTeamPost(postWriteRequest, "user1");
        // when
        when(postRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // then
        TeamPostDto foundedPost = postService.findTeamPostById(post.getId());
        assertThat(foundedPost).isEqualTo(teamPost);
    }

    @Test
    void 팀빌딩_게시물_페이징_조회() throws Exception {
        // given
        List<TeamBuildingPost> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TeamBuildingPost post = TeamBuildingPost.of(
                    User.of("username" + i, "password", "nickname", "name", 10L, "gym"),
                    "title" + i,
                    "content" + i,
                    "place" + i,
                    10L);
            temp.add(post);
        }
        // when
        when(postRepository.findAllTeamPostsWithPaging(anyInt())).thenReturn(Optional.of(temp));
        // then
        List<TeamPostDto> result = postService.findAllTeamPostsWithPaging(0);
        assertThat(result.size()).isEqualTo(10);
    }

    @Test
    void 팀빌딩_게시물_수정() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(post.getId(), "updated", "updated", "updated");
        // when
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        when(postRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // then
        TeamPostDto updatedPost = postService.updateTeamPost(param, "user1");
        assertThat(updatedPost.getTitle()).isEqualTo(param.getTitle());
        assertThat(updatedPost.getContent()).isEqualTo(param.getContent());
    }

    @Test
    void 팀빌딩_게시물_수정시_게시물이_존재하지_않는경우() throws Exception {
        // given
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(1L, "updated", "updated", "updated");
        // when
        when(postRepository.findTeamPostById(any())).thenReturn(Optional.empty());
        // then
        assertThatThrownBy(() -> postService.updateTeamPost(param, "user1"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_수정시_권한이_없는경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        TeamPostUpdateRequest param = new TeamPostUpdateRequest(post.getId(), "updated", "updated", "updated");
        // when
        when(postRepository.saveTeamPost(any())).thenReturn(post);
        when(postRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // then
        assertThatThrownBy(() -> postService.updateTeamPost(param, "none"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_게시물_삭제() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        when(postRepository.findTeamPostById(post.getId())).thenReturn(Optional.of(post));
        // when
        postService.removeTeamPost(post.getId(), user.getUsername());
        // then
        assertThatNoException();
    }

    @Test
    void 팀빌딩_게시물_삭제시_게시물이_없는경우() throws Exception {
        // given
        when(postRepository.findTeamPostById(any())).thenReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() -> postService.removeTeamPost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 팀빌딩_시물_삭제시_유저가_권한이_없는경우() throws Exception {
        // given
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        TeamPostWriteRequest postWriteRequest = new TeamPostWriteRequest("title1", "content1", "user1", "place1", 3L);
        TeamBuildingPost post = TeamBuildingPost.of(user, postWriteRequest.getTitle(), postWriteRequest.getContent(), postWriteRequest.getPlace(), postWriteRequest.getLimits());
        when(postRepository.findTeamPostById(any())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> postService.removeTeamPost(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }
}