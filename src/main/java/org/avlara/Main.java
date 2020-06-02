package org.avlara;

import spark.Service;
import spark.staticfiles.StaticFilesConfiguration;

public class Main {

    private static final JsonTransformer jsonTransformer = new JsonTransformer();
    public static void main(String[] args) throws Exception{

        ViewHandler.init();
        Service service = Service.ignite();

        service.ipAddress("0.0.0.0");
        service.port(8080);
        configure(service);
    }

    public static void configure(Service service) {

        service.path("/",()->{
            service.get("",ViewHandler.index);

            service.post("validate",UploadHandler.excelUpload,jsonTransformer);

        });

        String appStatic = System.getProperty("app.static","web");
        if (appStatic != null) {
            // configure static files
            StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
            staticHandler.configureExternal(appStatic);

            service.get("/css/*", (request, response) -> {
                staticHandler.consume(request.raw(), response.raw());
                return "OK";
            });

            service.get("/webfonts/*", (request, response) -> {
                staticHandler.consume(request.raw(), response.raw());
                return "OK";
            });

            service.get("/img/*", (request, response) -> {
                staticHandler.consume(request.raw(), response.raw());
                return "OK";
            });

            service.get("/js/*", (request, response) -> {
                staticHandler.consume(request.raw(), response.raw());
                return "OK";
            });
        }
    }
}
