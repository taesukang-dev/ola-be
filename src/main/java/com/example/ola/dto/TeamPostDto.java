package com.example.ola.dto;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.domain.User;
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
    private User user;
    private String content;
    private String place;
    private Long limits;
    private List<UserDto> member;
    private Timestamp registeredAt;

    public static TeamPostDto fromPost(TeamBuildingPost teamBuildingPost) {
        return new TeamPostDto(
                teamBuildingPost.getId(),
                teamBuildingPost.getUser(),
                teamBuildingPost.getContent(),
                teamBuildingPost.getPlace(),
                teamBuildingPost.getLimits(),
                teamBuildingPost.getMembers()
                        .stream().map(UserDto::fromUser)
                        .collect(Collectors.toList()),
                teamBuildingPost.getRegisteredAt()
        );
    }
}
