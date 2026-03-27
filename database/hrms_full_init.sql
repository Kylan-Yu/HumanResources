-- ============================================================================
-- HRMS Full Init (single entry)
-- Execute this file to initialize a clean database or rebuild demo environment.
-- ============================================================================
SOURCE 00_drop_create_db.sql;
SOURCE 01_schema.sql;
SOURCE 02_seed_base.sql;
SOURCE 03_seed_demo.sql;
SOURCE 04_migration_workflow.sql;
SET FOREIGN_KEY_CHECKS = 1;
