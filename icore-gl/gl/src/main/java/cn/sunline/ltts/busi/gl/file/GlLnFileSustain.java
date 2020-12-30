package cn.sunline.ltts.busi.gl.file;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_lnledger_check_seqDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_seq;
import cn.sunline.ltts.busi.gl.type.GlFile.GlCheckRecord;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileHead;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_BATCHTYPE;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaConst;

public class GlLnFileSustain {

	private static final BizLog bizlog = BizLogUtil.getBizLog(GlLnFileSustain.class);

	/**
	 * 
	 * @param firstLine busiBatchCode
	 * @Author 
	 *         <p>
	 *         <li>2020年11月2日-下午7:28:59</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param string
	 * @return
	 */
	public static GlFileHead checkHeadInfo(String firstLine, String busiBatchCode) {
		
		bizlog.method(" GlFileSustain.checkHeadInfo begin >>>>>>>>>>>>>>>>");

		GlFileHead glFileHead = SysUtil.getInstance(GlFileHead.class);
		String[] lineSplits = firstLine.split(FaConst.LOAN_SEPARATION_CHARACTER);
		if(lineSplits.length != 2) {
			setFileDealStatus(busiBatchCode);
			throw GlError.GL.E0235(firstLine);
		}
		
		glFileHead.setHead_total_count(Long.valueOf(lineSplits[0]));		// 文件头记录数
		glFileHead.setHead_total_amt(new BigDecimal(lineSplits[1]));		// 文件头金额
		
		// 如果文件头格式不符
		if (CommUtil.isNull(glFileHead.getHead_total_count()) || CommUtil.isNull(glFileHead.getHead_total_amt())) {
			setFileDealStatus(busiBatchCode);
			throw GlError.GL.E0113();
		}
		
		bizlog.debug("fileHead[%s]", glFileHead);
		bizlog.method(" GlFileSustain.checkHeadInfo end <<<<<<<<<<<<<<<<");
		
		return glFileHead;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月3日-上午11:05:09</li>
	 *         <li>功能说明：另起事务更新文件格式有误</li>
	 *         </p>
	 * @param busiBatchCode
	 */
	private static void setFileDealStatus(String busiBatchCode) {
		
		bizlog.method(" GlFileSustain.setFileDealStatus begin >>>>>>>>>>>>>>>>");

		DBTools.exeInNewTransation(new RunnableWithReturn<Void>() {
            @Override
            public Void execute() {
            	FaApBatch.setFormatErrorByImport(busiBatchCode);
                return null;
            }
        });
		
		bizlog.method(" GlFileSustain.setFileDealStatus end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月6日-上午10:27:14</li>
	 *         <li>功能说明：对比两个参数中的数据是否一致</li>
	 *         </p>
	 * @param fileCheckRecordList
	 * @param glCheckRecordedList
	 */
	public static void checkDataOfFileAndRecord(List<GlCheckRecord> fileCheckRecordList, List<GlCheckRecord> glCheckRecordedList) {
		
		bizlog.method(" GlLnFileSustain.checkDataOfFileAndRecord begin >>>>>>>>>>>>>>>>");
		
		// 对比大小
		if (fileCheckRecordList.size() != glCheckRecordedList.size()) {	
			throw GlError.GL.E0239(E_BATCHTYPE.LN_CHECK_DOWN.getLongName(), fileCheckRecordList.size(), glCheckRecordedList.size());
		}
		
		// 转换成以文件类型为key的map
		Map<E_BATCHTYPE, GlCheckRecord> fileMap = fileCheckRecordList.stream().collect(
				Collectors.toMap(GlCheckRecord::getFile_type, Function.identity(),(key1, key2) -> key2));
		Map<E_BATCHTYPE, GlCheckRecord> glMap = glCheckRecordedList.stream().collect(
				Collectors.toMap(GlCheckRecord::getFile_type, Function.identity(),(key1, key2) -> key2));
		bizlog.debug("fileMap[%s],glDatas[%s]", fileMap, glMap);
		
		// 以文件内容为基准，根据key值进行判断
		fileMap.forEach((fileType, fileData) -> {
			
			GlCheckRecord glData = glMap.get(fileType);
			if(CommUtil.isNull(glData)) {
				throw GlError.GL.E0240(fileType.getLongName());
			}
			if(!fileData.getRecord_number().equals(glData.getRecord_number())) {	// 对比记录数
				throw GlError.GL.E0236(fileType.getLongName(), fileData.getRecord_number(), glData.getRecord_number());
			}
			if(CommUtil.compare(fileData.getTotal_amt(),glData.getTotal_amt()) != 0) {	// 对比交易金额 
				throw GlError.GL.E0237(fileType.getLongName(), fileData.getTotal_amt(), glData.getTotal_amt());
			}
			
		});
		
		bizlog.method(" GlLnFileSustain.checkDataOfFileAndRecord end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月7日-下午5:33:38</li>
	 *         <li>功能说明：贷款分户余额文件解析失败后仍然落地</li>
	 *         </p>
	 * @param lederData
	 */
	public static void insertFailLnLedger(fab_lnledger_check_seq lederData) {
		
		bizlog.method(" GlLnFileSustain.insertFailLnLedger begin >>>>>>>>>>>>>>>>");

		DBTools.exeInNewTransation(new RunnableWithReturn<Void>() {
            @Override
            public Void execute() {
                lederData.setAnalysis_state(E_ANALYSISSTATE.FAILURE);  // 将失败记录插入到数据库
                Fab_lnledger_check_seqDao.insert(lederData);
                return null;
            }
        });
		
		bizlog.method(" GlLnFileSustain.insertFailLnLedger end <<<<<<<<<<<<<<<<");
	}

}
