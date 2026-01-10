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
