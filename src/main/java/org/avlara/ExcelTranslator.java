package org.avlara;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ExcelTranslator {

    private DataFormatter formatter = new DataFormatter(true);

    public void translate(InputStream is,SqlInserter inserter) {
        FormulaEvaluator formulaError = null;
        try {
            Workbook workbook = WorkbookFactory.create(is);
            formulaError = workbook.getCreationHelper()
                                   .createFormulaEvaluator();
            int maxSheets = workbook.getNumberOfSheets();
            for(int si = 0; si < maxSheets; si++) {
                Sheet sheet = workbook.getSheetAt(si);
                long st = System.currentTimeMillis();
                injestSheet(sheet,formulaError,inserter);
                long nd = System.currentTimeMillis();
                log.info("Sheet [{}] took {}sec",sheet.getSheetName(),((nd-st)/1000));
            }

        }catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    private Code cellToCode(int index, FormulaEvaluator formulaEval, Row row){
        // cells have 10 columns
        int maxCells = row.getLastCellNum();
        Code code = new Code();
        code.setRow(index);
        // 0 Descriptions
        // 1 Fk_CodeId
        // 2 Is_System_Defined
        // 3 Is_Taxable
        // 4 Zero_Padded_Count
        // 5 HasChildren
        // 6 IsDecision
        // 7 IsZeroPadded
        // 8 SequenceNumber
        // 9 ParsedCode
        if(maxCells >= 10)
        {

            try {
                code.setDescription(evalCell(formulaEval, row.getCell(0)));
                code.setFkCodeId(evalCell(formulaEval, row.getCell(1)));
                code.setSystemDefined(Boolean.parseBoolean(evalCell(formulaEval, row.getCell(2))));
                code.setTaxable(Boolean.parseBoolean(evalCell(formulaEval, row.getCell(3))));
                code.setZeroPaddedCount(Integer.parseInt(evalCell(formulaEval, row.getCell(4))));
                code.setHasChildren(Boolean.parseBoolean(evalCell(formulaEval, row.getCell(5))));
                code.setDecision(Boolean.parseBoolean(evalCell(formulaEval, row.getCell(6))));
                code.setZeroPadded(Boolean.parseBoolean(evalCell(formulaEval, row.getCell(7))));
                code.setSequenceNumber(evalCell(formulaEval, row.getCell(8)));
                code.setParsedCode(evalCell(formulaEval, row.getCell(9)));
            }catch(Exception e) {
                code.setHasError(true);
                code.getErrorMessages().add(e.getMessage());
            }
        }else {
            code.setHasError(true);
            code.getErrorMessages().add("Expected 10 columns got "+maxCells);
        }
        return code;
    }

    private String evalCell(FormulaEvaluator formulaEval, Cell cell) {
        if (cell != null) {
            try {
                return formatter.formatCellValue(cell);
                /*
                if (CellType.FORMULA.equals(cell.getCellType())) {
                    return formatter.formatCellValue(cell, formulaEval);
                } else {
                    return formatter.formatCellValue(cell);
                }*/
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
        return "";
    }

    private Rate cellToRate(int index, FormulaEvaluator formulaEval, Row row) {
        Rate rate = new Rate();
        rate.setRow(index);
        // 0 CitationTexts
        // 1 ManufactureSourceType
        // 2 ShippingDestinationType
        // 3 Fk_CodeId
        // 4 ShippingSourceType
        // 5 Formula
        // 6 Type
        // 7 TaxSection
        // 8 Fk_ShippingSource_GroupId
        // 9 Fk_ShippingSource_CountryId
        int maxCells = row.getLastCellNum();
        if(maxCells >= 8 )
        {
            rate.setCitationTexts(evalCell(formulaEval,row.getCell(0)));
            rate.setManufactureSourceType(evalCell(formulaEval, row.getCell(1)));
            rate.setShippingDestinationType(evalCell(formulaEval, row.getCell(2)));
            rate.setFKCodeId(evalCell(formulaEval, row.getCell(3)));
            rate.setShippingSourceType(evalCell(formulaEval, row.getCell(4)));
            rate.setFormula(evalCell(formulaEval, row.getCell(5)));
            rate.setFormulaType(evalCell(formulaEval, row.getCell(6)));
            rate.setTaxSection(evalCell(formulaEval, row.getCell(7)));

        }else {
            rate.setHasError(true);
            rate.getErrorMessages().add("Rate expected atleast 8 columns. got "+maxCells);
        }

        return rate;
    }

    private void injestSheet(Sheet sheet, FormulaEvaluator formulaEval, SqlInserter inserter) {
        int maxRows = sheet.getLastRowNum();
        String sheetName = sheet.getSheetName().toLowerCase().trim();

        for(int rowId = 1; rowId <= maxRows; rowId++) {
            Row row = sheet.getRow(rowId);
            if("codes".equalsIgnoreCase(sheetName))
            {
                Code code = cellToCode(rowId,formulaEval,row);
                //System.out.println(code.ts());
                if(code.isHasError()) {
                    // return code for reporting parsing errors
                    //System.out.println(code.ts());
                }else {
                    inserter.insert(code);
                }
            }else if("rates".equalsIgnoreCase(sheetName)) {
                Rate rate = cellToRate(rowId,formulaEval,row);
                if(rate.isHasError()) {
                    // return code for reporting parsing errors
                    //System.out.println(rate.ts());
                }else {
                    inserter.insert(rate);
                }
            }else {
                System.out.println("No match");
            }
        }
    }
}
