package cn.sunline.ltts.gns;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_YESORNO;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoDuplicateException;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.edsp.busi.icore.ap.gns.errors.GnsERROR;
import cn.sunline.ltts.gns.ApGns.AppGns;
import cn.sunline.ltts.gns.ApGns.AppGnsDao;
import cn.sunline.ltts.gns.ApGns.AppParm;
import cn.sunline.ltts.gns.ApGns.AppParmDao;
import cn.sunline.ltts.gns.api.GnsApi;
import cn.sunline.ltts.gns.api.GnsKey;
import cn.sunline.ltts.gns.api.GnsRes;
import cn.sunline.ltts.gns.type.ApGnsEnumType.E_GNSOPT;
import cn.sunline.ltts.gns.type.ApGnsEnumType.E_GNSSTS;

@Component
public class GnsImpl implements GnsApi {

	private static final SysLog log = SysLogUtil.getSysLog(GnsImpl.class) ; 

	@Override
	public void add(GnsKey gnsKey) {
		log.info("路由分片健信息:[%s]", gnsKey.toString());
		gnsKeyValiate(gnsKey);
		// 组装数据
		insertAppGns(gnsKey);
	}

	private void insertAppGns(GnsKey gnsKey) {
		AppGns ags = SysUtil.getInstance(AppGns.class);
		ags.setGnschn(gnsKey.getGnschn());
		ags.setGnskey(gnsKey.getGnskey());
		// 设置客户标识
		if (E_GNSOPT.CORP.getValue().equals(gnsKey.getGnsopt())) {
			ags.setGnsopt(E_GNSOPT.CORP);
		} else if (E_GNSOPT.CORP_USER.toString().equals(gnsKey.getGnsopt())) {
			ags.setGnsopt(E_GNSOPT.CORP_USER);
		} else if (E_GNSOPT.PERSON.toString().equals(gnsKey.getGnsopt())) {
			ags.setGnsopt(E_GNSOPT.PERSON);
		} else {
			ags.setGnsopt(E_GNSOPT.USER);
		}
		// 设置路由映射状态
		if (E_GNSSTS.INVLID.toString().equals(gnsKey.getGnssts())) {
			ags.setGnssts(E_GNSSTS.INVLID);
		} else {
			ags.setGnssts(E_GNSSTS.NORMAL);
		}
		ags.setGnstyp(gnsKey.getGnstyp());
		ags.setGnsval(gnsKey.getGnsval());
		try {
			AppGnsDao.insert(ags);
		}catch(AdpDaoDuplicateException e) {
			log.warn("已经有路由映射信息了，直接更新！！！");
			AppGnsDao.updateOne_odb1(ags);
		}
		
	}
	

	@Override
	public GnsRes query(GnsKey gnsKey) {

		log.info("Gnskey查询条件:[%s]", gnsKey.toString());
		E_GNSOPT opt = null;
		if (null != gnsKey.getGnsopt()) {
			// 客户标识查询条件
			if (E_GNSOPT.CORP.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.CORP;
			} else if (E_GNSOPT.CORP_USER.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.CORP_USER;
			} else if (E_GNSOPT.PERSON.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.PERSON;
			} else {
				opt = E_GNSOPT.USER;
			}
		}
		// 组合条件查询
		AppGns appGns = null;
		
		AppParm parm  = AppParmDao.selectOne_odb1(gnsKey.getGnschn(), false);
		if(parm != null && parm.getIs_ignore() == E_YESORNO.YES) {
			appGns = AppGnsDao.selectOne_odb1(gnsKey.getGnskey(), gnsKey.getGnstyp(), opt, gnsKey.getGnschn(), false);
		}else {
			appGns = AppGnsDao.selectFirst_ddb2(gnsKey.getGnskey(), gnsKey.getGnstyp(), opt, false);
		}
		
		GnsRes gnsRes = GnsRes.build();
		if(appGns != null) {
			gnsRes.setGnssts(appGns.getGnssts().toString());
			gnsRes.setGnsval(appGns.getGnsval());
		}
		return gnsRes;
	}


	// 校验路由key信息
	private void gnsKeyValiate(GnsKey gnsKey) {
		log.info("校验初始单元信息 start:[%s]"+gnsKey.toString());
		if (StringUtil.isEmpty(gnsKey.getGnskey())) {
			throw GnsERROR.gns.E0003();
		}
		if (StringUtil.isEmpty(gnsKey.getGnschn())) {
			throw GnsERROR.gns.E0004();
		}
		if (StringUtil.isEmpty(gnsKey.getGnsopt())) {
			throw GnsERROR.gns.E0005();
		}
		if (StringUtil.isEmpty(gnsKey.getGnssts())) {
			throw GnsERROR.gns.E0006();
		}
		if (StringUtil.isEmpty(gnsKey.getGnstyp())) {
			throw GnsERROR.gns.E0007();
		}
		if (StringUtil.isEmpty(gnsKey.getGnsval())) {
			throw GnsERROR.gns.E0008();
		}
		log.info("校验初始单元信息 end");
	}

	@Override
	public void add(List<GnsKey> gnsKey) {
		for (GnsKey gnsKey2 : gnsKey) {
			add(gnsKey2);
		}
	}

	@Override
	public void update(GnsKey gnsKey) {
		log.info("Gnskey更新条件:[%s]", gnsKey.toString());
		E_GNSOPT opt = null;
		if (null != gnsKey.getGnsopt()) {
			// 客户标识查询条件
			if (E_GNSOPT.CORP.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.CORP;
			} else if (E_GNSOPT.CORP_USER.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.CORP_USER;
			} else if (E_GNSOPT.PERSON.toString().equals(gnsKey.getGnsopt())) {
				opt = E_GNSOPT.PERSON;
			} else {
				opt = E_GNSOPT.USER;
			}
		}
		// 组合条件查询
		AppGns appGns = null;
		
		AppParm parm  = AppParmDao.selectOne_odb1(gnsKey.getGnschn(), false);
		if(parm != null && parm.getIs_ignore() == E_YESORNO.YES) {
			appGns = AppGnsDao.selectFirst_ddb2(gnsKey.getGnskey(), gnsKey.getGnstyp(), opt, false);
		}else {
			appGns = AppGnsDao.selectOne_odb1(gnsKey.getGnskey(), gnsKey.getGnstyp(), opt, gnsKey.getGnschn(), false);
		}
		
		if(appGns != null) {
			AppGnsDao.updateOne_odb1(appGns);
			log.info("路由映射更新成功:[%s]", appGns);
		}
	
		
	}
	
	
}
