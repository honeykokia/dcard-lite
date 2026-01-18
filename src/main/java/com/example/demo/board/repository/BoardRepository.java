package com.example.demo.board.repository;

import com.example.demo.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAll(Pageable pageable);
    Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

}
