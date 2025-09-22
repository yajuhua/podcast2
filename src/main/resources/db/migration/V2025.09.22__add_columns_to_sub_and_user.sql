-- V2025.09.22__add_columns_to_sub_and_user.sql

-- 在 sub 表中新增 xml_conf_name 字段，存放xml配置名称
ALTER TABLE sub ADD COLUMN `xml_conf_name` TEXT DEFAULT 'default';

-- 在 user 表中新增 xml_conf_data 字段，存放xml配置文件
ALTER TABLE user ADD COLUMN `xml_conf_data` TEXT DEFAULT NULL;
