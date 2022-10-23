package com.knziha.plod.PlainUI;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class JsonNames{
	public final static String typ = "T";
	public final static String clr = "C";
	public final static String note = "N";
	public final static String ntyp = "P";
	public final static String bon = "B";
	public final static String bin = "b";
	public final static String bclr = "Q";
	public final static String fclr = "q";
	public final static String fsz = "s";
	public final static String tPos = "p";
	
	public final static String b1 = "d";
	public final static String check = "k";
	public final static HashMap<String, String> readMap = new HashMap<>();
	static{
		final HashMap<String, String> map = readMap;
		map.put("T", "typ");
		map.put("C", "clr");
		map.put("N", "note");
		map.put("P", "ntyp");
		map.put("B", "bon");
		map.put("b", "bin");
		map.put("Q", "bclr");
		map.put("q", "fclr");
		map.put("s", "fsz");
		map.put("p", "tPos");
		map.put("d", "b1");
		map.put("k", "check");
	}
	
	public static String readString(JSONObject json, String key) {
		if(json==null) return null;
		if(json.containsKey("typ")) key = readMap.get(key);
		return json.getString(key);
	}
	public static int readInt(JSONObject json, String key, int def) {
		if(json==null) return def;
		if(json.containsKey("typ")) key = readMap.get(key);
		try {
			return json.getInteger(key);
		} catch (Exception e) {
			return def;
		}
	}
	public static boolean hasKey(JSONObject json, String key) {
		if(json==null) return false;
		if(json.containsKey("typ")) key = readMap.get(key);
		return json.containsKey(key);
	}
}
