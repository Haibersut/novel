package com.wtl.novel.repository;

import com.wtl.novel.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("select m from Message m " +
            "where m.userId = :userId and m.read = :read " +
            "and m.messageType in (:types) " +
            "order by m.createTime desc")
    Page<Message> findByUserIdAndReadAndMessageTypeInOrderByCreateTimeDesc(
            @Param("userId") Long userId,
            @Param("read") Boolean read,
            @Param("types") Collection<Message.MessageType> types,
            Pageable pageable);
    Page<Message> findByUserIdAndReadAndMessageTypeOrderByCreateTimeDesc(
            Long userId, Boolean read, Message.MessageType messageType, Pageable pageable);
    Page<Message> findByUserIdAndReadOrderByCreateTimeDesc(Long userId, Boolean read, Pageable pageable);
    /* 一键已读 */
    @Modifying
    @Query("update Message m set m.read = true where m.userId = :userId and m.read = false")
    void updateReadStatusByUserId(@Param("userId") Long userId);

    /* 可选：查未读数量 */
    long countByUserIdAndReadFalse(Long userId);
}