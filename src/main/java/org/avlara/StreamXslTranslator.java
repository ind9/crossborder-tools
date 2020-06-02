
package org.avlara;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@Slf4j
public class StreamXslTranslator {

    public StreamXslTranslator() {
    }

    public boolean process(InputStream rawXlsSource, SqlInserter persistance) throws IOException {
        boolean processed = false;

        try {
            try {
                OPCPackage opcPackage = OPCPackage.open(rawXlsSource);

                try {
                    XSSFReader xssfReader = new XSSFReader(opcPackage);
                    SharedStringsTable sst = xssfReader.getSharedStringsTable();
                    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                    SAXParser parser = saxParserFactory.newSAXParser();
                    StreamXslTranslator.SheetHandler handler = new StreamXslTranslator.SheetHandler(sst, new StreamXslTranslator.SqlLiteSink(persistance));
                    SheetIterator ss = (SheetIterator)xssfReader.getSheetsData();

                    while(true) {
                        if (!ss.hasNext()) {
                            processed = true;
                            break;
                        }

                        InputStream is = ss.next();

                        try {
                            long st = System.currentTimeMillis();
                            String sheetName = ss.getSheetName().trim().toLowerCase();
                            InputSource xis = new InputSource(is);
                            handler.setCurrentSheetName(sheetName);
                            parser.parse(xis, handler);
                            long nd = System.currentTimeMillis();
                            log.info("Processed sheet {} in {}ms", sheetName, nd - st);
                        } catch (Throwable var30) {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (Throwable var29) {
                                    var30.addSuppressed(var29);
                                }
                            }

                            throw var30;
                        }

                        if (is != null) {
                            is.close();
                        }
                    }
                } catch (Throwable var31) {
                    if (opcPackage != null) {
                        try {
                            opcPackage.close();
                        } catch (Throwable var28) {
                            var31.addSuppressed(var28);
                        }
                    }

                    throw var31;
                }

                if (opcPackage != null) {
                    opcPackage.close();
                }
            } catch (InvalidFormatException var32) {
                log.error("Invalid format", var32);
            } catch (SAXException var33) {
                log.error("Invalid format", var33);
            } catch (ParserConfigurationException var34) {
                log.error("Invalid format", var34);
            } catch (OpenXML4JException var35) {
                log.error("Invalid format", var35);
            }

            return processed;
        } finally {
            ;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Test");
        OPCPackage opcPackage = OPCPackage.open(new File("/home/awilliams/CTMX.raw.xlsx"));

        try {
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            SharedStringsTable sst = xssfReader.getSharedStringsTable();
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser parser = saxParserFactory.newSAXParser();
            StreamXslTranslator.SheetHandler handler = new StreamXslTranslator.SheetHandler(sst, new StreamXslTranslator.Sink() {
                public void flush(String sheetName, int rowId, List<String> row) {
                }
            });
            SheetIterator ss = (SheetIterator)xssfReader.getSheetsData();

            while(ss.hasNext()) {
                InputStream is = ss.next();

                try {
                    String sheetName = ss.getSheetName().trim().toLowerCase();
                    InputSource xis = new InputSource(is);
                    handler.setCurrentSheetName(sheetName);
                    parser.parse(xis, handler);
                } catch (Throwable var13) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Throwable var12) {
                            var13.addSuppressed(var12);
                        }
                    }

                    throw var13;
                }

                if (is != null) {
                    is.close();
                }
            }
        } catch (Throwable var14) {
            if (opcPackage != null) {
                try {
                    opcPackage.close();
                } catch (Throwable var11) {
                    var14.addSuppressed(var11);
                }
            }

            throw var14;
        }

        if (opcPackage != null) {
            opcPackage.close();
        }

    }

    private class SqlLiteSink implements StreamXslTranslator.Sink {
        private SqlInserter persistence;

        public SqlLiteSink(SqlInserter ps) {
            this.persistence = ps;
        }

        private boolean tryParseBool(String v) {
            if (v.matches("[0-9]+")) {
                return Integer.parseInt(v) > 0;
            } else {
                return Boolean.parseBoolean(v);
            }
        }

        private Code rowToCode(int index, List<String> row) {
            Code code = new Code();
            code.setRow(index);
            int actual = row.size();
            if (actual == 8) {
                StreamXslTranslator.log.info("Code is missing sequence number and parsed code. setting blank defaults");
                row.add("");
                row.add("");
            }

            if (row.size() >= 10) {
                try {
                    code.setDescription((String)row.get(0));
                    code.setFkCodeId((String)row.get(1));
                    code.setSystemDefined(this.tryParseBool((String)row.get(2)));
                    code.setTaxable(this.tryParseBool((String)row.get(3)));
                    code.setZeroPaddedCount(Integer.parseInt((String)row.get(4)));
                    code.setHasChildren(this.tryParseBool((String)row.get(5)));
                    code.setDecision(this.tryParseBool((String)row.get(6)));
                    code.setZeroPadded(this.tryParseBool((String)row.get(7)));
                    code.setSequenceNumber((String)row.get(8));
                    code.setParsedCode((String)row.get(9));
                } catch (Exception var6) {
                    code.setHasError(true);
                    code.getErrorMessages().add(var6.getMessage());
                }
            } else {
                code.setHasError(true);
                String data = (String)row.stream().collect(Collectors.joining(","));
                code.getErrorMessages().add("Expected 10 columns got [" + data + "]");
            }

            return code;
        }

        private Rate rowToRate(int index, List<String> row) {
            Rate rate = new Rate();
            rate.setRow(index);
            int maxCells = row.size();
            if (maxCells >= 8) {
                rate.setCitationTexts((String)row.get(0));
                rate.setManufactureSourceType((String)row.get(1));
                rate.setShippingDestinationType((String)row.get(2));
                rate.setFKCodeId((String)row.get(3));
                rate.setShippingSourceType((String)row.get(4));
                rate.setFormula((String)row.get(5));
                rate.setFormulaType((String)row.get(6));
                rate.setTaxSection((String)row.get(7));
            } else {
                rate.setHasError(true);
                String data = (String)row.stream().collect(Collectors.joining(","));
                rate.getErrorMessages().add("Rate expected atleast 8 columns. got [" + data + "]");
            }

            return rate;
        }

        public void flush(String sheetName, int index, List<String> row) {
            if (index == 0) {
                StreamXslTranslator.log.info("Skipping header for {}", sheetName);
            } else {
                byte var5 = -1;
                switch(sheetName.hashCode()) {
                    case 94834726:
                        if (sheetName.equals("codes")) {
                            var5 = 1;
                        }
                        break;
                    case 108285843:
                        if (sheetName.equals("rates")) {
                            var5 = 0;
                        }
                }

                switch(var5) {
                    case 0:
                        this.persistence.insert(this.rowToRate(index, row));
                        break;
                    case 1:
                        this.persistence.insert(this.rowToCode(index, row));
                        break;
                    default:
                        StreamXslTranslator.log.debug("Unmatched Sheetname {}", sheetName);
                }

            }
        }
    }

    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private StringBuilder lastContents;
        private boolean nextIsString;
        private List<String> data;
        private StreamXslTranslator.Sink sink;
        private String currentSheetName;
        private int rowId;
        private String lastColRef;

        private SheetHandler(SharedStringsTable ss, StreamXslTranslator.Sink si) {
            this.lastContents = new StringBuilder();
            this.data = new ArrayList();
            this.currentSheetName = "";
            this.rowId = 0;
            this.lastColRef = "A";
            this.sst = ss;
            this.sink = si;
        }

        public void setCurrentSheetName(String sheetName) {
            if (!this.currentSheetName.equals(sheetName)) {
                this.rowId = 0;
                this.lastColRef = "A";
            }

            this.currentSheetName = sheetName;
        }

        private String getExcelCellRef(String fromColRef) {
            if (fromColRef == null) {
                return null;
            } else {
                int i;
                for(i = 0; i < fromColRef.length() && !Character.isDigit(fromColRef.charAt(i)); ++i) {
                }

                return i == 0 ? fromColRef : fromColRef.substring(0, i);
            }
        }

        private int getDistance(String fromColRefString, String toColRefString) {
            String fromColRef = this.getExcelCellRef(fromColRefString);
            String toColRef = this.getExcelCellRef(toColRefString);
            int distance = 0;
            if (fromColRef != null && fromColRef.compareTo(toColRef) <= 0) {
                if (fromColRef != null && toColRef != null) {
                    while(fromColRef.length() < toColRef.length() || fromColRef.compareTo(toColRef) < 0) {
                        ++distance;
                        fromColRef = this.increment(fromColRef);
                    }
                }

                return distance;
            } else {
                return this.getDistance("A", toColRefString) + 1;
            }
        }

        public String increment(String s) {
            int length = s.length();
            char c = s.charAt(length - 1);
            if (c == 'Z') {
                return length > 1 ? this.increment(s.substring(0, length - 1)) + 'A' : "AA";
            } else {
                StringBuilder var10000 = (new StringBuilder()).append(s.substring(0, length - 1));
                ++c;
                return var10000.append(c).toString();
            }
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            byte var6 = -1;
            switch(qName.hashCode()) {
                case 99:
                    if (qName.equals("c")) {
                        var6 = 1;
                    }
                    break;
                case 113114:
                    if (qName.equals("row")) {
                        var6 = 0;
                    }
            }

            switch(var6) {
                case 0:
                    this.data.clear();
                    break;
                case 1:
                    String cellType = attributes.getValue("t");
                    this.nextIsString = cellType != null && "s".equals(cellType);
                    String currentColRef = attributes.getValue("r");

                    int distance;
                    for(distance = this.getDistance(this.lastColRef, currentColRef); distance > 1; --distance) {
                        this.data.add("");
                    }

                    if (distance > 1) {
                        StreamXslTranslator.log.info("Last{} current {} distance {}", new Object[]{this.lastColRef, currentColRef, distance});
                    }

                    this.lastColRef = currentColRef;
            }

            this.lastContents.setLength(0);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.lastContents.append(ch, start, length);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (this.nextIsString) {
                int idx = Integer.parseInt(this.lastContents.toString());
                this.lastContents.setLength(0);
                this.lastContents.append(this.sst.getItemAt(idx).toString());
                this.nextIsString = false;
            }

            byte var5 = -1;
            switch(qName.hashCode()) {
                case 118:
                    if (qName.equals("v")) {
                        var5 = 1;
                    }
                    break;
                case 113114:
                    if (qName.equals("row")) {
                        var5 = 0;
                    }
            }

            switch(var5) {
                case 0:
                    if (!this.data.isEmpty()) {
                        this.sink.flush(this.currentSheetName, this.rowId, this.data);
                    }

                    this.data.clear();
                    ++this.rowId;
                    break;
                case 1:
                    this.data.add(this.lastContents.toString());
                    this.lastContents.setLength(0);
            }

        }
    }

    private interface Sink {
        void flush(String var1, int var2, List<String> var3);
    }
}
