package com.example.ola.controller;

import com.example.ola.dto.request.CommentWriteRequest;
import com.example.ola.dto.response.CommentResponse;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.CommentService;
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
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public Response<List<CommentResponse>> commentList(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(commentService.commentList(postId)
                .stream().map(CommentResponse::fromCommentDto)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{postId}/comments")
    public Response<Void> writeComment(
            @RequestBody CommentWriteRequest commentWriteRequest,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.writeComment(postId, userPrincipal.getUsername(), commentWriteRequest.getContent(), commentWriteRequest.getType());
        return Response.success();
    }

    @PostMapping("/{postId}/comments/{parentId}")
    public Response<Void> writeCommentWithParent(
            @RequestBody CommentWriteRequest commentWriteRequest,
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.writeComment(postId, parentId, userPrincipal.getUsername(), commentWriteRequest.getContent(), commentWriteRequest.getType());
        return Response.success();
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public Response<Void> deleteComments(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        commentService.deleteComment(postId, userPrincipal.getUsername(), commentId);
        return Response.success();
    }
}
