CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    executor_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    executor_id BIGINT NOT NULL REFERENCES users(id),
    service_id BIGINT REFERENCES services(id),
    details TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW'
);