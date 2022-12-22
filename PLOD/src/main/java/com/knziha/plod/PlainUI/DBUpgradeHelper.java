package com.knziha.plod.PlainUI;

import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.settings.SettingsActivity;

public class DBUpgradeHelper {
	/**
    欢迎来到数据库升级程序。
    本流程运行完成后，将迁移旧数据库中的数据至新的数据库，不会删除旧有的数据库。首次安装直接升级即可。
    取消后，随时可以在设置里开启此界面。
    新的数据库格式更加完善，支持更多功能，比如可以从历史记录或收藏夹追溯查词时的词典。
    旧版数据库（多个文件）：PLOD/INTERNAL/history.sql（历史记录）、PLOD/INTERNAL/favorites/*.sql（收藏夹）、PLOD/bmDBs/ .* / *.sql（词典数据）
    新版数据库（一个文件）：PLOD/INTERNAL/databaseV2.sql（统一数据库）*/
	
	public static void showUpgradeDlg(SettingsActivity dlgAct, MainActivityUIBase a, boolean tickStart) {
		a.showT("已经删除数据库v2升级程序，请使用v7.5以前的旧版进行升级。");
	}
	
	public static  int favFolderUpdCnt;
}
