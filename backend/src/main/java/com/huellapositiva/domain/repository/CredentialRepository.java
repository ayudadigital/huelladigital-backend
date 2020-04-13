package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Integer> {

}
