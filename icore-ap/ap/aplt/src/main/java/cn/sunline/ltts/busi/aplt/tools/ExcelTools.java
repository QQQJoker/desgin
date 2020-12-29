package cn.sunline.ltts.busi.aplt.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.SlError;
import cn.sunline.ltts.busi.sys.errors.UsError;



public class ExcelTools {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ExcelTools.class);

	/**
	 * 设置某些列的值只能输入预制的数据，显示下拉框
	 * @param sheet 待设置的sheet
	 * @param textList  下拉框显示的内容
	 * @param firstRow  开始行 
	 * @param endRow    结束行
	 * @param firstCol  开始列
	 * @param endCol    结束列
	 * @return   设置好的sheet
	 */
   public  static HSSFSheet   setHSSFValidation(HSSFSheet  sheet,String[] textList,int firstRow,int  endRow,int firstCol,int endCol){
	   //加载下拉列表内容
	  DVConstraint contraint  =  DVConstraint.createExplicitListConstraint(textList); 
	  //设置数据有效性加载在哪个单元格上,参数依次为：起始行，终止行，起始列，终止列
	  CellRangeAddressList  regions  =  new CellRangeAddressList(firstRow,endRow,firstCol,endCol);
	  //创建数据有效性对象
	  HSSFDataValidation    data_validation_list  =  new HSSFDataValidation(regions,contraint);
	  sheet.addValidationData(data_validation_list);
	  
	   return sheet;
	   
   }
   
   /**
    * 设置单元格上提示
    * @param sheet 待设置的sheet
    * @param promptTitle  提示的标题
    * @param promptContent  提示的内容
    * @param firstRow   开始 行
    * @param endRow     结束行
    * @param firstCol   开始列
    * @param endCol     结束列
    * @return
    */
   public  static  HSSFSheet   setHSSFPrompt(HSSFSheet sheet,String  promptTitle,String promptContent,int firstRow,int endRow,int firstCol,int endCol){
	   //构造constraint对象
	   DVConstraint contraint  =  DVConstraint.createCustomFormulaConstraint("BB1");
	   //设置数据有效性加载在哪个单元格上,参数依次为：起始行，终止行，起始列，终止列
	   CellRangeAddressList  regions  =  new CellRangeAddressList(firstRow,endRow,firstCol,endCol);
	   HSSFDataValidation    data_validation_view  =  new HSSFDataValidation(regions,contraint);
	   data_validation_view.createPromptBox(promptTitle,promptContent);
	   sheet.addValidationData(data_validation_view);
	   return sheet;
   }
   /**
    * 根据List集合中的数据以横表形式生成工作簿对象，
    * @param fileName
    * @param sheetSize
    * @param sheetName
    * @param columns
    * @param cells
    * @return  返回工作簿对象
    */
   public  static   Workbook  write2Excel(String fileName,String filePath,long sheetSize,String sheetName,List<String> columns,List<List<ExcelEntity>> cells){
	   long curr_time = System.currentTimeMillis();
	   logger.debug("当前毫秒数："+curr_time);
	 //申明excel文件对象
	   Workbook  workBook =null;
	   if(fileName.endsWith("xlsx")){
		   workBook =  new XSSFWorkbook();
	   }else if(fileName.endsWith("xls")){
		   workBook =  new HSSFWorkbook();
		  
	   }else{
		   //抛出异常：不是.xlsx或.xls后缀的文件
		   logger.error("不是.xlsx或.xls后缀的文件，请核对后再试!");
		  throw  SlError.SlComm.E0001("不是Excel文件格式，请核对后再试!");
		  //throw  ApError1.Aplt.E0002(fileName);
		  
	   }
       //对大数据量的处理,解决excel文件内存溢出问题
	  // workBook = new SXSSFWorkbook(100);
	   
	   //TODO  表格总数据行数大于65535时如何处理？报异常还是新建另一个工作表
	   //office excel 2003及以下版本最大行数65536行，最大列数256列，office excel 2007及以上版本最大行数为1048576行，最大列数为16384列
	   try{
		   if(CommUtil.isNull(sheetSize)){
			   logger.error("工作表大小为空，未指定大小，请指定大小");
			   throw  SlError.SlComm.E0001("工作表大小为空，未指定大小，请指定大小");
		   }
		   if(sheetSize<1){
			   logger.error("指定大小不合法，请指定一个不小于1的整数");
			   throw  SlError.SlComm.E0001("指定大小不合法，请指定一个不小于1的整数");
		   }
		   if(sheetSize > 65536){
			   sheetSize  = 65536;
		   }
	   }catch(NumberFormatException e){
		   logger.error("sheetSize参数不是数字，请核对后再试!");
		   throw  SlError.SlComm.E0001("sheetSize参数不是数字，请核对后再试!");

	   }
	    //取出一共多少个sheet(如20/8,应该建立3个工作表)
	   
	   double  sheetNum = cells.size()% sheetSize==0?cells.size()/sheetSize:(cells.size()/sheetSize+1);
	   //通过for循环创建取出的多个sheet工作表并设置工作表的名称以及数据内容
	   for(int  index =  0;index <=  sheetNum;index++){
		   //构造工作表名称为sheetName的工作表对象
		   //TODO  多个工作表格命名规则
		   Sheet sheet  =  workBook.createSheet(sheetName+"_"+System.currentTimeMillis());
		   //设置表格默认宽度为21个字节
		   //sheet.setDefaultColumnWidth((short)21);
		   //TODO 生成一个样式,设置样式
		   //设置表格标题行
		  int rowIndex = 0;
		  writeHeader(columns,sheet,workBook,rowIndex);
		  //表格数据内容处理
		  if(cells!=null){
			  for (List<ExcelEntity> rows:cells) {
				  rowIndex++;
				  Row  rowColumn =  sheet.createRow(rowIndex);
				  int   cellIndex = 0;
				  for(ExcelEntity  m:rows){
					  //构造单元格
					  Cell cell = rowColumn.createCell(cellIndex);
					  //数据类型处理  ->数据类型  1-String 2-date,3-number,4-boolean,5-formula
					  if(CommUtil.isNull(m.getDataType())){
						  logger.error("数据类型不能为空");
						 // throw SlError.SlComm.E0001("数据类型不能为空");
					  }
					  switch(m.getDataType()){
					  case 1:
						  cell.setCellValue(m.getData());
						  break;
					  case 2:
						  cell.setCellValue(SlTools.SqlStringToDate(m.getData()));
						  break;
					  case 3:
						  cell.setCellValue(Double.parseDouble(m.getData()));
						  break;
					  default:
						  logger.error("数据类型设置异常");
						  //throw SlError.SlComm.E0001("数据类型设置异常");
					  }
					  cellIndex++;
				  }
			  }

		  }
		  
		  //写入硬盘
		  FileOutPut2Excel(fileName,filePath,workBook);
		  /*计算耗时*/
		  logger.debug("耗时："+(System.currentTimeMillis() - curr_time) / 1000);
	   }
	  
	   return  workBook;
	   
	   
   }
   
   
   /**
    * 将Excel中的A,B,C,D,E...命名的列映射成0,1,2,3,4...
    * @param col
    * @return
    */
   public  static  int  getExcelCol(String col){
	   col = col.toUpperCase();
	   int count = -1;
	   char[] ch =col.toCharArray();
	   for (int i = 0;i<ch.length;i++ ) {
		   count +=  (ch[i]-64)*Math.pow(26,ch.length-1-i);
	}
	   return  count;
   }
   
   /**
    * 生成excel文件存入服务器
    * @param fileName 文件名（含后缀）
    * @param filePath 文件目录，文件目录+"\\"+文件名=绝对路径
    * @param workBook
    */
   public static void FileOutPut2Excel(String fileName,String filePath,Workbook workBook){
	      //申明文件输出流对象
	      FileOutputStream  fileOut = null;
	      //申明缓冲
	      BufferedOutputStream bos = null;
		  File file =  new File(filePath+"\\"+fileName);
		  if(!file.exists()){
			  file.mkdirs();
		  }
		  try {
			fileOut = new FileOutputStream(file);
		    bos = new BufferedOutputStream(fileOut);
		    //刷新此输出流并强制写出所有缓冲的输出字节
		    bos.flush();
			workBook.write(bos);
		} catch (FileNotFoundException e) {
			logger.error("文件未找到异常");
			//TODO  抛出文件未找到异常
			
			
		}catch (IOException e) {
			logger.error("文件IO异常");
			//TODO  文件IO异常
			
		}finally{
			try {
				//关闭流顺序由外向内关闭
				if(CommUtil.isNotNull(fileOut)){
					fileOut.close();
				}
				if(CommUtil.isNotNull(bos)){
					bos.close();
				}
			} catch (IOException e) {
				logger.error("文件关闭异常");
				//TODO  文件关闭异常

			}
			
		}
		  logger.debug("文件生成中....");
   }
   /**
    * 创建指定工作表文件头
    * @param header 文件头内容
    * @param sheet   工作表名（唯一）
    * @param wb   工作簿名
    */
   public  static void  writeHeader(List<String> header,Sheet sheet,Workbook  wb,int index){
	   Row row = sheet.createRow(index);
	   Cell cell;
	   int n=0;
	   for(String  head :header){
		   sheet.setColumnWidth(n, 4000);
		   cell =row.createCell(n++);
		   //cell.setCellStyle(cellStyle);
		   cell.setCellValue(header.get(n));
	   }
	   
   }
   /**
    * 创建指定工作表文件体
    * @param exportDatas
    * @param sheet
    * @param wb
    */
   public  static  void  writeBody(List<Object> exportDatas,Sheet sheet,Workbook wb){
	   
   }
   
   /**
	 * 读取Excel文件
	 * @param headRow 列表名和头行数，即跳过读取的行数
	 * @param cells   读取的数据
	 */
	public static void readExcel(String filePath,String fileName,int headRow,List<List<ExcelEntity>> cells) {
		InputStream inp = null;
		Workbook wb = null;
		try {
			inp = new FileInputStream(filePath + fileName);
			
			wb = WorkbookFactory.create(inp);
		    Sheet sheet = wb.getSheetAt(0);
		    int rowIndex = 0;
		    for (Row row : sheet) {
		    	rowIndex++;
		    	if(rowIndex <= headRow)
		    		continue;
		    	List<ExcelEntity> datas = new LinkedList<>();
		        for (Cell cell : row) {
		        	ExcelEntity m = new ExcelEntity();
		        	short dataType = 1;
		        	//logger.debug("类型======================="+cell.getCellType());
		            switch (cell.getCellType()) {
		                case Cell.CELL_TYPE_STRING:
		                	dataType = 1;
		                    m.setData(cell.getRichStringCellValue().getString());
		                    break;
		                case Cell.CELL_TYPE_NUMERIC:
		                	logger.debug("是否日期=============="+DateUtil.isCellDateFormatted(cell));
		                	//TODO 日期如何判断是java.sql.date类型和java.sql.timestamp类型?YYMMDD  HHMMSS
		                    if (DateUtil.isCellDateFormatted(cell)) {
		                    	dataType= 2;
		                    	//TODO 设置date类型
		                    	//m.setData(DateTools.Date2String(cell.getDateCellValue(), DateTools.YYYYMMDD));
		                    	//m.setData(SlTools.SqlDateToString(cell.getDateCellValue());
		                    } else {
		                    	//将数字类型先转成字符串类型，避免大数字变成科学计数法表示
		                    	//TODO 设置
		                    	dataType= 3;
		                    	cell.setCellType(Cell.CELL_TYPE_STRING);
		                    	m.setData(cell.getStringCellValue());
		                    }
		                    break;
		                case Cell.CELL_TYPE_BOOLEAN:
		                	//boolean 类型
		                	dataType= 4;
		                	m.setData(String.valueOf(cell.getBooleanCellValue()));
		                    break;
		                case Cell.CELL_TYPE_FORMULA:
		                	//formula 公式类型
		                	dataType= 5;
		                	m.setData(cell.getCellFormula());
		                    break;
		                case Cell.CELL_TYPE_BLANK:
		                	dataType = 6;
		                	m.setData(null);
		                	break;
		                default:
		                	//throw new SumpException("未识别数据类型");
		            }
		            
		            m.setDataType(dataType);
		            
		            datas.add(m);
		        }
		        if(datas.size() == 1)
		        	break;
		        cells.add(datas);
		    }
		    
		} catch (FileNotFoundException e) {
			
			//throw new SumpException("没有找到文件");
		}catch (EncryptedDocumentException | InvalidFormatException
				| IOException e) {
			
			//throw new SumpException("读取EXCEL文件失败");
		}finally{
			if(inp != null){
				try {
					inp.close();
				} catch (IOException e) {
					
					//throw new SumpException("文件关闭异常");
				}
			}
		}
	}
   

/**
 * 
 * <p>
 * Description: excel导出数据
 * </p>
 * 
 * @author 39xuyanwu
 * @date 2019年10月21日
 * hearder sheet页首列头
 * datas 数据
 * path 生成文件及路径
 * mapKeys list中map的keys
 * sheetName sheet 页名称
 */
public static void writeExcelFile(String[] header,
        List<Map<String, Object>> datas, String path,String[] mapKeys,String sheetName) {
    File file = new File(path);
    if(!file.exists()){//校验文件是否创建成功，如果未成功创建文件目录，再创建文件
        File parentfFile = file.getParentFile();
        if(!parentfFile.exists()){
            parentfFile.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw UsError.UsArgs.E0247();
        }
    }
    if(logger.isDebugEnabled()){
        logger.debug("=========path = %s ", path);

    }
    SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(100);
    Sheet sheet = sxssfWorkbook.createSheet();
    sxssfWorkbook.setSheetName(0, sheetName);
    Row firstRow = sheet.createRow(0);
    for (int i = 0; i < header.length; i++) {
        Cell cell = firstRow.createCell(i);
        cell.setCellValue(header[i]);

    }

    if (datas != null && datas.size() > 0) {
        for (int i = 0; i < datas.size(); i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < mapKeys.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(CommUtil.isNull(datas.get(i).get(mapKeys[j]))?"":datas.get(i).get(mapKeys[j]).toString());
            }
        }
    }
    OutputStream outputStream = null;
    try {
        outputStream = new FileOutputStream(file);

        sxssfWorkbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
    } catch (FileNotFoundException e) {
        throw UsError.UsArgs.E0246();

    } catch (IOException e) {
        throw UsError.UsArgs.E0247();

    } finally {
        if(CommUtil.isNotNull(outputStream)){
            try {
                outputStream.flush();
                outputStream.close();
                
            } catch (IOException e) {
                throw UsError.UsArgs.E0247();
                
            }
        }
        sxssfWorkbook.dispose();

    }
}

}

