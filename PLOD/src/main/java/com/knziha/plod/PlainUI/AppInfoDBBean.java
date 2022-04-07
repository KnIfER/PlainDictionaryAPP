package com.knziha.plod.PlainUI;

import static com.knziha.plod.db.LexicalDBHelper.TABLE_APPID_v2;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.knziha.paging.AppIconCover.AppInfoBean;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import org.apache.commons.imaging.common.BinaryOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

public class AppInfoDBBean extends AppInfoBean {
	final long appid;
	
	public AppInfoDBBean(long appid, PackageManager pm) {
		this.appid = appid;
		this.pm = pm;
	}
	
	public Drawable load() {
		Drawable ret = null;
		boolean save = PDICMainAppOptions.storeIcon();
		if (pkgName==null) {
			//CMN.rt("package::"+appid);
			Cursor c = LexicalDBHelper.getInstance().getDB().rawQuery("select "+(save?"name,title,icon":"name")+" from " + TABLE_APPID_v2 + " where id=? limit 1", new String[]{"" + appid});
			if(!c.moveToNext()) return null;
			pkgName = c.getString(0);
			if (save) {
				appName = c.getString(1);
				byte[] tmp = c.getBlob(2);
				try {
					if (tmp!=null)
						ret = new BitmapDrawable(BitmapFactory.decodeByteArray(tmp, 0, tmp.length));
				} catch (Exception e) {
					CMN.debug(e);
				}
				//CMN.pt("获取成功 package::"+ret);
			}
			c.close();
			//CMN.Log("package::"+pkgName);
		}
		if (data==null && (appName==null || ret==null)) {
			intent = new Intent();
			intent.setPackage(pkgName);
			List<ResolveInfo> rinfo = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
			if(rinfo.size()==0) {
				CMN.debug("app not found!");
				return null;
			}
			data = rinfo.get(0);
		}
		if(appName==null) {
			appName = data.loadLabel(pm).toString();
		}
		if(ret==null) {
			ret = super.load();
			if (save && ret!=null) {
				try {
					ContentValues cv = new ContentValues();
					Bitmap bm = ret instanceof BitmapDrawable?((BitmapDrawable) ret).getBitmap():null;
					if (bm==null) {
						bm = Bitmap.createBitmap(ret.getIntrinsicWidth(), ret.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
						ret.draw(new Canvas(bm));
					}
					ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
					bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
					cv.put("icon",  bos.toByteArray());
					cv.put("title", appName);
					LexicalDBHelper.getInstance().getDB().update(TABLE_APPID_v2, cv, "id=?", new String[]{"" + appid});
					//CMN.Log("保存成功 package::"+pkgName, bos.toByteArray().length);
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		}
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AppInfoDBBean that = (AppInfoDBBean) o;
		return Objects.equals(appid, that.appid);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(appid);
	}
}