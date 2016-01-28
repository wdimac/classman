alter table class add column start_date_str varchar(10);
alter table class add column end_date_str varchar(10);

update class set start_date_str = DATE_FORMAT(start_date, '%Y-%m-%d');
update class set end_date_str = DATE_FORMAT(end_date, '%Y-%m-%d');

alter table class drop column start_date;
alter table class drop column end_date;