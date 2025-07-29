CREATE TABLE PRODUCT (
                         ID VARCHAR(36) PRIMARY KEY,
                         NAME VARCHAR(255),
                         PRICE DOUBLE,
                         CATEGORY_ID VARCHAR(36),
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);

CREATE TABLE CATEGORY (
                          ID VARCHAR(36) PRIMARY KEY,
                          NAME VARCHAR(255),
                          DESCRIPTION VARCHAR(1000),
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP
);