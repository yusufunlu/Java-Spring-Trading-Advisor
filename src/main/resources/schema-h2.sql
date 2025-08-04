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
                                  ticker VARCHAR(255),
                                  query_count INT,
                                  results_count INT,
                                  adjusted BOOLEAN,
                                  status VARCHAR(255),
                                  request_id VARCHAR(255),
                                  count INT,
                                  next_url VARCHAR(1024),
                                  url_tried VARCHAR(255),
                                  for_date DATE,
                                  retrieved_at TIMESTAMP,
                                  error_message VARCHAR(1024)
);
