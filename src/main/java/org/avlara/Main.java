package org.avlara;

import lombok.extern.slf4j.Slf4j;
import spark.Service;
import spark.staticfiles.StaticFilesConfiguration;

@Slf4j
public class Main {

    private static final JsonTransformer jsonTransformer = new JsonTransformer();
    public static void main(String[] args) throws Exception{

        ViewHandler.init();
        Service service = Service.ignite();

        service.ipAddress("0.0.0.0");
        service.port(8080);
        configure(service);

        Thread memoryMonitor = new Thread(new Runnable() {
            @Override
            public void run() {
                long mb = 1024*1024;
                while(true) {
                    long total = Runtime.getRuntime().totalMemory();
                    long free  = Runtime.getRuntime().freeMemory();
                    float pre = (((float)free)/total)*100;

                    log.info("Total [{}]mb Free [{}]mb : [{}]%",total/mb,free/mb,pre);
                    try {
                        Thread.sleep(10000);
                    }catch (Exception e ){}
                }
            }
        });
        memoryMonitor.setDaemon(true);
        memoryMonitor.run();

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
