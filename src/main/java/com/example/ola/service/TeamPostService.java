package com.example.ola.service;

import com.example.ola.domain.*;
import com.example.ola.dto.TeamPostDto;
import com.example.ola.dto.UserDto;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.MyPageResponse;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.exception.ErrorCode;
import com.example.ola.exception.OlaApplicationException;
import com.example.ola.repository.AlarmRepository;
import com.example.ola.repository.HomeGymRepository;
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
    private final HomeGymRepository homeGymRepository;
    private final AlarmService alarmService;
    private static final int TEAM_POST_SIZE = 9;

    /**
     * 나와 가장 가까운 장소의 팀 모집 조회
     * @param x
     * @param y
     * @param start
     * @return List<TeamPostDto>
     */
    public List<TeamPostDto> findTeamPostByLocation(Double x, Double y, int start) {
        return teamPostRepository.findPostsByShortestLocation(x, y, start)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND))
                .stream().map(TeamPostDto::fromPost)
                .collect(Collectors.toList());
    }

    /**
     * 내가 참여한 팀 모집 게시글 반환
     * @param userPrincipalUsername
     * @return List<TeamPostDto>
     */
    public List<TeamPostDto> findTeamPostByUsername(String userPrincipalUsername) {
        return teamPostRepository.findJoinedTeamPostByUsername(userPrincipalUsername)
                .map(e -> e.stream().map(TeamPostDto::fromPost)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    /**
     * 페이징된 게시글 반환
     * @param start
     * @param keyword
     * @param place
     * @return MyPageResponse
     */
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

    /**
     * 제목으로 검색한 페이징 게시글 반환
     * @param keyword
     * @return MyPageResponse
     */
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

    /**
     * 장소로 검색한 페이징 게시글 반환
     * @param keyword
     * @return MyPageResponse
     */
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

    /**
     * 게시글 작성, 자신은 자신의 팀 모집에 무조건 포함되어야 함
     * @param teamPostWriteRequest
     * @param userPrincipalUsername
     * @return TeamDto
     */
    @Transactional
    public TeamPostDto writeTeamPost(TeamPostWriteRequest teamPostWriteRequest, String userPrincipalUsername) {
        if (!userPrincipalUsername.equals(teamPostWriteRequest.getUsername())) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        TeamBuildingPost post = TeamBuildingPost.of(
                getUserByUsernameOrElseThrow(teamPostWriteRequest.getUsername()),
                teamPostWriteRequest.getTitle(),
                teamPostWriteRequest.getContent(),
                teamPostWriteRequest.getImgUri(),
                checkDuplicateHomeGymAndGetHomeGym(teamPostWriteRequest.getHomeGymRequest()),
                teamPostWriteRequest.getLimits());
        post.getMembers().add(TeamMember.of(post, post.getUser()));
        return TeamPostDto.fromPost(teamPostRepository.saveTeamPost(post));
    }

    /**
     * 게시글 수정
     * @param param
     * @param userPrincipalUsername
     * @return TeamPostDto
     */
    @Transactional
    public TeamPostDto updateTeamPost(TeamPostUpdateRequest param, String userPrincipalUsername) {
        TeamBuildingPost foundedPost = getTeamPostByPostIdOrElseThrow(param.getId());
        if (!getUserByUsernameOrElseThrow(foundedPost.getUser().getUsername()).getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        foundedPost.update(param.getTitle(), param.getContent(), param.getImgUri(), checkDuplicateHomeGymAndGetHomeGym(param.getHomeGymRequest()), param.getLimits());
        return TeamPostDto.fromPost(foundedPost);
    }

    /**
     * DB에 같은 사명으로 튜플이 존재하면 해당 튜플 반환, 아니면 새로 저장한다.
     * @param updateParam
     * @return HomeGym
     */
    private HomeGym checkDuplicateHomeGymAndGetHomeGym(HomeGymRequest updateParam) {
        HomeGym homeGym = homeGymRepository.findByPlaceName(updateParam.getPlaceName())
                .orElseGet(() -> saveAndReturnHomeGym(updateParam));
        if (!homeGym.getRoadAddressName().equals(updateParam.getRoadAddressName())) { // 사명은 같아도 주소가 다른 경우 저장
            homeGym = saveAndReturnHomeGym(updateParam);
        }
        return homeGym;
    }

    private HomeGym saveAndReturnHomeGym(HomeGymRequest homeGymRequest) {
        return homeGymRepository.save(
                HomeGym.of(
                        homeGymRequest.getPlaceName(),
                        homeGymRequest.getRoadAddressName(),
                        homeGymRequest.getCategoryName(),
                        homeGymRequest.getX(),
                        homeGymRequest.getY()));
    }

    /**
     * 팀 모집 게시글 삭제
     * @param postId
     * @param userPrincipalUsername
     */
    @Transactional
    public void removeTeamPost(Long postId, String userPrincipalUsername) {
        TeamBuildingPost post = getTeamPostByPostIdOrElseThrow(postId);
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        teamPostRepository.remove(post);
    }

    /**
     * 팀 모집 게시글 대기열 반환
     * @param postId
     * @return List<UserDto>
     */
    public List<UserDto> getWaitLists(Long postId) {
        return teamPostRepository.findTeamMemberWaitListsById(postId)
                .map(e -> e.stream()
                        .map(k -> UserDto.fromUser(k.getUser()))
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    /**
     * 유저를 대기열에 저장하고 알람을 전송한다.
     * @param id
     * @param userPrincipalUsername
     */
    @Transactional
    public void addWaitLists(Long id, String userPrincipalUsername) {
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        TeamBuildingPost post = getTeamPostByPostIdOrElseThrow(id);
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

    /**
     * 유저를 멤버에 추가하고 알람을 전송한다.
     * @param id
     * @param memberId
     * @param userPrincipalUsername
     */
    @Transactional
    public void addMember(Long id, Long memberId, String userPrincipalUsername) {
        User user = getUserByUserIdOrElseThrow(memberId);
        TeamBuildingPost post = getTeamPostByPostIdOrElseThrow(id);
        if (!post.getUser().getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (!post.checkLimits()) { // 정원이 차지 않은 경우.
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
        removeWaitListMember(id, memberId, userPrincipalUsername); // 대기열에 있던 유저는 삭제하고 멤버에 편입한다.
        sendAlarmToAllMembers(userPrincipalUsername, post, AlarmType.JOIN);
        sendAlarmToAllWaitLists(userPrincipalUsername, post, AlarmType.JOIN);
    }

    /**
     * 모든 멤버에게 알람 전송
     * @param userPrincipalUsername
     * @param post
     * @param alarmType
     */
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

    /**
     * 모든 대기열에 알람 전송
     * @param userPrincipalUsername
     * @param post
     * @param alarmType
     */
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

    /**
     * 유저를 멤버에서 삭제한다.
     * @param postId
     * @param userId
     * @param userPrincipalUsername
     */
    @Transactional
    public void removeTeamMember(Long postId, Long userId, String userPrincipalUsername) {
        TeamBuildingPost post = checkValidAndGetPost(postId, userId, userPrincipalUsername);
        TeamMember teamMember = teamPostRepository.findTeamMemberByPostIdAndUserId(postId, userId);
        post.getMembers().remove(teamMember);
    }

    /**
     * 유저를 대기열에서 삭제한다.
     * @param postId
     * @param userId
     * @param userPrincipalUsername
     */
    @Transactional
    public void removeWaitListMember(Long postId, Long userId, String userPrincipalUsername) {
        TeamBuildingPost post = checkValidAndGetPost(postId, userId, userPrincipalUsername);
        TeamMemberWaitList waitListMember = teamPostRepository.findWaitListMemberByPostIdAndUserId(postId, userId);
        post.getWaitLists().remove(waitListMember);
    }

    /**
     * 삭제하는 권한은 팀 모집 게시글 작성자이거나 본인이어야 한다.
     * @param postId
     * @param userId
     * @param userPrincipalUsername
     * @return
     */
    private TeamBuildingPost checkValidAndGetPost(Long postId, Long userId, String userPrincipalUsername) {
        User user = getUserByUserIdOrElseThrow(userId);
        TeamBuildingPost post = getTeamPostByPostIdOrElseThrow(postId);
        if (!userPrincipalUsername.equals(post.getUser().getUsername()) && !user.getUsername().equals(userPrincipalUsername)) {
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (post.getUser().getUsername().equals(user.getUsername())) { // 자기 자신은 멤버에서 빠질 수 없다.
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        return post;
    }

    /**
     * 팀 멤버를 확정시킨다.
     * @param postId
     * @param userPrincipalUsername
     */
    @Transactional
    public void confirmTeam(Long postId, String userPrincipalUsername) {
        TeamBuildingPost teamBuildingPost = getTeamPostByPostIdOrElseThrow(postId);
        User user = getUserByUsernameOrElseThrow(userPrincipalUsername);
        if (teamBuildingPost.getUser() != user) { // 작성자만 확정시킬 수 있다.
            throw new OlaApplicationException(ErrorCode.UNAUTHORIZED_BEHAVIOR);
        }
        if (teamBuildingPost.getMembers().size() != teamBuildingPost.getLimits()) { // 정원이 차지 않으면 확정시킬 수 없다.
            throw new OlaApplicationException(ErrorCode.MEMBERS_NOT_ENOUGH);
        }
        teamBuildingPost.updateStatus(TeamBuildingStatus.CONFIRMED);
    }

    private User getUserByUserIdOrElseThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    private TeamBuildingPost getTeamPostByPostIdOrElseThrow(Long postId) {
        return teamPostRepository.findTeamPostById(postId)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.POST_NOT_FOUND));
    }

    public TeamPostDto findTeamPostById(Long postId) {
        return TeamPostDto.fromPost(getTeamPostByPostIdOrElseThrow(postId));
    }

    private User getUserByUsernameOrElseThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new OlaApplicationException(ErrorCode.USER_NOT_FOUND));
    }
}
