package com.company.promo.service.persistence;

import com.company.promo.service.persistence.entity.LeaderboardTopPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardTopPlayersRepository extends JpaRepository<LeaderboardTopPlayerEntity,String> {
}
