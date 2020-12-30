package cn.sunline.ltts.busi.gltran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Fah_balanceDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.fah_balance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
	 /**
	  * 总账账户过账
	  *
	  */

public class gl65DataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo, cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctNoInfo> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctNoInfo dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Property property) {
			
			faa_account acctTable = Faa_accountDao.selectOneWithLock_odb1(dataItem.getAcct_no(), true);
			
			//每天都更新上日余额
			RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
			String lastDate = runEnvs.getLstrdt();
			E_DEBITCREDIT balDirection = acctTable.getBal_direction();
			BigDecimal acctBal = acctTable.getAcct_bal();
			acctTable.setPrevious_bal_direction(balDirection);
			acctTable.setPrevious_acct_bal(acctBal);
			acctTable.setBal_update_date(lastDate);  //
			Faa_accountDao.updateOne_odb1(acctTable);
			
			String endDay = ApConstants.DEFAULT_MAX_DATE; // 拉链表最后的日期
			// 取出历史余额表
			fah_balance exsitBalaTable = FaAccountDao.selHistoryBalance(runEnvs.getCorpno(), dataItem.getAcct_no(), false);

			// 如果历史余额表中不存在该记录，新增
			if (exsitBalaTable == null) {

				fah_balance newBalaTable = SysUtil.getInstance(fah_balance.class);

				newBalaTable.setAcct_no(dataItem.getAcct_no()); 	// 账号
				newBalaTable.setStart_date(lastDate); 				// 起始日期
				newBalaTable.setEnd_date(endDay); 					// 截止日期
				newBalaTable.setBal_direction(balDirection); 		// 余额方向
				newBalaTable.setAcct_bal(acctBal); 					// 账户余额
				newBalaTable.setRecdver(1L);    					// 数据版本号

				Fah_balanceDao.insert(newBalaTable);
			}
			else {

				// 如果账户有变更，拉链处理
				if (CommUtil.compare(exsitBalaTable.getAcct_bal(), acctBal) != 0
						|| CommUtil.compare(exsitBalaTable.getBal_direction(), balDirection) != 0) {

					// 上上次交易日期 赋值给截止日期
					exsitBalaTable.setEnd_date(DateTimeUtil.dateAdd("day", lastDate, -1));

					// 更新数据库，废止该记录
					Fah_balanceDao.updateOne_odb1(exsitBalaTable);

					// 新增记录
					fah_balance newBalaTable = SysUtil.getInstance(fah_balance.class);

					newBalaTable.setAcct_no(dataItem.getAcct_no()); 	// 账号
					newBalaTable.setStart_date(lastDate); 				// 起始日期
					newBalaTable.setEnd_date(endDay); 					// 截止日期
					newBalaTable.setBal_direction(balDirection); 		// 余额方向
					newBalaTable.setAcct_bal(acctBal); 					// 账户余额
					newBalaTable.setRecdver(1L);    					// 数据版本号
					// 插入数据库
					Fah_balanceDao.insert(newBalaTable);

				}
			}
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Property property) {
			// 调用服务查询非虚拟机构类机构信息
			Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();
			return new ListBatchDataWalker<>(branchList);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctNoInfo> getJobBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl65.Property property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem) {
			Params para = new Params();
			para.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			para.add("acct_branch", dataItem.getBrchno());
			
			return new CursorBatchDataWalker<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctNoInfo>(FaAccountDao.namedsql_lstAcctnoListBybrch, para);
		}
	  

}


