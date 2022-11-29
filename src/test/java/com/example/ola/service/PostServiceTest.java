package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.request.PostUpdateRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.PostResponse;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
class PostServiceTest {
    @Autowired PostService postService;
    @MockBean PostRepository postRepository;
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
    void 일반게시물_작성() throws Exception {
        // given
        HomeGym homeGym = HomeGym.of("test", "test", "test", 3.14, 3.14);
        Post post = Fixture.makePostFixture("user1", "title");
        User user = post.getUser();
        PostWriteRequest postWriteRequest = Fixture.makePostWriteRequest(post.getUser().getUsername(), post.getTitle());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(postRepository.save(any())).thenReturn(post);
        // when
        PostDto postDto = postService.write(postWriteRequest, "user1");
        // then
        assertThat(postDto.getUserDto().getUsername()).isEqualTo("user1");
        assertThat(postDto.getUserDto().getAgeRange()).isEqualTo(30L);
    }

    @Test
    void 일반게시물_작성_작성자와_로그인유저가_다른경우() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(post.getUser()));
        when(postRepository.save(any())).thenReturn(post);
        // when
        // then
        assertThatThrownBy(() -> postService.write(Fixture.makePostWriteRequest(post.getUser().getUsername(), post.getTitle()), "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_조회() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(post.getUser()));
        when(postRepository.save(any())).thenReturn(post);
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        PostDto postDto = postService.write(Fixture.makePostWriteRequest(post.getUser().getUsername(), post.getTitle()), "user1");
        // when
        PostDto foundedPost = postService.findById(post.getId());
        // then
        assertThat(foundedPost).isEqualTo(postDto);
    }

    @Test
    void 일반게시물_페이징_조회() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user" + i, "title" + i);
            temp.add(post);
        }
        when(postRepository.findAllPostsWithPaging(anyInt())).thenReturn(Optional.of(temp));
        when(postRepository.getPostCount(eq("post"))).thenReturn(1L);
        when(Paging.getPageList(anyInt(), anyInt(), anyInt())).thenReturn(List.of(1, 2));

        // when
        MyPageResponse allPostsWithPaging = postService.findAllPostsWithPaging(0, "");
        List<PostResponse> contents = (List<PostResponse>) allPostsWithPaging.getContents();
        List<Integer> pageList = allPostsWithPaging.getPageList();
        // then
        assertThat(contents.size()).isEqualTo(10);
        assertThat(pageList.size()).isEqualTo(2);
    }

    @Test
    void 제목_검색_게시물_페이징_조회() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user" + i, "title" + i);
            temp.add(post);
        }
        when(postRepository.findAllPostsByKeyword(any())).thenReturn(Optional.of(temp));
        when(postRepository.getPostCount(eq("post"))).thenReturn(1L);

        // when
        MyPageResponse allPostsWithPaging = postService.findAllPostsWithPaging(0, "title");
        List<PostResponse> contents = (List<PostResponse>) allPostsWithPaging.getContents();
        List<Integer> pageList = allPostsWithPaging.getPageList();
        // then
        assertThat(contents.size()).isEqualTo(10);
        assertThat(pageList.size()).isEqualTo(0);
    }

    @Test
    void 내가쓴_게시물_조회() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user", "title" + i);
            temp.add(post);
        }
        when(postRepository.findPostsByUsername("user")).thenReturn(Optional.of(temp));
        // when
        List<PostDto> postDtoList = postService.findPostsByUsername("user");
        // then
        assertThat(postDtoList.size()).isEqualTo(10);
    }

    @Test
    void 내가쓴_게시물이_없는경우() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user", "title" + i);
            temp.add(post);
        }
        when(postRepository.findPostsByUsername("user")).thenReturn(Optional.of(temp));
        // when
        List<PostDto> postDtoList = postService.findPostsByUsername("user1");
        // then
        assertThat(postDtoList.size()).isEqualTo(0);
    }

    @Test
    void 제목_검색_게시물_페이징_조회시_검색어가_없는_경우() throws Exception {
        // given
        List<Post> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user" + i, "title" + i);
            temp.add(post);
        }
        // when
        when(postRepository.findAllPostsByKeyword(any())).thenReturn(Optional.of(temp));
        when(postRepository.getPostCount(eq("post"))).thenReturn(1L);
        // then
        assertThatThrownBy(() -> postService.findAllPostsByKeyword(""))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_수정() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        PostUpdateRequest param = new PostUpdateRequest(post.getId(), "updated", "updated", "testImg");
        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        PostDto updatedPost = postService.updatePost(param, "user1");
        PostDto result = postService.findById(updatedPost.getId());
        // then
        assertThat(result.getTitle()).isEqualTo(param.getTitle());
        assertThat(result.getContent()).isEqualTo(param.getContent());
    }

    @Test
    void 일반게시물_수정시_게시물이_존재하지_않는경우() throws Exception {
        // given
        PostUpdateRequest param = new PostUpdateRequest(1L, "updated", "updated", "imgUri");
        // when
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        // then
        assertThatThrownBy(() -> postService.updatePost(param, "user1"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_수정시_권한이_없는경우() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");

        PostUpdateRequest param = new PostUpdateRequest(post.getId(), "updated", "updated", "img");
        // when
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        // then
        assertThatThrownBy(() -> postService.updatePost(param, "user2"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_삭제() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        User user = post.getUser();
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        // when
        postService.delete(post.getId(), user.getUsername());
        // then
        assertThatNoException();
    }

    @Test
    void 일반게시물_삭제시_게시물이_없는경우() throws Exception {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() -> postService.delete(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 일반게시물_삭제시_유저가_권한이_없는경우() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        // when
        // then
        assertThatThrownBy(() -> postService.delete(1L, "asd"))
                .isInstanceOf(OlaApplicationException.class);
    }
}