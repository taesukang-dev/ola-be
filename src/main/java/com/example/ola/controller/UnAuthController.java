package com.example.ola.controller;

import com.example.ola.dto.response.PostResponse;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
@RestController
public class UnAuthController {
    private final PostService postService;

    @GetMapping
    public Response<List<List<?>>> postList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return Response.success(postService.findAllPostsWithPaging(page, keyword));
    }

    @GetMapping("/team")
    public Response<List<List<?>>> teamPostList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String place) {
        return Response.success(postService.findAllTeamPostsWithPaging(page, keyword, place));
    }

    @GetMapping("/{postId}")
    public Response<PostResponse> post(@PathVariable Long postId) {
        return Response.success(PostResponse.fromPostDto(postService.findById(postId)));
    }

    @GetMapping("/team/{postId}")
    public Response<TeamPostResponse> teamPost(@PathVariable Long postId) {
        return Response.success(TeamPostResponse.fromTeamPostDto(postService.findTeamPostById(postId)));
    }

}
