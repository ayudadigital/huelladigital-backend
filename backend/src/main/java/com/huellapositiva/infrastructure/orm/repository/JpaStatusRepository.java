package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaStatusRepository extends JpaRepository<JpaStatus, Integer> {

    @Query("FROM JpaStatus s WHERE s.id = :id")
    Optional<JpaStatus> findById(@Param("id") Integer id);

}
