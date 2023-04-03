package com.knziha.plod.db;

import static com.knziha.plod.plaindict.MdictServer.emptyResponse;
import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.polymer.wget.WGet;
import com.knziha.polymer.wget.info.DownloadInfo;

import org.nanohttpd.protocols.http.HTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;

import java.io.File;
import java.net.URL;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FFDB extends SQLiteOpenHelper {
	static FFDB instance;
	private SQLiteDatabase database;
	private String pathName;
	boolean bUniqueTable;
	ExecutorService threadPool;
	
	public static FFDB getInstance(MainActivityUIBase context) {
		if (instance==null) {
			synchronized (FFDB.class) {
				try {
					if (instance==null)
						instance = new FFDB(context, "bluebook.db");
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		return instance;
	}
	
	public SQLiteDatabase getDB(){return database;}
	
	public FFDB(MainActivityUIBase context, String name) {
		super(context, context.opt.pathToFFDB(name), null, CMN.dbVersionCode);
		onConfigure();
	}
	void onConfigure() {
		database = getWritableDatabase();
		pathName = database.getPath();
		instance = this;
		database.execSQL("DROP TABLE IF EXISTS bilibili_history");
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
	}
	
	void getInitMap(JSONObject json, StringBuilder sb) {
		Object obj;
		int idx=0;
		Set<String> keys = json.keySet();
		Iterator<String> iter = keys.iterator();
		String key;
		boolean ftd = false;
		while (iter.hasNext()) {
			key=iter.next();
			obj = json.get(key);
			if (ftd) {
				sb.append(" , ");
			} else {
				ftd = true;
			}
			idx++;
			if (key.equals("rowId") || key.equals("id")) {
				sb.append("id INTEGER PRIMARY KEY AUTOINCREMENT");
			}
			else if (obj instanceof Integer
					|| obj instanceof Long
					|| obj instanceof Short
			) {
				sb.append(key).append(" INTEGER");
				if (IU.parsint(obj+"", -1)!=-1) {
					sb.append(" DEFAULT ").append(obj);
				}
			}
			else if (obj instanceof String) {
				String iniVal = obj.toString();
				if (iniVal.equals("blob")) {
					sb.append(key).append(" BLOB");
				} else {
					sb.append(key).append(" TEXT");
					if (!TextUtils.isEmpty(iniVal) && !iniVal.equals("-1")) {
						sb.append(" DEFAULT ").append("'").append(obj).append("'");
					}
				}
			}
			else {
				CMN.Log("getInitMap::不支持::", idx, obj);
			}
		}
	}
	
	public void openTable(String table, JSONObject map, JSONObject indexed) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ")
				.append(table)
				.append("(");
		getInitMap(map, sb);
		String sql = sb.append(")").toString();
		CMN.Log("openTable::sql::", sql);
		database.execSQL(sql);
		if (indexed!=null) {
			Iterator<String> iter = indexed.keySet().iterator();
			String key;
			boolean ftd = false;
			while (iter.hasNext()) {
				key=iter.next();
				sql="CREATE "+(IU.parsint(indexed.getInteger(key), 0)==1?" UNIQUE ":"")+"INDEX if not exists "+table+"_"+key+"_index ON "+table+"("+key+")";
				CMN.Log("openTable::sql::index::", sql);
				database.execSQL(sql);
			}
		}
	}
	
	void getValNames(JSONObject json, StringBuffer sb, boolean kuohao) {
		Set<String> keys = json.keySet();
		Iterator<String> iter = keys.iterator();
		String key;
		if(kuohao) sb.append("(");
		boolean ftd = false;
		while (iter.hasNext()) {
			key=iter.next();
			if (ftd) {
				sb.append(",");
			} else {
				ftd = true;
			}
			sb.append(key);
		}
		if(kuohao) sb.append(")");
	}
	
	void getValMaps(JSONObject json, StringBuffer sb) {
		Set<String> keys = json.keySet();
		Iterator<String> iter = keys.iterator();
		String key;
		boolean ftd = false;
		while (iter.hasNext()) {
			key=iter.next();
			if (ftd) {
				sb.append(" & ");
			} else {
				ftd = true;
			}
			sb.append(key)
					.append("=");
			Object obj = json.get(key);
			if (true) {
				sb.append("?");
			} else {
				if (obj instanceof Integer
						|| obj instanceof Long
						|| obj instanceof Short
				) {
					sb.append(obj);
				}
				else if (obj instanceof String) {
					sb.append("'").append(obj).append("'");
				}
				else {
					CMN.Log("setValues::不支持::", key, obj);
				}
			}
		}
	}
	
	String[] getWhereValues(JSONObject json) {
		ArrayList<String> where = new ArrayList<>();
		Iterator<String> iter = json.keySet().iterator();
		String key;
		while (iter.hasNext()) {
			key=iter.next();
			Object obj = json.get(key);
			where.add("" + obj);
		}
		return where.toArray(new String[0]);
	}
	
	void getValues(Cursor set, JSONObject json) {
		Iterator<String> iter = json.keySet().iterator();
		Object obj;
		int idx=0;
		String key;
		while (iter.hasNext()) {
			key=iter.next();
			idx++;
			//set.
			json.put(key, set.getString(idx));
		}
	}
	
	void setValues(SQLiteStatement prepared, JSONObject json) throws SQLException {
		prepared.clearBindings();
		Collection<Object> vals = json.values();
		Iterator<Object> iter = vals.iterator();
		Object obj;
		int idx=0;
		while (iter.hasNext()) {
			obj=iter.next();
			idx++;
			if (obj instanceof Integer) {
				prepared.bindLong(idx, (Integer) obj);
			}
			else if (obj instanceof Long) {
				prepared.bindLong(idx, (Long) obj);
			}
			else if (obj instanceof String) {
				prepared.bindString(idx, (String) obj);
			}
			else if (obj instanceof Blob) {
				prepared.bindBlob(idx, ((Blob) obj).getBytes(0, (int) ((Blob) obj).length()));
			} else {
				CMN.Log("setValues::不支持::", idx, obj);
			}
		}
	}
	
	
	public void putBatch(String table, JSONArray array) {
		try {
			//statement.execute("insert into TEST(rid, fav) VALUES("+rid+", "+fav+")");
			JSONObject json = array.getJSONObject(0);
			StringBuffer sb = new StringBuffer();
			sb.append("REPLACE INTO ").append(table);
			getValNames(json, sb, true);
			sb.append(" VALUES(");
			boolean ftd = false;
			for (int i = 0; i < json.size(); i++) {
				if (ftd) {
					sb.append(",");
				} else {
					ftd = true;
				}
				sb.append("?");
			}
			sb.append(")");
			String sql = sb.toString();
			synchronized (database) {
				SQLiteStatement prepared = database.compileStatement(sql);
				database.beginTransaction();  //开启事务
				int cc= 0;
				try {
					prepared.clearBindings();
					cc = 0;
					for (int i = 0; i < array.size(); i++) {
						json = array.getJSONObject(i);
						prepared.clearBindings();
						setValues(prepared, json);
						if (prepared.executeInsert()!=-1) {
							cc ++;
						}
					}
					database.setTransactionSuccessful();
				} catch (SQLException e) {
					CMN.Log(e);
					database.endTransaction();
				}
				database.endTransaction();
				CMN.Log("executeBatch::", cc, array.size());
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public long put(String table, JSONObject json) {
		long ret = -1;
		try {
			//statement.execute("insert into TEST(rid, fav) VALUES("+rid+", "+fav+")");
			StringBuffer sb = new StringBuffer();
			sb.append("REPLACE INTO ").append(table);
			getValNames(json, sb, true);
			sb.append(" VALUES(");
			boolean ftd = false;
			for (int i = 0; i < json.size(); i++) {
				if (ftd) {
					sb.append(",");
				} else {
					ftd = true;
				}
				sb.append("?");
			}
			sb.append(")");
			String sql = sb.toString();
			CMN.debug("put::sql::", sql);
			synchronized (database) {
				SQLiteStatement prepared = database.compileStatement(sql);
				database.beginTransaction();  //开启事务
				int cc= 0;
				try {
					prepared.clearBindings();
					setValues(prepared, json);
					ret = prepared.executeInsert();
					database.setTransactionSuccessful();
				} catch (SQLException e) {
					CMN.Log(e);
					database.endTransaction();
				}
				database.endTransaction();
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return ret;
	}
	
	public JSONObject get(String table, JSONObject ret, JSONObject where) {
		try {
			//ResultSet set = statement.executeQuery("select fav from TEST where rid="+rid+" limit 1");
			StringBuffer sb = new StringBuffer();
			sb.append("select ");
			getValNames(ret, sb, false); // 模板
			sb.append(" FROM ").append(table).append(" WHERE ");
			getValMaps(where, sb);
			sb.append(" limit 1");
			String sql = sb.toString();
			CMN.Log("get::sql::", sql);
			synchronized (database) {
				//SQLiteStatement prepared = database.compileStatement(sql);
				//setValues(prepared, where);
				try(Cursor cursor = database.rawQuery(sql, getWhereValues(where))) {
					if (cursor.moveToNext()) {
						getValues(cursor, ret);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return ret;
	}
	
	public JSONArray getBatch(String table, JSONObject temp, JSONArray array) {
		JSONObject where = array.getJSONObject(0);
		JSONArray ret = new JSONArray();
		try {
			//ResultSet set = statement.executeQuery("select fav from TEST where rid="+rid+" limit 1");
			StringBuffer sb = new StringBuffer();
			sb.append("select ");
			getValNames(temp, sb, false); // 模板
			sb.append(" FROM ").append(table).append(" WHERE ");
			getValMaps(where, sb);
			sb.append(" limit 1");
			String sql = sb.toString();
			CMN.Log("getBatch::sql::", sql);
			synchronized (database) {
				for (int i = 0; i < array.size(); i++) {
					where = array.getJSONObject(i);
					try(Cursor cursor = database.rawQuery(sql, getWhereValues(where))) {
						if (cursor.moveToNext()) {
							JSONObject templet = (JSONObject) temp.clone();
							getValues(cursor, templet);
							ret.add(templet);
						}
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		CMN.Log("getBatch::", ret.size(), array.size());
		return ret;
	}
	
	public void downloadUnique(JSONObject temp) {
		CMN.Log("downloadUnique::", temp);
//		filename: ""
//		finalUrl: "https://"
//		referrer: "https://"
		String filename = temp.getString("filename");
		String finalUrl = temp.getString("finalUrl");
		int idx = finalUrl.indexOf("?");
		if (idx>0) finalUrl = finalUrl.substring(0, idx);
		String referrer = temp.getString("referrer");
		synchronized (database) {
			if (!bUniqueTable) {
				try {
					openTable("files", FFDB.parseObject("{url:'', name:'', fav:0}"), FFDB.parseObject("{url:1}"));
					bUniqueTable = true;
					threadPool = Executors.newFixedThreadPool(5);
				} catch (SQLException ignored) { }
			}
		}
		JSONObject ret = new JSONObject();
		ret.put("name", null);
		get("files", ret, FFDB.parseObject("{url:'"+finalUrl+"'}"));
		if (ret.get("name") != null) {
			CMN.Log("已经下载过了！");
		} else {
			CMN.Log("下载…");
			ret.put("name", filename);
			ret.put("url", finalUrl);
			ret.put("fav", 0);
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						DownloadInfo info = new DownloadInfo(new URL(temp.getString("finalUrl")));
						info.setReferer(new URL(temp.getString("referrer")));
						//String path = "C:\\Users\\TEST\\Downloads\\Video\\";
						String path = new File(Environment.getExternalStorageDirectory(), "Download/PLOD/").getPath();
						String fn = filename;
						String fix = ".mp4";
						int idx = fn.lastIndexOf(".");
						if (idx>0) {
							fix = fn.substring(idx);
							fn = fn.substring(0, idx);
						}
						File file = new File(path+fn+fix);
						int cc=0;
						while (file.exists()) {
							file = new File(path+fn+"."+(++cc)+fix);
						}
						WGet wGet = new WGet(info, file);
						wGet.download();
						put("files", ret);
					} catch (Exception e) {
						CMN.Log(e);
					}
				}
			});
		}
	}
	
	public static JSONObject parseObject(String text) {
		if (text==null) {
			return null;
		}
		boolean nord = (JSON.DEFAULT_PARSER_FEATURE & Feature.OrderedField.getMask())==0;
		JSON.DEFAULT_PARSER_FEATURE |= Feature.OrderedField.getMask();
		JSONObject ret = JSONObject.parseObject(text);
		if (nord) {
			JSON.DEFAULT_PARSER_FEATURE &= ~Feature.OrderedField.getMask();
		}
		return ret;
	}
	
	public static Response handleRequest(MainActivityUIBase context, HTTPSession session) {
//		try {
//			CMN.debug("handleFFDB::", session.getMethod());
//			HashMap<String, String> map = new HashMap<>();
//			SU.Log("DB.jsp::", session.getParameters(), session.getMethod());
//			SU.Log("DB.jsp::", map);
//			InputStream input = session.getInputStream();
//			BU.printStreamToFile(input, 0, (int) session.getBodySize(), new File("/sdcard/test.png"));
//
//		} catch (Exception e) {
//			CMN.debug(e);
//		}
		String query = session.getQueryParameterString();
		CMN.debug("query::", query);
		CMN.debug("session::", session, session.getUri()
				, "content-type="+session.getHeaders().get("content-type"), session.getParameters());
		if (Method.POST.equals(session.getMethod())) {
			try {
				HashMap<String, String> map = new HashMap<>();
				session.parseBody(map);
				//SU.Log("DB.jsp::", session.getHeaders());
				SU.Log("DB.jsp::", session.getParameters(), session.getMethod());
				FFDB db = FFDB.getInstance(context);
//				String text = session.getParameter("data");
				String text = session.getParms().get("data");
				//SU.Log("text::", text, text.length());
				//text = URLDecoder.decode(text);
				Objects.requireNonNull(text);
				JSONObject data = null;
				if (text.startsWith("[")) {
					JSONArray objs = JSONArray.parseArray(text);
					for (int i = 0; i < objs.size(); i++) {
						if (objs.get(i) instanceof JSONObject) {
							data = objs.getJSONObject(0);
						}
					}
				} else {
					data = FFDB.parseObject(text);
				}
				if (data.containsKey("dwnld")) {
					getInstance(context).downloadUnique(data);
				} else {
					JSONArray fSet = data.getJSONArray("fSet");
					String tableName = data.getString("table");
					if (fSet != null) {
						// todo FHDB
					}
					else {
						//CMN.Log(data);
						JSONObject json = data.getJSONObject("json");
						JSONArray batch = data.getJSONArray("batch");
						JSONObject where = data.getJSONObject("where");
						JSONObject indexed = data.getJSONObject("indexed");
						if (json==null && batch!=null) {
							json = batch.getJSONObject(0);
						}
						CMN.Log(tableName, json, where, indexed);
						Objects.requireNonNull(tableName);
						Objects.requireNonNull(json);
						if (indexed != null) { // open
							db.openTable(tableName, json, indexed);
						}
						else if (where != null) { // get
							String ret;
							if (batch != null) {
								JSONArray result = db.getBatch(tableName, json, batch);
								ret = result.toString();
								CMN.Log("getBatch::", result.size(), json.size());
							} else {
								ret = db.get(tableName, json, where).toString();
							}
							return newFixedLengthResponse(ret);
						}
						else { // set
							if (batch != null) {
								db.putBatch(tableName, batch);
							} else {
								return newFixedLengthResponse("{\"id\":"+db.put(tableName, json)+"}");
							}
						}
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		return emptyResponse;
	}
	
}
