package com.example.ola.repository;

import com.example.ola.domain.Post;
import com.example.ola.domain.TeamBuildingPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PostRepository {
    private final EntityManager em;

    public Post save(Post post) {
        em.persist(post);
        return post;
    }

    public TeamBuildingPost save(TeamBuildingPost post) {
        em.persist(post);
        return post;
    }

    public Optional<Post> findById(Long postId) {
        return Optional.ofNullable(
                em.createQuery("select p from Post p" +
                                " join fetch p.user" +
                                " where p.id =:postId", Post.class)
                        .setParameter("postId", postId)
                        .getSingleResult()
        );
    }

    public Optional<TeamBuildingPost> findTeamPostById(Long postId) {
        return Optional.ofNullable(
                em.createQuery("select p from TeamBuildingPost p" +
                                " join fetch p.user" +
                                " where p.id =:postId", TeamBuildingPost.class)
                        .setParameter("postId", postId)
                        .getSingleResult()
        );
    }

    public Optional<List<Post>> findAllPostsWithPaging(int start) {
        return Optional.ofNullable(em.createQuery("select p from Post p" +
                        " join fetch p.user" +
                        " order by p.id desc", Post.class)
                .setFirstResult(start * 10)
                .setMaxResults(10)
                .getResultList());
    }

    public Optional<List<TeamBuildingPost>> findAllTeamPostsWithPaging(int start) {
        return Optional.ofNullable(em.createQuery("select p from TeamBuildingPost p" +
                        " join fetch p.user" +
                        " order by p.id desc", TeamBuildingPost.class)
                .setFirstResult(start * 10)
                .setMaxResults(10)
                .getResultList());
    }

    public void remove(Post post) {
        em.remove(post);
    }

    public void remove(TeamBuildingPost post) {
        em.remove(post);
    }
}