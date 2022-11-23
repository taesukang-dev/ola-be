package com.example.ola.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE post SET DELETED_AT = NOW() where id = ?")
@Where(clause = "deleted_at is null")
@DiscriminatorValue("T")
@Entity
public class TeamBuildingPost extends Post {
    @ManyToOne(fetch = FetchType.LAZY)
    HomeGym homeGym;
    private Long limits;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMemberWaitList> waitLists = new HashSet<>();

    @Enumerated(EnumType.STRING)
    TeamBuildingStatus teamBuildingStatus = TeamBuildingStatus.READY;

    public TeamBuildingPost(User user, String title, String content, String imgUri, HomeGym homeGym, Long limits) {
        super(user, title, content, imgUri);
        this.homeGym = homeGym;
        this.limits = limits;
    }

    public static TeamBuildingPost of(User user, String title, String content, String imgUri, HomeGym homeGym, Long limits) {
        return new TeamBuildingPost(user, title, content, imgUri, homeGym, limits);
    }

    public void update(String title, String content, String imgUri, HomeGym homeGym, Long limits) {
        this.update(title, content, imgUri);
        this.homeGym = homeGym;
        this.limits = limits;
    }

    public boolean checkLimits() {
        return limits <= members.size();
    }

    public void updateStatus(TeamBuildingStatus status) {
        this.teamBuildingStatus = status;
    }
}
