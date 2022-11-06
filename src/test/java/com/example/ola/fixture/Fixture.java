package com.example.ola.fixture;

import com.example.ola.domain.Comment;
import com.example.ola.domain.Post;
import com.example.ola.domain.User;

public class Fixture {
    public static Post makeFixture() {
        User user = new User("user1", "password1", "nick1", "name1", 30L, "home");
        return Post.of(user, "title", "content");
    }

    public static Comment commentFixture(Post post) {
        return Comment.of(
                post.getUser(),
                post,
                "content"
        );
    }
}
