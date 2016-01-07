drop table if exists class_type_detail;
drop table if exists class_type;

create table class_type (
  id MEDIUMINT NOT NULL AUTO_INCREMENT,
  name varchar(256),
  duration SMALLINT NOT NULL,
  primary key (id)
);

create table class_type_detail (
  id MEDIUMINT NOT NULL AUTO_INCREMENT,
  class_type_id MEDIUMINT,
  region varchar(40),
  image_id varchar(40),
  instance_type varchar(40),
  security_group_id varchar(40),

  primary key (id),

  foreign key (class_type_id)
    references class_type(id)
    on delete cascade,

  foreign key (image_id)
    references images(id),

  foreign key (security_group_id)
    references security_groups(id)
);