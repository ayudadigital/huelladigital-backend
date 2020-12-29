package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JpaProfileRepository extends JpaRepository<JpaProfile, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE JpaProfile p SET p.photoUrl = :photo WHERE p.id = :id")
    Integer updatePhoto(@Param("id") String id, @Param("photo") String photoUrl);
}
