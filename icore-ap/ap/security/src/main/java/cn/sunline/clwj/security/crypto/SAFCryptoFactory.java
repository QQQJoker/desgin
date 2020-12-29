package cn.sunline.clwj.security.crypto;

import com.sansec.hsm.saf.SAFCrypto;
import com.sansec.hsm.saf.SAFException;
import com.sansec.hsm.saf.SAFResult;
import com.sansec.impl.device.hsm.exception.CryptoException;
import com.sansec.impl.util.Bytes;

/**
 * 基于三未信安加密机功能封装。
 * 
 * 应用调用加密机主要完成功能：生成mac，验证mac，加密，转加密，验密
 * 
 * 生成MAC，要素：源数据、密钥（zak） 校验MAC，要素：源数据，密钥（zak），源MAC 加密PIN，要素：账号、明文密码、密钥(zpk)
 * 转PIN的，要素：账号、密文密码，源密钥(zpk)、目的密钥(zpk) 验PIN的，要素：账号、密文密码、源密钥(zpk)、验证密钥(zpk)
 * 
 * 其中：密钥存在加密机里，使用密钥索引调用。
 * 
 * @author chenji
 * @author yanggx
 * 
 */

public class SAFCryptoFactory {

	public static class CryptoInfo {
		public String ipaddr = "10.1.8.30"; // 加密机IP
		public int portnm = 8008; // 加密机端口
		public String macVetor = "www.sunline.cn";
		public long timeou = 10000;
	}

	private static SAFCryptoFactory instance = null;
	private CryptoInfo info = null;
	
	private SAFCryptoFactory(CryptoInfo info) {
		this.info = info;
	}

	public static void init(CryptoInfo info) {
		if (instance == null) {
			instance = new SAFCryptoFactory(info);
		} else {
			instance.info = info;
		}
	}
	
	public static SAFCryptoFactory get() {
		if (instance == null) {
			throw new RuntimeException("SAFCryptoFactory not init!");
		}
		return instance;
	}

	/**
	 * ZPK密钥方案（Z:单倍长DES密钥 X:双倍长3DES 密钥，Y:三倍长DES密钥，S:SM4密钥，P:SM1密钥，L:AES密钥）
	 */
	private static final char KEY_MODE_ZPK = 'S';

	/**
	 * ZPK密钥类型（ 000-ZMK 001-ZPK 008-ZAK OOA-ZEK）
	 */
	private static final String KEY_TYPE_ZPK = "001";

	/**
	 * ZAK密钥方案 （Z:单倍长DES密钥 X:双倍长3DES 密钥，Y:三倍长DES密钥，S:SM4密钥，P:SM1密钥，L:AES密钥）
	 */
	private static final char KEY_MODE_ZAK = 'Z';

	/**
	 * ZAK密钥类型（ 000-ZMK 001-ZPK 008-ZAK OOA-ZEK）
	 */
	private static final String KEY_TYPE_ZAK = "008";

	/**
	 * 计算MAC 使用3DES算法。 TODO mac模式：2-ISO9797模式 测试未通过
	 * 
	 * @param keyIndex
	 *            MAC密钥索引
	 * @param dataBytes
	 *            要计算MAC的数据
	 * @return 用keyIndex 的密钥计算得到的MAC值（HEX字符串）
	 * @throws CryptoException
	 * @throws SAFException
	 */
	public String generateMac(int keyIndex, String oriString) throws CryptoException, SAFException {
		SAFCrypto safapi = connect();

		byte[] iv = new byte[8];
		byte[] data = new byte[32];
		// byte[] zakCipher = new byte[16];

		// Bytes.Hex2Bin("56DBE15D2A138FDA4ADE8595F6B8D064".getBytes(), 0,
		// zakCipher, 0, 16);
		Bytes.Hex2Bin(oriString.getBytes(), 0, data, 0, 32);
		Bytes.Hex2Bin(info.macVetor.getBytes(), 0, iv, 0, 8);

		SAFResult result = safapi.SAF_GenerateMAC(1, // 加密算法 1-3DES 2-SM4 3-SM1
														// 4-AES
				1, // mac模式 1-ISO9797模式1，CBC加密取最后一分组 2-ISO9797模式3，3DES算法有效，即ANSI
					// X9.19.
				4, // 填充模式 0-不填充 1-PBOC方式 2-PBOC_MAC 3-PKCS5 4-9.19 5-X9.23方式
				KEY_TYPE_ZAK, // 密钥类型 000-ZMK 001-ZPK 008-ZAK OOA-ZEK
				keyIndex, // 密钥索引 取0表示使用外部密钥密文，使用内部密钥取值为1-2048
				null, // 外部密文（lmk加密），zmkIndex=0时生效
				iv, // 初始化向量，3DES时为8字节，其余为16字节
				data // 需要计算mac的数据
				);

		if (result.getResultCode() == 0) {
			return byte2HexStr(result.getData());
		} else {
			throw new CryptoException("Generate MAC fail. ErrorCode=" + result.getResultCode());
		}
	}

	/**
	 * 校验MAC。使用3DES算法。
	 * 
	 * @param keyIndex
	 *            MAC密钥索引
	 * @param dataBytes
	 *            生成MAC的原始数据
	 * @param macString
	 *            需要校验的MAC（HEX字符串）
	 * @return 校验相符返回true，否则返回false.
	 * @throws Exception
	 */
	public boolean validateMac(int keyIndex, String oriString, String macString) throws Exception {
		return generateMac(keyIndex, oriString).equalsIgnoreCase(macString);
	}

	/**
	 * PIN加密 使用。国密SM4算法
	 * 
	 * @param keyIndex
	 *            加密PIN的zpk密钥索引。
	 * @param pinData
	 *            PINBLOCK数据。ISO ANSI X9.8 规范，PIN BLOCK格式等于PIN按位异或主账号(PAN)。
	 * @param acctno
	 *            主账号（目前在方法内未使用）。
	 * @return 使用keyIndex密钥加密pinData的结果。（HEX字符串）
	 * @throws CryptoException
	 * @throws SAFException
	 */
	public String encryptPin(int keyIndex, String pinData, String acctno) throws CryptoException, SAFException {
		SAFCrypto safapi = connect();

		SAFResult result = safapi.SAF_Encrypt(2, // 算法 1-3DES 2-SM4 3-SM1 4-AES
				1, // 加密模式 1-ECB 2-CBC
				KEY_TYPE_ZPK, // 密钥类型 000-ZMK 001-ZPK 008-ZAK OOA-ZEK
				keyIndex, // key内部索引，取0表示使用外部密钥密文，使用内部密钥取值为1-2048
				null, // key外部密文（lmk加密），zmkIndex=0时生效
				4, // 填充方式 0-不填充 1-PBOC方式 2-PBOC_MAC 3-PKCS5 4-9.19 5-X9.23方式
				null, // 初始化向量，cbc加密时存在，3DES时为8字节，其余为16字节
				hexStr2Bytes(pinData) // 需要加密的数据
				);

		if (result.getResultCode() == 0) {
			return byte2HexStr(result.getData());
		} else {
			throw new CryptoException("Encrypt PIN fail. ErrorCode=" + result.getResultCode());
		}
	}

	/**
	 * PIN转加密。使用国密SM4算法。将srcKeyIndex加密的PIN，转为destKeyIndex加密的PIN。
	 * 
	 * @param srcKeyIndex
	 *            源加密zpk密钥索引
	 * @param destKeyIndex
	 *            目的zpk密钥索引
	 * @param acctno
	 *            账号，用于三未信安转加密API内部计算PIN BLOCK。
	 * @param pinData
	 *            源密文密码。使用源密钥索引srcKeyIndex中的zpk加密的密码。（HEX字符串）
	 * @return 转加密后的密文密码。使用目的密钥索引destKeyIndex中的zpk加密的密码（HEX字符串）
	 * @throws SAFException
	 * @throws CodeException
	 */
	public String convertPin(int srcKeyIndex, int destKeyIndex, String acctno, String pinData) throws CryptoException,
			SAFException {
		SAFCrypto safapi = connect();

		int len = acctno.length();
		String subAcctno = acctno.substring(len < 13 ? 0 : len - 13, len - 1);

		SAFResult result = safapi.SAF_TransPin('S', // 源密钥方案 Z X Y S P L
				srcKeyIndex, // 源zpk密钥索引
				null, // 源zpk密钥密文 索引为0时有效
				'S', // 目的密钥方案 Z X Y S P L
				destKeyIndex, // 目的zpk密钥索引
				null, // 目的zpk密钥密文 索引为0时有效
				01, // 源pin块格式
				hexStr2Bytes(pinData), // pin块密文
				subAcctno, // 账号去掉校验位最右12位
				01 // 目的pin块格式
				);

		if (result.getResultCode() == 0) {
			return byte2HexStr(result.getData());
		} else {
			throw new CryptoException("Convert PIN fail. ErrorCode=" + result.getResultCode());
		}
	}

	/**
	 * PIN解密。使用国密SM4算法。
	 * 
	 * @param keyIndex
	 *            加密PIN的zpk密钥索引。
	 * @param pinData
	 *            密文密码。使用源密钥索引keyIndex中的zpk加密的密码。（HEX字符串）
	 * @param acctno
	 *            主账号（目前在方法内未使用）。
	 * @return 使用keyIndex解密后的PIN BLOCK解密后
	 * @throws CryptoException
	 * @throws SAFException
	 */
	public String decryptPin(int keyIndex, String pinData, String acctno) throws CryptoException, SAFException {
		SAFCrypto safapi = connect();

		SAFResult result = safapi.SAF_Decrypt(2, // 算法 1-3DES 2-SM4 3-SM1 4-AES
				1, // 加密模式 1-ECB 2-CBC
				"001", // 密钥类型 000-ZMK 001-ZPK 008-ZAK OOA-ZEK
				keyIndex, // key内部索引，取0表示使用外部密钥密文，使用内部密钥取值为1-2048
				null, // key外部密文（lmk加密），zmkIndex=0时生效
				0, // 填充方式 0-不填充 1-PBOC方式 2-PBOC_MAC 3-PKCS5 4-9.19 5-X9.23方式
				null, // 初始化向量，cbc加密时存在，3DES时为8字节，其余为16字节
				hexStr2Bytes(pinData) // 需要加密的数据
				);
		if (result.getResultCode() == 0) {
			return byte2HexStr(result.getData());
		} else {
			throw new CryptoException("Decrypt PIN fail. ErrorCode=" + result.getResultCode());
		}
	}

	public boolean validatePin(int srcKeyIndex, String srcPinData, int valKeyIndex, String valPinData, String acctno)
			throws CryptoException, SAFException {
		String convertedPin = convertPin(srcKeyIndex, valKeyIndex, acctno, srcPinData);
		return convertedPin.equals(valPinData);
	}

	/**
	 * 生成密钥
	 * 
	 * @param keyMode
	 *            密钥方案。Z:单倍长DES密钥 X:双倍长3DES
	 *            密钥，Y:三倍长DES密钥，S:SM4密钥，P:SM1密钥，L:AES密钥
	 * @param genKeyIndex
	 *            。生成的密钥在加密机中存放的位置。
	 * @param genKeyType
	 *            。生成的密钥类型 000：ZMK，001：ZPK，008：ZAK，OOA: ZEK
	 * @throws CryptoException
	 * @throws SAFException
	 */
	private void generateKey(char keyMode, int genKeyIndex, String genKeyType) throws CryptoException, SAFException {
		int keyIndex = 1;
		SAFCrypto safapi = connect();

		// 返回值： SAFResult中 data: LMK加密下的密钥 subData: ZMK加密下的密钥 subData2:产生密钥的校验值
		SAFResult result = safapi.SAF_GenerateKey(keyMode, // 密钥方案 Z:单倍长DES密钥
															// X:双倍长3DES
															// 密钥，Y:三倍长DES密钥，S:SM4密钥，P:SM1密钥，L:AES密钥
				genKeyType, // 密钥类型： 000：ZMK，001：ZPK，008：ZAK，OOA: ZEK
				keyIndex, // zmk内部索引，取0表示使用外部密钥密文，使用内部密钥取值为1-2048。
				null, // zmk外部密文（lmk加密），zmkIndex=0时生效
				genKeyIndex, 0);

		if (result.getResultCode() != 0) {
			throw new CryptoException("Generate Key fail. ErrorCode=" + result.getResultCode());
		}
	}

	private SAFCrypto connect() throws SAFException {
		SAFCrypto safapi = SAFCrypto.getInstance();
		safapi.SAF_ConnectHsm(info.ipaddr, info.portnm);

		return safapi;
	}

	/**
	 * 字节编码，将两个hex字节编码成byte字节。
	 * 
	 * @param src0
	 * @param src1
	 * @return
	 */
	private byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 转换PIN格式（ISO ANSI X9.8 规范） 1byte长度，7byte内容，不足右补F SM4-16位， 后补F。
	 * 
	 * @param pin
	 * @return
	 */
	private byte[] formatPin(String pin) {
		byte[] pinBytes = pin.getBytes();

		// 长度域
		String pinLen = "00" + pinBytes.length;
		pinLen = pinLen.substring(pinLen.length() - 2);
		byte[] lenBytes = pinLen.getBytes();

		byte[] formatedByges = new byte[16];
		formatedByges[0] = (byte) uniteBytes(lenBytes[0], lenBytes[1]);

		byte subfixByte = "F".getBytes()[0];

		// 字节压缩
		for (int i = 1, j = 0; i < formatedByges.length; i++, j += 2) {
			formatedByges[i] = (byte) uniteBytes(pinBytes.length > j ? pinBytes[j] : subfixByte,
					pinBytes.length > j + 1 ? pinBytes[j + 1] : subfixByte);
		}

		return formatedByges;
	}

	/**
	 * 转换PIN加密的主帐号格式 8byte, 右边6byte取主账号的右12位（不包括最右边的校验位）。 SM4-16位， 3DES-8位，前补0。
	 * 
	 * @param acctno
	 * @return
	 */
	private byte[] formatAcctno(String acctno) {
		int len = acctno.length();
		byte[] acctBytes = acctno.substring(len < 13 ? 0 : len - 13, len - 1).getBytes();

		byte[] panBytes = new byte[12];
		for (int i = 0; i < 12; i++) {
			panBytes[i] = (i <= acctBytes.length ? acctBytes[i] : (byte) 0x00);
		}

		byte[] formatedBytes = new byte[16];

		// 前面10位0.
		for (int i = 0; i < 10; i++) {
			formatedBytes[i] = (byte) 0x00;
		}

		// 12位账号压缩为后6位。
		for (int i = 10, j = 0; i < formatedBytes.length; i++, j += 2) {
			formatedBytes[i] = (byte) uniteBytes(panBytes[j], panBytes[j + 1]);
		}

		return formatedBytes;
	}

	/**
	 * 计算PIN BLOCK （PIN按位异或主帐号PAN）。 PIN的格式符合ISO ANSI X9.8 规范. PIN
	 * BLOCK格式等于PIN按位异或主账号(PAN).
	 * 
	 * @param pin
	 *            密码
	 * @param accno
	 *            账号
	 * @return
	 */
	public byte[] formatPinBlock(String pin, String accno) {
		byte[] arrPin = formatPin(pin);
		byte[] arrAccno = formatAcctno(accno);

		// SM4-16位， 3DES-8位
		byte[] arrRet = new byte[16];

		// PIN BLOCK 格式等于 PIN 按位异或 主帐号;
		for (int i = 0; i < arrRet.length; i++) {
			arrRet[i] = (byte) (arrPin[i] ^ arrAccno[i]);
		}

		return arrRet;
	}

	/**
	 * bytes转换成十六进制HEX字符串
	 */
	public String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	/**
	 * 
	 * 十六进制HEX字符串转换成byte[]
	 * 
	 * @param hexStr
	 *            待转换的字符串
	 * @param length
	 *            hexStr必须达到的长度
	 * @param isLeft
	 *            左边补还是右边补
	 * @param hexStr
	 *            填充的字符
	 */
	public byte[] hexStr2Bytes(String hexStr) {
		// 转换的过程
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return bytes;
	}

	public void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase() + " ");
		}
		System.out.println("");
	}

	// -------------------------测试程序---------------------------------
	private static CryptoInfo getTestInfo() {
		CryptoInfo info = new CryptoInfo();
		info.ipaddr = "222.173.11.50";
		info.portnm = 10092; // 加密机端口
		info.macVetor = "www.sunline.cn";
		return info;
	}
	
	// 测试生成密钥
	public static void main0(String[] args) {
		try {
			SAFCryptoFactory.init(getTestInfo());
			SAFCryptoFactory.get().generateKey(KEY_MODE_ZAK, 10, KEY_TYPE_ZAK);
			SAFCryptoFactory.get().generateKey(KEY_MODE_ZPK, 11, KEY_TYPE_ZPK);
			SAFCryptoFactory.get().generateKey(KEY_MODE_ZPK, 12, KEY_TYPE_ZPK);
		} catch (CryptoException | SAFException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		System.out.println("生成密钥成功");

	}

	// 测试生成MAC\验证MAC
	public static void main2(String[] args) {
		// 生成MAC
		try {
			SAFCryptoFactory.init(getTestInfo());
			String mac = SAFCryptoFactory.get().generateMac(10, "Hello word");
			System.out.println("generateMac:" + mac);

			System.out.println("mac checkResult:" + SAFCryptoFactory.get().validateMac(10, "Hello word", mac));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 测试加、转加密、解密
	public static void main(String[] args) {
		String password = "123456";
		String acctno = "220301197607190054";

		SAFCryptoFactory.init(getTestInfo());
		System.out.println("账号:" + acctno + ", 密码:" + password);
		String pinData = SAFCryptoFactory.get().byte2HexStr(SAFCryptoFactory.get().formatPinBlock(password, acctno));

		// pinData = "06923897FFFFFFFFFFFFBFFFFFEDCBA9";

		System.out.println("原始pinData(pinblock,ISO ANSI X9.8):" + pinData);

		try {
			// 加密
			String encryptedPin = SAFCryptoFactory.get().encryptPin(11, pinData, acctno);
			System.out.println("密钥11加密后密文:" + encryptedPin);

			if (encryptedPin != null) {
				// 解密
				String decryptedPin = SAFCryptoFactory.get().decryptPin(11, encryptedPin, acctno);
				System.out.println("密钥11解密后pinData: " + decryptedPin);
				System.out.println("与原始pinData核对: " + decryptedPin.equals(pinData));
			}

			if (encryptedPin != null) {
				// 转加密
				String convertedPin = SAFCryptoFactory.get().convertPin(11, 12, acctno, encryptedPin);
				System.out.println("密钥12转PIN后密文: " + convertedPin);
				if (convertedPin != null) {
					// 解密转加密后的
					String decryptedPin2 = SAFCryptoFactory.get().decryptPin(12, convertedPin, acctno);
					System.out.println("密钥12解密转加密后的密文后pinData: " + decryptedPin2);
					System.out.println("与原始pinData核对: " + decryptedPin2.equals(pinData));
				}
			}

			System.out.println("密钥12直接加密解密");
			String encryptedPin12 = SAFCryptoFactory.get().encryptPin(12, pinData, acctno);
			System.out.println("密钥12加密后密文:" + encryptedPin12);
			String decryptedPin12 = SAFCryptoFactory.get().decryptPin(12, encryptedPin12, acctno);
			System.out.println("密钥13解密后pinData: " + decryptedPin12);
			System.out.println("与原始pinData核对: " + decryptedPin12.equals(pinData));

			// 验密
			boolean valResult = SAFCryptoFactory.get().validatePin(11, encryptedPin, 12, encryptedPin12, acctno);
			System.out.println("验密结果: " + valResult);

		} catch (CryptoException | SAFException e) {
			e.printStackTrace();
		}

	}

	// 测试计算Pin Block. ISO ANSI X9.8 规范
	public static void main3(String[] args) {
		
		SAFCryptoFactory.init(getTestInfo());
		
		// 123456
		// 123456789012345678
		// 06 12 53 DF FE DC BA 98
		// 0x06 0x12 0x53 0xDF 0xFE 0xDC 0xBA 0x98
		byte[] result1 = SAFCryptoFactory.get().formatPinBlock("123456", "123456789012345678");
		SAFCryptoFactory.get().printHexString(result1);

		// 923897
		// 4000001234562
		// 8位：06927897FFEDCBA9
		// 16位：06923897FFFFFFFFFFFFBFFFFFEDCBA9
		byte[] result2 = SAFCryptoFactory.get().formatPinBlock("923897", "4000001234562");
		SAFCryptoFactory.get().printHexString(result2);
	}
}
