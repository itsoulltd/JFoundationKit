CREATE TABLE IF NOT EXISTS Passenger
(
  id         BIGINT PRIMARY KEY,
  name       VARCHAR(1024) NOT NULL,
  age        INT CHECK (age > 0),
  sex        VARCHAR(12) NULL,
  dob        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  active     BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS Person
(
  uuid       VARCHAR(512) PRIMARY KEY,
  name       VARCHAR(512) NULL,
  age        INT CHECK (age > 0),
  gender     VARCHAR(12) NULL,
  email      VARCHAR(255) UNIQUE,
  active     BOOLEAN DEFAULT FALSE,
  salary     DECIMAL(10, 2) NULL,
  dob        TIMESTAMP     NULL,
  height     DECIMAL(10, 2) NULL,
  createDate TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);