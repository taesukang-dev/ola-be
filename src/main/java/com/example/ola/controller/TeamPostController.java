package com.example.ola.controller;

import com.example.ola.dto.request.TeamPostByLocationRequest;
import com.example.ola.dto.request.TeamPostUpdateRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;
import com.example.ola.dto.response.Response;
import com.example.ola.dto.response.TeamPostResponse;
import com.example.ola.dto.response.UserResponse;
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

    @PostMapping("/location")
    public Response<List<TeamPostResponse>> getPostsByLocation(
            @RequestBody TeamPostByLocationRequest teamPostByLocationRequest,
            @RequestParam int page) {
        return Response.success(teamPostService.findTeamPostByLocation(teamPostByLocationRequest.getX(), teamPostByLocationRequest.getY(), page)
                .stream().map(TeamPostResponse::fromTeamPostDto)
                .collect(Collectors.toList()));
    }

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

    @PostMapping("/{postId}/wait")
    public Response<Void> addWait(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.addWaitLists(postId, userPrincipal.getUsername());
        return Response.success();
    }

    @DeleteMapping("/{postId}/wait/{memberId}")
    public Response<Void> deleteWait(
            @PathVariable Long postId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.removeWaitListMember(postId, memberId, userPrincipal.getUsername());
        return Response.success();
    }

    @PostMapping("/{postId}/member/{memberId}")
    public Response<Void> addMember(
            @PathVariable Long postId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        teamPostService.addMember(postId, memberId, userPrincipal.getUsername());
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
