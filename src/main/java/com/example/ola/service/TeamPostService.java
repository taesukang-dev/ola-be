package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.TeamPostRepository;
import com.example.ola.repository.UserRepository;
import com.example.ola.utils.Paging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TeamPostService {
    private final TeamPostRepository teamPostRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmService alarmService;
    private static final int TEAM_POST_SIZE = 9;

    public TeamPostDto findTeamPostById(Long postId) {
        return TeamPostDto.fromPost(getTeamPostOrElseThrow(postId));
    }

    public List<TeamPostDto> findTeamPostByUsername(String userPrincipalUsername) {
        return teamPostRepository.findJoinedTeamPostByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(TeamPostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
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
        return TeamPostDto.fromPost(teamPostRepository.saveTeamPost(post));
    }

    @Transactional
    public TeamPostDto updateTeamPost(TeamPostUpdateRequest param, String userPrincipalUsername) {
        TeamBuildingPost foundedPost = getTeamPostOrElseThrow(param.getId());
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getPlace(), param.getLimits());
        return TeamPostDto.fromPost(foundedPost);
    }

    @Transactional
    public void removeTeamPost(Long postId, String userPrincipalUsername) {
        TeamBuildingPost post = getTeamPostOrElseThrow(postId);
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        teamPostRepository.remove(post);
    }

    public List<UserDto> getWaitLists(Long postId) {
        return teamPostRepository.findTeamMemberWaitListsById(postId)
                .map(e -> e.stream()
                        .map(k -> UserDto.fromUser(k.getUser()))
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    @Transactional
    public void addWaitLists(Long id, String userPrincipalUsername) {
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        TeamBuildingPost post = getTeamPostOrElseThrow(id);
        post.getWaitLists()
                .stream()
                .filter(e -> e.getUser().getUsername().equals(userPrincipalUsername))
                .findAny().ifPresent(it -> {
                    throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
                });
        post.getWaitLists().add(TeamMemberWaitList.of(post, user));
        sendAlarmToAllMembers(userPrincipalUsername, post, AlarmType.WAITING);
        sendAlarmToAllWaitLists(userPrincipalUsername, post, AlarmType.WAITING);
    }

    @Transactional
    public void addMember(Long id, Long memberId, String userPrincipalUsername) {
        User user = getUserOrElseThrow(memberId);
        TeamBuildingPost post = getTeamPostOrElseThrow(id);
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (!post.checkLimits()) {
            post.getMembers()
                    .stream()
                    .filter(e -> e.getUser().getUsername().equals(user.getUsername()))
                    .findAny().ifPresent(it -> {
                        throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
                    });
            post.getMembers().add(TeamMember.of(post, user));
        } else {
            throw new OlaApplicationException(ErrorCode.BAD_REQUEST);
        }
        removeWaitListMember(id, memberId, userPrincipalUsername);
        sendAlarmToAllMembers(userPrincipalUsername, post, AlarmType.JOIN);
        sendAlarmToAllWaitLists(userPrincipalUsername, post, AlarmType.JOIN);
    }

    private void sendAlarmToAllMembers(String userPrincipalUsername, TeamBuildingPost post, AlarmType alarmType) {
        post.getMembers().forEach(e -> {
            if (!e.getUser().getUsername().equals(userPrincipalUsername)) {
                Alarm alarm = alarmRepository.save(Alarm.of(
                        e.getUser(),
                        AlarmArgs.of(post.getId(), userPrincipalUsername),
                        alarmType));
                alarmService.send(alarm.getId(), e.getUser().getId());
            }
        });
    }

    private void sendAlarmToAllWaitLists(String userPrincipalUsername, TeamBuildingPost post, AlarmType alarmType) {
        post.getWaitLists().forEach(e -> {
            if (!e.getUser().getUsername().equals(userPrincipalUsername)) {
                Alarm alarm = alarmRepository.save(Alarm.of(
                        e.getUser(),
                        AlarmArgs.of(post.getId(), userPrincipalUsername),
                        alarmType));
                alarmService.send(alarm.getId(), e.getUser().getId());
            }
        });
    }

    @Transactional
    public void removeTeamMember(Long postId, Long userId, String userPrincipalUsername) {
        TeamBuildingPost post = checkValidAndGetPost(postId, userId, userPrincipalUsername);
        TeamMember teamMember = teamPostRepository.findTeamMemberByPostIdAndUserId(postId, userId);
        post.getMembers().remove(teamMember);
    }

    @Transactional
    public void removeWaitListMember(Long postId, Long userId, String userPrincipalUsername) {
        TeamBuildingPost post = checkValidAndGetPost(postId, userId, userPrincipalUsername);
        TeamMemberWaitList waitListMember = teamPostRepository.findWaitListMemberByPostIdAndUserId(postId, userId);
        post.getWaitLists().remove(waitListMember);
    }

    private TeamBuildingPost checkValidAndGetPost(Long postId, Long userId, String userPrincipalUsername) {
        User user = getUserOrElseThrow(userId);
        TeamBuildingPost post = getTeamPostOrElseThrow(postId);
        if (!userPrincipalUsername.equals(post.getUser().getUsername()) && !user.getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (post.getUser().getUsername().equals(user.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return post;
    }

    @Transactional
    public void confirmTeam(Long postId, String userPrincipalUsername) {
        TeamBuildingPost teamBuildingPost = getTeamPostOrElseThrow(postId);
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        if (teamBuildingPost.getUser() != user) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (teamBuildingPost.getMembers().size() != teamBuildingPost.getLimits()) {
            throw new OlaApplicationException(ErrorCode.MEMBERS_NOT_ENOUGH);
        }
        teamBuildingPost.updateStatus(TeamBuildingStatus.CONFIRMED);
    }

    public MyPageResponse findAllTeamPostsWithPaging(int start, String keyword, String place) {
        if (place.equals("장소")) {
            return findAllTeamPostsByPlace(keyword);
        }
        if (StringUtils.hasText(keyword)) {
            return findAllTeamPostsByKeyword(keyword);
        }
        List<TeamPostResponse> postList = teamPostRepository.findAllTeamPostsWithPaging(start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(TeamPostDto::fromPost)
                .map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList());
        List<Integer> pageList = Paging.getPageList(teamPostRepository.getPostCount("T").intValue(), TEAM_POST_SIZE, start);
        return MyPageResponse.of(postList, pageList);
    }

    public MyPageResponse findAllTeamPostsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new OlaApplicationException(ErrorCode.INVALID_KEYWORD);
        }
        List<TeamPostResponse> postList = teamPostRepository.findAllTeamPostsByKeyword(keyword)
                .map(e -> e.stream().map(TeamPostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of)
                .stream().map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList());
        return MyPageResponse.of(postList, List.of());
    }
    public MyPageResponse findAllTeamPostsByPlace(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new OlaApplicationException(ErrorCode.INVALID_KEYWORD);
        }
        List<TeamPostResponse> postList = teamPostRepository.findAllTeamPostsByPlace(keyword)
                .map(e -> e.stream().map(TeamPostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of)
                .stream().map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList());
        return MyPageResponse.of(postList, List.of());
    }

    private User getUserOrElseThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    private TeamBuildingPost getTeamPostOrElseThrow(Long postId) {
        return teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
    }

    private User getUserByUsernameOrElseThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }
}
