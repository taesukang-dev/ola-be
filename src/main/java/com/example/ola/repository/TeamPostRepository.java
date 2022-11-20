package com.example.ola.repository;

import com.example.ola.domain.TeamBuildingPost;
import com.example.ola.domain.TeamMember;
import com.example.ola.domain.TeamMemberWaitList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TeamPostRepository {

    private final EntityManager em;

    public TeamBuildingPost saveTeamPost(TeamBuildingPost post) {
        em.persist(post);
        return post;
    }

    public Optional<TeamBuildingPost> findTeamPostById(Long postId) {
        return Optional.ofNullable(
                em.createQuery("select p from TeamBuildingPost p" +
                                " join fetch p.user" +
                                " join fetch p.members" +
                                " where p.id =:postId", TeamBuildingPost.class)
                        .setParameter("postId", postId)
                        .getSingleResult()
        );
    }

    public Optional<List<TeamBuildingPost>> findAllTeamPostsWithPaging(int start) {
        return Optional.ofNullable(em.createQuery("select p from TeamBuildingPost p" +
                        " join fetch p.user" +
                        " order by p.id desc", TeamBuildingPost.class)
                .setFirstResult(start * 9)
                .setMaxResults(9)
                .getResultList());
    }

    public Optional<List<TeamBuildingPost>> findAllTeamPostsByKeyword(String keyword) {
        return Optional.ofNullable(em.createQuery("select p from TeamBuildingPost p" +
                        " join fetch p.user" +
                        " where p.title like :keyword" +
                        " order by p.id desc", TeamBuildingPost.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setMaxResults(10)
                .getResultList());
    }

    public Optional<List<TeamBuildingPost>> findAllTeamPostsByPlace(String place) {
        return Optional.ofNullable(em.createQuery("select p from TeamBuildingPost p" +
                        " join fetch p.user" +
                        " where p.place like :place" +
                        " order by p.id desc", TeamBuildingPost.class)
                .setParameter("place", "%" + place + "%")
                .setMaxResults(10)
                .getResultList());
    }

    public Optional<List<TeamBuildingPost>> findJoinedTeamPostByUsername(String username) {
        return Optional.ofNullable(
                em.createQuery("select p from TeamBuildingPost p" +
                                        " where p.id" +
                                        " in (select distinct t.post.id from TeamMember t" +
                                        " where t.user.username=:username" +
                                        " and t.deletedAt is null)"
                                , TeamBuildingPost.class)
                        .setParameter("username", username)
                        .getResultList());
    }

    public TeamMember findTeamMemberByPostIdAndUserId(Long postId, Long userId) {
        return em.createQuery("select m from TeamMember m" +
                        " where m.post.id=:postId" +
                        " and m.user.id=:userId" +
                        " and m.deletedAt is null", TeamMember.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public TeamMemberWaitList findWaitListMemberByPostIdAndUserId(Long postId, Long userId) {
        return em.createQuery("select m from TeamMemberWaitList m" +
                        " where m.post.id=:postId" +
                        " and m.user.id=:userId" +
                        " and m.deletedAt is null", TeamMemberWaitList.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public Optional<List<TeamMemberWaitList>> findTeamMemberWaitListsById(Long postId) {
        return Optional.ofNullable(em.createQuery("select w from TeamMemberWaitList w" +
                        " where w.post.id=:postId")
                .setParameter("postId", postId)
                .getResultList());
    }

    public void remove(TeamBuildingPost post) {
        em.remove(post);
    }

    public Long getPostCount(String type) {
        return em.createQuery("select count(*) from TeamBuildingPost p" +
                        " where dtype=:type", Long.class)
                .setParameter("type", type)
                .getSingleResult();
    }
}
