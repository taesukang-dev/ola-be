package com.example.ola.repository;

import com.example.ola.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserRepository {
    private final EntityManager em;

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return em.createQuery("select u from User u" +
                        " join fetch u.homeGym" +
                        " where u.username=:username", User.class)
                .setParameter("username", username)
                .getResultList()
                .stream().findFirst();
    }

    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(em.find(User.class, userId));
    }
}
