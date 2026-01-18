package com.example.demo.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "boards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_boards_name",columnNames = {"name"})
    }
)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    @Column(name = "description", nullable = false, length = 100)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
