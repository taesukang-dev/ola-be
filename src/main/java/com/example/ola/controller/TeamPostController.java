package com.example.ola.controller;

import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.dto.security.UserPrincipal;
import com.example.ola.service.PostService;
import com.example.ola.service.TeamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/team")
@RestController
public class TeamPostController {

    private final TeamPostService teamPostService;

    @GetMapping
    public Response<List<TeamPostResponse>> getMyTeamPosts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(teamPostService.findTeamPostByUsername(userPrincipal.getUsername())
                .stream().map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public Response<TeamPostResponse> writeTeamPost(
            @RequestBody TeamPostWriteRequest teamPostWriteRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(TeamPostResponse.fromTeamPostDto(teamPostService.writeTeamPost(teamPostWriteRequest, userPrincipal.getUsername())));
    }

    @PutMapping
    public Response<TeamPostResponse> updateTeamPost(
            @RequestBody TeamPostUpdateRequest teamPostUpdateRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return Response.success(TeamPostResponse.fromTeamPostDto(teamPostService.updateTeamPost(teamPostUpdateRequest, userPrincipal.getUsername())));
    }

    @DeleteMapping("/{postId}")
    public Response<Void> removeTeamPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.removeTeamPost(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @PostMapping("/{postId}/member")
    public Response<Void> addMember(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.addMember(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @DeleteMapping("/{postId}/member/{memberId}")
    public Response<Void> deleteMember(
            @PathVariable Long postId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.removeTeamMember(postId, memberId, userPrincipal.getUsername());
        return Response.success();
    }

    @GetMapping("/{postId}/confirm")
    public Response<Void> confirmTeam(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.confirmTeam(postId, userPrincipal.getUsername());
        return Response.success();
    }
}
