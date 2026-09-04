import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.util.StringList;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * CUSDTExcelUtils_mxJPO.java
 *
 * @author tanxiaolong
 * created on Aug 13, 2020
 * @desc
 */
public class JF_ExcelUtils_mxJPO {
    private static final Logger NIO_LOG = LoggerFactory.getLogger(JF_ExcelUtils_mxJPO.class);
    public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public static MapList getMapList(XSSFSheet sheet, int startRow, Boolean flag, Boolean prohibit) throws Exception {
        return getMapList(sheet, startRow, null, flag, prohibit);
    }

    public static MapList getMapList(XSSFSheet sheet, int startRow, Map loaction, Boolean flag, Boolean prohibit) throws Exception {
        MapList mList = new MapList();
        XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(sheet.getWorkbook());
        List<CellRangeAddress> arrayFormulas = getArrayFormulas(sheet);
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getPhysicalNumberOfCells() == 0) {
                continue;
            }

            //检查当前行是否为空行
            boolean notIsRowEmpty = false;
            Map<String, String> map = new HashMap<String, String>();
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                Cell cell2 = getFirstCellInArrayFormula(arrayFormulas, sheet, i, j);
                if (cell2 != null) {
                    cell = cell2;
                }
                String cellValue = getCellValue(cell, evaluator, flag, prohibit);
                if (UIUtil.isNotNullAndNotEmpty(cellValue)) {
                    notIsRowEmpty = true;
                }
                String key = String.valueOf(j);
                if (loaction != null) {
                    key = (String) loaction.get(key);
                }
                if (key != null) {
                    map.put(key, cellValue);
                }
            }
            if (notIsRowEmpty) {
                map.put("RowNum", String.valueOf(i+1));
                mList.add(map);
            }
        }
        return mList;
    }

    /**
     * @param sheet
     * @param startRow
     * @param startCol
     * @param loaction
     * @return
     * @throws Exception
     * @author tanxiaolong
     * @date Jun 29, 2021
     * @desc
     */
    public static MapList getMapList(XSSFSheet sheet, int startRow, int startCol, Map loaction) throws Exception {
        MapList mList = new MapList();
        try {
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                NIO_LOG.info("~~~ i:" + i);
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, String> map = new HashMap<String, String>();
                for (int j = startCol; j <= row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = "";
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        cellValue = cell.getStringCellValue();//getSingleCellValue(cell);
                    }
                    String key = String.valueOf(j);
                    if (loaction != null) {
                        key = (String) loaction.get(key);
                    }
                    if (key != null) {
                        cellValue = cellValue.trim();
                        map.put(key, cellValue);
                    }
                }
                map.put("RowNum", String.valueOf(i));
                mList.add(map);
            }
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
        }
        return mList;
    }

    public static MapList getMapList(HSSFSheet sheet, int startRow, int startCol, Map loaction) throws Exception {
        MapList mList = new MapList();
        try {
            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet.getWorkbook());
            List<CellRangeAddress> arrayFormulas = getArrayFormulas(sheet);
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                NIO_LOG.info("~~~ i:" + i);
                Row row = sheet.getRow(i);
                if (row == null || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }
                Map<String, String> map = new HashMap<String, String>();
                for (int j = startCol; j <= row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    Cell cell2 = getFirstCellInArrayFormula(arrayFormulas, sheet, i, j);
                    if (cell2 != null) {
                        cell = cell2;
                    }
                    String cellValue = getCellValue(cell, evaluator, true, false);
                    String key = String.valueOf(j);
                    if (loaction != null) {
                        key = (String) loaction.get(key);
                    }
                    if (key != null) {
                        map.put(key, cellValue);
                    }
                }
                map.put("RowNum", String.valueOf(i));
                mList.add(map);
            }
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
        }
        return mList;
    }

    public static List<CellRangeAddress> getArrayFormulas(XSSFSheet sheet) {
        List<CellRangeAddress> arrayFormulas = new ArrayList<CellRangeAddress>();
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            arrayFormulas.add(sheet.getMergedRegion(i));
        }
        return arrayFormulas;
    }

    public static List<CellRangeAddress> getArrayFormulas(HSSFSheet sheet) {
        List<CellRangeAddress> arrayFormulas = new ArrayList<CellRangeAddress>();
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            arrayFormulas.add(sheet.getMergedRegion(i));
        }
        return arrayFormulas;
    }

    public static boolean isCellInArrayFormulaContext(List<CellRangeAddress> arrayFormulas, int RowIndex, int CellIndex) {
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(RowIndex, CellIndex)) {
                return true;
            }
        }
        return false;
    }

    public static XSSFCell getFirstCellInArrayFormula(List<CellRangeAddress> arrayFormulas, XSSFSheet sheet, int RowIndex, int CellIndex) {
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(RowIndex, CellIndex)) {
                return sheet.getRow(range.getFirstRow()).getCell(range.getFirstColumn());
            }
        }
        return null;
    }

    public static HSSFCell getFirstCellInArrayFormula(List<CellRangeAddress> arrayFormulas, HSSFSheet sheet, int RowIndex, int CellIndex) {
        for (CellRangeAddress range : arrayFormulas) {
            if (range.isInRange(RowIndex, CellIndex)) {
                return sheet.getRow(range.getFirstRow()).getCell(range.getFirstColumn());
            }
        }
        return null;
    }

    public static String getFilePath(String fileName) throws Exception {
        String filePath = "";
        try {
            String resourcePath = JF_ExcelUtils_mxJPO.class.getResource("/").getFile().toString();
            String enoviaPath = resourcePath.substring(0, resourcePath.length() - 16) + "common\\templates\\";
            filePath = enoviaPath + fileName;
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
            throw e;
        }
        return filePath;
    }

    public static void SetExcelValue(MapList mList, Map<String, Integer> location, int iSheetAt, int startRow, String tempPath, String outPath) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(tempPath);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = wb.getSheetAt(iSheetAt);
            Cell c = getCell(getRow(sheet, startRow), 0);
            CellStyle cs = c.getCellStyle();
            Map m;
            Row r;
            Iterator<String> it;
            for (int i = 0; i < mList.size(); i++) {
                m = (Map) mList.get(i);
                r = getRow(sheet, startRow++);
                it = location.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    int cellIndex = location.get(key);
                    String value = "";
                    if ("RowIndex".equals(key)) {
                        value = String.valueOf(i + 1);
                    } else {
                        value = (String) m.get(key);
                    }
                    if (UIUtil.isNotNullAndNotEmpty(value)) {
                        c = getCell(r, cellIndex);
                        c.setCellValue(value);
                        c.setCellStyle(cs);
                    }
                }
            }
            SaveExcel(wb, outPath);
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static void SaveExcel(Workbook wb, String outPath) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outPath);
            wb.write(fos);
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                NIO_LOG.error(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static Row getRow(Sheet sheet, int index) throws Exception {
        Row r = sheet.getRow(index);
        if (r == null) {
            r = sheet.createRow(index);
        }
        return r;
    }

    public static void setCellValue(Sheet sheet, int rowIndex, int cellIndex, String value) throws Exception {
        setCellValue(getRow(sheet, rowIndex), cellIndex, value);
    }

    public static void setCellValue(Row r, int index, String value) throws Exception {
        Cell c = getCell(r, index);
        c.setCellValue(value);
    }

    public static Cell getCell(Row r, int index) throws Exception {
        Cell c = r.getCell(index);
        if (c == null) {
            c = r.createCell(index);
        }
        return c;
    }

    public static String getCellValue(Row r, int index) throws Exception {
        Cell c = r.getCell(index);
        if (c == null) {
            return "";
        }
        return getCellValue(c);
    }

    public static String getCellValue(Cell cell) throws Exception {
        return getCellValue(cell, null, true, false);
    }

    public static String getCellValue(Sheet sheet, int rowIndex, int cellIndex, Boolean flag) throws Exception {
        Row r = sheet.getRow(rowIndex);
        if (r == null) {
            return "";
        }
        return getCellValue(r, cellIndex);
    }

    public static String getCellValue(Cell cell, FormulaEvaluator evaluator, Boolean flag, Boolean prohibit) throws Exception {
        JF_ExcelUtils_mxJPO nioss = new JF_ExcelUtils_mxJPO();
        String value = "";
        try {
            if (cell == null) {
                return value;
            }
            switch (cell.getCellType()) {
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case FORMULA: {
                    if (evaluator == null) {
                        value = "###FORMULA####";
                    }
                    CellValue cv = evaluator.evaluate(cell);
                    switch (cv.getCellType()) {
                        case BOOLEAN:
                            value = String.valueOf(cv.getBooleanValue());
                            break;
                        case NUMERIC: {
                            double d = cell.getNumericCellValue();
                            BigDecimal bd =  BigDecimal.valueOf(d);
                            if (DateUtil.isCellDateFormatted(cell)) {
                                value = nioss.simpleDateFormat.format(cell.getDateCellValue());
                            } else {
                                value = bd.toString();
                                if (value.indexOf(".") != -1) {
                                    DecimalFormat df = new DecimalFormat("0.##");
                                    value = df.format(bd);
                                }

                            }

                        }
                        break;
                        case STRING:
                            value = cv.getStringValue();
                            break;
                        default:
                            value = "";
                            break;
                    }
                    break;
                }
                case NUMERIC: {
                    double d = cell.getNumericCellValue();
                    BigDecimal bd = BigDecimal.valueOf(d);
                    if (DateUtil.isCellDateFormatted(cell)) {
                        value = nioss.simpleDateFormat.format(cell.getDateCellValue());
                    } else {
                        value = bd.toString();
                        if (flag) {
                            if (value.indexOf(".") != -1) {
                                DecimalFormat df = new DecimalFormat("0.##");
                                value = df.format(bd);
                            }
                        } else {
                            if (prohibit) {
                                //是否禁用科学计数法
                                if (value.indexOf(".") != -1) {
//                                    double d1 = cell.getNumericCellValue();
//                                    BigDecimal bd1 = BigDecimal.valueOf(d);
                                    BigDecimal decimal = BigDecimal.valueOf(d);
                                    value = decimal.toPlainString();
                                }
                            } else {
                                if (value.indexOf(".") != -1) {
                                    double f1 = bd.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    value = String.valueOf(f1);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
            throw e;
        }
        if (value != null) {
            value = value.trim();
        } else {
            value = "";
        }
        return value;
    }

    public static Map<String, PictureData> getSheetPictrues03(int sheetNum,
                                                              HSSFSheet sheet, HSSFWorkbook workbook) {

        Map<String, PictureData> sheetIndexPicMap = new HashMap<String, PictureData>();
        List<HSSFPictureData> pictures = workbook.getAllPictures();
        if (pictures.size() != 0) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    int pictureIndex = pic.getPictureIndex() - 1;
                    HSSFPictureData picData = pictures.get(pictureIndex);
                    String picIndex = String.valueOf(sheetNum) + "_"
                            + String.valueOf(anchor.getRow1()) + "_"
                            + String.valueOf(anchor.getCol1());
                    sheetIndexPicMap.put(picIndex, picData);
                }
            }
            return sheetIndexPicMap;
        } else {
            return null;
        }
    }

    public static Map<String, PictureData> getSheetPictrues07(int sheetNum,
                                                              XSSFSheet sheet, XSSFWorkbook workbook) throws Exception {
        Map<String, PictureData> sheetIndexPicMap = new HashMap<String, PictureData>();

        for (POIXMLDocumentPart dr : sheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;
                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;

                    XSSFClientAnchor anchor = pic.getPreferredSize();
                    CTMarker ctMarker = anchor.getFrom();
                    String picIndex = String.valueOf(sheetNum) + "_"
                            + ctMarker.getRow() + "_" + ctMarker.getCol();
                    sheetIndexPicMap.put(picIndex, pic.getPictureData());
                }
            }
        }
        return sheetIndexPicMap;
    }

    public static Map<String, String> getSheetPictrues07(int sheetNum, XSSFSheet sheet, XSSFWorkbook workbook, String outputPath) throws Exception {
        return generateImg(getSheetPictrues07(sheetNum, sheet, workbook), outputPath);
    }

    public SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getFileName() {
        JF_ExcelUtils_mxJPO nioss = new JF_ExcelUtils_mxJPO();
        String newFileName = nioss.df.format(new Date()) + "_" + new Random().nextInt(1000);
        return newFileName;
    }

    public static Map<String, String> generateImg(Map<String, PictureData> map, String outputPath) throws IOException {
        Map<String, String> retMap = new HashMap<String, String>();
        Object key[] = map.keySet().toArray();
        FileOutputStream out = null;
        try {
            for (int i = 0; i < map.size(); i++) {
                PictureData pic = map.get(key[i]);

                String picLocation = key[i].toString();
                String timeStamp = getFileName() + i;
                String ext = pic.suggestFileExtension();
                String imgFileName = "";
                if (outputPath.endsWith("\\") || outputPath.endsWith("/")) {
                    imgFileName = outputPath + timeStamp + "." + ext;
                } else {
                    imgFileName = outputPath + File.separator + timeStamp + "." + ext;
                }
                byte[] data = pic.getData();
                out = new FileOutputStream(imgFileName);
                out.write(data);
                out.close();
                retMap.put(picLocation, imgFileName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null != out) {
                out.close();
            }
        }
        return retMap;
    }

    public static Map<String, String> generateImg(List<Map<String, PictureData>> sheetList, String outputPath) throws IOException {
        Map<String, String> retMap = new HashMap<String, String>();
        FileOutputStream out = null;
        try {
            for (Map<String, PictureData> map : sheetList) {
                Object key[] = map.keySet().toArray();
                for (int i = 0; i < map.size(); i++) {
                    PictureData pic = map.get(key[i]);
                    String picLocation = key[i].toString();
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    String ext = pic.suggestFileExtension();
                    String imgFileName = "";
                    if (outputPath.endsWith("\\") || outputPath.endsWith("/")) {
                        imgFileName = outputPath + timeStamp + "." + ext;
                    } else {
                        imgFileName = outputPath + File.separator + timeStamp + "." + ext;
                    }
                    byte[] data = pic.getData();
                    out = new FileOutputStream(imgFileName);
                    out.write(data);
                    out.close();
                    retMap.put(picLocation, imgFileName);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null != out) {
                out.close();
            }
        }
        return retMap;
    }

    public static HashMap<String, HashMap<String, String>> attrValueMap = new HashMap<String, HashMap<String, String>>();

    public static String getChoiceValue(Context ctx, String attr, String oldValue) throws Exception {
        if (UIUtil.isNullOrEmpty(attr) || UIUtil.isNullOrEmpty(oldValue)) {
            return oldValue;
        }
        String newValue = oldValue;
        if (!attrValueMap.containsKey(attr)) {
            StringList rangeList = FrameworkUtil.getRanges(ctx, attr);
            HashMap<String, String> map = new HashMap<String, String>();
            if (rangeList != null && rangeList.size() > 0) {
                for (int i = 0; i < rangeList.size(); i++) {
                    String range = (String) rangeList.get(i);
                    String cn = EnoviaResourceBundle.getRangeI18NString(ctx, attr, range, "zh_cn");
                    map.put(range, range);
                    map.put(cn, range);
                }
            }
            attrValueMap.put(attr, map);
        }
        HashMap<String, String> ChoiceMap = attrValueMap.get(attr);
        if (!ChoiceMap.isEmpty() && ChoiceMap.containsKey(oldValue)) {
            newValue = ChoiceMap.get(oldValue);
        }
        return newValue;
    }

    public static String getCellValue(Context ctx, String attr, Sheet sheet, int rowIndex, int cellIndex) throws Exception {
        String value = getCellValue(sheet, rowIndex, cellIndex, true);
        return getChoiceValue(ctx, attr, value);
    }

    /**
     * @param cell
     * @return
     * @throws Exception
     * @author tanxiaolong
     * @date Aug 12, 2021
     * @desc
     */
    private static String getSingleCellValue(Cell cell) throws Exception {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case BLANK:
                return "";

            case BOOLEAN:
            case FORMULA:
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                double d = cell.getNumericCellValue();
                int n = (int) d;
                switch (cell.getCellStyle().getDataFormat()) {
                    case 0:
                        double d2 = (double) n;
                        if (d2 == d)
                            return "" + n;
                        else
                            return "" + d;
                    case 1:
                        return "" + n;
                    default:
                        return "" + d;
                }

            case ERROR:
                throw new Exception("Error cell: " + cell.getErrorCellValue());
        }
        return "";
    }

    public static void calcAndSetRowHeigt(Sheet sheet) {
        try {
            int iLastRowNum = sheet.getLastRowNum();
            for (int i = 0; i < iLastRowNum; i++) {
                calcAndSetRowHeigt(sheet.getRow(i));
            }
        } catch (Exception e) {
            NIO_LOG.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void calcAndSetRowHeigt(Row sourceRow) throws Exception {
        for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex <= sourceRow.getPhysicalNumberOfCells(); cellIndex++) {
            double maxHeight = sourceRow.getHeight();
            Cell sourceCell = sourceRow.getCell(cellIndex);
            String cellContent = getCellValue(sourceCell);
            if (null == cellContent || "".equals(cellContent)) {
                continue;
            }
            Map cellInfoMap = getCellInfo(sourceCell);
            Integer cellWidth = (Integer) cellInfoMap.get("width");
            Integer cellHeight = (Integer) cellInfoMap.get("height");
            if (cellHeight > maxHeight) {
                maxHeight = cellHeight;
            }
            CellStyle cellStyle = sourceCell.getCellStyle();
            Font font = sourceRow.getSheet().getWorkbook().getFontAt(cellStyle.getFontIndex());

            short fontHeight = font.getFontHeight();

            double cellContentWidth = (double) cellContent.getBytes().length * 2 * 256;

            double stringNeedsRows = (double) cellContentWidth / cellWidth;

            if (stringNeedsRows < 1.0) {
                stringNeedsRows = 1.0;
            }

            double stringNeedsHeight = (double) fontHeight * stringNeedsRows;

            if (stringNeedsHeight > maxHeight) {
                maxHeight = stringNeedsHeight;

                if (maxHeight / cellHeight > 5) {
                    maxHeight = 5 * (double) cellHeight;
                }

                maxHeight = Math.ceil(maxHeight);

                Boolean isPartOfRowsRegion = (Boolean) cellInfoMap.get("isPartOfRowsRegion");
                if (isPartOfRowsRegion) {
                    Integer firstRow = (Integer) cellInfoMap.get("firstRow");
                    Integer lastRow = (Integer) cellInfoMap.get("lastRow");

                    double addHeight = (maxHeight - cellHeight) / (lastRow - firstRow + 1);
                    for (int i = firstRow; i <= lastRow; i++) {
                        double rowsRegionHeight = sourceRow.getSheet().getRow(i).getHeight() + addHeight;
                        sourceRow.getSheet().getRow(i).setHeight((short) rowsRegionHeight);
                    }
                } else {
                    sourceRow.setHeight((short) maxHeight);
                }
            }

        }
    }

    /**
     * 获取单元格及合并单元格的宽度
     *
     * @param cell
     * @return
     */
    private static Map getCellInfo(Cell cell) {
        Sheet sheet = cell.getSheet();
        int rowIndex = cell.getRowIndex();
        int columnIndex = cell.getColumnIndex();
        boolean isPartOfRegion = false;
        int firstColumn = 0;
        int lastColumn = 0;
        int firstRow = 0;
        int lastRow = 0;
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            firstColumn = ca.getFirstColumn();
            lastColumn = ca.getLastColumn();
            firstRow = ca.getFirstRow();
            lastRow = ca.getLastRow();
            if (rowIndex >= firstRow && rowIndex <= lastRow) {
                if (columnIndex >= firstColumn && columnIndex <= lastColumn) {
                    isPartOfRegion = true;
                    break;
                }
            }
        }
        Map map = new HashMap();
        Integer width = 0;
        Integer height = 0;
        boolean isPartOfRowsRegion = false;
        if (isPartOfRegion) {
            for (int i = firstColumn; i <= lastColumn; i++) {
                width += sheet.getColumnWidth(i);
            }
            for (int i = firstRow; i <= lastRow; i++) {
                height += sheet.getRow(i).getHeight();
            }
            if (lastRow > firstRow) {
                isPartOfRowsRegion = true;
            }
        } else {
            width = sheet.getColumnWidth(columnIndex);
            height += cell.getRow().getHeight();
        }
        map.put("isPartOfRowsRegion", isPartOfRowsRegion);
        map.put("firstRow", firstRow);
        map.put("lastRow", lastRow);
        map.put("width", width);
        map.put("height", height);
        return map;
    }
}

