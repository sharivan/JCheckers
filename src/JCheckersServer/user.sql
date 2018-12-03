create user 'jcheckers'@'localhost';
--set password for 'jcheckers'@'localhost' = password('fh837584rse');
alter user 'jcheckers'@'localhost' identified by 'fh837584rse';
grant all on jcheckers.* to 'jcheckers'@'localhost';
flush privileges;