alter table instances add column image_id varchar(40) not null;
alter table instances add index(image_id);
delete from instances where image_id is null;
alter table instances add foreign key (image_id) references images(id);
