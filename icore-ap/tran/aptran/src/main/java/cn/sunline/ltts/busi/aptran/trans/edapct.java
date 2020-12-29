package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrl;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrlAppl;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrlApplDao;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrlDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.trans.intf.Edapct.Input;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JYCHLIBZ;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SHXSZBZ;

public class edapct {
	
	/***
	 * 模式：录入、复核通过、复核拒绝、直通
	 * 数据操作类型：新增、修改、删除---相对于app_prop_ctrl表中数据
	 * 1.修改录入可修改新增录入数据和同批次修改录入
	 * 2.有删除申请后不可新增
	 * 3.新增、修改、删除需要调用审计
	 * @param input
	 */
	public static void edapct(
			final cn.sunline.ltts.busi.aptran.trans.intf.Edapct.Input input) {

		check(input);

		String trandt = CommToolsAplt.prcRunEnvs().getTrandt(); // 交易日期
		String transq = CommToolsAplt.prcRunEnvs().getTransq(); // 交易流水

		// 增加数据
		if (input.getDatatp().equals(E_SHXSZBZ.A)) {	
		// 增加录入模式 
		if (input.getOptype().equals(E_JYCHLIBZ.LR)) {
				
			for(int lr=0;lr<input.getProdpp().size();lr++){

				AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
						input.getProdcd(), input.getProdpp().get(lr)
								.getTablcd(), input.getProdpp().get(lr)
								.getFildcd(), false);

				if (CommUtil.isNotNull(appPropCtrl)) {

					throw ApError.Aplt.E0000("第" + (lr + 1) + "条数据产品属性已存在");
				}
					
				AppPropCtrlAppl appPropCtrlAppl2 = AppPropCtrlApplDao
						.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
								input.getProdcd(), input.getProdpp().get(lr)
										.getTablcd(), input.getProdpp()
										.get(lr).getFildcd(), false);
				
				if (CommUtil.isNotNull(appPropCtrlAppl2)) {

					throw ApError.Aplt.E0000("第" + (lr + 1) + "条数据产品属性同批次存在删除申请，请换批次增加录入");
					
				}		
				
				
				AppPropCtrlAppl appPropCtrlAppl = AppPropCtrlApplDao
						.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.A,
								input.getProdcd(), input.getProdpp().get(lr)
										.getTablcd(), input.getProdpp()
										.get(lr).getFildcd(), false);

				if (CommUtil.isNotNull(appPropCtrlAppl)) {
					if (appPropCtrlAppl.getOptype().equals(E_JYCHLIBZ.LR)) {

						throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性已存在申请记录");

					} else {// 审核过

						throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性申请记录已复核处理");

					}

				}
				
				AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);	
				appl.setCorpno(CommTools.getTranCorpno());			
				appl.setBtchno(input.getBtchno());
				appl.setProdcd(input.getProdcd());
				appl.setDatatp(E_SHXSZBZ.A);
				appl.setOptype(input.getOptype());
				appl.setTablcd(input.getProdpp().get(lr).getTablcd());//属性表
				appl.setFildcd(input.getProdpp().get(lr).getFildcd());//属性代码
				appl.setFildnm(input.getProdpp().get(lr).getFildnm());//属性名称
				appl.setFildtp(input.getProdpp().get(lr).getFildtp());//属性类型
				appl.setFinlfg(input.getProdpp().get(lr).getFinlfg());//是否固定值
				appl.setNullfg(input.getProdpp().get(lr).getNullfg());//空值是否覆盖
				appl.setFildvl(input.getProdpp().get(lr).getFildvl());//属性默认值
				appl.setFildmu(input.getProdpp().get(lr).getFildmu());//是否多值限制
				appl.setFildcv(input.getProdpp().get(lr).getFildcv());//属性限制值
				appl.setFildtx(input.getProdpp().get(lr).getFildtx());//属性描述
				appl.setNullab(input.getProdpp().get(lr).getNullab());//是否可控
				appl.setEnmuid(input.getProdpp().get(lr).getEnmuid());//枚举FULLID
				appl.setTempcd(input.getProdpp().get(lr).getTempcd());//产品模板代码
				appl.setTrandt(trandt);
				appl.setTransq(transq);
				appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
				appl.setUserid(input.getChckus());
				AppPropCtrlApplDao.insert(appl);
				
				}
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//增加审核通过
				for(int tg=0;tg<input.getProdpp().size();tg++){
					
					AppPropCtrlAppl appPropAppl= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.A,
									input.getProdcd(), input.getProdpp().get(tg)
											.getTablcd(), input.getProdpp()
											.get(tg).getFildcd(), false);
					
					if(CommUtil.isNotNull(appPropAppl)){
						
						if(appPropAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							
							if(input.getChckus().equals(appPropAppl.getUserid())){								
								throw ApError.Aplt.E0000("录入审核不能为同一柜员");
							}
													
							appPropAppl.setChckdt(trandt);
							appPropAppl.setChcksq(transq);
							appPropAppl.setChckus(input.getChckus());				
							appPropAppl.setFildnm(input.getProdpp().get(tg).getFildnm());
							appPropAppl.setFildtp(input.getProdpp().get(tg).getFildtp());
							appPropAppl.setFinlfg(input.getProdpp().get(tg).getFinlfg());
							appPropAppl.setNullfg(input.getProdpp().get(tg).getNullfg());
							appPropAppl.setFildvl(input.getProdpp().get(tg).getFildvl());
							appPropAppl.setFildmu(input.getProdpp().get(tg).getFildmu());
							appPropAppl.setFildcv(input.getProdpp().get(tg).getFildcv());
							appPropAppl.setFildtx(input.getProdpp().get(tg).getFildtx());
							appPropAppl.setNullab(input.getProdpp().get(tg).getNullab());
							appPropAppl.setEnmuid(input.getProdpp().get(tg).getEnmuid());
							appPropAppl.setTempcd(input.getProdpp().get(tg).getTempcd());
							appPropAppl.setOptype(input.getOptype());
							
							AppPropCtrlApplDao.updateOne_odb1(appPropAppl);
								
							AppPropCtrl appPropCtrl=SysUtil.getInstance(AppPropCtrl.class);
							appPropCtrl.setCorpno(CommTools.getTranCorpno());
							appPropCtrl.setEnmuid(input.getProdpp().get(tg).getEnmuid());
							appPropCtrl.setFildcd(input.getProdpp().get(tg).getFildcd());
							appPropCtrl.setFildcv(input.getProdpp().get(tg).getFildcv());
							appPropCtrl.setFildmu(input.getProdpp().get(tg).getFildmu());
							appPropCtrl.setFildnm(input.getProdpp().get(tg).getFildnm());
							appPropCtrl.setFildtp(input.getProdpp().get(tg).getFildtp());
							appPropCtrl.setFildtx(input.getProdpp().get(tg).getFildtx());
							appPropCtrl.setFildvl(input.getProdpp().get(tg).getFildvl());
							appPropCtrl.setFinlfg(input.getProdpp().get(tg).getFinlfg());
							appPropCtrl.setNullab(input.getProdpp().get(tg).getNullab());
							appPropCtrl.setNullfg(input.getProdpp().get(tg).getNullfg());
							appPropCtrl.setProdcd(input.getProdcd());
							appPropCtrl.setTablcd(input.getProdpp().get(tg).getTablcd());
							appPropCtrl.setTempcd(input.getProdpp().get(tg).getTempcd());
							appPropCtrl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
							
							AppPropCtrlDao.insert(appPropCtrl);
							
							//审计增加参数
							//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
							ApDataAudit.regLogOnInsertParameter(appPropCtrl);
																	
						}else{
							
							throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据同批次产品属性申请记录已复核");
						}
						
					}else{
					
						throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据同批次产品属性无申请记录");
						
					}
				}		
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//增加审核拒绝
				
				for(int jj=0;jj<input.getProdpp().size();jj++){
					
					AppPropCtrlAppl appPropAppl= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.A,
									input.getProdcd(), input.getProdpp().get(jj)
											.getTablcd(), input.getProdpp()
											.get(jj).getFildcd(), false);
					
					if(CommUtil.isNotNull(appPropAppl)){
						
						if(appPropAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							
							if(input.getChckus().equals(appPropAppl.getUserid())){								
								throw ApError.Aplt.E0000("录入审核不能为同一柜员");
							}
																		
							appPropAppl.setChckdt(trandt);
							appPropAppl.setChcksq(transq);
							appPropAppl.setChckus(input.getChckus());				
							appPropAppl.setFildnm(input.getProdpp().get(jj).getFildnm());
							appPropAppl.setFildtp(input.getProdpp().get(jj).getFildtp());
							appPropAppl.setFinlfg(input.getProdpp().get(jj).getFinlfg());
							appPropAppl.setNullfg(input.getProdpp().get(jj).getNullfg());
							appPropAppl.setFildvl(input.getProdpp().get(jj).getFildvl());
							appPropAppl.setFildmu(input.getProdpp().get(jj).getFildmu());
							appPropAppl.setFildcv(input.getProdpp().get(jj).getFildcv());
							appPropAppl.setFildtx(input.getProdpp().get(jj).getFildtx());
							appPropAppl.setNullab(input.getProdpp().get(jj).getNullab());
							appPropAppl.setEnmuid(input.getProdpp().get(jj).getEnmuid());
							appPropAppl.setTempcd(input.getProdpp().get(jj).getTempcd());
							appPropAppl.setOptype(input.getOptype());
							
							AppPropCtrlApplDao.updateOne_odb1(appPropAppl);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据同批次产品属性申请记录已复核");
							
						}
						
						
					}else{
						
						throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据同批次产品属性无申请记录");
						
					}
				}
						
			}else{//增加审核直通
				
				for(int zt=0;zt<input.getProdpp().size();zt++){
								
					AppPropCtrlAppl appPropCtrlAppl2 = AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
									input.getProdcd(), input.getProdpp().get(zt)
											.getTablcd(), input.getProdpp()
											.get(zt).getFildcd(), false);
					
					if (CommUtil.isNotNull(appPropCtrlAppl2)) {

						throw ApError.Aplt.E0000("第" + (zt + 1) + "条数据产品属性同批次存在删除申请，请换批次增加");
						
					}	
					
					AppPropCtrl appCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(zt)
									.getTablcd(), input.getProdpp().get(zt)
									.getFildcd(), false);
					
					if(CommUtil.isNotNull(appCtrl)){
						
						throw ApError.Aplt.E0000("第" + (zt + 1) + "条数据产品控制属性已存在");
						
					}
					
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.A,
									input.getProdcd(), input.getProdpp().get(zt)
											.getTablcd(), input.getProdpp()
											.get(zt).getFildcd(), false);
					
					if(CommUtil.isNotNull(appProp)){
					
						throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据同批次产品属性已存在申请记录");
						
					}else{
						AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);						
						appl.setCorpno(CommTools.getTranCorpno());			
						appl.setBtchno(input.getBtchno());
						appl.setProdcd(input.getProdcd());
						appl.setDatatp(input.getDatatp());
						appl.setOptype(input.getOptype());
						appl.setTablcd(input.getProdpp().get(zt).getTablcd());//属性表
						appl.setFildcd(input.getProdpp().get(zt).getFildcd());//属性代码
						appl.setFildnm(input.getProdpp().get(zt).getFildnm());//属性名称
						appl.setFildtp(input.getProdpp().get(zt).getFildtp());//属性类型
						appl.setFinlfg(input.getProdpp().get(zt).getFinlfg());//是否固定值
						appl.setNullfg(input.getProdpp().get(zt).getNullfg());//空值是否覆盖
						appl.setFildvl(input.getProdpp().get(zt).getFildvl());//属性默认值
						appl.setFildmu(input.getProdpp().get(zt).getFildmu());//是否多值限制
						appl.setFildcv(input.getProdpp().get(zt).getFildcv());//属性限制值
						appl.setFildtx(input.getProdpp().get(zt).getFildtx());//属性描述
						appl.setNullab(input.getProdpp().get(zt).getNullab());//是否可控
						appl.setEnmuid(input.getProdpp().get(zt).getEnmuid());//枚举FULLID
						appl.setTempcd(input.getProdpp().get(zt).getTempcd());//产品模板代码
						appl.setTrandt(trandt);
						appl.setTransq(transq);
						appl.setUserid(input.getChckus());
						appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						appl.setChckdt(trandt);
						appl.setChcksq(transq);
						appl.setChckus(input.getChckus());
						AppPropCtrlApplDao.insert(appl);
						
						AppPropCtrl appPropCtrl=SysUtil.getInstance(AppPropCtrl.class);
						appPropCtrl.setCorpno(CommTools.getTranCorpno());
						appPropCtrl.setEnmuid(input.getProdpp().get(zt).getEnmuid());
						appPropCtrl.setFildcd(input.getProdpp().get(zt).getFildcd());
						appPropCtrl.setFildcv(input.getProdpp().get(zt).getFildcv());
						appPropCtrl.setFildmu(input.getProdpp().get(zt).getFildmu());
						appPropCtrl.setFildnm(input.getProdpp().get(zt).getFildnm());
						appPropCtrl.setFildtp(input.getProdpp().get(zt).getFildtp());
						appPropCtrl.setFildtx(input.getProdpp().get(zt).getFildtx());
						appPropCtrl.setFildvl(input.getProdpp().get(zt).getFildvl());
						appPropCtrl.setFinlfg(input.getProdpp().get(zt).getFinlfg());
						appPropCtrl.setNullab(input.getProdpp().get(zt).getNullab());
						appPropCtrl.setNullfg(input.getProdpp().get(zt).getNullfg());
						appPropCtrl.setProdcd(input.getProdcd());
						appPropCtrl.setTablcd(input.getProdpp().get(zt).getTablcd());
						appPropCtrl.setTempcd(input.getProdpp().get(zt).getTempcd());
						appPropCtrl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						
						AppPropCtrlDao.insert(appPropCtrl);
						
						//审计增加参数
						//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
						ApDataAudit.regLogOnInsertParameter(appPropCtrl);
					}
									
				}
			}
		
		} else if (input.getDatatp().equals(E_SHXSZBZ.M)) {
			
		if(input.getOptype().equals(E_JYCHLIBZ.LR)){// 修改录入--可修改新增录入的,新增类型不变
				
			for (int lr = 0; lr < input.getProdpp().size(); lr++) {
				
				//修改原始数据
				AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
						input.getProdcd(), input.getProdpp().get(lr)
								.getTablcd(), input.getProdpp().get(lr)
								.getFildcd(), false);
				
				//修改-新增数据
				AppPropCtrlAppl appProp= AppPropCtrlApplDao
						.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.A,
								input.getProdcd(), input.getProdpp().get(lr)
										.getTablcd(), input.getProdpp()
										.get(lr).getFildcd(), false);
			
				if(CommUtil.isNull(appPropCtrl)){
					
					if(CommUtil.isNull(appProp)){
						
						throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性无申请记录可修改");
						
					}else{
						
						if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
							
							//appProp.setDatatp(E_SHXSZBZ.M);//新增类型不变
							appProp.setFildnm(input.getProdpp().get(lr).getFildnm());//属性名称
							appProp.setFildtp(input.getProdpp().get(lr).getFildtp());//属性类型
							appProp.setFinlfg(input.getProdpp().get(lr).getFinlfg());//是否固定值
							appProp.setNullfg(input.getProdpp().get(lr).getNullfg());//空值是否覆盖
							appProp.setFildvl(input.getProdpp().get(lr).getFildvl());//属性默认值
							appProp.setFildmu(input.getProdpp().get(lr).getFildmu());//是否多值限制
							appProp.setFildcv(input.getProdpp().get(lr).getFildcv());//属性限制值
							appProp.setFildtx(input.getProdpp().get(lr).getFildtx());//属性描述
							appProp.setNullab(input.getProdpp().get(lr).getNullab());//是否可控
							appProp.setEnmuid(input.getProdpp().get(lr).getEnmuid());//枚举FULLID
							appProp.setTempcd(input.getProdpp().get(lr).getTempcd());//产品模板代码
							appProp.setTrandt(trandt);
							appProp.setTransq(transq);
							appProp.setUserid(input.getChckus());
							
							AppPropCtrlApplDao.updateOne_odb1(appProp);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性不存在并且无新增的申请记录可修改");
							
						}
						
					}
					
					
				}else{
					//属性表存在-有修改申请
					AppPropCtrlAppl appPropLr= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.M,
									input.getProdcd(), input.getProdpp().get(lr)
											.getTablcd(), input.getProdpp()
											.get(lr).getFildcd(), false);
					//原始有数据--申请无数据					
					if(CommUtil.isNull(appPropLr)){
						
						AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);						
						appl.setCorpno(CommTools.getTranCorpno());			
						appl.setBtchno(input.getBtchno());
						appl.setProdcd(input.getProdcd());
						appl.setDatatp(input.getDatatp());
						appl.setOptype(input.getOptype());
						appl.setTablcd(input.getProdpp().get(lr).getTablcd());//属性表
						appl.setFildcd(input.getProdpp().get(lr).getFildcd());//属性代码
						appl.setFildnm(input.getProdpp().get(lr).getFildnm());//属性名称
						appl.setFildtp(input.getProdpp().get(lr).getFildtp());//属性类型
						appl.setFinlfg(input.getProdpp().get(lr).getFinlfg());//是否固定值
						appl.setNullfg(input.getProdpp().get(lr).getNullfg());//空值是否覆盖
						appl.setFildvl(input.getProdpp().get(lr).getFildvl());//属性默认值
						appl.setFildmu(input.getProdpp().get(lr).getFildmu());//是否多值限制
						appl.setFildcv(input.getProdpp().get(lr).getFildcv());//属性限制值
						appl.setFildtx(input.getProdpp().get(lr).getFildtx());//属性描述
						appl.setNullab(input.getProdpp().get(lr).getNullab());//是否可控
						appl.setEnmuid(input.getProdpp().get(lr).getEnmuid());//枚举FULLID
						appl.setTempcd(input.getProdpp().get(lr).getTempcd());//产品模板代码
						appl.setTrandt(trandt);
						appl.setTransq(transq);
						appl.setUserid(input.getChckus());
						appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						AppPropCtrlApplDao.insert(appl);
						
						//throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性无申请记录可修改");
						
					}else{
						
						if(appPropLr.getOptype().equals(E_JYCHLIBZ.LR)){
							
							appPropLr.setFildnm(input.getProdpp().get(lr).getFildnm());//属性名称
							appPropLr.setFildtp(input.getProdpp().get(lr).getFildtp());//属性类型
							appPropLr.setFinlfg(input.getProdpp().get(lr).getFinlfg());//是否固定值
							appPropLr.setNullfg(input.getProdpp().get(lr).getNullfg());//空值是否覆盖
							appPropLr.setFildvl(input.getProdpp().get(lr).getFildvl());//属性默认值
							appPropLr.setFildmu(input.getProdpp().get(lr).getFildmu());//是否多值限制
							appPropLr.setFildcv(input.getProdpp().get(lr).getFildcv());//属性限制值
							appPropLr.setFildtx(input.getProdpp().get(lr).getFildtx());//属性描述
							appPropLr.setNullab(input.getProdpp().get(lr).getNullab());//是否可控
							appPropLr.setEnmuid(input.getProdpp().get(lr).getEnmuid());//枚举FULLID
							appPropLr.setTempcd(input.getProdpp().get(lr).getTempcd());//产品模板代码
							appPropLr.setTrandt(trandt);
							appPropLr.setTransq(transq);
							appPropLr.setUserid(input.getChckus());
							AppPropCtrlApplDao.updateOne_odb1(appPropLr);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据同批次产品属性修改申请记录已复核");
							
						}					
						
					}
					
				}
		
			}
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//修改通过
				
				for (int tg = 0; tg < input.getProdpp().size(); tg++) {
										
					AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(tg)
									.getTablcd(), input.getProdpp().get(tg)
									.getFildcd(), false);
									
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.M,
									input.getProdcd(), input.getProdpp().get(tg)
											.getTablcd(), input.getProdpp()
											.get(tg).getFildcd(), false);
				
					if(CommUtil.isNull(appProp)){
						
						throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据同批次产品属性无修改申请记录可复核");
						
					}else{
						
						if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
														
							if(input.getChckus().equals(appProp.getUserid())){								
								throw ApError.Aplt.E0000("录入审核不能为同一柜员");
							}
			
							appProp.setOptype(input.getOptype());
							appProp.setChckdt(trandt);
							appProp.setChcksq(transq);
							appProp.setChckus(input.getChckus());
							appProp.setFildnm(input.getProdpp().get(tg).getFildnm());//属性名称
							appProp.setFildtp(input.getProdpp().get(tg).getFildtp());//属性类型
							appProp.setFinlfg(input.getProdpp().get(tg).getFinlfg());//是否固定值
							appProp.setNullfg(input.getProdpp().get(tg).getNullfg());//空值是否覆盖
							appProp.setFildvl(input.getProdpp().get(tg).getFildvl());//属性默认值
							appProp.setFildmu(input.getProdpp().get(tg).getFildmu());//是否多值限制
							appProp.setFildcv(input.getProdpp().get(tg).getFildcv());//属性限制值
							appProp.setFildtx(input.getProdpp().get(tg).getFildtx());//属性描述
							appProp.setNullab(input.getProdpp().get(tg).getNullab());//是否可控
							appProp.setEnmuid(input.getProdpp().get(tg).getEnmuid());//枚举FULLID
							appProp.setTempcd(input.getProdpp().get(tg).getTempcd());//产品模板代码
							
							AppPropCtrlApplDao.updateOne_odb1(appProp);
							
							AppPropCtrl appPropCtrlOld=SysUtil.getInstance(AppPropCtrl.class);
							CommUtil.copyProperties(appPropCtrlOld, appPropCtrl);
							
							appPropCtrl.setEnmuid(input.getProdpp().get(tg).getEnmuid());							
							appPropCtrl.setFildcv(input.getProdpp().get(tg).getFildcv());
							appPropCtrl.setFildmu(input.getProdpp().get(tg).getFildmu());
							appPropCtrl.setFildnm(input.getProdpp().get(tg).getFildnm());
							appPropCtrl.setFildtp(input.getProdpp().get(tg).getFildtp());
							appPropCtrl.setFildtx(input.getProdpp().get(tg).getFildtx());
							appPropCtrl.setFildvl(input.getProdpp().get(tg).getFildvl());
							appPropCtrl.setFinlfg(input.getProdpp().get(tg).getFinlfg());
							appPropCtrl.setNullab(input.getProdpp().get(tg).getNullab());
							appPropCtrl.setNullfg(input.getProdpp().get(tg).getNullfg());
							appPropCtrl.setTempcd(input.getProdpp().get(tg).getTempcd());
							
							AppPropCtrlDao.updateOne_odb1(appPropCtrl);
							//审计修改参数
							//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
							ApDataAudit.regLogOnUpdateParameter(appPropCtrlOld,appPropCtrl);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据同批次产品属性修改申请记录已复核");
							
						}
					}
									
				}
				
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//修改拒绝
				
				
				for (int jj = 0; jj < input.getProdpp().size(); jj++) {
										
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.M,
									input.getProdcd(), input.getProdpp().get(jj)
											.getTablcd(), input.getProdpp()
											.get(jj).getFildcd(), false);
				
					if(CommUtil.isNull(appProp)){
						
						throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据同批次产品属性修改申请记录已复核");
						
					}else{
						
						if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){//拒绝时原属性纪录不做更改
							
							if(input.getChckus().equals(appProp.getUserid())){								
								throw ApError.Aplt.E0000("录入审核不能为同一柜员");
							}
							
							appProp.setOptype(input.getOptype());
							appProp.setChckdt(trandt);
							appProp.setChcksq(transq);
							appProp.setChckus(input.getChckus());
							AppPropCtrlApplDao.updateOne_odb1(appProp);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据同批次产品属性修改申请记录已复核");
							
						}
					}
					
				}
								
			}else{//修改直通
				
				for (int zt = 0; zt < input.getProdpp().size(); zt++) {
				
					AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(zt)
									.getTablcd(), input.getProdpp().get(zt)
									.getFildcd(), false);
									
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.M,
									input.getProdcd(), input.getProdpp().get(zt)
											.getTablcd(), input.getProdpp()
											.get(zt).getFildcd(), false);
					
					if(CommUtil.isNull(appPropCtrl)){						
						throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据产品属不存在");						
					}
					if(CommUtil.isNull(appProp)){						
						AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);						
						appl.setCorpno(CommTools.getTranCorpno());			
						appl.setBtchno(input.getBtchno());
						appl.setProdcd(input.getProdcd());
						appl.setDatatp(input.getDatatp());
						appl.setOptype(input.getOptype());
						appl.setTablcd(input.getProdpp().get(zt).getTablcd());//属性表
						appl.setFildcd(input.getProdpp().get(zt).getFildcd());//属性代码
						appl.setFildnm(input.getProdpp().get(zt).getFildnm());//属性名称
						appl.setFildtp(input.getProdpp().get(zt).getFildtp());//属性类型
						appl.setFinlfg(input.getProdpp().get(zt).getFinlfg());//是否固定值
						appl.setNullfg(input.getProdpp().get(zt).getNullfg());//空值是否覆盖
						appl.setFildvl(input.getProdpp().get(zt).getFildvl());//属性默认值
						appl.setFildmu(input.getProdpp().get(zt).getFildmu());//是否多值限制
						appl.setFildcv(input.getProdpp().get(zt).getFildcv());//属性限制值
						appl.setFildtx(input.getProdpp().get(zt).getFildtx());//属性描述
						appl.setNullab(input.getProdpp().get(zt).getNullab());//是否可控
						appl.setEnmuid(input.getProdpp().get(zt).getEnmuid());//枚举FULLID
						appl.setTempcd(input.getProdpp().get(zt).getTempcd());//产品模板代码
						appl.setTrandt(trandt);
						appl.setTransq(transq);
						appl.setUserid(input.getChckus());
						appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						appl.setChckdt(trandt);
						appl.setChcksq(transq);
						appl.setChckus(input.getChckus());
						AppPropCtrlApplDao.insert(appl);
																		
					}else{
						if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
							throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据同批次产品属性存在修改申请录入记录请复核");
						}else{
							throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据同批次产品属性修改申请已复核");
						}
						
						/*if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
							
							appProp.setOptype(input.getOptype());
							appProp.setChckdt(trandt);
							appProp.setChcksq(transq);
							appProp.setChckus(input.getChckus());
							appProp.setFildnm(input.getProdpp().get(zt).getFildnm());//属性名称
							appProp.setFildtp(input.getProdpp().get(zt).getFildtp());//属性类型
							appProp.setFinlfg(input.getProdpp().get(zt).getFinlfg());//是否固定值
							appProp.setNullfg(input.getProdpp().get(zt).getNullfg());//空值是否覆盖
							appProp.setFildvl(input.getProdpp().get(zt).getFildvl());//属性默认值
							appProp.setFildmu(input.getProdpp().get(zt).getFildmu());//是否多值限制
							appProp.setFildcv(input.getProdpp().get(zt).getFildcv());//属性限制值
							appProp.setFildtx(input.getProdpp().get(zt).getFildtx());//属性描述
							appProp.setNullab(input.getProdpp().get(zt).getNullab());//是否可控
							appProp.setEnmuid(input.getProdpp().get(zt).getEnmuid());//枚举FULLID
							appProp.setTempcd(input.getProdpp().get(zt).getTempcd());//产品模板代码
							
							AppPropCtrlApplDao.updateOne_odb1(appProp);
							
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据同批次产品属性修改申请已复核");
							
						}*/
						
					}
														
						//历史数据
						AppPropCtrl appPropCtrlOld=SysUtil.getInstance(AppPropCtrl.class);
						CommUtil.copyProperties(appPropCtrlOld, appPropCtrl);
						
						appPropCtrl.setEnmuid(input.getProdpp().get(zt).getEnmuid());							
						appPropCtrl.setFildcv(input.getProdpp().get(zt).getFildcv());
						appPropCtrl.setFildmu(input.getProdpp().get(zt).getFildmu());
						appPropCtrl.setFildnm(input.getProdpp().get(zt).getFildnm());
						appPropCtrl.setFildtp(input.getProdpp().get(zt).getFildtp());
						appPropCtrl.setFildtx(input.getProdpp().get(zt).getFildtx());
						appPropCtrl.setFildvl(input.getProdpp().get(zt).getFildvl());
						appPropCtrl.setFinlfg(input.getProdpp().get(zt).getFinlfg());
						appPropCtrl.setNullab(input.getProdpp().get(zt).getNullab());
						appPropCtrl.setNullfg(input.getProdpp().get(zt).getNullfg());
						appPropCtrl.setTempcd(input.getProdpp().get(zt).getTempcd());
						
						AppPropCtrlDao.updateOne_odb1(appPropCtrl);
						
						//审计修改参数
						//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
						ApDataAudit.regLogOnUpdateParameter(appPropCtrlOld,appPropCtrl);
										
				}			
			}
										
		} else {// 删除数据

			if(input.getOptype().equals(E_JYCHLIBZ.LR)){//删除录入
				
			
				for (int lr = 0; lr < input.getProdpp().size(); lr++) {
					
					AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(lr)
									.getTablcd(), input.getProdpp().get(lr)
									.getFildcd(), false);
					
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
									input.getProdcd(), input.getProdpp().get(lr)
											.getTablcd(), input.getProdpp()
											.get(lr).getFildcd(), false);
					
					if(CommUtil.isNull(appPropCtrl)){
						
						throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据产品没有该属性");
						
					}
					
					if(CommUtil.isNull(appProp)){
						
						AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);						
						appl.setCorpno(CommTools.getTranCorpno());			
						appl.setBtchno(input.getBtchno());
						appl.setProdcd(input.getProdcd());
						appl.setDatatp(input.getDatatp());
						appl.setOptype(input.getOptype());
						appl.setTablcd(input.getProdpp().get(lr).getTablcd());//属性表
						appl.setFildcd(input.getProdpp().get(lr).getFildcd());//属性代码
						appl.setFildnm(input.getProdpp().get(lr).getFildnm());//属性名称
						appl.setFildtp(input.getProdpp().get(lr).getFildtp());//属性类型
						appl.setFinlfg(input.getProdpp().get(lr).getFinlfg());//是否固定值
						appl.setNullfg(input.getProdpp().get(lr).getNullfg());//空值是否覆盖
						appl.setFildvl(input.getProdpp().get(lr).getFildvl());//属性默认值
						appl.setFildmu(input.getProdpp().get(lr).getFildmu());//是否多值限制
						appl.setFildcv(input.getProdpp().get(lr).getFildcv());//属性限制值
						appl.setFildtx(input.getProdpp().get(lr).getFildtx());//属性描述
						appl.setNullab(input.getProdpp().get(lr).getNullab());//是否可控
						appl.setEnmuid(input.getProdpp().get(lr).getEnmuid());//枚举FULLID
						appl.setTempcd(input.getProdpp().get(lr).getTempcd());//产品模板代码
						appl.setTrandt(trandt);
						appl.setTransq(transq);
						appl.setUserid(input.getChckus());
						appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						AppPropCtrlApplDao.insert(appl);
											
					}else{
						
						throw ApError.Aplt.E0000("第" + (lr + 1)	+ "条数据产品属性删除申请已存在");
					}
					
				}
				
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//删除通过
				for (int tg = 0; tg < input.getProdpp().size(); tg++) {
									
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
									input.getProdcd(), input.getProdpp().get(tg)
											.getTablcd(), input.getProdpp()
											.get(tg).getFildcd(), false);
					
					AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(tg)
									.getTablcd(), input.getProdpp().get(tg)
									.getFildcd(), false);
					
					
				if(CommUtil.isNull(appProp)){
					
					throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据产品属性删除申请不存在");
					
				}else{
					
					if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
						
						if(input.getChckus().equals(appProp.getUserid())){								
							throw ApError.Aplt.E0000("录入审核不能为同一柜员");
						}
	
						appProp.setOptype(input.getOptype());
						appProp.setChckdt(trandt);
						appProp.setChcksq(transq);
						appProp.setChckus(input.getChckus());
						appProp.setFildnm(input.getProdpp().get(tg).getFildnm());//属性名称
						appProp.setFildtp(input.getProdpp().get(tg).getFildtp());//属性类型
						appProp.setFinlfg(input.getProdpp().get(tg).getFinlfg());//是否固定值
						appProp.setNullfg(input.getProdpp().get(tg).getNullfg());//空值是否覆盖
						appProp.setFildvl(input.getProdpp().get(tg).getFildvl());//属性默认值
						appProp.setFildmu(input.getProdpp().get(tg).getFildmu());//是否多值限制
						appProp.setFildcv(input.getProdpp().get(tg).getFildcv());//属性限制值
						appProp.setFildtx(input.getProdpp().get(tg).getFildtx());//属性描述
						appProp.setNullab(input.getProdpp().get(tg).getNullab());//是否可控
						appProp.setEnmuid(input.getProdpp().get(tg).getEnmuid());//枚举FULLID
						appProp.setTempcd(input.getProdpp().get(tg).getTempcd());//产品模板代码
						
						AppPropCtrlApplDao.updateOne_odb1(appProp);
						
						
						AppPropCtrlDao.deleteOne_odb1(input.getProdcd(), input.getProdpp().get(tg).getTablcd(), input.getProdpp().get(tg).getFildcd());
					
						//审计删除参数
						//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
						ApDataAudit.regLogOnDeleteParameter(appPropCtrl);
						
						
					}else{
						
						throw ApError.Aplt.E0000("第" + (tg + 1)	+ "条数据产品属性删除申请已复核");
						
					}
				
				}
				
				}
				
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//删除拒绝
				
				for (int jj = 0; jj < input.getProdpp().size(); jj++) {
					
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
									input.getProdcd(), input.getProdpp().get(jj)
											.getTablcd(), input.getProdpp()
											.get(jj).getFildcd(), false);
					
					if(CommUtil.isNull(appProp)){
						
						throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据产品没有该属性删除申请");
						
					}else{
						
						if(appProp.getOptype().equals(E_JYCHLIBZ.LR)){
							if(input.getChckus().equals(appProp.getUserid())){								
								throw ApError.Aplt.E0000("录入审核不能为同一柜员");
							}
							
							appProp.setOptype(input.getOptype());
							appProp.setChckdt(trandt);
							appProp.setChcksq(transq);
							appProp.setChckus(input.getChckus());
							appProp.setFildnm(input.getProdpp().get(jj).getFildnm());//属性名称
							appProp.setFildtp(input.getProdpp().get(jj).getFildtp());//属性类型
							appProp.setFinlfg(input.getProdpp().get(jj).getFinlfg());//是否固定值
							appProp.setNullfg(input.getProdpp().get(jj).getNullfg());//空值是否覆盖
							appProp.setFildvl(input.getProdpp().get(jj).getFildvl());//属性默认值
							appProp.setFildmu(input.getProdpp().get(jj).getFildmu());//是否多值限制
							appProp.setFildcv(input.getProdpp().get(jj).getFildcv());//属性限制值
							appProp.setFildtx(input.getProdpp().get(jj).getFildtx());//属性描述
							appProp.setNullab(input.getProdpp().get(jj).getNullab());//是否可控
							appProp.setEnmuid(input.getProdpp().get(jj).getEnmuid());//枚举FULLID
							appProp.setTempcd(input.getProdpp().get(jj).getTempcd());//产品模板代码
							
							AppPropCtrlApplDao.updateOne_odb1(appProp);
							
						}else{
							
							throw ApError.Aplt.E0000("第" + (jj + 1)	+ "条数据产品属性删除申请已复核");
							
						}
							
					}
							
				}
				
			}else{//删除直通
				
				for (int zt = 0; zt < input.getProdpp().size(); zt++) {
					
					AppPropCtrl appPropCtrl = AppPropCtrlDao.selectOne_odb1(
							input.getProdcd(), input.getProdpp().get(zt)
									.getTablcd(), input.getProdpp().get(zt)
									.getFildcd(), false);
					
					AppPropCtrlAppl appProp= AppPropCtrlApplDao
							.selectOne_odb1(input.getBtchno(), E_SHXSZBZ.D,
									input.getProdcd(), input.getProdpp().get(zt)
											.getTablcd(), input.getProdpp()
											.get(zt).getFildcd(), false);
					
					if(CommUtil.isNull(appPropCtrl)){
						
						throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据产品没有该属性");
						
					}else{
						
						if(CommUtil.isNull(appProp)){
							
							AppPropCtrlAppl appl=SysUtil.getInstance(AppPropCtrlAppl.class);						
							appl.setCorpno(CommTools.getTranCorpno());			
							appl.setBtchno(input.getBtchno());
							appl.setProdcd(input.getProdcd());
							appl.setDatatp(input.getDatatp());
							appl.setOptype(input.getOptype());
							appl.setTablcd(input.getProdpp().get(zt).getTablcd());//属性表
							appl.setFildcd(input.getProdpp().get(zt).getFildcd());//属性代码
							appl.setFildnm(input.getProdpp().get(zt).getFildnm());//属性名称
							appl.setFildtp(input.getProdpp().get(zt).getFildtp());//属性类型
							appl.setFinlfg(input.getProdpp().get(zt).getFinlfg());//是否固定值
							appl.setNullfg(input.getProdpp().get(zt).getNullfg());//空值是否覆盖
							appl.setFildvl(input.getProdpp().get(zt).getFildvl());//属性默认值
							appl.setFildmu(input.getProdpp().get(zt).getFildmu());//是否多值限制
							appl.setFildcv(input.getProdpp().get(zt).getFildcv());//属性限制值
							appl.setFildtx(input.getProdpp().get(zt).getFildtx());//属性描述
							appl.setNullab(input.getProdpp().get(zt).getNullab());//是否可控
							appl.setEnmuid(input.getProdpp().get(zt).getEnmuid());//枚举FULLID
							appl.setTempcd(input.getProdpp().get(zt).getTempcd());//产品模板代码
							appl.setTrandt(trandt);
							appl.setTransq(transq);
							appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
							appl.setChckdt(trandt);
							appl.setChcksq(transq);
							appl.setChckus(input.getChckus());
							AppPropCtrlApplDao.insert(appl);

							
							AppPropCtrlDao.deleteOne_odb1(input.getProdcd(), input.getProdpp().get(zt).getTablcd(), input.getProdpp().get(zt).getFildcd());
							
							//审计删除参数
							//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
							ApDataAudit.regLogOnDeleteParameter(appPropCtrl);
						}else{
							
								throw ApError.Aplt.E0000("第" + (zt + 1)	+ "条数据同批次产品属性已存在删除申请记录");
							
							}
						
					}
				}
			}		
			
		}
	}

	// 校验
	public static void check(Input input) {

		if (CommUtil.isNull(input.getBtchno())) {
			throw ApError.Aplt.E0000("批次号不能为空");
		}
		if (CommUtil.isNull(input.getProdcd())) {
			throw ApError.Aplt.E0000("产品代码不能为空");
		}
		if (CommUtil.isNull(input.getDatatp())) {
			throw ApError.Aplt.E0000("数据操作类型不能为空");
		}
		if (CommUtil.isNull(input.getOptype())) {
			throw ApError.Aplt.E0000("操作类型不能为空");
		}
		if (CommUtil.isNull(input.getChckus())) {
			throw ApError.Aplt.E0000("操作柜员不能为空");
		}
		if (CommUtil.isNull(input.getProdpp())) {
			throw ApError.Aplt.E0000("产品属性列表不能为空");
		}

		// seq规则列表序列
		for (int seq = 0; seq < input.getProdpp().size(); seq++) {

			if (CommUtil.isNull(input.getProdpp().get(seq).getTablcd())) {
				throw ApError.Aplt.E0000("第" + (seq + 1) + "条数据产品属性表名不能为空");
			}

			if (CommUtil.isNull(input.getProdpp().get(seq).getFildcd())) {
				throw ApError.Aplt.E0000("第" + (seq + 1) + "条数据产品属性代码不能为空");
			}
										
		}
	}
}
