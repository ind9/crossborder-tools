SELECT c.rid, "CODES: IsTaxable is TRUE. HasChildren an IsDecision must be FALSE" as msg
FROM codes c WHERE c."isTaxable" = 1 AND (c."hasChildren" = 1 OR c."isDecision" = 1);

SELECT c.rid,"CODES: For HangingCode (+), IsDecision must be TRUE" as msg
FROM codes c WHERE instr("parsedCode",'+') AND "isDecision" = 0;

SELECT c.rid,"CODES: HasChildren and IsTaxable are both can't be TRUE" as msg
FROM codes c WHERE "hasChildren" = 1 and "isTaxable" = 1;

SELECT c.rid,"CODES: ZeroPaddedCount is 0 but isZeroPadded must be FALSE" as msg
FROM codes c WHERE "zeroPaddedCount" == 0 AND "isZeroPadded" = 1;

SELECT c.rid,zeroPaddedCount,"CODES: ZeroPaddedCount is greater than 0. isZeroPadded must be TRUE" as msg
FROM codes c WHERE "zeroPaddedCount" > 0 AND "isZeroPadded" = 0;

SELECT f.rid,"CODES: IsTaxable is True but no corresponding rate found" as msg
FROM codes f LEFT JOIN rates r ON f."rateRef"=r."fKCodeId"
WHERE f."isTaxable" = 1 and r.fKCodeId is NULL;

SELECT R.rid,"RATES: No Matching IsTaxable rows found in CODES" as msg
FROM rates R LEFT JOIN (SELECT rateRef FROM codes WHERE "isTaxable" = 1) AS TC ON R.fKCodeId = TC.rateRef
WHERE TC.rateRef IS NULL;

SELECT H.hscode as rid,'WCO: Missing sub-header' || H.description as msg
FROM hscodes H LEFT JOIN ( select distinct(wco) as dwco from rates) TC ON H.hscode = TC.dwco
WHERE H.year = #year# AND TC.dwco IS NULL;