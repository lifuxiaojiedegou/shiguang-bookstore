INSERT INTO product (name, category, description, price_cents, stock, active)
SELECT  'Java Web 开发基础', '教材', '从 Servlet 到 Spring MVC 的入门教材。', 5900, 20,1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Java Web 开发基础');

INSERT INTO product (name, category, description, price_cents, stock, active)
SELECT '数据结构与算法', '教材', '课程指定教材，含习题解析。', 6800, 15, 1
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '数据结构与算法');

INSERT INTO product (name, category, description, price_cents, stock, active)
SELECT '考研英语真题', '考试', '近十年真题与详解。', 4500, 30, 1
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '考研英语真题');

INSERT INTO product (name, category, description, price_cents, stock, active)
SELECT '人间值得', '文学', '一本适合睡前读的散文集。', 3200, 12, 1
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '人间值得');

INSERT INTO product (name, category, description, price_cents, stock, active)
SELECT '拾光书店帆布袋', '文创', '书店限定帆布袋，结实耐用。', 3900, 25, 1
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '拾光书店帆布袋');