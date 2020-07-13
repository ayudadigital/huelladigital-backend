package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrganizationEmployeeRepository extends JpaRepository<OrganizationEmployee, Integer> {

    @Query("FROM OrganizationEmployee o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH c.roles WHERE o.id = :id")
    Optional<OrganizationEmployee> findByIdWithCredentialsAndRoles(@Param("id") Integer id);

    @Query("FROM OrganizationEmployee o LEFT JOIN FETCH o.credential c WHERE c.email = :email")
    Optional<OrganizationEmployee> findByEmail(@Param("email") String email);

    @Query("UPDATE OrganizationEmployee o SET o.joinedOrganization = :organization WHERE o.id = :employeeId")
    Integer updateOrganization(Integer employeeId, Organization organization);
}
