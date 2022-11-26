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

    public static TeamBuildingPost makeTeamPostFixture(String username, String title, double x, double y) {
        HomeGym homeGym = HomeGym.of("test", "test", "test", x, y);
        User user = new User(username, "imgUri", "password1", "nick1", "name1", 30L, homeGym, UserGender.M.getName());
        return TeamBuildingPost.of(user, title, "content", "imgUri", homeGym, 5L);
    }

    public static PostWriteRequest makePostWriteRequest(String username, String title) {
        return new PostWriteRequest(title, "content", username, "imgUri");
    }

    public static TeamPostWriteRequest makeTeamPostWriteRequest(String username, String title) {
        HomeGymRequest homeGymRequest = makeHomeGymRequestFixture("place", "road");
        return new TeamPostWriteRequest(title, "content", username, homeGymRequest,5L, "imgUri");
    }

    public static Comment makeCommentFixture(Post post) {
        return Comment.of(
                post.getUser(),
                post,
                "content"
        );
    }

    public static User makeUserFixture(String username, String password) {
        HomeGym homeGym = HomeGym.of("test", "test", "test", 3.14, 3.14);
        return new User(username, "imgUri", password, "nick1", "name1", 30L, homeGym, UserGender.M.getName());
    }

    public static Alarm makeAlarmFixture(String username, String fromUser, Long postId) {
        return Alarm.of(
                makeUserFixture(username, "password1"),
                AlarmArgs.of(postId, fromUser),
                AlarmType.COMMENT);
    }

    public static HomeGymRequest makeHomeGymRequestFixture(String placeName, String address) {
        return new HomeGymRequest(placeName, address, "category", 3.14, 3.14);
    }
}
