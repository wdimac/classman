alter table class_type_detail drop foreign key class_type_detail_ibfk_3,
  drop column security_group_id;
alter table security_groups add column user_id MEDIUMINT, 
  add foreign key fk_sec_grp_user(user_id) references user(id);
alter table class add column security_group_id varchar(40),
  add foreign key fk_sec_grp_class(security_group_id) references security_groups(id);
  