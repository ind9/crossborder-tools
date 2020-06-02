package org.avlara;

import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class UploadHandler {

    private static String tmpDir = System.getProperty("app.tmp.dir","/tmp/");
    private static String templatedb = System.getProperty("app.db.template","conf/template.sql3");

    public static final Route excelUpload = new Route() {

        @Override
        public Object handle(Request request, Response response) throws Exception {

            // clone sql db
            ServiceResponse<ValidationResponse>  validatonResponse = new ServiceResponse<ValidationResponse> ();

            validatonResponse.setData(new ValidationResponse());


            request.attribute("org.eclipse.jetty.multipartConfig",new MultipartConfigElement(tmpDir));
            SqliteInserter sqldb = new SqliteInserter(templatedb,tmpDir);

            boolean dbSetupSuccess = sqldb.testdb();
            if(!dbSetupSuccess)
            {
                validatonResponse.getErrors().add("DB Setup failed");
                sqldb.cleanup();
            }else {
                String year = request.queryParams("year");
                if(year == null || year.trim().isEmpty()) {
                    year = "2017";
                }
                try(InputStream efis = request.raw().getPart("uploaded_file").getInputStream())
                {
                    ExcelTranslator translator = new ExcelTranslator();
                    translator.translate(efis,sqldb);
                    ServiceResponse<ValidationResponse>  vr =  sqldb.validate(year);
                    validatonResponse.getErrors().addAll(vr.getErrors());
                    validatonResponse.setData(vr.getData());

                }catch(Exception error) {

                }finally {
                    sqldb.cleanup();
                }
            }
            return validatonResponse;
        }
    };
}
