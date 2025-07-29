-- Insert sample categories
INSERT INTO CATEGORY (ID, NAME, DESCRIPTION) VALUES
                                                 ('11111111-1111-1111-1111-111111111111', 'Electronics', 'Devices and gadgets'),
                                                 ('22222222-2222-2222-2222-222222222222', 'Books', 'Printed and digital books');

-- Insert sample products
INSERT INTO PRODUCT (ID, NAME, PRICE, CATEGORY_ID) VALUES
                                                       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Laptop', 1299.99, '11111111-1111-1111-1111-111111111111'),
                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'E-Reader', 99.99, '22222222-2222-2222-2222-222222222222');
