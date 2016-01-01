create table security_groups (
  id varchar(40) not null,
  region varchar(40) not null,
  owner_id varchar(40) not null,
  name varchar(40),
  description varchar(255),
  primary key (id)
);
