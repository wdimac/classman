drop procedure if exists drop_pool_user_id;

delimiter $$
create procedure drop_pool_user_id() begin

IF EXISTS ( SELECT * FROM information_schema.columns 
  WHERE table_name = 'eip' AND column_name = 'pool_user_id'
  AND table_schema = DATABASE() ) THEN
    ALTER TABLE eip DROP FOREIGN KEY fk_EIP_POOL_USER;
    ALTER TABLE eip DROP COLUMN pool_user_id;
END IF;

IF EXISTS ( SELECT * FROM information_schema.columns 
  WHERE table_name = 'security_groups' AND column_name = 'vpc_id'
  AND table_schema = DATABASE() ) THEN
    ALTER TABLE security_groups DROP COLUMN vpc_id;
END IF;

end$$

delimiter ;
call drop_pool_user_id();
drop procedure if exists drop_pool_user_id;


alter table eip add column pool_user_id mediumint;
alter table eip add index(pool_user_id);
alter table eip add constraint fk_EIP_POOL_USER foreign key (pool_user_id) references user(id);

alter table security_groups add column vpc_id varchar(40);
