package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

/**
 * 换日前检查
 * 
 */

public class agochckDataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.Agochck.Input, cn.sunline.ltts.busi.aptran.batchtran.Agochck.Property> {

	 private static final BizLog bizlog = LogManager.getBizLog(agochckDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.Agochck.Input input,
			cn.sunline.ltts.busi.aptran.batchtran.Agochck.Property property) {

		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();// 交易日期
		
		
	
		String timetm = CommToolsAplt.prcRunEnvs().getTmstmp();// 当前时间戳
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();// 法人代码
		String timted = "19000101000001";// 初始当前时间
		String rndate = "19000101000000";// 初始跑批时间
		bizlog.info("corpno=[%s]", corpno);
		// 增加时间点内不允许跑批校验
		try {
			//KnpPara para = KnpParaDao.selectOne_odb1("DateCheck", "%", "%", "%",corpno, true);
			KnpPara par = CommTools.KnpParaQryByCorpno("DateCheck", "%", "%", "%", true);
			if (CommUtil.equals(par.getPmval1(), "Y")) {
				AppSydt tblAppSydt = AppSydtDao.selectOne_odb1(corpno, true);
				rndate = tblAppSydt.getSystdt() + par.getPmval3();
				timted = timetm.substring(0, 14);
			
				
				bizlog.info("timted=[%s]", timted);
				bizlog.info("rndate=[%s]", rndate);
			}
		}catch (Exception e) {
			
		}

		bizlog.info("timted=[%s]", timted);
		bizlog.info("rndate=[%s]", rndate);
		
		if (CommUtil.compare(timted, rndate) < 0) {
			throw Aplt.E0000("当前时间点未超过" + rndate + "不允许进行切日操作！");
		}
		
		
/*	
		// 获取交易日期还未结息笔数
		int count = ApSysBatchDao.selCntByintr(trandt, false);

		if (count > 0) {
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {

				@Override
				public Integer execute() {
					// 监控预警平台
					KnpPara para = CommTools.KnpParaQryByCorpno("DAYENDNOTICE", "%", "%", "%", true);

					String bdid = para.getPmval1();// 服务绑定ID

					String mssdid = CommTools.getMessageId();// 随机生成消息ID

					String mesdna = para.getPmval2();// 媒介名称

					IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(IoCaOtherService.class, bdid);

					IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = CommTools
							.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);

					String timetm = DateTools.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
					IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
					content.setPljioyma("agochck");
					content.setPljyzbsh("90000");
					content.setPljyzwmc("完成结息检查异常预警");
					content.setErrmsg("完成结息检查失败");
					content.setTrantm(timetm);

					// 发送消息
					mqInput.setMsgid(mssdid); // 消息ID
					mqInput.setMdname(mesdna); // 媒介名称
					mqInput.setTypeCode("NAS");
					mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
					mqInput.setItemId("NAS_BATCH_WARN");
					mqInput.setItemName("电子账户核心批量执行错误预警");

					String str = JSON.toJSONString(content);
					mqInput.setContent(str);

					mqInput.setWarnTime(timetm);

					caOtherService.dayEndFailNotice(mqInput);

					return null;
				}
			});
			throw Aplt.E0000("还存在未结息笔数：" + count + "笔！");
		  }*/
		
		}
	}
