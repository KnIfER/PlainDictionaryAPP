package com.knziha.plod.PlainUI;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.SwitchPreference;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.settings.SettingsActivity;

import org.knziha.metaline.Metaline;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.knziha.plod.db.LexicalDBHelper;

public class DBUpgradeHelper {
	
	/**
    欢迎来到数据库升级程序。
    本流程运行完成后，将迁移旧数据库中的数据至新的数据库，不会删除旧有的数据库。首次安装直接升级即可。
    取消后，随时可以在设置里开启此界面。
    新的数据库格式更加完善，支持更多功能，比如可以从历史记录或收藏夹追溯查词时的词典。
    旧版数据库（多个文件）：PLOD/INTERNAL/history.sql（历史记录）、PLOD/INTERNAL/favorites/*.sql（收藏夹）、PLOD/bmDBs/ .* / *.sql（词典数据）
    新版数据库（一个文件）：PLOD/INTERNAL/databaseV2.sql（统一数据库）*/
	@Metaline(trim=false)
	private final static String upgradeMsg = "";
	public final static int message = 20210823;
	
	public static void showUpgradeDlg(SettingsActivity dlgAct, MainActivityUIBase a, boolean tickStart) {
		//if(true) return;
		AlertDialog dlg = new AlertDialog.Builder(dlgAct==null?a:dlgAct)
				.setNeutralButton("倒计时5秒", null)
				.setNegativeButton("取消", null)
				.setPositiveButton("开始升级！", null)
				.setTitle("升级至数据库V2")
				.setMessage("升级至数据库V2")
				.show();
		dlg.setMessage(upgradeMsg);
		dlg.setCanceledOnTouchOutside(false);
		TextView tv = dlg.findViewById(android.R.id.message);
		//tv.setHorizontallyScrolling(true);
		AtomicInteger tickCnt = new AtomicInteger(16);
		AtomicBoolean abortTick = new AtomicBoolean(false);
		
		Button NeutralButton = dlg.findViewById(android.R.id.button3);
		Button NegativeButton = dlg.findViewById(android.R.id.button2);
		Button PositiveButton = dlg.findViewById(android.R.id.button1);
		
		Objects.requireNonNull(NeutralButton);
		Objects.requireNonNull(NegativeButton);
		Objects.requireNonNull(PositiveButton);
		
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				abortTick.set(true);
				if (v==NeutralButton) {
					NeutralButton.setVisibility(View.GONE);
				}
				else if (v==NegativeButton) {
					dlg.dismiss();
				}
				else if (v==PositiveButton) {
					if (TextUtils.equals(PositiveButton.getText(), "完成")) {
						dlg.dismiss();
						return;
					}
					historyUpdCnt = 0;
					favFolderUpdCnt = 0;
					favUpdCnt = 0;
					NeutralButton.setVisibility(View.GONE);
					NegativeButton.setVisibility(View.GONE);
					PositiveButton.setText("请等待...");
					dlg.setCancelable(false);
					if (dlgAct!=null) {
						SwitchPreference preference = dlgAct.fragment.findPreference("dbv2");
						if (preference!=null) {
							preference.setChecked(true);
							preference.setSummary(null);
						}
					}
					a.activateDataV2();
					//dlg.dismiss();
					String msg="";
					msg += "收录历史记录"+historyUpdCnt+"项\n";
					msg += "收录收藏夹"+favFolderUpdCnt+"项\n";
					msg += "收录词条收藏"+favUpdCnt+"项\n";
					if (new File(a.opt.pathToDatabases().toString()).exists()) {
						msg += "注：未迁移旧版词典笔记（包括重载页面和词典书签），如有需要请联系开发者。\n";
						msg += "\n建议彻底关闭进程后，重启应用；否则，直接使用可能存在未知的问题。\n";
					}
					dlg.setMessage(msg);
					PositiveButton.setText("完成");
					dlg.setCanceledOnTouchOutside(true);
					dlg.setCancelable(true);
				}
			}
		};
		
		NeutralButton.setOnClickListener(onClickListener);
		NegativeButton.setOnClickListener(onClickListener);
		PositiveButton.setOnClickListener(onClickListener);
		
		if (!tickStart) {
			NeutralButton.setVisibility(View.GONE);
		} else {
			NeutralButton.post(new Runnable() {
				@Override
				public void run() {
					int left = tickCnt.decrementAndGet();
					if (left>0) {
						if (!abortTick.get()) {
							NeutralButton.postDelayed(this, 1000);
							NeutralButton.setText("倒计时"+left+"秒");
						}
					} else {
						NeutralButton.setVisibility(View.GONE);
						if(!abortTick.get()) PositiveButton.performClick();
					}
				}
			});
		}
	}
	
	public static  int favFolderUpdCnt;
	static  int favUpdCnt;
	static  int historyUpdCnt;
	
	public static void upgradeFavToFavFolder(LexicalDBHelper favCon, LexicalDBHelper dbCon, long folder) {
		SQLiteDatabase fromDB = favCon.getReadableDatabase();
		SQLiteDatabase toDB = dbCon.getWritableDatabase();
		//SQLiteDatabase toDB = dbCon.getDB();
		Cursor cursor = fromDB.rawQuery("select lex,date from t1", null);
		if (cursor.getCount()>0)
		{
			String sql2 = "INSERT INTO "+LexicalDBHelper.TABLE_FAVORITE_v2
					+"(lex, "+LexicalDBHelper.FIELD_FOLDER+", "+LexicalDBHelper.FIELD_VISIT_TIME+", "+LexicalDBHelper.FIELD_CREATE_TIME+") VALUES(?,?,?,?)";
			SQLiteStatement preparedInsertExecutor = toDB.compileStatement(sql2);
			SQLiteStatement preparedQueryExecutor = toDB.compileStatement("SELECT id FROM favorite WHERE lex=? AND folder=?");
			toDB.beginTransaction();  //开启事务
			try {
				while(cursor.moveToNext()) {
					String lex=cursor.getString(0);
					long date=cursor.getLong(1);
					preparedQueryExecutor.bindString(1, lex);
					preparedQueryExecutor.bindLong(2, folder);
					long alreadyHas = -1;
					try {
						alreadyHas = preparedQueryExecutor.simpleQueryForLong();
					} catch (Exception ignored) { }
					if (alreadyHas==-1) {
						preparedInsertExecutor.bindString(1, lex);
						preparedInsertExecutor.bindLong(2, folder);
						preparedInsertExecutor.bindLong(3, date);
						preparedInsertExecutor.bindLong(4, date);
						alreadyHas = preparedInsertExecutor.executeInsert();
						if (alreadyHas!=-1) {
							favUpdCnt++;
						}
						CMN.Log("激活DBV2::插入收藏夹", lex, folder, alreadyHas);
					}
				}
				toDB.setTransactionSuccessful();
			} catch (Exception e) {
				CMN.Log(e);
			}
			toDB.endTransaction();  //结束事务
		}
		cursor.close();
	}
	
	public static void upgradeHistoryToDBV2(LexicalDBHelper favCon, LexicalDBHelper dbCon) {
		SQLiteDatabase fromDB = favCon.getReadableDatabase();
		SQLiteDatabase toDB = dbCon.getWritableDatabase();
		//SQLiteDatabase toDB = dbCon.getDB();
		Cursor cursor = fromDB.rawQuery("select lex,date from t1", null);
		if (cursor.getCount()>0)
		{
			String sql2 = "INSERT INTO "+LexicalDBHelper.TABLE_HISTORY_v2
					+"(lex, "+LexicalDBHelper.FIELD_VISIT_TIME+", "+LexicalDBHelper.FIELD_CREATE_TIME+") VALUES(?,?,?)";
			SQLiteStatement preparedInsertExecutor = toDB.compileStatement(sql2);
			SQLiteStatement preparedQueryExecutor = toDB.compileStatement("SELECT id FROM history WHERE lex=?");
			toDB.beginTransaction();  //开启事务
			try {
				while(cursor.moveToNext()) {
					String lex=cursor.getString(0);
					long date=cursor.getLong(1);
					preparedQueryExecutor.bindString(1, lex);
					long alreadyHas = -1;
					try {
						alreadyHas = preparedQueryExecutor.simpleQueryForLong();
					} catch (Exception ignored) { }
					if (alreadyHas==-1) {
						preparedInsertExecutor.bindString(1, lex);
						preparedInsertExecutor.bindLong(2, date);
						preparedInsertExecutor.bindLong(3, date);
						alreadyHas = preparedInsertExecutor.executeInsert();
						if (alreadyHas!=-1) {
							historyUpdCnt++;
						}
						CMN.Log("激活DBV2::插入历史记录", lex, alreadyHas);
					}
				}
				toDB.setTransactionSuccessful();
			} catch (Exception e) {
				CMN.Log(e);
			}
			toDB.endTransaction();  //结束事务
		}
		cursor.close();
	}
	
	
}
