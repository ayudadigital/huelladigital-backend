package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Integer> {

    @Query("FROM Role r WHERE r.name = :name")
    Optional<Role> findByName(@Param("name") String name);

    @Query("FROM Role r LEFT JOIN r.credentials c WHERE c.id = :accountId")
    List<Role> findAllByAccountId(@Param("accountId") String accountId);

    @Query("FROM Role r LEFT JOIN r.credentials c WHERE c.email = :email")
    List<Role> findAllByEmailAddress(@Param("email") String email);
}
