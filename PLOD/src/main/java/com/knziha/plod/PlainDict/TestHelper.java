package com.knziha.plod.plaindict;

import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.PowerManager;

import androidx.appcompat.app.GlobalOptions;
import androidx.core.text.HtmlCompat;

import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.text.BreakIteratorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.knziha.plod.plaindict.MainActivityUIBase.digestKey;
import static com.knziha.plod.plaindict.MainActivityUIBase.hashKey;

public class TestHelper {
	static{
		CMN.debug("TestHelper_LOADED!!!");
	}
	
	static long now;
	static long bid;
	
	public static void testLianheMoshiXuanxiang() {
		PDICMainAppOptions opt = null;
		//opt.get
	}
	
	public static void insertMegaInAnnotDb(SQLiteDatabase db, mdict mdict, int start, int numEntry, int noteStart, int numNotes) throws JSONException {
//		String entry = mWebView.word;
//		String lex = value;
//
		now = CMN.now();
		int cc=0;
		CMN.rt();
		String sql = "INSERT INTO "+LexicalDBHelper.TABLE_BOOK_ANNOT_v2
				+"(bid,entry,entryHash,entryDig,lex,lexHash,lexDig,pos,last_edit_time,creation_time,param,annot) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		SQLiteStatement preparedInsertExecutor = db.compileStatement(sql);
		db.beginTransaction();  //开启事务
		
		String entry="", content="", lex="";
		int st, ed, step, pos=0;
		try {
			Random rand = new Random();
			bid = mdict.getBooKID();
			Random random1 = new Random(100);
			Random random2 = new Random(24);
			numEntry += start;
			for (pos = start; pos < mdict.getNumberEntries(); pos++) {
				if(pos>numEntry) break;
				entry = mdict.getEntryAt(pos);
				content = mdict.getRecordAt(pos, null, true);
				content = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().replaceAll("[\r\n]", "_");
				st=10;
				if(noteStart>0) st += noteStart*20;
				BreakIteratorHelper iteratorHelper = new BreakIteratorHelper();
				iteratorHelper.setText(content);
				st = iteratorHelper.following(st);
				for (int j = 0; j < numNotes; ) {
					ed = iteratorHelper.following(st);
					if (st<0 || ed<0 || ed>=content.length()) break;
					String lexTmp = content.substring(st, ed).trim();
					st = ed;
					if(lexTmp.length()<3) continue;
					lex = lexTmp;
					j++;
					preparedInsertExecutor.bindLong(1, bid);
					preparedInsertExecutor.bindString(2, entry);
					preparedInsertExecutor.bindLong(3, hashKey(entry));
					preparedInsertExecutor.bindString(4, digestKey(entry, 3));
					preparedInsertExecutor.bindString(5, lex);
					preparedInsertExecutor.bindLong(6, hashKey(lex));
					preparedInsertExecutor.bindString(7, digestKey(lex, 2));
					preparedInsertExecutor.bindLong(8, pos);
					preparedInsertExecutor.bindLong(9, now);
					preparedInsertExecutor.bindLong(10, now);
					String path = "";
					for (int i = 0, len=Math.max(7, random1.nextInt(20)); i < len; i++) {
						path += random2.nextInt(29) + "/";
					}
					preparedInsertExecutor.bindString(11, path);
					JSONObject json = new JSONObject();
					json.put("x", 0);
					json.put("y", 0);
					json.put("s", BookPresenter.def_zoom);
					preparedInsertExecutor.bindBlob(11, json.toString().getBytes());
					long rowId = preparedInsertExecutor.executeInsert();
					if(rowId>=0)cc++;
					now += 10;
				}
				
				for (int j = 0; j < 0; j++)
				//while(true)
				{
					step = Math.max(rand.nextInt(7), 3);
					ed = st+step;
					if (ed>=content.length()) break;
					lex = content.substring(st, ed);
					st = ed;
					preparedInsertExecutor.bindLong(1, bid);
					preparedInsertExecutor.bindString(2, entry);
					preparedInsertExecutor.bindLong(3, hashKey(entry));
					preparedInsertExecutor.bindString(4, digestKey(entry, 3));
					preparedInsertExecutor.bindString(5, lex);
					preparedInsertExecutor.bindLong(6, hashKey(lex));
					preparedInsertExecutor.bindString(7, digestKey(lex, 2));
					preparedInsertExecutor.bindLong(8, pos);
					preparedInsertExecutor.bindLong(9, now);
					preparedInsertExecutor.bindLong(10, now);
					JSONObject json = new JSONObject();
					json.put("x", 0);
					json.put("y", 0);
					json.put("s", BookPresenter.def_zoom);
					preparedInsertExecutor.bindBlob(11, json.toString().getBytes());
					long rowId = preparedInsertExecutor.executeInsert();
					if(rowId>=0)cc++;
					now += 10;
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			CMN.Log(e);
		}
		db.endTransaction();  //结束事务
		
		
		CMN.Log("成功插入几条：", cc, "每秒插入：", CMN.pt()==0?"INF":cc*1000/CMN.pt());
		
		CMN.rt();
		lex += "HAHHA";
		ContentValues values = new ContentValues();
		values.put("bid", bid);
		values.put("entry", entry);
		values.put("entryHash", hashKey(entry));
		values.put("entryDig", digestKey(entry, 3));
		values.put("lex", lex);
		values.put("pos", pos);
		values.put(LexicalDBHelper.FIELD_EDIT_TIME, now);
		values.put(LexicalDBHelper.FIELD_CREATE_TIME, now);
		JSONObject json = new JSONObject();
		json.put("x", 0);
		json.put("y", 1);
		json.put("s", 2);
		values.put(LexicalDBHelper.FIELD_PARAMETERS, json.toString().getBytes());
		long id = db.insert(LexicalDBHelper.TABLE_BOOK_ANNOT_v2, null, values);
		CMN.Log("最后插入：", id, "插入时间：", CMN.pt());
	}
	
	
	public static void insertMegaInPasteBin(SQLiteDatabase db, mdict mdict) throws JSONException {
		now = CMN.now();
		int cc=0;
		CMN.rt();
		String sql = "INSERT INTO "+LexicalDBHelper.TABLE_PASTE_BIN
				+"(chn, fav, creation_time, content) VALUES(?,?,?,?)";
		SQLiteStatement preparedInsertExecutor = db.compileStatement(sql);
		db.beginTransaction();  //开启事务
		
		String entry="", content="", lex="";
		int pos=0;
		try {
			Random rand = new Random();
			for (pos = 0; pos < 100; pos++) {
				for (int i = (int) (rand.nextDouble()*pos*10); i < 10; i++) {
					content += mdict.getEntryAt(i);
				}
				
				preparedInsertExecutor.bindLong(1, 0);
				preparedInsertExecutor.bindLong(2, 0);
				preparedInsertExecutor.bindLong(3, now);
				preparedInsertExecutor.bindString(4, content);
				
				long rowId = preparedInsertExecutor.executeInsert();
				if(rowId>=0)cc++;
				now += 10;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			CMN.Log(e);
		}
		db.endTransaction();  //结束事务
		
		CMN.Log("成功插入几条：", cc, "每秒插入：", CMN.pt()==0?"INF":cc*1000/CMN.pt());
	}
	
	
	public static void testRhino(MainActivityUIBase mainActivityUIBase) {
//		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
//		try {
//			// Initialize the standard objects (Object, Function, etc.)
//			// This must be done before scripts can be executed. Returns
//			// a scope object that we use in later calls.
//			org.mozilla.javascript.Scriptable scope = cx.initStandardObjects();
//
//			// Collect the arguments into a single string.
//			String s = "var a=1;";
//
//			// Now evaluate the string we've colected.
//			Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
//
//			// Convert the result to a string and print it.
//			CMN.Log(result);
//
//		} finally {
//			// Exit from the context.
//			org.mozilla.javascript.Context.exit();
//		}
	}
	
	public static void wakeUpAndUnlock(Context context){
		if (GlobalOptions.debug) {
			try {
				KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");           //这句 过期了。。但是整个代码 在 我的 htc android4.4 还是能管用的
				//解锁
				kl.disableKeyguard();
				//获取电源管理器对象
				PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
				//获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
				PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"kn:debug");
				//点亮屏幕
				wl.acquire();
				//释放
				wl.release();
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	
	public static String RotateEncrypt(String input, boolean dec) {
		final List<Integer> Vowels = new ArrayList<>(8);
		final List<Integer> Sonants = new ArrayList<>(32);
		for (int i = 0; i < 5; i++) Vowels.add(i*2);
		for (Integer i = 0; i < 32; i++) if (!Vowels.contains(i)) Sonants.add(i);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i), b=0;
			if (c>=97)
			{
				if(c<=122)b = 97;
			}
			else if (c>=65)
			{
				if(c<=90)b = 65;
			}
			else if (c==63||c==61)
			{
				c = (char) (dec?61:63);
			}
			if (b>0) {
				int val = c-b;
				int vid=Vowels.indexOf(val);
				if(vid>=0) {
					c = (char) (b+Vowels.get((vid+(dec?3:2))%5));
				} else {
					vid=Sonants.indexOf(val);
					c = (char) (b+Sonants.get((vid+(dec?11:10))%21));
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static void annotRetrieveTest(PDICMainActivity a) {
		try {
//			BookPresenter mdx = currentDictionary;
//			//TestHelper.insertMegaInAnnotDb(prepareHistoryCon().getDB(), (mdict) mdx.bookImpl, 0, 1500, 0, 10);

//			for (int i = 0; i < 10; i++) {
//				if (a.loadManager.md_get(i).isMdict()) {
//					TestHelper.insertMegaInAnnotDb(a.prepareHistoryCon().getDB(), a.loadManager.md_get(i).getMdict(), 0, 20000, 0, 30);
//				}
//			}


			int sep = 1000;
			int slice = 20000/1000;
			for (int step = 0; step < sep; step++) {
				for (int i = 0; i < 10; i++) {
					if (a.loadManager.md_get(i).isMdict()) {
						TestHelper.insertMegaInAnnotDb(a.prepareHistoryCon().getDB(), a.loadManager.md_get(i).getMdict(), step*slice, slice, 0, 30);
					}
				}
			}
			
			Random rand = new Random(100);
			if (true) {
				for (int i = 0; i < 10; i++) { // 十本书
					BookPresenter md = a.loadManager.md_get(i);
					if (md.isMdict()) {
						CMN.rt();
						int pageCnt = 0;
						int annotCnt = 0;
						int st = 8500 + rand.nextInt(10000);
						for (int j = 0; j < 1000; j++) { // 一千页
							int pos = st + j;
							if (pos >= md.bookImpl.getNumberEntries()) {
								break;
							}
							//String entry = md.bookImpl.getEntryAt(pos);
							Cursor cursor = a.prepareHistoryCon().getDB().rawQuery("select annot from bookannot where bid=? and pos=?", new String[]{md.getId()+""
									//, ""+hashKey(entry), digestKey(entry, 3)
									, ""+pos
							});
							while (cursor.moveToNext()) {
								String annot = cursor.getString(0);
								annotCnt++;
							}
							cursor.close();
							pageCnt++;
						}
						long total = CMN.pt();
						CMN.Log("共"+pageCnt+"页"+annotCnt+"标记", " 平均每页获取时间:", total/pageCnt);
					}
				}
			} else {
				ArrayList<Long> intws = new ArrayList<>();
				for (int i = 0; i < 10; i++) { // 十本书
					BookPresenter md = a.loadManager.md_get(i);
					if (md.isMdict()) {
						CMN.rt();
						int pageCnt = 0;
						int annotCnt = 0;
						int st = 8500 + rand.nextInt(10000);
						for (int j = 0; j < 1000; j++) { // 一千页
							int pos = st + j;
							if (pos >= md.bookImpl.getNumberEntries()) {
								break;
							}
							//String entry = md.bookImpl.getEntryAt(pos);
							Cursor cursor = a.prepareHistoryCon().getDB().rawQuery("select id from bookannot where bid=? and pos=?", new String[]{md.getId()+""
									//, ""+hashKey(entry), digestKey(entry, 3)
									, ""+pos
							});
							intws.clear();
							while (cursor.moveToNext()) {
								intws.add(cursor.getLong(0));
								annotCnt++;
							}
							cursor.close();
							for (int k = 0; k < intws.size(); k++) {
								cursor = a.prepareHistoryCon().getDB().rawQuery("select annot from bookannot where id=? limit 1", new String[]{intws.get(k)+""});
								cursor.moveToNext();
								String annot = cursor.getString(0);
								cursor.close();
							}
							pageCnt++;
						}
						long total = CMN.pt();
						CMN.Log("共"+pageCnt+"页"+annotCnt+"标记", " 平均每页获取时间:", total/pageCnt);
					}
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
}
