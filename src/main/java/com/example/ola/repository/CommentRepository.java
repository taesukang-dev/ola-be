package com.example.ola.repository;

import com.example.ola.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CommentRepository {
    private final EntityManager em;

    public Optional<Comment> findById(Long commentId) {
        return Optional.ofNullable(em.find(Comment.class, commentId));
    }

    public Optional<List<Comment>> findByPostId(Long postId) {
        return Optional.ofNullable(
                em.createQuery("select c from Comment c" +
                                " join fetch c.user" +
                                " join fetch c.post" +
                                " where c.post.id =:postId" +
                                " and c.parent is null")
                        .setParameter("postId", postId)
                        .getResultList());
    }

    public void save(Comment comment) {
        em.persist(comment);
    }

    public void delete(Comment comment) {
        em.remove(comment);
    }

    public void deleteByPostId(Long postId) {
        em.createQuery("UPDATE Comment c SET deleted_at = NOW() where c.post.id=:postId")
                .setParameter("postId", postId)
                .executeUpdate();
    }
}
