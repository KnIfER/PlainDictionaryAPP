package db;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.PDICMainAppOptions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

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

    public static String Key_ID = "_id"; //主键
    public static final String Date = "path"; //路径
    

    
    //构造s
    public MdxDBHelper(Context context, String name, PDICMainAppOptions opt) {
        super(context, LexicalDBHelper.conduct(opt.pathTo().append(name).append(".sql").toString()), null, CMN.dbVersionCode);
        DATABASE=name;
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
		preparedSelectExecutor.bindString(1, ""+id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedSelectExecutor.simpleQueryForString();
			return true;
		}catch(Exception e){}
		return false;
	}
    
	public long insertUpdate (int lex) {
		prepareContain();
		long ret=-1;
		if(!contains(lex)) {
			ret = insert(lex);
		}else {
			ContentValues values = new ContentValues();
			values.put(Date, System.currentTimeMillis());
			ret = database.update("t1", values, "_id =?", new String[]{""+lex});
		}
		return ret;
	}
	
	
	
	
}
