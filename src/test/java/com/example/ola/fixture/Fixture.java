package com.example.ola.fixture;

import com.example.ola.domain.*;
import com.example.ola.dto.request.HomeGymRequest;
import com.example.ola.dto.request.PostWriteRequest;
import com.example.ola.dto.request.TeamPostWriteRequest;

public class Fixture {
    public static Post makePostFixture(String username, String title) {
        HomeGym homeGym = HomeGym.of("test", "test", "test", 3.14, 3.14);
        User user = new User(username, "imgUri", "password1", "nick1", "name1", 30L, homeGym, UserGender.M.getName());
        return Post.of(user, title, "content", "imgUri");
    }

    public static TeamBuildingPost makeTeamPostFixture(String username, String title) {
        HomeGym homeGym = HomeGym.of("test", "test", "test", 3.14, 3.14);
        User user = new User(username, "imgUri", "password1", "nick1", "name1", 30L, homeGym, UserGender.M.getName());
        return TeamBuildingPost.of(user, title, "content", "imgUri", homeGym, 5L);
    }

    public static PostWriteRequest makePostWriteRequest(String username, String title) {
        return new PostWriteRequest(title, "content", username, "imgUri");
    }

    public static TeamPostWriteRequest makeTeamPostWriteRequest(String username, String title) {
        HomeGymRequest homeGymRequest = new HomeGymRequest("place", "road", "category", 3.14, 3.14);
        return new TeamPostWriteRequest(title, "content", username, homeGymRequest,5L, "imgUri");
    }

    public static Comment commentFixture(Post post) {
        return Comment.of(
                post.getUser(),
                post,
                "content"
        );
    }
}
