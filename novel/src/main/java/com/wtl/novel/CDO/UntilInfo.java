package com.wtl.novel.CDO;

import java.time.LocalDateTime;

public class UntilInfo {
    private  boolean all;
    private  boolean stillValid;
    private  String  deadline;
    private  Long userId;
    private  String username;
    private  Long dicId;
    private LocalDateTime createdAt;

    public UntilInfo() {
    }

    public UntilInfo(boolean all, boolean stillValid, String deadline, Long userId, String username) {
        this.all = all;
        this.stillValid = stillValid;
        this.deadline = deadline;
        this.userId = userId;
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDicId() {
        return dicId;
    }

    public void setDicId(Long dicId) {
        this.dicId = dicId;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public boolean isStillValid() {
        return stillValid;
    }

    public void setStillValid(boolean stillValid) {
        this.stillValid = stillValid;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}