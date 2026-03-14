CREATE  TABLE user ( 
 id int ,
 username string ,
 dateregistered string ,
 password string ,
 displaypicture string ,
 statusmessage string ,
 countryid int ,
 language string ,
 emailaddress string ,
 chatroomadmin int ,
 chatroombans int ,
 registrationipaddress string ,
 registrationdevice string ,
 firstlogindate string ,
 lastlogindate string ,
 mobilephone string ,
 mobiledevice string ,
 useragent string ,
 mobileverified int ,
 type int ,
 merchantcreated string ,
 referredby string ,
 balance double ,
 fundedbalance double ,
 status int ,
 currency string ,
 verificationcode string ,
 notes string  ) 
 ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
WITH SERDEPROPERTIES ( 'serialization.format'='\001',
 'field.delim'='\001' )  STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' 
 OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ;


