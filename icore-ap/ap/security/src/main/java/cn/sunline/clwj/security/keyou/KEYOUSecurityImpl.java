package cn.sunline.clwj.security.keyou;

import java.util.HashMap;
import java.util.Map;

import com.sunline.encrypt.keyou.IConfig;
import com.sunline.encrypt.keyou.intf.A0.Enc_RspA1;
import com.sunline.encrypt.keyou.intf.CC.Enc_RspCD;
import com.sunline.encrypt.keyou.intf.JG.Enc_RspJH;
import com.sunline.encrypt.keyou.intf.R30.Enc_Rsp31;
import com.sunline.encrypt.keyou.intf.R34.Enc_Rsp35;
import com.sunline.encrypt.keyou.intf.R43.Enc_Rsp44;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.clwj.security.component.KEYOUSecurityComponent.AbstractKEYOUSecurity;
import cn.sunline.edsp.busi.security.tables.Security.AppSecuZPK;
import cn.sunline.edsp.busi.security.tables.Security.AppSecuZPKDao;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class KEYOUSecurityImpl extends AbstractKEYOUSecurity implements IConfig{
    private static final BizLog bizlog = BizLogUtil.getBizLog(KEYOUSecurityImpl.class);
    private static  Map<String, String> map=null;
    
    public KEYOUSecurityImpl() {
        // 从抽象组件的属性（ksys_cxzjsl、ksys_zjslsx）中获得IP等信息
        map = new HashMap<String, String>();
        if (super.getUrl() != null) {
            map.put("url", super.getUrl());
        }
        else {
            throw ApError.Aplt.E0000("url为空,请检查表配置");
        }
        if (super.getPort() != null) {
            map.put("port", super.getPort());
        }
        else {
            throw ApError.Aplt.E0000("port为空,请检查表配置");
        }
        if (super.getTimeout() != null) {
            map.put("timeout", super.getTimeout());
        }
        else {
            throw ApError.Aplt.E0000("timeout为空,请检查表配置");
        }
        if (super.getHeadlen() != null) {
            map.put("headlen", super.getHeadlen());
        }
        else {
            throw ApError.Aplt.E0000("headlen为空,请检查表配置");
        }
        if (super.getMsghead() != null) {
            map.put("msghead", super.getMsghead());
        }
        else {
            throw ApError.Aplt.E0000("msghead为空,请检查表配置");
        }

        if (super.getRsakeylen() != null) {
            map.put("rsakeylen", super.getRsakeylen());
        }
        else {
            throw ApError.Aplt.E0000("rsakeylen为空,请检查表配置");
        }

        if (super.getRsaprikeyidx() != null) {
            map.put("rsaprikeyidx", super.getRsaprikeyidx());
        }
        else {
            throw ApError.Aplt.E0000("rsaprikeyidx为空,请检查表配置");
        }

        if (super.getRsapubkeyidx() != null) {
            map.put("rsapubkeyidx", super.getRsapubkeyidx());
        }
        else {
            throw ApError.Aplt.E0000("rsapubkeyidx为空,请检查表配置");
        }
        
        KEYOUCryptoFactory.init(map);
    }
    
    @Override
    public Map<String, String> getEncryptorConfig() throws Exception {
        return map;
    }
    
    /**
     * 功能：申请工作密钥
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @param toKeyName   目标秘钥索引（即：当前系统转加密后的子系统代码）
     * @return 转加密后的密文
     */
    public Enc_RspA1 applyZpk(String fromKeyName,String toKeyName){
        Enc_RspA1 a1=new Enc_RspA1(); 
       try {
           a1= KEYOUCryptoFactory.get().applyZpk(getZPK(fromKeyName), getZPK(toKeyName));
    } catch (Exception e) {
        bizlog.error("申请工作密钥发生错误，e:", e);
        throw ApError.Aplt.E0000("申请工作密钥发生错误！", e);
    }
       if (bizlog.isDebugEnabled()) {
           bizlog.debug("申请工作密钥结果[%s]", a1.getKeyZmk());
       }
        return a1; 
    }
    /**
     * 功能：生成RSA密钥,参数在组件表中配置
     * @return 私钥结果
     */
    public Enc_Rsp35 applyRSAkey(){
        Enc_Rsp35 p35=new Enc_Rsp35();
        try {
            p35=KEYOUCryptoFactory.get().applyRSAkey();
        } catch (Exception e) {
            bizlog.error("申请RSA密钥发生错误，e:", e);
            throw ApError.Aplt.E0000("申请RSA密钥发生错误！", e);
        }
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("申请公钥结果[%s]", p35.getPublicKey());
            bizlog.debug("申请私钥结果[%s]", p35.getPrivKey());
        }
        return p35;
    }
    /**
     * 功能：使用RSA公钥加密密码
     * @param publicKey 公钥
     * @param dataLen   1024位密钥长度密码固定为128位 '0128'
     * * @param data  明文密码
     * @return 转加密后的密文
     */
    public Enc_Rsp31 rsaEncbyPlkey(String publicKey,String dataLen,String data){
        Enc_Rsp31 p31=new Enc_Rsp31();
        try {
            p31=KEYOUCryptoFactory.get().rsaEncbyPlkey(publicKey,dataLen,data);
        } catch (Exception e) {
            bizlog.error("使用指定的RSA公钥加密数据发生错误，e:", e);
            throw ApError.Aplt.E0000("使用指定的RSA公钥加密数据发生错误！", e);
        }
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("转加密结果[%s]", p31.getResdata());
        }
        return p31;
    }
    /**
     * 功能：将RSA公钥加密的密文转成用ZPK加密的密文
     * @param fromKeyName  源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @param pan  账号
     * @param pinblock   RSA公钥加密的密码
     * @param privKeyIdx   默认”99” 密钥索引
     * @param privKey  私钥
     * @param privKeylen   设置密钥长度 4，不足前补0 
     * @return 转加密后的密文
     */
    public Enc_Rsp44 rsaEnc2Zpk(String fromKeyName,String pan,String pinblock,String privKeyIdx,String privKey,String privKeylen) {
        Enc_Rsp44 p44=new Enc_Rsp44();
        try {
            p44=KEYOUCryptoFactory.get().rsaEnc2Zpk(getZPK(fromKeyName),pan,pinblock,privKeyIdx,privKey,privKeylen);
        } catch (Exception e) {
            bizlog.error("将RSA公钥加密的密文转成用ZPK加密的密文发生错误，e:", e);
            throw ApError.Aplt.E0000("将RSA公钥加密的密文转成用ZPK加密的密文发生错误！", e);
        }
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("将RSA公钥加密的密文转成用ZPK加密结果[%s]", p44.getPin());
        }
        return null;
    }
    /**
     * 功能：zpk2zpkEnc转加密
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @param toKeyName   目标秘钥索引（即：当前系统转加密后的子系统代码）
     * @param fromAcctno  源加密因子（即：账号，若开户交易前手系统未上送账号，则为空）
     * @param fromPinBlock 待转加密密文
     * @return 转加密后的密文
     */
    public Enc_RspCD zpk2zpkEnc(String fromKeyName, String toKeyName, String fromAcctno, String fromPinBlock){
        Enc_RspCD cd=new Enc_RspCD();
        try {
            cd= KEYOUCryptoFactory.get().zpk2zpkEnc(getZPK(fromKeyName), getZPK(toKeyName), fromAcctno,fromPinBlock);
        } catch (Exception e) {
            bizlog.error("转加密发生错误，e:", e);
            throw ApError.Aplt.E0000("转加密发生错误！", e);
        }
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("转加密结果[%s]", cd.getDstPbk());
        }
        return  cd;
    }
    
    /**
     * 功能：encryptPin明文加密
     * @param Pinblk 明文密码
     * @param Acctno   账号
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @return 转加密后的密文
     */
    public Enc_RspJH encryptPin(String Pinblk,String Acctno,String fromKeyName){
        Enc_RspJH jh=new Enc_RspJH();
        try {
            jh= KEYOUCryptoFactory.get().encryptPin(Pinblk, Acctno,getZPK(fromKeyName));
        } catch (Exception e) {
            bizlog.error("明文加密发生错误，e:", e);
            throw ApError.Aplt.E0000("明文加密发生错误！", e);
        }
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("明文加密结果[%s]", jh.getPinzpk());
        }
        return jh;
    }
    @Override
    public String cvvBuild(String arg0, String arg1, String arg2) {
        throw ApError.Aplt.E0000("暂不支持【cvv生成】功能！");
    }
    @Override
    public Boolean cvvCheck(String arg0, String arg1, String arg2, String arg3) {
        throw ApError.Aplt.E0000("暂不支持【cvv校验】功能！");
    }
    @Override
    public String decryptPin(String arg0, String arg1) {
        throw ApError.Aplt.E0000("暂不提供【解密】功能！");
    }
    @Override
    public String encryptPin(String arg0, String arg1) {
        throw ApError.Aplt.E0000("请使用带秘钥Key的方法进行明文加密！");
    }
    @Override
    public Boolean macCheck(String arg0, String arg1, String arg2) {
        throw ApError.Aplt.E0000("暂不支持【mac校验】功能！");
    }
    @Override
    public String translatePin(String arg0, String arg1, String arg2) {
        throw ApError.Aplt.E0000("请使用带秘钥Key的方法进行转加密！");
    }
    @Override
    public String translatePin(String arg0, String arg1, String arg2, String arg3, String arg4) {
        throw ApError.Aplt.E0000("暂不支持【translatePin】功能！");
    }
    private String getZPK(String systcd) {
        AppSecuZPK zpk = AppSecuZPKDao.selectOne_odb1(systcd, false);
        if (zpk == null || zpk.getZpkval() == null) {
            throw ApError.Aplt.E0000("未找到系统[" + systcd + "]的ZPK定义，请检查参数配置！");
        }
        try {
            return zpk.getZpkval();
        } catch (NumberFormatException e) {
            throw ApError.Aplt.E0000("系统[" + systcd + "]的ZPK定义应为ZPK的Index，请检查参数配置！");
        }

    }
}
