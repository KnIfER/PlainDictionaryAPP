package com.knziha.plod.dictionarymodels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.knziha.plod.dictionary.GetRecordAtInterceptor;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.LinkastReUsageHashMap;
import com.knziha.plod.dictionary.Utils.ReusableByteOutputStream;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.files.ArrayListTree;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RBTree;
import com.knziha.rbtree.RBTree_additive;
import com.knziha.rbtree.additiveMyCpr1;

import org.adrianwalker.multilinestring.Multiline;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knziha.plod.dictionary.mdBase.compareByteArrayIsPara;
import static com.knziha.plod.dictionary.mdBase.processText;

/**
 Model handling local lingvo-dsl files.<br/>
 date:2020.2.18<br/>
 author:KnIfER<br/>
 licence:GPL2.0<br/>
*/
public class PlainDSL extends DictionaryAdapter {
	final static String UTF16BOM = new String(new byte[]{(byte) 0xff, (byte) 0xfe}, StandardCharsets.UTF_16LE);
	final static Pattern entryLinkPattern = Pattern.compile("<<(.*?)>>");
	final static Pattern dslTagPattern = Pattern.compile("\\[(.{1,50}?)]|(\t)");
	final static byte[] UTF8LineBreakText = "\n".getBytes(StandardCharsets.UTF_8);
	private final int lcl;
	private boolean bIsUTF16;
	private long file_length;
	byte[] lineBreakText;
	byte[] contentBreakText;
	byte[][] excludesNextBytes;
	final static int block_size = 4096;
	int mBlockSize = block_size;
	final static int DefaultCacheItemCount = 1024*1024/4096;
	int mCacheItemCount;
	class TextBlock{
		int blockIndex;
		int breakIndex=-1;
		int blockSize;
		byte[] data = new byte[mBlockSize];
		@NonNull
		@Override
		public String toString() {
			return "#"+blockIndex+":"+breakIndex+"/"+blockSize+"≈"+new String(data, 0, 100, _charset);
		}
	}
	LinkastReUsageHashMap<Integer, TextBlock> block_cache;
	RBTree<Entry> index_tree = new RBTree<>();
	RBTree_additive appendix = new RBTree_additive();
	ArrayListTree<Entry> index_array = new ArrayListTree<>();
	
	final File mIndexFolder;
	final File mIndexFile;
	
	void dumpEntries() throws IOException {
		ArrayList<Entry> data = index_array.getList();
		mIndexFolder.mkdirs();
		FileOutputStream fout = new FileOutputStream(new File(mIndexFolder, f.getName()));
		int DSLIndexBlockSize = 4096;
		byte[] buff = new byte[DSLIndexBlockSize];
		ReusableByteOutputStream bos;
		DataOutputStream data_out = new DataOutputStream(bos=new ReusableByteOutputStream(buff, DSLIndexBlockSize));
		int count=0; int size;
		byte[] name;
		boolean init=false;
		/* writing normal entries, including rerouting ones. */
		_num_entries = data.size();
		CMN.Log("dumpEntries", _num_entries);
		ArrayList<additiveMyCpr1> appendix_arr = appendix.flatten();
		int appendSize = appendix_arr.size();
		int U8BTL = UTF8LineBreakText.length;
		for (int i = 0; i <= _num_entries; i++) {//4k对齐写入
			if(!init){
				name = "EXTIDX".getBytes(StandardCharsets.UTF_8);
				data_out.write(name);
				data_out.write(UTF8LineBreakText);
				data_out.writeLong(0);
				data_out.writeLong(_num_entries);
				count=name.length+U8BTL+16;
				init=true;
			}
			Entry entry;
			if(i==_num_entries){
				entry = new Entry();
				entry.text="APPEND";
				entry.contentStart=appendSize;
			} else {
				entry = data.get(i);
			}
			name = entry.text.getBytes(StandardCharsets.UTF_8);
			size=name.length+U8BTL+16;
			if(count+size > DSLIndexBlockSize){//溢出写入
				byte[] source = bos.getBytes();
				Arrays.fill(source, count, DSLIndexBlockSize, (byte) 0);
				fout.write(source);
				fout.flush();
				bos.reset();
				count = 0;
			}
			data_out.write(name);
			data_out.write(UTF8LineBreakText);
			data_out.writeLong(entry.contentStart);
			data_out.writeLong(entry.contentEnd);
			count+=size;
		}
		/* writing gd appending entries, including duplicated ones. */
		for (int i = 0; i < appendSize; i++) {
			OUT:
			if(true){
				String keyText = appendix_arr.get(i).key;
				ArrayList arr = (ArrayList) appendix_arr.get(i).value;
				name = keyText.getBytes(StandardCharsets.UTF_8);
				size = name.length+U8BTL; if(count+size > DSLIndexBlockSize){break OUT;}
				data_out.write(name);
				data_out.write(UTF8LineBreakText);
				for (Object oI:arr) {
					if(oI instanceof String){
						name = ((String)oI).getBytes(StandardCharsets.UTF_8);
						size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
						data_out.write(name);
						data_out.write(UTF8LineBreakText);
						data_out.writeLong(0);
						data_out.writeLong(0);
					} else if(oI instanceof Entry){
						Entry entry = ((Entry) oI);
						name = entry.text.getBytes(StandardCharsets.UTF_8);
						size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
						data_out.write(name);
						data_out.write(UTF8LineBreakText);
						data_out.writeLong(entry.contentStart);
						data_out.writeLong(entry.contentEnd);
					}
				}
				name = "APPEND".getBytes(StandardCharsets.UTF_8);
				size += name.length+U8BTL+16; if(count+size > DSLIndexBlockSize){break OUT;}
				data_out.write(name);
				data_out.write(UTF8LineBreakText);
				data_out.writeLong(-1);
				data_out.writeLong(-1);
				count+=size;
				continue;
			}
			//溢出写入
			byte[] source = bos.getBytes();
			Arrays.fill(source, count, DSLIndexBlockSize, (byte) 0);
			fout.write(source);
			fout.flush();
			bos.reset();
			count = 0;
		}
		byte[] source = bos.getBytes();
		if(count>0 && count<=DSLIndexBlockSize) { //正常写入
			fout.write(source ,0, count);
			fout.flush();
			fout.close();
		}
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
	@Multiline
	final static String stylesheet="stylesheet";
	
	@Override
	public String getRecordAt(int position, GetRecordAtInterceptor getRecordAtInterceptor, boolean allowJump) throws IOException {
		if(getRecordAtInterceptor!=null)
		{
			String ret = getRecordAtInterceptor.getRecordAt(this, position);
			if (ret!=null) {
				return ret;
			}
		}
		StringBuffer sb = new StringBuffer();
		getRecordsAt_internal(sb, index_array.itemAt(position), 0);
		return sb.toString();
	}

	private void getRecordsAt_internal(StringBuffer sb, Entry eI, int depth) throws IOException {
		int centerBlock = (int) (eI.contentStart/mBlockSize);
		int length = (int) (eI.contentEnd - eI.contentStart);
		ReusableByteOutputStream bos = new ReusableByteOutputStream(mBlockSize *2);
		bos.reset();
		InputStream fin = BU.SafeSkipReam(new FileInputStream(f), centerBlock*mBlockSize);
		TextBlock tmpBlock;
		TextBlock tmpLastBlock;
		int toSkip=0; int cc=0;
		while(length>0 && centerBlock<_num_record_blocks) {
			tmpBlock = block_cache.get(centerBlock);
			if(tmpBlock==null){
				if(toSkip>0) {
					BU.SafeSkipReam(fin, toSkip);
					toSkip=0;
				}
				tmpBlock = new TextBlock();
				tmpBlock.blockIndex = centerBlock;
				tmpBlock.blockSize = fin.read(tmpBlock.data);
				block_cache.put(centerBlock, tmpBlock);
			} else {
				//CMN.Log("找到DSL缓存区");
				toSkip+=tmpBlock.blockSize;
			}
			tmpLastBlock=tmpBlock;
			int start = (int) Math.max(0, eI.contentStart - (centerBlock * mBlockSize));
			int len = Math.min(tmpBlock.blockSize - start, length);
			bos.write(tmpBlock.data, start, len);
			length-=len;
			centerBlock++;
		}
		fin.close();
		String val = new String(bos.getBytes(), 0, bos.size(), _charset);
		//CMN.Log(" "); CMN.Log(" "); CMN.Log(val);
		ConvertDslToHtml(sb, eI, val, depth);
	}

	public void ConvertDslToHtml(StringBuffer sb, Entry entry, String dsl, int depth) throws IOException {
		boolean bIsStart = sb.length()==0;
		dsl = entryLinkPattern.matcher(dsl).replaceAll("<a href='entry://$1'>$1</a>");
		Matcher m = dslTagPattern.matcher(dsl);
		sb.append(stylesheet);
		if(bIsStart){
			sb.append("<h3 class='_DSL_HEAD'>"+entry.text+"</h3>");
		}
		boolean replace;
		while(m.find()){
			String val = m.group(1);
			int start=m.start();
			if(val==null){
				val = m.group(2);
				if(start>0 && start+2<dsl.length() && !(dsl.charAt(start+1)=='['&&dsl.charAt(start+2)=='m')){
					m.appendReplacement(sb, "<br/>");
				}
			} else {
				replace=true;
				if (start > 0 && val.length() > 0 && dsl.charAt(start - 1) == '\\') {
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
				//	if(dsl.charAt(start)=='\t')
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
								sb.append("span");
								if (!closing) {
									sb.append(" style='display:none'");
									break ParseTagWithArgs;
								}
							break;
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
									sb.append(" class='_DSL_OPT'");
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
		RBTNode<additiveMyCpr1> append = appendix.searchByString(entry.text);
		if(append!=null){
			ArrayList arr = (ArrayList) append.getKey().value;
			//CMN.Log("追加???", entry, "<---", arr.toArray().length);
			//CMN.Log( arr.toArray() );
			for (Object oI:arr) {
				Entry enpentry = null;
				if(oI instanceof String){
					String appendKey = (String) oI;
					int idx=lookUp(appendKey);
					if(idx>=0 && idx<index_array.size()){
						enpentry = index_array.itemAt(idx);
					}
				} else if(oI instanceof Entry){
					enpentry=(Entry)oI;
				}
				if(enpentry!=null && enpentry!=entry && depth<=3) {
					sb.append("<br/><p>➟").append(enpentry.text).append("</p>");
					getRecordsAt_internal(sb, enpentry, ++depth);
				}
			}
		}
		//CMN.Log(" "); CMN.Log(" "); CMN.Log(sb.toString());
	}

	static class Entry implements Comparable<Entry> {
		private String text;
		private String cptext;
		byte[] data;
		long contentStart;
		long contentEnd;
		long entryStart;

		@Override
		public int compareTo(Entry o) {
			return text.compareToIgnoreCase(o.text);
			//return compareText().compareTo(processText(o.compareText()));
		}

		private String compareText() {
			return cptext==null?(cptext=processText(text)):cptext;
		}

		private byte[] data(Charset charset) {
			if(data==null) data = text.getBytes(charset);
			return data;
		}

		@NonNull
		@Override
		public String toString() {
			return "["+text+"-"+Integer.toHexString((int) contentStart)+"_"+Integer.toHexString((int) contentEnd)+"]";
		}
	}
	
	//构造
	public PlainDSL(File fn, MainActivityUIBase _a) throws IOException {
		super(fn, _a);
		opt=_a.opt;
		mType = PLAIN_BOOK_TYPE.PLAIN_TYPE_DSL;
		
		_num_record_blocks=-1;
		
		mBlockSize = 1*block_size;
		//readInConfigs(a.UIProjects);

		mCacheItemCount = DefaultCacheItemCount;
		block_cache = new LinkastReUsageHashMap<>(mCacheItemCount);

		FileInputStream fis = new FileInputStream(f);
		TextBlock tmpBlock=new TextBlock();
		tmpBlock.blockSize = fis.read(tmpBlock.data);

		//block_cache.put(0, tmpBlock);

		CharsetDetector detector = new CharsetDetector();
		detector.setText(tmpBlock.data);
		CharsetMatch match = detector.detect();
		String charset = "utf8";
		if(match!=null && match.getConfidence()>=75)
			charset = match.getName();
		//CMN.Log("检测结果：", charset);
		_charset = Charset.forName(charset);
		if(_charset.equals(StandardCharsets.UTF_16LE) || _charset.equals(StandardCharsets.UTF_16BE))
			bIsUTF16=true;
		else if(!_charset.equals(StandardCharsets.UTF_8))
			throw new IllegalArgumentException("invalid dsl encoding!");
		lineBreakText = "\n".getBytes(_charset);
		contentBreakText = "\t".getBytes(_charset);
		lcl = lineBreakText.length+contentBreakText.length;
		excludesNextBytes = new byte[][]{ "#".getBytes(_charset), "\t".getBytes(_charset)};
		file_length = f.length();
		_num_record_blocks = (long) Math.ceil(file_length*1.f/ mBlockSize);
		//if(bgColor==null)
		//	bgColor=CMN.GlobalPageBackground;


		boolean scan = true;
		CMN.Log("Scan Indexes...");
		
		mIndexFolder = _a.getExternalFilesDir("DSLIndex");
		mIndexFile = new File(mIndexFolder, f.getName());
		if( mIndexFile.exists() && mIndexFile.lastModified()>=f.lastModified()){
			CMN.rt();
			try {
				FileInputStream fin = new FileInputStream(mIndexFile);
				int DSLIndexBlockSize = 4096;
				int sourceCount, idx;
				int now;
				byte[] source = new byte[DSLIndexBlockSize];
				ArrayList<Entry> index_data = new ArrayList<>();
				boolean init=true;
				//4k对齐读取。
				boolean appending=false;
				ReadBlocks:
				while ((sourceCount = fin.read(source, 0, DSLIndexBlockSize)) > 0) {
					now=0;
					while((idx= mdict.indexOf(source, 0, sourceCount, UTF8LineBreakText, 0, UTF8LineBreakText.length, now))>0){
						if(!appending) {
							Entry entry = new Entry();
							entry.text = new String(source, now, idx - now);
							entry.contentStart = readLong(source, idx + UTF8LineBreakText.length);
							entry.contentEnd = readLong(source, idx + UTF8LineBreakText.length + 8);
							if (init && entry.text.equals("EXTIDX")) {
								_num_entries = entry.contentEnd;
								index_data = new ArrayList<>((int) _num_entries);
								init = false;
								CMN.Log("EI HEAD: N#"+_num_entries);
							} else if (entry.contentStart >= 0 && entry.contentEnd > entry.contentStart) {
								index_data.add(entry);
							} else if (entry.text.equals("APPEND")) {
								appending = true;
							}
							now = idx + UTF8LineBreakText.length + 16;
						} else {
							String target = new String(source, now, idx - now);
							now = idx + UTF8LineBreakText.length;
							//CMN.Log("appending...", target);
							while((idx= mdict.indexOf(source, 0, sourceCount, UTF8LineBreakText, 0, UTF8LineBreakText.length, now))>0) {
								Entry entry = new Entry();
								entry.text = new String(source, now, idx - now);
								entry.contentStart = readLong(source, idx + UTF8LineBreakText.length);
								entry.contentEnd = readLong(source, idx + UTF8LineBreakText.length + 8);
								now = idx + UTF8LineBreakText.length + 16;
								//CMN.Log("---append :", entry.text, entry.contentStart, entry.contentEnd);
								if (entry.contentStart >= 0 && entry.contentEnd > entry.contentStart) {
									appendix.insert(target, entry);
								} else if (entry.contentStart == 0 && entry.contentEnd == 0) {
									appendix.insert(target, entry.text);
								} else if (entry.text.equals("APPEND")) {
									//CMN.Log("append end...", target);
									break;
								}
							}
						}
					}
				}
				index_array = new ArrayListTree<>(index_data);
				_num_entries = index_array.size();
				CMN.pt(" 树大小："+_num_entries+"恢复索引耗时：");
				CMN.Log("recoverEntries", _num_entries);
				scan=false;
			} catch (Exception e) { if(GlobalOptions.debug)CMN.Log(e); }
		}

		if(scan) {
			fis = new FileInputStream(f);
			CMN.rt();
			ReusableByteOutputStream bos = new ReusableByteOutputStream(512);
			boolean checkTail=false;
			//int StartBlock=4198400/block_size-2; fis.skip(StartBlock*block_size);
			ArrayList<Entry> lastEntry=new ArrayList<>(8);
			ScanBlocks:
			//for (int i = StartBlock; i < StartBlock+100; i++) {
			for (int i = 0; i < _num_record_blocks; i++) {
				tmpBlock.blockIndex = i;
				tmpBlock.blockSize = fis.read(tmpBlock.data);
				int entryBreak=0;
				//CMN.Log("区块", i, checkTail, bos.size(), new String(tmpBlock.data, 0, 500, _charset));
				if(!checkTail && bos.size()>0){ //entry residue
					int next;
					boolean bSemiContentTurn = false;
					if(endWithLineBreak(bos) && startWithContent(tmpBlock, 0)){
						bSemiContentTurn=true;
						next=0;
						bos.recess(lineBreakText.length);
					}else{
						next = findNextContentBreakIndex(tmpBlock, 0);
					}
					if(next>=0){
						if(bSemiContentTurn) {
							next += contentBreakText.length;
						} else {
							bos.write(tmpBlock.data, 0, next);
							next += lcl;
						}
						entryBreak = next;
						//CMN.Log("residue!!!", new String(bos.getBytes(), 0, bos.size(), _charset));
						addEntry(bos.getBytes(), 0, bos.size(), i*mBlockSize+entryBreak-contentBreakText.length, lastEntry);
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
						addEntry(tmpBlock.data, entryBreak, next-entryBreak, i*mBlockSize+next+lineBreakText.length, lastEntry);
						entryBreak=next+lcl;
					} else{ /* but without any result */
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
			index_array = new ArrayListTree<>(index_tree.flatten());
			_num_entries = index_array.size();
			CMN.pt(" 树大小："+_num_entries+" 扫描文件耗时：");
			//fout.flush(); fout.close();
			if(lastEntry.size()>0)
				for (Entry eI:lastEntry)
					eI.contentEnd = f.length();
			CMN.rt();
			dumpEntries();
			CMN.pt("写入词条耗时：");
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

	private void addEntry(byte[] data, int offset, int size, int contentStart, ArrayList<Entry> lastSibling) {
		String text = new String(data, offset, size, _charset).trim();
		if(text.indexOf("\\(")>=-1){
			text = text.replace("\\(", "(").replace("\\)", ")");
		}
		if(index_tree.getRoot()==null && bIsUTF16)
			text = text.replace(UTF16BOM, "");
		String[] arr = text.split("\n");
		Entry newEntry = new Entry();
		newEntry.text = arr[0];
		//String[] arr1 = text.split(VerbatimSearchTask.RegExp_VerbatimDelimiter);
		newEntry.contentStart=contentStart;
		newEntry.entryStart = contentStart-size;
		RBTNode<Entry> heyNode = index_tree.insert(newEntry);
		if(lastSibling!=null) {
			for (Entry eI:lastSibling)
				eI.contentEnd = newEntry.entryStart - lineBreakText.length;
		}
		lastSibling.clear();
		lastSibling.add(newEntry);
		if(heyNode.getKey()!=newEntry){
//			if(heyNode.getKey().text.compareToIgnoreCase("cheap")==0)
//				CMN.Log("处理重复词条11");
			appendix.insert(heyNode.getKey().text, newEntry);
		}
		if (arr.length > 1) {
			for (int i = 1; i < arr.length; i++) {
				String tI = arr[i];
				//String[] arrI = tI.split(VerbatimSearchTask.RegExp_VerbatimDelimiter);
				if (tI.contains("{(") && tI.endsWith(")}")) { //advanced append
					int idx = tI.indexOf("{(");
					if (idx > 0) {
						if (tI.charAt(idx - 1) == ' ')
							--idx;
						appendix.insert(tI.substring(0, idx).trim(), newEntry.text);
					}
				} else { //alias
					Entry entry = new Entry();
					lastSibling.add(entry);
					entry.text = tI;
					entry.contentStart=newEntry.contentStart;
					entry.entryStart = newEntry.entryStart;
					index_tree.insert(entry);
				}
			}
		}
	}

	private int findNextEntryBreakIndex(ReusableByteOutputStream lastTmpBlock, TextBlock tmpBlock, int nowIndex) {
		if(nowIndex==0 && (tmpBlock.blockIndex==0&&startWithEntry(tmpBlock, nowIndex+(bIsUTF16?2:0)) || lastTmpBlock!=null && endWithLineBreak(lastTmpBlock) && startWithEntry(tmpBlock, nowIndex))){
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


	private boolean startWithContent(TextBlock tmpBlock, int nowIndex) {
		if(compareByteArrayIsPara(tmpBlock.data, nowIndex, tmpBlock.blockSize, contentBreakText))
			return true;
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
	public String getEntryAt(int position) {
		if(position==-1) return "about:";
		return index_array.itemAt(position).text;
	}

	@Override
	public String getEntryAt(int position, Flag mflag) {
		return getEntryAt(position);
	}

	@Override
	public int lookUp(String keyword, boolean isSrict) {
		ArrayList<Entry> data = index_array.getList();
		if(data.size()==0) return -1;

		int idx = reduce(data, keyword,0,data.size());

		if(idx<0 || !isSrict) return idx;

		String looseMatch = processText(getEntryAt(idx));
		boolean bIsEqual = looseMatch.equals(processText(keyword));
		if(!bIsEqual){
			return -1* (idx+2);
		}
		return idx;
	}

	public int reduce(ArrayList<Entry> data, String val,int start,int end) {//via mdict-js
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
		return null;
	}

	@Override
	public boolean hasMdd() {
		return false;
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
	public void flowerFindAllContents(String key, int selfAtIdx, mdict.AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
	////	super.flowerFindAllContents(key, selfAtIdx, SearchLauncher);
	////}
	////
	////@Override
	////public void flowerFindAllContents(String key, int selfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException {
//		//SU.Log("Find In All Contents Stated");
//		byte[][][] matcher=null;
//		Regex Joniregex = null;
//		if(getUseJoniRegex(1)){
//			if(encoding==null) bakeJoniEncoding();
//			if(encoding!=null) {
//				//if (getRegexAutoAddHead() && !key.startsWith(".*"))
//				//	key = ".*" + key;
//				byte[] pattern = key.getBytes(_charset);
//				Joniregex = new Regex(pattern, 0, pattern.length, getRegexOption(), encoding);
//			}
//		}
//		if(Joniregex==null) matcher =  leafSanLieZhi(SearchLauncher);
//
//		split_recs_thread_number = _num_entries<6?1:(int) (_num_entries/6);//Runtime.getRuntime().availableProcessors()/2*2+10;
//		split_recs_thread_number = split_recs_thread_number>16?6:split_recs_thread_number;
//		final int thread_number = Math.min(Runtime.getRuntime().availableProcessors()/2*2+2, split_recs_thread_number);
//		//SU.Log("fatal_","split_recs_thread_number", split_recs_thread_number);
//		//SU.Log("fatal_","thread_number", thread_number);
//
//		final int step = (int) (_num_entries/split_recs_thread_number);
//		final int yuShu=(int) (_num_entries%split_recs_thread_number);
//
//		ArrayList<Integer>[] _combining_search_tree=SearchLauncher.getCombinedTree(selfAtIdx);
//		boolean hold=false;
//		if(SearchLauncher.combining_search_tree==null){
//			hold=true; _combining_search_tree=combining_search_tree_4;
//		}
//		if(_combining_search_tree==null || _combining_search_tree.length!=split_recs_thread_number){
//			_combining_search_tree = new ArrayList[split_recs_thread_number];
//			if(hold)
//				combining_search_tree_4=_combining_search_tree;
//			else
//				SearchLauncher.setCombinedTree(selfAtIdx, _combining_search_tree);
//		}
//
//		ConcurrentHashMap<Integer, TextBlock> cache_tmp = new ConcurrentHashMap<>(block_cache);
//		SearchLauncher.poolEUSize.set(SearchLauncher.dirtyProgressCounter=0);
//		block_cache.syncAccommodationSize();
//		//ArrayList<Thread> fixedThreadPool = new ArrayList<>(thread_number);
//		ExecutorService fixedThreadPool = OpenThreadPool(thread_number);
//		for(int ti=0; ti<split_recs_thread_number; ti++){//分  thread_number 股线程运行
//			//SU.Log("执行", ti , split_recs_thread_number);
//			if(SearchLauncher.IsInterrupted || searchCancled) break;
//			final int it = ti;
//			if(split_recs_thread_number>thread_number) while (SearchLauncher.poolEUSize.get()>=thread_number) {
//				try {
//					Thread.sleep(2);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//
//			if(combining_search_tree_4[it]==null)
//				combining_search_tree_4[it] = new ArrayList<>();
//
//			if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(1);
//
//			Regex finalJoniregex = Joniregex;
//			byte[][][] finalMatcher = matcher;
//			fixedThreadPool.execute(
//					new Runnable(){@Override public void run()
//					{
//						if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
//						final ReusableByteOutputStream bos = new ReusableByteOutputStream(mBlockSize *2);//!!!避免反复申请内存
//						try
//						{
//							Flag flag = new Flag();
//							long toSkip=it*step* mBlockSize;
//							InputStream data_in = BU.SafeSkipReam(new FileInputStream(f), toSkip);
//							int jiaX=0;
//							if(it==split_recs_thread_number-1) jiaX=yuShu;
//							TextBlock tmpBlock=null;
//							for(int i=it*step; i<it*step+step+jiaX; i++)//_num_entries
//							{
//								if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
//								bos.reset();
//
//								if(i>=_num_entries) return;
//
//								Entry eI = index_array.itemAt(i);
//								int centerBlock = (int) (eI.contentStart/mBlockSize);
//								int length = (int) (eI.contentEnd - eI.contentStart);
//								InputStream fin = BU.SafeSkipReam(new FileInputStream(f), centerBlock*mBlockSize);
//								toSkip=0;
//								TextBlock cachedBlock;
//								while(length>0 && centerBlock<_num_record_blocks) {
//									cachedBlock = cache_tmp.get(centerBlock);
//									if(cachedBlock==null){
//										if(toSkip>0) {
//											BU.SafeSkipReam(fin, toSkip);
//											toSkip=0;
//										}
//										if(tmpBlock==null) tmpBlock = new TextBlock();
//										tmpBlock.blockIndex = centerBlock;
//										tmpBlock.blockSize = fin.read(tmpBlock.data);
//										cachedBlock=tmpBlock;
//										int space = block_cache.accommodation.addAndGet(-1);
//										if(space>0){ //可加入缓存队列
//											tmpBlock = null;
//											cache_tmp.put(centerBlock, cachedBlock);
//										}
//									} else {
//										//CMN.Log("找到DSL缓存区", block_cache.size());
//										toSkip+=mBlockSize;
//									}
//									int start = (int) Math.max(0, eI.contentStart - (cachedBlock.blockIndex * mBlockSize));
//									int len = Math.min(cachedBlock.blockSize - start, length);
//									bos.write(cachedBlock.data, start, len);
//									length-=len;
//									centerBlock++;
//								}
//
//								byte[] record_block_ = bos.getBytes();
//								int recordodKeyLen = bos.size();
//								//内容块读取完毕
//
//								org.joni.Matcher Jonimatcher = null;
//								if(finalJoniregex !=null)
//									Jonimatcher = finalJoniregex.matcher(record_block_);
//								if(SearchLauncher.IsInterrupted  || searchCancled ) break;
//
//								int try_idx=Jonimatcher==null?
//										flowerIndexOf(record_block_,0,recordodKeyLen, finalMatcher,0,0, SearchLauncher, flag)
//										:Jonimatcher.searchInterruptible(0, recordodKeyLen, Option.DEFAULT)
//										;
//								//SU.Log(try_idx, record_block_.length, recordodKeyLen);
//								if(try_idx!=-1) {
//									SearchLauncher.dirtyResultCounter++;
//									combining_search_tree_4[it].add(i);
//								}
//								SearchLauncher.dirtyProgressCounter++;
//							}
//							data_in.close();
//
//						} catch (Exception e) {
//							CMN.Log(e);
//						}
//						SearchLauncher.thread_number_count--;
//						if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(-1);
//					}}
//			);
//		}
//		SearchLauncher.currentThreads=fixedThreadPool;
//		fixedThreadPool.shutdown();
//		try {
//			fixedThreadPool.awaitTermination(5, TimeUnit.MINUTES);
//		} catch (Exception e1) {
//			SU.Log("Find In Full Text Interrupted!!!");
//		} finally {
//			int size = block_cache.size();
//			block_cache.putAll(cache_tmp);
//			//a.root.postDelayed(() -> a.showT(CMN.Log("添加后", block_cache.size(),block_cache.size()-size)), 500);
//		}
	}
	
	@Override
	public String getRichDescription() {
		return _Dictionary_fName+"<br>"+f.getAbsolutePath()+"<br>"+f.lastModified()+"<br>"+_charset+"<br>"+mp4meta.utils.CMN.formatSize(f.length());
	}
	
	@Override
	public String getDictInfo() {
		return getRichDescription();
	}

}