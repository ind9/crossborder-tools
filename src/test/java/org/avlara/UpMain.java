package org.avlara;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class UpMain {

    public static void main(String[] args) throws Exception {
        String templatedb = "",
                tmpDir = "";

        SqliteInserter sqldb = new SqliteInserter("/var/depot/src/avhack/conf/template.sql3","/var/depot/src/avhack/tmp/wrk/");

        boolean dbSetupSuccess = sqldb.testdb();
        if(!dbSetupSuccess)
        {
            log.info("DB Setup failed");
            sqldb.cleanup();
        }else {
            try(InputStream efis = new FileInputStream(new File(args[0])))
            {
                ExcelTranslator translator = new ExcelTranslator();

                translator.translate(efis,sqldb);

                sqldb.validate("2017");

            }catch(Exception error) {
                log.error("Error upload",error);
            }finally {
                sqldb.cleanup();
            }
        }

    }
}
