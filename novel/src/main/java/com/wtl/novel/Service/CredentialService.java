package com.wtl.novel.Service;

import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CredentialService {
    @Autowired
    private CredentialRepository credentialRepository;

    public Credential generateCredential(User user) {
        List<Credential> byUserId = credentialRepository.findByUser_Id(user.getId());
        String token = UUID.randomUUID().toString() + System.currentTimeMillis();
        if (byUserId.isEmpty()) {
            Credential credential = new Credential();
            credential.setUser(user);
            credential.setToken(token);
            credential.setExpiredAt(LocalDateTime.now().plusDays(7));
            return credentialRepository.save(credential);
        } else {
            Credential credential = byUserId.get(0);
            if (credential.getExpiredAt().isBefore(LocalDateTime.now())) {
                credential.setExpiredAt(LocalDateTime.now().plusDays(7));
                credential.setToken(token);
                return credentialRepository.save(credential);
            }
            return credential;
        }
    }

    public Credential findByToken(String token) {
        return credentialRepository.findByToken(token);
    }

    public void deleteCredential(Credential credential) {
        credentialRepository.delete(credential);
    }
}