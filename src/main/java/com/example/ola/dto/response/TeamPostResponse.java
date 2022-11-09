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
    private UserJoinResponse user;
    private String title;
    private String content;
    private String place;
    private Long limits;
    private List<UserJoinResponse> member;
    private Timestamp registeredAt;
    private TeamBuildingStatus status;

    public static TeamPostResponse fromTeamPostDto(TeamPostDto teamBuildingPost) {
        return new TeamPostResponse(
                teamBuildingPost.getId(),
                UserJoinResponse.fromUserDto(teamBuildingPost.getUserDto()),
                teamBuildingPost.getTitle(),
                teamBuildingPost.getContent(),
                teamBuildingPost.getPlace(),
                teamBuildingPost.getLimits(),
                teamBuildingPost.getMember()
                        .stream().map(UserJoinResponse::fromUserDto)
                        .collect(Collectors.toList()),
                teamBuildingPost.getRegisteredAt(),
                teamBuildingPost.getStatus()
        );
    }

}
