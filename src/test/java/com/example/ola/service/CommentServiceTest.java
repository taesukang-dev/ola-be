package com.example.ola.service;

import com.example.ola.domain.Alarm;
import com.example.ola.domain.Comment;
import com.example.ola.domain.Post;
import com.example.ola.domain.User;
import com.example.ola.dto.CommentDto;
import com.example.ola.dto.request.PostType;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.fixture.Fixture;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
class CommentServiceTest {
    @Autowired
    CommentService commentService;
    @MockBean
    PostRepository postRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    AlarmRepository alarmRepository;
    @MockBean
    CommentRepository commentRepository;
    @MockBean
    AlarmService alarmService;


    @Test
    void 댓글_조회() throws Exception {
        // given
        List<Comment> temp = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post post = Fixture.makePostFixture("user" + i, "title" + i);
            Comment comment = Comment.of(
                    post.getUser(),
                    post,
                    "content" + i
            );
            temp.add(comment);
        }
        when(postRepository.findById(any())).thenReturn(Optional.of(mock(Post.class)));
        when(commentRepository.findByPostId(any())).thenReturn(Optional.of(temp));
        // when
        List<CommentDto> commentDtos = commentService.commentList(1L);
        // then
        assertThat(commentDtos.size()).isEqualTo(10);
        assertThat(commentDtos.get(9).getContent()).isEqualTo("content9");
    }

    @Test
    void 댓글_작성() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        Alarm alarm = Fixture.makeAlarmFixture(post.getUser().getUsername(), "name", post.getId());
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(post.getUser()));
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(commentRepository.findById(any())).thenReturn(Optional.of(Fixture.makeCommentFixture(post)));
        when(alarmRepository.save(any())).thenReturn(alarm);
        doNothing().when(commentRepository).save(any());
        // when
        commentService.writeComment(post.getId(), "name", "content", PostType.POST);
        commentService.writeComment(post.getId(), 1L, "name", "content", PostType.POST);
        // then
        verify(alarmRepository, times(2)).save(any());
        verify(alarmService, times(2)).send(eq(alarm.getId()), eq(post.getUser().getId()));
    }

    @Test
    void 댓글_작성시_내가쓴_댓글일시_알람_전송없는경우() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        Alarm alarm = Fixture.makeAlarmFixture(post.getUser().getUsername(), "name", post.getId());
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(post.getUser()));
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(commentRepository.findById(any())).thenReturn(Optional.of(Fixture.makeCommentFixture(post)));
        when(alarmRepository.save(any())).thenReturn(alarm);
        doNothing().when(commentRepository).save(any());
        // when
        commentService.writeComment(post.getId(), "user1", "content", PostType.POST);
        commentService.writeComment(post.getId(), 1L, "user1", "content", PostType.POST);
        // then
        verify(alarmRepository, never()).save(any());
        verify(alarmService, never()).send(eq(alarm.getId()), eq(post.getUser().getId()));
    }

    @Test
    void 댓글_작성시_유저가_없는_경우() throws Exception {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.of(Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14)));
        when(commentRepository.findById(any())).thenReturn(Optional.of(mock(Comment.class)));
        // when then
        assertThatThrownBy(() -> commentService.writeComment(1L, "name", "content", PostType.POST))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 댓글_작성시_게시글이_없는_경우() throws Exception {
        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(mock(User.class)));
        when(commentRepository.findById(any())).thenReturn(Optional.of(mock(Comment.class)));
        // when then
        assertThatThrownBy(() -> commentService.writeComment(1L, "name", "content", PostType.POST))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 대댓글_작성시_부모_댓글이_없는_경우() throws Exception {
        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(mock(User.class)));
        when(postRepository.findById(any())).thenReturn(Optional.of(Fixture.makePostFixture("user1", "title1")));
        // when then
        assertThatThrownBy(() -> commentService.writeComment(1L, null, "name", "content", PostType.POST))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 댓글_삭제() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        Comment comment = Fixture.makeCommentFixture(post);
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(commentRepository.findById(any())).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(any());
        // when
        commentService.deleteComment(1L, "user1", 1L);
        // then
        assertThatNoException();
    }

    @Test
    void 댓글_삭제시_댓글을_적은_유저와_삭제시도_유저가_다른경우() throws Exception {
        // given
        Post post = Fixture.makeTeamPostFixture("user1", "title1", 3.14, 3.14);
        Comment comment = Fixture.makeCommentFixture(post);
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(commentRepository.findById(any())).thenReturn(Optional.of(comment));
        // when then
        assertThatThrownBy(() -> commentService.deleteComment(1L, "none", 1L))
                .isInstanceOf(OlaApplicationException.class);
    }

    @Test
    void 댓글_삭제시_게시글이_다른경우() throws Exception {
        // given
        Post post = Fixture.makePostFixture("user1", "title1");
        Post post2 = Fixture.makePostFixture("user2", "title2");
        Comment comment = Fixture.makeCommentFixture(post2);
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(commentRepository.findById(any())).thenReturn(Optional.of(comment));
        // when then
        assertThatThrownBy(() -> commentService.deleteComment(1L, "none", 1L))
                .isInstanceOf(OlaApplicationException.class);
    }
}