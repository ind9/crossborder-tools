package org.avlara;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void loadExcel() throws Exception
    {
        try(FileInputStream fis = new FileInputStream(new File("src/test/resources/CTIN.raw.xlsx")))
        {
            ExcelTranslator ext = new ExcelTranslator();
            ext.translate(fis,new DebugInserter());
        }finally {

        }


    }

}
