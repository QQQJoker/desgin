package cn.sunline.clwj.security.crypto;

import com.sansec.hsm.saf.SAFException;
import com.sansec.impl.device.hsm.exception.CryptoException;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.security.component.SAFSecurityComponent.AbstractSAFSecurity;
import cn.sunline.clwj.security.crypto.SAFCryptoFactory.CryptoInfo;
import cn.sunline.edsp.busi.security.tables.Security.AppSecuZPK;
import cn.sunline.edsp.busi.security.tables.Security.AppSecuZPKDao;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 加密组件实现
 * <p>
 * 使用说明： BaseComp.Security security = SysUtil.getInstance(BaseComp.Security
 * .class,SAFSecurityComponent.SAFSecurity);
 * 
 * @author yanggx
 * 
 */
public class SAFSecurityImpl extends AbstractSAFSecurity {
	private static final BizLog bizlog = BizLogUtil.getBizLog(SAFSecurityImpl.class);
	CryptoInfo info = null;


	public void init() {
		if (info == null) {
			// 从抽象组件的属性（ksys_cxzjsl、ksys_zjslsx）中获得IP等信息
			this.info = new CryptoInfo();
			this.info.ipaddr = super.getIpaddr(); // IP地址
			if (super.getPortnm() != null)
				this.info.portnm = super.getPortnm();// 端口
			this.info.macVetor = super.getMacvor();// MAC向量
			if (super.getTimeou() != null)
				this.info.timeou = super.getTimeou();// 超时时间（暂时三味不支持）
			SAFCryptoFactory.init(info);
		}

	}

	@Override
	public Boolean macCheck(String pin, String mac, String lastsysid) {
		
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("准备验MAC:pin[%s],mac[%s],id[%s]", pin, mac, lastsysid);
		}

		this.init();
		if (mac == null) {
			throw ApError.Aplt.E0000("待校验的MAC为空！");
		}
		try {
			String genmac = SAFCryptoFactory.get().generateMac(getZPK(lastsysid), pin);
			if (bizlog.isDebugEnabled()) {
				bizlog.debug("生成的MAC[%s]", genmac);
			}
			return mac.equals(genmac);
		} catch (CryptoException e) {
			bizlog.error("验证MAC发生错误，e:", e);
			throw ApError.Aplt.E0000("验证MAC发生错误！", e);
		} catch (SAFException e) {
			bizlog.error("验证MAC发生错误,ResultCode[" + e.getResultCode() + "],Msg[" + e.getMsg() + "]，e:", e);
			throw ApError.Aplt.E0000("验证MAC发生错误！", e);
		}
	}

	@Override
	public String encryptPin(String acctno, String pin) {
		this.init();
		throw ApError.Aplt.E0000("暂不支持【明文加密】功能！");
	}

	@Override
	public String decryptPin(String acctno, String pinBlock) {
		this.init();
		throw ApError.Aplt.E0000("暂不支持【明文解密】功能！");
	}

	@Override
	public String translatePin(String fromAcctno, String toAcctno, String fromPinBlock) {
		this.init();
		throw ApError.Aplt.E0000("请使用带秘钥Key的方法进行转加密！");
	}

	@Override
	/**
	 * 功能：转加密
	 * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
	 * @param toKeyName   目标秘钥索引（即：当前系统转加密后的子系统代码）
	 * @param fromAcctno  源加密因子（即：账号，若开户交易前手系统未上送账号，则为空）
	 * @param toAcctno    目标加密因子（即：账号,若开户交易则当前系统开户后产生的账号）
	 * @param fromPinBlock 待转加密密文
	 * @return 转加密后的密文
	 */
	public String translatePin(String fromKeyName, String toKeyName, String fromAcctno, String toAcctno,
			String fromPinBlock) {

		if (bizlog.isDebugEnabled()) {
			bizlog.debug("准备转加密:fromKeyName[%s],toKeyName[%s],fromAcctno[%s]，toAcctno[%s],fromPinBlock[%s]",
					fromKeyName, toKeyName, fromAcctno, toAcctno, fromPinBlock);
		}
		if (CommUtil.compare(fromAcctno, toAcctno) != 0) {
			throw ApError.Aplt.E0000("暂不支持不同因子的转加密！");
		}

		try {
			this.init();
			String newPinBlock = SAFCryptoFactory.get().convertPin(getZPK(fromKeyName), getZPK(toKeyName), fromAcctno,
					fromPinBlock);
			if (bizlog.isDebugEnabled()) {
				bizlog.debug("转加密结果[%s]", newPinBlock);
			}
			return newPinBlock;
		} catch (CryptoException e) {
			bizlog.error("转加密发生错误，e:", e);
			throw ApError.Aplt.E0000("转加密发生错误！", e);
		} catch (SAFException e) {
			bizlog.error("转加密发生错误,ResultCode[" + e.getResultCode() + "],Msg[" + e.getMsg() + "]，e:", e);
			throw ApError.Aplt.E0000("转加密发生错误！", e);
		}
	}

	@Override
	public String cvvBuild(String cardNo, String expireDate, String svcCode) {
		this.init();
		throw ApError.Aplt.E0000("暂不支持【cvv生成】功能！");
	}

	@Override
	public Boolean cvvCheck(String cvv, String cardno, String serviceCode, String matdata) {
		this.init();
		throw ApError.Aplt.E0000("暂不支持【cvv校验】功能！");
	}

	private int getZPK(String systcd) {
		AppSecuZPK zpk = AppSecuZPKDao.selectOne_odb1(systcd, false);
		if (zpk == null || zpk.getZpkval() == null) {
			throw ApError.Aplt.E0000("未找到系统[" + systcd + "]的ZPK定义，请检查参数配置！");
		}

		try {
			return Integer.parseInt(zpk.getZpkval());
		} catch (NumberFormatException e) {
			throw ApError.Aplt.E0000("系统[" + systcd + "]的ZPK定义应为ZPK的Index，请检查参数配置！");
		}

	}

}
