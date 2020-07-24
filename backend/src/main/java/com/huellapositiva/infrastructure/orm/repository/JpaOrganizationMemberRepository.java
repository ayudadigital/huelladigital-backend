package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrganizationMemberRepository extends JpaRepository<OrganizationMember, Integer> {

    @Query("FROM OrganizationMember o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH c.roles WHERE o.id = :id")
    Optional<OrganizationMember> findByIdWithCredentialsAndRoles(@Param("id") Integer id);

    @Query("FROM OrganizationMember o LEFT JOIN FETCH o.credential c WHERE c.email = :email")
    Optional<OrganizationMember> findByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE OrganizationMember o SET o.joinedOrganization = :organization WHERE o.id = :employeeId")
    Integer updateJoinedOrganization(@Param("employeeId") Integer employeeId, @Param("organization") Organization organization);

    @Modifying
    @Query("UPDATE OrganizationMember om SET om.joinedOrganization = NULL WHERE om.joinedOrganization.id = :organizationId")
    Integer unlinkMembersOfOrganization(@Param("organizationId")int organizationId);
}
