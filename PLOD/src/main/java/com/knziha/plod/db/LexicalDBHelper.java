package com.knziha.plod.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.ParcelFileDescriptor;
import android.view.ViewGroup;

import com.knziha.plod.PlainUI.DBUpgradeHelper;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class LexicalDBHelper extends SQLiteOpenHelper {
	public static final String TABLE_HISTORY_v2 = "history";
	public static final String TABLE_BOOK_v2 = "book";
	//public static final String TABLE_BOOKMARK_v2 = "bookmark";
	public static final String TABLE_FAVORITE_v2 = "favorite";
	public static final String TABLE_FAVORITE_FOLDER_v2 = "favfolder";
	public static final String TABLE_BOOK_NOTE_v2 = "booknote";
	public static final String TABLE_BOOK_ANNOT_v2 = "bookannot";
	public static final String TABLE_DATA_v2 = "data";
	
	public static final String FIELD_CREATE_TIME = "creation_time";
	public static final String FIELD_VISIT_TIME = "last_visit_time";
	public static final String FIELD_EDIT_TIME = "last_edit_time";
	public static final String FIELD_ENTRY_NAME = "lex";
	public static final String FIELD_NOTES_STORAGE = "notesType";
	public static final String FIELD_NOTES = "notes";
	public static final String FIELD_FOLDER = "folder";
	public static final String FIELD_BOOK_ID = "bid";
	public static final String FIELD_POSITION = "pos";
	public static final String FIELD_PARAMETERS = "param";
	
    public final String DATABASE;
    HashMap<Long, Long> versions = new HashMap<>();
	public boolean lastAdded;
	void incrementDBVersion(Long fid) {
		Long ver = versions.get(fid);
		versions.put(fid, (ver==null?1L:(ver+1)));
	}
	void incrementFavVersion(Long fid) {
		if(fid==-1) {
			Long ver = versions.get(fid);
			versions.clear();
			if(ver!=null) {
				versions.put(fid, ver);
			}
		} else {
			incrementDBVersion(fid);
		}
	}
	public long getDBVersion(Long fid) {
		Long ver = versions.get(fid);
		return ver==null?0L:ver;
	}
	private SQLiteDatabase database;
	public SQLiteDatabase getDB(){return database;}
    
    public static final String TABLE_MARKS = "t1";

    public static String Key_ID = "lex"; //主键
    public static final String Date = "date"; //路径
    
    public String pathName;
	
	public final boolean testDBV2;
	
	public void setFavoriteFolderId(long favoriteFolderId) {
		this.favoriteFolderId = favoriteFolderId;
	}
	
	private long favoriteFolderId = -1;
	
	static LexicalDBHelper instance;
	boolean closed = false;
	
	/** 创建收藏夹数据库（从名称） */
    public LexicalDBHelper(Context context, PDICMainAppOptions opt, String name, boolean testDBV2) {
        super(context, opt.pathToFavoriteDatabase(name, testDBV2), null, CMN.dbVersionCode);
        DATABASE=name;
        this.testDBV2 = testDBV2;
		onConfigure();
    }

	/** 创建历史纪录数据库 */
	public LexicalDBHelper(Context context, PDICMainAppOptions opt, boolean testDBV2) {
		super(context, opt.pathToFavoriteDatabase(null, testDBV2), null, CMN.dbVersionCode);
		DATABASE="history.sql";
		this.testDBV2 = testDBV2;
		onConfigure();
	}
	
	void onConfigure() {
		database = getWritableDatabase();
		pathName = database.getPath();
		oldVersion=CMN.dbVersionCode;
		if(this.testDBV2)instance = this;
		else {
			String sql = "select rowid from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
			preparedGetIsFavoriteWord = database.compileStatement(sql);
		}
		
	}
	
	public static LexicalDBHelper getInstance() {
		return instance;
	}
	
	@Override
    public void onCreate(SQLiteDatabase db) {//第一次
		if (testDBV2) {
			String sqlBuilder;
			
//			db.execSQL("DROP TABLE IF EXISTS "+TABLE_BOOK_ANNOT_v2);
//			db.execSQL("DROP INDEX if exists bookannot_book_hash_index");
//			db.execSQL("DROP INDEX if exists bookannot_time_index");
			// TABLE_BOOK_ANNOT_v2 记录高亮标记
			sqlBuilder = "create table if not exists " +
					TABLE_BOOK_ANNOT_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", bid INTEGER NOT NULL"+
					", entryHash INTEGER NOT NULL" +
					", lexHash INTEGER NOT NULL" +
					", entryDig VARCHAR(5)" +
					", lexDig VARCHAR(5)" +
					", hasNotes BOOLEAN DEFAULT 0 NOT NULL" +
					", entry LONGVARCHAR" +
					", lex LONGVARCHAR" +
					", pos INTEGER NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_edit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					", param BLOB" +
					", notes BLOB" +
					")";
			db.execSQL(sqlBuilder);
			//db.execSQL("CREATE INDEX if not exists booknote_book_index ON bookannot (bid)");
			db.execSQL("CREATE INDEX if not exists bookannot_book_hash_index ON bookannot (bid, entryDig, entryHash, lexHash, lexDig, pos, hasNotes)"); // 查询，页面笔记视图
			db.execSQL("CREATE INDEX if not exists bookannot_time_index ON bookannot (bid, last_edit_time)"); // 词典笔记视图
			db.execSQL("CREATE INDEX if not exists bookannot_time_index ON bookannot (last_edit_time)"); // 全部笔记视图
//			db.execSQL("DROP INDEX if exists favorite_term_index");
//			db.execSQL("DROP INDEX if exists favorite_folder_index");
//			db.execSQL("DROP INDEX if exists booknote_term_index");
//			db.execSQL("DROP INDEX if exists booknote_book_index");
//			db.execSQL("DROP INDEX if exists booknote_time_index");
			
			//db.execSQL("DROP TABLE IF EXISTS "+TABLE_BOOK_NOTE_v2);
			//db.execSQL("DROP TABLE IF EXISTS "+TABLE_BOOKMARK_v2);
			// TABLE_BOOK_NOTE_v2 记录书签以及词条重写
			sqlBuilder = "create table if not exists " +
					TABLE_BOOK_NOTE_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", bid INTEGER NOT NULL"+
					", pos INTEGER NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_edit_time INTEGER NOT NULL" +
					", edit_count INTEGER DEFAULT 0 NOT NULL" +
					", miaoshu TEXT"+
					", param BLOB" +
					", notesType INTEGER DEFAULT 0" +
					", notes BLOB" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists booknote_term_index ON booknote (lex, bid, notesType)"); // query view | booknotes view1
			db.execSQL("CREATE INDEX if not exists booknote_book_index ON booknote (bid, last_edit_time, notesType)"); // booknotes view
			db.execSQL("CREATE INDEX if not exists booknote_time_index ON booknote (last_edit_time)"); // all view
			//db.execSQL("CREATE INDEX if not exists booknote_edit_index ON booknote (edit_count)"); // edit_count view
			
			
			// TABLE_BOOKMARK_v2 记录书签
//			sqlBuilder = "create table if not exists " +
//					TABLE_BOOKMARK_v2 +
//					"(" +
//					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
//					", bid INTEGER NOT NULL"+
//					", pos INTEGER NOT NULL" +
//					", param BLOB" +
//					", creation_time INTEGER NOT NULL"+
//					")";
//			db.execSQL(sqlBuilder);
//			db.execSQL("CREATE INDEX if not exists bkmk_index ON bookmark (bid, pos)");
//			db.execSQL("CREATE INDEX if not exists bkmk_time_index ON bookmark (creation_time)");
			
			
			// TABLE_BOOK_v2 记录书本
			sqlBuilder = "create table if not exists " +
					TABLE_BOOK_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", name LONGVARCHAR UNIQUE" +
					", path LONGVARCHAR"+
					", options BLOB"+
					", creation_time INTEGER DEFAULT 0 NOT NULL"+
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists book_name_index ON book (name)");
			
			
			// TABLE_FAVORITE_FOLDER_v2 记录收藏夹
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
			
			
			// TABLE_FAVORITE_v2 记录收藏的词条及笔记
			sqlBuilder = "create table if not exists " +
					TABLE_FAVORITE_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", books TEXT"+
					", src INTEGER DEFAULT 0 NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					", folder INTEGER DEFAULT 0 NOT NULL" +
					", level INTEGER DEFAULT 0 NOT NULL" +
					", notes LONGVARCHAR" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists favorite_term_index ON favorite (lex, folder)"); // query view
			//db.execSQL("CREATE INDEX if not exists favorite_level_index ON favorite (folder, level)");  // level view
			db.execSQL("CREATE INDEX if not exists favorite_folder_index ON favorite (folder, last_visit_time)"); // folder view
			//db.execSQL("CREATE INDEX if not exists favorite_filter_index ON favorite (folder, visit_count, level,last_visit_time)"); // filter view
			db.execSQL("CREATE INDEX if not exists favorite_time_index ON favorite (last_visit_time)"); // all view
			
			
			// TABLE_HISTORY_v2 记录历史
			sqlBuilder = "create table if not exists " +
					TABLE_HISTORY_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", books TEXT"+
					", src INTEGER DEFAULT 0 NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists history_term_index ON history (lex)"); // query view
			db.execSQL("CREATE INDEX if not exists history_time_index ON history (last_visit_time)"); // main view
			try {
				db.execSQL("CREATE INDEX if not exists history_visit_index ON history (visit_count, last_visit_time) where visit_count>0"); // visit_count view
			} catch (SQLException e) {
				CMN.debug(e);
			}
			
			// TABLE_HISTORY_v2 记录自定义数据
			//db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATA_v2);
			sqlBuilder = "create table if not exists " +
					TABLE_DATA_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", name LONGVARCHAR" +
					", data blob"+
					", len INTEGER DEFAULT 0 NOT NULL"+
					", type INTEGER DEFAULT 0 NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_edit_time INTEGER NOT NULL" +
					", edit_count INTEGER DEFAULT 0 NOT NULL" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists data_name_index ON data (name)");
			
			if (preparedHasBookNoteForEntry ==null) {
				preparedHasBookNoteForEntry = db.compileStatement("select id from " + TABLE_BOOK_NOTE_v2 + " where lex=? and bid=? and notesType>0");
				preparedGetBookNoteForEntry = db.compileStatement("select notes from "+TABLE_BOOK_NOTE_v2+" where lex=? and bid=? and notesType>0");
				preparedHasBookmarkForEntry = db.compileStatement("select id from "+TABLE_BOOK_NOTE_v2+" where lex=? and bid=?");
				preparedGetBookOptions = db.compileStatement("select options from "+TABLE_BOOK_v2+" where id=?");
				
				String sql = "select id from " + TABLE_FAVORITE_v2 + " where lex=?";
				preparedGetIsFavoriteWordInFolder = db.compileStatement(sql+" and folder=?");
				preparedGetIsFavoriteWord = db.compileStatement(sql);
			}
		} else {
			StringBuilder sqlBuilder = new StringBuilder("create table if not exists ")
					.append(TABLE_MARKS)
					.append("(")
					.append(Key_ID).append(" text PRIMARY KEY not null,")
					.append(Date).append(" integer")
					.append(")")
					;
			db.execSQL(sqlBuilder.toString());
			String sql = "select rowid from " + TABLE_MARKS + " where " + Key_ID + " = ? ";
			preparedGetIsFavoriteWord = db.compileStatement(sql);
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
			onCreate(db);
		}
    }

    /////
	/** has text term in the favorite table */
    public boolean GetIsFavoriteTerm(String lex, long folder) {
    	if (folder==-2) {
			folder = favoriteFolderId;
		}
    	if (folder>=0) {
    		// Get Is Favorite Term For Folder
			preparedGetIsFavoriteWordInFolder.bindString(1, lex);
			preparedGetIsFavoriteWordInFolder.bindLong(2, folder);
			try {
				//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
				preparedGetIsFavoriteWordInFolder.simpleQueryForLong();
				return true;
			} catch(Exception e) {
				//CMN.Log(e);
			}
		} else {
    		// for all
			preparedGetIsFavoriteWord.bindString(1, lex);
			try {
				//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
				preparedGetIsFavoriteWord.simpleQueryForLong();
				return true;
			} catch(Exception e) {
				//CMN.Log(e);
			}
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
	
	/** insert favorite text term
	 * @param lex the text term
	 * @param folder the favorite folder id.
	 * */
	public long insert(MainActivityUIBase a, String lex, long folder, ViewGroup webviewholder) {
    	CMN.Log("insert");
		isDirty=true;
		incrementFavVersion(folder);
		if (folder==-1) {
			folder = a.opt.getCurrFavoriteNoteBookId();
		}
		if (testDBV2) {
			int count=-1;
			try {
				long id=-1;
				String books = null;
				String[] where = new String[]{lex, ""+folder};
				boolean insertNew=true;
				Cursor c = database.rawQuery("select id,visit_count,books from "+TABLE_FAVORITE_v2+" where lex=? and folder=? ", where);

				if(c.moveToFirst()) {
					id = c.getLong(0);
					insertNew = false;
					count = c.getInt(1);
					books = c.getString(2);
				}
				
				c.close();
				
				ContentValues values = new ContentValues();
				values.put("lex", lex);
				
				values.put("books", a.collectDisplayingBooks(books, webviewholder));
				
				values.put("visit_count", ++count);
				
				long now = CMN.now();
				values.put("last_visit_time", now);
				
				if(insertNew) {
					values.put("folder", folder);
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
			long ret=-1;
			if(!GetIsFavoriteTerm(lex, -1)) {
				ret = database.insert(TABLE_MARKS, null, values);
			} else {
				ret = database.update(TABLE_MARKS, values, "lex =?", new String[]{lex});
			}
			return ret;
		}
		return -1;
	}
	
	/** remove favorite text term
	 * @param lex the text term
	 * @param folder the favorite folder id. if -1 then remove all
	 **/
	public int remove(String lex, long folder) {
		incrementFavVersion(folder);
		if(folder==-2) {
			folder = favoriteFolderId;
		}
		if (testDBV2) {
			if (folder<0) {
				return database.delete(TABLE_FAVORITE_v2, Key_ID + " = ? ", new String[]{lex});
			} else {
				return database.delete(TABLE_FAVORITE_v2, "lex=? and folder=?", new String[]{lex, ""+folder});
			}
		} else {
			return database.delete(TABLE_MARKS, Key_ID + " = ? ", new String[]{lex});
		}
	}

	public void refresh() {
		preparedGetIsFavoriteWord.close();
		if(isDirty) {
			//database.execSQL("CREATE UNIQUE INDEX idxmy ON "+TABLE_MDXES+" ("+NAME+"); ");
		}
	}
	
	
	/** insert history */
	public long insertUpdate(MainActivityUIBase a, String lex, ViewGroup webviewholder) {
		if (testDBV2) {
			return updateHistoryTerm(a, lex, webviewholder);
		} else {
			CMN.Log("insertUpdate");
			incrementDBVersion(-1L);
			long ret=-1;
			if(!GetIsFavoriteTerm(lex, -1)) {
				ret = insert(a, lex, 0, null);
			} else {
				ContentValues values = new ContentValues();
				values.put(Date, System.currentTimeMillis());
				ret = database.update("t1", values, "lex =?", new String[]{lex});
			}
			return ret;
		}
	}
	
	public long updateHistoryTerm (MainActivityUIBase a, String lex, ViewGroup webviewholder) {
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
			
			values.put("books", a.collectDisplayingBooks(books, webviewholder));
			
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
			incrementDBVersion(-1L);
		} catch (Exception e) {
			CMN.Log(e);
		}
		CMN.pt("历史插入时间：");
		return id;
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
		String path=fullPath;
		try {
			String[] where = new String[]{bookName};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id,path from book where name = ? ", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
				path = c.getString(1);
			}
			c.close();
			
			if(insertNew) {
				ContentValues values = new ContentValues();
				values.put("name", bookName);
				values.put("path", fullPath);
				values.put(FIELD_CREATE_TIME, CMN.now());
				id = database.insert(TABLE_BOOK_v2, null, values);
			} else if(path==null && fullPath!=null) {
//				ContentValues values = new ContentValues();
//				values.put("path", fullPath);
//				database.update(TABLE_BOOK_v2, values, "id=?", new String[]{""+id});
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return id;
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
		closed = true;
		if(preparedGetIsFavoriteWord !=null)
			preparedGetIsFavoriteWord.close();
	}
	
	public boolean isClosed() {
		return closed || database==null;
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
	
	public long ensureNoteBookByName(String name) {
		long ret;
		Cursor cursor = database.rawQuery("select id from favfolder where lex=?", new String[]{name});
		if (cursor.moveToNext()) {
			ret = cursor.getLong(0);
		} else {
			ret = newFavFolder(name);
			DBUpgradeHelper.favFolderUpdCnt++;
		}
		cursor.close();
		return ret;
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
	
	SQLiteStatement preparedHasBookNoteForEntry;
	SQLiteStatement preparedGetBookNoteForEntry;
	public SQLiteStatement preparedHasBookmarkForEntry;
	public SQLiteStatement preparedGetBookOptions;
	
	SQLiteStatement preparedGetIsFavoriteWord;
	SQLiteStatement preparedGetIsFavoriteWordInFolder;
	
	public String getPageString(long bid, String entry){
		ReusableByteOutputStream data = getPage_internal(bid, entry);
		if(data==null)
			return null;
		return new String(data.data(),0, data.size(), StandardCharsets.UTF_8);
	}
	
	public InputStream getPageStream(long bid, String url){
		ReusableByteOutputStream data = getPage_internal(bid, url);
		if(data==null)
			return null;
		return new ByteArrayInputStream(data.data(),0, data.size());
	}
	
	private ReusableByteOutputStream getPage_internal(long bid, String entry) {
		try {
			preparedGetBookNoteForEntry.bindString(1, entry);
			preparedGetBookNoteForEntry.bindLong(2, bid);
			ParcelFileDescriptor fd = preparedGetBookNoteForEntry.simpleQueryForBlobFileDescriptor();
			if(fd!=null) {
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
			}
		} catch (Exception e) {
			//CMN.Log(e);
		}
		return null;
	}
	
	public long containsPage(long bid, String entry) {
		preparedHasBookNoteForEntry.bindString(1, entry);
		preparedHasBookNoteForEntry.bindLong(2, bid);
		try {
			//Log.e("preparedSelectExecutor",preparedSelectExecutor.simpleQueryForString());
			return preparedHasBookNoteForEntry.simpleQueryForLong();
		}catch(Exception e){
			//CMN.Log(e);
		}
		return -1;
	}
	
	public long putPage(long bid, String url, long position, String name, String page) throws Exception {
		ContentValues values = new ContentValues();
		values.put(FIELD_ENTRY_NAME, url);
		values.put(FIELD_BOOK_ID, bid);
		values.put(FIELD_POSITION, position);
		//values.put(FIELD_ENTRY_NAME, title);
		long now = CMN.now();
		values.put(FIELD_EDIT_TIME, now);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream inf = new DeflaterOutputStream(out);
		byte[] data = page.getBytes();
		//inf.write("<!DOCTYPE html>\n".getBytes());
		inf.write(data,0, data.length);
		inf.finish();
		inf.close();
		data = out.toByteArray();
		if(data.length>1024*1024*2)
			throw new MdxDBHelper.DataTooLargeException("page too large!!!");
		
		values.put(FIELD_NOTES_STORAGE, 1);
		
		values.put(FIELD_NOTES, data);
		
		Cursor cursor = database.rawQuery("SELECT id,edit_count from "+TABLE_BOOK_NOTE_v2+" where bid=? and lex=?"
				, new String[]{url, ""+bid});
		long ret;
		if (cursor.moveToNext()) {
			values.put("edit_count", cursor.getInt(1)+1);
			ret = database.update(TABLE_BOOK_NOTE_v2, values, "id=?", new String[]{""+cursor.getLong(0)});
		} else {
			values.put(FIELD_CREATE_TIME, now);
			ret = database.insert(TABLE_BOOK_NOTE_v2, null, values);
		}
		cursor.close();
		return ret;
	}
	
	public void removePage(long bid, String entry){
		database.delete(TABLE_BOOK_NOTE_v2, "lex=? and bid=?", new String[]{entry, ""+bid});
	}
}
