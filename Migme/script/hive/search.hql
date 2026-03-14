insert overwrite table precompiled_recommendations partition(rectype='ABC',domaintype='U',datecreated='2013-06-03')

select distinct 
union_recommended_items.recommendeditemid,
union_recommended_items.score,
union_recommended_items.targetID
from(
select distinct
u.id as recommendeditemid,
100 as score,
abc.submitteruserid as targetID
from
addressbookcontactslog abc join user u on(abc.contacttype=1 and u.mobileverified=1 and
u.mobilephone = abc.contactvalue ) 
join userprofile up on (u.id = up.id and up.status=1)

union all

select distinct
u.id as recommendeditemid,
100 as score,
abc.submitteruserid as targetID
from
addressbookcontactslog abc join user u on(abc.contacttype=1 and u.mobileverified=1 and
substr(u.mobilephone,1) = abc.contactvalue  ) 
join userprofile up on (u.id = up.id and up.status=1)

union all

select distinct
u.id as recommendeditemid,
100 as score,
abc.submitteruserid as targetID
from
addressbookcontactslog abc join user u on(abc.contacttype=1 and u.mobileverified=1 and
substr(u.mobilephone,2) = abc.contactvalue  ) 
join userprofile up on (u.id = up.id and up.status=1)


union all

select distinct
u.id as recommendeditemid,
100 as score,
abc.submitteruserid as targetID
from
addressbookcontactslog abc join user u on(abc.contacttype=1 and u.mobileverified=1 and
substr(u.mobilephone,3) = abc.contactvalue  ) 
join userprofile up on (u.id = up.id and up.status=1)

) union_recommended_items;


