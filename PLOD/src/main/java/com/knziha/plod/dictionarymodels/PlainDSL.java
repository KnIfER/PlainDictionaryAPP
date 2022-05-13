package com.knziha.plod.dictionarymodels;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.knziha.filepicker.utils.ExtensionHelper;
import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.SearchResultBean;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.F1ag;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.IntStr;
import com.knziha.plod.dictionary.Utils.LinkastReUsageHashMap;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionaryBuilder.mdictBuilder;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.rbtree.RBTree_additive;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joni.Option;
import org.joni.Regex;
import org.knziha.metaline.Metaline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.knziha.plod.dictionary.mdBase.compareByteArrayIsPara;
import static com.knziha.plod.dictionary.mdBase.processText;
import static com.knziha.plod.dictionary.mdict.leafSanLieZhi;

/**
 Model handling local lingvo-dsl files.<br/>
 date:2020.2.18<br/>
 author:KnIfER<br/>
 licence:GPL2.0<br/>
*/
public class PlainDSL extends DictionaryAdapter {
	final static byte[] UTF16BOMBYTES = new byte[]{(byte) 0xff, (byte) 0xfe};
	final static String UTF16BOM = new String(UTF16BOMBYTES, StandardCharsets.UTF_16LE);
	final static Pattern dslTagPattern = Pattern.compile("\\[(.{1,50}?)]|(\t)|<<(.*?)>>");
	final static byte[] UTF8LineBreakText = "\n".getBytes(StandardCharsets.UTF_8);
	private final File mResZipFile;
	private ZipFile mResZip;
	private DictInputStream.GZIPDictReader dictReader;
	private long firstflag;
	private boolean bIsUTF16;
	private long file_length;
	byte[] lineBreakText;
	byte[] contentBreakText;
	byte[] contentBreakText1;
	byte[][] excludesNextBytes;
	final static int block_size = 4096;
	int mBlockSize = block_size;
	final static int DefaultCacheItemCount = 1024*1024/block_size;
	int mCacheItemCount;
	private int contentBreakText_length;
	private int _BOM_SIZE;
	public final boolean isDictZip;
	
	@Metaline(flagPos=0, shift=1) public boolean getIsGzipArchive() {firstflag=firstflag;throw new RuntimeException();}
	@Metaline(flagPos=0, shift=1) public void setIsGzipArchive(boolean value) {firstflag=firstflag;throw new RuntimeException();}
	
	int SafeRead(InputStream input, byte[] data, int offset, int tempRead) throws IOException {
		int read=0;
		int len;
		while((len = input.read(data, offset+read, tempRead-read))>0) {
			read += len;
		}
		return read;
	}
	
	static class TextBlock{
		int blockIndex;
		int breakIndex=-1;
		int blockSize;
		final byte[] data;
		TextBlock(int blockSize) {
			data = new byte[blockSize];
		}
		@NonNull
		@Override
		public String toString() {
			return "#"+blockIndex+":"+breakIndex+"/"+blockSize+"≈"+new String(data, 0, 100, StandardCharsets.UTF_16LE);
		}
		public void read(InputStream fis) throws IOException {
			//blockSize = fis.read(data);
			blockSize=0;
			int len;
			while(blockSize<data.length&&(len = fis.read(data, blockSize, data.length-blockSize))>0) {
				blockSize += len;
			}
		}
	}
	
	LinkastReUsageHashMap<Integer, TextBlock> block_cache;
	mdict keyIndex;
	
	/*final*/ File mIndexFolder;
	/*final*/ File mIndexFile;
	
	void dumpEntries() throws IOException {
//		ArrayList<Entry> data = index_array.getList();
//		mIndexFolder.mkdirs();
//		FileOutputStream fout = new FileOutputStream(mIndexFile);
//		// 32 bytes
//		{
//			byte[] buffer = new byte[8];
//			BU.putIntLE(buffer, 0, DSLINDEXVERSION);
//			fout.write(buffer,0,4);
//			BU.putIntLE(buffer, 0, 0);
//			fout.write(buffer,0,4);
//			BU.putLongLE(buffer, 0, file_length);
//			fout.write(buffer);
//			BU.putLongLE(buffer, 0, firstflag);
//			fout.write(buffer);
//			BU.putLongLE(buffer, 0, _num_entries);
//			fout.write(buffer);
//		}
//		int DSLIndexBlockSize = 4096;
//		byte[] buff = new byte[DSLIndexBlockSize];
//		ReusableByteOutputStream bos;
//		DataOutputStream data_out = new DataOutputStream(bos=new ReusableByteOutputStream(buff, DSLIndexBlockSize));
//		int count=0; int size;
//		byte[] name;
//		boolean init=false;
//		/* writing normal entries, including rerouting ones. */
//		_num_entries = data.size();
//		CMN.Log("dumpEntries", _num_entries);
//		ArrayList<additiveMyCpr1> appendix_arr = appendix.flatten();
//		int appendSize = appendix_arr.size();
//		int U8BTL = UTF8LineBreakText.length;
//		for (int i = 0; i <= _num_entries; i++) {//4k对齐写入
//			if(!init){
//				name = "EXTIDX".getBytes(StandardCharsets.UTF_8);
//				data_out.write(name);
//				data_out.write(UTF8LineBreakText);
//				data_out.writeLong(0);
//				data_out.writeLong(_num_entries);
//				count=name.length+U8BTL+16;
//				init=true;
//			}
//			Entry entry;
//			if(i==_num_entries){
//				entry = new Entry();
//				entry.text="APPEND";
//				entry.contentStart=appendSize;
//			} else {
//				entry = data.get(i);
//			}
//			name = entry.text.getBytes(StandardCharsets.UTF_8);
//			size=name.length+U8BTL+16;
//			if(count+size > DSLIndexBlockSize){//溢出写入
//				byte[] source = bos.getBytes();
//				Arrays.fill(source, count, DSLIndexBlockSize, (byte) 0);
//				fout.write(source);
//				fout.flush();
//				bos.reset();
//				count = 0;
//			}
//			data_out.write(name);
//			data_out.write(UTF8LineBreakText);
//			data_out.writeLong(entry.contentStart);
//			data_out.writeLong(entry.contentEnd);
//			count+=size;
//		}
//		/* writing gd appending entries, including duplicated ones. */
//		for (int i = 0; i < appendSize; i++) {
//			OUT:
//			if(true){
//				String keyText = appendix_arr.get(i).key;
//				ArrayList arr = (ArrayList) appendix_arr.get(i).value;
//				name = keyText.getBytes(StandardCharsets.UTF_8);
//				size = name.length+U8BTL; if(count+size > DSLIndexBlockSize){break OUT;}
//				data_out.write(name);
//				data_out.write(UTF8LineBreakText);
//				for (Object oI:arr) {
//					if(oI instanceof String){
//						name = ((String)oI).getBytes(StandardCharsets.UTF_8);
//						size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
//						data_out.write(name);
//						data_out.write(UTF8LineBreakText);
//						data_out.writeLong(0);
//						data_out.writeLong(0);
//					} else if(oI instanceof Entry){
//						Entry entry = ((Entry) oI);
//						name = entry.text.getBytes(StandardCharsets.UTF_8);
//						size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
//						data_out.write(name);
//						data_out.write(UTF8LineBreakText);
//						data_out.writeLong(entry.contentStart);
//						data_out.writeLong(entry.contentEnd);
//					}
//				}
//				name = "APPEND".getBytes(StandardCharsets.UTF_8);
//				size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
//				data_out.write(name);
//				data_out.write(UTF8LineBreakText);
//				data_out.writeLong(-1);
//				data_out.writeLong(-1);
//				count+=size;
//				continue;
//			}
//			//溢出写入
//			byte[] source = bos.getBytes();
//			Arrays.fill(source, count, DSLIndexBlockSize, (byte) 0);
//			fout.write(source);
//			fout.flush();
//			bos.reset();
//			count = 0;
//		}
//		byte[] source = bos.getBytes();
//		if(count>0 && count<=DSLIndexBlockSize) { //正常写入
//			fout.write(source ,0, count);
//			fout.flush();
//			fout.close();
//		}
//		mIndexFile.setLastModified(f.lastModified());
	}

	/**
	 <style type="text/css">
	 ._DSL_COM,._DSL_OPT,._DSL_EX{
		 color:gray;
	 }
	 ._DSL_OPT{
		 display:none;
	 }
	 ._DSL_HEAD{
		 margin:0;
	 	 margin-bottom: 8px;
	 }
	 </style>
	 */
	@Metaline
	final static String stylesheet="stylesheet";
	
	@Override
	public String getRecordAt(long position, GetRecordAtInterceptor getRecordAtInterceptor, boolean allowJump) throws IOException {
		if(getRecordAtInterceptor!=null)
		{
			String ret = getRecordAtInterceptor.getRecordAt(this, position);
			if (ret!=null) {
				return ret;
			}
		}
		StringBuffer sb = new StringBuffer();
		getRecordsAt_internal(sb, position, 0);
		return sb.toString();
	}
	
	protected InputStream mOpenInputStream(long fileOffset) throws IOException {
		if (isDictZip) {
			if(getIsGzipArchive()||dictReader!=null) {
				if(dictReader!=null) {
					return new DictInputStream(dictReader, fileOffset);
				}
				try {
					return BU.SafeSkipReam(new GZIPInputStream(new FileInputStream(f)), fileOffset);
				} catch (IOException e) {
					setIsGzipArchive(false);
					return BU.SafeSkipReam(mOpenInputStream(fileOffset), fileOffset);
				}
			} else {
				ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(f));
				java.util.zip.ZipEntry entry;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".dsl")) {
						return BU.SafeSkipReam(zipInputStream, fileOffset);
					}
				}
			}
		}
		return BU.SafeSkipReam(new FileInputStream(f), fileOffset);
	}
	
	TextBlock tmpLastBlock;
	
	private void getRecordsAt_internal(StringBuffer sb, long position, int depth) throws IOException {
		String indexRecord = keyIndex.getRecordAt(position, null, false);
		long contentStart = keyIndex.getEntryExtNumber(position, 0);
		long contentLength = keyIndex.getEntryExtNumber(position, 1);
		//SU.Log("indexRecord::", indexRecord.replace("\n", "\\n"), contentStart, contentLength);
		int centerBlock = (int) (contentStart/mBlockSize);
		int length = (int) (contentLength);
		ReusableByteOutputStream bos = new ReusableByteOutputStream(mBlockSize *2);
		bos.reset();
		TextBlock tmpBlock;
		int toSkip=0;
		InputStream fin=null;
		while(length>0 && centerBlock<_num_record_blocks) {
			//tmpBlock = null;
			tmpBlock = tmpLastBlock!=null&&tmpLastBlock.blockIndex==centerBlock?tmpLastBlock:block_cache.get(centerBlock);
			if(tmpBlock==null){
				try {
					if(fin==null)
						fin = mOpenInputStream(centerBlock*mBlockSize);
					else if(toSkip>0) {
						BU.SafeSkipReam(fin, toSkip);
						toSkip=0;
					}
					tmpBlock = new_TextBlock();
					tmpBlock.blockIndex = centerBlock;
					//tmpBlock.blockSize = fin.read(tmpBlock.data);
					tmpBlock.read(fin);
					block_cache.put(centerBlock, tmpBlock);
				} catch (IOException e) {
					if(fin!=null)
						fin.close();
					throw e;
				}
			} else {
				//CMN.Log("找到DSL缓存区");
				if(fin!=null)toSkip+=tmpBlock.blockSize;
			}
			if(tmpLastBlock!=tmpBlock)tmpLastBlock=tmpBlock;
			//CMN.Log(centerBlock, tmpBlock);
			int start = (int) Math.max(0, contentStart - (centerBlock * mBlockSize));
			int len = Math.min(tmpBlock.blockSize - start, length);
			bos.write(tmpBlock.data, start, len);
			length-=len;
			centerBlock++;
		}
		String val = new String(bos.getBytes(), 0, bos.size(), _charset);
		//CMN.Log(" "); CMN.Log(" "); CMN.Log(val);
		ConvertDslToHtml(sb, keyIndex.getEntryAt(position), val, indexRecord, depth);
	}
	
	private TextBlock new_TextBlock() {
		return new TextBlock(mBlockSize);
	}
	
	private byte[] getRecordDataForEntry(TextBlock tmpBlock_A, TextBlock tmpBlock_B, Entry eI, ReusableByteOutputStream bos, F1ag recordSt) throws IOException {
		bos.reset();
		int centerBlock = (int) (eI.contentStart/mBlockSize);
		int endBlock = (int) (eI.contentEnd/mBlockSize);
		int length = (int) (eI.contentEnd - eI.contentStart);
		if(endBlock==centerBlock) {
			recordSt.val = (int)(eI.contentStart - centerBlock*mBlockSize);
			if(tmpBlock_B.blockIndex==centerBlock) {
				return tmpBlock_B.data;
			}
			if(tmpBlock_A.blockIndex==centerBlock) {
				return tmpBlock_A.data;
			}
			recordSt.val = 0;
		}
		TextBlock tmpBlock;
		int toSkip=0;
		while(length>0 && centerBlock<_num_record_blocks) {
			//tmpBlock = tmpLastBlock!=null&&tmpLastBlock.blockIndex==centerBlock?tmpLastBlock:block_cache.get(centerBlock);
			if(tmpBlock_B.blockIndex==centerBlock) tmpBlock=tmpBlock_B;
			else if(tmpBlock_A.blockIndex==centerBlock) tmpBlock=tmpBlock_A;
			else tmpBlock=null;
			if(tmpBlock==null) { // &&false
				//if(true) return bos.getBytes();
				// oops
				IOException exception=null;
				try(InputStream fin = mOpenInputStream(centerBlock*mBlockSize)) {
					if(toSkip>0) {
						BU.SafeSkipReam(fin, toSkip);
						toSkip=0;
					}
					tmpBlock = new_TextBlock();
					tmpBlock.blockIndex = centerBlock;
					//tmpBlock.blockSize = fin.read(tmpBlock.data);
					tmpBlock.read(fin);
					//block_cache.put(centerBlock, tmpBlock);
				} catch (IOException e) {
					exception = e;
				}
				if(exception!=null)throw exception;
			} else {
				//CMN.Log("找到DSL缓存区");
				toSkip+=tmpBlock.blockSize;
			}
			int start = (int) Math.max(0, eI.contentStart - (centerBlock * mBlockSize));
			int len = Math.min(tmpBlock.blockSize - start, length);
			bos.write(tmpBlock.data, start, len);
			length-=len;
			centerBlock++;
		}
		return bos.getBytes();
	}

	public void ConvertDslToHtml(StringBuffer sb, String entry, String source, String routeAndLinks, int depth) throws IOException {
		boolean bIsStart = sb.length()==0;
		//source = entryLinkPattern.matcher(source).replaceAll("<a href='entry://$1'>$1</a>");
		//todo naive 转义
		//if(source.contains("\\\\")) source = source.replace("\\\\", "\\");
		//  Pattern.compile("\\[(.{1,50}?)]|(\t)|<<(.*?)>>");
		Matcher m = dslTagPattern.matcher(source);
		sb.append(stylesheet);
		if(bIsStart){
			sb.append("<h3 class='_DSL_HEAD'>").append(entry).append("</h3>");
		}
		
		String[] indexArr = null;
		if(!TextUtils.isEmpty(routeAndLinks)) {
			indexArr = routeAndLinks.split("\n");
			if(indexArr.length>0) {
				String rerouteTarget = indexArr[0];
				if(!TextUtils.isEmpty(rerouteTarget)) { // ==>
					sb.append("<h5 class='_DSL_HEAD'> ➥ ").append(rerouteTarget).append("</h5>");
				}
			}
		}
		ArrayList<IntStr> tmpTags = new ArrayList<>();
		
		boolean replace;
		while(m.find()){
			String val = m.group(1);
			int start=m.start();
			if(val==null) {
				val = m.group(2);
				if (val!=null) {
					if(start>0 && start+2<source.length() && !(source.charAt(start+1)=='['&&source.charAt(start+2)=='m')){
						m.appendReplacement(sb, "<br/>");
					}
					continue;
				}
				val = m.group(3);
				if (val!=null) {
					m.appendReplacement(sb, "<a href='entry://$1'>$1</a>");
					continue;
				}
			}
			else {
				replace=true;
				if (start > 0 && val.length() > 0 && source.charAt(start - 1) == '\\') {
					if(val.charAt(val.length() - 1) == '\\') {
						m.appendReplacement(sb, "");
						sb.delete(sb.length() - 1, sb.length());
						sb.append("[").append(val, 0, val.length() - 1).append("]");
						continue;
					} else {
						int idx = val.indexOf("[");
						if(idx>=0){
							start += idx;
							replace=false;
							m.appendReplacement(sb, "");
							sb.delete(sb.length() - 1, sb.length());
							sb.append("[").append(val.substring(0, idx)).append("<");
							val = val.substring(idx+1);
						}
					}
				}
				if(replace)
					m.appendReplacement(sb, "<");
				//if(start>0){
				//	if(source.charAt(start)=='\t')
				//		sb.append("br/><");
				//}
				String[] args = val.split(" +");
				//CMN.Log(args);
				boolean closing = false;
				char c0;
				int indent;
				String aI;
				ParseTagWithArgs:
				for (int i = 0; i < args.length; i++) {
					aI = args[i];
					if (aI.length() > 0) {
						c0 = aI.charAt(0);
						if (c0 == '/') {
							closing = true;
							sb.append("/");
							aI = aI.substring(1);
						} else if (c0 == 'm' && aI.length() > 1) {
							indent = IU.parsint(aI.substring(1), -1);
							if (indent > 0) {
								//sb.append("p style='text-indent:").append(indent).append("em;'");
								sb.append("div style='padding-left:").append(9 * indent).append("px;'");
								break ParseTagWithArgs;
							}
						}
						switch (aI) {
							case "m":
								sb.append("div");
							break;
							case "c":
								sb.append("span");
								if (!closing) {
									for (int j = 1; j < args.length; j++) {
										if (args[j].trim().length() > 0) {
											sb.append(" style='color:").append(args[j]).append("'");
											break;
										}
									}
									sb.append(" style='color:crimson'");
									break ParseTagWithArgs;
								}
							break;
							case "s":
								//sb.append("span");
								if (!closing) {
									tmpTags.add(new IntStr(sb.length(), "img"));
									sb.append("img");
									//sb.append(" style='display:none'");
								} else if(tmpTags.size()>0) {
									IntStr tmpTag = tmpTags.remove(tmpTags.size() - 1);
									//"img".equals(tmpTag.string)
									String suffix = null;
									int len=sb.length()-2;
									sb.setLength(len);
									for (int j = Math.max(0, len-8); j < len; j++) {
										if (sb.charAt(j)=='.') {
											suffix = sb.substring(j).toLowerCase();
											break;
										}
									}
									String newTagName = tmpTag.string;
									boolean isAudio=false;
									if (suffix!=null) {
										if (ExtensionHelper.SOUNDS.contains(suffix)) {
											newTagName = "audio";
											isAudio = true;
										}
									}
									sb.replace(tmpTag.number, tmpTag.number+tmpTag.string.length()+1, newTagName+" src=\"");
									sb.append("\"");
									if(isAudio) sb.append("controls=\"\"");
									sb.append(">🔈</");
									sb.append(newTagName);
									//CMN.Log("替换string::", sb);
									//CMN.Log("替换string::", newTagName, suffix);
								} else {
									sb.append("img");
								}
								break ParseTagWithArgs;
							//break;
							case "i":
								sb.append("span");
								if (!closing) {
									sb.append(" style='font-style:italic'");
									break ParseTagWithArgs;
								}
							break;
							case "*":
								sb.append("div");
								if (!closing) {
									sb.append(" class='_DSL_OPTX'");
									break ParseTagWithArgs;
								}
							break;
							case "ex":
								sb.append("div");
								if (!closing) {
									sb.append(" class='_DSL_EX'");
									break ParseTagWithArgs;
								}
							break;
							case "com":
								sb.append("div");
								if (!closing) {
									sb.append(" class='_DSL_COM'");
									break ParseTagWithArgs;
								}
							break;
							case "url":
								sb.append("a");
							break;
							case "trn":
								sb.append("span");
							break;
							case "p":
								sb.append("font");
								if (!closing) {
									sb.append(" color='blue'");
									break ParseTagWithArgs;
								}
							break;
							default:
								sb.append(aI);
							break;
						}
					}
				}
				sb.append(">");
			}
		}
		m.appendTail(sb);
		
		if(indexArr!=null) {
			++depth;
			for (int i = 1; i < indexArr.length; i++) {
				if (TextUtils.getTrimmedLength(indexArr[i])>0) {
					sb.append("<br/><p>➟").append(indexArr[i]).append("</p>");
					int idx=lookUp(indexArr[i], true);
					if(idx>=0 && depth<=3) getRecordsAt_internal(sb, idx, depth);
				}
			}
		}
		//CMN.Log(" "); CMN.Log(" "); CMN.Log(sb.toString());
	}

	static class Entry implements Comparable<Entry> {
		private String text;
		public String rerouteTarget;
		private String cptext;
		byte[] data;
		long contentStart;
		long contentEnd;
		long entryStart;
		long entryLength;
		long dataOffset;

		@Override
		public int compareTo(Entry o) {
			return text.compareToIgnoreCase(o.text);
			//return compareText().compareTo(processText(o.compareText()));
		}

		private String compareText() {
			return cptext==null?(cptext=processText(text)):cptext;
		}

		@NonNull
		@Override
		public String toString() {
			return "["+text+"-"+Integer.toHexString((int) contentStart)+"_"+Integer.toHexString((int) contentEnd)+"]";
		}
	}
	
	
	public void buildTextForTempEntry(Entry tmpEntry) {
		if(tmpEntry.data!=null && tmpEntry.text==null) {
			String text = new String(tmpEntry.data, (int)tmpEntry.dataOffset, (int)tmpEntry.entryLength, _charset).trim();
			if(text.contains("\\(")){
				text = text.replace("\\(", "(").replace("\\)", ")");
			}
			if(bIsUTF16 && text.startsWith(UTF16BOM))
				text = text.replace(UTF16BOM, "");
			int idx = text.indexOf("\n");
			if (idx>0) {
				text = text.substring(0, idx);
			}
			tmpEntry.text = stripNonSortParts(text);
			tmpEntry.data = null;
		}
	}
	
	static class IndexBuilder {
		ArrayList<String> appendixArr = new ArrayList<>();
		mdictBuilder mdb;
	}
	
	//构造
	public PlainDSL(File fn, MainActivityUIBase _a, mdict.AbsAdvancedSearchLogicLayer taskRecv) throws IOException {
		super(fn, _a);
		isDictZip = _Dictionary_fName.regionMatches(true, _Dictionary_fName.length()-3, ".dz", 0, 3);
		opt=_a.opt;
		mType = PLAIN_BOOK_TYPE.PLAIN_TYPE_DSL;
		
		_num_record_blocks=-1;
		
		mBlockSize = 1*block_size;

		mCacheItemCount = DefaultCacheItemCount;
		block_cache = new LinkastReUsageHashMap<>(mCacheItemCount);
		
		if(isDictZip) {
			try {
				dictReader = new DictInputStream.GZIPDictReader(f);
				file_length = dictReader.blockDecLength*dictReader.blockCount;
			} catch (IOException e) {
				if(GlobalOptions.debug)CMN.Log(e);
			}
		}
		mIndexFolder = _a.getExternalFilesDir("DSLIndex");
		mIndexFile = new File(mIndexFolder, f.getName());
		if(mIndexFile.exists())mIndexFile.delete();
		mIndexFolder = _a.getExternalFilesDir("DzIndex");
		mIndexFile = new File(mIndexFolder, f.getName()+".idx");
		//SU.Log("Scan Indexes...", mIndexFile);
		//if(false)
		if( mIndexFile.exists()) {
			try {
				CMN.rt();
				keyIndex = new mdict(mIndexFile.getPath());
				CMN.pt("恢复索引耗时：", keyIndex.getNumberEntries());
			} catch (IOException e) {
				if(GlobalOptions.debug) SU.Log(e);
			}
		}
		String md5 = null;
		if (keyIndex != null) {
			if(IU.parsint(keyIndex.getField("idxVer"), 0)!=1
				|| IU.parseLong(keyIndex.getField("idxLen"), 0)!=f.length())
				keyIndex = null;
			else {
				long lastModified = f.lastModified();
				if(mIndexFile.lastModified()!=lastModified) {
					// check md5
					md5 = BU.calcMD5(f.getPath());
					if(TextUtils.equals(keyIndex.getField("idxMd5"), md5)) {
						keyIndex = null;
					} else {
						mIndexFile.setLastModified(lastModified);
					}
				}
			}
		}
		
		if (keyIndex != null) {
			String charset = keyIndex.getField("idxEnc");
			setCharset(charset);
			_BOM_SIZE = IU.parsint(keyIndex.getField("idxBOM"));
		}
		else {
			// build the index
			if(f.length()>=2.33*1024*1024 &&
					!opt.getAutoBuildIndex() && CMN.mid==Thread.currentThread().getId()) {
				throw new IllegalStateException("Needs Index Building!");
			}
			InputStream data_in = mOpenInputStream(0);
			TextBlock tmpBlock=new_TextBlock();
			IndexBuilder db = new IndexBuilder();
			mdictBuilder mdb = db.mdb = new mdictBuilder(mIndexFile.getName(), "Index file for "+getDictionaryName(), "utf8");
			mdb.setDiacriticsInsensitive(true);
			mdb.setKeyCaseInsensitive(true);
			CMN.rt();
			ReusableByteOutputStream bos = new ReusableByteOutputStream(512);
			boolean checkTail=false;
			//int StartBlock=4198400/block_size-2; fis.skip(StartBlock*block_size);
			ArrayList<Entry> lastEntry=new ArrayList<>(8);
			int i = -1;
			tmpBlock.blockSize=mBlockSize;
			String name=null;
			DictInputStream dzInput = data_in instanceof DictInputStream? (DictInputStream) data_in :null;
			// split keys
			ScanBlocks:
			while(tmpBlock.blockSize==mBlockSize) {
				i++;
				tmpBlock.blockIndex = i;
				tmpBlock.read(data_in);
				if(taskRecv!=null) {
					taskRecv.dirtyTotalProgress = (int) file_length;
					if(dzInput!=null) {
						taskRecv.dirtyProgressCounter = dzInput.tellPosition();
					} else {
						taskRecv.dirtyProgressCounter+=tmpBlock.blockSize;
					}
					if(taskRecv.IsInterrupted || searchCancled)
						throw new IllegalStateException("Needs Index Building!");
				}
				if(i==0) {
					//CMN.Log("blockSize", tmpBlock.blockSize, new String(tmpBlock.data, 0, 10, StandardCharsets.UTF_16LE));
					CharsetDetector detector = new CharsetDetector();
					detector.setText(tmpBlock.data);
					CharsetMatch match = detector.detect();
					String charset = "utf8";
					if(match!=null && match.getConfidence()>=75)
						charset = match.getName();
					if(GlobalOptions.debug)CMN.Log("检测结果：", charset, isDictZip);
					setCharset(charset);
					if(compareByteArrayIsPara(tmpBlock.data, 0, UTF16BOMBYTES)) _BOM_SIZE=2;
					try {
						String infos = new String(tmpBlock.data, _BOM_SIZE, Math.min(128, tmpBlock.blockSize)/2*2-2, _charset);
						if(infos.charAt(0)=='#') {
							int idx = infos.indexOf("#NAME");
							if(idx>=0) {
								int idx1 = infos.indexOf("\n", idx);
								if(idx1>0) {
									name = infos.substring(idx+5, idx1).trim();
									name = name.substring(1, name.length()-1);
								}
							}
						}
						//CMN.Log("infos", infos, name);
					} catch (Exception ignored) { }
				}
				int entryBreak=0;
				//if(i%100==0)CMN.Log("区块", i, _num_record_blocks);
				//CMN.Log("区块", i, checkTail, bos.size(), new String(tmpBlock.data, 0, 500, _charset));
				if(!checkTail && bos.size()>0){ //entry residue
					int next;
					boolean bSemiContentTurn = false;
					if(endWithLineBreak(bos) && startWithContent(tmpBlock, 0)){
						bSemiContentTurn=true;
						next=0;
						bos.recess(lineBreakText.length);
					} else {
						next = findNextContentBreakIndex(tmpBlock, 0);
					}
					if(next>=0){
						if(bSemiContentTurn) {
							next += contentBreakText_length;
						} else {
							bos.write(tmpBlock.data, 0, next);
							next += lcl();
						}
						entryBreak = next;
						//CMN.Log("residue!!!", new String(bos.getBytes(), 0, bos.size(), _charset));
						addEntry(db, bos.getBytes(), 0, bos.size(), i*mBlockSize+entryBreak-contentBreakText_length, lastEntry);
						//fout.write(bos.getBytes(), 0, bos.size());
						//fout.write(lineBreakText);
					}
				}
				/* expecting one next \n\t break that brings in the contents. */
				while((entryBreak = findNextEntryBreakIndex(checkTail?bos:null, tmpBlock, entryBreak))>=0){
					if(entryBreak>0 || startWithBreak(tmpBlock))
						entryBreak+=lineBreakText.length;
					int next = findNextContentBreakIndex(tmpBlock, entryBreak);
					//CMN.Log("entryBreak", entryBreak, next);
					if(next>0){
						//CMN.Log(new String(tmpBlock.data, entryBreak, next-entryBreak, _charset));
						//fout.write(tmpBlock.data, entryBreak, next-entryBreak);
						//fout.write(lineBreakText);
						addEntry(db, tmpBlock.data, entryBreak, next-entryBreak, i*mBlockSize+next+lineBreakText.length, lastEntry);
						entryBreak=next+lcl();
					} else { /* but without any result */
						//CMN.Log("FNCBI without any result, \n* opened but not closed!", entryBreak);
						checkTail=false;
						bos.reset();
						bos.write(tmpBlock.data, entryBreak, tmpBlock.blockSize-entryBreak);
						continue ScanBlocks;
					}
				}
				bos.reset();
				if(tmpBlock.blockSize>lineBreakText.length) {
					checkTail = true;
					bos.write(tmpBlock.data, tmpBlock.blockSize - lineBreakText.length, lineBreakText.length);
				}
			}
			data_in.close();
			file_length = tmpBlock.blockIndex*(long)mBlockSize+tmpBlock.blockSize;
			if(lastEntry.size()>0) {
				for (Entry eI:lastEntry) {
					//CMN.Log("最后：：", eI.text);
					mdb.insert(eI.text, null, 0, eI.contentStart, file_length-eI.contentStart);
				}
			}
			for (int j = 0, len=db.appendixArr.size(); j < len; j+=2) {
				String rawName = db.appendixArr.get(j);
				String linkedName = db.appendixArr.get(j+1);
				mdictBuilder.myCprKey keyNode = mdb.lookUp(rawName);
				if(keyNode==null) {
					mdb.insert(rawName, null, 0, 0, 0);
					keyNode = mdb.lookUp(rawName);
				}
				if(keyNode!=null) {
					//SU.Log(rawName, " 嵌入其他链接 ", linkedName);
					if(keyNode.postProcessor==null)keyNode.postProcessor = new LinkedEntryBuilder();
					((LinkedEntryBuilder)keyNode.postProcessor).addLinkedEntry(linkedName);
				} else if(GlobalOptions.debug) {
					SU.Log("！！！未找到嵌入目标:>>>"+rawName+"<<<", rawName.length());
				}
			}
			//mdb.insert("happy", "hah");
			SU.pt(" 树大小："+entryCount);
			SU.pt(" 树大小："+mdb.getNumberEntries()+" 扫描文件耗时：");
			if(name!=null) mdb.setName(name);
			mdb.field("idxVer", 1);
			mdb.field("idxLen", f.length());
			mdb.field("idxEnc", _charset.name());
			mdb.field("idxBOM", _BOM_SIZE);
			mdb.field("stdSort", true);
			if(md5==null) {
				CMN.rt();
				md5 = BU.calcMD5(f.getPath());
				CMN.pt("计算Md5耗时：");
			}
			mdb.field("idxMd5", md5);
			SU.rt();
			mdb.setContentDelimiter(ArrayUtils.EMPTY_BYTE_ARRAY);
			mdb.setCompressionType(2);
			mdb.setIndexUnitSize(32);
			mdb.setRecordUnitSize(4);
			mdb.write(mIndexFile.getPath());
			SU.pt("写入词条耗时：");
			
			keyIndex = new mdict(mIndexFile.getPath());
			mIndexFile.setLastModified(f.lastModified());
		}
		if(file_length==0) file_length = f.length();
		_num_entries = keyIndex.getNumberEntries();
		_num_record_blocks = (long) Math.ceil(file_length*1.f/ mBlockSize);
		
		String path = f.getParent();
		String base = isDictZip?_Dictionary_fName.substring(0, _Dictionary_fName.length()-3):_Dictionary_fName;
		mResZipFile = new File(path, base + ".files.zip");
		if (mResZipFile.exists()) {
			hasResources = true;
		}
	}
	
	private void setCharset(String charset) {
		_charset = Charset.forName(charset);
		//_charset = StandardCharsets.UTF_16LE;
		if(_charset.equals(StandardCharsets.UTF_16LE) || _charset.equals(StandardCharsets.UTF_16BE)) {
			bIsUTF16=true;
		}
		else if(!_charset.equals(StandardCharsets.UTF_8))
			throw new IllegalArgumentException("invalid dsl encoding!");
		htmlOpenTagStr="[";
		htmlCloseTagStr="]";
		postGetCharset();
		lineBreakText = "\n".getBytes(_charset);
		contentBreakText = "\t".getBytes(_charset);
		contentBreakText1 = " ".getBytes(_charset);
		contentBreakText_length = contentBreakText.length;
		excludesNextBytes = new byte[][]{contentBreakText, contentBreakText1, "#".getBytes(_charset) }; //, "\r".getBytes(_charset)
	}
	
	static class LinkedEntryBuilder implements mdictBuilder.PostEntryValueProcessor {
		/** 重定向词条，链接词条1，链接词条2, ... */
		ArrayList<String> tags = new ArrayList<>();
		public void addLinkedEntry(String text) {
			if(tags.size()==0) tags.add("");
			tags.add(text);
		}
		public void setRerouteTarget(String text) {
			if(tags.size()==0) tags.add("");
			tags.set(0, text);
		}
		@Override
		public void processEntry(mdictBuilder builder, mdictBuilder.myCprKey entry) {
			if(tags.size()>0) {
				entry.value = StringUtils.join(tags, "\n");
				// 复刻重定向词条记录的开始与终止。（已经预先处理）
			}
		}
	}

	public final long readLong(byte[] readBuffer, int start) {
		return (((long)readBuffer[start+0] << 56) +
				((long)(readBuffer[start+1] & 255) << 48) +
				((long)(readBuffer[start+2] & 255) << 40) +
				((long)(readBuffer[start+3] & 255) << 32) +
				((long)(readBuffer[start+4] & 255) << 24) +
				((readBuffer[start+5] & 255) << 16) +
				((readBuffer[start+6] & 255) <<  8) +
				((readBuffer[start+7] & 255) <<  0));
	}
	
	
	protected void findInAllContentsForEntry(TextBlock tmpBlock_A, TextBlock tmpBlock_B, ArrayList<SearchResultBean> searchReceiver
			, byte[] data, int offset, int size, int contentStart, ArrayList<Entry> newEntrys, ArrayList<Entry> lastSibling
			, byte[][][][][] finalMatcher, Regex finalJoniregex, mdict.AbsAdvancedSearchLogicLayer searchLauncher, Flag flag
			, ReusableByteOutputStream rec_bos, boolean insert) throws IOException, InterruptedException {
		Entry newEntry = null;
		if(insert) {
			if(newEntrys.size()>0) {
				newEntry = newEntrys.remove(newEntrys.size()-1);
			} else {
				newEntry = new Entry();
			}
			newEntry.text = null;
			newEntry.contentStart=contentStart;
			newEntry.entryStart = contentStart-size;
			newEntry.data = data;
			newEntry.entryLength = size;
			newEntry.dataOffset = offset;
			if(data!=tmpBlock_A.data)
			{
				buildTextForTempEntry(newEntry);
			}
		}
		if(lastSibling!=null) {
			for (Entry eI:lastSibling) {
				eI.contentEnd = contentStart - size - lineBreakText.length;
				//SU.Log(tmpBlock_B.blockIndex, tmpBlock_A.blockIndex, "mBlockSize="+mBlockSize
				//		, eI.text+"@"+eI.contentStart, (int) (eI.contentStart/mBlockSize)
				//		, newEntry.text+"@"+newEntry.contentStart, (int) (newEntry.contentStart/mBlockSize));
				F1ag recordSt=new F1ag();
				byte[] record_block_ = getRecordDataForEntry(tmpBlock_A, tmpBlock_B, eI, rec_bos, recordSt);
				int recordLen = (int) (eI.contentEnd-eI.contentStart);
				recordLen = Math.min(recordLen, record_block_.length-recordSt.val);
				// 内容块读取完毕，接下来进行搜索。
				org.joni.Matcher Jonimatcher = null;
				if(finalJoniregex !=null)
					Jonimatcher = finalJoniregex.matcher(record_block_);
				
				int try_idx;
				if(Jonimatcher==null){
					try_idx=-1;
					ArrayList<ArrayList<Object>> mpk;
					ArrayList<Object> mParallelKeys;
					for (int j = 0; j < finalMatcher.length; j++) { // and group
						mpk = searchLauncher.mParallelKeys.get(j);
						for (int k = 0; k < finalMatcher[j].length; k++) { // or group
							mParallelKeys = mpk.get(k);
							int len = finalMatcher[j][k].length;
							int[] jumpMap = (int[]) mParallelKeys.get(len);
							try_idx=flowerIndexOf(record_block_,recordSt.val,recordLen, finalMatcher[j][k],0,0, searchLauncher, flag, mParallelKeys, jumpMap);
							//SU.Log("and_group>>"+j, "or_group#"+k, try_idx, nna);
							if(try_idx<0 ^ (jumpMap[len]&4)==0) break;
						}
						if(try_idx<0){
							break;
						}
					}
				} else {
					try_idx=Jonimatcher.searchInterruptible(recordSt.val, recordLen, Option.DEFAULT);
				}
				if(try_idx!=-1) {
					buildTextForTempEntry(eI);
//					if(eI.text.equals("woodie")) {
//						SU.Log("找到了 yesyes：", eI.text, eI.contentEnd-eI.contentStart, rec_bos.getBytes().length, try_idx, new String(record_block_, recordSt.val+try_idx, 10, _charset));
//						SU.Log("找到了 yesyes：", try_idx, eI.contentStart, eI.contentEnd-eI.contentStart);
//						SU.Log("找到了 yesyes：", new String(record_block_, recordSt.val, (int)(eI.contentEnd-eI.contentStart), _charset));
//						SU.Log("找到了 yesyes：", new String(record_block_, recordSt.val+try_idx, 1000, _charset));
//						SU.Log("找到了 yesyes：", new String(record_block_, recordSt.val+try_idx+10, 1000, _charset));
//						//SU.Log("找到了 yesyes：", new String(record_block_, recordSt.val+try_idx-200, 200, _charset));
//					}
//					SU.Log("找到了：", eI.text);
					searchLauncher.dirtyResultCounter++;
					int firstMat = lookUp(eI.text, true);
					if(firstMat>0) {
						int nxt = firstMat++;
						String cptext = processMyText(eI.text), nxtEty;
						while(nxt<_num_entries && processMyText(nxtEty=getEntryAt(nxt)).equals(cptext)) {
							if(eI.text.equals(nxtEty)) {
								firstMat = nxt;
								break;
							}
							nxt++;
						}
					}
					searchReceiver.add(new SearchResultBean(firstMat));
				}
				break;
			}
			searchLauncher.dirtyProgressCounter++;
			newEntrys.addAll(lastSibling);
			lastSibling.clear();
			if(insert)lastSibling.add(newEntry);
		}
	}
	
	private boolean isKeyCaseSensitive=false;
	private boolean isStripKey=true;
	protected String processMyText(String input) {
		String ret = isStripKey?mdict.replaceReg.matcher(input).replaceAll(""):input;
		return isKeyCaseSensitive?ret:ret.toLowerCase();
	}
	
	final Pattern patterNonSortParts = Pattern.compile("\\{.*?\\}|\r");
	
	/** see https://documentation.help/ABBYY-Lingvo8/unsorted_head_part.htm */
	private String stripNonSortParts(String text) {
		if(text.contains("{"))
			return patterNonSortParts.matcher(text).replaceAll("");
		return SU.trimEnd(text);
		//return text.replace("{·}", "");
	}
	
	int entryCount=0;
	
	private void addEntry(IndexBuilder db, byte[] data, int offset, int size, int contentStart, ArrayList<Entry> lastSibling) {
		String text = new String(data, offset, size, _charset).trim();
		//CMN.Log("addEntry::", text);
		if(text.contains("\\("))
		{
			text = text.replace("\\(", "(").replace("\\)", ")");
		}
		String[] arr = text.split("\n");
		
		Entry newEntry = new Entry();
		entryCount++;
		
		String entryFullName = arr[0];
		if(bIsUTF16 && text.startsWith(UTF16BOM))
			entryFullName = entryFullName.replace(UTF16BOM, "");
		newEntry.text = stripNonSortParts(entryFullName);
		newEntry.contentStart=contentStart;
		newEntry.entryStart = contentStart-size;
		
		if(lastSibling!=null) {
			for (Entry eI:lastSibling) {
				mdictBuilder.myCprKey keyNode = db.mdb.insert(eI.text, null, 0, eI.contentStart, newEntry.entryStart - lineBreakText.length - eI.contentStart);
				if(eI.rerouteTarget!=null) {
					// 重定向
					if(keyNode.postProcessor==null)keyNode.postProcessor = new LinkedEntryBuilder();
					((LinkedEntryBuilder)keyNode.postProcessor).setRerouteTarget(eI.rerouteTarget);
				}
				//CMN.Log("mdb.insert::", eI.text);
			}
			lastSibling.clear();
			lastSibling.add(newEntry); // waiting to be inserted...
		}
		//CMN.Log("array::", StringUtils.join(arr, ","));
		if (arr.length > 1) {
			for (int i = 1; i < arr.length; i++) {
				String tI = SU.trimEnd(arr[i]);
				//CMN.Log("array items::", tI, tI.endsWith(")}"), tI.contains("{("));
				//String[] arrI = tI.split(VerbatimSearchTask.RegExp_VerbatimDelimiter);
				if (tI.endsWith(")}") && tI.contains("{(")) { //advanced append
					//CMN.Log("通常是在别的词条下嵌入当前词条", tI);
					int idx = tI.indexOf("{(");
					String linkedName = tI.substring(idx+2, tI.length()-2);
					if (tI.charAt(idx - 1) == ' ')
						--idx;
					String rawName = SU.trimEnd(tI.substring(0, idx));
					db.appendixArr.add(rawName); // 原词
					db.appendixArr.add(linkedName); // 嵌入词
				} else { //alias
					// 别名而已
					Entry entry = new Entry();
					entry.text = stripNonSortParts(tI);
					entry.contentStart=newEntry.contentStart;
					entry.entryStart = newEntry.entryStart;
					entry.rerouteTarget = newEntry.text;
					lastSibling.add(entry);
				}
			}
		}
	}
	
	/** Find line beak followed by a new entry. */
	private int findNextEntryBreakIndex(ReusableByteOutputStream lastTmpBlock, TextBlock tmpBlock, int nowIndex) {
		if(nowIndex==0 && (tmpBlock.blockIndex==0&&startWithEntry(tmpBlock, nowIndex+_BOM_SIZE)
				|| lastTmpBlock!=null && endWithLineBreak(lastTmpBlock) && startWithEntry(tmpBlock, nowIndex))){
			//CMN.Log("接天莲叶无穷碧", lastTmpBlock==null);
			return 0;
		}
		while((nowIndex=mdict.indexOf(tmpBlock.data, 0, tmpBlock.blockSize, lineBreakText, 0, lineBreakText.length, nowIndex))>=0){
			if(!bIsUTF16 || nowIndex%2==0){
				if(startWithEntry(tmpBlock, nowIndex+lineBreakText.length))
					return nowIndex;
			}
			nowIndex += lineBreakText.length;
		}
		return -1;
	}

	/** Find line beak followed by contents. return the start of contents. */
	private int findNextContentBreakIndex(TextBlock tmpBlock,int nowIndex) {
		while((nowIndex=mdict.indexOf(tmpBlock.data, 0, tmpBlock.blockSize, lineBreakText, 0, lineBreakText.length, nowIndex))>=0){
			if(!bIsUTF16 || nowIndex%2==0){
				if(startWithContent(tmpBlock, nowIndex+lineBreakText.length))
					return nowIndex;
			}
			nowIndex += lineBreakText.length;
		}
		return -1;
	}

	private int findNextBreakIndex(TextBlock tmpBlock,int nowIndex) {
		int lastIndex=nowIndex;
		while((lastIndex=mdict.indexOf(tmpBlock.data, 0, tmpBlock.blockSize, lineBreakText, 0, lineBreakText.length, lastIndex))>=0){
			if(!bIsUTF16 || lastIndex%2==0){
				return lastIndex;
			}
			lastIndex += lineBreakText.length;
		}
		return -1;
	}

	private boolean startWithEntry(TextBlock tmpBlock, int nowIndex) {
		if(nowIndex>=tmpBlock.blockSize) return false;
		for(byte[] eI:excludesNextBytes){
			if(compareByteArrayIsPara(tmpBlock.data, nowIndex, tmpBlock.blockSize, eI))
				return false;
		}
		return true;
	}

	int lcl() {
		return lineBreakText.length + contentBreakText_length;
	}

	private boolean startWithContent(TextBlock tmpBlock, int nowIndex) {
		if(compareByteArrayIsPara(tmpBlock.data, nowIndex, tmpBlock.blockSize, contentBreakText)) {
			contentBreakText_length = contentBreakText.length;
			return true;
		}
		if(compareByteArrayIsPara(tmpBlock.data, nowIndex, tmpBlock.blockSize, contentBreakText1)) {
			contentBreakText_length = contentBreakText1.length;
			return true;
		}
		return false;
	}

	private boolean startWithBreak(TextBlock tmpBlock) {
		if(compareByteArrayIsPara(tmpBlock.data, 0, lineBreakText))
			return true;
		return false;
	}

	private boolean endWithLineBreak(ReusableByteOutputStream tmpBlock) {
		if(compareByteArrayIsPara(tmpBlock.getBytes(), tmpBlock.size()-lineBreakText.length, lineBreakText))
			return true;
		return false;
	}
	
//	@Override
//	protected void onPageSaved() {
//		super.onPageSaved();
//		a.notifyDictionaryDatabaseChanged(PlainDSL.this);
//	}

//	@Override
//	String getSimplestInjection() {
//		return js;
//	}

	@Override
	public String getEntryAt(long position) {
		if(position==-1) return "about:";
		try {
			return keyIndex.getEntryAt(position);
		} catch (Exception e) {
			return "!!!"+position;
		}
	}

	@Override
	public String getEntryAt(long position, Flag mflag) {
		return getEntryAt(position);
	}

	@Override
	public int lookUp(String keyword, boolean isStrict) {
		return keyIndex.lookUp(keyword, isStrict);
	}
	
	@Override
	public int lookUpRange(String keyword, ArrayList<myCpr<String, Long>> rangReceiver, RBTree_additive treeBuilder, long SelfAtIdx, int theta, AtomicBoolean task) {
		return keyIndex.lookUpRange(keyword, rangReceiver, treeBuilder, SelfAtIdx, theta, task);
		// todo std lookUpRange
	}
	
	public int reduce(ArrayList<Entry> data, String val, int start, int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			return val.compareToIgnoreCase(data.get(start + len - 1).text)>0
					? reduce(data, val,start+len,end)
					: reduce(data, val,start,start+len);
		} else {
			return start;
		}
	}

	@Override
	public InputStream getResourceByKey(String key) {
		//CMN.Log("lingvo::getResourceByKey", key);
		if (hasResources) {
			try {
				if(mResZip ==null) mResZip = new ZipFile(mResZipFile);
				ZipEntry ent = mResZip.getEntry(key.substring(1));
				if (ent!=null) return mResZip.getInputStream(ent);
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		return null;
	}

//	@Override
//	protected InputStream mOpenInputStream() throws FileNotFoundException {
//		return new FileInputStream(f);
//	}

//	@Override
//	protected boolean StreamAvailable() {
//		return false;
//	}
	
	
	@Override
	public void flowerFindAllKeys(String key, Object book, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
		keyIndex.flowerFindAllKeys(key, book, SearchLauncher);
	}
	
	public void flowerFindAllContents(String key, Object book, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
		//SU.Log("Find In All Contents Stated");
		byte[][][][][] matcher=null;
		Regex Joniregex = null;
		if(SearchLauncher.getSearchEngineType()==1){
			if(encoding==null) mdict.bakeJoniEncoding(_charset);
			if(encoding!=null) {
				//if (getRegexAutoAddHead() && !key.startsWith(".*"))
				//	key = ".*" + key;
				byte[] pattern = key.getBytes(_charset);
				Joniregex = new Regex(pattern, 0, pattern.length, getRegexOption(), encoding);
			}
		}
		if(Joniregex==null) {
			matcher =  leafSanLieZhi(SearchLauncher, _charset);
		}

		split_recs_thread_number = _num_entries<6?1:(int) (_num_entries/6);//Runtime.getRuntime().availableProcessors()/2*2+10;
		split_recs_thread_number = split_recs_thread_number>16?6:split_recs_thread_number;
		final int thread_number = Math.min(Runtime.getRuntime().availableProcessors()/2*2+2, split_recs_thread_number);
		SU.Log("split_recs_thread_number", split_recs_thread_number);
		SU.Log("thread_number", thread_number);
		
		/* 调试调试调试 */
		//split_recs_thread_number = 1;
		/* 调试调试调试 */

		final int step = (int) (_num_record_blocks/split_recs_thread_number);
		final int yuShu=(int) (_num_record_blocks%split_recs_thread_number);
		
		ArrayList<SearchResultBean>[] _combining_search_tree=SearchLauncher.getTreeBuilding(book, split_recs_thread_number);

		ConcurrentHashMap<Integer, TextBlock> cache_tmp = new ConcurrentHashMap<>(block_cache);
		SearchLauncher.poolEUSize.set(SearchLauncher.dirtyProgressCounter=0);
		block_cache.syncAccommodationSize();
		//ArrayList<Thread> fixedThreadPool = new ArrayList<>(thread_number);
		ExecutorService fixedThreadPool = OpenThreadPool(thread_number);
		for(int ti=0; ti<split_recs_thread_number; ti++){//分  thread_number 股线程运行
			//SU.Log("执行", ti , split_recs_thread_number);
			if(SearchLauncher.IsInterrupted || searchCancled) break;
			final int it = ti;
			if(split_recs_thread_number>thread_number) while (SearchLauncher.poolEUSize.get()>=thread_number) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if(_combining_search_tree[it]==null)
				_combining_search_tree[it] = new ArrayList<>();

			if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(1);

			Regex finalJoniregex = Joniregex;
			byte[][][][][] finalMatcher = matcher;
			fixedThreadPool.execute(
					new Runnable(){@Override public void run()
					{
						if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
						final ReusableByteOutputStream bos = new ReusableByteOutputStream(mBlockSize *2);//!!!避免反复申请内存
						final ReusableByteOutputStream rec_bos = new ReusableByteOutputStream(mBlockSize *2);//!!!避免反复申请内存
						Flag flag = new Flag();
						long toSkip=it*step*mBlockSize;
						try(InputStream data_in = mOpenInputStream(toSkip)) // 避免重复打开文件
						{
							CMN.rt();
							boolean checkTail=false;
							//int StartBlock=4198400/block_size-2; fis.skip(StartBlock*block_size);
							ArrayList<Entry> newEntrys=new ArrayList<>(8);
							ArrayList<Entry> lastEntry=new ArrayList<>(8);
							int jiaX=0;
							if(it==split_recs_thread_number-1) jiaX=yuShu;
							TextBlock tmpBlock_A=new_TextBlock();
							TextBlock tmpBlock_B=new_TextBlock();
							TextBlock tmpBlock_tmp;
							tmpBlock_A.blockIndex = -1;
							boolean reading=true;
							// split keys
							ScanBlocks:
							for(int i=it*step,len=it*step+step+jiaX; i<len; i+=1)//_num_entries
							{
								for (int j = 0; j < lastEntry.size(); j++) {
									buildTextForTempEntry(lastEntry.get(j));
								}
								if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
								tmpBlock_tmp = tmpBlock_B;
								tmpBlock_B = tmpBlock_A;
								tmpBlock_A = tmpBlock_tmp;
								
								tmpBlock_A.blockIndex = i;
								tmpBlock_A.read(data_in);
								
								int entryBreak=0;
								//CMN.Log("区块", i+"/"+_num_record_blocks, checkTail, bos.size(), new String(tmpBlock_A.data, 0, 20, _charset));
								if(!checkTail && bos.size()>0){ //entry residue
									int next;
									boolean bSemiContentTurn = false;
									if(endWithLineBreak(bos) && startWithContent(tmpBlock_A, 0)){
										bSemiContentTurn=true;
										next=0;
										bos.recess(lineBreakText.length);
									} else {
										next = findNextContentBreakIndex(tmpBlock_A, 0);
									}
									if(next>=0){
										if(bSemiContentTurn) {
											next += contentBreakText_length;
										} else {
											bos.write(tmpBlock_A.data, 0, next);
											next += lcl();
										}
										entryBreak = next;
										findInAllContentsForEntry(tmpBlock_A, tmpBlock_B, _combining_search_tree[it], bos.getBytes(), 0, bos.size(), i*mBlockSize+entryBreak-contentBreakText_length, newEntrys, lastEntry, finalMatcher, finalJoniregex, SearchLauncher, flag, rec_bos, reading);
									}
								}
								/* expecting one next \n\t break that brings in the contents. */
								while((entryBreak = findNextEntryBreakIndex(checkTail?bos:null, tmpBlock_A, entryBreak))>=0){
									if(SearchLauncher.IsInterrupted  || searchCancled ) break;
									if(entryBreak>0 || startWithBreak(tmpBlock_A))
										entryBreak+=lineBreakText.length;
									int next = findNextContentBreakIndex(tmpBlock_A, entryBreak);
									//CMN.Log("entryBreak", entryBreak, next);
									if(next>0) {
										findInAllContentsForEntry(tmpBlock_A, tmpBlock_B, _combining_search_tree[it], tmpBlock_A.data, entryBreak, next-entryBreak, i*mBlockSize+next+lineBreakText.length, newEntrys, lastEntry, finalMatcher, finalJoniregex, SearchLauncher, flag, rec_bos, reading);
										entryBreak=next+lcl();
									} else { /* but without any result */
										//CMN.Log("FNCBI without any result, \n* opened but not closed!", entryBreak);
										checkTail=false;
										bos.reset();
										bos.write(tmpBlock_A.data, entryBreak, tmpBlock_A.blockSize-entryBreak);
										if(i==len-1 && reading) {
											//CMN.Log("老骥伏枥志在千里");
											if(len+1<_num_record_blocks)len++;
											reading=false;
										}
										continue ScanBlocks;
									}
								}
								bos.reset();
								if(tmpBlock_A.blockSize>lineBreakText.length) {
									checkTail = true;
									bos.write(tmpBlock_A.data, tmpBlock_A.blockSize - lineBreakText.length, lineBreakText.length);
								}
								if(lastEntry.size()>0 && i==len-1 && reading) {
									//CMN.Log("老当益壮穷且益坚");
									if(len+1<_num_record_blocks)len++;
									reading=false;
								}
							}
							if(lastEntry.size()>0) {
								findInAllContentsForEntry(tmpBlock_A, tmpBlock_B, _combining_search_tree[it], null, 0, 0, (int) file_length, newEntrys, lastEntry, finalMatcher, finalJoniregex, SearchLauncher, flag, rec_bos, false);
							}
						} catch (Exception e) {
							CMN.Log(e);
						}
						SearchLauncher.thread_number_count--;
						if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(-1);
					}}
			);
		}
		SearchLauncher.currentThreads=fixedThreadPool;
		fixedThreadPool.shutdown();
		try {
			fixedThreadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (Exception e1) {
			SU.Log("Find In Full Text Interrupted!!!");
		} finally {
			int size = block_cache.size();
			block_cache.putAll(cache_tmp);
			//a.root.postDelayed(() -> a.showT(CMN.Log("添加后", block_cache.size(),block_cache.size()-size)), 500);
		}
	}
	
	@Override
	public String getRichDescription() {
		return new StringBuilder()
				.append(_Dictionary_fName).append("<br>")
				.append(f.getAbsolutePath()).append("<br>")
				.append(f.lastModified()).append("<br>")
				.append(_charset).append("<br>")
				.append(mp4meta.utils.CMN.formatSize(f.length())).append("<br>")
				.append("索引v").append(keyIndex.getField("idxVer"))
				.append("&nbsp;&nbsp;：").append(keyIndex.getRichDescription()).append("<br>")
				.toString();
	}
	
	@Override
	public String getDictInfo() {
		return keyIndex.getDictInfo();
	}

}