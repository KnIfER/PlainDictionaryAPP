package db;

import java.io.File;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.MainActivityUIBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

/**
 * Created by KnIfER on 2018/8/4.
 * 数据库将创建于 data/com.knziha/databases/ 下
 */

public class LexicalDBHelper extends SQLiteOpenHelper {
    private final Context c;
    
    public final String DATABASE;
    private SQLiteDatabase database;
    public SQLiteDatabase getDB(){return database;}
    
    public static final String TABLE_MARKS = "t1";

    public static String Key_ID = "lex"; //主键
    public static final String Date = "date"; //路径
    
    public final String pathName;
    
    //构造s
    public LexicalDBHelper(MainActivityUIBase context, String name) {
        super(context, conduct(context.opt.pathToInternal().append(name).append(".sql").toString()), null, CMN.dbVersionCode);
        DATABASE=name;
        c=context;
        database = getWritableDatabase();
        pathName = database.getPath();
        oldVersion=CMN.dbVersionCode;
    }

    static String conduct(String path){
    	new File(path).getParentFile().mkdirs();
    	//CMN.Log("path is ",new File(path).getParentFile());
    	return path;
    }

	public LexicalDBHelper(MainActivityUIBase context, File file) {
        super(context, file.getAbsolutePath(), null, CMN.dbVersionCode);
        DATABASE=file.getName();
        c=context;
        database = getWritableDatabase();
        pathName = database.getPath();
        oldVersion=CMN.dbVersionCode;
	}



	@Override
    public void onCreate(SQLiteDatabase db) {//第一次
    	StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
    			.append(TABLE_MARKS)
    			.append("(")
    			.append(Key_ID).append(" text PRIMARY KEY not null,")
				.append(Date).append(" integer")
				.append(")")
				;
        db.execSQL(sqlBuilder.toString());
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
    
    
    
    public boolean contains(String id) {
		//preparedSelectExecutor.clearBindings();
		preparedSelectExecutor.bindString(1, ""+id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedSelectExecutor.simpleQueryForString();
			return true;
		}catch(Exception e){}
		return false;
	}
    
	
	public boolean containsRaw(String lex) {
     	boolean ret = false;
     	String sql = "select * from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
     	Cursor c = database.rawQuery(sql,new String[]{lex});
     	if(c.getCount()>0) ret=true;
     	c.close();
		return ret;
	}
	public boolean containsOld(String fn) {
     	boolean ret = false;
     	Cursor c = database.query(TABLE_MARKS, new String[]{Key_ID}, Key_ID + " = ? ", new String[] {fn}, null, null, null) ;
     	if(c.getCount()>0) ret=true;
     	c.close();
		return ret;
	}	
	
	boolean isDirty=false;
	public long insert(String lex) {
		isDirty=true;
		ContentValues values = new ContentValues();
		values.put(Key_ID, lex);
		values.put(Date, System.currentTimeMillis());
		return database.insert(TABLE_MARKS, null, values);
	}
	public int remove(String id) {
		return database.delete(TABLE_MARKS, Key_ID + " = ? ", new String[]{id});
	}
	public void refresh() {
		preparedSelectExecutor.close();
		if(isDirty) {
			//database.execSQL("CREATE UNIQUE INDEX idxmy ON "+TABLE_MDXES+" ("+NAME+"); ");
		}
	}
	public long insertUpdate (String lex) {
		prepareContain();
		long ret=-1;
		if(!contains(lex)) {
			ret = insert(lex);
		}else {
			ContentValues values = new ContentValues();
			values.put(Date, System.currentTimeMillis());
			ret = database.update("t1", values, "lex =?", new String[]{lex});
		}
		return ret;
	}
	
	SQLiteStatement preparedSelectExecutor;
	public void prepareContain() {
		if(preparedSelectExecutor==null) {
	     	String sql = "select * from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
			preparedSelectExecutor = database.compileStatement(sql);
		}
	}


	public long updateBookMark(String key){
		StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
    			.append("b")
    			.append("(")
    			.append(Key_ID).append(" text PRIMARY KEY not null,")
				.append(Date).append(" integer")
				.append(")")
				;
        database.execSQL(sqlBuilder.toString());
        
        database.delete("b", null, null);
        ContentValues cv  =new ContentValues();
        cv.put(Key_ID, key);
        cv.put(Date, System.currentTimeMillis());
        
        return database.insert("b", null, cv);
	}


	public String getLastBookMark() {
		try {
			Cursor c = database.rawQuery("select * from b", null);
			if(c.getCount()>0) {
				c.moveToFirst();
				return c.getString(0);
			}
			c.close();
		}catch(Exception e) {}
		return null;
	}
	
	@Override
	public void close(){
		super.close();
		if(preparedSelectExecutor!=null)
			preparedSelectExecutor.close();
	}


	public boolean isFileExsits() {
		return new File(pathName).exists();
	}
	

}
