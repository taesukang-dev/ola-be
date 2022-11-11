package com.example.ola.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("T")
@Entity
public class TeamBuildingPost extends Post {
    private String place;
    private Long limits;
    @ManyToMany
    private List<User> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    TeamBuildingStatus teamBuildingStatus = TeamBuildingStatus.READY;

    public TeamBuildingPost(User user, String title, String content, String place, Long limits) {
        super(user, title, content);
        this.place = place;
        this.limits = limits;
    }

    public static TeamBuildingPost of(User user, String title, String content, String place, Long limits) {
        return new TeamBuildingPost(user, title, content, place, limits);
    }

    public void update(String title, String content, String place, Long limits) {
        this.update(title, content);
        this.place = place;
        this.limits = limits;
    }


    /**
     * true : 꽉 참
     * false : 자리 남음!
     * @return
     */
    public boolean checkLimits() {
        return limits <= members.size();
    }

    public void updateStatus(TeamBuildingStatus status) {
        this.teamBuildingStatus = status;
    }
}
