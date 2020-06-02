package org.avlara;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class SqliteInserter implements SqlInserter {

    private String db = "jdbc:sqlite:";
    private String dbname = "";
    private String queriesFilePath = System.getProperty("app.queries");
    private String queries;
    private String summary;
    private int codebatch;
    private int ratebatch;
    private Connection gconn;
    private PreparedStatement codePs;
    private PreparedStatement ratePs;
    private List<String> validationQueries;
    private List<String> summaryQueries;

    public SqliteInserter(String templateDb, String workingDir) throws IOException {
        this.queries = this.queriesFilePath.endsWith("/") ? this.queriesFilePath + "queries.sql" : this.queriesFilePath + "/queries.sql";
        this.summary = this.queriesFilePath.endsWith("/") ? this.queriesFilePath + "summary.sql" : this.queriesFilePath + "/summary.sql";
        this.codebatch = 0;
        this.ratebatch = 0;
        this.gconn = null;
        this.validationQueries = new ArrayList();
        this.summaryQueries = new ArrayList();
        String randomDB = Math.random() + ".sql";
        this.db = this.db + workingDir;
        if (!this.db.endsWith("/")) {
            this.db = this.db + "/";
        }

        this.dbname = workingDir + randomDB;
        this.db = this.db + randomDB;
        log.info("Cloning {} to {} :", templateDb, this.dbname);
        Files.copy(Paths.get(templateDb), Paths.get(this.dbname));
    }

    private SqliteInserter() {
        this.queries = this.queriesFilePath.endsWith("/") ? this.queriesFilePath + "queries.sql" : this.queriesFilePath + "/queries.sql";
        this.summary = this.queriesFilePath.endsWith("/") ? this.queriesFilePath + "summary.sql" : this.queriesFilePath + "/summary.sql";
        this.codebatch = 0;
        this.ratebatch = 0;
        this.gconn = null;
        this.validationQueries = new ArrayList();
        this.summaryQueries = new ArrayList();
    }

    public boolean testdb() {
        log.info("Trying to connect using [{}]", this.db);

        try {
            Connection conn = DriverManager.getConnection(this.db);

            try {
                Statement ps = conn.createStatement();
                ResultSet rs = ps.executeQuery("select * from codes");

                while(rs.next()) {
                    log.info(rs.getString("description"));
                }

                rs.close();
                ps.close();
                log.info("Test connection success");
            } catch (Throwable var14) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Throwable var8) {
                        var14.addSuppressed(var8);
                    }
                }

                throw var14;
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException var15) {
            log.error(this.db, var15);
            return false;
        }

        File query = new File(this.queries);

        try {
            Scanner queryScanner = new Scanner(query);

            try {
                queryScanner.useDelimiter(";");

                while(queryScanner.hasNext()) {
                    this.validationQueries.add(queryScanner.next().trim());
                }
            } catch (Throwable var12) {
                try {
                    queryScanner.close();
                } catch (Throwable var7) {
                    var12.addSuppressed(var7);
                }

                throw var12;
            }

            queryScanner.close();
        } catch (FileNotFoundException var13) {
            log.error("Cant load query file", var13);
            return false;
        }

        File summaryFile = new File(this.summary);

        try {
            Scanner queryScanner = new Scanner(summaryFile);

            try {
                queryScanner.useDelimiter(";");

                while(queryScanner.hasNext()) {
                    this.summaryQueries.add(queryScanner.next().trim());
                }
            } catch (Throwable var10) {
                try {
                    queryScanner.close();
                } catch (Throwable var6) {
                    var10.addSuppressed(var6);
                }

                throw var10;
            }

            queryScanner.close();
        } catch (FileNotFoundException var11) {
            log.error("Cant load query file", var11);
            return false;
        }

        try {
            this.gconn = DriverManager.getConnection(this.db);
            this.gconn.setAutoCommit(false);
            String scodeps = "INSERT INTO codes (rid, description, fkCodeId,sequenceNumber, parsedCode,  isSystemDefined,isTaxable,  hasChildren, isZeroPadded,isDecision, zeroPaddedCount,rateRef)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            this.codePs = this.gconn.prepareStatement(scodeps);
            String srateps = "INSERT INTO rates(rid,citationTexts,manufactureSourceType,shippingDestinationType,fKCodeId,shippingSourceType,formula,formulaType,taxSection,wco)VALUES(?,?,?,?,?,?,?,?,?,?)";
            this.ratePs = this.gconn.prepareStatement(srateps);
            return true;
        } catch (SQLException var9) {
            log.error(this.db, var9);
            return false;
        }
    }

    private void rateBulk(Rate rate) {
        try {
            this.ratePs.setInt(1, rate.getRow());
            this.ratePs.setString(2, rate.getCitationTexts());
            this.ratePs.setString(3, rate.getManufactureSourceType());
            this.ratePs.setString(4, rate.getShippingDestinationType());
            this.ratePs.setString(5, rate.getFKCodeId());
            this.ratePs.setString(6, rate.getShippingSourceType());
            this.ratePs.setString(7, rate.getFormula());
            this.ratePs.setString(8, rate.getFormulaType());
            this.ratePs.setString(9, rate.getTaxSection());
            String fk = rate.getFKCodeId();
            String wco = fk.substring(fk.lastIndexOf(95) + 1, fk.length());
            wco = wco.substring(0, 6);
            this.ratePs.setString(10, wco);
            this.ratePs.executeUpdate();
            ++this.ratebatch;
            if (this.ratebatch > 1000) {
                this.ratebatch = 0;
                this.gconn.commit();
            }
        } catch (SQLException var4) {
            log.error("Error inserting batch", var4);
        }

    }

    public ServiceResponse<ValidationResponse> validate(String year) {
        ServiceResponse<ValidationResponse> validation = new ServiceResponse();
        validation.setData(new ValidationResponse());

        try {
            this.ratePs.executeUpdate();
            this.codePs.executeUpdate();
            this.gconn.commit();
            this.ratePs.close();
            this.codePs.close();
            Statement st = this.gconn.createStatement();
            boolean atleastOneFailure = false;
            Iterator var5 = this.validationQueries.iterator();

            while(true) {
                String query;
                ResultSet rs;
                Validation summary;
                while(var5.hasNext()) {
                    query = (String)var5.next();
                    if (query.trim().isEmpty()) {
                        log.info("Skipping empty validation query");
                    } else {
                        try {
                            if (query.contains("#year#")) {
                                query = query.replaceAll("#year#", year);

                                for(rs = st.executeQuery(query); rs.next(); atleastOneFailure = true) {
                                    summary = new Validation();
                                    summary.setRow(Integer.parseInt(rs.getString("rid")));
                                    summary.setMessage(rs.getString("msg"));
                                    ((ValidationResponse)validation.getData()).getErrors().add(summary);
                                }
                            } else {
                                for(rs = st.executeQuery(query); rs.next(); atleastOneFailure = true) {
                                    summary = new Validation();
                                    summary.setRow(rs.getInt("rid") + 1);
                                    summary.setMessage(rs.getString("msg"));
                                    ((ValidationResponse)validation.getData()).getErrors().add(summary);
                                }
                            }
                        } catch (SQLException var10) {
                            log.error("Error Running query {}", query, var10);
                        }
                    }
                }

                var5 = this.summaryQueries.iterator();

                while(true) {
                    while(var5.hasNext()) {
                        query = (String)var5.next();
                        if (query.trim().isEmpty()) {
                            log.info("Skipping empty summary query");
                        } else {
                            try {
                                if (query.contains("#year#")) {
                                    query = query.replaceAll("#year#", year);
                                }

                                log.debug("Running summary query [{}]", query);

                                for(rs = st.executeQuery(query); rs.next(); atleastOneFailure = true) {
                                    summary = new Validation();
                                    summary.setRow(rs.getInt("cn"));
                                    summary.setMessage(rs.getString("msg"));
                                    ((ValidationResponse)validation.getData()).getSummary().add(summary);
                                }
                            } catch (SQLException var9) {
                                log.error("Error Running summary {}", query, var9);
                            }
                        }
                    }

                    if (atleastOneFailure) {
                        validation.getErrors().add("One or more Validations failed");
                    }

                    st.close();
                    return validation;
                }
            }
        } catch (SQLException var11) {
            log.error("Error commiting inserts", var11);
            validation.getErrors().add("One or more Validations failed");
            return validation;
        }
    }

    public void insert(Model code) {
        if (code.isHasError()) {
            log.info((String)code.getErrorMessages().stream().collect(Collectors.joining(",")));
        } else if (code instanceof Code) {
            this.codeBulk((Code)code);
        } else if (code instanceof Rate) {
            this.rateBulk((Rate)code);
        }

    }

    private void codeBulk(Code code) {
        try {
            String rateRef = code.getFkCodeId().substring(0, code.getFkCodeId().lastIndexOf(95) + 1) + code.getParsedCode();
            this.codePs.setInt(1, code.getRow());
            this.codePs.setString(2, code.getDescription());
            this.codePs.setString(3, code.getFkCodeId());
            this.codePs.setString(4, code.getSequenceNumber());
            this.codePs.setString(5, code.getParsedCode());
            this.codePs.setBoolean(6, code.isSystemDefined());
            this.codePs.setBoolean(7, code.isTaxable());
            this.codePs.setBoolean(8, code.isHasChildren());
            this.codePs.setBoolean(9, code.isZeroPadded());
            this.codePs.setBoolean(10, code.isDecision());
            this.codePs.setInt(11, code.getZeroPaddedCount());
            this.codePs.setString(12, rateRef);
            this.codePs.executeUpdate();
            ++this.codebatch;
            if (this.codebatch > 1000) {
                this.gconn.commit();
                this.codebatch = 0;
            }
        } catch (SQLException var3) {
            log.error("Error llogging code", var3);
        }

    }

    public void cleanup() {
        if (this.gconn != null) {
            try {
                this.gconn.close();
            } catch (SQLException var3) {
                log.error("Error closing", var3);
            }
        }

        try {
            Files.delete(Paths.get(this.dbname));
        } catch (Exception var2) {
            log.error(this.dbname, var2);
        }

    }

    public static void main(String[] args) throws Exception {
        String fk = "hs_kq32_01012100";
        String wco = fk.substring(fk.lastIndexOf(95) + 1, fk.length());
        wco = wco.substring(0, wco.length() - 2);
        System.out.println(wco);
    }
}
