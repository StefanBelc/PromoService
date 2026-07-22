package com.company.promo.service.persistence;

import com.company.promo.service.persistence.entity.LeaderboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntity, String> {

}
