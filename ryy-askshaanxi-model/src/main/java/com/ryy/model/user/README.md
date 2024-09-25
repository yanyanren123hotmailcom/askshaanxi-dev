ApUserLogin对应表：
ap_user_login:
create table askshaanxi_user.ap_user_login
(
id       int unsigned auto_increment comment '主键'
primary key,
salt     varchar(32) null comment '密码、通信等加密盐',
password varchar(32) null comment '密码,md5加密',
phone    varchar(11) null comment '手机号',
constraint phone
unique (phone)
)
comment 'APP用户登录信息表';


插入数据：

insert into askshaanxi_user.ap_user_login values (1,"abc","bf97b2b1c6869723142956aa8dca0c49","15091148044");
insert into askshaanxi_user.ap_user_login values (2,"abc123","b0e1960ffdde7e2be0ad9686b966732a","13361039243");
insert into askshaanxi_user.ap_user_login values (3,"sdsa","9eb995eed358b8cbd7c03d89af58c093","18351697229");
insert into askshaanxi_user.ap_user_login values (4,"abc123","b0e1960ffdde7e2be0ad9686b966732a","13511223456");
insert into askshaanxi_user.ap_user_login values (5,"123","cb71e13d79d17b7973b36f77d72f78ab","14709649778");
insert into askshaanxi_user.ap_user_login values (6,"abcd","3d7417156c58640293d7a23e92b2bc75","15877602743");

