package com.example.ola.dto;

import com.example.ola.domain.*;
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
public class TeamPostDto {
    private Long id;
    private UserDto userDto;
    private String title;
    private String content;
    private String imgUri;
    private HomeGymDto homeGymDto;
    private Long limits;
    private List<UserDto> members;
    private Timestamp registeredAt;
    private TeamBuildingStatus status;

    public static TeamPostDto fromPost(TeamBuildingPost teamBuildingPost) {
        return new TeamPostDto(
                teamBuildingPost.getId(),
                UserDto.fromUser(teamBuildingPost.getUser()),
                teamBuildingPost.getTitle(),
                teamBuildingPost.getContent(),
                teamBuildingPost.getImgUri(),
                HomeGymDto.fromHomeGym(teamBuildingPost.getHomeGym()),
                teamBuildingPost.getLimits(),
                teamBuildingPost.getMembers()
                        .stream().map(e -> UserDto.fromUser(e.getUser()))
                        .collect(Collectors.toList()),
                teamBuildingPost.getRegisteredAt(),
                teamBuildingPost.getTeamBuildingStatus()
        );
    }
}
