package com.knziha.plod.PlainUI;

import com.knziha.plod.plaindict.CMN;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WordPopupTask implements Runnable {
	final WeakReference<WordPopup> wordPopupRef;
	final static int TASK_POP_SCH=0;
	final static int TASK_POP_NAV=1;
	final static int TASK_POP_NAV_NXT=2;
	public final static int TASK_LOAD_HISTORY=3;
	Thread t;
	public final AtomicInteger taskVersion = new AtomicInteger();
	public volatile AtomicBoolean taskRunning =new AtomicBoolean();
	private final AtomicBoolean ended=new AtomicBoolean();
	public final AtomicBoolean activated=new AtomicBoolean();
	public final AtomicBoolean acquired=new AtomicBoolean();
	private int mType;
	
	public WordPopupTask(WordPopup wordPopup) {
		this.wordPopupRef = new WeakReference<>(wordPopup);
	}
	
	@Override
	public void run() {
		if(ended.get()) {
			return;
		}
		final WordPopup wordPopup = wordPopupRef.get();
		int lastTaskVer=-1;
		if (wordPopup!=null)
		do {
			int taskVer = taskVersion.get();
			if(lastTaskVer!=taskVer) {
				taskRunning.set(false);
				AtomicBoolean runningTask=this.taskRunning=new AtomicBoolean(true);
				wordPopup.PerformSearch(mType, runningTask, taskVer, taskVersion);
//				if(runningTask.get() && taskVer == taskVersion.get()) {
//					//harvest...
//
//				}
				runningTask.set(false);
				lastTaskVer = taskVer;
			}
			while (!ended.get()) {
				try {
					Thread.sleep(activated.get()?500:2000);
					//Thread.sleep(1*60*1000);
				} catch (InterruptedException e) {
					CMN.Log(e);
				}
				if(taskVersion.get()!=taskVer) {
					break;
				}
			}
		} while(acquired.get() && !ended.get());
		ended.set(true);
	}
	
	public boolean start(int type) {
		mType = type;
		activated.set(true);
		acquired.set(true);
		if(ended.get()) {
			activated.set(false);
			return false;
		}
		taskRunning.set(false); // 取消旧的任务
		taskVersion.incrementAndGet(); // 更新任务版本
		if(t==null) {
			t=new Thread(this, "wp");
			t.start();
		} else {
			t.interrupt(); // 恢复运行状态
		}
		activated.set(false);
		if(ended.get()) {
			return false;
		}
		return true;
	}
	
	public void stop() {
		acquired.set(false);
		ended.set(true);
		taskRunning.set(false);
		if(t!=null) {
			t.interrupt();
			wordPopupRef.clear();
		}
	}
	
}