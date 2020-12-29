package cn.sunline.clwj.security.keyou;

import java.util.Map;

import com.sunline.encrypt.keyou.Encryptor;
import com.sunline.encrypt.keyou.IConfig;
import com.sunline.encrypt.keyou.intf.A0.Enc_ReqA0;
import com.sunline.encrypt.keyou.intf.A0.Enc_RspA1;
import com.sunline.encrypt.keyou.intf.BA.Enc_ReqBA;
import com.sunline.encrypt.keyou.intf.CC.Enc_ReqCC;
import com.sunline.encrypt.keyou.intf.CC.Enc_RspCD;
import com.sunline.encrypt.keyou.intf.JG.Enc_ReqJG;
import com.sunline.encrypt.keyou.intf.JG.Enc_RspJH;
import com.sunline.encrypt.keyou.intf.R30.Enc_Req30;
import com.sunline.encrypt.keyou.intf.R30.Enc_Rsp31;
import com.sunline.encrypt.keyou.intf.R34.Enc_Req34;
import com.sunline.encrypt.keyou.intf.R34.Enc_Rsp35;
import com.sunline.encrypt.keyou.intf.R43.Enc_Req43;
import com.sunline.encrypt.keyou.intf.R43.Enc_Rsp44;

public class KEYOUCryptoFactory {
    private IConfig conf;
    private static Map<String, String> map = null;
    private static KEYOUCryptoFactory instance = null;

    private KEYOUCryptoFactory(Map<String, String> map) {
        this.map = map;
    }

    public static void init(Map<String, String> map) {
        if (instance == null) {
            instance = new KEYOUCryptoFactory(map);
        } else {
            instance.map = map;
        }
    }

    public static KEYOUCryptoFactory get() {
        if (instance == null) {
            throw new RuntimeException("KEYOUCryptoFactory not init!");
        }
        return instance;
    }

    private Encryptor connect() {
        Encryptor keyouapi = new Encryptor();
        keyouapi.setEnvironments(conf);
        return keyouapi;
    }
    /**
     * 功能：zpk2zpkEnc转加密
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @param toKeyName   目标秘钥索引（即：当前系统转加密后的子系统代码）
     * @param fromAcctno  源加密因子（即：账号，若开户交易前手系统未上送账号，则为空）
     * @param fromPinBlock 待转加密密文
     * @return 转加密后的密文
     */
    public Enc_RspCD zpk2zpkEnc(String srcZpk, String dstZpk, String acctno,String srcPbk) throws Exception {
        Enc_RspCD response = new Enc_RspCD();
        Encryptor keyouapi = connect();
        Enc_ReqCC request=new Enc_ReqCC();
        request.setSrcZpk(srcZpk);
        request.setDstZpk(dstZpk);
        request.setAcctno(acctno);
        request.setSrcPbk(srcPbk);
        response = keyouapi.zpk2zpkEnc(request);
        return response;
    }
    /**
     * 功能：encryptPin明文加密
     * @param Pinblk 明文密码
     * @param Acctno   账号
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @return 转加密后的密文
     */
    public Enc_RspJH encryptPin(String pinblk,String acctno,String keyzmk) throws Exception {
        Enc_RspJH response = new Enc_RspJH();
        Encryptor keyouapi = connect();
        Enc_ReqBA ba=new Enc_ReqBA();
        Enc_ReqJG jg=new Enc_ReqJG();
        ba.setPinblk(pinblk);
        ba.setAcctno(acctno);
        jg.setDstZpk(keyzmk);
        response=keyouapi.encryptPin(ba, jg);
        return response;
    }

    /**
     * 功能：申请工作密钥
     * @param fromKeyName 源秘钥索引(即：上送密文所属的子系统代码,相关表knp_secu_zpk)
     * @param toKeyName   目标秘钥索引（即：当前系统转加密后的子系统代码）
     * @return 转加密后的密文
     */
    public Enc_RspA1 applyZpk(String zmk,String keyzmk) throws Exception{
        Enc_RspA1 response=new Enc_RspA1();
        Enc_ReqA0 request=new Enc_ReqA0();
        request.setCaseZmk(zmk);
        request.setKeyZmk(keyzmk);
        Encryptor keyouapi = connect();
        response=keyouapi.applyZpk(request);
        return response;
    }
    /**
     * 功能：生成RSA密钥,参数在组件表中配置
     * @return 私钥结果
     */
    public Enc_Rsp35 applyRSAkey() throws Exception{
        Enc_Rsp35 response=new Enc_Rsp35();
        Encryptor keyouapi = connect();
        Enc_Req34 request=new Enc_Req34();
        response=keyouapi.applyRSAkey(request);
        return response;
    }
    /**
     * 功能：使用RSA公钥加密密码
     * @param publicKey 公钥
     * @param dataLen   1024位密钥长度密码固定为128位 '0128'
     * * @param data  明文密码
     * @return 转加密后的密文
     */
    public Enc_Rsp31 rsaEncbyPlkey(String publicKey,String dataLen,String data) throws Exception{
        Enc_Rsp31 response=new Enc_Rsp31();
        Enc_Req30 request=new Enc_Req30();
        request.setPublickey(publicKey);
        request.setDatalen(dataLen);
        request.setData(data);
        Encryptor keyouapi = connect();
        response=keyouapi.rsaEncbyPlkey(request);
        return response;
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
    public Enc_Rsp44 rsaEnc2Zpk(String keyzpk,String pan,String pinblock,String privKeyIdx,String privKey,String privKeylen) throws Exception{
        Enc_Rsp44 response=new Enc_Rsp44();
        Enc_Req43 request=new Enc_Req43();
        request.setKeyzpk(keyzpk);
        request.setPan(pan);
        request.setPinblock(pinblock);
        request.setPrivKeyIdx(privKeyIdx);
        request.setPrivKey(privKey);
        request.setPrivKeylen(privKeylen);
        Encryptor keyouapi = connect();
        response=keyouapi.rsaEnc2Zpk(request);
        return response; 
    }
}
