package cn.sunline.ltts.busi.hc.util;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.hc.tables.HcbPendDeal.HcbPedl;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IohcbFileIn;
import cn.sunline.ltts.busi.sys.errors.UsError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCTYPE;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HSREAD;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_NESTYN;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_SYNCST;

public class FileUtil {
    private static final BizLog BIZLOG = LogManager.getBizLog(FileUtil.class);
    private static final String FENGEFU = "|@|";//分隔符
    private static final String BIGDECIMALTYPE = "class java.math.BigDecimal";
    private static String tablnm;//分表序号
    public FileUtil(String tablnm){
		FileUtil.tablnm = tablnm;
	}
    /**
     * 导入库
     * 
     * @param tableName 表名
     * @param selSql 查询所有信息sql
     * @param paramName 参数
     */
    public static void readFileContent(String fileph) {
        FileTools.readFile(fileph, new FileDataExecutor() {
            @Override
            public void process(int index, String line) {
                try {
                    String[] lineList = line.split("\\|\\@\\|");

                   // HcbPedl tblHcbHcbPedl = SysUtil.getInstance(HcbPedl.class);
                   // HcbPedl hcbpedl=HcbPedlDao.selectOne_odb1(lineList[0], lineList[2], false);
                    HcbPedl hcbpedl=DaoSplitInvokeUtil.selectOne(HcbPedl.class, "selectOne_odb1", tablnm, lineList[0], lineList[2],false);
                    if(CommUtil.isNull(hcbpedl)){
	                    HcbPedl hcbPedl=SysUtil.getInstance(HcbPedl.class);
	    				hcbPedl.setAmntcd(CommUtil.toEnum(E_AMNTCD.class, lineList[5]));
	    				hcbPedl.setCdcnno(lineList[1]);
	    				hcbPedl.setCorpno(lineList[12]);
	    				hcbPedl.setDealtm(0);
	    				hcbPedl.setDetlsq(lineList[3]);
	    				hcbPedl.setDetlss(E_DETLSS.WCL);
	    				hcbPedl.setHcmain(lineList[6]);
	    				hcbPedl.setHctype(CommUtil.toEnum(E_HCTYPE.class, lineList[4]));
	    				hcbPedl.setNestyn(CommUtil.toEnum(E_NESTYN.class, lineList[8]));
	    				hcbPedl.setTranam(new BigDecimal(lineList[7]));
	    				hcbPedl.setTrandt(lineList[0]);
	    				hcbPedl.setTransq(lineList[2]);
	    				hcbPedl.setTmstmp(lineList[13]);
	    				hcbPedl.setSyncst(E_SYNCST.SUCC);
	    				DaoSplitInvokeUtil.insert(hcbPedl, tablnm);
                    }
                    /*if(CommUtil.isNull(hcbpedl)){

                    	 //设置字段值
                        tblHcbHcbPedl.setTrandt(lineList[0]);
                        tblHcbHcbPedl.setCdcnno(lineList[1]);
                        tblHcbHcbPedl.setTransq(lineList[2]);
                        tblHcbHcbPedl.setDetlsq(lineList[3]);
                        tblHcbHcbPedl.setHctype(CommUtil.toEnum(E_HCTYPE.class, lineList[4]));
                        tblHcbHcbPedl.setAmntcd(CommUtil.toEnum(E_AMNTCD.class, lineList[5]));
                        tblHcbHcbPedl.setHcmain(lineList[6]);
                        tblHcbHcbPedl.setTranam(new BigDecimal(lineList[7]));
                        tblHcbHcbPedl.setNestyn(CommUtil.toEnum(E_NESTYN.class, lineList[8]));
                        tblHcbHcbPedl.setDealtm(0);
                        tblHcbHcbPedl.setDetlss(E_DETLSS.WCL);
                        tblHcbHcbPedl.setSyncst(E_SYNCST.SUCC);
                        tblHcbHcbPedl.setCorpno(lineList[12]);
                        tblHcbHcbPedl.setTmstmp(lineList[13]);
                        HcbPedlDao.insert(tblHcbHcbPedl);   
                    }*/

                              
                } catch (Exception e) {
                    throw UsError.UsComm.E0006(
                            "文件第[" + index + "]行数据处理失败。", e);
                }

            }
        });
    }

    /**
     * 生成表文件
     * 
     * @param tableName 表名
     * @param selSql 查询所有信息sql
     * @param selSql
     * @param paramName 参数
     */

    public static void wirteTableContent(KnpPara knpPara, String tableName, String selSql,String tabnum) { 	

        RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
        String trandt = runEnvs.getTrandt();
        String tmstmp = runEnvs.getTmstmp();


        //上送业务流水赋值
        runEnvs.setBusisq(runEnvs.getTransq());

        KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "UPDATE_LIMIT_SYNC",true);
		int updateCunt=Integer.parseInt(knpPara3.getPmval1());//更同步中改限制条数
        String filePath = knpPara.getPmval1() + File.separator + trandt;
        String fileName = tableName +"_"+ tmstmp.substring(8) + ".txt";
        // 获取文件名
        BIZLOG.info("文件名称 filename:[" + fileName + "]");
        // 获取是否产生文件标志
        BIZLOG.info("文件产生标志 :[" + knpPara.getPmval3() + "]");
        // 产生文件的日期目录
        if (CommUtil.equals(knpPara.getPmval3(), "Y")) {
            final Table table = BaseApltPlugin.getTableByName(BaseApltPlugin.getTableClazzByName(tableName));//获取表模型
            final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
            file.open();// 打开文件流
            try {
                DaoUtil.selectList(selSql, getParams(tableName,updateCunt,corpno),
                        new CursorHandler<Object>() {
                            @Override
                            public boolean handle(int index, Object entity) {
                                if(CommUtil.isNotNull(entity)){
                                    writeContent(file, entity, table);  
                                }   
                                return true;
                            }
                        });

                //inSertHcbFile(filePath,fileName,trandt,corpno);
                IohcbFileIn hcbfilein=SysUtil.getInstance(IohcbFileIn.class);
				hcbfilein.setCorpno(corpno);
				hcbfilein.setFlname(fileName);
				hcbfilein.setFlpath(filePath);
				hcbfilein.setHsread(E_HSREAD.WD);
				hcbfilein.setTrandt(trandt);
				hcbfilein.setTabnum(tabnum);
				BIZLOG.info("===========外调管理节点===========");
				SysUtil.getRemoteInstance(IoHotCtrlSvcType.class).addHcbFile(hcbfilein);

            } catch (Exception e) {
            	File failfile=new File(filePath+File.separator+fileName);    
            	if(failfile.exists())    
            	{    
            		failfile.delete();
            	}              	
                throw LnError.geno.E0000("生成" + table.getLongname() + "文件错误");
            } finally {   

            	BIZLOG.info("===========关闭文件流===========");

                file.close();// 关闭文件流
            }
        }
    }


   /* public static void inSertHcbFile(final String filePath,final String fileName,final String trandt,final String corpno) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				BIZLOG.debug("===========《《外调管理节点》》===========");

				//文件记录输入要素
				IohcbFileIn hcbfilein=SysUtil.getInstance(IohcbFileIn.class);
				hcbfilein.setCorpno(corpno);
				hcbfilein.setFlname(fileName);
				hcbfilein.setFlpath(filePath);
				hcbfilein.setHsread(E_HSREAD.WD);
				hcbfilein.setTrandt(trandt);		
				SysUtil.getRemoteInstance(IoHotCtrlSvcType.class).addHcbFile(hcbfilein);
			    return null;
			}
		});

	}*/

    /**
     * 
     * @param file
     *        写入流
     * @param entity
     *        需写入的内容
     * @param clazz
     *        表模型
     */
    public static void writeContent(final LttsFileWriter file, Object entity,
            Table table) {
        StringBuffer sbRecvInfo = new StringBuffer();
        Map<String, Object> map = CommUtil.toMap(entity);//将查出数据转为map
        List<Field> fields = table.getAllElements();//获取表所有字段
        for (Field field : fields) {
            /*判断字段值类型，如果为字段值为空时，根据类型初始化字段值*/
            sbRecvInfo.append(CommUtil.isNull(map.get(field.getId())) ?
                    (CommUtil.equals(BIGDECIMALTYPE, field.getElementJavaClass().toString()) ?
                            BigDecimal.ZERO : "") : map.get(field.getId()));

            sbRecvInfo.append(FENGEFU);
        }
        file.write(sbRecvInfo.toString());
    }

    private static Params getParams(String tableName,int updateCunt,String corpno) {
        Params params = new Params();
        params.put("tablename", tableName);
        params.put("limict", updateCunt);
        params.put("corpno", corpno);
        return params;
    }
}
