CREATE TABLE device (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255),
                        type VARCHAR(50),
                        location VARCHAR(255),
                        user_id BIGSERIAL,

                        CONSTRAINT fk_device_user
                            FOREIGN KEY (user_id)
                                REFERENCES app_user(id)
                                ON DELETE CASCADE
);

CREATE INDEX idx_device_user_id
    ON device(user_id);