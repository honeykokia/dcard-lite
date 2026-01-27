package com.example.demo.post.repository;

import com.example.demo.post.dto.PostItem;
import com.example.demo.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT new com.example.demo.post.dto.PostItem(" +
            "p.postId, " +
            "p.author.userId, " +
            "p.author.displayName, " +
            "p.board.boardId, " +
            "p.board.name, " +
            "p.title, " +
            "p.likeCount, " +
            "p.hotScore, " +
            "p.status, " +
            "p.createdAt) " +
            "FROM Post p WHERE p.board.boardId = :boardId")
    Page<PostItem> findByBoardId(Long boardId, Pageable pageable);

}

