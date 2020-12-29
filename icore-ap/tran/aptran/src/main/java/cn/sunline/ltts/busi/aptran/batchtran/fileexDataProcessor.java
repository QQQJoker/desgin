package cn.sunline.ltts.busi.aptran.batchtran;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.namedsql.ApSynFileDao;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.ApbFileStat;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.ApbFileStatDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aptran.tools.FileexTools;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SYNCTP;

/**
 * 批量文件导出
 * 
 */

public class fileexDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Property, cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(fileexDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			String jobId,
			int index,
			cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove dataItem,
			cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Input input,
			cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Property property) {
		// TODO:
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String lstrdt = runEnvs.getTrandt();
		String cdcnno = runEnvs.getCdcnno();

		KnpPara knpPara = CommTools.KnpParaQryByCorpno("SyncFile", "ApFile", "%",
				"%", false);
		if (CommUtil.isNull(knpPara)) {
			throw ApError.Aplt.E0001("没有配置文件路径，请核对！！！");
		}
		String filePath = knpPara.getPmval1() + lstrdt;
		String fileName = dataItem.getDaorbm() + "_" + dataItem.getDacdcn()
				+ "_" + dataItem.getDardcn() + "_" + lstrdt+"_"+"FN"+"_"+dataItem.getUnikey()
				+ knpPara.getPmval2();

		// 登记文件生成状态表
		ApbFileStat apbFileStat = ApbFileStatDao.selectOne_odb1(lstrdt,
				fileName, false);

		if (CommUtil.isNotNull(apbFileStat)) {
			return;
		}
		List<?> list = new ArrayList<>();
		StringBuffer paramBuffer = new StringBuffer("'");
		// clazz.newInstance();
		// runEnvsMap.
		if (E_SYNCTP.ZL == dataItem.getSynctp()) {
			paramBuffer.append(lstrdt);
		} else {
			paramBuffer.append(cdcnno);
		}
		paramBuffer.append("'");
		String param = paramBuffer.toString();
		String sql = dataItem.getNamsql().replaceFirst("\\?", param);
		bizlog.method("执行SQL[%s]", sql);
		appendSQL(sql);
		list = FileexTools.selectObject(sql, dataItem.getDaocbm());
		final LttsFileWriter file = new LttsFileWriter(filePath, fileName,
				"UTF-8");
		;
		file.open();
		int count = list.size();
		file.write(Integer.toString(count) + "/" + "n");
		for (Object object : list) {
			file.write(JsonUtil.format(object));
		}
		file.close();
		apbFileStat = SysUtil.getInstance(ApbFileStat.class);
		apbFileStat.setPrcscd(runEnvs.getPrcscd());
		apbFileStat.setTrandt(lstrdt);
		apbFileStat.setFilena(fileName);
		apbFileStat.setFilest(E_FILEST.YSC);
		ApbFileStatDao.insert(apbFileStat);

	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove> getBatchDataWalker(
			cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Input input,
			cn.sunline.ltts.busi.aptran.batchtran.intf.Fileex.Property property) {
		// TODO:
		Params params = new Params();
		params.add("cdcnno", CommToolsAplt.prcRunEnvs().getCdcnno());
		return new CursorBatchDataWalker<>(
				ApSynFileDao.namedsql_selAppDataMoveInfo, params);
	}

	// nameSql =
	// "select  corpno,prodcd,fnacst,#cdcnno# as dcnbianh ,prodna,prodbr,inbgdt,ineddt,ftrate,ttrate,facevl,sum(totaam),sum(totafe),sum(nowtam),sum(purcam),sum(ranmam),sum(failam)  from fna_acct group by  corpno,prodcd,fnacst;";
	public String appendSQL(String nameSql) {
		String[] splits = nameSql.split("#");
		StringBuffer paramBuffer = new StringBuffer("");
		Map<String, Object> runEnvsMap = CommUtil.toMap(CommToolsAplt
				.prcRunEnvs());
		for (int i = 0; i < splits.length; i++) {
			if (i % 2 == 0) {
				paramBuffer.append(splits[i]);
			} else {
				String fieldName = splits[i];
				splits[i] = runEnvsMap.get(fieldName).toString();
				paramBuffer.append("'").append(splits[i]).append("'");
			}
		}
		return paramBuffer.toString();
	}

}
