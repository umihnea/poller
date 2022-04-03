create database poller;
-- grant all privileges on poller.* to '<user>'@'localhost';

-- initial table creation
use poller;
create table node (
  id int not null auto_increment,
  name varchar(120) not null,
  url varchar(400) not null,
  created_at timestamp,
  primary key (id)
);

create table record (
  id int not null auto_increment,
  node_id int not null,
  status_code int,
  state int not null,
  delay int,
  created_at timestamp,
  primary key (id),
  foreign key (node_id) references node(id)
);
