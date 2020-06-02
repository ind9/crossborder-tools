package org.avlara;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class SqliteInserter implements SqlInserter {

    private String db = "jdbc:sqlite:";
    private String dbname = "",
            queriesFilePath = System.getProperty("app.queries"),
            queries = queriesFilePath.endsWith("/")? queriesFilePath + "queries.sql" : queriesFilePath +"/queries.sql",
            summary = queriesFilePath.endsWith("/")? queriesFilePath + "summary.sql" : queriesFilePath +"/summary.sql";

    private int codebatch = 0, ratebatch = 0;

    private Connection gconn = null;
    private PreparedStatement codePs, ratePs;

    private List<String> validationQueries = new ArrayList<>(),
                         summaryQueries = new ArrayList<>();

    public SqliteInserter(String templateDb, String workingDir) throws IOException
    {
        String randomDB = Math.random()+".sql";
        db += workingDir;
        if(!db.endsWith("/"))
        {
            db +="/";
        }
        dbname = workingDir + randomDB;
        db += randomDB;

        log.info("Cloning {} to {} :",templateDb, dbname);

        Files.copy(Paths.get(templateDb),Paths.get(dbname));

    }

    private SqliteInserter() {

    }

    public boolean testdb() {
        log.info("Trying to connect using [{}]",db);
        try(Connection conn = DriverManager.getConnection(db)) {

            Statement ps = conn.createStatement();
            ResultSet rs = ps.executeQuery("select * from codes");

            while(rs.next())
            {
                log.info(rs.getString("description"));
            }
            rs.close();
            ps.close();

            log.info("Test connection success");

        }catch(SQLException sqle){
            sqle.printStackTrace();
            log.error(db,sqle);
            return false;
        }

        File query = new File(queries);
        try(Scanner queryScanner = new Scanner(query)) {
            queryScanner.useDelimiter(";");

            while (queryScanner.hasNext()) {
                validationQueries.add(queryScanner.next().trim());
            }
        }catch(FileNotFoundException fne) {
            log.error("Cant load query file",fne);
            return false;
        }

        File summaryFile = new File(summary);
        try(Scanner queryScanner = new Scanner(summaryFile)) {
            queryScanner.useDelimiter(";");

            while (queryScanner.hasNext()) {
                summaryQueries.add(queryScanner.next().trim());
            }
        }catch(FileNotFoundException fne) {
            log.error("Cant load query file",fne);
            return false;
        }

        try {
            gconn = DriverManager.getConnection(db);
            gconn.setAutoCommit(false);

            String scodeps = "INSERT INTO codes (rid, description, fkCodeId,"+
            "sequenceNumber, parsedCode,  isSystemDefined,"+
            "isTaxable,  hasChildren, isZeroPadded,"+
            "isDecision, zeroPaddedCount,rateRef)"+
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

            codePs = gconn.prepareStatement(scodeps);

            String srateps = "INSERT INTO rates(" +
            "rid,citationTexts,manufactureSourceType,"+
            "shippingDestinationType,fKCodeId,shippingSourceType,"+
            "formula,formulaType,taxSection,wco)"+
            "VALUES(?,?,?,?,?,?,?,?,?,?)";

            ratePs = gconn.prepareStatement(srateps);




        }catch(SQLException sqle){
            log.error(db,sqle);
            return false;
        }
        return true;
    }

    private void rateBulk(Rate rate) {
        //log.info(rate.ts());
        try {

            ratePs.setInt(1, rate.getRow());
            ratePs.setString(2,rate.getCitationTexts());
            ratePs.setString(3,rate.getManufactureSourceType());

            ratePs.setString(4,rate.getShippingDestinationType());
            ratePs.setString(5,rate.getFKCodeId());
            ratePs.setString(6,rate.getShippingSourceType());

            ratePs.setString(7,rate.getFormula());
            ratePs.setString(8,rate.getFormulaType());
            ratePs.setString(9,rate.getTaxSection());

            String fk = rate.getFKCodeId();
            String wco = fk.substring(fk.lastIndexOf('_')+1,fk.length());

            wco = wco.substring(0,6);

            ratePs.setString(10,wco);

            ratePs.executeUpdate();
            /*
            ratePs.addBatch();
            ratebatch++;

            if(ratebatch > 1000)
            {
                ratebatch = 0;
                ratePs.executeUpdate();
            }
            */
        }catch(SQLException sqle){
            log.error("Error inserting batch",sqle);
        }
    }



    public ServiceResponse<ValidationResponse>  validate(String year)
    {
        ServiceResponse<ValidationResponse> validation = new ServiceResponse<ValidationResponse>();

        validation.setData(new ValidationResponse());

        try {

            ratePs.executeUpdate();
            codePs.executeUpdate();

            gconn.commit();

            ratePs.close();
            codePs.close();

            Statement st = gconn.createStatement();
            boolean atleastOneFailure = false;
            for(String query : validationQueries)
            {
                if(query.contains("#year#"))
                {
                    query = query.replaceAll("#year#",year);
                    ResultSet rs = st.executeQuery(query);
                    while (rs.next()) {
                        Validation failure = new Validation();
                        failure.setRow(Integer.parseInt(rs.getString("rid")));
                        failure.setMessage(rs.getString("msg"));

                        validation.getData().getErrors().add(failure);
                        atleastOneFailure = true;
                    }

                }else {
                    //log.info("Executing [{}]",query);
                    ResultSet rs = st.executeQuery(query);
                    while (rs.next()) {
                        Validation failure = new Validation();
                        failure.setRow((rs.getInt("rid") + 1));
                        failure.setMessage(rs.getString("msg"));

                        validation.getData().getErrors().add(failure);
                        atleastOneFailure = true;
                    }
                }
            }
            // summary
            for(String query : summaryQueries) {
                if(query.contains("#year#")) {
                    query = query.replaceAll("#year#", year);
                }
                ResultSet rs = st.executeQuery(query);
                while (rs.next()) {
                    Validation summary = new Validation();
                    summary.setRow((rs.getInt("cn")));
                    summary.setMessage(rs.getString("msg"));

                    validation.getData().getSummary().add(summary);
                    atleastOneFailure = true;
                }
            }
            if(atleastOneFailure){
                validation.getErrors().add("One or more Validations failed");
            }
            st.close();

        }catch(SQLException sqle) {
            log.error("Error commiting inserts",sqle);
            validation.getErrors().add("One or more Validations failed");
        }

        return  validation;
    }

    @Override
    public void insert(Model code)
    {
        if(code instanceof  Code) {
            codeBulk((Code)code);
        }else if(code instanceof Rate ) {
            rateBulk((Rate)code);
        }
    }



    private void codeBulk(Code code) {

        //log.info(code.ts());
        try
        {
            String rateRef = code.getFkCodeId().substring(0,code.getFkCodeId().lastIndexOf('_')+1) +
                             code.getParsedCode();

            codePs.setInt(1,code.getRow());
            codePs.setString(2,code.getDescription());
            codePs.setString(3,code.getFkCodeId());
            codePs.setString(4,code.getSequenceNumber());
            codePs.setString(5,code.getParsedCode());
            codePs.setBoolean(6,code.isSystemDefined());
            codePs.setBoolean(7,code.isTaxable());
            codePs.setBoolean(8,code.isHasChildren());
            codePs.setBoolean(9,code.isZeroPadded());
            codePs.setBoolean(10,code.isDecision());
            codePs.setInt(11,code.getZeroPaddedCount());
            codePs.setString(12,rateRef);

            codePs.executeUpdate();
            /*
            codePs.addBatch();
            codebatch++;
            if(codebatch > 1000)
            {
                codePs.executeUpdate();
                codebatch = 0;
            }
            */
        }catch(SQLException sqle) {
            log.error("Error llogging code",sqle);
        }
    }


    public void cleanup()
    {
        if(gconn != null) {
            try {
                gconn.close();
            } catch (SQLException sqle) {
                log.error("Error closing", sqle);
            }
        }
        try
        {
            Files.delete(Paths.get(dbname));
        }catch(Exception e){
            log.error(dbname,e);
        }
    }

    public static void main(String[] args) throws Exception {

        String fk = "hs_kq32_01012100";

        String wco = fk.substring(fk.lastIndexOf('_')+1,fk.length());

        wco = wco.substring(0,wco.length()-2);

        System.out.println(wco);

    }
}
