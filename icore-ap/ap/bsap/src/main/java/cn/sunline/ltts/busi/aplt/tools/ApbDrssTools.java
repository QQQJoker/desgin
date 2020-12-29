package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrssDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRSSTATUS;

/**
 * 
 * <p>Title:ApbDrssTools</p>
 * <p>Description:  处理drs注册登记表 </p>
 * @author Cuijia
 * @date   2017年9月12日
 */
public class ApbDrssTools {

    /**
     * 
     * <p>Title:setDrsStatusFail </p>
     * <p>Description:	登记失败记录，如果有记录不做处理。事务后处理失败应该是新增数据</p>
     * @author Cuijia
     * @date   2017年9月12日 
     * @param acctno 
     * @param cdcnno
     * @param corpno
     * @param drstyp
     */
	public static void setDrsStatusFail(final String acctno, final String cdcnno, final String corpno,
			final String drstyp) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				ApbDrss apbDrssDO = ApbDrssDao.selectOne_odb1(acctno, false);
				if (CommUtil.isNull(apbDrssDO)) {
					apbDrssDO = SysUtil.getInstance(ApbDrss.class);
					apbDrssDO.setAcctno(acctno);
					apbDrssDO.setCdcnno(cdcnno);
					apbDrssDO.setCorpno(corpno);
					apbDrssDO.setDrstyp(drstyp);
					apbDrssDO.setStatus(E_DRSSTATUS.FAIL);
					ApbDrssDao.insert(apbDrssDO);
				}
				return null;
			}
		});
	}
	
    /**
     * 
     * <p>Title:setDrsStatusSuccess </p>
     * <p>Description:	更新drs注册状态为成功</p>
     * @author Cuijia
     * @date   2017年9月12日 
     * @param acctno 帐号
     */
    public static void setDrsStatusSuccess(String acctno){
        ApbDrss apbDrssDO = ApbDrssDao.selectOne_odb1(acctno, false);
        if(CommUtil.isNotNull(apbDrssDO)){
            apbDrssDO.setStatus(E_DRSSTATUS.SUCCESS);
            ApbDrssDao.updateOne_odb1(apbDrssDO);
        }
    }
}
