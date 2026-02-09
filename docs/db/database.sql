CREATE TABLE `users` (
                         `user_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `email` VARCHAR(100) NOT NULL,
                         `password_hash` VARCHAR(200) NOT NULL,
                         `display_name` VARCHAR(20) NOT NULL,
                         `role` VARCHAR(10) NOT NULL DEFAULT 'USER',
                         `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                         CONSTRAINT `pk_users` PRIMARY KEY (`user_id`),
                         CONSTRAINT `uq_users_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `boards` (
    `board_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) NOT NULL,
    `description` VARCHAR(100) NOT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT `pk_boards` PRIMARY KEY (`board_id`),
    CONSTRAINT `uk_boards_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `posts` (
    `post_id` BIGINT NOT NULL AUTO_INCREMENT,
    `board_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `title` VARCHAR(50) NOT NULL,
    `body` VARCHAR(300) NOT NULL,
    `like_count` INT NOT NULL DEFAULT 0,
    `comment_count` INT NOT NULL DEFAULT 0,
    `hot_score` DOUBLE NOT NULL DEFAULT 0,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT `pk_posts` PRIMARY KEY (`post_id`),
    CONSTRAINT `fk_posts_board_id` FOREIGN KEY (`board_id`) REFERENCES `boards` (`board_id`),
    CONSTRAINT `fk_posts_author_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comments` (
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `body` VARCHAR(200) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT `pk_comments` PRIMARY KEY (`comment_id`),
    CONSTRAINT `fk_comments_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`),
    CONSTRAINT `fk_comments_author_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 新增索引以優化看板文章列表查詢
CREATE INDEX `idx_posts_board_created` ON `posts` (`board_id`, `created_at`);
CREATE INDEX `idx_posts_board_hot` ON `posts` (`board_id`, `hot_score`);
