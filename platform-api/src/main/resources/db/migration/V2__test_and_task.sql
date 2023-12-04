CREATE TABLE IF NOT EXISTS tests(
    id BIGSERIAL PRIMARY KEY,
    issuer_id BIGINT NOT NULL ,
    name VARCHAR(256) NOT NULL ,
    description TEXT,
    duration INTEGER,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    number_task INT,
    CONSTRAINT fk_issuer_id FOREIGN KEY(issuer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS questions(
    id BIGSERIAL PRIMARY KEY,
    image TEXT,
    content TEXT NOT NULL,
    options TEXT ARRAY,
    correct_answer TEXT ARRAY
);

-- Связь тестов и задач

CREATE TABLE IF NOT EXISTS test_questions(
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT REFERENCES questions(id) ON DELETE RESTRICT,
    test_id BIGINT REFERENCES tests(id) ON DELETE RESTRICT,
    UNIQUE (question_id, test_id)
);

-- Нужен для контроля и просмотра результатов

CREATE TABLE IF NOT EXISTS user_tests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    test_id BIGINT REFERENCES tests(id),
    started_time TIMESTAMP,
    completed_time TIMESTAMP,
    count_correct INTEGER,
    UNIQUE (user_id, test_id)
);

-- Ответ пользователей task_id - id из таблицы test_questions;

CREATE TABLE IF NOT EXISTS user_answers (
    user_test_id BIGINT,
    task_id BIGINT,
    answer TEXT ARRAY NOT NULL,
    is_correct BOOLEAN,
    FOREIGN KEY (user_test_id) REFERENCES user_tests(id) ON DELETE RESTRICT,
    FOREIGN KEY (task_id) REFERENCES test_questions(id) ON DELETE RESTRICT,
    PRIMARY KEY (task_id, user_test_id)
);
