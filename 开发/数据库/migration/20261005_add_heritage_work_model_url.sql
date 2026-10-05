USE intangible_heritage_platform;

ALTER TABLE heritage_work
    ADD COLUMN IF NOT EXISTS model_url VARCHAR(500) DEFAULT NULL COMMENT 'GLB/GLTF模型地址';
