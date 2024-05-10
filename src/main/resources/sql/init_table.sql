create table customer(
    id bigint not null auto_increment primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null
);

create table customer2(
    id bigint not null auto_increment primary key,
    full_name varchar(255) not null,
    created_at timestamp(6) not null
);
