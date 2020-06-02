
select * from rates;
select * from codes where "isTaxable" = 1;

insert into codes(rid,description,"fkCodeId","sequenceNumber","parsedCode","isSystemDefined","isTaxable","hasChildren","isZeroPadded","isDecision","zeroPaddedCount","rateRef")
values(10,"valid","fkCodeId","seq-00","pscode",0,1,0,0,0,0,"shitpdes-valid")

insert into rates(rid,"citationTexts","manufactureSourceType","shippingDestinationType","fKCodeId","shippingSourceType",formula,"formulaType","taxSection")
values(2,"extra-valid","ex-man-valid","exman-source-valid","exshitpdes-valid","ex-valid-fkCodeId","ex-valid-ship-sc","ex-form","ex-manual")

select * from rates;

SELECT H.hscode as rid,'WCO: ' || H.description as msg
FROM hscodes H LEFT JOIN (select distinct substr(wco,0,6) as wco from rates) TC ON H.hscode = TC.wco
WHERE H.year = 2017 AND TC.wco IS NULL;

SELECT H.hscode as rid,'WCO: ' || H.description as msg FROM hscodes H LEFT JOIN ( select (distinct wco) as dwco from rates) TC ON H.hscode = TC.dwco WHERE H.year = 2017 AND TC.wco IS NULL;

select count(*) from hscodes wher year = 2017;
-- 10592

select count(*) from
(select distinct wco from rates);

select distinct substr(wco,0,6) from rates;

SELECT * FROM (select count(distinct substr(wco,0,6)) as cn, "RATES: Distinct WCO codes." as msg from rates
union
select count(*) as cn, "CODES: Total rows." as msg from codes
union
select count(*) as cn, "CODES: Total IsTaxable rows." as msg from codes where "isTaxable" = 1
union
select count(*) as cn, "CODES: Total IsSystemDefined rows" as msg from codes where "isSystemDefined" = 1
union
select count(*) as cn, "CODES: Total  ZeroPadded rows" as msg from codes where "isZeroPadded" = 1
union
select count(*) as cn, "CODES: Total Hanging Codes" as msg from codes where "isDecision" = 1
union
select count(*) as cn, "WCO: Total HS-Codes" as msg from hscodes where year = 2017);



--18266