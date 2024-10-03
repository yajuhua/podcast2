-- V2024.10.02__add_columns_to_user.sql

-- 在 user 表中新增 api_token 字段，存放api访问使用的token ,none表示没有apiToken
ALTER TABLE user ADD COLUMN `api_token` TEXT default 'none';

-- 在 user 表中新增 bot_info 字段
ALTER TABLE user ADD COLUMN `bot_info` TEXT;
