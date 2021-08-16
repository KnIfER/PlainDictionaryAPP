package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import static com.knziha.plod.plaindict.PDICMainAppOptions.testDBV2;

public class LexicalDBHelper extends SQLiteOpenHelper {
	public static final String TABLE_HISTORY_v2 = "history";
	public static final String TABLE_BOOK_v2 = "book";
	public static final String TABLE_BOOKMARK_v2 = "bookmark";
	public static final String TABLE_FAVORITE_v2 = "favorite";
	public static final String TABLE_FAVORITE_FOLDER_v2 = "favfolder";
	
	public static final String FIELD_CREATE_TIME = "creation_time";
	
    public final String DATABASE;
	public boolean lastAdded;
	private SQLiteDatabase database;
    public SQLiteDatabase getDB(){return database;}
    
    public static final String TABLE_MARKS = "t1";

    public static String Key_ID = "lex"; //主键
    public static final String Date = "date"; //路径
    
    public String pathName;

	/** 创建收藏夹数据库（从名称） */
    public LexicalDBHelper(Context context, PDICMainAppOptions opt, String name) {
        super(context, opt.pathToFavoriteDatabase(name), null, CMN.dbVersionCode);
        DATABASE=name;
		onConfigure();
    }

	/** 创建历史纪录数据库 */
	public LexicalDBHelper(Context context, PDICMainAppOptions opt) {
		super(context, opt.pathToFavoriteDatabase(null), null, CMN.dbVersionCode);
		DATABASE="history.sql";
		onConfigure();
	}
	
	void onConfigure() {
		database = getWritableDatabase();
		pathName = database.getPath();
		oldVersion=CMN.dbVersionCode;
		prepareContain();
	}
	
	@Override
    public void onCreate(SQLiteDatabase db) {//第一次
		if (testDBV2) {
			String sqlBuilder;
			
			
			// TABLE_BOOKMARK_v2
			sqlBuilder = "create table if not exists " +
					TABLE_BOOKMARK_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", bid INTEGER NOT NULL"+
					", pos INTEGER NOT NULL" +
					", param BLOB" +
					", creation_time INTEGER NOT NULL"+
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists bkmk_index ON bookmark (bid, pos)");
			db.execSQL("CREATE INDEX if not exists bkmk_time_index ON bookmark (creation_time)");
			
			
			// TABLE_BOOK_v2
			sqlBuilder = "create table if not exists " +
					TABLE_BOOK_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", name LONGVARCHAR" +
					", path LONGVARCHAR"+
					", options BLOB"+
					", creation_time INTEGER NOT NULL"+
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists book_name_index ON book (name)");
			
			
			// TABLE_FAVORITE_FOLDER_v2
			sqlBuilder = "create table if not exists " +
					TABLE_FAVORITE_FOLDER_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", miaoshu TEXT"+
					", creation_time INTEGER NOT NULL"+
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists favfolder_index ON favfolder (lex)");
			
			
			// TABLE_FAVORITE_v2
			sqlBuilder = "create table if not exists " +
					TABLE_FAVORITE_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", books TEXT"+
					", creation_time INTEGER NOT NULL"+
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					", folder INTEGER DEFAULT 0 NOT NULL" +
					", level INTEGER DEFAULT 0 NOT NULL" +
					", notes LONGVARCHAR" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists favorite_term_index ON favorite (lex)");
			db.execSQL("CREATE INDEX if not exists favorite_level_index ON favorite (level)");
			db.execSQL("CREATE INDEX if not exists favorite_folder_index ON favorite (folder)");
			db.execSQL("CREATE INDEX if not exists favorite_time_index ON favorite (last_visit_time)");
			
			
			
			// TABLE_HISTORY_v2
			sqlBuilder = "create table if not exists " +
					TABLE_HISTORY_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", books TEXT"+ //6
					", creation_time INTEGER NOT NULL"+ //6
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists history_term_index ON history (lex)");
			db.execSQL("CREATE INDEX if not exists history_time_index ON history (last_visit_time)");
			
			
		} else {
			StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
					.append(TABLE_MARKS)
					.append("(")
					.append(Key_ID).append(" text PRIMARY KEY not null,")
					.append(Date).append(" integer")
					.append(")")
					;
			db.execSQL(sqlBuilder.toString());
		}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int _oldVersion, int newVersion) {
        //在setVersion前已经调用
        oldVersion=_oldVersion;
        //Toast.makeText(c,oldVersion+":"+newVersion+":"+db.getVersion(),Toast.LENGTH_SHORT).show();

    }

    //lazy Upgrade
	boolean isDirty=false;
    int oldVersion=1;
    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setVersion(oldVersion);
	
		if (testDBV2) {
			//onCreate(db);
		}
    }

    /////

    public boolean contains(String id) {
		//preparedSelectExecutor.clearBindings();
		preparedSelectExecutor.bindString(1, id);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			preparedSelectExecutor.simpleQueryForString();
			return true;
		} catch(Exception e) {
			//CMN.Log(e);
		}
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

	public long insert(MainActivityUIBase a, String lex) {
    	CMN.Log("insert");
		isDirty=true;
		lastAdded=true;
		if (testDBV2) {
			int count=-1;
			try {
				long id=-1;
				long NID = a.opt.getCurrFavoriteNoteBookId();
				String books = null;
				String[] where = new String[]{lex};
				boolean insertNew=true;
				Cursor c = database.rawQuery("select id,visit_count,books from favorite where lex = ? ", where);
				
				while(c.moveToFirst()) {
					id = c.getLong(0);
					if (id==NID) {
						insertNew = false;
						count = c.getInt(1);
						books = c.getString(2);
						break;
					}
				}
				
				c.close();
				
				ContentValues values = new ContentValues();
				values.put("lex", lex);
				
				values.put("books", a.collectDisplayingBooks(books));
				
				values.put("visit_count", ++count);
				
				long now = CMN.now();
				values.put("last_visit_time", now);
				
				if(insertNew) {
					values.put("folder", NID);
					values.put("creation_time", now);
					id = database.insert(TABLE_FAVORITE_v2, null, values);
				} else {
					where[0]=""+id;
					database.update(TABLE_FAVORITE_v2, values, "id=?", where);
				}
				return id;
			} catch (Exception e) {
				CMN.Log(e);
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(Key_ID, lex);
			values.put(Date, System.currentTimeMillis());
			return database.insert(TABLE_MARKS, null, values);
		}
		return -1;
	}

	public int remove(String id) {
		if (testDBV2) {
			return database.delete(TABLE_FAVORITE_v2, Key_ID + " = ? ", new String[]{id});
		} else {
			return database.delete(TABLE_MARKS, Key_ID + " = ? ", new String[]{id});
		}
	}

	public void refresh() {
		preparedSelectExecutor.close();
		if(isDirty) {
			//database.execSQL("CREATE UNIQUE INDEX idxmy ON "+TABLE_MDXES+" ("+NAME+"); ");
		}
	}

	public long updateHistoryTerm (MainActivityUIBase a, String lex) {
		CMN.rt();
		int count=-1;
		long id=-1;
		try {
			String books = null;
			String[] where = new String[]{lex};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id,visit_count,books from history where lex = ? ", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
				count = c.getInt(1);
				books = c.getString(2);
			}
			c.close();
			
			ContentValues values = new ContentValues();
			values.put("lex", lex);
			
			values.put("books", a.collectDisplayingBooks(books));
			
			values.put("visit_count", ++count);
			long now = CMN.now();
			values.put("last_visit_time", now);
			if(insertNew) {
				values.put("creation_time", now);
				id = database.insert(TABLE_HISTORY_v2, null, values);
			} else {
				//values.put("id", id);
				//database.update(TABLE_URLS, values, "url=?", where);
				//database.insertWithOnConflict(TABLE_URLS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
				where[0]=""+id;
				database.update(TABLE_HISTORY_v2, values, "id=?", where);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		CMN.pt("历史插入时间：");
		return id;
	}
	
	public int bookMarkToggle (long bid, int pos) {
		CMN.rt();
		try {
			long id=-1;
			String[] where = new String[]{""+bid, ""+pos};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id from bookmark where bid = ? and pos = ? ", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
			}
			c.close();
			
			if(insertNew) {
				ContentValues values = new ContentValues();
				values.put("bid", bid);
				values.put("pos", pos);
				long now = CMN.now();
				values.put("creation_time", now);
				database.insert(TABLE_BOOKMARK_v2, null, values);
				return 1;
			} else {
				where=new String[]{""+id};
				database.delete(TABLE_BOOKMARK_v2, "id=?", where);
				return 2;
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return 0;
	}
	
	public String getBookName(long bid) {
    	String name = null;
		CMN.rt();
		try {
			String[] where = new String[]{""+bid};
			Cursor c = database.rawQuery("select name from book where id = ? ", where);
			if(c.moveToFirst()) {
				name = c.getString(0);
			}
			c.close();
		} catch (Exception e) {
			CMN.Log(e);
		}
    	return name;
	}
	
	public String getBookPath(long bid) {
    	String name = null;
		CMN.rt();
		try {
			String[] where = new String[]{""+bid};
			Cursor c = database.rawQuery("select path from book where id = ? ", where);
			if(c.moveToFirst()) {
				name = c.getString(0);
			}
			c.close();
		} catch (Exception e) {
			CMN.Log(e);
		}
    	return name;
	}
	
	public long getBookID(String fullPath, String bookName) {
		CMN.rt();
		long id=-1;
		try {
			String[] where = new String[]{bookName};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id from book where name = ? ", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
			}
			c.close();
			
			if(insertNew) {
				ContentValues values = new ContentValues();
				values.put("name", bookName);
				values.put("path", fullPath);
				long now = CMN.now();
				values.put("creation_time", now);
				id = database.insert(TABLE_BOOK_v2, null, values);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return id;
	}

	public long insertUpdate(MainActivityUIBase a, String lex) {
    	if (testDBV2) {
			return updateHistoryTerm(a, lex);
		}
    	CMN.Log("insertUpdate");
		lastAdded=true;
		long ret=-1;
		if(!contains(lex)) {
			ret = insert(a, lex);
		} else {
			ContentValues values = new ContentValues();
			values.put(Date, System.currentTimeMillis());
			ret = database.update("t1", values, "lex =?", new String[]{lex});
		}
		return ret;
	}
	
	SQLiteStatement preparedSelectExecutor;
	private void prepareContain() {
		if(preparedSelectExecutor==null) {
	     	String sql = "select * from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
	     	if (testDBV2) {
				sql = "select * from " + TABLE_FAVORITE_v2 + " where " + Key_ID + " = ? ";
			}
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

	public boolean wipeData() {
		return database.delete(TABLE_MARKS, null, null)>0;
	}
	
	public long newFavFolder(String name) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("lex", name);
		contentValues.put("creation_time", CMN.now());
		return database.insert(TABLE_FAVORITE_FOLDER_v2, null, contentValues);
	}
	
	public String getFavoriteNoteBookNameById(long NID) {
		String ret;
		try (Cursor cursor = database.rawQuery("select lex from favfolder where id=?", new String[]{""+NID})) {
			cursor.moveToNext();
			return cursor.getString(0);
		} catch (Exception e) {
			CMN.Log(e);
			ret = "默认收藏夹";
		}
		return ret;
	}
	
	/** 删除收藏夹 */
	public int removeFolder(long value) {
		int ret=-1;
		try {
			ret = database.delete(TABLE_FAVORITE_v2, "folder = ?", new String[]{"" + value});
			database.delete(TABLE_FAVORITE_FOLDER_v2, "id = ?", new String[]{"" + value});
		} catch (Exception e) {
			CMN.Log(e);
		}
		return ret;
	}
}
