package com.wtl.novel.repository;

import com.wtl.novel.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Credential findByToken(String token);

    List<Credential> findByUser_Id(Long userId);
}