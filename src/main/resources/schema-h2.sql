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

CREATE TABLE polygon_metadata (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  ticker VARCHAR(10) NOT NULL,
                                  query_count INT,
                                  results_count INT,
                                  adjusted BOOLEAN,
                                  status VARCHAR(50),
                                  request_id VARCHAR(100),
                                  count INT,
                                  next_url VARCHAR(1024),
                                  for_date DATE NOT NULL,
                                  retrieved_at TIMESTAMP NOT NULL
);
