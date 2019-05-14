package cn.emoney.acg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

public class DataSerializableUtil {

	public static byte[] serialize(HashMap<String, String> hashMap) {
		try {
			ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(mem_out);
			
			out.writeObject(hashMap);
			
			out.close();
			mem_out.close();
			
			byte[] bytes = mem_out.toByteArray();
			return bytes;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static HashMap<String, String> deserialize(byte[] bytes) {
		try {
			ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(mem_in);
			
			HashMap<String, String> hashMap = (HashMap<String, String>) in.readObject();
			
			in.close();
			mem_in.close();
			
			return hashMap;
		} catch (StreamCorruptedException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	
}
