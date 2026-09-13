-- =====================================================================
-- MIGRATION V002 — allow the PAYMENT_* notification types
-- ---------------------------------------------------------------------
-- Hibernate created notifications.type with a CHECK constraint listing the
-- NotificationType values that existed at the time. ddl-auto=update never
-- refreshes it, so inserting PAYMENT_COMPLETED / PAYMENT_FAILED would be
-- rejected. Run once on any database created before this change; a fresh
-- database already gets the full list. Safe to re-run.
-- =====================================================================
BEGIN;

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

COMMIT;
