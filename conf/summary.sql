SELECT * FROM (
select count(*) as cn, "RATES: Total WCO codes in RATES" as msg from (select distinct(wco) from rates)
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
select count(*) as cn, "WCO: (#year#) Total HS-Codes" as msg from hscodes where year = #year#);