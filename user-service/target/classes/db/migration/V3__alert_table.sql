CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGSERIAL NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_device_user
                   FOREIGN KEY (user_id)
                   REFERENCES app_user(id)
                   ON DELETE CASCADE
);

CREATE INDEX idx_alert_user_id ON alert(user_id);