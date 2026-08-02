-- ============================================================
-- 待办系统数据库建表语句
-- 适用: MySQL 5.7+, 字符集 utf8mb4, 排序规则 utf8mb4_bin
-- ============================================================

-- 创建数据库（如已存在则跳过）
CREATE DATABASE IF NOT EXISTS task_view
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_bin;

USE task_view;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
  id            VARCHAR(32)  NOT NULL COMMENT '主键ID',
  username      VARCHAR(64)  NOT NULL COMMENT '用户名（登录用）',
  password      VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
  real_name     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '真实姓名/昵称',
  email         VARCHAR(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  phone         VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
  avatar        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '头像URL',
  status        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  last_login_time DATETIME   DEFAULT NULL COMMENT '最后登录时间',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_real_name (real_name),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户表';

-- ============================================================
-- 2. 待办任务表
-- ============================================================
DROP TABLE IF EXISTS t_task;
CREATE TABLE t_task (
  id            VARCHAR(32)  NOT NULL COMMENT '主键ID',
  title         VARCHAR(256) NOT NULL COMMENT '标题',
  type          VARCHAR(16)  NOT NULL DEFAULT 'task' COMMENT '类型: story-需求, bug-Bug, task-任务',
  priority      TINYINT(1)   NOT NULL DEFAULT 3 COMMENT '优先级: 1-紧急, 2-高, 3-中, 4-低',
  status        VARCHAR(16)  NOT NULL DEFAULT 'wait' COMMENT '状态: wait-未开始, doing-进行中, done-已完成, closed-已关闭',
  assigned_to   VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '负责人',
  deadline      DATE         DEFAULT NULL COMMENT '截止日期',
  description   LONGTEXT     COMMENT '描述（Markdown 内容）',
  project       VARCHAR(128) NOT NULL DEFAULT '' COMMENT '所属项目',
  module        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '所属模块',
  progress      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '进度 0-100',
  created_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
  created_date  DATE         DEFAULT NULL COMMENT '创建日期',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_type (type),
  KEY idx_priority (priority),
  KEY idx_status (status),
  KEY idx_assigned_to (assigned_to),
  KEY idx_project (project),
  KEY idx_module (module),
  KEY idx_deadline (deadline),
  KEY idx_created_by (created_by),
  KEY idx_create_time (create_time),
  FULLTEXT KEY ft_title_desc (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='待办任务表';

-- ============================================================
-- 3. 任务操作日志表（可选，用于记录任务变更历史，类似禅道）
-- ============================================================
DROP TABLE IF EXISTS t_task_log;
CREATE TABLE t_task_log (
  id            VARCHAR(32)  NOT NULL COMMENT '主键ID',
  task_id       VARCHAR(32)  NOT NULL COMMENT '关联任务ID',
  action        VARCHAR(32)  NOT NULL COMMENT '操作类型: create-创建, update-修改, delete-删除, status_change-状态变更',
  field_name    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '变更字段名',
  old_value     TEXT         COMMENT '旧值',
  new_value     TEXT         COMMENT '新值',
  operator      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '操作人',
  remark        VARCHAR(512) NOT NULL DEFAULT '' COMMENT '备注',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_operator (operator),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务操作日志表';

-- ============================================================
-- 4. 任务附件表
-- ============================================================
DROP TABLE IF EXISTS t_task_file;
CREATE TABLE t_task_file (
  id            VARCHAR(32)  NOT NULL COMMENT '主键ID',
  task_id       VARCHAR(32)  NOT NULL COMMENT '关联任务ID',
  file_name     VARCHAR(256) NOT NULL COMMENT '原始文件名',
  stored_name   VARCHAR(128) NOT NULL COMMENT '存储后的文件名（UUID）',
  file_path     VARCHAR(512) NOT NULL COMMENT '文件存储路径（相对路径）',
  file_size     BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  file_type     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'MIME 类型',
  created_by    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '上传人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_task_id (task_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务附件表';

-- ============================================================
-- 初始化数据：默认管理员账号 admin / 123
-- 密码为 admin123
-- 此处用占位，上线前替换为真实加密值
-- ============================================================
INSERT INTO t_user (id, username, password, real_name, email, status) VALUES
('1', 'admin', '202cb962ac59075b964b07152d234b70', '管理员', 'admin@taskview.com', 1);
