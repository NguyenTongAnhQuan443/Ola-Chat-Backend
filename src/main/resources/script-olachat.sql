-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.5.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for olachat
DROP DATABASE IF EXISTS `olachat`;
CREATE DATABASE IF NOT EXISTS `olachat` /*!40100 DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci */;
USE `olachat`;

-- Dumping structure for table olachat.friend
DROP TABLE IF EXISTS `friend`;
CREATE TABLE IF NOT EXISTS `friend` (
  `id` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `friend_id` varchar(255) DEFAULT NULL,
  `friend_since` datetime(6) DEFAULT NULL,
  `status` enum('ACTIVE','BLOCKED') DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf9qk6e95h3da7o1u83ihtwrqa` (`friend_id`),
  KEY `FK3uu8s7yyof1qmenthngm24hry` (`user_id`),
  CONSTRAINT `FK3uu8s7yyof1qmenthngm24hry` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKf9qk6e95h3da7o1u83ihtwrqa` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Dumping data for table olachat.friend: ~0 rows (approximately)

-- Dumping structure for table olachat.friend_request
DROP TABLE IF EXISTS `friend_request`;
CREATE TABLE IF NOT EXISTS `friend_request` (
  `id` varchar(255) NOT NULL,
  `receiver_id` varchar(255) DEFAULT NULL,
  `response_at` datetime(6) DEFAULT NULL,
  `sender_id` varchar(255) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('ACCEPTED','PENDING','REJECTED') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpu7xdjn95orp6rucjsxps7gkg` (`receiver_id`),
  KEY `FK9rnftqmm2lmkhv4xrq8b9lp4f` (`sender_id`),
  CONSTRAINT `FK9rnftqmm2lmkhv4xrq8b9lp4f` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKpu7xdjn95orp6rucjsxps7gkg` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Dumping data for table olachat.friend_request: ~0 rows (approximately)

-- Dumping structure for table olachat.user
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(255) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','DELETED','INACTIVE') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `dob` datetime(6) DEFAULT NULL,
  `role` enum('ADMIN','USER') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Dumping data for table olachat.user: ~3 rows (approximately)
INSERT INTO `user` (`id`, `avatar`, `created_at`, `display_name`, `email`, `password`, `status`, `updated_at`, `username`, `dob`, `role`) VALUES
	('24563552-60d4-4f72-90c2-4582b57cffc4', NULL, '2025-02-16 16:04:26.092647', NULL, NULL, '$2a$10$RYXAu1sqeFBNNTmdLUcdVuksWhjEYFpxgKNN4/rhFFrJ/FThFqEi2', 'ACTIVE', '2025-02-16 16:04:26.092647', 'admin', NULL, 'ADMIN'),
	('77c004f0-7c9e-4659-bdc6-0475bde86445', 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/TrumpPortrait.jpg/800px-TrumpPortrait.jpg', '2024-02-14 08:10:00.000000', 'Donal Trump', 'trump-donal@example.com', 'password456', 'ACTIVE', '2024-02-14 08:40:00.000000', 'jane_smith', NULL, 'USER'),
	('d75870d3-f500-43ce-9198-6b96ea1435e0', 'https://byvn.net/Iii8', '2024-02-14 08:00:00.000000', 'Nguyen Thanh Nhut', 'john.doe@example.com', '$2a$12$Bz8nt/JDGyJUKo9KbAqiWeRuwoRPWVE11YT8vsWDLkkRwupZy35zC', 'ACTIVE', '2024-02-14 08:30:00.000000', 'nguyenthanhnhut13', NULL, 'USER');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
