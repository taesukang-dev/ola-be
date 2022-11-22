package com.example.ola.dto.response;

import com.example.ola.domain.TeamBuildingStatus;
import com.example.ola.dto.TeamPostDto;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamPostResponse {
    private Long id;
    private UserResponse user;
    private String title;
    private String content;
    private String imgUri;
    private String place;
    private Long limits;
    private List<UserResponse> member;
    private Timestamp registeredAt;
    private TeamBuildingStatus status;

    public static TeamPostResponse fromTeamPostDto(TeamPostDto teamBuildingPost) {
        return new TeamPostResponse(
                teamBuildingPost.getId(),
                UserResponse.fromUserDto(teamBuildingPost.getUserDto()),
                teamBuildingPost.getTitle(),
                teamBuildingPost.getContent(),
                teamBuildingPost.getImgUri(),
                teamBuildingPost.getPlace(),
                teamBuildingPost.getLimits(),
                teamBuildingPost.getMembers()
                        .stream().map(UserResponse::fromUserDto)
                        .collect(Collectors.toList()),
                teamBuildingPost.getRegisteredAt(),
                teamBuildingPost.getStatus()
        );
    }

}
