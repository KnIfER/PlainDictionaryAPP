package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.Utils.F1ag;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.rbtree.RBTree_additive;

import org.jcodings.Encoding;
import org.joni.Option;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.knziha.plod.dictionary.SearchResultBean.SEARCHTYPE_SEARCHINTEXTS;
import static com.knziha.plod.dictionary.mdict.bingStartWith;
import static com.knziha.plod.dictionary.mdict.kalyxIndexOf;
import static com.knziha.plod.dictionary.mdict.kalyxLastIndexOf;

public class DictionaryAdapter implements UniversalDictionaryInterface {
	public enum PLAIN_BOOK_TYPE
	{
		PLAIN_TYPE_TEXT
		,PLAIN_TYPE_WEB
		,PLAIN_TYPE_MDICT
		,PLAIN_TYPE_PDF
		,PLAIN_TYPE_DSL
		,PLAIN_TYPE_EMPTY
	}
	File f;
	boolean fExist;
	long _bid;
	long _num_entries;
	long _num_record_blocks;
	String _Dictionary_fName;
	PDICMainAppOptions opt;
	Charset _charset;
	Encoding encoding;
	public volatile boolean searchCancled;
	
	DictionaryAdapter.PLAIN_BOOK_TYPE mType;
	byte[] options;
	
	/** validation schema<br/>
	 * 0=none; 1=check even; 2=check four; 3=check direct; 4=check direct for all; 5=1/3; */
	protected int checkEven;
	protected int maxEB;
	
	public String htmlOpenTagStr="<";
	public String htmlCloseTagStr=">";
	public byte[] htmlOpenTag;
	public byte[] htmlCloseTag;
	public byte[][] htmlTags;
	public byte[][] htmlTagsA;
	public byte[][] htmlTagsB;
	private mdict.EncodeChecker encodeChecker;
	
	protected int split_recs_thread_number;
	
	protected boolean hasResources;
	
	
	public DictionaryAdapter(File fn, MainActivityUIBase _a) {
		f = fn;
		_Dictionary_fName=fn.getName();
		if (_a!=null) {
			opt=_a.opt;
		}
		mType = PLAIN_BOOK_TYPE.PLAIN_TYPE_EMPTY;
	}
	
	protected DictionaryAdapter() {
	}
	
	@Override
	public String getEntryAt(long position, Flag mflag) {
		return "";
	}
	
	@Override
	public String getEntryAt(long position) {
		return "";
	}
	
	@Override
	public long getNumberEntries() {
		return _num_entries;
	}
	
	@Override
	public String getRecordAt(long position, GetRecordAtInterceptor getRecordAtInterceptor, boolean allowJump) throws IOException {
		return null;
	}
	
	@Override
	public String getRecordsAt(GetRecordAtInterceptor getRecordAtInterceptor, long... positions) throws IOException {
		if(getRecordAtInterceptor!=null)
		{
			String ret = getRecordAtInterceptor.getRecordAt(this, positions[0]);
			if (ret!=null) {
				return ret;
			}
		}
		return getRecordAt(positions[0], null, true);
	}
	
	@Override
	public byte[] getRecordData(int position) throws IOException {
		return null;
	}
	
	@Override
	public void setCaseStrategy(int val) {
	
	}
	
	@Override
	public File getFile() {
		return f;
	}
	
	@Override
	public String getDictionaryName() {
		return _Dictionary_fName;
	}
	
	@Override
	public boolean hasVirtualIndex() {
		return false;
	}
	
	@Override
	public StringBuilder AcquireStringBuffer(int capacity) {
		return new StringBuilder(capacity);
	}
	
	@Override
	public boolean hasMdd() {
		return hasResources;
	}
	
	@Override
	public String getRichDescription() {
		return "";
	}
	
	@Override
	public String getDictInfo() {
		return "";
	}
	
	@Override
	public boolean getIsResourceFile() {
		return false;
	}
	
	@Override
	public Object[] getSoundResourceByName(String canonicalName) throws IOException {
		return null;
	}
	
	@Override
	public String getCharsetName() {
		return _charset==null?null:_charset.name();
	}
	
	@Override
	public void Reload(Object context) {
	
	}
	
	@Override
	public int lookUp(String keyword, boolean isSrict, List<UniversalDictionaryInterface> morphogen) {
		return lookUp(keyword, isSrict);
	}
	
	@Override
	public int lookUp(String keyword, boolean isSrict) {
		return -1;
	}
	
	@Override
	public int lookUp(String keyword) {
		return lookUp(keyword, false);
	}
	
	@Override
	public int guessRootWord(UniversalDictionaryInterface d, String keyword){
		return -1;
	}
	
	@Override
	public int lookUpRange(String keyword, ArrayList<myCpr<String, Long>> rangReceiver, RBTree_additive treeBuilder, long SelfAtIdx, int theta, AtomicBoolean task, boolean strict) {
		return 0;
	}
	
	@Override
	public int lookUpRangeQuick(int startIndex, String keyword, ArrayList<myCpr<String, Long>> rangReceiver, RBTree_additive treeBuilder, long SelfAtIdx, int theta, AtomicBoolean task, boolean strict) {
		return 0;
	}
	
	@Override
	public InputStream getResourceByKey(String key) {
		return null;
	}
	
	@Override
	public Object ReRoute(String key) throws IOException {
		return null;
	}
	
	@Override
	public String getVirtualRecordAt(Object presenter, long vi) throws IOException {
		return null;
	}
	
	@Override
	public String getVirtualRecordsAt(Object presenter, long[] args) throws IOException {
		return getVirtualRecordAt(presenter, args[0]);
	}
	
	@Override
	public String getVirtualTextValidateJs(Object presenter, WebViewmy mWebView, long position) {
		return "";
	}
	
	@Override
	public String getVirtualTextEffectJs(Object presenter, long[] positions) {
		return null;
	}
	
	@Override
	public long getBooKID() {
		return _bid;
	}
	
	@Override
	public void setBooKID(long id) {
		_bid = id;
	}
	
	@Override
	public void flowerFindAllContents(String key, Object book, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException{
	
	}
	
	@Override
	public void flowerFindAllKeys(String key, Object book, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException{
	
	}
	
	@Override
	public String getResourcePaths() {
		return "";
	}
	
	@Override
	public byte[] getOptions() {
		return options;
	}
	
	@Override
	public void setOptions(byte[] options) {
		this.options = options;
	}
	
	@Override
	public int getType() {
		return mType.ordinal();
	}
	
	@Override
	public long getEntryExtNumber(long position, int index) {
		return 0;
	}
	
	@Override
	public String getField(String fieldName) {
		return null;
	}
	
	@Override
	public void setPerThreadKeysCaching(ConcurrentHashMap<Long, Object> keyBlockOnThreads) {
	
	}
	
	@Override
	public void doForAllRecords(Object book, mdict.AbsAdvancedSearchLogicLayer SearchLauncher, DoForAllRecords dor, Object parm) throws IOException {
		Object tParm = dor.onThreadSt(parm);
		searchCancled = false;
		for (int i = 0; i < _num_entries; i++) {
			if(SearchLauncher.IsInterrupted || searchCancled) break;
			String text = getRecordAt(i, null, false);
			dor.doit(parm, tParm, null, i, text, null, 0, 0, _charset);
			SearchLauncher.dirtyProgressCounter++;
		}
		dor.onThreadEd(parm);
	}
	
	public boolean handlePageUtils(BookPresenter presenter, WebViewmy mWebView, int pos) {
		return false;
	}
	
	public String getLexicalEntryAt(int position) {
		return null;
	}
	
	protected void postGetCharset() {
		htmlOpenTag = htmlOpenTagStr.getBytes(_charset);
		htmlCloseTag = htmlCloseTagStr.getBytes(_charset);
		htmlTags = new byte[][]{htmlOpenTag, htmlCloseTag};
		htmlTagsA = new byte[][]{htmlOpenTag};
		htmlTagsB = new byte[][]{htmlCloseTag};
		switch (_charset.name()){
			case "EUC-JP":
				checkEven=5;
				maxEB = 3;
			break;
			case "EUC-KR":
			case "x-EUC-TW":
			case "Shift_JIS":
			case "Windows-31j":
				checkEven=3;
				maxEB = 2;
			break;
			case "GB2312"://1981 unsafe double bytes
			case "GBK"://1995 unsafe double bytes
				checkEven=4;
				maxEB = 2;
			break;
			case "GB18030"://2000 unsafe double bytes
				checkEven=4;
				maxEB = 4;
				encodeChecker = new mdict.EncodeChecker();
			break;
			case "UTF-16BE":
			case "UTF-16LE":
				checkEven=1;
				maxEB = 4;
			break;
			case "UTF-32BE":
			case "UTF-32LE":
				checkEven=2;
				maxEB = 4;
			break;
			case "Big5":// safe double bytes?
			case "Big5-HKSCS":// safe double bytes?
				checkEven=3;
			break;
			case "UTF-8":// safe tripple bytes?
				//checkEven=3;
				maxEB = 4;
			break;
			default:
				maxEB = 1;
			break;
		}
	}
	
	protected ExecutorService OpenThreadPool(int thread_number) {
		return Executors.newFixedThreadPool(thread_number);
		//return Executors.newCachedThreadPool();
		//return Executors.newScheduledThreadPool(thread_number);
		//return Executors.newWorkStealingPool();
	}
	
	protected int flowerIndexOf(byte[] source, int sourceOffset, int sourceCount, byte[][][] matchers, int marcherOffest, int fromIndex, mdict.AbsAdvancedSearchLogicLayer launcher, F1ag flag, ArrayList<Object> mParallelKeys, int[] jumpMap)
	{
		int tagCheckFrame = 32;
		int tagSkipFrame = 32;
		int lastSeekLetSize=0;
		int totalLen = matchers.length;
		boolean bSearchInContents = launcher.type==SEARCHTYPE_SEARCHINTEXTS;
		boolean trimStart = fromIndex==0 && (jumpMap[matchers.length]&1)!=0;
		int hudieling;
		if(true) {
			//checkEven=0;
			//bCheckTags=false;
		}
//		boolean debug=false;
//		if(new String(source, sourceOffset, sourceCount, _charset).equals("democracy")) {
//			SU.Log("正在搜索"); debug=true;
//		}
		int bInTagCheckNeeded=-1;
//		RBTree<MyIntPair> tagsMap = new RBTree<>();
//		for (int i = 0; i < sourceCount; i++) {
//			if (source[i]==htmlOpenTag[0]) {
//				//tagsMap.insert(new MyIntPair(i,0));
//			}
//			else if(source[i]==htmlCloseTag[0]) {
//				//tagsMap.insert(new MyIntPair(i,0));
//			}
//		}
		while(fromIndex<sourceCount) {
			//SU.Log("==");
			//int idx = -1;
			int fromIndex_=fromIndex;
			boolean isSeeking=!trimStart;
			boolean Matched = false;
			boolean pass; int len;
			int lexiPartIdx;
			for(lexiPartIdx=marcherOffest;lexiPartIdx<totalLen;lexiPartIdx++) {
				//if(fromIndex_>sourceCount-1) return -1;
				hudieling = jumpMap[lexiPartIdx];
//				if(debug) {
//					SU.Log("stst: " + sourceCount + "::" + (fromIndex_ /*+ seekPos*/) + " fromIndex_: " + fromIndex_/* + " seekPos: " + seekPos*/ + " lexiPartIdx: " + lexiPartIdx);
//					SU.Log("hudieling::"+hudieling, "isSeeking::"+isSeeking, /*"seekPos: " + seekPos + */" lexiPartIdx: " + lexiPartIdx + " fromIndex_: " + fromIndex_);
//				}
				if(hudieling>0) {
					if(lexiPartIdx==totalLen-1) {
						if(fromIndex_>=sourceCount)
							return -1;
						return fromIndex-lastSeekLetSize;//HERE
					}//Matched=true
					//SU.Log("miansi: "+lexiPartIdx);
					//SU.Log("miansi: "+sourceCount+"::"+(fromIndex_+seekPos)+"sourceL: "+source.length);
					//SU.Log("jumpped c is: "+new String(source,fromIndex_+seekPos,Math.min(4, sourceCount-(fromIndex_+seekPos-sourceOffset)),_encoding).substring(0, 1));
					int newSrcCount = Math.min(maxEB*hudieling, sourceCount-(fromIndex_));
					if(newSrcCount<=0) return -1;
					//TODO calc char length for different charsets.
					String c = new String(source,sourceOffset+fromIndex_,newSrcCount,_charset);
					if(hudieling>c.length()) return -1;
					int jumpShort = c.substring(0, hudieling).getBytes(_charset).length;
					fromIndex_+=jumpShort;
					continue;
				}
				else if(hudieling<0) {
					if(lexiPartIdx==totalLen-1)
						return fromIndex-lastSeekLetSize;//HERE
					int newSrcCount=sourceCount;
					hudieling*=-1;//todo 边界处理
					if(hudieling>1) newSrcCount=fromIndex_+hudieling-1;
					if(flowerIndexOf(source, sourceOffset,newSrcCount, matchers,lexiPartIdx+1, fromIndex_, launcher, flag, mParallelKeys, jumpMap)!=-1){
						return fromIndex-lastSeekLetSize;
					}
					return -1;
				}
				Matched = false;
				if(isSeeking) {
					int seekPos=-1;
					int	newSeekPos = kalyxIndexOf(source, sourceOffset, sourceCount, matchers[lexiPartIdx], fromIndex_, flag);
//					if(debug) SU.Log("newSeekPos", newSeekPos, checkEven);
					if(newSeekPos>=fromIndex_){
						//todo verify first match
						pass = true;
						lastSeekLetSize = matchers[lexiPartIdx][flag.val].length;
						if (checkEven != 0 ) {//zzz
							if((len = newSeekPos - fromIndex_) != 0) {
								if (checkEven == 2) {
									pass = len % 4 == 0;
								} else if (checkEven == 1 && len % 2 != 0) {
									pass = false;
								} else {
									if(encodeChecker !=null)
										pass = encodeChecker.checkBefore(source, sourceOffset, fromIndex_, newSeekPos);
									else {
										//len = sourceOffset + newSeekPos;
										int start;// = (checkEven == 3 || bSearchInContents) ? Math.max(sourceOffset + fromIndex_, sourceOffset + newSeekPos - maxEB) : (sourceOffset + fromIndex_);
										//start = (sourceOffset + fromIndex_) ;
										//int start = Math.max(sourceOffset+fromIndex_, sourceOffset+newSeekPos-4);
										for (int i = 1; i < maxEB; i++) {
											if (checkEven == 1 && i == 1/*二四变长编码*/
													|| checkEven != 5 && i == 3/*一二四变长编码*/) i++;
											if (newSeekPos - i < 0) break;
											start = sourceOffset + newSeekPos - i;
											//if (start > sourceOffset + fromIndex_) break;
											len = sourceOffset + newSeekPos - start;
											String validfyCode = new String(source, start, len, _charset);
											len = validfyCode.length();
											pass = len > 0 && (validfyCode.charAt(len - 1) != 65533);
											//SU.Log("validfyCode", validfyCode, validfyCode.charAt(len - 1) == 65533, pass);
											if (pass) break;
										}
										//}
										//								if(pass){
										//									validfyCode = new String(source, sourceOffset+newSeekPos+matchers[lexiPartIdx][flag.val].length, maxEB, _charset);
										//									//SU.Log("validfyCode", validfyCode);
										//									len = validfyCode.length();
										//									pass = len > 0 && validfyCode.charAt(0) != 65533;
										//								}
									}
								}
							}
//							else if((len = sourceCount - newSeekPos - matchers[lexiPartIdx][flag.val].length) != 0){
//								int start = newSeekPos+matchers[lexiPartIdx][flag.val].length;
//								len = Math.min(start+maxEB, len);
//								String validfyCode = new String(source, start, len, _charset);
//								SU.Log("validfyCode2", validfyCode, validfyCode.charAt(len - 1) == 65533);
//								pass = validfyCode.length() > 0 && validfyCode.charAt(0) != 65533;
//							}
						}
						if (pass) {
							seekPos = newSeekPos;
							Matched = true;
						}
						else {
							fromIndex=fromIndex_=newSeekPos + lastSeekLetSize;
							lexiPartIdx--;
							continue;
						}
					}
//					if(debug)SU.Log("seekPos:"+seekPos+" fromIndex_: "+fromIndex_, Matched);
					if(!Matched)
						return -1;
					seekPos+=lastSeekLetSize;
					fromIndex=fromIndex_=seekPos;
					isSeeking=false;
					continue;
				}/* End seek */
				else {
//					if(debug)SU.Log("deadline", fromIndex_+" "+sourceCount);
					if(fromIndex_>sourceCount-1) {
//						if(debug)SU.Log("deadline reached"+fromIndex_+" "+sourceCount, new String(source, sourceOffset, sourceCount, _charset));
						return -1;
					}
//					if(debug) {
//						SU.Log("matchedHonestily? ", lexiPartIdx, mParallelKeys.get(lexiPartIdx));
//						SU.Log("matchedHonestily? str", new String(source, sourceOffset+fromIndex_, 100, _charset), bingStartWith(source,sourceOffset, sourceCount, matchers[lexiPartIdx][0],0,matchers[lexiPartIdx][0].length,fromIndex_));
//					}
					for(byte[] marchLet:matchers[lexiPartIdx]) {
						if(marchLet==null) break;
						if(bingStartWith(source,sourceOffset, sourceCount, marchLet,0,marchLet.length,fromIndex_)) {
							Matched=true;
							if(bInTagCheckNeeded>=0){
								//延后的标签内检查 delay the check
								int from = bInTagCheckNeeded;
								//todo skip html tags 检查不在<>之中。 前进找>，截止于<>两者。若找先到<则放行。
								//									若找先到>则需要进一步检查。 [36]
								//									后退找<，截止于<>两者。若找先到>则放行。[36]
								//									若找先到<则简单认为需要跳过。
								//int htmlForward = indexOf(source, sourceOffset, Math.min(sourceCount, from+tagCheckFrame), htmlCloseTag, 0, htmlCloseTag.length, from);
								int htmlForward = safeKalyxIndexOf(source, sourceOffset, Math.min(sourceCount, from+tagCheckFrame), htmlTags, from, flag);
								if (htmlForward>=from) {
									//if(flag.val==1){ // x >
									from = bInTagCheckNeeded-1;
									//int htmlBackward = safeKalyxLastIndexOf(source, sourceOffset, sourceOffset+Math.max(0, from-tagCheckFrame), htmlTags, from, flag);
									int htmlBackward = safeKalyxLastIndexOf(source, sourceOffset, sourceOffset+Math.max(0, from-tagCheckFrame), htmlTags, from, flag);
									if(htmlBackward>=0){
										if(flag.val==0){ // < x
											fromIndex=fromIndex_=htmlForward+htmlTags[flag.val].length;
											Matched=false;
											bInTagCheckNeeded=0;
											break; // re-seek
										}
									}
									//}
								}
								bInTagCheckNeeded=0;
							}
//							if(debug) {
//								SU.Log("matchedHonestily: ", sourceCount, "::", " fromIndex_: ", fromIndex_ + " seekPos: ");
//								SU.Log("matchedHonestily: ", lexiPartIdx, mParallelKeys.get(lexiPartIdx));
//							}
							fromIndex_+=marchLet.length;
							break;
						}
					}
				}/* End honest match */
				if(!Matched) {
					//SU.Log("Matched failed this round: "+lexiPartIdx);
					break;
				}
			}/* End lexical parts loop */
			if(Matched){
				if((jumpMap[matchers.length]&2)!=0&&fromIndex_<sourceCount)
					return -1;
				return fromIndex-lastSeekLetSize;
			}
			else if(trimStart)
				return -1;
		}
		return -1;
	}

	
	public int safeKalyxIndexOf(byte[] source, int sourceOffset, int sourceCount, byte[][] targets, int fromIndex, F1ag seelHolder) {
		int fromIndex_ = fromIndex;
		int ret, len;
		boolean pass=true;
		while((ret = kalyxIndexOf(source, sourceOffset, sourceCount, targets, fromIndex, seelHolder))>=fromIndex){
			len = ret-fromIndex_;
			//if(false)
			if (checkEven != 0 && ret>fromIndex_) {
				if (checkEven == 2) {
					pass = len % 4 == 0;
				} else if (checkEven == 1 && len % 2 != 0) {
					pass = false;
				} else {
					if(encodeChecker !=null)
						pass = encodeChecker.checkBefore(source, sourceOffset, fromIndex_, ret);
					else{
						int start;
						for (int i = 1; i < maxEB; i++) {
							if(checkEven==1&&i==1/*二四变长编码*/
									|| checkEven!=5&&i==3/*一二四变长编码*/) i++;
							if (ret - i < 0) break;
							start = sourceOffset + ret - i;
							len = sourceOffset + ret - start;
							String validfyCode = new String(source, start, len, _charset);
							len = validfyCode.length();
							pass = len > 0 && (validfyCode.charAt(len - 1) != 65533);
							//SU.Log("validfyCode", validfyCode, validfyCode.charAt(len - 1) == 65533, pass);
							if(pass) break;
						}
					}
				}
			}
			if(pass) return ret;
			fromIndex = ret + targets[seelHolder.val].length;
		}
		return -1;
	}
	
	public int safeKalyxLastIndexOf(byte[] source, int sourceOffset, int sourceStart, byte[][] targets, int fromIndex, F1ag seelHolder) {
		//if(true) return -1;
		int fromIndex_ = fromIndex;
		int ret, len;
		boolean pass=true;
		while((ret = kalyxLastIndexOf(source, sourceOffset, sourceStart, targets, fromIndex, seelHolder))>=0){
			len = fromIndex_-ret;
			if(ret<fromIndex_){
				if (checkEven == 2) {
					pass = len % 4 == 0;
				} else if (checkEven == 1 && len % 2 != 0) {
					pass = false;
				} else {
					if(encodeChecker !=null)
						pass = encodeChecker.checkAfter(source, sourceOffset, fromIndex, ret);
					else{
						int start =  ret + targets[seelHolder.val].length;
						for (int i = 1; i < maxEB; i++) {
							if(checkEven==1&&i==1/*二四变长编码*/
									|| checkEven!=5&&i==3/*一二四变长编码*/) i++;
							if(start+i>fromIndex+1) break;
							String validfyCode = new String(source, sourceOffset+start, i, _charset);
							len = validfyCode.length();
							pass = len > 0 && (validfyCode.charAt(len - 1) != 65533);
							//SU.Log("validfyCode", validfyCode, validfyCode.charAt(len - 1) == 65533, pass);
							if(pass) break;
						}
					}
//					int start = sourceOffset + ret + targets[seelHolder.val].length;
//					len = maxEB;
//					String validfyCode = new String(source, start, len, _charset);
//					//SU.Log("validfyCode", validfyCode);
//					pass = validfyCode.length() > 0 && validfyCode.charAt(0) != 65533;
				}
			}
			
			if(pass) return ret;
			fromIndex = ret - targets[seelHolder.val].length;
		}
		return -1;
	}
	
	protected int getRegexOption(){
		return Option.IGNORECASE;
	}
	
	@Override
	public InputStream getRecordStream(int idx) throws IOException {
		return new ByteArrayInputStream(getRecordData(idx));
	}
	
	@Override
	public void saveConfigs(Object book) {
	
	}
	
	@Override
	public void onPageFinished(BookPresenter invoker, WebViewmy mWebView, String url, boolean b) {
	
	}
}
