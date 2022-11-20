package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.request.PostType;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.PostRepository;
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
        return TeamPostDto.fromPost(teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND)));
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
        TeamBuildingPost foundedPost = teamPostRepository.findTeamPostById(param.getId())
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!foundedPost.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getPlace(), param.getLimits());
        return TeamPostDto.fromPost(foundedPost);
    }

    @Transactional
    public void removeTeamPost(Long postId, String userPrincipalUsername) {
        TeamBuildingPost post = teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        teamPostRepository.remove(post);
    }

    @Transactional
    public void addMember(Long id, String userPrincipalUsername) {
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
        TeamBuildingPost post = teamPostRepository.findTeamPostById(id)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!post.checkLimits()) {
            post.getMembers()
                    .stream()
                    .filter(e -> e.getUser().getUsername().equals(userPrincipalUsername))
                    .findAny().ifPresent(it -> {
                        throw new OlaApplicationException(ErrorCode.DUPLICATED_MEMBER);
                    });
            post.getMembers().add(TeamMember.of(post, user));
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
        TeamBuildingPost post = teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        if (!userPrincipalUsername.equals(post.getUser().getUsername()) && !user.getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (post.getUser().getUsername().equals(user.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        TeamMember teamMember = teamPostRepository.findTeamMemberByPostIdAndUserId(postId, userId);
        post.getMembers().remove(teamMember);
    }

    @Transactional
    public void confirmTeam(Long postId, String userPrincipalUsername) {
        TeamBuildingPost teamBuildingPost = teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUsername(userPrincipalUsername)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
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
}
