package com.example.demo.post.repository;

import com.example.demo.post.dto.PostItem;
import com.example.demo.post.entity.Post;
import com.example.demo.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
            "FROM Post p WHERE p.board.boardId = :boardId AND p.status = :status")
    Page<PostItem> findByBoardId(@Param("boardId") Long boardId, @Param("status") PostStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")
    void incrementCommentCount(@Param("postId") long postId);

    Optional<Post> findBasicByPostIdAndStatus(long postId, PostStatus status);

    @EntityGraph(attributePaths = {"author", "board"})
    Optional<Post> findByPostIdAndStatus(long postId, PostStatus status);

}

