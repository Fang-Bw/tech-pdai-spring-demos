CREATE DATABASE IF NOT EXISTS spring_transaction_test;

USE spring_transaction_test;

CREATE TABLE IF NOT EXISTS payment_info (
                              payment_id CHAR(36) NOT NULL PRIMARY KEY,
                              account_no VARCHAR(255) NOT NULL,
                              amount DOUBLE NOT NULL,
                              card_type VARCHAR(50) NOT NULL,
                              passenger_id BIGINT
);

CREATE TABLE IF NOT EXISTS passenger_info (
                                              p_id BIGINT AUTO_INCREMENT NOT NULL,
                                              name VARCHAR(255) NOT NULL,
                                              email VARCHAR(255) NOT NULL,
                                              source VARCHAR(255) NOT NULL,
                                              destination VARCHAR(255) NOT NULL,
                                              travel_date DATE NOT NULL,
                                              pickup_time VARCHAR(50),
                                              arrival_time VARCHAR(50),
                                              fare DOUBLE NOT NULL
);
