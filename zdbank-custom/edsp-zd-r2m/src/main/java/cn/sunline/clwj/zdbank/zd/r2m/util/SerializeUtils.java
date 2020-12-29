package cn.sunline.clwj.zdbank.zd.r2m.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeUtils {    
	/**
	 * 序列化
	 * 
	 * @param obj
	 * @return 序列化后的byte数组
	 */
	public static byte[] serialize(Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				;
			}
			try {
				baos.close();
			} catch (IOException e) {
				;
			}
		}
		return bytes;
	}

	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return 实现了Serializable接口的对象
	 * @throws Exception 
	 */
	public static <T extends Serializable> T deSerialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		Object obj = null;
		ObjectInputStream ois = null;
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			obj = ois.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				;
			}
			try {
				bais.close();
			} catch (IOException e) {
				;
			}
		}
		return (T) obj;
	}
}
