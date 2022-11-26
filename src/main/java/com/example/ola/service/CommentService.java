package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.CommentDto;
import com.example.ola.dto.request.PostType;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    /**
     * post 별 댓글 조회
     * @param postId
     * @return List<CommentDto>
     */
    public List<CommentDto> commentList(Long postId) {
        return commentRepository.findByPostId(
                        getPostByIdOrElseThrow(postId).getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(CommentDto::fromComment)
                .collect(Collectors.toList());
    }

    /**
     * post 별 댓글 작성
     * @param postId
     * @param userPrincipalUsername
     * @param content
     * @param type
     */
    @Transactional
    public void writeComment(Long postId, String userPrincipalUsername, String content, PostType type) {
        Post post = getPostByIdOrElseThrow(postId);
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        commentRepository.save(Comment.of(user, post, content));
        selectAlarmTypeAndSend(type, post.getUser(), post, userPrincipalUsername);
    }

    /**
     * post 별 대댓글 작성
     * @param postId
     * @param parentId
     * @param userPrincipalUsername
     * @param content
     * @param type
     */
    @Transactional
    public void writeComment(Long postId, Long parentId, String userPrincipalUsername, String content, PostType type) {
        Comment parent = getCommentByIdOrElseThrow(parentId);
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        Post post = getPostByIdOrElseThrow(postId);
        parent.addChild(Comment.of(user, post, content, parent));
        selectAlarmTypeAndSend(type, parent.getUser(), post, userPrincipalUsername);
    }

    /**
     * 알람 저장, 댓글 작성 시에는 post 작성자, 대댓글 작성 시에는 부모 댓글 작성자가 target
     * @param type
     * @param user
     * @param post
     * @param userPrincipalUsername
     */
    private void selectAlarmTypeAndSend(PostType type, User user, Post post, String userPrincipalUsername) {
        Alarm alarm;
        if (userPrincipalUsername.equals(user.getUsername())) {
            return;
        }
        if (type.getName().equals("post")) {
            alarm = alarmRepository.save(Alarm.of(
                    user,
                    AlarmArgs.of(post.getId(), userPrincipalUsername),
                    AlarmType.COMMENT));
        } else {
            alarm = alarmRepository.save(Alarm.of(
                    user,
                    AlarmArgs.of(post.getId(), userPrincipalUsername),
                    AlarmType.TEAM_COMMENT));
        }
        alarmService.send(alarm.getId(), user.getId());
    }

    /**
     * 댓글 삭제
     * @param postId
     * @param userPrincipalUsername
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long postId, String userPrincipalUsername, Long commentId) {
        Post post = getPostByIdOrElseThrow(postId);
        Comment comment = getCommentByIdOrElseThrow(commentId);
        if (!comment.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (comment.getPost() != post) {
            throw new OlaApplicationException(ErrorCode.POST_NOT_FOUND);
        }
        commentRepository.delete(comment);
    }

    private Comment getCommentByIdOrElseThrow(Long parentId) {
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private Post getPostByIdOrElseThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
    }

    private User getUserByUsernameOrElseThrow(String userPrincipalUsername) {
        return userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }
}
