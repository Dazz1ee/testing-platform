CREATE TABLE IF NOT EXISTS tests(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    issuer_id BIGINT NOT NULL ,
    name VARCHAR(256) NOT NULL ,
    description TEXT,
    private BOOLEAN NOT NULL ,
    CONSTRAINT fk_issuer_id FOREIGN KEY(issuer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS tasks(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    image BIGINT,
    test_id BIGINT NOT NULL,
    question TEXT NOT NULL ,
    options TEXT ARRAY,
    correct_answer TEXT ARRAY,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS user_answers (
    user_id BIGINT,
    task_id BIGINT,
    answer TEXT ARRAY NOT NULL,
    correct BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    PRIMARY KEY(user_id, task_id)
);

CREATE TABLE IF NOT EXISTS user_tests (
    user_id BIGINT REFERENCES users(id),
    test_id BIGINT REFERENCES tests(id),
    completed_time TIMESTAMP NOT NULL,
    count_correct INTEGER,
    PRIMARY KEY (user_id, test_id)
)