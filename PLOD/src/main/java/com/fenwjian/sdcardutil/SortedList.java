package com.fenwjian.sdcardutil;

import android.util.Log;

public class SortedList {

  
    private Data first = null;  
      
    public void insert(long obj){  
        Data data = new Data(obj);  
        Data pre = null;  
        Data cur = first;  
      //只要未遍历至末尾，且新增项data项值>cur项值，下一一位
        while(cur != null && (data.obj >cur.obj)){  
            pre = cur;  
            cur = cur.next;  
        }  
        if(pre == null)  
            first = data;  
        else  
            pre.next = data;  
        data.next = cur;  
    }  
      
    public long deleteFirst() throws Exception{  
        if(first == null)  
            throw new Exception("empty!");  
        Data temp = first;  
        first = first.next;  
        return temp.obj;  
    }  
      
    public void display(){  
        if(first == null)  
            System.out.println("empty");  
        Log.i("logmy_sdcardUtil_sortedList", "empty");
        Log.i("logmy_sdcardUtil_sortedList","first -> last : ");  
        Data cur = first;  
        while(cur != null){  
        	Log.i("logmy_sdcardUtil_sortedList",Long.valueOf(cur.obj) + " -> ");  
            cur = cur.next;  
        }  
        Log.i("logmy_sdcardUtil_sortedList","\n");  
    }  
       
}  