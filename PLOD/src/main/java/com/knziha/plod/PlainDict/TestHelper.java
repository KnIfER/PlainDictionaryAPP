package com.knziha.plod.plaindict;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.core.text.HtmlCompat;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.text.BreakIteratorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import db.LexicalDBHelper;

import static com.knziha.plod.plaindict.MainActivityUIBase.digestKey;
import static com.knziha.plod.plaindict.MainActivityUIBase.hashKey;

public class TestHelper {
	static{
		CMN.Log("IC_LOADED!!!");
	}
	
	
	static void testClassLoading(Class loader){
		
		try {
//			Method f=ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
//			f.setAccessible(true);
//			CMN.Log("IC_", f.invoke(loader,
//					"com.knziha.plod.plaindict.MainActivityUIBase$SaveAndRestorePagePosDelegate"));
//
//			CMN.Log("IC_2", f.invoke(loader, "com.knziha.plod.plaindict.MainActivityUIBase$ICTest"));
		
		
		} catch (Exception e) {
			CMN.Log("IC_",e);
		}
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
}
