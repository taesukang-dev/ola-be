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

    @GetMapping
    public Response<List<PostResponse>> getMyPosts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(postService.findPostsByUsername(userPrincipal.getUsername())
                .stream().map(PostResponse::fromPostDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/team")
    public Response<List<TeamPostResponse>> getMyTeamPosts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(postService.findTeamPostByUsername(userPrincipal.getUsername())
                .stream().map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public Response<PostResponse> write(
            @RequestBody PostWriteRequest postWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(PostResponse.fromPostDto(postService.write(postWriteRequest, userPrincipal.getUsername())));
    }

    @PostMapping("/team")
    public Response<TeamPostResponse> writeTeamPost(
            @RequestBody TeamPostWriteRequest teamPostWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(TeamPostResponse.fromTeamPostDto(postService.writeTeamPost(teamPostWriteRequest, userPrincipal.getUsername())));
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
        postService.delete(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @DeleteMapping("/team/{postId}")
    public Response<Void> removeTeamPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.removeTeamPost(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @PostMapping("/team/{postId}/member")
    public Response<Void> addMember(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.addMember(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @DeleteMapping("/team/{postId}/member/{memberId}")
    public Response<Void> deleteMember(
            @PathVariable Long postId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.removeTeamMember(postId, memberId, userPrincipal.getUsername());
        return Response.success();
    }

    @GetMapping("/team/{postId}/confirm")
    public Response<Void> confirmTeam(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.confirmTeam(postId, userPrincipal.getUsername());
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
            @RequestBody CommentWriteRequest commentWriteRequest,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.writeComment(postId, userPrincipal.getUsername(), commentWriteRequest.getContent(), commentWriteRequest.getType());
        return Response.success();
    }

    @PostMapping("/{postId}/{parentId}/comments")
    public Response<Void> writeCommentWithParent(
            @RequestBody CommentWriteRequest commentWriteRequest,
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.writeComment(postId, parentId, userPrincipal.getUsername(), commentWriteRequest.getContent(), commentWriteRequest.getType());
        return Response.success();
    }

    @DeleteMapping("/{postId}/{commentId}/comments")
    public Response<Void> deleteComments(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deleteComment(postId, userPrincipal.getUsername(), commentId);
        return Response.success();
    }

    @GetMapping("/alarms")
    public Response<List<AlarmResponse>> alarmList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(postService.alarms(userPrincipal.getUsername())
                .stream().map(AlarmResponse::fromAlarmDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/alarms/{alarmId}")
    public Response<Void> deleteAlarm(@PathVariable Long alarmId) {
        postService.deleteAlarm(alarmId);
        return Response.success();
    }
}
