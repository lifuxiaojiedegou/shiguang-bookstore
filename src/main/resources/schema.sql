CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(300) NOT NULL,
    price_cents INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    active TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price CHECK (price_cents > 0),
    CONSTRAINT chk_product_stock CHECK (stock >= 0),
    CONSTRAINT chk_product_active CHECK (active IN (0,1))
) ;
CREATE TABLE IF NOT EXISTS shop_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    name VARCHAR(30) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_shop_user_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
) ;
CREATE TABLE IF NOT EXISTS cart_item (
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES shop_user(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT chk_cart_quantity CHECK (quantity BETWEEN 1 AND 99)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
                                      total_cents BIGINT NOT NULL,
                                      address VARCHAR(300) NOT NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      paid_at DATETIME NULL,
                                      tracking_no VARCHAR(80) NULL,
                                      CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES shop_user(id),
                                      CONSTRAINT chk_orders_status CHECK (status IN ('UNPAID','PAID','SHIPPED','COMPLETED','CANCELLED')),
                                      CONSTRAINT chk_orders_total CHECK (total_cents > 0),
                                      INDEX idx_orders_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          order_id BIGINT NOT NULL,
                                          product_id BIGINT NOT NULL,
                                          name VARCHAR(80) NOT NULL,
                                          unit_price_cents INT NOT NULL,
                                          quantity INT NOT NULL,
                                          CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
                                          CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id),
                                          CONSTRAINT chk_order_item_quantity CHECK (quantity BETWEEN 1 AND 99),
                                          INDEX idx_order_item_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mail_outbox (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           order_id BIGINT NOT NULL,
                                           recipient VARCHAR(254) NOT NULL,
                                           subject VARCHAR(200) NOT NULL,
                                           body TEXT NOT NULL,
                                           status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                           error VARCHAR(200) NULL,
                                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           sent_at DATETIME NULL,
                                           CONSTRAINT fk_mail_order FOREIGN KEY (order_id) REFERENCES orders(id),
                                           CONSTRAINT chk_mail_status CHECK (status IN ('PENDING','SENDING','SENT','SAVED','FAILED')),
                                           INDEX idx_mail_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
