package cn.sunline.ltts.busi.aplt.tools;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.ApbAcctRout;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.ApbAcctRoutDao;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;

public class ApAcctRoutTools {
       
    /**
     * @Author lid
     *         <p>
     *         <li>2017年1月18日-下午5:37:21</li>
     *         <li>功能说明：注册</li>
     *         </p>
     * @param acctno
     * @param accttp
     */
    public static void register(String acctno, E_ACCTROUTTYPE acctrt) {

        ApbAcctRout acctRoute = SysUtil.getInstance(ApbAcctRout.class);

        acctRoute.setAcctno(acctno);
        acctRoute.setAcctrt(acctrt);
        acctRoute.setCorpno(CommTools.prcRunEnvs().getCorpno());

        ApbAcctRoutDao.insert(acctRoute);
        //暂不使用
        //if (routList.get() == null){
        //     routList.set(new ArrayList<ApbAcctRout>()); 
        //}
        //routList.get().add(acctRoute);


        //如果是分布式，增加drs上注册，注册类型为个人客户号，卡号，电子账号
        if (CommTools.isDistributedSystem()) {
            Map<String,String> dcn_route = new HashMap<String,String>();
            dcn_route.put("acctno", acctno);
            dcn_route.put("corpno", CommTools.prcRunEnvs().getCorpno());
            dcn_route.put("dcnno", CommTools.prcRunEnvs().getCdcnno());
            if (E_ACCTROUTTYPE.CARD == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_CARDNO", dcn_route);
            } else if (E_ACCTROUTTYPE.CUSTAC == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_CUSTAC", dcn_route);
            } else if (E_ACCTROUTTYPE.PERSON == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_CUSTNO", dcn_route);
            } else if (E_ACCTROUTTYPE.LOAN == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_LNCFNO", dcn_route);
            }else if (E_ACCTROUTTYPE.PHONE == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_PHONE", dcn_route);
            }else if (E_ACCTROUTTYPE.IDTFNO == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_IDTFNO", dcn_route);
            }
            else if (E_ACCTROUTTYPE.CUSTID == acctrt) {
                ApltEngineContext.getTxnTempObjMap()
                .put("DRS_CUSTID", dcn_route);

            }
        }
    }

    /**
     * @Author lid
     *         <p>
     *         <li>2017年1月18日-下午5:37:39</li>
     *         <li>功能说明：获取账号的路由分类</li>
     *         </p>
     * @param acctno
     * @return
     */
    public static E_ACCTROUTTYPE getRouteType(String acctno) {
        ApbAcctRout acctRoute = getAcctRout(acctno, false);
        return CommUtil.isNull(acctRoute) ? E_ACCTROUTTYPE.NONE : acctRoute.getAcctrt();
    }

    /**
     * 
     * <p>Title:getAcctCorpno </p>
     * <p>Description: 根据账号获取所属法人</p>
     * 
     * @author cuijia
     * @date 2017年6月8日
     * @param acctno 账号
     * @return 法人号
     */
    public static String getAcctCorpno(String acctno) {
        return getAcctRout(acctno, true).getCorpno();
    }

    /**
     * 
     * <p>Title:getAcctRout </p>
     * <p>Description:根据账号获取账号路由表数据</p>
     * 
     * @author cuijia
     * @date 2017年6月8日
     * @param acctno 账号
     * @param throwable 抛出空值异常标志
     * @return 账号路由表数据
     */
    private static ApbAcctRout getAcctRout(String acctno, boolean throwable) {
        ApbAcctRout acctRoute = ApbAcctRoutDao.selectOne_odb1(
                acctno, false);
        if (throwable && CommUtil.isNull(acctRoute)) {
            throw ApError.Aplt.E0063(acctno);
        }
        return acctRoute;
    }
    
}