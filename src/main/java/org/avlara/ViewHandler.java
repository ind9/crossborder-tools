package org.avlara;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Version;
import lombok.extern.slf4j.Slf4j;
import spark.ModelAndView;
import spark.Route;
import spark.template.freemarker.FreeMarkerEngine;

@Slf4j
public class ViewHandler
{

    public static final Route index = (request, response) -> {
        Map<String,Object> model = new HashMap<>();

        return render(model,"index.ftl");
    };

    private static FreeMarkerEngine renderEngine = null;
    private static Configuration fmConfig  = null;

    public static void init() throws Exception
    {
        String templateDirectory = System.getProperty("app.templates");

        log.info("Using : "+templateDirectory);

        File templateDir = new File(templateDirectory);

        fmConfig = new Configuration(new Version(2,3,23));
        fmConfig.setDirectoryForTemplateLoading(templateDir);
        renderEngine = new FreeMarkerEngine(fmConfig);
    }

    public static String render(Map<String,Object> model,String template)
    {
        return renderEngine.render(new ModelAndView(model,template));
    }
}