package com.example.ola.controller;

import com.example.ola.dto.response.PostResponse;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
@RestController
public class UnAuthController {
    private final PostService postService;

    @GetMapping
    public Response<List<PostResponse>> postList(@RequestParam(required = false, defaultValue = "0") int page) {
        return Response.success(
                postService.findAllPostsWithPaging(page)
                        .stream().map(PostResponse::fromPostDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/team")
    public Response<List<TeamPostResponse>> teamPostList(@RequestParam(required = false, defaultValue = "0") int page) {
        return Response.success(
                postService.findAllTeamPostsWithPaging(page)
                    .stream().map(TeamPostResponse::fromTeamPostDto)
                    .collect(Collectors.toList()));
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
