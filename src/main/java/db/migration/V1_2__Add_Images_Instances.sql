create table images (
  id varchar(40) not null,
  region varchar(40) not null,
  description varchar(255),
  primary key (id)
);

create table instances (
	id varchar(40) not null,
  region varchar(40) not null,
  description varchar(255),
  is_terminated boolean,
  primary key (id)
);