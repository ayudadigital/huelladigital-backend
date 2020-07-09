package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrganizationRepository extends JpaRepository<Organization, Integer> {

    @Query("FROM Organization o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH c.roles WHERE o.id = :id")
    Optional<Organization> findByIdWithCredentialsAndRoles(@Param("id") Integer id);

    @Query("FROM Organization o WHERE o.name = :name")
    Optional<Organization> findByName(@Param("name") String name);
}
