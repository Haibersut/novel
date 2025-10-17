package com.wtl.novel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "point")
    private Long point;

    private Boolean upload;
    private Boolean hideReadBooks;

    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }

    @OneToOne
    @JoinColumn(name = "invitation_code_id")
    private InvitationCode invitationCode;

    public Boolean getHideReadBooks() {
        return hideReadBooks;
    }

    public void setHideReadBooks(Boolean hideReadBooks) {
        this.hideReadBooks = hideReadBooks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public InvitationCode getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(InvitationCode invitationCode) {
        this.invitationCode = invitationCode;
    }

    public Long getPoint() {
        return point;
    }

    public void setPoint(Long point) {
        this.point = point;
    }
}