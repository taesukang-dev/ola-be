package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.AlarmDto;
import com.example.ola.dto.CommentDto;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.*;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.CommentRepository;
import com.example.ola.repository.PostRepository;
import com.example.ola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;

    @Transactional
    public PostDto write(PostWriteRequest postWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(postWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return PostDto.fromPost(
                postRepository.save(
                        Post.of(
                                userRepository.findByUsername(postWriteRequest.getUsername())
                                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                                postWriteRequest.getTitle(),
                                postWriteRequest.getContent())));
    }

    @Transactional
    public TeamPostDto writeTeamPost(TeamPostWriteRequest teamPostWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(teamPostWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        TeamBuildingPost post = TeamBuildingPost.of(
                userRepository.findByUsername(teamPostWriteRequest.getUsername())
                        .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND)),
                teamPostWriteRequest.getTitle(),
                teamPostWriteRequest.getContent(),
                teamPostWriteRequest.getPlace(),
                teamPostWriteRequest.getLimits());
        post.getMembers().add(post.getUser());
        return TeamPostDto.fromPost(postRepository.saveTeamPost(post));
    }

    @Transactional
    public PostDto updatePost(PostUpdateRequest param, String userPrincipalUsername) {
        Post foundedPost = postRepository.findById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent());
        return PostDto.fromPost(foundedPost);
    }

    @Transactional
    public TeamPostDto updateTeamPost(TeamPostUpdateRequest param, String userPrincipalUsername) {
        TeamBuildingPost foundedPost = postRepository.findTeamPostById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getPlace(), param.getLimits());
        return TeamPostDto.fromPost(foundedPost);
    }

    public PostDto findById(Long postId) {
        return PostDto.fromPost(postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public TeamPostDto findTeamPostById(Long postId) {
        return TeamPostDto.fromPost(postRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
    }

    public List<PostDto> findAllPostsWithPaging(int start) {
        return postRepository.findAllPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(PostDto::fromPost)
                .collect(Collectors.toList());
    }

    public List<TeamPostDto> findAllTeamPostsWithPaging(int start) {
        return postRepository.findAllTeamPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(TeamPostDto::fromPost)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long postId, String userPrincipalUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
        commentRepository.deleteByPostId(postId);
        alarmRepository.deleteByPostId(postId);
    }

    @Transactional
    public void removeTeamPost(Long postId, String userPrincipalUsername) {
        TeamBuildingPost post = postRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        postRepository.remove(post);
    }

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
    public void writeComment(Long postId, String userPrincipalUsername, String content, CommentWriteRequestType type) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        commentRepository.save(Comment.of(user, post, content));
        selectAlarmTypeAndSend(type, post.getUser(), post, userPrincipalUsername);
    }

    @Transactional
    public void writeComment(Long postId, Long parentId, String userPrincipalUsername, String content, CommentWriteRequestType type) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.COMMENT_NOT_FOUND));
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        parent.addChild(Comment.of(user, post, content, parent));
        selectAlarmTypeAndSend(type, parent.getUser(), post, userPrincipalUsername);
    }

    private void selectAlarmTypeAndSend(CommentWriteRequestType type, User user, Post post, String userPrincipalUsername) {
        Alarm alarm;
        if (userPrincipalUsername.equals(user.getUsername())) {
            return ;
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

    @Transactional
    public List<AlarmDto> alarms(String username) {
        return alarmRepository.findByUsername(username)
                .map(alarms -> alarms.stream().map(AlarmDto::fromAlarm)
                        .collect(Collectors.toList())).orElseGet(List::of);
    }

    @Transactional
    public void deleteAlarm(Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.ALARM_NOT_FOUND));
        alarmRepository.remove(alarm);
    }

    @Transactional
    public void addMember(Long id, String userPrincipalUsername) {
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        TeamBuildingPost post = postRepository.findTeamPostById(id)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.checkLimits()) {
            post.getMembers()
                    .stream()
                    .filter(e -> e.getUsername().equals(userPrincipalUsername))
                    .findAny().ifPresent(it -> {
                        throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
                    });
            post.getMembers().add(user);
            // TODO : 방장이 확인하면 confirmed로
            if (post.getMembers().size() == post.getLimits()) {
                post.updateStatus(TeamBuildingStatus.CONFIRMED);
            }
        } else {
            throw new OlaApplicationException(ErrorCode.BAD_REQUEST);
        }
        sendAlarmToAllMembers(userPrincipalUsername, post);
    }

    private void sendAlarmToAllMembers(String userPrincipalUsername, TeamBuildingPost post) {
        post.getMembers().forEach(e -> {
            if (!e.getUsername().equals(userPrincipalUsername)){
                Alarm alarm = alarmRepository.save(Alarm.of(
                        e,
                        AlarmArgs.of(post.getId(), userPrincipalUsername),
                        AlarmType.JOIN));
                alarmService.send(alarm.getId(), e.getId());
            }
        });
    }

    @Transactional
    public void removeTeamMember(Long postId, Long userId, String userPrincipalUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        TeamBuildingPost post = postRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!user.getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (post.getUser().getUsername().equals(userPrincipalUsername)) {
            // 방장은 나갈 수 없음
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        post.getMembers().remove(user);
    }
}
