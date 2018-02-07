-- ACL Schema SQL for MySQL 5.5+ / MariaDB equivalent

-- drop table acl_entry;
-- drop table acl_object_identity;
-- drop table acl_class;
-- drop table acl_sid;

CREATE TABLE acl_sid (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    principal BOOLEAN NOT NULL,
    sid VARCHAR(100) NOT NULL,
    UNIQUE KEY unique_acl_sid (sid, principal)
) ENGINE=InnoDB;

CREATE TABLE acl_class (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    class VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_acl_class (class)
) ENGINE=InnoDB;

CREATE TABLE acl_object_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    object_id_class BIGINT UNSIGNED NOT NULL,
    object_id_identity BIGINT NOT NULL,
    parent_object BIGINT UNSIGNED,
    owner_sid BIGINT UNSIGNED,
    entries_inheriting BOOLEAN NOT NULL,
    UNIQUE KEY uk_acl_object_identity (object_id_class, object_id_identity),
    CONSTRAINT fk_acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
    CONSTRAINT fk_acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB;

CREATE TABLE acl_entry (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    acl_object_identity BIGINT UNSIGNED NOT NULL,
    ace_order INTEGER NOT NULL,
    sid BIGINT UNSIGNED NOT NULL,
    mask INTEGER UNSIGNED NOT NULL,
    granting BOOLEAN NOT NULL,
    audit_success BOOLEAN NOT NULL,
    audit_failure BOOLEAN NOT NULL,
    UNIQUE KEY unique_acl_entry (acl_object_identity, ace_order),
    CONSTRAINT fk_acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
    CONSTRAINT fk_acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id)
) ENGINE=InnoDB;


CREATE TABLE permission (
	id MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name varchar(255) NOT NULL,
	description varchar(255) DEFAULT NULL
);

CREATE INDEX acl_entry_obj_id
ON acl_entry (acl_object_identity);

INSERT INTO permission(name,description)
VALUES ('GET_PROFILE','Get profile data');

INSERT INTO acl_class(class)
VALUES ('com.obs.dto.Profile');

INSERT INTO acl_sid(principal, sid)
select 1, username from user;

-- this object identify 0 for com.obs.dto.Profile class indicates all profile instances
INSERT INTO acl_object_identity(object_id_class,object_id_identity,owner_sid,entries_inheriting)
select acl_class.id, 0,acl_sid.id,0 from user left join acl_class on acl_class.class='com.obs.dto.Profile'
left join acl_sid on acl_sid.sid='admin'
where user.username='admin';
INSERT INTO acl_object_identity(object_id_class,object_id_identity,owner_sid,entries_inheriting)
select acl_class.id, profile.id,acl_sid.id,0 from user left join acl_class on acl_class.class='com.obs.dto.Profile'
left join acl_sid on acl_sid.sid='user'
left join profile on profile.user_id= user.id
where user.username='user';
INSERT INTO acl_object_identity(object_id_class,object_id_identity,owner_sid,entries_inheriting)
select acl_class.id, profile.id,acl_sid.id,0 from user left join acl_class on acl_class.class='com.obs.dto.Profile'
left join acl_sid on acl_sid.sid='user2'
left join profile on profile.user_id= user.id
where user.username='user2';

INSERT INTO acl_entry(acl_object_identity,ace_order,sid,mask,granting,audit_success,audit_failure)
select acl_object_identity.id,0,acl_sid.id,1,1,0,0  from acl_object_identity inner join acl_class on acl_object_identity.object_id_class=acl_class.id
inner join acl_sid on acl_object_identity.owner_sid = acl_sid.id
inner join user on acl_sid.sid = user.username and user.username='admin'
where acl_class.class='com.obs.dto.Profile';
INSERT INTO acl_entry(acl_object_identity,ace_order,sid,mask,granting,audit_success,audit_failure)
select acl_object_identity.id,0,acl_sid.id,1,1,0,0  from acl_object_identity inner join acl_class on acl_object_identity.object_id_class=acl_class.id
inner join acl_sid on acl_object_identity.owner_sid = acl_sid.id
inner join user on acl_sid.sid = user.username and user.username='user'
where acl_class.class='com.obs.dto.Profile';
INSERT INTO acl_entry(acl_object_identity,ace_order,sid,mask,granting,audit_success,audit_failure)
select acl_object_identity.id,0,acl_sid.id,1,1,0,0  from acl_object_identity inner join acl_class on acl_object_identity.object_id_class=acl_class.id
inner join acl_sid on acl_object_identity.owner_sid = acl_sid.id
inner join user on acl_sid.sid = user.username and user.username='user2'
where acl_class.class='com.obs.dto.Profile';