package com.knziha.plod.db;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.knziha.plod.PlainUI.DBUpgradeHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.plaindict.BuildConfig;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.VersionUtils;
import com.knziha.plod.plaindict.WebViewListHandler;

import org.knziha.metaline.Metaline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class LexicalDBHelper extends SQLiteOpenHelper {
	public static final String TABLE_HISTORY_v2 = "history";
	public static final String TABLE_BOOK_v2 = "book";
	//public static final String TABLE_BOOKMARK_v2 = "bookmark";
	public static final String TABLE_FAVORITE_v2 = "favorite";
	public static final String TABLE_FAVORITE_FOLDER_v2 = "favfolder";
	/** 记录书签以及词条重写 */
	public static final String TABLE_BOOK_NOTE_v2 = "booknote";
	/** 记录页面上的文本标记与笔记附注 */
	public static final String TABLE_BOOK_ANNOT_v2 = "bookmarks";  // 表 bookannot 废弃
	public static final String TABLE_DATA_v2 = "data";
	public static final String TABLE_APPID_v2 = "appid";
	
	public static final String FIELD_CREATE_TIME = "creation_time";
	public static final String FIELD_VISIT_TIME = "last_visit_time";
	public static final String FIELD_EDIT_TIME = "last_edit_time";
	public static final String FIELD_ENTRY_NAME = "lex";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_NOTES_STORAGE = "notesType";
	public static final String FIELD_NOTES = "notes";
	public static final String FIELD_FOLDER = "folder";
	public static final String FIELD_BOOK_ID = "bid";
	public static final String FIELD_POSITION = "pos";
	public static final String FIELD_PARAMETERS = "param";
	
    public final String DATABASE;
	private final AssetManager assets;
	HashMap<Long, Long> versions = new HashMap<>();
	public boolean lastAdded;
	void incrementDBVersion(Long fid) {
		Long ver = versions.get(fid);
		versions.put(fid, (ver==null?1L:(ver+1)));
	}
	void increaseFavVersion(Long fid) {
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
	byte[] Hzh;
	
	/** 创建收藏夹数据库（从名称） */
    public LexicalDBHelper(Context context, PDICMainAppOptions opt, String name, boolean testDBV2) {
        super(context, opt.pathToFavoriteDatabase(name, testDBV2), null, CMN.dbVersionCode);
        DATABASE=name;
        this.testDBV2 = testDBV2;
		this.assets = context.getAssets();
		onConfigure();
    }

	/** 创建历史纪录数据库 */
	public LexicalDBHelper(Context context, PDICMainAppOptions opt, boolean testDBV2) {
		super(context, opt.pathToFavoriteDatabase(null, testDBV2), null, CMN.dbVersionCode);
		DATABASE="history.sql";
		this.testDBV2 = testDBV2;
		this.assets = context.getAssets();
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
	
	
	private static boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
		String query;
		try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
			while (cursor.moveToNext()) {
				query = cursor.getString(cursor.getColumnIndex("name"));
				// CMN.debug("columnExists::", query);
				if (columnName.equals(query)) {
					return true;
				}
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return false;
	}
	
	
	@Override
    public void onCreate(SQLiteDatabase db) {//第一次
		if (testDBV2) {
			String sqlBuilder;
			
//			db.execSQL("DROP TABLE IF EXISTS "+TABLE_BOOK_ANNOT_v2); //hot
//			db.execSQL("DROP INDEX if exists bookannot_book_hash_index");
//			db.execSQL("DROP INDEX if exists bookannot_time_index");
			// TABLE_BOOK_ANNOT_v2 记录高亮标记
if (!VersionUtils.AnnotOff) {
			String tableBookAnnotv2 = TABLE_BOOK_ANNOT_v2;
			sqlBuilder = "create table if not exists " +
					tableBookAnnotv2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", bid INTEGER NOT NULL"+
					", pos INTEGER NOT NULL"+
					", tPos INTEGER DEFAULT 0"+ // 文本位置，用于排序
					", web INTEGER DEFAULT 0" + /** 用于标识web网页，见 {@link com.knziha.plod.PlainUI.AnnotAdapter.AnnotationReader#web} */
					", entry LONGVARCHAR" +
					", lex LONGVARCHAR" +
					", url LONGVARCHAR" + // 在线页面的标记  获取虚拟pos
					", annot TEXT"+ // 标注json，用于恢复高亮/下划线标记
					", type INTEGER"+ // 下划线/高亮
					", noteType INTEGER"+ // 正文/气泡/脚注
					", color INTEGER"+
					", hue INTEGER"+ // 主色，暂未使用
					", notes LONGVARCHAR"+ // 附注笔记
					", creation_time INTEGER NOT NULL"+
					", last_edit_time INTEGER NOT NULL" + // 零代表书签记录的备份，目前仅用于在线页面的标记
					", edit_count INTEGER DEFAULT 0 NOT NULL" + // 是书签时代表 mark_count
					", param BLOB" + // 其他页面信息，暂未使用
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists bookmarks_bpt_index ON "+ tableBookAnnotv2 +" (bid, pos, last_edit_time, id)"); // 页面笔记视图
			db.execSQL("CREATE INDEX if not exists bookmarks_bpp_index ON "+ tableBookAnnotv2 +" (bid, pos, tPos, last_edit_time, id)"); // 页面笔记视图
			db.execSQL("CREATE INDEX if not exists bookmarks_book_index ON "+ tableBookAnnotv2 +" (bid, last_edit_time, id)"); // 词典笔记视图
			db.execSQL("CREATE INDEX if not exists bookmarks_time_index ON "+ tableBookAnnotv2 +" (last_edit_time, bid, pos, id)"); // 全部笔记视图
			try { // 部分索引
				db.execSQL("CREATE INDEX if not exists bookmarks_bkmk_partial ON "+ tableBookAnnotv2 +" (url,bid) where url!=0 or last_edit_time==0"); // 书签查询
				// 还是把书签和页面标记分表记录吧…… db.execSQL("CREATE INDEX if not exists bookmarks_bkmk_partial_1 ON "+ tableBookAnnotv2 +" (last_edit_time, bid, pos, id) where url!=0 or last_edit_time==0"); // 书签查询
			} catch (SQLException e) {
				db.execSQL("CREATE INDEX if not exists bookmarks_bkmk_index ON "+ tableBookAnnotv2 +" (url)");
				CMN.debug(e);
			}
}

			// TABLE_BOOK_NOTE_v2 记录书签以及词条重写
			sqlBuilder = "create table if not exists " +
					TABLE_BOOK_NOTE_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", lex LONGVARCHAR" +
					", bid INTEGER NOT NULL"+
					", pos INTEGER NOT NULL"+ // 用于列表定位
					", creation_time INTEGER NOT NULL"+
					", last_edit_time INTEGER NOT NULL" +
					", edit_count INTEGER DEFAULT 0 NOT NULL" +
					", miaoshu TEXT"+
					", param BLOB" +
					", notesType INTEGER DEFAULT 0" +
					", notes BLOB" +
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("DROP INDEX IF EXISTS booknote_book_index");
			db.execSQL("DROP INDEX IF EXISTS booknote_time_index");
			db.execSQL("CREATE INDEX if not exists booknote_term_index ON booknote (lex, bid, notesType)"); // query view | booknotes view1
			db.execSQL("CREATE INDEX if not exists booknote_book_index1 ON booknote (bid, last_edit_time, id, notesType)"); // booknotes view
			db.execSQL("CREATE INDEX if not exists booknote_time_index1 ON booknote (last_edit_time, bid, pos, id)"); // all view
			db.execSQL("CREATE INDEX if not exists booknote_pos_index1 ON booknote (bid, pos, id)"); // list decoration view
			//db.execSQL("CREATE INDEX if not exists booknote_edit_index ON booknote (edit_count)"); // edit_count view
			
			
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
					", grp INTEGER DEFAULT 0 NOT NULL"+
					", ivk INTEGER DEFAULT 0 NOT NULL"+
					", hid INTEGER DEFAULT 0 NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					", folder INTEGER DEFAULT 0 NOT NULL" +
					", level INTEGER DEFAULT 0 NOT NULL" +
					", notes LONGVARCHAR" +
					")";
			db.execSQL(sqlBuilder);
			if (!columnExists(db, TABLE_FAVORITE_v2, "src")) {
				db.execSQL("ALTER TABLE "+TABLE_FAVORITE_v2+" ADD COLUMN src INTEGER DEFAULT 0 NOT NULL");
			}
			if (!columnExists(db, TABLE_FAVORITE_v2, "grp")) {
				db.execSQL("ALTER TABLE "+TABLE_FAVORITE_v2+" ADD COLUMN grp INTEGER DEFAULT 0 NOT NULL");
				db.execSQL("ALTER TABLE "+TABLE_FAVORITE_v2+" ADD COLUMN ivk INTEGER DEFAULT 0 NOT NULL");
			}
			if (!columnExists(db, TABLE_FAVORITE_v2, "hid")) {
				db.execSQL("ALTER TABLE "+TABLE_FAVORITE_v2+" ADD COLUMN hid INTEGER DEFAULT 0 NOT NULL");
			}
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
					", grp INTEGER DEFAULT 0 NOT NULL"+
					", ivk INTEGER DEFAULT 0 NOT NULL"+
					", creation_time INTEGER NOT NULL"+
					", last_visit_time INTEGER NOT NULL" +
					", visit_count INTEGER DEFAULT 0 NOT NULL" +
					")";
			db.execSQL(sqlBuilder);
			if (!columnExists(db, TABLE_HISTORY_v2, "src")) {
				db.execSQL("ALTER TABLE "+TABLE_HISTORY_v2+" ADD COLUMN src INTEGER DEFAULT 0 NOT NULL");
			}
			if (!columnExists(db, TABLE_HISTORY_v2, "grp")) {
				db.execSQL("ALTER TABLE "+TABLE_HISTORY_v2+" ADD COLUMN grp INTEGER DEFAULT 0 NOT NULL");
				db.execSQL("ALTER TABLE "+TABLE_HISTORY_v2+" ADD COLUMN ivk INTEGER DEFAULT 0 NOT NULL");
			}
			db.execSQL("CREATE INDEX if not exists history_term_index ON history (lex, ivk)"); // query view
			db.execSQL("DROP INDEX if exists history_time_index"); // main view
			db.execSQL("CREATE INDEX if not exists history_time_index_1 ON history (last_visit_time, visit_count, src)"); // main view
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
			
			sqlBuilder = "create table if not exists " +
					TABLE_APPID_v2 +
					"(" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT" +
					", name TEXT" +
					", title TEXT" +
					", icon blob" +
					", creation_time INTEGER NOT NULL"+
					")";
			db.execSQL(sqlBuilder);
			db.execSQL("CREATE INDEX if not exists app_name_index ON appid (name)");
			
			if (preparedHasBookNoteForEntry ==null) {
				preparedHasBookNoteForEntry = db.compileStatement("select id from " + TABLE_BOOK_NOTE_v2 + " where lex=? and bid=? and notesType>0");
				preparedGetBookNoteForEntry = db.compileStatement("select notes from "+TABLE_BOOK_NOTE_v2+" where lex=? and bid=? and notesType>0");
				preparedHasBookmarkForEntry = db.compileStatement("select id from "+TABLE_BOOK_NOTE_v2+" where lex=? and bid=?");
				preparedHasWebBookmarkForEntry = db.compileStatement("select id from "+TABLE_BOOK_NOTE_v2+" where lex=?");
				
				preparedGetBookOptions = db.compileStatement("select options from "+TABLE_BOOK_v2+" where id=?");
				preparedBookIdChecker = db.compileStatement("select id from "+TABLE_BOOK_v2+" where id=?");
				
				String sql = "select id from " + TABLE_FAVORITE_v2 + " where lex=?";
				preparedGetIsFavoriteWordInFolder = db.compileStatement(sql+" and folder=?");
				preparedGetIsFavoriteWord = db.compileStatement(sql);
				
if(!VersionUtils.AnnotOff)
				try {
					preparedHasBookmarkForUrl = db.compileStatement("select id from "+ TABLE_BOOK_ANNOT_v2 +" where url=? and bid=?");
					preparedGetBookmarkForPos = db.compileStatement("select id from "+ TABLE_BOOK_ANNOT_v2 +" where bid=? and pos=? and last_edit_time=?");
					predictNextBookmarkId = db.compileStatement("select seq from SQLITE_SEQUENCE where name=?");
					predictNextBookmarkId.bindString(1, TABLE_BOOK_ANNOT_v2);
					preparedGetBookmarkForPos.bindLong(3, 0);
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
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
    public boolean GetIsFavoriteTerm(@NonNull String lex, long folder) {
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
	public long insert(MainActivityUIBase a, String lex, long folder, WebViewListHandler weblist) {
    	//CMN.debug("insert");
		isDirty=true;
		increaseFavVersion(folder);
		if (folder==-1) {
			folder = a.opt.getCurrFavoriteNoteBookId();
		}
		if (testDBV2) {
			int count=-1;
			try {
				long id=-1;
				long hid=-1, ivkAppId = -1;
				String books = null;
				String[] where = new String[]{lex, ""+folder};
				boolean insertNew=true;
				Cursor c = database.rawQuery("select id,ivk from "+TABLE_HISTORY_v2+" where lex=?", new String[]{lex});
				if(c.moveToFirst()) {
					try {
						hid = c.getLong(0);
						ivkAppId = c.getLong(1);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
				c.close();
				c = database.rawQuery("select id,visit_count,books from "+TABLE_FAVORITE_v2+" where lex=? and folder=? ", where);
				if(c.moveToFirst()) {
					id = c.getLong(0);
					insertNew = false;
					count = c.getInt(1);
					try {
						books = c.getString(2);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
				
				c.close();
				
				ContentValues values = new ContentValues();
				values.put("lex", lex);
				
				if (weblist!=null) {
					values.put("books", a.collectDisplayingBooks(books, weblist));
				}
				
				values.put("visit_count", ++count);
				
				long now = CMN.now();
				values.put("last_visit_time", now);
				
				if (hid>=0) values.put("hid", hid);
				
				if (ivkAppId>=0) values.put("ivk", ivkAppId);
				
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
		increaseFavVersion(folder);
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
	@Deprecated
	public long insertUpdate(MainActivityUIBase a, String lex, WebViewListHandler weblist) {
		if (testDBV2) {
			return updateHistoryTerm(a, lex, 0, weblist);
		} else {
			//CMN.debug("insertUpdate");
			incrementDBVersion(-1L);
			long ret=-1;
			if(!GetIsFavoriteTerm(lex, -1)) {
				ret = insert(a, lex, 0, weblist);
			} else {
				ContentValues values = new ContentValues();
				values.put(Date, System.currentTimeMillis());
				ret = database.update("t1", values, "lex =?", new String[]{lex});
			}
			return ret;
		}
	}
	
	
	/**
	 * @param source 0=default; 1=listview; 2=tap translator; 3=peruse view
	 *   */
	public long updateHistoryTerm(MainActivityUIBase a, String lex, int source, WebViewListHandler weblist) {
//		if (BuildConfig.DEBUG) {
//			try {
//				throw new RuntimeException();
//			} catch (RuntimeException e) {
//				CMN.debug("updateHistoryTerm::", e);
//			}
//		}
		CMN.rt();
		int count=-1;
		int src=0;
		long id=-1;
		try {
			String books = null;
			String[] where = new String[]{lex};
			boolean insertNew=true;
			Cursor c = database.rawQuery("select id,visit_count,books,src from history where lex = ? ", where);
			if(c.moveToFirst()) {
				insertNew = false;
				id = c.getLong(0);
				count = c.getInt(1);
				books = c.getString(2);
				src = c.getInt(3);
			}
			c.close();
			
			ContentValues values = new ContentValues();
			values.put(FIELD_ENTRY_NAME, lex);
			
			if (weblist!=null) {
				values.put("books", a.collectDisplayingBooks(books, weblist));
			}
			long ivkAppId = -1;
			String ivk = a.extraInvoker;
			if(ivk!=null && lex.equals(a.extraText)) {
				where[0] = ivk;
				c = database.rawQuery("select id from "+TABLE_APPID_v2+" where name = ? ", where);
				if (c.moveToNext())
					ivkAppId = c.getLong(0);
				c.close();
				if(ivkAppId==-1) {
					ContentValues value = new ContentValues();
					value.put(FIELD_NAME, ivk);
					value.put(FIELD_CREATE_TIME, CMN.now());
					ivkAppId = database.insert(TABLE_APPID_v2, null, value);
					if(ivkAppId==0) {
						where[0] = ""+0;
						value.put(FIELD_NAME, "empty");
						database.update(TABLE_APPID_v2, value, "id = ?", where);
						value.put(FIELD_NAME, ivk);
						ivkAppId = database.insert(TABLE_APPID_v2, null, value);
					}
				}
				//CMN.Log("updateHistoryTerm::ivk::", ivk, ivkAppId);
				a.extraInvoker = null;
			}
			if (ivkAppId!=-1) {
				values.put("ivk", ivkAppId);
			}
			values.put("visit_count", ++count);
			values.put("src", src|source);
			long now = CMN.now();
			values.put(FIELD_VISIT_TIME, now);
			if(insertNew) {
				values.put(FIELD_CREATE_TIME, now);
				id = database.insert(TABLE_HISTORY_v2, null, values);
//				CMN.debug("database.insert："+id);
			} else {
				//values.put("id", id);
				//database.update(TABLE_URLS, values, "url=?", where);
				//database.insertWithOnConflict(TABLE_URLS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
				where[0]=""+id;
				int cnt = database.update(TABLE_HISTORY_v2, values, "id=?", where);
//				CMN.debug("database.update："+cnt);
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
				id = GenIdStr(bookName);
				if (id>0) {
					values.put("id", id);
				}
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
	
	
	/** 生成具有一定确定性的ID */
	private long GenIdStr(String nameKey) {
		long ret=-1;
		try {
			int idx = nameKey.lastIndexOf(".");
			if(idx>0) nameKey = nameKey.substring(0, idx);
			nameKey = nameKey.replaceAll("\\(.*?\\)", "");
			nameKey = nameKey.replaceAll("\\[.*?\\]", "");
			nameKey = nameKey.replaceAll("\\{.*?\\}", "");
			StringBuilder keyName = new StringBuilder();
			int i = 0, len=nameKey.length();
			for (; i < len; i++) {
				char ch = nameKey.charAt(i);
				if (ch>=0x4e00&&ch<=0x9fa5) {
					keyName.append((char)getHzh()[ch-0x4e00]);
					if(keyName.length()>=4) {i++;break;}
				}
				else if (ch>='A'&&ch<='Z') {
					keyName.append(ch);
					if(keyName.length()>=4) {i++;break;}
				}
			}
			boolean b1=TextUtils.regionMatches(nameKey, 0, keyName, 0, Math.min(3, keyName.length()))
					|| len<17;
			if (b1) {
				final int max = 9/*至多九位，争取最大特征*/;
				for (; i < len; i++) {
					char ch = nameKey.charAt(i);
					if (ch >= 0x4e00 && ch <= 0x9fa5) {
						keyName.append((char) getHzh()[ch - 0x4e00]);
						if (keyName.length() >= max) break;
					} else if (ch >= 'A' && ch <= 'Z'||ch >= 'a' && ch <= 'z'||ch >= '0' && ch <= '9') {
						keyName.append(ch);
						if (keyName.length() >= max) break;
					}
				}
			}
			b1 = !b1 || keyName.length() < len/2/*给你机会你…*/;
			if(b1){ // 至多八位
				keyName.append(len%10);
				IU.NumberToText_SIXTWO_LE(Math.abs(nameKey.hashCode())% 0x800, keyName);
				if(keyName.length()>8) keyName.setLength(8);
			}
			/*防止重复*/
			int cc=0;
			len=keyName.length();
			int max = (int) (Math.pow(62,10-len)-1);
			while (true) {
				ret = IU.TextToNumber_SIXTWO_LE(keyName);
				preparedBookIdChecker.bindLong(1, ret);
				try {
					preparedBookIdChecker.simpleQueryForLong();
				} catch (Exception e) {
					// 不存在，生成的ID可用
					break;
				}
				// 计数累加，重新生成新的ID
				if (++cc>max) {
					// 超出最大计数
					if (len == 9) {
						len = 8;
						cc = 0;
					} else {
						ret = -1;
						break;
					}
				}
				keyName.setLength(len);
				IU.NumberToText_SIXTWO_LE(cc, keyName);
			}
			// CMN.debug("keyName::", keyName);
		} catch (Exception e) {
			CMN.debug(e);
		}
		return ret;
	}
	
	@Metaline(file = "src\\main\\assets\\Hzh.dat")
	final static int Hzh_filesize = 0;

	/** 获取常规汉字的拼音首字母 */
	public byte[] getHzh(){
		if (Hzh==null) {
			try {
				InputStream input = assets.open("Hzh.dat");
				Hzh = new byte[Hzh_filesize];
				input.read(Hzh);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return Hzh;
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
			CMN.debug(e);
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
	
	/** Predict next auto-inserted row id */
	public SQLiteStatement predictNextBookmarkId;
	SQLiteStatement preparedHasBookNoteForEntry;
	SQLiteStatement preparedGetBookNoteForEntry;
	public SQLiteStatement preparedHasBookmarkForEntry;
	public SQLiteStatement preparedHasBookmarkForUrl;
	public SQLiteStatement preparedGetBookmarkForPos;
	public SQLiteStatement preparedHasWebBookmarkForEntry;
	public SQLiteStatement preparedGetBookOptions;
	public SQLiteStatement preparedBookIdChecker;
	
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
		
		Cursor cursor = database.rawQuery("SELECT id,edit_count from "+TABLE_BOOK_NOTE_v2+" where lex=? and bid=?"
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
	
	public static long annotDbVer;
	public static long increaseAnnotDbVer() {
		return annotDbVer++;
	}
	
}
