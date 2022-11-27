package com.example.ola.controller;

import com.example.ola.dto.request.*;
import com.example.ola.dto.response.*;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@RestController
public class PostController {
    private final PostService postService;

    /**
     * 내가 작성한 게시글 조회
     * @param userPrincipal
     * @return Response<List<PostResponse>>
     */
    @GetMapping
    public Response<List<PostResponse>> getMyPosts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(postService.findPostsByUsername(userPrincipal.getUsername())
                .stream().map(PostResponse::fromPostDto)
                .collect(Collectors.toList()));
    }

    /**
     * 게시글 작성
     * @param postWriteRequest
     * @param userPrincipal
     * @return Response<PostResponse>
     */
    @PostMapping
    public Response<PostResponse> write(
            @RequestBody PostWriteRequest postWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(PostResponse.fromPostDto(postService.write(postWriteRequest, userPrincipal.getUsername())));
    }

    /**
     * 게시글 수정
     * @param postUpdateRequest
     * @param userPrincipal
     * @return Response<PostResponse>
     */
    @PutMapping
    public Response<PostResponse> update(
            @RequestBody PostUpdateRequest postUpdateRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(PostResponse.fromPostDto(postService.updatePost(postUpdateRequest, userPrincipal.getUsername())));
    }

    /**
     * 게시글 삭제
     * @param postId
     * @param userPrincipal
     * @return Response<Void>
     */
    @DeleteMapping("/{postId}")
    public Response<Void> removePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.delete(postId, userPrincipal.getUsername());
        return Response.success();
    }
}
