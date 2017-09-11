DROP DATABASE IF EXISTS sharewoodBootDB;

CREATE DATABASE sharewoodBootDB DEFAULT CHARACTER SET 'utf8'
  DEFAULT COLLATE 'utf8_unicode_ci';

USE sharewoodBootDB;


CREATE TABLE photo(
    id        	BIGINT NOT NULL AUTO_INCREMENT,
    title  	VARCHAR(20) NULL,
    username    VARCHAR(20) NOT NULL,
    shared	BOOLEAN NOT NULL, 	
    PRIMARY KEY (id)
) ENGINE = InnoDB;


create table oauth_client_details (
  client_id VARCHAR(30) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
) ENGINE = InnoDB;

create table oauth_client_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(30),
  user_name VARCHAR(256),
  client_id VARCHAR(256)
) ENGINE = InnoDB;

create table oauth_access_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
) engine = InnoDB;

create table oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
) ENGINE = InnoDB;

create table oauth_code (
  code VARCHAR(256), authentication BLOB
) ENGINE = InnoDB;

create table oauth_approvals (
	userId VARCHAR(256),
	clientId VARCHAR(256),
	scope VARCHAR(256),
	status VARCHAR(10),
	expiresAt TIMESTAMP,
	lastModifiedAt TIMESTAMP
) ENGINE = InnoDB;


CREATE TABLE user (
  userId BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(30) NOT NULL,
  hashedPassword BINARY(60) NOT NULL,
  accountNonExpired BOOLEAN NOT NULL,
  accountNonLocked BOOLEAN NOT NULL,
  credentialsNonExpired BOOLEAN NOT NULL,
  enabled BOOLEAN NOT NULL,
  CONSTRAINT user_unique UNIQUE (username)
) ENGINE = InnoDB;

CREATE TABLE user_Authority (
  userId BIGINT UNSIGNED NOT NULL,
  authority VARCHAR(100) NOT NULL,
  UNIQUE KEY user_Authority_User_Authority (userId, authority),
  CONSTRAINT user_Authority_UserId FOREIGN KEY (userId)
    REFERENCES user (userId) ON DELETE CASCADE
) ENGINE = InnoDB;




INSERT INTO user (username, hashedPassword, accountNonExpired,
                           accountNonLocked, credentialsNonExpired, enabled)
VALUES ( -- s1a2t3o4r
  'Carol', '$2a$10$.6kYphQ8VqJ9NPKtMje.JeWt1aoX4/ZRzVFGiO7Cen.rk88laGTCi',
  TRUE, TRUE, TRUE, TRUE
);

INSERT INTO user (username, hashedPassword, accountNonExpired,
                           accountNonLocked, credentialsNonExpired, enabled)
VALUES ( -- a5r6e7p8o
  'Albert', '$2a$10$CXNlyhzVkeHbqNpIJNBTl.WXP9WVouQwqh7M7IkI/WTXywakU5kha',
  TRUE, TRUE, TRUE, TRUE
);

INSERT INTO user (username, hashedPassword, accountNonExpired,
                           accountNonLocked, credentialsNonExpired, enabled)
VALUES ( -- t4e3n2e1t
  'Werner', '$2a$10$BKSkkYAh5COX/vvQrwPwuucL77Ydf61EEd97kwaBndKtxHJktQX/S',
  TRUE, TRUE, TRUE, TRUE
);

INSERT INTO user (username, hashedPassword, accountNonExpired,
                           accountNonLocked, credentialsNonExpired, enabled)
VALUES ( -- o8p7e6r5a
  'Alice', '$2a$10$fDcpl4fYkqyaKwVfBOFwTu5igi7yXzCw2AWp0oSkZ0iwMXzZsZ2t.',
  TRUE, TRUE, TRUE, TRUE
);

INSERT INTO user (username, hashedPassword, accountNonExpired,
                           accountNonLocked, credentialsNonExpired, enabled)
VALUES ( -- r1o2t3a4s
  'Richard', '$2a$10$ej/Cmw3p0b8UbWOQAhiEW.2LW03nspV2yLFaoli0CdxK8./miktoW',
  TRUE, TRUE, TRUE, TRUE
);



INSERT INTO user_Authority (UserId, Authority)
  VALUES (1, 'USER');

INSERT INTO user_Authority (UserId, Authority)
  VALUES (2, 'USER');

INSERT INTO user_Authority (UserId, Authority)
  VALUES (3, 'USER');

INSERT INTO user_Authority (UserId, Authority)
  VALUES (3, 'SQUIRREL');

INSERT INTO user_Authority (UserId, Authority)
  VALUES (4, 'USER');

INSERT INTO user_Authority (UserId, Authority)
  VALUES (5, 'ADMIN');

   


INSERT INTO photo VALUES (
  '1', 'photo1', 'Alice', 'false'
);

INSERT INTO photo VALUES (
  '2', 'photo2', 'Carol', 'false'
);

INSERT INTO photo VALUES (
  '3', 'photo3', 'Alice', 'false'
);

INSERT INTO photo VALUES (
  '4', 'photo4', 'Carol', 'false'
);

INSERT INTO photo VALUES (
  '5', 'photo5', 'Alice', 'false'
);

INSERT INTO photo VALUES (
  '6', 'photo6', 'Carol', 'false'
);


-- Store a single client with authorization code grant 

INSERT INTO oauth_client_details (
  client_id, 
  resource_ids, 
  client_secret, 
  scope, 
  authorized_grant_types, 
  web_server_redirect_uri,
  authorities,
  access_token_validity,
  refresh_token_validity,
  additional_information,
  autoapprove)
VALUES('Fleetwood', 'SHAREWOOD', 'y471l12D2y55U5558rd2', 'READ,WRITE,DELETE', 'authorization_code', null, 'ROLE_CLIENT', '520', null, '{}', null

);




