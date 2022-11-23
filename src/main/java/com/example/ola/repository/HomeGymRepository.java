package com.example.ola.repository;

import com.example.ola.domain.HomeGym;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class HomeGymRepository {
    private final EntityManager em;

    public HomeGym save(HomeGym homeGym) {
        em.persist(homeGym);
        return homeGym;
    }

    public Optional<HomeGym> findByPlaceName(String placeName) {
        try {
            return Optional.of(em.createQuery("select h from HomeGym h" +
                            " where h.placeName=:placeName", HomeGym.class)
                    .setParameter("placeName", placeName)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
