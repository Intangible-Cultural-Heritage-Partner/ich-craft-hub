SET @heritage_model_column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'heritage_work'
      AND column_name = 'model_url'
);

SET @heritage_model_ddl = IF(
    @heritage_model_column_exists = 0,
    'ALTER TABLE heritage_work ADD COLUMN model_url VARCHAR(500) DEFAULT NULL COMMENT ''GLB/GLTF模型地址''',
    'SELECT 1'
);

PREPARE heritage_model_statement FROM @heritage_model_ddl;
EXECUTE heritage_model_statement;
DEALLOCATE PREPARE heritage_model_statement;
