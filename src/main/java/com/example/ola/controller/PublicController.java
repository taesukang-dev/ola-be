package com.example.ola.controller;

import com.example.ola.dto.request.RecommendRequest;
import com.example.ola.dto.response.*;
import com.example.ola.service.PostService;
import com.example.ola.service.TeamPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
@RestController
public class PublicController {
    private final PostService postService;
    private final TeamPostService teamPostService;

    /**
     * 게시글 조회
     * @param page
     * @param keyword
     * @return Response<MyPageResponse>
     */
    @GetMapping
    public Response<MyPageResponse> postList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return Response.success(postService.findAllPostsWithPaging(page, keyword));
    }

    /**
     * 팀 빌딩 게시글 조회
     * @param page
     * @param keyword
     * @param place
     * @return Response<MyPageResponse>
     */
    @GetMapping("/team")
    public Response<MyPageResponse> teamPostList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String place) {
        return Response.success(teamPostService.findAllTeamPostsWithPaging(page, keyword, place));
    }

    /**
     * 게시글 단건 조회
     * @param postId
     * @return Response<PostResponse>
     */
    @GetMapping("/{postId}")
    public Response<PostResponse> post(@PathVariable Long postId) {
        return Response.success(PostResponse.fromPostDto(postService.findById(postId)));
    }

    /**
     * 팀 빌딩 게시글 단건 조회
     * @param postId
     * @return Response<TeamPostResponse>
     */
    @GetMapping("/team/{postId}")
    public Response<TeamPostResponse> teamPost(@PathVariable Long postId) {
        return Response.success(TeamPostResponse.fromTeamPostDto(teamPostService.findTeamPostById(postId)));
    }

    /**
     * 팀 빌딩 게시글 대기열 조회
     * @param postId
     * @return Response<List<UserResponse>>
     */
    @GetMapping("team/{postId}/wait")
    public Response<List<UserResponse>> getWaitList(@PathVariable Long postId) {
        return Response.success(teamPostService.getWaitLists(postId).stream().map(UserResponse::fromUserDto)
                .collect(Collectors.toList()));
    }

    /**
     * 추천 게시물 og 이미지 리스트 반환
     * @param request
     * @return Response<List<String>>
     */
    @PostMapping("/recommend")
    public Response<List<String>> recommendPost(@RequestBody RecommendRequest request) {
        return Response.success(postService.getRecommendPosts(request.getUrls()));
    }
}
