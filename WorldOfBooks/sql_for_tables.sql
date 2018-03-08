create table orders
             (
                          order_id          int not null primary key
                        , buyer_name        varchar(255) not null
                        , buyer_email       varchar(255) not null
                        , order_date        date
                        , order_total_value decimal(20,2) not null
                        , address           varchar(255) not null
                        , postcode          int not null
             )
;

create table order_item
             (
                          order_item_id    int not null primary key
                        , order_id         int not null
                        , sale_price       decimal(20,2) not null
                        , shipping_price   decimal(20,2) not null
                        , total_item_price decimal(20,2) not null
                        , sku              varchar(255) not null
                        , status enum('IN_STOCK','OUT_OF_STOCK') not null
             )
;

alter table order_item add foreign key fk_order_id(order_id) references orders(order_id)
on
delete
       restrict
on
update
       cascade
;