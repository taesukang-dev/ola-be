package com.example.ola.controller;

import com.example.ola.dto.request.PostUpdateRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.CommentResponse;
import com.example.ola.dto.response.PostResponse;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@RestController
public class PostController {
    private final PostService postService;

    @PostMapping
    public Response<Void> write(
            @RequestBody PostWriteRequest postWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.write(postWriteRequest, userPrincipal.getUsername());
        return Response.success();
    }

    @PostMapping("/team")
    public Response<Void> writeTeamPost(
            @RequestBody TeamPostWriteRequest teamPostWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.writeTeamPost(teamPostWriteRequest, userPrincipal.getUsername());
        return Response.success();
    }

    @PutMapping
    public Response<PostResponse> update(
            @RequestBody PostUpdateRequest postUpdateRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(PostResponse.fromPostDto(postService.updatePost(postUpdateRequest, userPrincipal.getUsername())));
    }

    @PutMapping("/team")
    public Response<TeamPostResponse> updateTeamPost(
            @RequestBody TeamPostUpdateRequest teamPostUpdateRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(TeamPostResponse.fromTeamPostDto(postService.updateTeamPost(teamPostUpdateRequest, userPrincipal.getUsername())));
    }

    @DeleteMapping("/{postId}")
    public Response<Void> removePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.removePost(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @DeleteMapping("/team/{postId}")
    public Response<Void> removeTeamPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.removeTeamPost(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @GetMapping("/{postId}/comments")
    public Response<List<CommentResponse>> commentList(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(postService.commentList(postId)
                .stream().map(CommentResponse::fromCommentDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{postId}/comments")
    public Response<Void> writeComment(
            @RequestBody String content,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.writeComment(postId, userPrincipal.getUsername(), content);
        return Response.success();
    }

    @PostMapping("/{postId}/{parentId}/comments")
    public Response<Void> writeCommentWithParent(
            @RequestBody String content,
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.writeComment(postId, parentId, userPrincipal.getUsername(), content);
        return Response.success();
    }

    @DeleteMapping("/{postId}/{commentId}/comments")
    public Response<Void> deleteComments(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.delete(postId, userPrincipal.getUsername(), commentId);
        return Response.success();
    }
}
