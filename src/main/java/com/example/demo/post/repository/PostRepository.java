package com.example.demo.post.repository;

import com.example.demo.post.dto.PostItem;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
            "FROM Post p WHERE p.board.boardId = :boardId AND p.status = 'ACTIVE'")
    Page<PostItem> findByBoardId(Long boardId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "board"})
    Optional<Post> findByPostIdAndStatus(Long postId, PostStatus status);
}

