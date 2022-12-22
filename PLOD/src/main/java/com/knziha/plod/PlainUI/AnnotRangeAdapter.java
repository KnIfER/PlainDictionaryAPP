package com.knziha.plod.PlainUI;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.LongSparseArray;
import android.view.View;

import com.knziha.paging.ConstructorInterface;
import com.knziha.paging.CursorReader;
import com.knziha.paging.PagingAdapterInterface;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.plaindict.CMN;

import java.util.ArrayList;
import java.util.Arrays;

public class AnnotRangeAdapter<T extends CursorReader> implements PagingAdapterInterface<T> {
	final SQLiteDatabase database;
	final ArrayList<String> rowIds;
	final ConstructorInterface<T> readerMaker;
	final LongSparseArray<T> readers;
	
	public AnnotRangeAdapter(SQLiteDatabase database, ConstructorInterface<T> readerMaker, String[] rowIds) {
		this.database = database;
		this.rowIds = new ArrayList<>(Arrays.asList(rowIds));
		this.readerMaker = readerMaker;
		this.readers = new LongSparseArray<>(rowIds.length);
	}
	
	@Override
	public int getCount() {
		return rowIds.size();
	}
	
	@Override
	public T getReaderAt(int position, boolean triggerPaging) {
		long id = IU.parseLong(rowIds.get(position), -1);
		if (id != -1) {
			T ret = readers.get(id);
			if (ret == null) {
				Exception exception = null;
				Cursor cursor = null;
				try {
					cursor = database.rawQuery("select "+AnnotAdapter.data_fields+" from " + LexicalDBHelper.TABLE_BOOK_ANNOT_v2 + " where id=? limit 1", new String[]{"" + id});
					if (cursor.moveToNext()) {
						ret = readerMaker.newInstance(0);
						ret.ReadCursor(this, cursor, id, 0);
						readers.put(id, ret);
					}
				} catch (Exception e) {
					CMN.debug(exception=e);
				}
				if (cursor != null) {
					cursor.close();
				}
				if (exception != null) {
					throw new RuntimeException(exception);
				}
			}
			return ret;
		} else {
			throw new IllegalArgumentException(position+"::"+this.getClass());
		}
	}
	
	@Override
	public void close() {
	}
	
	public void growUp(View recyclerView) {
	}
	
	@Override
	public void recheckBoundary() {
	
	}
	
	@Override
	public void recheckBoundaryAt(int i, boolean start) {
	
	}
	
	public void deleteAt(int position) {
		if (position>=0 && position<rowIds.size()) {
			long id = IU.parseLong(rowIds.get(position), -1);
			rowIds.remove(position);
			readers.remove(id);
		}
	}
	
	public boolean getTopReached() {
		return true;
	}
	
	public int getPageIdx(int position) {
		return 0;
	}
}