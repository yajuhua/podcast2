-- V2024.10.01__add_columns_to_items.sql

-- 在 items 表中新增 plugin 字段，存放插件名称
ALTER TABLE items ADD COLUMN `plugin` TEXT;

-- 在 items 表中新增 input_and_select_data_list 字段，存放单集节目扩展配置
ALTER TABLE items ADD COLUMN `input_and_select_data_list` TEXT;
