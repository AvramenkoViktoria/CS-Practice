CREATE TABLE IF NOT EXISTS product_groups (
    id      VARCHAR(64) PRIMARY KEY,
    name    VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS products (
    id       VARCHAR(64) PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    price    NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0)
    );

CREATE TABLE IF NOT EXISTS product_group_members (
    group_id   VARCHAR(64) NOT NULL REFERENCES product_groups(id) ON DELETE CASCADE,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, product_id)
    );

CREATE TABLE IF NOT EXISTS users (
    username      VARCHAR(64) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL
    );