package com.joker.notice;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtils {
	private static String File_NAME = "D:\\person.txt";
	
	public static void writeObject(Serializable s) {
		try {
			ObjectOutputStream ots = new ObjectOutputStream(new FileOutputStream(File_NAME));
			ots.writeObject(s);
			ots.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object readObject() {
		Object obj = null;
		try {
			ObjectInput  input = new ObjectInputStream(new FileInputStream(File_NAME));
			obj = input.readObject();
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
}
