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
                                  id SERIAL PRIMARY KEY,
                                  ticker VARCHAR(10) NOT NULL,
                                  query_count INTEGER,
                                  results_count INTEGER,
                                  adjusted BOOLEAN,
                                  status VARCHAR(50),
                                  request_id VARCHAR(100),
                                  count INTEGER,
                                  next_url TEXT,
                                  for_date DATE NOT NULL,
                                  retrieved_at TIMESTAMP NOT NULL
);

