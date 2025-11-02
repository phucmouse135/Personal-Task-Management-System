package org.example.cv.repositories;

import java.util.List;

import org.example.cv.models.entities.ChatMessageEntity;
import org.example.cv.models.entities.UserEntity;
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

    // Lấy danh sách người dùng đã chat với user hiện tại
    @Query(
            """
		SELECT DISTINCT u
		FROM UserEntity u
		WHERE u.id IN (
			SELECT DISTINCT CASE
				WHEN m.sender.id = :userId THEN m.receiver.id
				ELSE m.sender.id
			END
			FROM ChatMessageEntity m
			WHERE m.sender.id = :userId OR m.receiver.id = :userId
		)
		""")
    List<UserEntity> findConversationUsers(@Param("userId") Long userId);

    // Lấy tin nhắn cuối cùng giữa 2 user
    @Query(
            """
		SELECT m FROM ChatMessageEntity m
		WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id)
		OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
		ORDER BY m.createdAt DESC
		LIMIT 1
		""")
    ChatMessageEntity findLastMessageBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Lấy tin nhắn cuối cùng của một project
    ChatMessageEntity findTopByProjectIdOrderByCreatedAtDesc(Long projectId);
}
