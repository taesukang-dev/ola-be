package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.AlarmDto;
import com.example.ola.dto.CommentDto;
import com.example.ola.dto.PostDto;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.*;
import com.example.ola.dto.response.PostResponse;
import com.example.ola.dto.response.TeamPostResponse;
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
import java.util.stream.IntStream;

@Slf4j
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
        post.getMembers().add(TeamMember.of(post, post.getUser()));
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

    public List<List<?>> findAllPostsWithPaging(int start) {
        List<PostResponse> postList = postRepository.findAllPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(PostDto::fromPost)
                .map(PostResponse::fromPostDto)
                .collect(Collectors.toList());
        List<Integer> pageList = getPageList(PostType.POST, start);
        return List.of(postList, pageList);
    }

    // 없을 때에는 빈 list 반환
    public List<PostDto> findPostsByUsername(String userPrincipalUsername) {
        return postRepository.findPostsByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(PostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    public List<List<?>> findAllTeamPostsWithPaging(int start) {
        List<TeamPostResponse> postList = postRepository.findAllTeamPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(TeamPostDto::fromPost)
                .map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList());
        List<Integer> pageList = getPageList(PostType.TEAM_POST, start);
        return List.of(postList, pageList);
    }

    public List<TeamPostDto> findTeamPostByUsername(String userPrincipalUsername) {
        return postRepository.findJoinedTeamPostByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(TeamPostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
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
                    .filter(e -> e.getUser().getUsername().equals(userPrincipalUsername))
                    .findAny().ifPresent(it -> {
                        throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
                    });
            post.getMembers().add(TeamMember.of(post, user));
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
            if (!e.getUser().getUsername().equals(userPrincipalUsername)) {
                Alarm alarm = alarmRepository.save(Alarm.of(
                        e.getUser(),
                        AlarmArgs.of(post.getId(), userPrincipalUsername),
                        AlarmType.JOIN));
                alarmService.send(alarm.getId(), e.getUser().getId());
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
        TeamMember teamMember = postRepository.findTeamMemberByPostIdAndUserId(postId, userId);
        post.getMembers().remove(teamMember);
    }

    public List<Integer> getPageList(PostType postType, Integer currentPage) {
        int result;
        int total;
        if (postType == PostType.POST) {
            result = postRepository.getPostCount("post").intValue();
            total = (int) Math.ceil((float) result / 10);
        } else {
            result = postRepository.getPostCount("T").intValue();
            total = (int) Math.ceil((float) result / 9);
        }
        int startNumber = Math.max(currentPage - (5 / 2), 0);
        int endNumber = Math.min(startNumber + 5, total);
        return IntStream.range(startNumber, endNumber).boxed().collect(Collectors.toList());
    }

}
