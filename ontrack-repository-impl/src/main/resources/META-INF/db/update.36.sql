-- 36. Index on promotion & validation names (#472)

CREATE INDEX IF NOT EXISTS PROMOTION_LEVELS_IX_NAME ON PROMOTION_LEVELS(NAME);
CREATE INDEX IF NOT EXISTS VALIDATION_STAMPS_IX_NAME ON VALIDATION_STAMPS(NAME);
