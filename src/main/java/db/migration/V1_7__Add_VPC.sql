drop procedure if exists drop_vpc_ref;

delimiter $$
create procedure drop_vpc_ref() begin

IF EXISTS ( SELECT * FROM information_schema.columns 
  WHERE table_name = 'class_type_detail' AND column_name = 'subnet_id'
  AND table_schema = DATABASE() ) THEN
    #ALTER TABLE instances drop FOREIGN KEY fk_CLASSTYPE_SUBNET;
    ALTER TABLE class_type_detail DROP COLUMN subnet_id;
END IF;
end$$

delimiter ;
call drop_vpc_ref();
drop procedure if exists drop_vpc_ref;

drop table if exists vpc;

create table vpc (
  vpc_id varchar(256) NOT NULL,
  subnet_id varchar(256),
  region varchar(256),
  primary key (subnet_id)
);

alter table class_type_detail add column subnet_id varchar(256);
alter table class_type_detail add index(subnet_id);
alter table class_type_detail add CONSTRAINT fk_CLASSTYPE_SUBNET foreign key (subnet_id) references vpc(subnet_id);