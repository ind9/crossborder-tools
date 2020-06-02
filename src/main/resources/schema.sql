DROP TABLE IF EXISTS codes;
DROP TABLE IF EXISTS rates;
DROP TABLE IF EXISTS hscodes;

CREATE TABLE codes(
    rid int ,
    description varchar (5000),
    fkCodeId varchar (256),
    sequenceNumber varchar (256),
    parsedCode varchar (20),
    isSystemDefined boolean ,
    isTaxable boolean,
    hasChildren boolean,
    isZeroPadded boolean,
    isDecision boolean,
    zeroPaddedCount int,
    rateRef varchar(256)
);

CREATE TABLE rates(
   rid int,
   citationTexts varchar (5000),
   manufactureSourceType  varchar (500),
   shippingDestinationType  varchar (500),
   fKCodeId  varchar (256),
   shippingSourceType  varchar (500),
   formula  varchar (500),
   formulaType  varchar (500),
   taxSection  varchar (500),
   fKShippingSourceGroupId  varchar (500),
   fKShippingSourceCountryId  varchar (500),
   wco varchar(10)
);

CREATE TABLE hscodes(
    hscode varchar (25),
    year int,
    description varchar (500)
);

DROP INDEX IF EXISTS uq_hscode;
CREATE INDEX uq_hscode ON hscodes(hscode,year);

DROP INDEX IF EXISTS ix_codes;
CREATE INDEX ix_code ON codes(parsedCode,isSystemDefined,isTaxable,hasChildren,isZeroPadded,isDecision);

DROP INDEX IF EXISTS ix_rates;
CREATE INDEX ix_rates ON rates(fKCodeId);

