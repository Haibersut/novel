package com.wtl.novel.DTO;

public class UserIdUsernameDTO {
    private final Long userId;
    private final String username;

    public UserIdUsernameDTO(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

}