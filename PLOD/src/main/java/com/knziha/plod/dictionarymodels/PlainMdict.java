package com.knziha.plod.dictionarymodels;

import android.os.Looper;

import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.plaindict.CrashHandler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.knziha.plod.plaindict.AgentApplication.BufferAllocatorInst;

public class PlainMdict extends mdict {
	DictionaryAdapter.PLAIN_BOOK_TYPE mType;
	public PlainMdict(File fn, int pseudoInit, StringBuilder buffer, Object tag, boolean isResourceFile) throws IOException {
		super(fn, pseudoInit, buffer, tag);
		this.isResourceFile = isResourceFile;
		mType = DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_MDICT;
	}
	
	protected byte[] AcquireCompressedBlockOfSize(int compressedSize) {
		CrashHandler.hotDebugMessage = compressedSize+", "+maxComKeyBlockSize+", "+maxComRecSize;
		byte[] ret = BufferAllocatorInst.AcquireCompressedBlockOfSize(compressedSize, Math.max(maxComKeyBlockSize, maxComRecSize));
		CrashHandler.hotDebugMessage = null;
		return ret;
	}
	
	protected byte[] AcquireDeCompressedKeyBlockOfSize(int BlockSize) {
		CrashHandler.hotDebugMessage = CrashHandler.hotDebugMessage+"::"+BlockSize+"::"+maxDecomKeyBlockSize;
		return BufferAllocatorInst.AcquireDeCompressedKeyBlockOfSize(BlockSize, maxDecomKeyBlockSize);
	}
	
	@Override
	protected boolean isMainThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}
	
	@Override
	protected ExecutorService OpenThreadPool(int thread_number) {
		return Executors.newFixedThreadPool(thread_number);
		//return Executors.newCachedThreadPool();
		//return Executors.newScheduledThreadPool(thread_number);
		//return Executors.newWorkStealingPool();
	}
	
	@Override
	protected void MoveOrRenameResourceLet(mdictRes md, String token, String pattern, File newPath) {
		//File f = md.f();
		//String tokee = f().getName();
		//if(tokee.startsWith(token) && tokee.charAt(Math.min(token.length(), tokee.length()))=='.'){
		//	String suffix = tokee.substring(token.length());
		//	String np = f.getParent();
		//	if(np!=null && np.equals(np=newPath.getParent())){ //重命名
		//		File mnp=new File(np, pattern+suffix);
		//		if(FU.rename5(a, f, mnp)>=0)
		//			md.Rebase(mnp);
		//	} else {
		//		File mnp=new File(np, f.getName());
		//		if(FU.move3(a, f, mnp)>=0)
		//			md.Rebase(mnp);
		//	}
		//}
	}
	
	@Override
	public int getType() {
		return mType.ordinal();
	}
}
