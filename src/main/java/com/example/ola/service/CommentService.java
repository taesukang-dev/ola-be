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

    public List<CommentDto> commentList(Long postId) {
        return commentRepository.findByPostId(
                        postRepository.findById(postId)
                                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                                .getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(CommentDto::fromComment)
                .collect(Collectors.toList());
    }

    @Transactional
    public void writeComment(Long postId, String userPrincipalUsername, String content, PostType type) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        commentRepository.save(Comment.of(user, post, content));
        selectAlarmTypeAndSend(type, post.getUser(), post, userPrincipalUsername);
    }

    @Transactional
    public void writeComment(Long postId, Long parentId, String userPrincipalUsername, String content, PostType type) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        parent.addChild(Comment.of(user, post, content, parent));
        selectAlarmTypeAndSend(type, parent.getUser(), post, userPrincipalUsername);
    }

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

    @Transactional
    public void deleteComment(Long postId, String userPrincipalUsername, Long commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (comment.getPost() != post) {
            throw new OlaApplicationException(ErrorCode.POST_NOT_FOUND);
        }
        commentRepository.delete(comment);
    }
}
