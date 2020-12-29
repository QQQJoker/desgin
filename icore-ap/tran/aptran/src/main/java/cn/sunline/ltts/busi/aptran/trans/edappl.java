package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRule;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRuleAppl;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRuleApplDao;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRuleDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.trans.intf.Edappl.Input;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JYCHLIBZ;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SHXSZBZ;

public class edappl {
		
	/**
	 * 模式：录入、复核通过、复核拒绝、直通
	 * 数据操作类型：新增、修改、删除---相对于app_rule表中数据
	 * 1.修改录入可修改新增录入和同批次修改录入
	 * 2.有删除申请后不可新增
	 * 3.新增、修改、删除需要调用审计
	 * @param input
	 * @param output
	 */
	public static void edappl( 
			final cn.sunline.ltts.busi.aptran.trans.intf.Edappl.Input input){

		check(input);
		
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt(); // 交易日期
		String transq = CommToolsAplt.prcRunEnvs().getTransq(); // 交易流水
		
			//增加-数据操作
		if(input.getDatatp().equals(E_SHXSZBZ.A)){
			//录入模式
			if(input.getOptype().equals(E_JYCHLIBZ.LR)){
				
				for(int lr=0;lr<input.getAprule().size();lr++){
					
				//根据规则编号查询申请表
				AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), E_SHXSZBZ.A,input.getAprule().get(lr).getRulecd(), false);			
				//根据规则编号查询规则表
				AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(lr).getGrupcd(),  input.getAprule().get(lr).getRulecd(), false);
				//根据规则组内序号查询申请表
				AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(),E_SHXSZBZ.A, false);
				//根据规则组内序号查询规则表
				AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(), false);
				
				if(CommUtil.isNotNull(appRule)){
					
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则编号已存在，对应组内序号为"+input.getAprule().get(lr).getRulesq());
				}
			    if(CommUtil.isNotNull(appRuleBySeq)){
			    					
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"已存在，对应规则编号是"+appRuleBySeq.getRulecd());
				}
				if(CommUtil.isNotNull(appRuleAppl)){				
				
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则编号已存在申请记录，对应组内序号为"+input.getAprule().get(lr).getRulesq());								
				}
				if(CommUtil.isNotNull(appApplBySeq)){
					
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据同批次产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"已存在申请记录，对应规则编号是"+appApplBySeq.getRulecd());
				}
				
				AppRuleAppl appRuleAppl1=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), E_SHXSZBZ.D,input.getAprule().get(lr).getRulecd(), false);
				
				if(CommUtil.isNotNull(appRuleAppl1)){
					
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据同批次产品规则存在删除申请记录，请换批次增加录入");
					
				}
				
				AppRuleAppl appAppl=SysUtil.getInstance(AppRuleAppl.class);
				appAppl.setBtchno(input.getBtchno());
				appAppl.setContxt(input.getAprule().get(lr).getContxt());
				appAppl.setCorpno(CommTools.getTranCorpno());
				appAppl.setDatatp(input.getDatatp());
				appAppl.setOptype(input.getOptype());
				appAppl.setRulecd(input.getAprule().get(lr).getRulecd());
				appAppl.setTransq(transq);
				appAppl.setTrandt(trandt);
				appAppl.setGrupcd(input.getAprule().get(lr).getGrupcd());
				appAppl.setRuletx(input.getAprule().get(lr).getRuletx());
				appAppl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
				appAppl.setRulesq(input.getAprule().get(lr).getRulesq());
				appAppl.setUserid(input.getChckus());
				AppRuleApplDao.insert(appAppl);											
				}								
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//复核通过
				
			for(int tg=0;tg<input.getAprule().size();tg++){
				//申请表根据规则编号查询
				AppRuleAppl appRuleAppl1=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(tg).getGrupcd(), E_SHXSZBZ.A,input.getAprule().get(tg).getRulecd(), false);			
					
				if(CommUtil.isNull(appRuleAppl1)){
					
					throw ApError.Aplt.E0000("第"+(tg+1)+"条数据无规则申请记录需要复核");
					
				}else{
					if(!appRuleAppl1.getOptype().equals(E_JYCHLIBZ.LR)){
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据无此规则申请记录需要复核");
					}
						
				 if(!input.getAprule().get(tg).getRulesq().equals(appRuleAppl1.getRulesq())){
					 
					 throw ApError.Aplt.E0000("第"+(tg+1)+"条数据规则对应组内序号与现有对应序号"+appRuleAppl1.getRulesq()+"不一致");
					 
				 }
														
				if(appRuleAppl1.getUserid().equals(input.getChckus())){		
					throw ApError.Aplt.E0000("录入复核不能为同一柜员");
				}
				 //操作申请表  复核可修改规则
			     appRuleAppl1.setOptype(input.getOptype());//操作类型
				 appRuleAppl1.setContxt(input.getAprule().get(tg).getContxt());//规则			
				 appRuleAppl1.setRuletx(input.getAprule().get(tg).getRuletx());//规则说明			
				 appRuleAppl1.setChckdt(trandt);//复核日期
				 appRuleAppl1.setChckus(input.getChckus());//复核人
				 appRuleAppl1.setChcksq(transq);//复核流水号
				 
				 AppRuleApplDao.updateOne_odb1(appRuleAppl1);
				 
				 //操作规则表					 
				 AppRule appRule =SysUtil.getInstance(AppRule.class);
				 								 				 
				 appRule.setContxt(input.getAprule().get(tg).getContxt());
				 appRule.setGrupcd(input.getAprule().get(tg).getGrupcd());
				 appRule.setRulecd(input.getAprule().get(tg).getRulecd());
				 appRule.setRulesq(input.getAprule().get(tg).getRulesq());
				 appRule.setRuletx(input.getAprule().get(tg).getRuletx());
					 
				 AppRuleDao.insert(appRule);
					
				 //审计增加参数
				 //ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
				 ApDataAudit.regLogOnInsertParameter(appRule);			 
				 
				}
			}
											
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//复核拒绝
				
				for(int jj=0;jj<input.getAprule().size();jj++){
					
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(jj).getGrupcd(),E_SHXSZBZ.A, input.getAprule().get(jj).getRulecd(), false);			
					
					if(CommUtil.isNull(appRuleAppl)){
						
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据无规则申请记录");
						
					}else{
						
						if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据无此规则申请记录需要复核");
						}
						
						if(appRuleAppl.getUserid().equals(input.getChckus())){		
							throw ApError.Aplt.E0000("录入复核不能为同一柜员");
						}
									
						appRuleAppl.setChckdt(trandt);
						appRuleAppl.setChcksq(transq);
						appRuleAppl.setChckus(input.getChckus());					
						appRuleAppl.setOptype(input.getOptype());
						
						AppRuleApplDao.updateOne_odb1(appRuleAppl);
					}
					
				}
								
			}else{//直通模式
				for(int zt=0;zt<input.getAprule().size();zt++){	
					
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(zt).getGrupcd(), E_SHXSZBZ.A,input.getAprule().get(zt).getRulecd(), false);			
					//根据规则编号查询规则表
					AppRule appRule1=AppRuleDao.selectOne_odb2(input.getAprule().get(zt).getGrupcd(),  input.getAprule().get(zt).getRulecd(), false);
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(zt).getGrupcd(), input.getAprule().get(zt).getRulesq(),E_SHXSZBZ.A, false);
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(zt).getGrupcd(), input.getAprule().get(zt).getRulesq(), false);		
					
					AppRuleAppl appRuleAppl1=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(zt).getGrupcd(), E_SHXSZBZ.D,input.getAprule().get(zt).getRulecd(), false);
					
					if(CommUtil.isNotNull(appRuleAppl1)){
						
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据同批次产品规则存在删除申请记录，请换批次增加录入");
						
					}
					if(CommUtil.isNotNull(appRule1)){
						
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则编号已存在，对应组内序号为"+input.getAprule().get(zt).getRulesq());
					}
				    if(CommUtil.isNotNull(appRuleBySeq)){
				    					
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内序号"+input.getAprule().get(zt).getRulesq()+"已存在，对应规则编号是"+appRuleBySeq.getRulecd());
					}
					if(CommUtil.isNotNull(appRuleAppl)){				
					
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则编号已存在申请记录，对应组内序号为"+input.getAprule().get(zt).getRulesq());								
					}
					if(CommUtil.isNotNull(appApplBySeq)){
						
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据同批次产品规则组内序号"+input.getAprule().get(zt).getRulesq()+"已存在申请记录，对应规则编号是"+appApplBySeq.getRulecd());
					}
					
					
					 //规则表	
					 AppRule appRule =SysUtil.getInstance(AppRule.class);
										
					 appRule.setContxt(input.getAprule().get(zt).getContxt());
					 appRule.setGrupcd(input.getAprule().get(zt).getGrupcd());
					 appRule.setRulecd(input.getAprule().get(zt).getRulecd());
					 appRule.setRulesq(input.getAprule().get(zt).getRulesq());
					 appRule.setRuletx(input.getAprule().get(zt).getRuletx());
					 
					 AppRuleDao.insert(appRule);
					 
					 //审计增加参数
					 //ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
					 ApDataAudit.regLogOnInsertParameter(appRule);
					 
					 //申请表
					 AppRuleAppl appl=SysUtil.getInstance(AppRuleAppl.class);
					 appl.setBtchno(input.getBtchno());
					 appl.setCorpno(CommTools.getTranCorpno());
					 appl.setDatatp(E_SHXSZBZ.A);
					 appl.setGrupcd(input.getAprule().get(zt).getGrupcd());
					 appl.setOptype(E_JYCHLIBZ.ZT);
					 appl.setRulecd(input.getAprule().get(zt).getRulecd());
					 appl.setRuletx(input.getAprule().get(zt).getRuletx());
					 appl.setRulesq(input.getAprule().get(zt).getRulesq());
					 appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
					 appl.setChckdt(trandt);
					 appl.setChcksq(transq);
					 appl.setChckus(input.getChckus());
					 appl.setTrandt(trandt);
					 appl.setTransq(transq);
					 appl.setUserid(input.getChckus());
					 appl.setContxt(input.getAprule().get(zt).getContxt());
					 AppRuleApplDao.insert(appl);
				}													
			}
												
		}else if(input.getDatatp().equals(E_SHXSZBZ.M)){				
			//修改录入模式(规则表有数据或者申请表中有新增录入数据才可修改)
			if(input.getOptype().equals(E_JYCHLIBZ.LR)){
				
				for(int lr=0;lr<input.getAprule().size();lr++){
					//修改历史修改数据数据      
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl3=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(lr).getGrupcd(),E_SHXSZBZ.M, input.getAprule().get(lr).getRulecd(), false);			
					//根据规则编号查询规则表
					AppRule appRule3=AppRuleDao.selectOne_odb2(input.getAprule().get(lr).getGrupcd(),  input.getAprule().get(lr).getRulecd(), false);
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq1=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(),E_SHXSZBZ.M, false);
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq1=AppRuleDao.selectOne_odb1(input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(), false);
							
					// 修改新增的数据
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl4=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(lr).getGrupcd(),E_SHXSZBZ.A, input.getAprule().get(lr).getRulecd(), false);			
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq2=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(),E_SHXSZBZ.A, false);
				
					
					//规则表有数据新增修改记录
					if(CommUtil.isNotNull(appRule3)){
						
						if(!input.getAprule().get(lr).getRulesq().equals(appRule3.getRulesq())){
							throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内规则"+input.getAprule().get(lr).getRulecd()+"对应组内序号是"+appRule3.getRulesq());
						}
						
					}
					if(CommUtil.isNotNull(appRuleBySeq1)){
						
						if(!input.getAprule().get(lr).getRulecd().equals(appRuleBySeq1.getRulecd())){
							throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"对应规则编号是"+appRuleBySeq1.getRulecd());
						}
						
					}
										
					if(CommUtil.isNotNull(appApplBySeq1)){
						
						if(!input.getAprule().get(lr).getRulecd().equals(appApplBySeq1.getRulecd())){
							throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"对应规则编号是"+appApplBySeq1.getRulecd());
						}
						
					}
					if(CommUtil.isNotNull(appRuleAppl3)){
						
						if(!input.getAprule().get(lr).getRulesq().equals(appRuleAppl3.getRulesq())){
							throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内规则"+input.getAprule().get(lr).getRulecd()+"对应组内序号是"+appRuleAppl3.getRulesq());
						}
						
					}
						
																																																		
					if(CommUtil.isNull(appRuleAppl3) && CommUtil.isNull(appRule3) && CommUtil.isNull(appRuleAppl4)){
						
						throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则不存在或同批次没有申请记录");
						
					}
						
					if(CommUtil.isNull(appRuleAppl4)){
						if(CommUtil.isNull(appRuleAppl3)){						
							if(CommUtil.isNull(appRule3)){
								throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则不存在");
							}
							//新增申请表
							AppRuleAppl appRuleAppl=SysUtil.getInstance(AppRuleAppl.class);
							appRuleAppl.setBtchno(input.getBtchno());
							appRuleAppl.setContxt(input.getAprule().get(lr).getContxt());
							appRuleAppl.setCorpno(CommTools.getTranCorpno());
							appRuleAppl.setDatatp(input.getDatatp());
							appRuleAppl.setOptype(input.getOptype());
							appRuleAppl.setRulecd(input.getAprule().get(lr).getRulecd());
							appRuleAppl.setTransq(transq);
							appRuleAppl.setTrandt(trandt);
							appRuleAppl.setGrupcd(input.getAprule().get(lr).getGrupcd());
							appRuleAppl.setRuletx(input.getAprule().get(lr).getRuletx());
							appRuleAppl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
							appRuleAppl.setRulesq(input.getAprule().get(lr).getRulesq());
							appRuleAppl.setUserid(input.getChckus());
							AppRuleApplDao.insert(appRuleAppl);
															
						}else{
							
							//修改同批次 修改录入的数据
							if(appRuleAppl3.getOptype().equals(E_JYCHLIBZ.LR)){
														
								//更新申请表
								if(!input.getAprule().get(lr).getRulesq().equals(appRuleAppl3.getRulesq())){
									 
									 throw ApError.Aplt.E0000("第"+(lr+1)+"条数据规则对应组内序号与现有对应序号"+appRuleAppl3.getRulesq()+"不一致");
									 
								 }
			
								appRuleAppl3.setOptype(input.getOptype());//操作类型
								appRuleAppl3.setContxt(input.getAprule().get(lr).getContxt());//规则			
								appRuleAppl3.setRuletx(input.getAprule().get(lr).getRuletx());//规则说明			
								appRuleAppl3.setTrandt(trandt);//日期
								appRuleAppl3.setTransq(input.getChckus());
								appRuleAppl3.setUserid(input.getChckus());
								 
								AppRuleApplDao.updateOne_odb1(appRuleAppl3);
								
							}else{
								
								throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则修改申请记录已复核");
								
							}
							
						}							
					}else{
						if(!appRuleAppl4.getOptype().equals(E_JYCHLIBZ.LR)){
							
							if(CommUtil.isNull(appRuleAppl3)){
								
								if(CommUtil.isNull(appRule3)){
									throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则不存在");
								}
								
								//新增申请表
								AppRuleAppl appRuleAppl=SysUtil.getInstance(AppRuleAppl.class);
								appRuleAppl.setBtchno(input.getBtchno());
								appRuleAppl.setContxt(input.getAprule().get(lr).getContxt());
								appRuleAppl.setCorpno(CommTools.getTranCorpno());
								appRuleAppl.setDatatp(input.getDatatp());
								appRuleAppl.setOptype(input.getOptype());
								appRuleAppl.setRulecd(input.getAprule().get(lr).getRulecd());
								appRuleAppl.setTransq(transq);
								appRuleAppl.setTrandt(trandt);
								appRuleAppl.setUserid(input.getChckus());
								appRuleAppl.setGrupcd(input.getAprule().get(lr).getGrupcd());
								appRuleAppl.setRuletx(input.getAprule().get(lr).getRuletx());
								appRuleAppl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
								appRuleAppl.setRulesq(input.getAprule().get(lr).getRulesq());
								
								AppRuleApplDao.insert(appRuleAppl);
								
							}else{
								
								if(appRuleAppl3.getOptype().equals(E_JYCHLIBZ.LR)){
									
									appRuleAppl3.setOptype(input.getOptype());//操作类型
									appRuleAppl3.setContxt(input.getAprule().get(lr).getContxt());//规则			
									appRuleAppl3.setRuletx(input.getAprule().get(lr).getRuletx());//规则说明			
									appRuleAppl3.setTrandt(trandt);//日期
									appRuleAppl3.setTransq(input.getChckus());
									appRuleAppl3.setUserid(input.getChckus());
									 
									AppRuleApplDao.updateOne_odb1(appRuleAppl3);		
								}else{
									
									throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品修改规则存在已复核修改申请记录");
									
								}
								
							}
																			
						}else{
							
							appRuleAppl4.setContxt(input.getAprule().get(lr).getContxt());//规则			
							appRuleAppl4.setRuletx(input.getAprule().get(lr).getRuletx());//规则说明			
							appRuleAppl4.setTransq(transq);
							appRuleAppl4.setTrandt(trandt);
							appRuleAppl4.setUserid(input.getChckus());
							AppRuleApplDao.updateOne_odb1(appRuleAppl4);
							
						}																									
					}					
				}
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//修改复核通过
				
				for(int tg=0;tg<input.getAprule().size();tg++){
				//根据规则编号查询申请表
				AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(tg).getGrupcd(),E_SHXSZBZ.M, input.getAprule().get(tg).getRulecd(), false);			
				//根据规则组内序号查询申请表
				AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(tg).getGrupcd(), input.getAprule().get(tg).getRulesq(),E_SHXSZBZ.M, false);				
				//根据规则编号查询规则表
				AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(tg).getGrupcd(),  input.getAprule().get(tg).getRulecd(), false);			
									
				if(CommUtil.isNotNull(appApplBySeq)){
					
					if(!input.getAprule().get(tg).getRulecd().equals(appApplBySeq.getRulecd())){
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内序号"+input.getAprule().get(tg).getRulesq()+"对应规则编号是"+appApplBySeq.getRulecd());
					}
					
				}else{
					throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内序号未申请");
				}
				
				if(CommUtil.isNotNull(appRuleAppl)){
					
					if(!input.getAprule().get(tg).getRulecd().equals(appRuleAppl.getRulecd())){
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内序号"+input.getAprule().get(tg).getRulesq()+"对应规则编号是"+appRuleAppl.getRulecd());
					}else{
						if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则无需要复核的申请记录");
						}	
					}
					
				}else{
					throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则代码未申请");
				}
				
				if(appRuleAppl.getUserid().equals(input.getChckus())){
					throw ApError.Aplt.E0000("录入复核不能为同一柜员");
				}
				
				appRuleAppl.setChckdt(trandt);
				appRuleAppl.setChcksq(transq);
				appRuleAppl.setChckus(input.getChckus());
				appRuleAppl.setOptype(input.getOptype());
				appRuleAppl.setContxt(input.getAprule().get(tg).getContxt());
				appRuleAppl.setRuletx(input.getAprule().get(tg).getRuletx());
				
				AppRuleApplDao.updateOne_odb1(appRuleAppl);
				
				//规则表存在数据
				if(CommUtil.isNotNull(appRule)){
					
				 //历史数据放入审计
				 AppRule appRuleOld=SysUtil.getInstance(AppRule.class);
				 CommUtil.copyProperties(appRuleOld, appRule);
				 
				 appRule.setContxt(input.getAprule().get(tg).getContxt());
				 appRule.setRuletx(input.getAprule().get(tg).getRuletx());
				 AppRuleDao.updateOne_odb1(appRule)	;
				 
				 //审计修改参数
				 //ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
				 ApDataAudit.regLogOnUpdateParameter(appRuleOld,appRule);
				 	 
				}else{//规则表无，申请表有,修改新增录入数据不更改  增加  状态则不会出现先保留
				
					AppRule apprule=SysUtil.getInstance(AppRule.class);
					apprule.setContxt(input.getAprule().get(tg).getContxt());
					apprule.setRulecd(input.getAprule().get(tg).getRulecd());	
					apprule.setRulesq(input.getAprule().get(tg).getRulesq());
					apprule.setRuletx(input.getAprule().get(tg).getRuletx());
					
					AppRuleDao.insert(apprule);
				
					//审计增加参数
					//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
					ApDataAudit.regLogOnInsertParameter(appRule);
					
					}								
				}
												
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//修改复核拒绝
				
				for(int jj=0;jj<input.getAprule().size();jj++){
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(jj).getGrupcd(),E_SHXSZBZ.M, input.getAprule().get(jj).getRulecd(), false);			
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(jj).getGrupcd(), input.getAprule().get(jj).getRulesq(),E_SHXSZBZ.M, false);
					
					if(CommUtil.isNotNull(appRuleAppl)){
						
						if(!input.getAprule().get(jj).getRulecd().equals(appRuleAppl.getRulecd())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内序号"+input.getAprule().get(jj).getRulesq()+"对应规则编号是"+appRuleAppl.getRulecd());
						}else{
							if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
								throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则无需要复核的申请记录");
							}					
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则代码未申请");
					}
					
					if(CommUtil.isNotNull(appApplBySeq)){
						
						if(!input.getAprule().get(jj).getRulecd().equals(appApplBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内序号"+input.getAprule().get(jj).getRulesq()+"对应规则编号是"+appApplBySeq.getRulecd());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内序号未申请");
					}
					
					if(appRuleAppl.getUserid().equals(input.getChckus())){
						throw ApError.Aplt.E0000("录入复核不能为同一柜员");
					}
					
					appRuleAppl.setChckdt(trandt);
					appRuleAppl.setChcksq(transq);
					appRuleAppl.setChckus(input.getChckus());
					appRuleAppl.setOptype(input.getOptype());
					
					AppRuleApplDao.updateOne_odb1(appRuleAppl);
																	
					}
								
			}else{//修改直通模式
				
				for(int zt=0;zt<input.getAprule().size();zt++){
					
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(zt).getGrupcd(),E_SHXSZBZ.M, input.getAprule().get(zt).getRulecd(), false);			
					//根据规则编号查询规则表
					AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(zt).getGrupcd(),  input.getAprule().get(zt).getRulecd(), false);
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(zt).getGrupcd(), input.getAprule().get(zt).getRulesq(), false);
					
					if(CommUtil.isNotNull(appRule)){
						
						if(!input.getAprule().get(zt).getRulesq().equals(appRule.getRulesq())){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内规则"+input.getAprule().get(zt).getRulecd()+"对应组内序号是"+appRule.getRulesq());
						}
						
					}else{
						
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则不存在");
						
					}
					
					if(CommUtil.isNotNull(appRuleBySeq)){
						
						if(!input.getAprule().get(zt).getRulecd().equals(appRuleBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内序号"+input.getAprule().get(zt).getRulesq()+"对应规则编号是"+appRuleBySeq.getRulecd());
						}
						
					}else{
						
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内序号不存在");
						
					}
					
								
					if(CommUtil.isNotNull(appRuleAppl)){
						
					//	if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则已存在申请记录");
					//	}	
						
						//appRuleAppl.setChckdt(trandt);
						//appRuleAppl.setChcksq(transq);
						//appRuleAppl.setChckus(input.getChckus());
						//appRuleAppl.setContxt(input.getAprule().get(zt).getContxt());
						//appRuleAppl.setRuletx(input.getAprule().get(zt).getRuletx());
						//appRuleAppl.setOptype(input.getOptype());
						//AppRuleApplDao.updateOne_odb1(appRuleAppl);
						
					}else{
						
						 AppRuleAppl appl=SysUtil.getInstance(AppRuleAppl.class);
						 appl.setContxt(input.getAprule().get(zt).getContxt());
						 appl.setBtchno(input.getBtchno());
						 appl.setCorpno(CommTools.getTranCorpno());
						 appl.setDatatp(E_SHXSZBZ.M);
						 appl.setGrupcd(input.getAprule().get(zt).getGrupcd());
						 appl.setOptype(E_JYCHLIBZ.ZT);
						 appl.setRulecd(input.getAprule().get(zt).getRulecd());
						 appl.setRuletx(input.getAprule().get(zt).getRuletx());
						 appl.setRulesq(input.getAprule().get(zt).getRulesq());
						 appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
						 appl.setChckdt(trandt);
						 appl.setChcksq(transq);
						 appl.setUserid(input.getChckus());
						 appl.setChckus(input.getChckus());
						 appl.setTrandt(trandt);
						 appl.setTransq(transq);
						 AppRuleApplDao.insert(appl);
						 
						 //历史数据放入审计
						 AppRule appRuleOld=SysUtil.getInstance(AppRule.class);
						 CommUtil.copyProperties(appRuleOld, appRule);	
						 						
						 appRule.setContxt(input.getAprule().get(zt).getContxt());
						 appRule.setRuletx(input.getAprule().get(zt).getRuletx());
						
						 AppRuleDao.updateOne_odb1(appRule);
			 
						 //审计修改参数
						 //ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
						 ApDataAudit.regLogOnUpdateParameter(appRuleOld,appRule);
					}
				}
			}
						
		}else{
			//删除录入模式
			if(input.getOptype().equals(E_JYCHLIBZ.LR)){
				
				for(int lr=0;lr<input.getAprule().size();lr++){
					
				//根据规则编号查询申请表
				AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(lr).getGrupcd(),E_SHXSZBZ.D, input.getAprule().get(lr).getRulecd(), false);			
				//根据规则编号查询规则表
				AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(lr).getGrupcd(),  input.getAprule().get(lr).getRulecd(), false);
				//根据规则组内序号查询申请表
				AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(),E_SHXSZBZ.D, false);
				//根据规则组内序号查询规则表
				AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(lr).getGrupcd(), input.getAprule().get(lr).getRulesq(), false);
				
				
				if(CommUtil.isNotNull(appRule)){
					
					if(!input.getAprule().get(lr).getRulesq().equals(appRule.getRulesq())){
						throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内规则"+input.getAprule().get(lr).getRulecd()+"对应组内序号是"+appRule.getRulesq());
					}
					
				}else{
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则不存在");
				}
				
				if(CommUtil.isNotNull(appRuleBySeq)){
					
					if(!input.getAprule().get(lr).getRulecd().equals(appRuleBySeq.getRulecd())){
						throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"对应规则编号是"+appRuleBySeq.getRulecd());
					}
					
				}else{
					throw ApError.Aplt.E0000("第"+(lr+1)+"条数据组内序号对应的产品规则不存在");
				}
							
				if(CommUtil.isNotNull(appApplBySeq)){
					
					if(!input.getAprule().get(lr).getRulecd().equals(appApplBySeq.getRulecd())){
						throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则组内序号"+input.getAprule().get(lr).getRulesq()+"对应规则编号是"+appApplBySeq.getRulecd());
					}
					
				}
				if(CommUtil.isNotNull(appRuleAppl)){
					
				//	if(!input.getAprule().get(lr).getRulesq().equals(appRuleAppl.getRulesq())){
						throw ApError.Aplt.E0000("第"+(lr+1)+"条数据产品规则存在删除记录");
				//	}else{
						
				//		appRuleAppl.setOptype(input.getOptype());
				//		appRuleAppl.setTrandt(trandt);
				//		appRuleAppl.setTransq(transq);
				//		appRuleAppl.setUserid(input.getChckus());
				//		AppRuleApplDao.updateOne_odb1(appRuleAppl);							
					//}												
				}else{									
					 AppRuleAppl appl=SysUtil.getInstance(AppRuleAppl.class);
					 appl.setContxt(input.getAprule().get(lr).getContxt());
					 appl.setBtchno(input.getBtchno());
					 appl.setCorpno(CommTools.getTranCorpno());
					 appl.setDatatp(E_SHXSZBZ.D);
					 appl.setGrupcd(input.getAprule().get(lr).getGrupcd());
					 appl.setOptype(E_JYCHLIBZ.LR);
					 appl.setRulecd(input.getAprule().get(lr).getRulecd());
					 appl.setRuletx(input.getAprule().get(lr).getRuletx());
					 appl.setRulesq(input.getAprule().get(lr).getRulesq());
					 appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());						
					 appl.setTrandt(trandt);
					 appl.setTransq(transq);
					 appl.setUserid(input.getChckus());
					 AppRuleApplDao.insert(appl);
					}
																						
				}
			}else if(input.getOptype().equals(E_JYCHLIBZ.TG)){//删除复核通过
				
				for(int tg=0;tg<input.getAprule().size();tg++){
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(tg).getGrupcd(),E_SHXSZBZ.D, input.getAprule().get(tg).getRulecd(), false);			
					//根据规则编号查询规则表
					AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(tg).getGrupcd(),  input.getAprule().get(tg).getRulecd(), false);
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(tg).getGrupcd(), input.getAprule().get(tg).getRulesq(),E_SHXSZBZ.D, false);
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(tg).getGrupcd(), input.getAprule().get(tg).getRulesq(), false);
					
					
					if(CommUtil.isNotNull(appRule)){
						
						if(!input.getAprule().get(tg).getRulesq().equals(appRule.getRulesq())){
							throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内规则"+input.getAprule().get(tg).getRulecd()+"对应组内序号是"+appRule.getRulesq());
						}						
						
					}else{
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则不存在");
					}
					
					if(CommUtil.isNotNull(appRuleBySeq)){
						
						if(!input.getAprule().get(tg).getRulecd().equals(appRuleBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内序号"+input.getAprule().get(tg).getRulesq()+"对应规则编号是"+appRuleBySeq.getRulecd());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据组内序号对应的产品规则不存在");
					}
								
					if(CommUtil.isNotNull(appApplBySeq)){
						
						if(!input.getAprule().get(tg).getRulecd().equals(appApplBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内序号"+input.getAprule().get(tg).getRulesq()+"对应规则编号是"+appApplBySeq.getRulecd());
						}
						
					}
					if(CommUtil.isNotNull(appRuleAppl)){
						
						if(!input.getAprule().get(tg).getRulesq().equals(appRuleAppl.getRulesq())){
							throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则组内规则"+input.getAprule().get(tg).getRulecd()+"对应组内序号是"+appRuleAppl.getRulesq());
						}else{								
							
							if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
								
								throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则没有需要复核的删除申请记录");
							}
							
							if(appRuleAppl.getUserid().equals(input.getChckus())){
								throw ApError.Aplt.E0000("录入复核不能为同一柜员");
							}
									
							appRuleAppl.setOptype(input.getOptype());
							appRuleAppl.setChckdt(trandt);
							appRuleAppl.setChcksq(transq);
							appRuleAppl.setChckus(input.getChckus());
							AppRuleApplDao.updateOne_odb1(appRuleAppl);
																							
							AppRuleDao.deleteOne_odb2(input.getAprule().get(tg).getGrupcd(), input.getAprule().get(tg).getRulecd());
							//审计删除参数
							//ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
							ApDataAudit.regLogOnDeleteParameter(appRule);
						
						}				
					}else{
						throw ApError.Aplt.E0000("第"+(tg+1)+"条数据产品规则没有需要复核的删除申请记录");
					}
				}	
				
			}else if(input.getOptype().equals(E_JYCHLIBZ.JJ)){//删除复核拒绝
				
				for(int jj=0;jj<input.getAprule().size();jj++){
					
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(jj).getGrupcd(),E_SHXSZBZ.D, input.getAprule().get(jj).getRulecd(), false);			
					//根据规则编号查询规则表
					AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(jj).getGrupcd(),  input.getAprule().get(jj).getRulecd(), false);
					//根据规则组内序号查询申请表
					AppRuleAppl appApplBySeq=AppRuleApplDao.selectOne_odb2(input.getBtchno(), input.getAprule().get(jj).getGrupcd(), input.getAprule().get(jj).getRulesq(),E_SHXSZBZ.D, false);
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(jj).getGrupcd(), input.getAprule().get(jj).getRulesq(), false);
					
					
					if(CommUtil.isNotNull(appRule)){
						
						if(!input.getAprule().get(jj).getRulesq().equals(appRule.getRulesq())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内规则"+input.getAprule().get(jj).getRulecd()+"对应组内序号是"+appRule.getRulesq());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则删除申请不存在");
					}
					
					if(CommUtil.isNotNull(appRuleBySeq)){
						
						if(!input.getAprule().get(jj).getRulecd().equals(appRuleBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内序号"+input.getAprule().get(jj).getRulesq()+"对应规则编号是"+appRuleBySeq.getRulecd());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据组内序号对应的产品规则删除申请不存在");
					}
								
					if(CommUtil.isNotNull(appApplBySeq)){
						
						if(!input.getAprule().get(jj).getRulecd().equals(appApplBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内序号"+input.getAprule().get(jj).getRulesq()+"对应规则编号是"+appApplBySeq.getRulecd());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据组内序号对应的产品规则删除申请不存在");
					}
					if(CommUtil.isNotNull(appRuleAppl)){
						
						if(!input.getAprule().get(jj).getRulesq().equals(appRuleAppl.getRulesq())){
							throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则组内规则"+input.getAprule().get(jj).getRulecd()+"对应组内序号是"+appRuleAppl.getRulesq());
						}else{
							
							if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){								
								throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则没有需要复核的删除申请记录");
							}
							if(appRuleAppl.getUserid().equals(input.getChckus())){
								throw ApError.Aplt.E0000("录入复核不能为同一柜员");
							}
							appRuleAppl.setOptype(input.getOptype());
							appRuleAppl.setChckdt(trandt);
							appRuleAppl.setChcksq(transq);
							appRuleAppl.setChckus(input.getChckus());
							AppRuleApplDao.updateOne_odb1(appRuleAppl);												
						}
					
					}else{
														
						throw ApError.Aplt.E0000("第"+(jj+1)+"条数据产品规则删除申请不存在");
						
					}
				}
																		
			}else{//删除直通模式
				
				for(int zt=0;zt<input.getAprule().size();zt++){
					
					AppRule appRule=AppRuleDao.selectOne_odb2(input.getAprule().get(zt).getGrupcd(),  input.getAprule().get(zt).getRulecd(), false);
					//根据规则编号查询申请表
					AppRuleAppl appRuleAppl=AppRuleApplDao.selectOne_odb1(input.getBtchno(), input.getAprule().get(zt).getGrupcd(),E_SHXSZBZ.D, input.getAprule().get(zt).getRulecd(), false);			
					//根据规则组内序号查询规则表
					AppRule appRuleBySeq=AppRuleDao.selectOne_odb1(input.getAprule().get(zt).getGrupcd(), input.getAprule().get(zt).getRulesq(), false);
					
					
					if(CommUtil.isNotNull(appRule)){
						
						if(!input.getAprule().get(zt).getRulesq().equals(appRule.getRulesq())){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内规则"+input.getAprule().get(zt).getRulecd()+"对应组内序号是"+appRule.getRulesq());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则不存在");
					}
					if(CommUtil.isNotNull(appRuleBySeq)){
						
						if(!input.getAprule().get(zt).getRulecd().equals(appRuleBySeq.getRulecd())){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则组内序号"+input.getAprule().get(zt).getRulesq()+"对应规则编号是"+appRuleBySeq.getRulecd());
						}
						
					}else{
						throw ApError.Aplt.E0000("第"+(zt+1)+"条数据组内序号对应的产品规则不存在");
					}
																									
					if(CommUtil.isNotNull(appRuleAppl)){
						
					//	if(!appRuleAppl.getOptype().equals(E_JYCHLIBZ.LR)){
							throw ApError.Aplt.E0000("第"+(zt+1)+"条数据产品规则存在删除申请记录");
					//	}
					//	
					//	appRuleAppl.setOptype(input.getOptype());
					//	appRuleAppl.setChckdt(trandt);
					//	appRuleAppl.setChcksq(transq);
					//	appRuleAppl.setChckus(input.getChckus());
					//	AppRuleApplDao.updateOne_odb1(appRuleAppl);	
						
					}else{
						
					 AppRuleAppl appl=SysUtil.getInstance(AppRuleAppl.class);
					 appl.setContxt(input.getAprule().get(zt).getContxt());
					 appl.setBtchno(input.getBtchno());
					 appl.setCorpno(CommTools.getTranCorpno());
					 appl.setDatatp(E_SHXSZBZ.D);
					 appl.setGrupcd(input.getAprule().get(zt).getGrupcd());
					 appl.setOptype(E_JYCHLIBZ.ZT);
					 appl.setRulecd(input.getAprule().get(zt).getRulecd());
					 appl.setRuletx(input.getAprule().get(zt).getRuletx());
					 appl.setRulesq(input.getAprule().get(zt).getRulesq());
					 appl.setTmstmp(CommToolsAplt.prcRunEnvs().getTrantm());
					 appl.setChckdt(trandt);
					 appl.setChcksq(transq);
					 appl.setChckus(input.getChckus());
					 appl.setTrandt(trandt);
					 appl.setTransq(transq);
					 appl.setUserid(input.getChckus());
					 AppRuleApplDao.insert(appl);
					 
					 AppRuleDao.deleteOne_odb2(input.getAprule().get(zt).getGrupcd(), input.getAprule().get(zt).getRulecd());
					 //审计删除参数
					 //ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
					 ApDataAudit.regLogOnDeleteParameter(appRule);
					}
					
				}
				
			}
								
		}		
			
	}	
	
	
	
	//校验
	public static void check(Input input){
		
		if(CommUtil.isNull(input.getBtchno())){
			throw ApError.Aplt.E0000("批次号不能为空");
		}
		
		if(CommUtil.isNull(input.getDatatp())){
			throw ApError.Aplt.E0000("数据操作类型不能为空");
		}	
		if(CommUtil.isNull(input.getOptype())){
			throw ApError.Aplt.E0000("操作类型不能为空");
		}	
		if(CommUtil.isNull(input.getChckus())){
			throw ApError.Aplt.E0000("操作柜员不能为空");
		}							
		if(CommUtil.isNull(input.getAprule())){
			throw ApError.Aplt.E0000("产品规则不能为空");
		}
		
		//seq规则列表序列
		for(int seq=0;seq<input.getAprule().size();seq++){
			
			if(CommUtil.isNull(input.getAprule().get(seq).getGrupcd())){
				throw ApError.Aplt.E0000("第"+(seq+1)+"条产品规则分组代码不能为空");
			}
			if(CommUtil.isNull(input.getAprule().get(seq).getRulecd())){
				throw ApError.Aplt.E0000("第"+(seq+1)+"条产品规则规则编号不能为空");
			}
			if(CommUtil.isNull(input.getAprule().get(seq).getRulesq())){
				throw ApError.Aplt.E0000("第"+(seq+1)+"条产品规则组内序号不能为空");
			}
			if(CommUtil.isNull(input.getAprule().get(seq).getContxt())){
				throw ApError.Aplt.E0000("第"+(seq+1)+"条产品规则表达式不能为空");
			}
			if(CommUtil.isNotNull(input.getAprule().get(seq).getRuletx())){
				if (input.getAprule().get(seq).getRuletx().getBytes().length > 400) {
					throw LnError.geno.E0001("第"+(seq+1)+"条数据规则说明长度过长");
				}				
			}								
		}			
	}
}	