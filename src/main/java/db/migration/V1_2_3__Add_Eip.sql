create table eip (
  id MEDIUMINT NOT NULL AUTO_INCREMENT,
  region varchar(40),
  description varchar(40),
  instance_id varchar(40),
  public_ip varchar(40) not null,
  allocation_id varchar(40),
  association_id varchar(40),
  domain varchar(40),
  network_interface_id varchar(40),
  network_interface_owner_id varchar(40),
  private_ip_address varchar(40),
  primary key (id)
);

