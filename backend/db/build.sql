create table user (
    uid BIGINT UNSIGNED PRIMARY KEY auto_increment comment '用户id',
    username varchar(64) NOT NULL UNIQUE,
    email varchar(256) NOT NULL UNIQUE,
    password varchar(32) NOT NULL,
    avatar varchar(256) DEFAULT 'https://survey-planet-test.oss-cn-beijing.aliyuncs.com/avatar_xiangliyao.jpg' comment '头像url',
    create_time DATETIME NOT NULL comment '账号创建时间'
) comment '用户表';


CREATE TABLE survey (
    sid BIGINT UNSIGNED PRIMARY KEY auto_increment comment '问卷id',
    uid BIGINT UNSIGNED NOT NULL comment '创建用户id',
    title varchar(64) NOT NULL comment '问卷标题',
    description TEXT comment '问卷描述',
    type ENUM('normal', 'exam') NOT NULL comment '问卷类型',
    state ENUM('delete', 'open', 'close') default 'close' comment '问卷状态',
    create_time DATETIME NOT NULL comment '问卷创建时间',
    update_time DATETIME NOT NULL comment '问卷更新时间',
    open_time DATETIME comment '问卷上次开放时间',
    fill_num INT UNSIGNED DEFAULT 0 comment '填写人数',
    time_limit INT UNSIGNED comment '回答时间限制(min)',
    FOREIGN KEY (uid) REFERENCES user(uid)
) comment '问卷表';


CREATE TABLE question (
    qid BIGINT UNSIGNED PRIMARY KEY auto_increment comment '问题id',
    sid BIGINT UNSIGNED NOT NULL,
    title varchar(64) NOT NULL comment '问题标题',
    description TEXT comment '问题描述',
    type ENUM('single_choice', 'multiple_choice', 'fill_blank', 'file') NOT NULL comment '问题类型',
    required BOOLEAN NOT NULL comment '是否必填',
    score INT UNSIGNED comment '问题分值',
    FOREIGN KEY (sid) REFERENCES survey(sid)
) comment '问题表(父表)';

CREATE TABLE file_question (
    qid BIGINT UNSIGNED PRIMARY KEY comment '问题id',
    max_file_size INT UNSIGNED NOT NULL comment '文件大小限制(MB)',
    FOREIGN KEY (qid) REFERENCES question(qid)
) comment '文件问题表(子表)';

CREATE TABLE single_choice_question (
    qid BIGINT UNSIGNED PRIMARY KEY comment '问题id',
    options JSON NOT NULL comment '选项',
    answer JSON comment '答案',
    FOREIGN KEY (qid) REFERENCES question(qid)
) comment '单选问题表(子表)';

CREATE TABLE multiple_choice_question (
    qid BIGINT UNSIGNED PRIMARY KEY comment '问题id',
    options JSON NOT NULL comment '选项',
    answer JSON comment '答案',
    FOREIGN KEY (qid) REFERENCES question(qid)
) comment '多选问题表(子表)';

CREATE TABLE fill_blank_question (
    qid BIGINT UNSIGNED PRIMARY KEY comment '问题id',
    answer JSON comment '答案',
    FOREIGN KEY (qid) REFERENCES question(qid)
) comment '填空问题表(子表)';


CREATE TABLE verification_code (
    vid BIGINT UNSIGNED PRIMARY KEY auto_increment comment '验证码id',
    email varchar(256) NOT NULL comment '邮箱',
    code char(10) NOT NULL comment '验证码',
    expire_time DATETIME NOT NULL comment '过期时间'
) comment '验证码表';