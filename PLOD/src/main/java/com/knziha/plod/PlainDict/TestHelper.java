package com.knziha.plod.plaindict;

import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
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
		CMN.Log("IC_LOADED!!!");
	}
	
	static long now;
	static long bid;
	
	public static void insertMegaInAnnotDb(SQLiteDatabase db, mdict mdict) throws JSONException {
	
//		String entry = mWebView.word;
//		String lex = value;
//
		now = CMN.now();
		int cc=0;
		CMN.rt();
		String sql = "INSERT INTO "+LexicalDBHelper.TABLE_BOOK_ANNOT_v2
				+"(bid,entry,entryHash,entryDig,lex,lexHash,lexDig,pos,last_edit_time,creation_time,param) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		SQLiteStatement preparedInsertExecutor = db.compileStatement(sql);
		db.beginTransaction();  //开启事务
		
		String entry="", content="", lex="";
		int st, ed, step, pos=0;
		try {
			Random rand = new Random();
			bid = mdict.getBooKID();
			for (pos = 0; pos < mdict.getNumberEntries(); pos++) {
				if(pos>1500) break;
				entry = mdict.getEntryAt(pos);
				content = mdict.getRecordAt(pos, null, true);
				content = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().replaceAll("[\r\n]", "_");
				st=10;
				BreakIteratorHelper iteratorHelper = new BreakIteratorHelper();
				iteratorHelper.setText(content);
				st = iteratorHelper.following(st);
				for (int j = 0; j < 10; ) {
					ed = iteratorHelper.following(st);
					if (ed<0 || ed>=content.length()) break;
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
		
		
		CMN.Log("成功插入几条：", cc, "每秒插入：", cc*1000/CMN.pt());
		
		CMN.rt();
		lex += "HAHHA";
		ContentValues values = new ContentValues();
		values.put("bid", bid);
		values.put("entry", entry);
		values.put("entryHash", hashKey(entry));
		values.put("entryDig", digestKey(entry, 3));
		values.put("lex", lex);
		values.put("lexHash", hashKey(lex));
		values.put("lexDig", digestKey(lex, 2));
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
}
