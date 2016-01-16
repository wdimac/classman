drop procedure if exists drop_class_id;

delimiter $$
create procedure drop_class_id() begin

IF EXISTS ( SELECT * FROM information_schema.columns 
  WHERE table_name = 'instances' AND column_name = 'class_id'
  AND table_schema = DATABASE() ) THEN
    ALTER TABLE instances drop FOREIGN KEY fk_INSTANCES_CLASS;
    ALTER TABLE instances DROP COLUMN class_id;
END IF;
end$$

delimiter ;
call drop_class_id();
drop procedure if exists drop_class_id;
drop table if exists class;
drop table if exists user;

create table user (
  id MEDIUMINT NOT NULL AUTO_INCREMENT,
  first_name varchar(255),
  last_name varchar(255),
  email varchar(256),

  primary key (id)
);

create table class (
  id MEDIUMINT NOT NULL AUTO_INCREMENT,
  descr varchar(256),
  class_type_detail_id MEDIUMINT NOT NULL,
  instructor_id MEDIUMINT,
  count INT,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  time_zone varchar(40) NOT NULL,

  primary key (id),

  CONSTRAINT fk_CLASS_DETAIL
  foreign key (class_type_detail_id)
    references class_type_detail(id),

  CONSTRAINT fk_CLASS_INTRUCTOR
  foreign key (instructor_id)
    references user(id)
);

alter table instances add column class_id MEDIUMINT;
alter table instances add index(class_id);
alter table instances add CONSTRAINT fk_INSTANCES_CLASS foreign key (class_id) references class(id);
