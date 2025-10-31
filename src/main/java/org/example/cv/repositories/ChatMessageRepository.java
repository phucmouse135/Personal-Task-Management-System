package org.example.cv.repositories;

import java.util.List;

import org.example.cv.models.entities.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // Lấy lịch sử chat của một dự án (fixed method name: removed duplicate "Asc")
    Page<ChatMessageEntity> findByProjectIdOrderByCreatedAtAsc(Long projectId, Pageable pageable);

    // Lấy lịch sử chat riêng giữa 2 người
    @Query(
            """
		SELECT m FROM ChatMessageEntity m
		WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id)
		OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
				ORDER BY m.createdAt ASC
		""")
    List<ChatMessageEntity> findPrivateChatHistory(
            @Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, Pageable pageable);
}
