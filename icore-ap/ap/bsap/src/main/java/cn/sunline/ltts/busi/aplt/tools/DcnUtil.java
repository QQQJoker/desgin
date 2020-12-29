package cn.sunline.ltts.busi.aplt.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil;
import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil.TargetInfo;
import cn.sunline.adp.core.util.SpringUtils;
import cn.sunline.adp.cedar.service.router.drs.util.CustomDrsRegistUtil;
import cn.sunline.adp.cedar.service.router.drs.util.RouteUtil;
import cn.sunline.edsp.midware.drs.common.exception.DRSException;
import cn.sunline.edsp.midware.drs.common.model.Consumer;
import cn.sunline.edsp.midware.drs.common.model.DCN;
import cn.sunline.edsp.midware.drs.common.model.KeyAndNoId;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.gns.api.GnsApi;
import cn.sunline.ltts.gns.api.GnsKey;
import cn.sunline.ltts.gns.api.GnsRes;

public class DcnUtil {
	private static final BizLog bizlog = BizLogUtil.getBizLog(DcnUtil.class);

	public static boolean isHX() {
		return "010".equals(SysUtil.getSystemId());
	}

	public static boolean isGL() {
		return ApConstants.GL_SYSTEMID.equals(SysUtil.getSystemId());
	}

	/**
	 * 获取当前DCN号
	 */
	public static String getCurrDCN() {
		/*
		 * if (IS_SHARDING_ROUTE) { return CoreUtil.getCurrentShardingId(); }
		 */
		return RouteUtil.findMyDcnNo();
	}

	/**
	 * 获取所有节点DCN号,包括管理节点
	 */
	public static List<String> findAllDcnNosWithAdmin() {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			ArrayList<String> dcnList = new ArrayList<String>();
			dcnList.add(getCurrDCN());
			return dcnList;
		}
		return CustomDRSUtil.findAllDcnNosWithAdmin();
	}

	/**
	 * 获取所有零售节点DCN号
	 */
	public static List<String> findAllRDcnNos() {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			ArrayList<String> dcnList = new ArrayList<String>();
			dcnList.add(getCurrDCN());
			return dcnList;
		}
		return CustomDRSUtil.findAllRDcnNos();
	}

	/**
	 * 获取所有CDCN号
	 */
	public static List<String> findAllCDcnNos() {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			ArrayList<String> dcnList = new ArrayList<String>();
			dcnList.add(getCurrDCN());
			return dcnList;
		}
		return CustomDRSUtil.findAllCDcnNos();
	}

	/**
	 * 根据卡号获取DCN
	 */
	public static String findDcnNoByCardNo(String cardNo) {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			return getCurrDCN();
		}
		Map<Integer, TargetInfo> result;
		try {
			result = CustomDRSUtil.findDcnNoByCardNo(getMap(cardNo));
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("CustomDRSUtil.findDcnNoByCardNo:" + cardNo + " fail, Exception:", e);
		}
		return result.get(KEY).getDcnNo();
		// return CustomDRSUtil.findDcnNoByCardNo(cardNo).getDcnNo();
	}

	/**
	 * 根据客户号获取DCN
	 */
	public static String findDcnNoByEcifNo(String cifNo) {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			return getCurrDCN();
		}
		Map<Integer, TargetInfo> result;
		try {
			result = CustomDRSUtil.findDcnNoByEcifNo(getMap(cifNo));
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("CustomDRSUtil.findDcnNoByEcifNo:" + cifNo + " fail, Exception:", e);
		}
		return result.get(KEY).getDcnNo();
		// return CustomDRSUtil.findDcnNoByEcifNo(cifNo).getDcnNo();
	}

	private static final Integer KEY = 1;

	private static Map<KeyAndNoId, Integer> getMap(String no) {
		Map<KeyAndNoId, Integer> queryMap = new HashMap<>();
		KeyAndNoId keyCorp = new KeyAndNoId();
		keyCorp.setKey(no);
		keyCorp.setNoId(getCorpno());
		queryMap.put(keyCorp, KEY);
		return queryMap;
	}

	private static String getCorpno() {
		return CommTools.prcRunEnvs().getCorpno();
	}

	/**
	 * 获取Admin DCN
	 */
	public static String findAdminDcnNo() {
		if (!CommTools.isDistributedSystem()) {// 集中式 返回当前dcn号
			return getCurrDCN();
		}
		return CustomDRSUtil.findAdminDcnNo();
	}

	/**
	 * 判断是否是ADM-DCN
	 */
	public static boolean isAdminDcn(String dcnNo) {
		if (!CommTools.isDistributedSystem()) {// 集中式默认为管理节点
			return true;
		}
		return CustomDRSUtil.isAdminDcn(dcnNo);
	}

	public static String getTargetRoute(TargetInfo targetInfo) {
		if (CommUtil.isNotNull(targetInfo)) {
			return CustomDRSUtil.mergeDcnAndCorpno(targetInfo);
		}
		return targetInfo.getDcnNo();
	}

	/**
	 * 根据路由字段类型与路由字段找到对应的DCN
	 * 
	 * @author jiangyaming 2019年1月16日 下午2:33:20
	 */
	public static TargetInfo findDcnNoByRouterKey(RouteUtil.BizKeyType type, String value) {
		try {
			return CustomDRSUtil.findDcnNoByRouterKey(value, type, getCorpno());
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("findDcnNoByRouterKey:[" + type + "," + value + " fail, Exception:", e);
		}
	}

	/**
	 * 注册路由---客户号
	 */
	public static String registRouteByECIF(String ecifNo, String primaryRouteKey, TargetInfo targetInfo) {
		bizlog.debug("CustomDRSUtil.registryDCNByECIF(%s,%s)", ecifNo, targetInfo);
		try {
			return registryDCN(primaryRouteKey, "ecif", ecifNo, getCorpno(), getCurrDCN()).getDcnId();
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("registRouteByECIF:" + ecifNo + " fail, Exception:", e);
		}
	}

	/**
	 * 注册路由---卡号
	 */
	public static String registRouteByCard(String bankCard, String primaryRouteKey, TargetInfo targetInfo) {
		bizlog.debug("CustomDRSUtil.registryDCNByCard(%s,%s)", bankCard, targetInfo);
		try {
			return registryDCN(primaryRouteKey, "bankCard", bankCard, getCorpno(), getCurrDCN()).getDcnId();
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("registRouteByCard:" + bankCard + " fail, Exception:", e);
		}
	}

	/**
	 * 注册路由---电子账号
	 */
	public static String registRouteByAccount(String bankAccount, String primaryRouteKey, TargetInfo targetInfo) {
		bizlog.debug("CustomDRSUtil.registryDCNByAccount(%s,%s)", bankAccount, targetInfo);
		try {
			// registryDCN(primaryRouteKey, "otherField", bankAccount, getCorpno(), getCurrDCN());//TODO 测试注册多个值list jym 
			return registryDCN(primaryRouteKey, "bankAccount", bankAccount, getCorpno(), getCurrDCN()).getDcnId();
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("registRouteByAccount:" + bankAccount + " fail, Exception:", e);
		}
	}

	/**
	 * 注册路由---手机号
	 */
	public static String registRouteByMobile(String mobile, String primaryRouteKey, TargetInfo targetInfo) {
		bizlog.debug("CustomDRSUtil.registryDCNByMobile(%s,%s)", mobile, targetInfo);
		try {
			return registryDCN(primaryRouteKey, "mobile", mobile, getCorpno(), getCurrDCN()).getDcnId();
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("registRouteByMobile:" + mobile + " fail, Exception:", e);
		}
	}

	/**
	 * 注册路由---证件号 (sharding模式下：证件号是主分片key，不需要手动再注册了)
	 */
	public static String registRouteByIDCard(String idCardNo, TargetInfo targetInfo) {
		bizlog.debug("CustomDRSUtil.registryDCNByIDCard(%s,%s)", idCardNo, targetInfo);
		try {
			return CustomDrsRegistUtil.registryDCN(idCardNo, getCorpno(), getCurrDCN()).getDcnId();
		} catch (DRSException e) {
			e.printStackTrace();
			throw ApError.Aplt.E0000("registRouteByIDCard:" + idCardNo + " fail, Exception:", e);
		}

	}

	//
	// Consumer c = new Consumer();
	// c.setPrimaryKeyValue("key_1.value");
	// c.setFields(fields);
	// Map map;
	// map.put("mobile", "13900001234");
	// map.put("mobile", Stream.of("13300001233","13900001234").collect(collector));
	private static DCN registryDCN(String primaryKeyValue, String routeKey, String value, String noId, String dcn)
			throws DRSException {
		Consumer consumer = new Consumer();
		consumer.setPrimaryKeyValue(primaryKeyValue);
		Map<String, Object> fields = new HashMap<>();
		
		fields.put(routeKey, Arrays.asList(value));
		consumer.setFields(fields);
		return CustomDrsRegistUtil.registryDCN(consumer, noId, dcn);
	}

	/**
	 * 获取当前分片ID
	 */
	public static String getCurrentShardingId() {
		return CoreUtil.getCurrentShardingId();
	}

	
}
