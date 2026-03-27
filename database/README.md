# HRMS SQL Layout (Unified)

## Directory

```text
database/
  00_drop_create_db.sql
  01_schema.sql
  02_seed_base.sql
  03_seed_demo.sql
  04_migration_workflow.sql
  05_patch_workflow_text_repair.sql
  06_patch_workflow_template_identity_guard.sql
  hrms_full_init.sql
  archive/deprecated_sql/
```

## Single Init Entry

- `hrms_full_init.sql` is the only recommended one-click entry script.
- Execution order:
  1. `00_drop_create_db.sql`
  2. `01_schema.sql`
  3. `02_seed_base.sql`
  4. `03_seed_demo.sql`
  5. `04_migration_workflow.sql`

## Scope of Each SQL

- `00_drop_create_db.sql`: recreate database with `utf8mb4`.
- `01_schema.sql`: legacy full baseline schema + historical baseline data.
- `02_seed_base.sql`: reserved base-seed layer (currently no-op).
- `03_seed_demo.sql`: reserved demo-seed layer (currently no-op).
- `04_migration_workflow.sql`: workflow template/table upgrade and historical data backfill.
- `05_patch_workflow_text_repair.sql`: repair existing workflow records that were already written as `?` (historical dirty data patch, optional).
- `06_patch_workflow_template_identity_guard.sql`: check/fix duplicate template identity and ensure unique keys.

## Workflow Canonical Upgrade

`04_migration_workflow.sql` includes:

- add `template_id`, `template_code`, `category`, `current_version`
- add `latest_definition_json`, `latest_layout_json`, `latest_snapshot_json`
- add `published_version`, `published_snapshot_json`
- add `created_by`, `updated_by`
- create `hr_workflow_template_version`
- backfill historical template keys and normalize status values
- normalize workflow tables to `utf8mb4`

## Deprecated SQL

- Old fragmented SQL scripts were moved to `archive/deprecated_sql/`.
- They are kept only for audit/reference and are not part of the active init path.
