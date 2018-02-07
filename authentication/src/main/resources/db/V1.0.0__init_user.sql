CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_username_ind` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `ur_role_id_ind` (`role_id`),
  CONSTRAINT `ur_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `ur_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  CONSTRAINT `profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO user(username,password)
VALUES ('user2','$2a$10$LYMs2dw2l9h9SCaI/uUDjO.kukhP1FwAxpgYoLc2a67N9dUeBUGMO');
INSERT INTO user(username,password)
VALUES ('admin','$2a$10$LYMs2dw2l9h9SCaI/uUDjO.kukhP1FwAxpgYoLc2a67N9dUeBUGMO');
INSERT INTO user(username,password)
VALUES ('user','$2a$10$LYMs2dw2l9h9SCaI/uUDjO.kukhP1FwAxpgYoLc2a67N9dUeBUGMO');

INSERT INTO profile(first_name,last_name,user_id)
select 'admin',null, user.id from user where username="admin";
INSERT INTO profile(first_name,last_name,user_id)
select 'user','2', user.id from user where username="user2";
INSERT INTO profile(first_name,last_name,user_id)
select 'user','1', user.id from user where username="user";

INSERT INTO role(role,description)
VALUES ('ROLE_ADMIN',null);
INSERT INTO role(role,description)
VALUES ('ROLE_USER',null);

INSERT INTO user_roles(user_id,role_id)
select user.id,role.id from user left join role on role.role='ROLE_ADMIN' where user.username='admin' ;
INSERT INTO user_roles(user_id,role_id)
select user.id,role.id from user left join role on role.role='ROLE_USER' where user.username='user' ;
INSERT INTO user_roles(user_id,role_id)
select user.id,role.id from user left join role on role.role='ROLE_USER' where user.username='user2' ;

