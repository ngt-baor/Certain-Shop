package com.certainshop.repository;

import com.certainshop.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Long> {
    List<ChatLieu> findAllByOrderByTenChatLieuAsc();
    boolean existsByTenChatLieu(String tenChatLieu);
    boolean existsByTenChatLieuAndIdNot(String tenChatLieu, Long id);
}
