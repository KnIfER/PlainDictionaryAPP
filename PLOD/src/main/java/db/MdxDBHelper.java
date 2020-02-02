package db;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.ParcelFileDescriptor;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Created by Zenjo Kang on 2018/8/4.
 * 数据库将创建于 data/com.knziha/databases/ 下
 */

public class MdxDBHelper extends SQLiteOpenHelper {
    private final Context c;
    
    public final String DATABASE;
    private SQLiteDatabase database;
    public SQLiteDatabase getDB(){return database;}
    
    public static final String TABLE_MARKS = "t1";
    public static final String TABLE_PAGES = "t2";
    public static final String TABLE_URLS = "t3";

    public static String Key_ID = "_id"; //主键
    public static final String Date = "path"; //路径

	public static final String Title = "data"; //路径
	public static final String Bin_Data = "bin"; //路径

    //构造s
    public MdxDBHelper(Context context, String name, PDICMainAppOptions opt) {
        super(context, name, null, CMN.dbVersionCode);
        DATABASE=name;
        //todo :: path no-e crash will throw a lot, why ???
        database = getWritableDatabase();
        c=context;
        oldVersion=CMN.dbVersionCode;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//第一次
    	StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
    			.append(TABLE_MARKS)
    			.append("(")
    			.append(Key_ID).append(" integer primary key,")
				.append(Date).append(" text")
				.append(")")
				;
        db.execSQL(sqlBuilder.toString());
    }

    public void enssurePageTable(){
		StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
				.append(TABLE_PAGES)
				.append("(")
				.append(Key_ID).append(" text primary key,")
				.append(Title).append(" text,")
				.append(Bin_Data).append(" BLOB,")
				.append(Date).append(" text")
				.append(")")
				;
		database.execSQL(sqlBuilder.toString());
	}

    public void enssureUrlTable(){
		StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
				.append(TABLE_URLS)
				.append("(")
				.append(Key_ID).append(" text primary key,")
				.append(Title).append(" text,")
				.append(Date).append(" text")
				.append(")")
				;
		database.execSQL(sqlBuilder.toString());
	}
    
    
    
    
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int _oldVersion, int newVersion) {
        //在setVersion前已经调用
        oldVersion=_oldVersion;
        Toast.makeText(c,"编辑器：项目系统的数据库架构需要更新，请随便保存一个项目以更新",Toast.LENGTH_LONG).show();
        //Toast.makeText(c,oldVersion+":"+newVersion+":"+db.getVersion(),Toast.LENGTH_SHORT).show();

    }
    //lazy Upgrade
    int oldVersion=1;
    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setVersion(oldVersion);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    




    
    
    /////
    
    
    
    public boolean contains(int id) {
     	//String sql = "select * from " + TABLE_MDXES + " where " + NAME + " = ? ";
		preparedSelectExecutor.clearBindings();
		preparedSelectExecutor.bindString(1, ""+id);
		try {
			Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			return true;
		}catch(SQLiteDoneException e){}
		return false;
     	//Cursor cursor = database.rawQuery(sql, new String[]{fn});
		//return cursor.getCount()<=0?false:true;
	}
	
	public boolean containsRaw(int id) {
     	boolean ret = false;
     	String sql = "select * from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
     	Cursor c = database.rawQuery(sql,new String[]{""+id});
     	if(c.getCount()>0) ret=true;
     	c.close();
		return ret;
     	//Cursor cursor = database.rawQuery(sql, new String[]{fn});
		//return cursor.getCount()<=0?false:true;
	}
	public boolean containsOld(String fn) {
     	boolean ret = false;
     	Cursor c = database.query(TABLE_MARKS, new String[]{Key_ID}, Key_ID + " = ? ", new String[] {fn}, null, null, null) ;
     	if(c.getCount()>0) ret=true;
     	c.close();
		return ret;
     	//Cursor cursor = database.rawQuery(sql, new String[]{fn});
		//return cursor.getCount()<=0?false:true;
	}	
	boolean isDirty=false;
	public long insert(int id) {
		isDirty=true;
		ContentValues values = new ContentValues();
		values.put(Key_ID, id);
		values.put(Date, System.currentTimeMillis()+"");
		return database.insert(TABLE_MARKS, null, values);
	}
	public int remove(int id) {
		return database.delete(TABLE_MARKS, Key_ID + " = ? ", new String[]{""+id});
	}
	
	public void refresh() {
		preparedSelectExecutor.close();
		if(isDirty) {
			//database.execSQL("CREATE UNIQUE INDEX idxmy ON "+TABLE_MDXES+" ("+NAME+"); ");
		}
	}
	SQLiteStatement preparedSelectExecutor;
	public void prepareContain() {
     	String sql = "select * from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
		preparedSelectExecutor = database.compileStatement(sql);
	}

    public boolean contains(String id) {
		//preparedSelectExecutor.clearBindings();
		preparedSelectExecutor.bindString(1, id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedSelectExecutor.simpleQueryForString();
			return true;
		}catch(Exception e){}
		return false;
	}

	//////////////////////// Url Override Pages /////////////////////
	///////////////////// Start Url Override Pages /////////////////////
	public long insertUpdate (int lex) {
		prepareContain();
		long ret=-1;
		if(!contains(lex)) {
			ret = insert(lex);
		}else {
			ContentValues values = new ContentValues();
			values.put(Date, System.currentTimeMillis()+"");
			ret = database.update("t1", values, "_id =?", new String[]{""+lex});
		}
		return ret;
	}

	SQLiteStatement preparedPageSelectExecutor;
	public void preparePageContain() {
		if(preparedPageSelectExecutor==null){
			String sql = "select * from " + TABLE_PAGES + " where " + Key_ID + " = ? ";
			preparedPageSelectExecutor = database.compileStatement(sql);
		}
	}

	public boolean containsPage(String id) {
		preparePageContain();
		preparedPageSelectExecutor.bindString(1, id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedPageSelectExecutor.simpleQueryForString();
			return true;
		}catch(Exception e){
			//CMN.Log(e);
		}
		return false;
	}

	public long putPage (String url, String title, String page) throws Exception {
		ContentValues values = new ContentValues();
		values.put(Key_ID, url);
		values.put(Title, title);
		values.put(Date, System.currentTimeMillis());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream inf = new DeflaterOutputStream(out);
		byte[] data = page.getBytes();
		//inf.write("<!DOCTYPE html>\n".getBytes());
		inf.write(data,0, data.length);
		inf.finish();
		inf.close();
		data = out.toByteArray();
		if(data.length>1024*1024*2)
			throw new DataTooLargeException("page too large!!!");
		values.put(Bin_Data, data);
		if(!containsPage(url)) {
			values.put(Key_ID, url);
			return database.insert(TABLE_PAGES, null, values);
		}else {
			//CMN.Log("更新页面……", data.length);
			return database.update("t2", values, "_id =?", new String[]{url});
		}
	}

	public String getPageString(String url, Charset _charset){
		ReusableByteOutputStream data = getPage_internal(url);
		if(data==null)
			return null;
		return new String(data.data(),0, data.size(), _charset);
	}

	public InputStream getPageStream(String url){
		ReusableByteOutputStream data = getPage_internal(url);
		if(data==null)
			return null;
		return new ByteArrayInputStream(data.data(),0, data.size());
	}

	SQLiteStatement preparedPageSelectExecutor2;
	private ReusableByteOutputStream getPage_internal (String url) {
		try {
			try {
				if(preparedPageSelectExecutor2==null)
					preparedPageSelectExecutor2 = database.compileStatement("select bin from t2 where _id = ?");
				preparedPageSelectExecutor2.bindAllArgsAsStrings(new String[] {url});
				ParcelFileDescriptor fd = null;
				try {
					fd = preparedPageSelectExecutor2.simpleQueryForBlobFileDescriptor();
				} catch (SQLiteDoneException ignored) { }
				if(fd==null)
					return null;
				FileInputStream fin = new FileInputStream(fd.getFileDescriptor());
				ReusableByteOutputStream out = new ReusableByteOutputStream();
				InflaterOutputStream inf = new InflaterOutputStream(out);
				byte[] buff = new byte[4096];
				int len;
				while((len=fin.read(buff))>0){
					inf.write(buff,0, len);
				}
				fin.close();
				inf.close();
				return out;
			} catch (Exception e) {
				//CMN.Log(e);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return null;
	}

	public Object[] getPageAndTime (String url) {
		try {
			Cursor c = database.query(TABLE_PAGES, new String[]{Key_ID, Bin_Data, Date}, Key_ID + " = ? ", new String[] {url}, null, null, null) ;
			if(c.getCount()<=0)
				return null;
			c.moveToNext();
			byte[] data = c.getBlob(1);
			long time = Long.valueOf(c.getString(2));
			data = BU.zlib_decompress(data, 0, data.length);
			c.close();
			return new Object[]{data, time};
		} catch (Exception e) {
			CMN.Log(e);
		}
		return null;
	}

	public Cursor getPageCursor() {
		//return database.query(TABLE_PAGES, new String[]{Key_ID, Title}, null, null, null, null, null) ;
		return database.rawQuery("select * from t2", null);
	}

	public int removePage(String url) {
		return database.delete(TABLE_PAGES, "_id =?", new String[]{url});
	}

	/////////////////////// URL Bookmarks /////////////////////
	///////////////////// Start URL Bookmarks /////////////////////
	SQLiteStatement preparedUrlSelectExecutor;
	public void prepareUrlContain() {
		if(preparedUrlSelectExecutor==null){
			String sql = "select * from " + TABLE_URLS + " where " + Key_ID + " = ? ";
			preparedUrlSelectExecutor = database.compileStatement(sql);
		}
	}

	public boolean containsUrl(String id) {
		prepareUrlContain();
		preparedUrlSelectExecutor.bindString(1, id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedUrlSelectExecutor.simpleQueryForString();
			return true;
		}catch(Exception e){}
		return false;
	}

	public long putUrl(String title, String url) {
		ContentValues values = new ContentValues();
		values.put(Key_ID, url);
		values.put(Title, title);
		values.put(Date, System.currentTimeMillis()+"");
		if(!containsUrl(url)) {
			return database.insert(TABLE_URLS, null, values);
		}else {
			CMN.Log("更新Url……");
			return database.update(TABLE_URLS, values, "_id =?", new String[]{url});
		}
	}

	public int removeUrl(String url) {
		return database.delete(TABLE_URLS, "_id =?", new String[]{url});
	}

	public static class DataTooLargeException extends Exception {
		public DataTooLargeException(String msg) { super(msg); }
	}
}
