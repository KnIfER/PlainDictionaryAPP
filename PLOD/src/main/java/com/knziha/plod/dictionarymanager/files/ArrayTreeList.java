package com.knziha.plod.dictionarymanager.files;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class ArrayTreeList<T> {
	//wonderful!

	protected final ArrayList<T> data;
	protected final Comparator<? super T> mComparator;
	//boolean isdirty=false;

	public ArrayTreeList(Comparator<? super T> _Comparator){
		data = new  ArrayList<>();
		mComparator = _Comparator;
	}
	
	public int insert(T val){
		if(data.size()==0 || mComparator.compare(data.get(data.size()-1), val)<0) {//!!!不允许重复
			data.add(data.size(),val);
			return data.size();
		}
		int idx=-1;
		if(mComparator.compare(data.get(0), val)>=0)
			idx=0;
		else
			idx = reduce(val,0,data.size());
		
		if(mComparator.compare(val, data.get(idx))==0) {//不允许重复 GOOD
			//isdirty=true;
			return -1;
		}
		data.add(idx,val);
		return idx;
	}
	
	public int reduce(T val,int start,int end) {//via mdict-js
        int len = end-start;
        if (len > 1) {
          len = len >> 1;
          return mComparator.compare(val, data.get(start + len - 1))>0
                    ? reduce(val,start+len,end)
                    : reduce(val,start,start+len);
        } else {
          return start;
        }
    }
	

	public int getCountOf(T key) {
		if(data.size()==0 || mComparator.compare(data.get(data.size()-1), key)<0) {
			return 0;
		}
		int idx = reduce(key,0,data.size());
		int cc=0;
		if(mComparator.compare(key, data.get(idx))==0) {
			cc++;
			while(idx<data.size()-1 && mComparator.compare(key, data.get(idx+1))==0) {
				idx++;cc++;
			}
		}
		return cc;
	}

	public Integer size() {
		return data.size();
	}

	public void add(T val) {
		data.add(val);
	}

	public ArrayList<T> getList() {
		return data;
	}
	
	public final HashSet<T> OverFlow = new HashSet<>();
	public boolean insertOverFlow(T val) {
		if(OverFlow.contains(val)) {//用于文件夹式管理
			insert(val);
			return true;
		}else{
			OverFlow.add(val);
			return false;
		}
	}

	public boolean contains(T val) {
		if(data.size()==0) return false;
		int idx = reduce(val,0,data.size());
		if(idx==-1) return false;
		if(mComparator.compare(val, data.get(idx))==0) {
			return true;
		}
		return false;
	}

	public T get(T val) {
		if(val==null || data.size()==0) return null;
		int idx = reduce(val,0,data.size());
		if(idx==-1) return null;
		if(mComparator.compare(val, data.get(idx))==0) {
			return data.get(idx);
		}
		return null;
	}

	public int indexOf(T val) {//默认 严格模式 GOOD
		if(data.size()==0) return -1;
		int idx = reduce(val,0,data.size());
		if(idx==-1) return idx;
		if(mComparator.compare(val, data.get(idx))==0) {
			return idx;
		}
		return -1;
	}

	public int remove(T val) {
		if(data.size()==0) return -1;
		int idx = reduce(val,0,data.size());
		if(idx==-1) return -1;
		if(mComparator.compare(val, data.get(idx))==0) {
			data.remove(idx);
			return idx;
		}
		return -1;
	}

	public void clear() {
		data.clear();
	}
	
	
	
}
