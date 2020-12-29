package cn.sunline.ltts.busi.aptran.batchtran;

import java.util.List;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.dao.base.DbType;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 分区表切换分区
	  *
	  */

public class ap03DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap03.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap03.Property> {
  
	
	private static BizLog log = LogManager.getBizLog(ap03DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap03.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap03.Property property) {
		 
		 String cnfrdm = CommTools.getCenterCorpno();
		 AppSydt tblKapp_sysdat = AppSydtDao.selectOne_odb1(cnfrdm, true);
		 
		 List<KnpPara> lstHistTab = KnpParaDao.selectAll_odb2("HistTab", false);

			for(KnpPara para : lstHistTab){
				hisTabDeal(para, tblKapp_sysdat);
			}
	}
	 
	 private static void hisTabDeal(KnpPara para, AppSydt tblKapp_sysdat){
			
		String tbname = para.getPmkey1().toUpperCase(); //表名称
		String part_model = para.getPmkey3(); //分区模式，按天stndard 或按月month
		String hld_count = para.getPmval1(); //表中保留的分区个数，默认2个分区
		String day_length = CommUtil.nvl(para.getPmval2(), "8"); //数据表中 用于做分区的日期字段的长度 10 或者 8
		String subpart_num = para.getPmval3(); //存放子分区数量


		String last_date = "";
		String curr_date = "";
		String next_date = tblKapp_sysdat.getNextdt(); //下次日期
		
		if(CommUtil.equals(part_model, "month")){ //按月分区
			curr_date = tblKapp_sysdat.getSystdt(); //当前日期
			last_date = DateTools.calDateByTerm(curr_date, "-1M"); //推算上一个月的日期
			
			if(CommUtil.isNotNull(hld_count)){
				String term = "-" + hld_count + "M";
				last_date = DateTools.calDateByTerm(curr_date, term); //推算上一个月的日期
			}
			
			partitionMon(tbname, last_date, curr_date, day_length);
			
		} else if(CommUtil.equals(part_model, "stndard") || CommUtil.isNull(part_model)){ //分区模式 按天分区
			curr_date = tblKapp_sysdat.getSystdt(); //当前日期
			last_date = tblKapp_sysdat.getBflsdt(); //上上次日期
			String afnx_date = tblKapp_sysdat.getAfnxdt();//下下日期
			
			if(CommUtil.isNotNull(hld_count)){
				curr_date = tblKapp_sysdat.getSystdt(); //当前日期
				last_date = DateTools.dateAdd(ConvertUtil.toInteger(hld_count), curr_date);
			}
			
			partitionDay(tbname, last_date, curr_date, next_date, day_length, subpart_num);//检查当日分区及下日分区
			partitionDay(tbname, curr_date, next_date, afnx_date, day_length, subpart_num);//检查下日分区及下下日分区
		}
		
		log.info("<<======转储日期:[%s]", last_date);
		log.info("<<======当前日期:[%s]", curr_date);
		log.info("<<======转储表名:[%s]", tbname);
			
	 }
	 /**
	  * @Title: partitionMon 
	  * @Description: 按月切换分区  
	  * @param tbname
	  * @param last_mon_date 需要卸载的分区的月份日期
	  * @param curr_date
	  * @author zhangan
	  * @date 2017年3月22日 上午11:11:57 
	  * @version V2.3.0
	  */
	 private static void partitionMon(String tbname, String last_mon_date, String curr_date, String length){
		 String bgn_date = ""; //开始日期
		 String end_date = ""; //结束日期
		 String sql = "";
		 String ptname = ""; //分区名称
		 
		//获取数据库类型
		//DbType databaseType = DBConnectionFactory.getDataSource(DBConnectionFactory.getDefaultDataSourceId()).getDatabaseType();
		DbType databaseType = DbType.valueOfType(EdspCoreBeanUtil.getDBConnectionManager().getDatabaseDbType());
		log.info("databaseType=============================>[%s]", databaseType);
		
		 //当前月分区
		 ptname = curr_date.substring(0, 6);
		 String name = ApSysBatchDao.selPartitionFromSyscat(tbname, ptname, false);
		 if(CommUtil.isNull(name)){
			 if(databaseType == DbType.MYSQL){
				//1：新增一个分区	
				bgn_date = cvtDate(DateTools.getCalendarDate(curr_date, "02"), length); //获取下月初日期
				sql = "alter table " + tbname + " add partition (partition p" + ptname + " values less than ('" + bgn_date +"'))";
				log.info("<<======增加当前月分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			 } else if(databaseType == DbType.ORACLE){
				//Oracle11g间隔分区功能 ,按月或日建分区可自动生成分区
				bgn_date = cvtDate(DateTools.getCalendarDate(curr_date, "02"), length); //获取下月初日期
				bgn_date = bgn_date.replace("-", "");
				//sql = "alter table " + tbname + " add partition  p" + ptname + " VALUES LESS THAN(TO_DATE('" + bgn_date +"','YYYY-MM-DD'))";
				sql = "alter table " + tbname + " add partition  p" + ptname + " VALUES LESS THAN('" + bgn_date +"')"; //日期为varchar2类型
				log.info("<<======增加当前月分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);												 
			} else if(databaseType == DbType.DB2){
				bgn_date = cvtDate(DateTools.getCalendarDate(curr_date, "01"), length); //获取月初
				end_date = cvtDate(DateTools.getCalendarDate(curr_date, "03"), length);; //获取月末日期
				sql = "alter table " + tbname + " add partition " + ptname + " starting '" + bgn_date + "' ending '" + end_date + "'";
				log.info("<<======增加当前日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			}
					 
		 }
		//查询下个分区是否存在，不存在则创建
		bgn_date = DateTools.getCalendarDate(curr_date, "02"); //下个月的第一天
		end_date = DateTools.getCalendarDate(curr_date, "04"); //下个月的月末
		ptname = bgn_date.substring(0, 6); //截取前六位
		name = ApSysBatchDao.selPartitionFromSyscat(tbname, ptname, false);
		if(CommUtil.isNull(name)){
			 if(databaseType == DbType.MYSQL){
				//1：新增一个分区	
				bgn_date = DateTools.getCalendarDate(bgn_date, "02"); //下下个月的第一天
				sql = "alter table " + tbname + " add partition (partition p" + ptname + " values less than ('" + bgn_date +"'))";
				log.info("<<======增加下个月分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			 } else if(databaseType == DbType.ORACLE){
				String aftnex = cvtDate(DateTools.getCalendarDate(bgn_date, "02"), length); //下下个月的第一天
				aftnex = aftnex.replace("-", "");
				//sql = "alter table " + tbname + " add partition  p" + ptname + " VALUES LESS THAN(TO_DATE('" + aftnex +"','YYYY-MM-DD'))";
				sql = "alter table " + tbname + " add partition  p" + ptname + " VALUES LESS THAN('" + aftnex +"')"; //日期为varchar2类型
				log.info("<<======增加下个月分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);												 
			} else if(databaseType == DbType.DB2){				
				bgn_date = cvtDate(bgn_date, length);
				end_date = cvtDate(end_date, length);
				sql = "alter table " + tbname + " add partition " + ptname + " starting '" + bgn_date + "' ending '" + end_date + "'";
				log.info("<<======增加下次日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			}
		}
		
		//2:查询旧分区是否存在
		ptname = last_mon_date.substring(0, 6);
		name = ApSysBatchDao.selPartitionFromSyscat(tbname, ptname, false);
		if(CommUtil.isNotNull(name)){
			if(databaseType == DbType.DB2){
				String his_tbname = "H_" + tbname + "_" + ptname;
				//3: 卸载一个旧的分区
				sql = "alter table " + tbname + " detach partition " + ptname + " into " + his_tbname;
				log.info("<<======卸载上上次日期分区SQL:[%s]", sql);
				log.info("<<======上上次分区历史表名称:[%s]", his_tbname);
				DBTools.executeSQL(sql);
			}
		}
	 }
	 /**
	  * @Title: partition 
	  * @Description: 按天分区时，做分区转换  
	  * @param tbname 表名
	  * @param last_date 上个分区名
	  * @param curr_date 当前分区名
	  * @param next_date 下个分区名
	  * @author zhangan
	  * @date 2017年3月22日 上午11:10:09 
	  * @version V2.3.0
	  */
	 private static void partitionDay(String tbname, String last_date, String curr_date, String next_date, String length,String subpart_num){
		
		//获取数据库类型
		//DbType databaseType = DBConnectionFactory.getDataSource(DBConnectionFactory.getDefaultDataSourceId()).getDatabaseType();
		DbType databaseType = DbType.valueOfType(EdspCoreBeanUtil.getDBConnectionManager().getDatabaseDbType());
		log.info("databaseType=============================>[%s]", databaseType);
		
		String bgn_date = "";
		String end_date = "";
		//查询当前日期的分区是否存在
		String name = ApSysBatchDao.selPartitionFromSyscat(tbname, curr_date, false);
		String sql = "";
		if(CommUtil.isNull(name)){
			//1：新增当前日期一个分区
			if(databaseType == DbType.MYSQL){
			//mysql数据库增加分区
				sql = "alter table " + tbname + " add partition (partition p" + curr_date + " values less than ('" +next_date +"'))";
				log.info("<<======增加当前日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			} else if(databaseType == DbType.ORACLE){
			//oracle数据库 增加分区
				sql = "alter table " + tbname + " add partition p" + curr_date + " values less than ('" +next_date +"')";
				 if(CommUtil.isNotNull(subpart_num)){
					 int subpartNum = Integer.parseInt(subpart_num);
					 int subPart = subpartNum -1 ;
					 log.info("subpartNum======================>[%s]", subPart);
					 sql = sql + "(";
					 for(int i = 0; i < subpartNum; i++ ){ 
						 if(i != subPart){
							sql = sql + "subpartition p" + curr_date + "_p" + i +",";
						 } else {
							sql = sql + "subpartition p" + curr_date + "_p" + i +")";
						 }
						}
					}
				 log.info("<<======增加当前日期分区及子分区SQL:[%s]", sql);
				 DBTools.executeSQL(sql);
			}else if(databaseType == DbType.DB2){
				bgn_date = cvtDate(curr_date, length);
				end_date = cvtDate(curr_date, length);
				sql = "alter table " + tbname + " add partition " + curr_date + " starting '" + bgn_date + "' ending '" + end_date + "'";
				log.info("<<======增加当前日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			}
		}
		//查询下个分区是否存在，不存在则创建
		name = ApSysBatchDao.selPartitionFromSyscat(tbname, next_date, false);
		int nextDate = Integer.parseInt(next_date)+1;
		if(CommUtil.isNull(name)){
			//2：新增下个日期分区
			if(databaseType == DbType.MYSQL){ 
			//mysql数据库增加分区
				sql = "alter table " + tbname + " add partition (partition p"  + next_date + " values less than ('"+ nextDate + "'))";
				log.info("<<======增加下次日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			} else if(databaseType == DbType.ORACLE){ 
			//oracle数据库 增加分区
				sql = "alter table " + tbname + " add partition p" + next_date + " values less than ('" + nextDate +"')";
				if(CommUtil.isNotNull(subpart_num)){
					 int subpartNum = Integer.parseInt(subpart_num);
					 int subPart = subpartNum -1 ;
					 log.info("subpartNum======================>[%s]", subPart);
					 sql = sql + "(";
					 for(int i = 0; i < subpartNum; i++ ){ 
						 if(i != subPart){
							sql = sql + "subpartition p" + next_date + "_p" + i +",";
						 } else {
							sql = sql + "subpartition p" + next_date + "_p" + i +")";
						 }
						}
					 }
					 log.info("<<======增加下日期分区及子分区SQL:[%s]", sql);
					 DBTools.executeSQL(sql);
			} else if(databaseType == DbType.DB2){
				bgn_date = cvtDate(next_date, length);
				end_date = cvtDate(next_date, length);
				sql = "alter table " + tbname + " add partition " + next_date + " starting '" + bgn_date + "' ending '" + end_date + "'";
				log.info("<<======增加下次日期分区SQL:[%s]", sql);
				DBTools.executeSQL(sql);
			}
		}
		
		//3:查询旧分区是否存在
		name = ApSysBatchDao.selPartitionFromSyscat(tbname, last_date, false);
		if(CommUtil.isNotNull(name)){
			if(databaseType == DbType.DB2){
				String his_tbname = "H_" + tbname + "_" + last_date;
				//3: 卸载一个旧的分区
				sql = "alter table " + tbname + " drop partition p" + last_date ;
				log.info("<<======卸载上上次日期分区SQL:[%s]", sql);
				log.info("<<======上上次分区历史表名称:[%s]", his_tbname);
				DBTools.executeSQL(sql);
			}
		}
	 }
	 
	 private static String cvtDate(String strDate, String length){
		 
		 if(CommUtil.equals("10", length)){ //日期长度10位 yyyy-mm-dd 需要将8位日期转换成10位日期
			 strDate = strDate.substring(0,4) + "-" + strDate.substring(4,6) + "-" + strDate.substring(6);
		 }
		 
		 return strDate;
	 }

}


