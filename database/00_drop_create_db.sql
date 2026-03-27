-- ============================================================================
-- 00 - Drop/Create Database (UTF8MB4)
-- ============================================================================
SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `hrms_db`;
CREATE DATABASE `hrms_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `hrms_db`;
