package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaContactPersonRepository extends JpaRepository<JpaContactPerson, Integer> {

    @Query("FROM JpaContactPerson o LEFT JOIN FETCH o.credential c WHERE c.id = :accountId")
    Optional<JpaContactPerson> findByAccountId(@Param("accountId") String accountId);

    @Query("FROM JpaContactPerson o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH c.roles LEFT JOIN FETCH o.contactPersonProfile WHERE o.id = :id")
    Optional<JpaContactPerson> findByIdWithCredentialsAndRoles(@Param("id") String id);

    @Query("FROM JpaContactPerson o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH o.contactPersonProfile WHERE c.email = :email")
    Optional<JpaContactPerson> findByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE JpaContactPerson o SET o.joinedEsal = :organization WHERE o.id = :id")
    Integer updateJoinedESAL(@Param("id") String id, @Param("organization") JpaESAL organization);

    @Modifying
    @Query("UPDATE JpaContactPerson om SET om.joinedEsal = NULL WHERE om.joinedEsal.id = :organizationId")
    Integer unlinkMembersOfESAL(@Param("organizationId")String organizationId);

    @Query("FROM JpaContactPerson o LEFT JOIN FETCH o.credential c LEFT JOIN FETCH c.roles WHERE o.id = :id")
    Optional<JpaContactPerson> findByUUID(@Param("id") String id);

    @Query("FROM JpaContactPerson cp LEFT JOIN FETCH cp.joinedEsal je WHERE je.id = :id")
    Optional<JpaContactPerson> findByEsalId(@Param("id") String id);

    @Query("FROM JpaContactPerson cp LEFT JOIN FETCH cp.contactPersonProfile p LEFT JOIN FETCH cp.credential c WHERE c.id = :accountId")
    Optional<JpaContactPerson> findByAccountIdWithProfile(@Param("accountId") String accountId);
}
