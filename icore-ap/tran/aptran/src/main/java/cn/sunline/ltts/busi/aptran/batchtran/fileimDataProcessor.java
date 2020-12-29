package cn.sunline.ltts.busi.aptran.batchtran;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.file.FileUtil;
import cn.sunline.ltts.busi.aplt.namedsql.ApSynFileDao;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.ApbFileStat;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.ApbFileStatDao;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Input;
import cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Property;
import cn.sunline.ltts.busi.bsap.util.DaoInvokeUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SYNCTP;
	 /**
	  * 其他节点数据文件导入目标节点
	  *
	  */

public class fileimDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Property, cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove> {
	private static final BizLog bizlog = LogManager.getBizLog(fileimDataProcessor.class);
	private static char sperate = '_';//文件名分隔符
	private static String daorbm = null;//导入表名
	private static String unikey=null;//主键
	private static String selway=null;//方法名
	private static String selway1=null;//查询方法
	private static String selway2=null;//更新方法
	private static E_SYNCTP synctp=null;//同步方法

	private static String dbdcnf=null;//删除数据依据dcn字段
	private static int count=0;//循环次数
	private static int time=0;//每次等待时间
	private static String path=null;//路径
	private static String pattern=null;//文件格式
	
	@Override
		public void beforeTranProcess(String taskId, Input input,
				Property property) {
			// TODO Auto-generated method stub
			super.beforeTranProcess(taskId, input, property);
			
			KnpPara knpPara  = CommTools.KnpParaQryByCorpno("SyncFile", "ApFile", "%", "%", false);
			if(CommUtil.isNull(knpPara)){
				throw Aplt.E0000("查询参数表parmcd=SyncFile,pmkey1=ApFile,pmkey2=%,pmkey3=% 的记录为空，请检查");
			}
			if(CommUtil.isNull(knpPara.getPmval1())|| CommUtil.isNull(knpPara.getPmval2())|| CommUtil.isNull(knpPara.getPmval3()) || CommUtil.isNull(knpPara.getPmval4())){
				throw Aplt.E0000("查询参数表parmcd=SyncFile,pmkey1=ApFile,pmkey2=%,pmkey3=% 的数据记录Pmval1、Pmval2、Pmval3、Pmval4有空值，请检查");
			}
			path=knpPara.getPmval1();//路径
			pattern=knpPara.getPmval2();//文件格式
			//数字校验
			if(isNumber(knpPara.getPmval3()) && isNumber(knpPara.getPmval4())){
				 count= Integer.parseInt(knpPara.getPmval3().trim());
			     time=Integer.parseInt(knpPara.getPmval4().trim());
			 }else{
				 throw Aplt.E0000("查询参数表parmcd=SyncFile,pmkey1=ApFile,pmkey2=%,pmkey3=% 的数据记录Pmval3、Pmval4 应为正整数，请检查"); 
			 }	
			
		}
	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove dataItem, cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Property property) {
			

			String dacdcn=dataItem.getDacdcn();//导出dcn
			String dardcn=dataItem.getDardcn();//导入dcn
			daorbm=dataItem.getDaorbm();//导入表名	
			synctp=dataItem.getSynctp();//同步方式：增量-全量
			unikey=dataItem.getUnikey();//主键
			selway=dataItem.getSelway();//查询方法名
			dbdcnf=dataItem.getDbdcnf();//删除数据依据dcn字段
					
			if(CommUtil.isNull(dacdcn)){	
				throw Aplt.E0000("app_data_move表中数据存在导出dcn为空，请输入");
			}

			if(CommUtil.isNull(dardcn)){	
				throw Aplt.E0000("app_data_move表中数据存在导入dcn为空，请输入");
			}
	
			if(CommUtil.isNull(daorbm)){	
				throw Aplt.E0000("app_data_move表中数据存在导入表名为空，请输入");
			}
			
			if(CommUtil.isNull(synctp)){	
				throw Aplt.E0000("app_data_move表中数据，导入表名为"+daorbm+"的同步方式为空，请输入");
			}	
			if(synctp==E_SYNCTP.UQ){
				if(CommUtil.isNull(unikey)){	
					throw Aplt.E0000("app_data_move表中数据，导入表名为"+daorbm+"的主键为空，请输入，若多个','隔开");
				}
				String[] method = selway.replace(" ", "").split(",");
				if(CommUtil.isNotNull(selway)){
					for(int m=0;m<method.length;m++){
						if(method[m].startsWith("sele")){
							selway1=method[m];
						}else if(method[m].startsWith("upda")){
							selway2=method[m];				
						}	
					}
				}else{
					throw Aplt.E0000("app_data_move表中数据，导入表名为"+daorbm+"的方法名为空，请输入查询及更新方法名','隔开");
				}
			}
			String trandt=CommTools.prcRunEnvs().getTrandt();//上日日期	
			String filename=daorbm+sperate+dacdcn+sperate+dardcn+sperate+trandt+sperate+"FN"+sperate+dataItem.getUnikey()+pattern;//文件名
			String fileFullName=path+trandt+"/"+filename;//全路径			
			bizlog.debug("文件全路径[" + fileFullName + "]");	
			
			ApbFileStat apbFileStat=ApbFileStatDao.selectOne_odb1(trandt, filename, false);//查询状态
		
			if(CommUtil.isNotNull(apbFileStat) && apbFileStat.getFilest()==E_FILEST.YDR){
				return;
			}else{
				 boolean re=false;
				 for(int ct=0;ct<count;ct++){
					if(readSleep(fileFullName,time)){
						re=true;
						break;
					}
					
				 }
				if(re==false){
					throw Aplt.E0000(fileFullName+"文件未生成");
				}else{
					if (synctp==E_SYNCTP.QL){//全部删除后全量新增
						if(CommUtil.isNotNull(dbdcnf)){
							DBTools.executeSQL("delete from "+ daorbm +" where "+dbdcnf+" = '"+dacdcn+"'");
						}else{
							DBTools.executeSQL("delete from "+ daorbm);
						}	
					}
					fileRead(fileFullName);
					
					//导入后修改状态
					if(CommUtil.isNull(apbFileStat)){
						ApbFileStat apbfilestat=SysUtil.getInstance(ApbFileStat.class);
						apbfilestat.setFilena(filename);
						apbfilestat.setFilest(E_FILEST.YDR);
						apbfilestat.setPrcscd("fileim");
						apbfilestat.setTrandt(trandt);
						
						ApbFileStatDao.insert(apbfilestat);
					}else{
						apbFileStat.setFilest(E_FILEST.YDR);
						ApbFileStatDao.updateOne_odb1(apbFileStat);						
					}	
				}

			}
	 		  
			//公共业务参数,
		    KnpPara knppar = ApKnpPara.getKnpPara("fntran.parm", "%", "%");
		    if(CommUtil.isNotNull(knppar)){
			List<AppDataMove> apsyn=ApSynFileDao.selAppDataMoveInfo(dacdcn, false);
			for (AppDataMove appDataMove : apsyn) {
				if(CommUtil.isNull(appDataMove.getUnikey())){
					continue;
				}
				String numbl=appDataMove.getUnikey();
				BigDecimal nmhder=BigDecimal.ZERO;//下次文件后缀
				BigDecimal nuber=new BigDecimal(numbl);//文件末尾数字
				BigDecimal nuberd=new BigDecimal(knppar.getPmval1());//最大条数
				BigDecimal addnu=new BigDecimal(1);//最大条数
				if(CommUtil.compare(nuber, nuberd)>=0){//超过最大条数
					nuber=nmhder;
				}
				nmhder=nuber.add(addnu);
				ApSynFileDao.updateAppDateMove(nmhder.toString(),dacdcn);
			  }
		    }
		}
		//读取文件解析导入目标数据库
		public static void fileRead(String fileFullName){
			// 处理文件记录
			FileUtil.readFile(fileFullName, new FileDataExecutor() {	
				@Override
				public void process(int index, String line) {
					try {		
						// 第一行为文件头
						if (index == 1) {
							return;
						}	
						Object entity=SysUtil.deserialize(line,BaseApltPlugin.getTableClazzByName(daorbm));	
						String[] unikeys = unikey.replace(" ", "").split(",");
						Object[] object = new Object[unikeys.length + 1];
						if(CommUtil.isNotNull(unikey)){	
							Map<String,Object> map=CommUtil.toMap(entity);				
							for(int l=0;l<unikeys.length;l++){			
								for(String filed:map.keySet()){
									if(CommUtil.equals(filed, unikeys[l])){
										object[l]=map.get(filed);
										break;
									}
								}
							}
							object[unikeys.length]= Boolean.FALSE;//查询方法后的布尔类型
						}
						if(synctp==E_SYNCTP.ZL || synctp==E_SYNCTP.QL ){
							DaoInvokeUtil.insert(entity);
						}else{
							if(CommUtil.isNotNull(DaoInvokeUtil.selectOne(daorbm, selway1, object))){//查询
								DaoInvokeUtil.update(entity, selway2);//更新
							}else{
								DaoInvokeUtil.insert(entity);//新增
							}
						}				
					} catch (Exception e) {
						e.printStackTrace();
						throw Aplt.E0000("文件第[" + index + "]行数据处理失败.",e);
					}			
				}
			});				
			
		}
		
		//等待时间
		public static boolean readSleep(String fileFullName,int time ){
			boolean result=false;
			File file=new File(fileFullName);    
			if(!file.exists()){    
			    try {    
			    	Thread.sleep(time);  
			    } catch (Exception e) {
					e.printStackTrace();
				}    
			} else{
				result=true;
			}
			return result;  
		}
		//数字校验
		 public static boolean isNumber(String str){
		        Pattern pattern=java.util.regex.Pattern.compile("^[1-9]*[1-9][0-9]*$");
		        Matcher match=pattern.matcher(str);
		        return match.matches();
		    }
		 
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove> getBatchDataWalker(cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Fileim.Property property) {
			Params params = new Params();
			params.add("cdcnno", CommToolsAplt.prcRunEnvs().getCdcnno());
			return new CursorBatchDataWalker<>(
					ApSynFileDao.namedsql_selAppDataMoveInput, params);
		}

}


