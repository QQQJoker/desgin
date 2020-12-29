package cn.sunline.clwj.oss.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsMD5Util {

	private static final Logger log = LogManager.getLogger(MsMD5Util.class);

	public static String getMD5(String str) throws NoSuchAlgorithmException {

		return null;
	}

	public static String getMD5(byte[] buff) throws NoSuchAlgorithmException {

		return null;
	}

	private static byte[] createChecksum(String filename) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(filename);
			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead = -1;

			while ((numRead = fis.read(buffer)) != -1) {
				complete.update(buffer, 0, numRead);
			}
			return complete.digest();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (null != fis) {
					fis.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		return null;

	}

	public static String getMD5Checksum(String filename) {

		if (!new File(filename).isFile()) {
			log.error("Error: " + filename + " is not a valid file.");
			return null;
		}
		byte[] b = createChecksum(filename);
		if (null == b) {
			log.error("Error:create md5 string failure!");
			return null;
		}
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < b.length; i++) {
			result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();

	}

	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	protected static MessageDigest messagedigest = null;
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsaex) {
			System.err.println(MsMD5Util.class.getName() + "初始化失败，MessageDigest不支持MD5Util。");
			nsaex.printStackTrace();
		}
	}

	/**
	 * 生成字符串的md5校验值
	 * 
	 * @param s
	 * @return
	 */
	public static String getMD5String(String s) {
		return getMD5String(s.getBytes());
	}

	/**
	 * 判断字符串的md5校验码是否与一个已知的md5码相匹配
	 * 
	 * @param password
	 *            要校验的字符串
	 * @param md5PwdStr
	 *            已知的md5校验码
	 * @return
	 */
	public static boolean checkPassword(String password, String md5PwdStr) {
		String s = getMD5String(password);
		return s.equals(md5PwdStr);
	}

	/**
	 * 生成文件的md5校验值
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileMD5String(File file) throws IOException {
		InputStream fis;
		fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int numRead = 0;
		while ((numRead = fis.read(buffer)) > 0) {
			messagedigest.update(buffer, 0, numRead);
		}
		fis.close();
		return bufferToHex(messagedigest.digest());
	}

	/**
	 * JDK1.4中不支持以MappedByteBuffer类型为参数update方法，并且网上有讨论要慎用MappedByteBuffer， 原因是当使用
	 * FileChannel.map 方法时，MappedByteBuffer 已经在系统内占用了一个句柄， 而使用 FileChannel.close
	 * 方法是无法释放这个句柄的，且FileChannel有没有提供类似 unmap 的方法， 因此会出现无法删除文件的情况。
	 * 
	 * 不推荐使用
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileMD5String_old(File file) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			FileChannel ch = in.getChannel();
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			messagedigest.update(byteBuffer);
			return bufferToHex(messagedigest.digest());
		} catch (Exception e) {
			// LogUtil.log.error(file+"生成md5失败");
			return file.getAbsolutePath();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// LogUtil.log.error("文件[" + file + "]关闭流失败");
					throw new RuntimeException("文件[" + file + "]关闭流失败", e);
				}
			}
		}
	}

	public static String getMD5String(byte[] bytes) {
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>>
												// 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}
	
	public static void main(String args[]) {
		try {
			long beforeTime = System.currentTimeMillis();
			String path = "C:\\Users\\user\\Desktop\\work_shedule.txt";
			String before = "999E42920C54CF7D66190731CD54F0E6".toLowerCase();
			String md5 = getMD5Checksum(path);
			System.out.println(md5);
			System.out.println(md5.equals(before));

			File file = new File(path);

			System.out.println(path + "'s size is : " + file.length() + " bytes, it consumes "
					+ (System.currentTimeMillis() - beforeTime) + " ms.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
