/*  Copyright 2018 KnIfER

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

	Mdict-Java Query Library
*/

package com.knziha.plod.dictionary;


import android.util.SparseArray;

import androidx.appcompat.app.GlobalOptions;

import com.alibaba.fastjson.JSONObject;
import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.F1ag;
import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.GetIndexedString;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.Utils.key_info_struct;
import com.knziha.plod.dictionary.Utils.myCpr;
import com.knziha.plod.dictionary.Utils.record_info_struct;
import com.knziha.rbtree.RBTree_additive;

import org.adrianwalker.multilinestring.Multiline;
import org.anarres.lzo.LzoDecompressor1x;
import org.anarres.lzo.lzo_uintp;
import org.apache.commons.text.StringEscapeUtils;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.BIG5Encoding;
import org.jcodings.specific.Big5HKSCSEncoding;
import org.jcodings.specific.CP949Encoding;
import org.jcodings.specific.EUCJPEncoding;
import org.jcodings.specific.EUCKREncoding;
import org.jcodings.specific.EUCTWEncoding;
import org.jcodings.specific.GB18030Encoding;
import org.jcodings.specific.GB2312Encoding;
import org.jcodings.specific.GBKEncoding;
import org.jcodings.specific.ISO8859_10Encoding;
import org.jcodings.specific.ISO8859_11Encoding;
import org.jcodings.specific.ISO8859_13Encoding;
import org.jcodings.specific.ISO8859_14Encoding;
import org.jcodings.specific.ISO8859_15Encoding;
import org.jcodings.specific.ISO8859_16Encoding;
import org.jcodings.specific.ISO8859_1Encoding;
import org.jcodings.specific.ISO8859_2Encoding;
import org.jcodings.specific.ISO8859_3Encoding;
import org.jcodings.specific.ISO8859_4Encoding;
import org.jcodings.specific.ISO8859_5Encoding;
import org.jcodings.specific.ISO8859_6Encoding;
import org.jcodings.specific.ISO8859_7Encoding;
import org.jcodings.specific.ISO8859_8Encoding;
import org.jcodings.specific.ISO8859_9Encoding;
import org.jcodings.specific.KOI8REncoding;
import org.jcodings.specific.KOI8UEncoding;
import org.jcodings.specific.SJISEncoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.jcodings.specific.UTF16LEEncoding;
import org.jcodings.specific.UTF32BEEncoding;
import org.jcodings.specific.UTF32LEEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.specific.Windows_1250Encoding;
import org.jcodings.specific.Windows_1251Encoding;
import org.jcodings.specific.Windows_1252Encoding;
import org.jcodings.specific.Windows_1253Encoding;
import org.jcodings.specific.Windows_1254Encoding;
import org.jcodings.specific.Windows_1257Encoding;
import org.jcodings.specific.Windows_31JEncoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.exception.SyntaxException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * **Mdict Java Library**<br/><br/>
 * <b>FEATURES</b>:<br/>
 * 1. Basic listing and fast binary query of mdx files.<br/>
 * 2. Dictionary conjunction search.<br/>
 * 3. Fast Multi-threaded search in all contents.<br/>
 * 4. Fast Multi-threaded search in all entries.<br/>
 * 5. Optional regex expression engine( Joni ) or wildcards( .* ) for above two search modes.<br/><br/>
 * Author : KnIfER<br/>
 * <b>Licence</b> : Apache2.0 under this package (com.knziha.plod.dictionary.*); GPL3.0 for everything else including the mdictBuilder. <br/>
 */
@SuppressWarnings("SpellCheckingInspection")
public class mdict extends mdBase{
	private mdict parent;
	byte[] textLineBreak;
	protected Encoding encoding;

	/** Packed mdd files. */
	protected List<mdictRes> mdd;
	/** Unpacked file tree. */
	protected List<File> ftd;

	protected mdict virtualIndex;

	public String _Dictionary_fName;
	public /*final*/ boolean isResourceFile;

	public boolean getIsDedicatedFilter(){
		return false;
	}
	public boolean getIsDedicatedFilter(byte firstFlag){
		return false;
	}
	public void setIsDedicatedFilter(boolean val){
	}
	//public int KeycaseStrategy=0;//0:global 1:Java API 2:classical
	public int getCaseStrategy(){
		return 0;
	}
	public void setCaseStrategy(int val){
	}
	public static boolean bGlobalUseClassicalKeycase=false;

	public String currentDisplaying;

	public volatile boolean searchCancled;

	//构造
	public mdict(String fn) throws IOException {
		super(fn);
		if(_num_record_blocks==-1) return;
		calcFName();
	}

	protected mdict(mdict master, DataInputStream data_in, long _ReadOffset) throws IOException {
		super(master, data_in);
		parent=master;
		ReadOffset=_ReadOffset;
		isResourceFile=false;
	}

	@Override
	protected void init(DataInputStream data_in) throws IOException {
		super.init(data_in);
		textLineBreak=lineBreakText.getBytes(_charset);
		// ![0] load options
		ScanSettings();
		// ![1] load mdds
		loadInResourcesFiles(null);
		calcFuzzySpace();
		if(_header_tag.containsKey("hasSlavery")){
			try {
				long skip = data_in.skipBytes((int) _key_block_size);
				decode_record_block_size(data_in);
				int toTail=(int) (_record_block_size+_record_block_info_size);
				//SU.Log("Slavery.Init ...", skip, _key_block_size, ReadOffset, _record_block_offset, _version, toTail);
				skip+=data_in.skipBytes(toTail);
				if(skip==_key_block_size + toTail && data_in.available()>0){
					virtualIndex = new mdict(this, data_in, ReadOffset+_record_block_offset+(_version>=2?32:16)+toTail);
					SU.Log("Slavery.Init OK");
				}
			} catch (IOException e) {
				SU.Log("Slavery.Init Error");
				SU.Log(e);
			}
		}
		data_in.close();
	}

	protected boolean handleDebugLines(String line) {
		if(line.length()>0){
			File p = f.getParentFile();
			if(line.startsWith("\\")){
				File f = new File(p, line.substring(1));
				if(f.isDirectory())
					ftd.add(f);
				return true;
			}
			if(line.startsWith("`")&&line.length()>1){
				int nxt=line.indexOf("`", 1);
				_stylesheet.put(line.substring(1,nxt), line.substring(nxt+1).trim().split("`",2));
				return true;
			}
		}
		return false;
	}

	public boolean hasVirtualIndex() {
		return virtualIndex!=null;
	}

	public InputStream getResourceByKey(String key) {
		if(isResourceFile){
			int idx = lookUp(key);
			if(idx>=0) {
				try {
					return getResourseAt(idx);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if(ftd !=null && ftd.size()>0){
				String keykey = key.replace("\\",File.separator);
				for(File froot: ftd){
					File ft= new File(froot, keykey);
					//SU.Log(ft.getAbsolutePath(), ft.exists());
					if(ft.exists()) {
						try {
							return new FileInputStream(ft);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			if(mdd!=null && mdd.size()>0){
				for(mdictRes mddTmp:mdd){
					int idx = mddTmp.lookUp(key);
					if(idx>=0) {
						try {
							return mddTmp.getResourseAt(idx);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					//else SU.Log("chrochro inter_ key is not find:",_Dictionary_fName,key, idx);
				}
			}
		}
		return null;
	}

	protected void ScanSettings() {

	}

	public String getCachedEntryAt(int pos) {
		return currentDisplaying;
	}

	@Override
	public long getNumberEntries() {
		if(virtualIndex!=null)
			return virtualIndex._num_entries;
		return _num_entries;
	}

	//for lv
	public String getEntryAt(int position, Flag mflag) {
		if(virtualIndex!=null)
			return virtualIndex.getEntryAt(position, mflag);
		if(position==-1) return "about:";
		if(_key_block_info_list==null) read_key_block_info(null);
		int blockId = accumulation_blockId_tree.xxing(new myCpr<>(position,1)).getKey().value;
		key_info_struct infoI = _key_block_info_list[blockId];
		if(compareByteArray(infoI.headerKeyText, infoI.tailerKeyText)==0)
			mflag.data = new String(infoI.headerKeyText,_charset);
		else
			mflag.data = null;
		//TODO null pointer error
		return new String(prepareItemByKeyInfo(infoI,blockId,null).keys[(int) (position-infoI.num_entries_accumulator)],_charset);
	}

	@Override
	public String getEntryAt(int position) {
		if(virtualIndex!=null)
			return virtualIndex.getEntryAt(position);
		return super.getEntryAt(position);
	}

	public int reduce_index2(byte[] phrase, int start, int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//show("reducearound:"+(start + len - 1)+"@"+len+": "+new String(_key_block_info_list[start + len - 1].tailerKeyText));
			//show(start+"::"+end+"   "+new String(_key_block_info_list[start].tailerKeyText,_charset)+"::"+new String(_key_block_info_list[end==_key_block_info_list.length?end-1:end].tailerKeyText,_charset));
			byte[] zhujio = _key_block_info_list[start + len - 1].tailerKeyText;
			return compareByteArray(phrase, /*isCompact*/false?zhujio:processMyText(new String(zhujio,_charset)).getBytes(_charset))>0
					? reduce_index2(phrase,start+len,end)
					: reduce_index2(phrase,start,start+len);
		} else {
			return start;
		}
	}
	public int reduce_index(String phrase,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//show("reducearound:"+(start + len - 1)+"@"+len+": "+new String(_key_block_info_list[start + len - 1].tailerKeyText));
			//show(start+"::"+end+"   "+new String(_key_block_info_list[start].tailerKeyText,_charset)+"::"+new String(_key_block_info_list[end==_key_block_info_list.length?end-1:end].tailerKeyText,_charset));
			String zhujio = new String(_key_block_info_list[start + len - 1].tailerKeyText,_charset);
			return phrase.compareToIgnoreCase(/*isCompact*/false?zhujio:processMyText(zhujio))>0
					? reduce_index(phrase,start+len,end)
					: reduce_index(phrase,start,start+len);
		} else {
			return start;
		}
	}
	public int lookUp(String keyword) {
		return lookUp(keyword,false);
	}
	String HeaderTextStr, TailerTextStr;

	public int lookUp(String keyword,boolean isSrict)
	{
		if(isResourceFile) {
			if(!keyword.startsWith("\\"))
				keyword="\\"+keyword;
			return super.lookUp(keyword, isSrict);
		}
		if(virtualIndex!=null){
			return virtualIndex.lookUp(keyword, isSrict);
		}
		if(_key_block_info_list==null) read_key_block_info(null);
		String keyOrg=keyword;
		keyword = processMyText(keyword);
		byte[] kAB = keyword.getBytes(_charset);

		int blockId = -1;

		if(_encoding.startsWith("GB")) {
			int boudaryCheck = compareByteArray(_key_block_info_list[(int)_num_key_blocks-1].tailerKeyText,kAB);
			if(boudaryCheck<0)
				return -1;
			if(boudaryCheck==0) blockId = (int)_num_key_blocks-1;
			boudaryCheck = compareByteArray(_key_block_info_list[0].headerKeyText,kAB);
			if(boudaryCheck>0)
				return -1;
			if(boudaryCheck==0) return 0;
		}
		else {
			int boudaryCheck = processMyText(new String(_key_block_info_list[(int)_num_key_blocks-1].tailerKeyText,_charset)).compareTo(keyword);
			if(boudaryCheck<0)
				return -1;
			if(boudaryCheck==0) blockId = (int)_num_key_blocks-1;
			if(HeaderTextStr==null)
				HeaderTextStr=processMyText(new String(_key_block_info_list[0].headerKeyText,_charset));
			boudaryCheck = HeaderTextStr.compareTo(keyword);
			if(boudaryCheck>0) {
				if(HeaderTextStr.startsWith(keyword)) {
					return isSrict?-(0+2):0;
				}else
					return -1;
			}
			if(boudaryCheck==0) return 0;
		}
		if(blockId==-1)
			blockId = _encoding.startsWith("GB")?reduce_index2(keyword.getBytes(_charset),0,_key_block_info_list.length):reduce_index(keyword,0,_key_block_info_list.length);
		if(blockId==-1) return blockId;

		//SU.Log("blockId:",blockId, new String(_key_block_info_list[blockId].headerKeyText,_charset), new String(_key_block_info_list[blockId].tailerKeyText,_charset));
		//while(blockId!=0 &&  compareByteArray(_key_block_info_list[blockId-1].tailerKeyText,kAB)>=0) blockId--;
		//SU.Log("finally blockId is:"+blockId+":"+_key_block_info_list.length);


		key_info_struct infoI = _key_block_info_list[blockId];

		//smart shunt
		if(compareByteArray(infoI.headerKeyText, infoI.tailerKeyText)==0) {
			if(isSrict)
				return -1*(int) ((infoI.num_entries_accumulator+2));
			else
				return (int) infoI.num_entries_accumulator;
		}

		cached_key_block infoI_cache = prepareItemByKeyInfo(infoI,blockId,null);

		int res;
		if(_encoding.startsWith("GB"))
			//res = binary_find_closest2(infoI_cache.keys,keyword);//keyword
			res = reduce_keys2(infoI_cache.keys,kAB,0,infoI_cache.keys.length);
		else
			//res = binary_find_closest(infoI_cache.keys,keyword);//keyword
			res = reduce_keys(infoI_cache.keys,keyword,0,infoI_cache.keys.length);

		if (res==-1){
			System.out.println("search failed!"+keyword);
			return -1;
		}
		//SU.Log(keyword, res, getEntryAt((int) (res+infoI.num_entries_accumulator)));
		////if(isCompact) //compatibility fix
		String other_key = new String(infoI_cache.keys[res],_charset);
		String looseMatch = processMyText(other_key);
		boolean bIsEqual = looseMatch.equals(keyword);

		if(!bIsEqual){
			boolean b1 = keyOrg.endsWith(">"),b2=false;
			Matcher m=null;
			if(!b1) {
				m = numSuffixedReg.matcher(keyOrg);
				b2=m.find();
			}
			if((b1||b2) && other_key.endsWith(">")){
				int idx3 = other_key.lastIndexOf("<", other_key.length() - 2);
				int idx2 = b2?m.start(1):keyOrg.lastIndexOf("<",keyOrg.length()-2);
				//CMN.Log(idx2,idx3,idx2,idx3);
				if(idx2!=-1 && idx2==idx3) {
					int start = parseint(other_key.substring(idx2+1,other_key.length()-1));
					int target;
					if(b2){
						target = IU.parsint(m.group(1));
					}else{
						String itemA=keyOrg.substring(idx2+1,keyOrg.length()-1);
						target = parseint(itemA);
					}
					//CMN.Log(keyOrg,other_key,start,target);
					int PstPosition = (int) (infoI.num_entries_accumulator + res + (target-start));
					String other_other_key = getEntryAt(PstPosition);
					if(other_other_key.length()>idx2 && other_other_key.endsWith(">") && other_other_key.charAt(idx2)=='<')
						if(keyOrg.startsWith(other_other_key.substring(0, idx2))){
							String itemB=other_other_key.substring(idx2+1,other_other_key.length()-1);
							int end = parseint(itemB);
							if(target==end)
								return PstPosition;
						}
				}
			}
		}

		if(isSrict)
			if(!bIsEqual) {
				//SU.Log(res+"::"+Integer.toString(-1*(res+2)));
				return -1*(int) ((infoI.num_entries_accumulator+res+2));
			}
		//String KeyText= infoI_cache.keys[res];
		//for(String ki:infoI.keys) SU.Log(ki);
		//show("match key "+KeyText+" at "+res);
		return (int) (infoI.num_entries_accumulator+res);
	}

	private int parseint(String item) {
		if(IU.shuzi.matcher(item).find())
			return IU.parsint(item);
		else if(IU.hanshuzi.matcher(item).find())
			return IU.recurse1wCalc(item,0,item.length()-1,1);
		return -1;
	}

	private int try_get_tailing_number(String keyOrg) {
		if(keyOrg.endsWith(">")){
			int idx2 = keyOrg.lastIndexOf("<",keyOrg.length()-2);
			if(idx2!=-1){
				String item = keyOrg.substring(idx2+1,keyOrg.length()-1);
				if(IU.hanshuzi.matcher(item).find())
					return IU.recurse1wCalc(item,0,item.length()-1,1);
				else if(IU.shuzi.matcher(item).find())
					return IU.parsint(item);
			}
		}else{
			Matcher m = numSuffixedReg.matcher(keyOrg);
			if(m.find()){
				return IU.parsint(m.group());
			}
		}
		return -1;
	}

	public int reduce_keys(byte[][] keys,String val,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//String zhujue = processMyText(new String(keys[start + len - 1],_charset));
          /*if(!isCompact) {//fixing python writemdict compatibility
			  if(infoI_cache.hearderTextStr==null) {
				  infoI_cache.hearderTextStr=new String(infoI_cache.hearderText,_charset);
				  infoI_cache.tailerKeyTextStr=new String(infoI_cache.tailerKeyText,_charset);
			  }
			  if(infoI_cache.tailerKeyTextStr.compareTo(zhujue)>0 || infoI_cache.hearderTextStr.compareTo(zhujue)<0) {
				  zhujue = replaceReg2.matcher(zhujue).replaceAll(emptyStr);
			  }
		  }*/

			//show("->"+new String(keys[start + len - 1],_charset)+" ="+val.compareTo(processMyText(new String(keys[start + len - 1],_charset))));
			//show(start+"::"+end+"   "+new String(keys[start],_charset)+"::"+new String(keys[end==keys.length?end-1:end],_charset));


			return val.compareTo(processMyText(new String(keys[start + len - 1],_charset)))>0
					? reduce_keys(keys,val,start+len,end)
					: reduce_keys(keys,val,start,start+len);
		} else {
			return start;
		}
	}
	public int reduce_keys2(byte[][] keys,byte[] val,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//String zhujue = processMyText(new String(keys[start + len - 1],_charset));
          /*if(!isCompact) {//fixing python writemdict compatibility
			  if(infoI_cache.hearderTextStr==null) {
				  infoI_cache.hearderTextStr=new String(infoI_cache.hearderText,_charset);
				  infoI_cache.tailerKeyTextStr=new String(infoI_cache.tailerKeyText,_charset);
			  }
			  if(infoI_cache.tailerKeyTextStr.compareTo(zhujue)>0 || infoI_cache.hearderTextStr.compareTo(zhujue)<0) {
				  zhujue = replaceReg2.matcher(zhujue).replaceAll(emptyStr);
			  }
		  }*/
			//SU.Log(start+"::"+end+"   "+new String(keys[start],_charset)+"::"+new String(keys[end],_charset));
			return compareByteArray(val, processMyText(new String(keys[start + len - 1],_charset)).getBytes(_charset))>0
					? reduce_keys2(keys,val,start+len,end)
					: reduce_keys2(keys,val,start,start+len);
		} else {
			return start;
		}
	}

	public Object ReRoute(String keyraw) throws IOException {
		if(virtualIndex!=null)
			return virtualIndex.ReRoute(keyraw);
		if(isResourceFile)
			return -1;
		int c=0;
		int i = lookUp(keyraw, true);
		if(i<0){
			return -1;
		}
		return getRecordAt(i);
	}

	public String getRecordsAt(int... positions) throws IOException {
		if(isResourceFile)
			return constructLogicalPage(positions);
		StringBuilder sb = new StringBuilder();
		int c=0;
		for(int i:positions) {
			sb.append(getRecordAt(i));//.trim()
			if(c!=positions.length-1)
				sb.append("<HR>");
			c++;
		}
		sb.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
		if(mdd!=null && mdd.size()>0) sb.append("<p class='MddExist'/>");
		sb.append("</div>");
		return processStyleSheet(sb.toString(), positions[0]);
	}

	/** @param positions virutal indexes*/
	public String getVirtualRecordsAt(int... positions) throws IOException {
		if(virtualIndex==null)
			return getRecordsAt(positions);
		StringBuilder sb = new StringBuilder();
		int c=0, lastAI=-1;
		for(int i:positions) {
			String vi = virtualIndex.getRecordAt(i);
			JSONObject vc = JSONObject.parseObject(vi);
			int AI=vc.getInteger("I");
			if(lastAI==AI){
				//TODO overlaping case
			}
			else{
				String JS = vc.getString("JS");
				String record = getRecordAt(AI);
				int headId= record.indexOf("<head>");
				if(headId<0) {
					headId=-6;
					sb.append("<head>");
				}
				sb.append(record, 0, headId+6);
				sb.append("<script>");
				sb.append(JS==null?"":JS);
				sb.append("</script>");
				if(headId<0) sb.append("<head>");
				sb.append(record, headId+6, record.length());
			}
			lastAI=AI;

			if(c!=positions.length-1)
				sb.append("<HR>");
			c++;
		}
		sb.append("<div class=\"_PDict\" style='display:none;'><p class='bd_body'/>");
		if(mdd!=null && mdd.size()>0) sb.append("<p class='MddExist'/>");
		sb.append("</div>");
		return processStyleSheet(sb.toString(), positions[0]);
	}

	/**
	<style>
	audio {
		position:absolute;
		top:32%;
		width:100%;
	}
	h2 {
	 	position:absolute;
	 	top:1%;
	 	width:100%;
	 	text-align: center;
	}
	</style>
	 */
	@Multiline
	String logicalPageHeader="SUBPAGE";

	/** Construct Logical Page For mdd resource file. */
	private String constructLogicalPage(int...positions) {
		StringBuilder LoPageBuilder = new StringBuilder();
		LoPageBuilder.append(logicalPageHeader);
		for(int i:positions) {
			String key = getEntryAt(i);
			if(key.startsWith("/")||key.startsWith("\\"))
				key=key.substring(1);
			key=StringEscapeUtils.escapeHtml3(key);
			if(htmlReg.matcher(key).find()){
				LoPageBuilder.append(decodeRecordData(positions[0], StandardCharsets.UTF_8));
			}else{
				if(imageReg.matcher(key).find()){
					LoPageBuilder.append("<img style='width:100%; height:auto;' src=\"").append(key).append("\"></img>");
				}
				else if(soundReg.matcher(key).find()){
					LoPageBuilder.append("<h2>").append(key).append("</h2>");
					LoPageBuilder.append("<audio controls='controls' autoplay='autoplay' src=\"").append(key).append("\"></audio>");
					LoPageBuilder.append("<h2 style='top:56%'>").append(key).append("</h2>");
				}
				else if(videoReg.matcher(key).find()){
					LoPageBuilder.append("<video width='320' height='240' controls=\"controls\" src=\"").append(key).append("\"></video>");
				}
			}
		}
		LoPageBuilder.append("<div class='bd_body'/>");
		return LoPageBuilder.toString();
	}

	public static int offsetByTailing(String token) {
		//calculating relative offset represented by number of tailing '\n'.
		//entrys: abc abc acc TO: abc abc\n acc
		if(token.endsWith("\n")) {
			int first=token.length()-1;
			while(first-1>0 && token.charAt(first-1)=='\n') {
				first--;
			}
			return token.length()-first;
		}
		return 0;
	}

	public String getRecordAt(int position) throws IOException {
		if(ftd!=null && ftd.size()>0 && ReadOffset==0){
			File ft;
			for(File f:ftd){
				ft=new File(f, ""+position);
				//SU.Log(ft.getAbsolutePath(), ft.exists());
				if(ft.exists())
					return BU.fileToString(ft);
			}
		}
		if(position<0||position>=_num_entries)
			return "404 index out of bound";
		RecordLogicLayer va1=new RecordLogicLayer();
		super.getRecordData(position, va1);
		byte[] data = va1.data;
		int record_start=va1.ral;
		int record_end=va1.val;

		if(textTailed(data, record_end-textLineBreak.length, textLineBreak)) record_end-=textLineBreak.length;

		String tmp = new String(data, record_start, record_end-record_start,_charset);

		if(tmp.startsWith(linkRenderStr)) {
			//SU.Log("rerouting",tmp);
			//SU.Log(tmp.replace("\n", "1"));
			String key = tmp.substring(linkRenderStr.length());
			//todo clean up
			int offset = offsetByTailing(key);
			key = key.trim();
			//Log.e("rerouting offset",""+offset);
			int idx = lookUp(key);
			if(idx!=-1) {
				String looseKey = processMyText(key);
				int tmpIdx = lookUp(key,false);
				if(tmpIdx!=-1) {
					String looseMatch = getEntryAt(tmpIdx);
					while(processMyText(looseMatch).equals(looseKey)) {
						if(looseMatch.equals(key)) {
							idx=tmpIdx;
							break;
						}
						if(tmpIdx>=getNumberEntries()-1)
							break;
						looseMatch = getEntryAt(++tmpIdx);
					}
				}

				if(offset>0) {
					if(key.equals(getEntryAt(idx+offset)))
						idx+=offset;
				}
				tmp=getRecordAt(idx);
			}
		}
		return tmp;
	}

	@Override
	public String decodeRecordData(int position, Charset charset) {
		if(ftd !=null && ftd.size()>0){
			for(File froot: ftd){
				File ft= new File(froot, ""+position);
				if(ft.exists()) {
					try {
						return new String(BU.fileToByteArr(ft), charset);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return super.decodeRecordData(position, charset);
	}

	public static boolean textTailed(byte[] data, int off, byte[] textLineBreak) {
		if(off+2<data.length){
			return data[off]==textLineBreak[0]&&data[off+1]==textLineBreak[1]&&data[off+2]==textLineBreak[2];
		}
		return false;
	}

	long[] keyBlocksHeaderTextKeyID;
	public void fetch_keyBlocksHeaderTextKeyID(){
		int blockId = 0;
		long[] _keyBlocksHeaderTextKeyID = new long[(int)_num_key_blocks];
		byte[] key_block = new byte[(int) maxDecomKeyBlockSize];
		byte[]  _key_block_compressed = new byte[(int) maxComKeyBlockSize];
		byte[] currentKeyBlock;
		for(key_info_struct infoI:_key_block_info_list){
			currentKeyBlock=key_block;
			try {
				long start = infoI.key_block_compressed_size_accumulator;
				long compressedSize;
				if(blockId==_key_block_info_list.length-1)
					compressedSize = _key_block_size - _key_block_info_list[_key_block_info_list.length-1].key_block_compressed_size_accumulator;
				else
					compressedSize = _key_block_info_list[blockId+1].key_block_compressed_size_accumulator-infoI.key_block_compressed_size_accumulator;

				DataInputStream data_in = getStreamAt(_key_block_offset+start);

				data_in.read(_key_block_compressed, 0,(int) compressedSize);
				data_in.close();

				int BlockOff=0;
				int BlockLen=(int) infoI.key_block_decompressed_size;
				//解压开始
				switch (_key_block_compressed[0]|_key_block_compressed[1]<<8|_key_block_compressed[2]<<16|_key_block_compressed[3]<<32){
					case 0://no compression
						BlockOff=8;
						currentKeyBlock=_key_block_compressed;
						//System.arraycopy(_key_block_compressed, 8, key_block, 0,(int) (compressedSize-8));
					break;
					case 1:
						new LzoDecompressor1x().decompress(_key_block_compressed, 8, (int)(compressedSize-8), currentKeyBlock, 0,new lzo_uintp());
					break;
					case 2:
						//key_block = zlib_decompress(_key_block_compressed,(int) (+8),(int)(compressedSize-8));
						Inflater inf = new Inflater();
						inf.setInput(_key_block_compressed, 8 ,(int)(compressedSize-8));
						try {
							int ret = inf.inflate(currentKeyBlock,0,(int)(infoI.key_block_decompressed_size));
						} catch (DataFormatException e) {e.printStackTrace();}
					break;
				}
				//!!spliting curr Key block

				_keyBlocksHeaderTextKeyID[blockId] = _version<2 ?BU.toInt(currentKeyBlock, BlockOff)
						:BU.toLong(currentKeyBlock, BlockOff);

				blockId++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		keyBlocksHeaderTextKeyID=_keyBlocksHeaderTextKeyID;
	}


	protected int split_recs_thread_number;
	public void flowerFindAllContents(String key, int selfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher) throws IOException{
		//SU.Log("Find In All Contents Started");
		if(isResourceFile) return;
		byte[][][] matcher=null;
		Regex Joniregex = null;
		if(getUseJoniRegex(1)){
			if(encoding==null) bakeJoniEncoding();
			if(encoding!=null) {
				//if (getRegexAutoAddHead() && !key.startsWith(".*"))
				//	key = ".*" + key;
				byte[] pattern = key.getBytes(_charset);
				Joniregex = new Regex(pattern, 0, pattern.length, getRegexOption(), encoding);
			}
		}
		if(Joniregex==null) matcher =  leafSanLieZhi(SearchLauncher);

		if(_key_block_info_list==null) read_key_block_info(null);

		if(_record_info_struct_list==null) decode_record_block_header();

		if(keyBlocksHeaderTextKeyID==null) fetch_keyBlocksHeaderTextKeyID();

		split_recs_thread_number = _num_record_blocks<6?1:(int) (_num_record_blocks/6);//Runtime.getRuntime().availableProcessors()/2*2+10;
		split_recs_thread_number = split_keys_thread_number>16?6:split_keys_thread_number;
		//split_recs_thread_number = 1;
		final int thread_number = Math.min(Runtime.getRuntime().availableProcessors()/2*2+2, split_keys_thread_number);
		//SU.Log("fatal_","split_recs_thread_number"+split_recs_thread_number);
		//SU.Log("fatal_","thread_number"+thread_number);

		final int step = (int) (_num_record_blocks/split_recs_thread_number);
		final int yuShu=(int) (_num_record_blocks%split_recs_thread_number);


		ArrayList<Integer>[] _combining_search_tree=SearchLauncher.getCombinedTree(selfAtIdx);
		boolean hold=false;
		if(SearchLauncher.combining_search_tree==null){
			hold=true; _combining_search_tree=combining_search_tree_4;
		}
		if(_combining_search_tree==null || _combining_search_tree.length!=split_keys_thread_number){
			_combining_search_tree = new ArrayList[split_keys_thread_number];
			if(hold)
				combining_search_tree_4=_combining_search_tree;
			else
				SearchLauncher.setCombinedTree(selfAtIdx, _combining_search_tree);
		}


		SearchLauncher.poolEUSize.set(SearchLauncher.dirtyProgressCounter=0);

		//ArrayList<Thread> fixedThreadPool = new ArrayList<>(thread_number);
		ExecutorService fixedThreadPool = OpenThreadPool(thread_number);
		for(int ti=0; ti<split_recs_thread_number; ti++){//分  thread_number 股线程运行
			if(SearchLauncher.IsInterrupted || searchCancled) break;
			final int it = ti;
			//if(false)
			if(split_recs_thread_number>thread_number) while (SearchLauncher.poolEUSize.get()>=thread_number) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if(combining_search_tree_4[it]==null)
				combining_search_tree_4[it] = new ArrayList<>();

			if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(1);

			Regex finalJoniregex = Joniregex;
			byte[][][] finalMatcher = matcher;
			//Thread t;
			//fixedThreadPool.add(t=new Thread(
			fixedThreadPool.execute(
					//(
				new Runnable(){@Override public void run()
				{
					if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
					final byte[] record_block_compressed = new byte[(int) maxComRecSize];//!!!避免反复申请内存
					final byte[] record_block_ = new byte[(int) maxDecompressedSize];//!!!避免反复申请内存
					F1ag flag = new F1ag();
					InputStream data_in = null;
					try
					{
						data_in = mOpenInputStream();
						long seekTarget=_record_info_struct_list[it*step].compressed_size_accumulator+_record_block_offset+_number_width*4+_num_record_blocks*2*_number_width;
						long seek = data_in.skip(seekTarget);
						//if(seek!=seekTarget)
						//	throw new RuntimeException("seek!=seekTarget !!!");
						int jiaX=0;
						if(it==split_recs_thread_number-1) jiaX=yuShu;
						for(int i=it*step; i<it*step+step+jiaX; i++)//_num_record_blocks
						{
							if(SearchLauncher.IsInterrupted || searchCancled) { SearchLauncher.poolEUSize.set(0); return; }
							record_info_struct RinfoI = _record_info_struct_list[i];

							int compressed_size = (int) RinfoI.compressed_size;
							int decompressed_size = (int) RinfoI.decompressed_size;
							data_in.read(record_block_compressed,0, compressed_size);

							//解压开始
							switch (record_block_compressed[0]|record_block_compressed[1]<<8|record_block_compressed[2]<<16|record_block_compressed[3]<<32){
								case 0:
									System.arraycopy(record_block_compressed, 8, record_block_, 0, compressed_size-8);
								break;
								case 1:
									new LzoDecompressor1x().decompress(record_block_compressed, 8, (compressed_size-8), record_block_, 0, new lzo_uintp());
								break;
								case 2:
									Inflater inf = new Inflater();
									inf.setInput(record_block_compressed,8,compressed_size-8);
									int ret = inf.inflate(record_block_,0,decompressed_size);
								break;
							}

							//内容块解压完毕
							long off = RinfoI.decompressed_size_accumulator;
							int key_block_id = binary_find_closest(keyBlocksHeaderTextKeyID,off);

							org.joni.Matcher Jonimatcher = null;
							if(finalJoniregex !=null)
								Jonimatcher = finalJoniregex.matcher(record_block_);
							long[] ko; int recordodKeyLen, try_idx;
							OUT:
							while(true) {
								if(SearchLauncher.IsInterrupted  || searchCancled || key_block_id>=_key_block_info_list.length) break;
								ko = prepareItemByKeyInfo(null,key_block_id,null).key_offsets;
								//if(infoI_cacheI.blockID!=key_block_id)
								//	throw new RuntimeException("bad !!!"+infoI_cacheI.blockID+" != "+key_block_id);
								for(int relative_pos=binary_find_closest(ko,off);relative_pos<ko.length;relative_pos++) {
									if(relative_pos<ko.length-1){//不是最后一个entry
										recordodKeyLen=(int) (ko[relative_pos+1]-ko[relative_pos]);
									}
									else if(key_block_id<keyBlocksHeaderTextKeyID.length-1){//不是最后一块key block
										recordodKeyLen=(int) (keyBlocksHeaderTextKeyID[key_block_id+1]-ko[relative_pos]);
									}else {
										recordodKeyLen = (int) (decompressed_size-(ko[ko.length-1]-RinfoI.decompressed_size_accumulator));
									}

									if(ko[relative_pos]-RinfoI.decompressed_size_accumulator+recordodKeyLen>RinfoI.decompressed_size) {
										//show("break OUT");
										break OUT;
									}

									/*
									File dump = new File("D:\\record_dump."+i+".bin");
									if(!dump.exists()) //块调试器
										BU.printFile(record_block_, 0, decompressed_size, dump);*/

									try_idx=(int) (ko[relative_pos]-RinfoI.decompressed_size_accumulator);

									GlobalOptions.debug=relative_pos+_key_block_info_list[key_block_id].num_entries_accumulator==47952;
//									if(GlobalOptions.debug){
//										SU.Log("full res ::str", new String(record_block_, try_idx, recordodKeyLen, _charset));
//										SU.Log("full res ::relative_pos", relative_pos, "kos_len", ko.length-1, "key_block_id", key_block_id, "keyBlocksHeaderTextKeyID_len", keyBlocksHeaderTextKeyID.length-1);
//										SU.Log("full res ::start-len", try_idx, recordodKeyLen, record_block_.length);
//									} else continue;

									try_idx=Jonimatcher==null?
											flowerIndexOf(record_block_,try_idx,recordodKeyLen, finalMatcher,0,0, SearchLauncher, flag)
											:Jonimatcher.searchInterruptible(try_idx, try_idx+recordodKeyLen, Option.DEFAULT)
											;

									if(SearchLauncher.IsInterrupted || searchCancled) break;

									//if(GlobalOptions.debug) SU.Log("full res ::", try_idx, key, (int) (ko[relative_pos]-RinfoI.decompressed_size_accumulator), recordodKeyLen, record_block_.length);

									if(try_idx!=-1) {
										//SU.Log("full res ::", try_idx, finalKey, new String(record_block_, (int) (ko[relative_pos]-RinfoI.decompressed_size_accumulator)+try_idx-100, 200, _charset));
										int pos = (int) (relative_pos+_key_block_info_list[key_block_id].num_entries_accumulator);
										SearchLauncher.dirtyResultCounter++;
										combining_search_tree_4[it].add(pos);
									}
									SearchLauncher.dirtyProgressCounter++;
								}
								key_block_id++;
							}
						}
						data_in.close();

					} catch (Exception e) {
						try {
							if(data_in!=null) data_in.close();
						} catch (IOException ignored) {  }
						//BU.printBytes(record_block_compressed,0,4);
						//CMN.Log(record_block_compressed[0]|record_block_compressed[1]<<8|record_block_compressed[2]<<16|record_block_compressed[3]<<32);
						SU.Log(e);
					}
					SearchLauncher.thread_number_count--;
					if(split_recs_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(-1);
				}}
					//)
				//)
				);
			//t.start();
		}
		SearchLauncher.currentThreads=fixedThreadPool;
		fixedThreadPool.shutdown();
		try {
			fixedThreadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (Exception e1) {
			SU.Log("Find In Full Text Interrupted!!!");
			//e1.printStackTrace();
		}
//		for(Thread t:fixedThreadPool){
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				SU.Log("Find In Full Text Interrupted!!!");
//				e.printStackTrace();
//			}
//		}
	}

	protected void bakeJoniEncoding() {
		switch (_charset.name()){
			case "US-ASCII":
				encoding=ASCIIEncoding.INSTANCE;
			break;
			case "Big5":
				encoding=BIG5Encoding.INSTANCE;
			break;
			case "Big5-HKSCS":
				encoding=Big5HKSCSEncoding.INSTANCE;
			break;
			case "x-IBM949":
				encoding=CP949Encoding.INSTANCE;
			break;
			case "EUC-JP":
				encoding=EUCJPEncoding.INSTANCE;
			break;
			case "EUC-KR":
				encoding=EUCKREncoding.INSTANCE;
			break;
			case "x-EUC-TW":
				encoding=EUCTWEncoding.INSTANCE;
			break;
			case "GB2312":
				encoding=GB2312Encoding.INSTANCE;
			break;
			case "GB18030":
				encoding=GB18030Encoding.INSTANCE;
			break;
			case "GBK":
				encoding=GBKEncoding.INSTANCE;
			break;
			case "ISO-8859-1":
				encoding=ISO8859_1Encoding.INSTANCE;
			break;
			case "ISO-8859-2":
				encoding=ISO8859_2Encoding.INSTANCE;
			break;
			case "ISO-8859-3":
				encoding=ISO8859_3Encoding.INSTANCE;
			break;
			case "ISO-8859-4":
				encoding=ISO8859_4Encoding.INSTANCE;
			break;
			case "ISO-8859-5":
				encoding=ISO8859_5Encoding.INSTANCE;
			break;
			case "ISO-8859-6":
				encoding=ISO8859_6Encoding.INSTANCE;
			break;
			case "ISO-8859-7":
				encoding=ISO8859_7Encoding.INSTANCE;
			break;
			case "ISO-8859-8":
				encoding=ISO8859_8Encoding.INSTANCE;
			break;
			case "ISO-8859-9":
				encoding=ISO8859_9Encoding.INSTANCE;
			break;
			case "ISO-8859-10":
				encoding=ISO8859_10Encoding.INSTANCE;
			break;
			case "ISO-8859-11":
				encoding=ISO8859_11Encoding.INSTANCE;
			break;
			case "ISO-8859-13":
				encoding=ISO8859_13Encoding.INSTANCE;
			break;
			case "ISO-8859-14":
				encoding=ISO8859_14Encoding.INSTANCE;
			break;
			case "ISO-8859-15":
				encoding=ISO8859_15Encoding.INSTANCE;
			break;
			case "ISO-8859-16":
				encoding=ISO8859_16Encoding.INSTANCE;
			break;
			case "KOI8-R":
				encoding=KOI8REncoding.INSTANCE;
			break;
			case "KOI8-U":
				encoding=KOI8UEncoding.INSTANCE;
			break;
			case "Shift_JIS":
				encoding=SJISEncoding.INSTANCE;
			break;
			case "UTF-8":
				encoding=UTF8Encoding.INSTANCE;
			break;
			case "UTF-16BE":
				encoding=UTF16BEEncoding.INSTANCE;
			break;
			case "UTF-16LE":
				encoding=UTF16LEEncoding.INSTANCE;
			break;
			case "UTF-32BE":
				encoding=UTF32BEEncoding.INSTANCE;
			break;
			case "UTF-32LE":
				encoding=UTF32LEEncoding.INSTANCE;
			break;
			case "Windows-31j":
				encoding=Windows_31JEncoding.INSTANCE;
			break;
			case "Windows-1250":
				encoding=Windows_1250Encoding.INSTANCE;
			break;
			case "Windows-1251":
				encoding=Windows_1251Encoding.INSTANCE;
			break;
			case "Windows-1252":
				encoding=Windows_1252Encoding.INSTANCE;
			break;
			case "Windows-1253":
				encoding=Windows_1253Encoding.INSTANCE;
			break;
			case "Windows-1254":
				encoding=Windows_1254Encoding.INSTANCE;
			break;
			case "Windows-1257":
				encoding=Windows_1257Encoding.INSTANCE;
			break;
		}
	}

	/** derived */
	public static int kalyxIndexOf(byte[] source, int sourceOffset, int sourceCount, byte[][] targets, int fromIndex, F1ag seelHolder) {
		int targetCounts = targets.length;
		byte[] target;
		int targetCount;
		int lookat=0;
		byte sI;
		int max1 = sourceOffset + sourceCount -1;
		int i = sourceOffset + fromIndex;
		while(i <= max1) {
			for (; i <= max1; i++) {//亦步亦趋求首项
				/* Look for first character. */
				sI = source[i];
				for (lookat = 0; lookat < targetCounts && sI != targets[lookat][0]; lookat++)
					;
				if(lookat < targetCounts) break;
			}
			if(lookat>=targetCounts)/* 开始即结束 */ return -1;
			for (; lookat < targetCounts; lookat++){
				if (source[i] == targets[lookat][0]) {
					target = targets[lookat];
					targetCount = target.length;
					/* Found first character, now look at the rest of v2 */
					int j = i + 1;
					int end = j + targetCount - 1;
					if(end<=max1 - targetCount) {
						for (int k = 1; j < end && source[j] == target[k]; j++, k++)
							;
						if (j == end) {
							/* Found whole string. */
							seelHolder.val = lookat;
							return i - sourceOffset;//试之得
						}
					}
					//试之不得
				}
			}
			i++;
		}
		return -1;
	}

	/*
	 * https://stackoverflow.com/questions/21341027/find-indexof-a-byte-array-within-another-byte-array
	 * Gustavo Mendoza's Answer*/
	public static int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset, int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		byte first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

	public int split_keys_thread_number;
	//public ArrayList<myCpr<String,Integer>>[] combining_search_tree;
	public ArrayList<Integer>[] combining_search_tree2;
	public ArrayList<Integer>[] combining_search_tree_4;

	public void executeAdvancedSearch(String key, int i, AbsAdvancedSearchLogicLayer layer) throws IOException {
		if(layer.type==-1||layer.type==1){
			flowerFindAllKeys(key, i, layer);
		}
		else{
			flowerFindAllContents(key, i, layer);
		}
	}

	public String getVirtualRecordAt(int vi) throws IOException {
		return virtualIndex.getRecordAt(vi);
	}

	public String getName() {
		return _Dictionary_fName;
	}

	public List<mdictRes> getMdd() {
		return mdd;
	}

	public static abstract class AbsAdvancedSearchLogicLayer{
		public int type;
		public int Idx;
		public volatile boolean IsInterrupted;
		public volatile String ErrorMessage=null;
		public volatile int thread_number_count = 1;
		public GetIndexedString jnFanMap;
		public GetIndexedString fanJnMap;
		/** .is 免死金牌  that exempt you from death for just one time */
		HashSet<Integer> miansi = new HashSet<>();
		/** *is 越级天才, i.e., super super genius leap */
		HashSet<Integer> yueji = new HashSet<>();

		public volatile int dirtyResultCounter;
		public volatile int dirtyProgressCounter;
		public long st;
		public String key;

		ArrayList<String>[] mParallelKeys;

		/** Disperse an search term into a 2D byte array. */
		public void flowerSanLieZhi(String str) {
			miansi.clear();
			yueji.clear();
			int len=str.length();
			mParallelKeys = new ArrayList[len];
			for(int i=0;i<len;i++){
				char ch = str.charAt(i);
				if(ch=='.')
					miansi.add(i);
				else if(ch=='*')
					yueji.add(i);
				else{
					ArrayList<String> lexipart = new ArrayList<>();
					String mLexicalPart = str.substring(i, i+1);
					lexipart.add(mLexicalPart);
					String val=null;
					if(getEnableFanjnConversion()){ //繁简一
						if((val=jnFanMap.get(ch))!=null){
							for (int j = 1; j < val.length(); j++) {
								lexipart.add(val.substring(j, j+1));
							}
						} else if((val=fanJnMap.get(ch))!=null){
							lexipart.add(val.substring(0, 1));
						}
					}
					if(val==null){ //忽略大小写
						String UpperKey = mLexicalPart.toUpperCase();
						if(!UpperKey.equals(mLexicalPart)){
							lexipart.add(UpperKey);
						}
					}
					mParallelKeys[i] = lexipart;
				}
			}
		}

		public AtomicInteger poolEUSize = new AtomicInteger(0);

		public Object currentThreads;

		public ArrayList<ArrayList<Integer>[]> combining_search_tree;

		public abstract ArrayList<Integer>[] getCombinedTree(int DX);

		public abstract void setCombinedTree(int DX, ArrayList<Integer>[] val);

		public abstract ArrayList<Integer>[] getInternalTree(mdict mdtmp);

		public abstract boolean getEnableFanjnConversion();

		public abstract Pattern getBakedPattern();

		public abstract void bakePattern(String plainPattern, String currentSearchText);

		public abstract String getBakedPatternStr(boolean useInPageRegex);
	}

	public int thread_number,step,yuShu;
	public void calcFuzzySpace(){
		//final String fkeyword = keyword.toLowerCase().replaceAll(replaceReg,emptyStr);
		//int entryIdx = 0;
		//show("availableProcessors: "+Runtime.getRuntime().availableProcessors());
		//show("keyBLockN: "+_key_block_info_list.length);
		split_keys_thread_number = _num_key_blocks<6?1:(int) (_num_key_blocks/6);//Runtime.getRuntime().availableProcessors()/2*2+10;
		split_keys_thread_number = split_keys_thread_number>16?6:split_keys_thread_number;
		thread_number = Math.min(Runtime.getRuntime().availableProcessors()/2*2+2, split_keys_thread_number);


		step = (int) (_num_key_blocks/split_keys_thread_number);
		yuShu=(int) (_num_key_blocks%split_keys_thread_number);

	}

	protected boolean getUseJoniRegex(int mode){
		if(parent!=null)
			return parent.getUseJoniRegex(mode);
		return true;
	}

	protected boolean getRegexAutoAddHead(){
		//if(parent!=null)
		//	return parent.getRegexAutoAddHead();
		return false;
	}

	protected int getRegexOption(){
		if(parent!=null)
			return parent.getRegexOption();
		return Option.IGNORECASE;
	}

	//XXX2
	public void flowerFindAllKeys(String key, int SelfAtIdx, AbsAdvancedSearchLogicLayer SearchLauncher)
	{
		if(virtualIndex!=null){
			virtualIndex.flowerFindAllKeys(key, SelfAtIdx, SearchLauncher);
			return;
		}
		Pattern keyPattern=null;//用于 复核 ，并不直接参与搜索
		byte[][][] matcher=null;
		Regex Joniregex = null;
		boolean regexIntent=getUseJoniRegex(-1);
		if(regexIntent){
			if(encoding==null) bakeJoniEncoding();
			if(encoding!=null) {
				//.if (getRegexAutoAddHead() && !key.startsWith(".*"))
				//.	key = ".*" + key;
				byte[] pattern = key.getBytes(_charset);
				Joniregex = new Regex(pattern, 0, pattern.length, getRegexOption()|Option.SINGLELINE, encoding);
			}
		}

		if(Joniregex==null){
			String keyword = key.toLowerCase();
			try {
				keyPattern=Pattern.compile(regexIntent?keyword:keyword.replace("*", ".+?"),Pattern.CASE_INSENSITIVE);
			}catch(Exception ignored) {}

			matcher =  leafSanLieZhi(SearchLauncher);
		}

		if(_key_block_info_list==null) read_key_block_info(null);

		//final String fkeyword = keyword.toLowerCase().replaceAll(replaceReg,emptyStr);
		//int entryIdx = 0;
		//SU.Log("availableProcessors: ", Runtime.getRuntime().availableProcessors());
		//show("keyBLockN: "+_key_block_info_list.length);
		split_keys_thread_number = _num_key_blocks<6?1:(int) (_num_key_blocks/6);//Runtime.getRuntime().availableProcessors()/2*2+10;
		final int thread_number = Math.min(Runtime.getRuntime().availableProcessors()/2*2+5, split_keys_thread_number);

		SearchLauncher.poolEUSize.set(SearchLauncher.dirtyProgressCounter=0);

		SearchLauncher.thread_number_count = split_keys_thread_number;
		final int step = (int) (_num_key_blocks/split_keys_thread_number);
		final int yuShu=(int) (_num_key_blocks%split_keys_thread_number);

		ExecutorService fixedThreadPoolmy = OpenThreadPool(thread_number);

		//show("~"+step+"~"+split_keys_thread_number+"~"+_num_key_blocks);

		ArrayList<Integer>[] _combining_search_tree=SearchLauncher.getCombinedTree(SelfAtIdx);
		boolean hold=false;
		if(SearchLauncher.combining_search_tree==null){
			hold=true;
			_combining_search_tree=parent!=null?parent.combining_search_tree2:combining_search_tree2;
		}
		if(_combining_search_tree==null || _combining_search_tree.length!=split_keys_thread_number){
			_combining_search_tree = new ArrayList[split_keys_thread_number];
			if(hold){
				if(parent!=null)
					parent.combining_search_tree2=_combining_search_tree;
				else
					combining_search_tree2=_combining_search_tree;
			}
			else
				SearchLauncher.setCombinedTree(SelfAtIdx, _combining_search_tree);
		}


		for(int ti=0; ti<split_keys_thread_number; ti++){//分  thread_number 股线程运行
			if(SearchLauncher.IsInterrupted || searchCancled ) break;
			//if(false)
			if(split_keys_thread_number>thread_number) while (SearchLauncher.poolEUSize.get()>=thread_number) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(split_keys_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(1);
			final int it = ti;
			Regex finalJoniregex = Joniregex;
			Pattern finalKeyPattern = keyPattern;
			byte[][][] finalMatcher = matcher;
			ArrayList<Integer>[] final_combining_search_tree = _combining_search_tree;
			fixedThreadPoolmy.execute(
					new Runnable(){@Override public void run()
					{
						if(SearchLauncher.IsInterrupted || searchCancled ) {SearchLauncher.poolEUSize.set(0); return; }
						int jiaX=0;
						if(it==split_keys_thread_number-1) jiaX=yuShu;
						if(final_combining_search_tree[it]==null)
							final_combining_search_tree[it] = new ArrayList<>();
						ArrayList<Integer> item = final_combining_search_tree[it];

						int compressedSize_many = 0, _maxDecomKeyBlockSize = 0;
						//小循环
						for(int blockId=it*step; blockId<it*step+step+jiaX; blockId++){
							//prepareItemByKeyInfo(_key_block_info_list[blockCounter],blockCounter);
							key_info_struct infoI = _key_block_info_list[blockId];
							_maxDecomKeyBlockSize = Math.max(_maxDecomKeyBlockSize, (int) infoI.key_block_decompressed_size);
							if(blockId==_key_block_info_list.length-1)
								compressedSize_many += _key_block_size - _key_block_info_list[_key_block_info_list.length-1].key_block_compressed_size_accumulator;
							else
								compressedSize_many += _key_block_info_list[blockId+1].key_block_compressed_size_accumulator-infoI.key_block_compressed_size_accumulator;
						}
						//TODO optimise compressedSize_many
						//SU.Log("compressedSize_many;;"+compressedSize_many);
						byte[] key_block = new byte[_maxDecomKeyBlockSize];/*分配资源 maxDecomKeyBlockSize 32770   65536 (common cache for index blocks)*/
						long start = _key_block_info_list[it*step].key_block_compressed_size_accumulator;

						try {
							DataInputStream data_in = getStreamAt(_key_block_offset+start);

							byte[]  _key_block_compressed_many = new byte[ compressedSize_many];
							data_in.read(_key_block_compressed_many, 0, _key_block_compressed_many.length);
							data_in.close();
							data_in=null;
							//大循环
							for(int blockId=it*step; blockId<it*step+step+jiaX; blockId++){
								if(SearchLauncher.IsInterrupted || searchCancled ) { SearchLauncher.poolEUSize.set(0); return; }

								int compressedSize;
								key_info_struct infoI = _key_block_info_list[blockId];

								//redundant check growing cache size
								if(infoI.key_block_decompressed_size>key_block.length) {
									key_block=null;
									key_block = new byte[(int) maxDecomKeyBlockSize];
								}

								if(blockId==_key_block_info_list.length-1)
									compressedSize = (int) (_key_block_size - _key_block_info_list[_key_block_info_list.length-1].key_block_compressed_size_accumulator);
								else
									compressedSize = (int) (_key_block_info_list[blockId+1].key_block_compressed_size_accumulator-infoI.key_block_compressed_size_accumulator);

								int startI = (int) (infoI.key_block_compressed_size_accumulator-start);


								//byte[] record_block_type = new byte[]{_key_block_compressed_many[(int) startI],_key_block_compressed_many[(int) (startI+1)],_key_block_compressed_many[(int) (startI+2)],_key_block_compressed_many[(int) (startI+3)]};
								//int adler32 = getInt(_key_block_compressed_many[(int) (startI+4)],_key_block_compressed_many[(int) (startI+5)],_key_block_compressed_many[(int)(startI+6)],_key_block_compressed_many[(int) (startI+7)]);

								//SU.Log(key_block.length+";;"+infoI.key_block_decompressed_size+";;"+maxDecomKeyBlockSize);
								//SU.Log(_key_block_compressed_many.length, startI, key_block.length,_key_block_size-8);
								//解压开始
								switch (_key_block_compressed_many[startI]|_key_block_compressed_many[startI+1]<<8|_key_block_compressed_many[startI+2]<<16|_key_block_compressed_many[startI+3]<<32){
									case 0:
										System.arraycopy(_key_block_compressed_many, (startI+8), key_block, 0, (int) infoI.key_block_decompressed_size);
									break;
									case 1:
										new LzoDecompressor1x().decompress(_key_block_compressed_many, startI+8, compressedSize-8, key_block, 0,new lzo_uintp());
									break;
									case 2:
										Inflater inf = new Inflater();
										inf.setInput(_key_block_compressed_many,(startI+8),(compressedSize-8));
										try {
											int ret = inf.inflate(key_block,0,(int)(infoI.key_block_decompressed_size));
										} catch (DataFormatException e) {e.printStackTrace();}
									break;
								}
								find_in_keyBlock(finalJoniregex, finalKeyPattern, key_block,infoI, finalMatcher,SelfAtIdx,item, SearchLauncher);
							}
							_key_block_compressed_many=null;
						}
						catch (Exception e1) {
							e1.printStackTrace();
							if(e1 instanceof SyntaxException){
								SearchLauncher.IsInterrupted =true;
								SearchLauncher.ErrorMessage=e1.getMessage();
							}
						}
						SearchLauncher.thread_number_count--;
						if(split_keys_thread_number>thread_number) SearchLauncher.poolEUSize.addAndGet(-1);
					}});
		}//任务全部分发完毕
		fixedThreadPoolmy.shutdown();
		try {
			fixedThreadPoolmy.awaitTermination(1, TimeUnit.MINUTES);
			fixedThreadPoolmy.shutdownNow();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		fixedThreadPoolmy=null;
		//System.gc();
	}

	protected ExecutorService OpenThreadPool(int thread_number) {
		if(parent!=null)
			return parent.OpenThreadPool(thread_number);
		return Executors.newFixedThreadPool(thread_number);
	}

	protected int flowerIndexOf(byte[] source, int sourceOffset, int sourceCount, byte[][][] matchers, int marcherOffest, int fromIndex, AbsAdvancedSearchLogicLayer searchLauncher, F1ag flag)
	{
		//if(GlobalOptions.debug)SU.Log("flowerIndexOf", sourceOffset, sourceCount);
		int lastSeekLetSize=0;
		int totalLen = matchers.length;
		boolean bSearchInContents = searchLauncher.type==2||searchLauncher.type==-2;
		while(fromIndex<sourceCount) {
			//SU.Log("==");
			//int idx = -1;
			int fromIndex_=fromIndex;
			boolean isSeeking=true;
			boolean Matched = false;
			boolean pass; int len;
			for(int lexiPartIdx=marcherOffest;lexiPartIdx<totalLen;lexiPartIdx++) {
				//if(fromIndex_>sourceCount-1) return -1;
				//SU.Log("stst: "+sourceCount+"::"+(fromIndex_+seekPos)+" fromIndex_: "+fromIndex_+" seekPos: "+seekPos+" lexiPartIdx: "+lexiPartIdx);

				//SU.Log("seekPos: "+seekPos+" lexiPartIdx: "+lexiPartIdx+" fromIndex_: "+fromIndex_);
				if(searchLauncher.miansi.contains(lexiPartIdx)) {
					if(lexiPartIdx==totalLen-1) {
						if(fromIndex_>=sourceCount)
							return -1;
						return fromIndex-lastSeekLetSize;//HERE
					}//Matched=true
					//SU.Log("miansi: "+lexiPartIdx);
					//SU.Log("miansi: "+sourceCount+"::"+(fromIndex_+seekPos)+"sourceL: "+source.length);
					//SU.Log("jumpped c is: "+new String(source,fromIndex_+seekPos,Math.min(4, sourceCount-(fromIndex_+seekPos-sourceOffset)),_encoding).substring(0, 1));
					int newSrcCount = Math.min(4, sourceCount-(fromIndex_));
					if(newSrcCount<=0)
						return -1;
					//TODO calc char length for different charsets.
					String c = new String(source,sourceOffset+fromIndex_,newSrcCount,_charset);
					int jumpShort = c.substring(0, 1).getBytes(_charset).length;
					fromIndex_+=jumpShort;
					continue;
				} else if(searchLauncher.yueji.contains(lexiPartIdx)) {
					if(lexiPartIdx==totalLen-1)
						return fromIndex-lastSeekLetSize;//HERE
					if(flowerIndexOf(source, sourceOffset+fromIndex_,sourceCount-(fromIndex_), matchers,lexiPartIdx+1, 0, searchLauncher, flag)!=-1){
						return fromIndex-lastSeekLetSize;
					}
					return -1;
				}
				Matched = false;
				if(isSeeking) {
					int seekPos=-1;
					int	newSeekPos = kalyxIndexOf(source, sourceOffset, sourceCount, matchers[lexiPartIdx], fromIndex_, flag);
					//if(newSeekPos>=0)
					//SU.Log("newSeekPos", newSeekPos);
					if(newSeekPos>=fromIndex_){
						//todo verify first match
						pass = true;
						if (checkEven != 0 && (len = newSeekPos - fromIndex_) != 0) {
							if (checkEven == 2) {
								pass = len % 4 == 0;
							} else if (checkEven == 1 && len % 2 != 0) {
								pass = false;
							} else {
								len = sourceOffset + newSeekPos;
								int start = (checkEven == 3) ? Math.max(sourceOffset + fromIndex_, sourceOffset + newSeekPos - maxEB) : (sourceOffset + fromIndex_);
								//int start = Math.max(sourceOffset+fromIndex_, sourceOffset+newSeekPos-4);
								len = len - start;
								String validfyCode = new String(source, start, len, _charset);
								//SU.Log("validfyCode", validfyCode);
								len = validfyCode.length();
								pass = len > 0 && validfyCode.charAt(len - 1) != 65533;
							}
						}
						if (pass) {
							if(bSearchInContents){
								//todo skip html tags 检查不在<>之中。 往前找>，截止于<>两者。若找先到<则放行。
								//									若找先到>则需要进一步检查。
								//									往后找<，截止于<>两者。若找先到>则放行。
								//									若找先到<则简单认为需要跳过。
								//if (bingStartWith(source, sourceOffset, sourceCount, htmlOpenTag, 0, htmlOpenTag.length, newSeekPos+marchLet.length)) {
									//CMN.Log("found htmlOpenTag!!!");
								//}

							}
							seekPos = newSeekPos;
							lastSeekLetSize = matchers[lexiPartIdx][flag.val].length;
							Matched = true;
						}
					}
					//SU.Log("seekPos:"+seekPos+" fromIndex_: "+fromIndex_);
					if(!Matched)
						return -1;
					seekPos+=lastSeekLetSize;
					fromIndex=fromIndex_=seekPos;
					isSeeking=false;
					continue;
				}/* End seek */
				else {
					//SU.Log("deadline"+fromIndex_+" "+sourceCount);
					if(fromIndex_>sourceCount-1) {
						//if(GlobalOptions.debug) SU.Log("deadline reached"+fromIndex_+" "+sourceCount);
						return -1;
					}
//					if(GlobalOptions.debug) {
//						SU.Log("matchedHonestily? ", lexiPartIdx, searchLauncher.mParallelKeys[lexiPartIdx].get(0));
//						CMN.Log("matchedHonestily? str", new String(source, sourceOffset+fromIndex_, 100, _charset));
//					}
					for(byte[] marchLet:matchers[lexiPartIdx]) {
						if(marchLet==null) break;
						if(bingStartWith(source,sourceOffset, sourceCount, marchLet,0,marchLet.length,fromIndex_)) {
							Matched=true;
//							if(GlobalOptions.debug) {
//								SU.Log("matchedHonestily: ", sourceCount, "::", " fromIndex_: ", fromIndex_ + " seekPos: ");
//								SU.Log("matchedHonestily: ", lexiPartIdx, searchLauncher.mParallelKeys[lexiPartIdx].get(0));
//							}
							fromIndex_+=marchLet.length;
							break;
						}
					}
					if(!Matched && bSearchInContents) {
						//todo skip html tags 三步，检查紧邻< ，搜索>，而后推进fromIndex_

					}
				}/* End honest match */
				if(!Matched) {
					//SU.Log("Matched failed this round: "+lexiPartIdx);
					break;
				}
				//fromIndex_+=matchers[lexiPartIdx][0].length;
			}/* End lexical parts loop */
			if(Matched)
				return fromIndex-lastSeekLetSize;
		}
		return -1;
	}

	protected byte[][][] leafSanLieZhi(AbsAdvancedSearchLogicLayer searchLauncher) {
		ArrayList<String>[] pm = searchLauncher.mParallelKeys;
		byte[][][] res = new byte[pm.length][][];
		for (int i = 0; i < pm.length; i++) {
			if(pm[i]!=null){
				int size = pm[i].size();
				byte[][] lexipart = new byte[size][];
				for (int j = 0; j < size; j++) {
					lexipart[j] = pm[i].get(j).getBytes(_charset);
				}
				res[i] = lexipart;
			}
		}
		return res;
	}

	/** Disperse an search term into a 2D byte array. */
	protected byte[][] flowerSanLieZhi(String str, AbsAdvancedSearchLogicLayer searchLauncher) {
		searchLauncher.miansi.clear();
		searchLauncher.yueji.clear();
		int len=str.length();
		byte[][] res = new byte[len][];
		for(int i=0;i<len;i++){
			char ch = str.charAt(i);
			if(ch=='.')
				searchLauncher.miansi.add(i);
			else if(ch=='*')
				searchLauncher.yueji.add(i);
			else
				res[i] = str.substring(i, i+1).getBytes(_charset);
		}
		return res;
	}


	protected void find_in_keyBlock(Regex JoniRegx, Pattern keyPattern, byte[] key_block, key_info_struct infoI, byte[][][] matcher, int SelfAtIdx, ArrayList<Integer> it, AbsAdvancedSearchLogicLayer SearchLauncher) {
		//org.joni.Matcher Jonimatcher = null;
		//if(JoniRegx!=null)
		//	Jonimatcher = JoniRegx.matcher(key_block);
		//!!spliting curr Key block
		int key_start_index = 0;
		//String delimiter;
		int key_end_index;
		//int keyCounter = 0;
		Flag flag = new Flag();
		//ByteBuffer sf = ByteBuffer.wrap(key_block);//must outside of while...
		int keyCounter = 0;
		while(key_start_index < infoI.key_block_decompressed_size){
			//long key_id;
			//if(_version<2)
			// sf.position(4);
			//key_id = sf.getInt(key_start_index);//Key_ID
			//else
			// sf.position(8);
			//key_id = sf.getLong(key_start_index);//Key_ID
			//show("key_id"+key_id);
			key_end_index = key_start_index + _number_width;

			SK_DELI:
			while(true){
				for(int sker=0;sker<delimiter_width;sker++) {
					if(key_block[key_end_index+sker]!=0) {
						key_end_index+=delimiter_width;
						continue SK_DELI;
					}
				}
				break;
			}

			//if(true)
			try {
				//TODO: alter
				//xxxx
				int try_idx = JoniRegx==null?
						flowerIndexOf(key_block,key_start_index+_number_width, key_end_index-(key_start_index+_number_width), matcher,0,0, SearchLauncher, flag)
						:JoniRegx.matcher(key_block, key_start_index+_number_width, key_end_index).search(key_start_index+_number_width, key_end_index, Option.SINGLELINE)
						;

				if(try_idx!=-1){
					//复核 re-collate
					if(false)
					if (keyPattern != null){
						String LexicalEntry = new String(key_block, key_start_index + _number_width, key_end_index - (key_start_index + _number_width), _charset);
						//SU.Log("checking ", LexicalEntry);
						if (!keyPattern.matcher(LexicalEntry).find()) {
							CMN.Log("发现错匹！！！", LexicalEntry, _Dictionary_fName);
//							key_start_index = key_end_index + delimiter_width;
//							SearchLauncher.dirtyProgressCounter++;
//							keyCounter++;
//							continue;
						}
					}
					//StringBuilder sb = new StringBuilder(LexicalEntry);
					//byte[] arraytmp = new byte[key_end_index-(key_start_index+_number_width)];
					//System.arraycopy(key_block,key_start_index+_number_width, arraytmp, 0,arraytmp.length);
					//additiveMyCpr1 tmpnode = new additiveMyCpr1(LexicalEntry,""+SelfAtIdx+""+((int) (infoI.num_entries_accumulator+keyCounter)));//new ArrayList<Integer>() new int[] {SelfAtIdx,(int) (infoI.num_entries_accumulator+keyCounter)}
					//tmpnode.value.add(SelfAtIdx);
					//tmpnode.value.add((int) (infoI.num_entries_accumulator+keyCounter));
					it.add((int) (infoI.num_entries_accumulator+keyCounter));//new additiveMyCpr1(LexicalEntry,infoI.num_entries_accumulator+keyCounter));
					//SU.Log("fuzzyKeyCounter"+fuzzyKeyCounter);
					SearchLauncher.dirtyResultCounter++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}


			key_start_index = key_end_index + delimiter_width;
			keyCounter++;SearchLauncher.dirtyProgressCounter++;
		}
		//assert(adler32 == (calcChecksum(key_block)));
	}

	byte[] key_block_cache = null;
	int key_block_cacheId=-1;
	int key_block_Splitted_flag=-1;
	int[][] scaler = null;

	public int reduce(String phrase,byte[] data,int[][] scaler,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//int iI = start + len - 1;
			String zhujio= new String(data, scaler[start + len - 1][0], scaler[start + len - 1][1], _charset);
			/*if(!isCompact) {//fixing python writemdict compatibility
				  if(wml.hearderTextStr==null) {
					  wml.hearderTextStr=new String(wml.infoI.headerKeyText,_charset);
					  wml.tailerKeyTextStr=new String(wml.infoI.tailerKeyText,_charset);
				  }
				  if(wml.tailerKeyTextStr.compareTo(zhujio)>0 || wml.hearderTextStr.compareTo(zhujio)<0) {
					  zhujio = replaceReg2.matcher(zhujio).replaceAll(emptyStr);
				  }
			  }*/
			return phrase.compareTo(processMyText(zhujio))>0
					? reduce(phrase,data,scaler,start+len,end)
					: reduce(phrase,data,scaler,start,start+len);
		} else {
			return start;
		}
	}

	public int reduce2(byte[] phrase,byte[] data,int[][] scaler,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			//int iI = start + len - 1;
			String zhujio= new String(data, scaler[start + len - 1][0], scaler[start + len - 1][1], _charset);
			/*if(!isCompact) {//fixing python writemdict compatibility
				  if(wml.hearderTextStr==null) {
					  wml.hearderTextStr=new String(wml.infoI.headerKeyText,_charset);
					  wml.tailerKeyTextStr=new String(wml.infoI.tailerKeyText,_charset);
				  }
				  if(wml.tailerKeyTextStr.compareTo(zhujio)>0 || wml.hearderTextStr.compareTo(zhujio)<0) {
					  zhujio = replaceReg2.matcher(zhujio).replaceAll(emptyStr);
				  }
			  }*/
			byte[] sub_data = processMyText(zhujio).getBytes(_charset);

			return compareByteArray(phrase, sub_data)>0
					? reduce2(phrase,data,scaler,start+len,end)
					: reduce2(phrase,data,scaler,start,start+len);
		} else {
			return start;
		}
	}

	public ArrayList<myCpr<String, Integer>> combining_search_list;
	//联合搜索  555
	public void size_confined_lookUp5(String keyword,
									  RBTree_additive combining_search_tree, int SelfAtIdx, int theta) //多线程
	{
		if(virtualIndex!=null){
			virtualIndex.combining_search_list=combining_search_list;
			virtualIndex.size_confined_lookUp5(keyword, combining_search_tree, SelfAtIdx, theta);
			return;
		}
		int[][] scaler_ = null;
		byte[] key_block_cache_ = null;
		ArrayList<myCpr<String, Integer>> _combining_search_list = combining_search_list;
		keyword = processMyText(keyword);
		byte[] kAB = keyword.getBytes(_charset);
		if(_encoding.startsWith("GB")) {
			if(compareByteArray(_key_block_info_list[(int)_num_key_blocks-1].tailerKeyText,kAB)<0)
				return;
			if(compareByteArray(_key_block_info_list[0].headerKeyText,kAB)>0)
				return;
		}else {
			if((TailerTextStr==null? TailerTextStr =processMyText(new String(_key_block_info_list[(int)_num_key_blocks-1].tailerKeyText,_charset).toLowerCase()):TailerTextStr).compareTo(keyword)<0) {
				return;
			}
			if((HeaderTextStr==null? HeaderTextStr=processMyText(new String(_key_block_info_list[0].headerKeyText,_charset).toLowerCase()):HeaderTextStr).compareTo(keyword)>0) {
				if(!HeaderTextStr.startsWith(keyword))
					return;
			}
		}
		if(_key_block_info_list==null) read_key_block_info(null);
		int blockId = _encoding.startsWith("GB")?reduce_index2(kAB,0,_key_block_info_list.length):reduce_index(keyword,0,_key_block_info_list.length);

		if(blockId==-1) return;
		//show(_Dictionary_fName+_key_block_info_list[blockId].tailerKeyText+"1~"+_key_block_info_list[blockId].headerKeyText);
		//while(blockId!=0 &&  compareByteArray(_key_block_info_list[blockId-1].tailerKeyText,kAB)>=0)
		//	blockId--;

		boolean doHarvest=false;

		//OUT:
		while(theta>0) {//complexity explanation: the aim is to harvest at most number theta matching results. but they might be crossing-blocks.
			key_info_struct infoI = _key_block_info_list[blockId];
			try {
				long start = infoI.key_block_compressed_size_accumulator;
				long compressedSize;
				if(key_block_cacheId!=blockId || key_block_cache==null) {
					if(blockId==_key_block_info_list.length-1)
						compressedSize = _key_block_size - _key_block_info_list[_key_block_info_list.length-1].key_block_compressed_size_accumulator;
					else
						compressedSize = _key_block_info_list[blockId+1].key_block_compressed_size_accumulator-infoI.key_block_compressed_size_accumulator;

					DataInputStream data_in = getStreamAt(_key_block_offset+start);

					byte[]  _key_block_compressed = new byte[(int) compressedSize];
					data_in.read(_key_block_compressed, 0, _key_block_compressed.length);
					data_in.close();

					//int adler32 = getInt(_key_block_compressed[(int) (+4)],_key_block_compressed[(int) (+5)],_key_block_compressed[(int) (+6)],_key_block_compressed[(int) (+7)]);
					if(compareByteArrayIsPara(_zero4, _key_block_compressed)){
						//System.out.println("no compress!");
						key_block_cache_ = new byte[(int) (_key_block_compressed.length-start-8)];
						System.arraycopy(_key_block_compressed, (int)(start+8), key_block_cache_, 0,key_block_cache_.length);
					}else if(compareByteArrayIsPara(_1zero3, _key_block_compressed))
					{
						//MInt len = new MInt((int) infoI.key_block_decompressed_size);
						//key_block_cache_ = new byte[len.v];
						//byte[] arraytmp = new byte[(int) compressedSize];
						//System.arraycopy(_key_block_compressed, (int)(+8), arraytmp, 0,(int) (compressedSize-8));
						//MiniLZO.lzo1x_decompress(arraytmp,arraytmp.length,key_block_cache_,len);
						key_block_cache_ =  new byte[(int) infoI.key_block_decompressed_size];
						new LzoDecompressor1x().decompress(_key_block_compressed, 8, (int)(compressedSize-8), key_block_cache_, 0, new lzo_uintp());
					}
					else if(compareByteArrayIsPara(_2zero3, _key_block_compressed)){
						//key_block_cache_ = zlib_decompress(_key_block_compressed,(int) (+8),(int)(compressedSize-8));
						key_block_cache_ =  new byte[(int) infoI.key_block_decompressed_size];
						Inflater inf = new Inflater();
						inf.setInput(_key_block_compressed,8,(int)compressedSize-8);
						int ret = inf.inflate(key_block_cache_,0,key_block_cache_.length);
					}
					key_block_cache=key_block_cache_;
					key_block_cacheId = blockId;
				}else {
					key_block_cache_=key_block_cache;
				}
				/*!!spliting curr Key block*/
				if(key_block_Splitted_flag!=blockId || scaler==null) {
					if(!doHarvest)
						scaler_ = new int[(int) infoI.num_entries][2];
					int key_start_index = 0;
					int key_end_index;
					int keyCounter = 0;

					while(key_start_index < key_block_cache_.length){
						key_end_index = key_start_index + _number_width;
						SK_DELI:
						while(true){
							for(int sker=0;sker<delimiter_width;sker++) {
								if(key_block_cache_[key_end_index+sker]!=0) {
									key_end_index+=delimiter_width;
									continue SK_DELI;
								}
							}
							break;
						}
						//SU.Log(new String(key_block_cache_,key_start_index+_number_width,key_end_index-(key_start_index+_number_width),_charset));
						//if(EntryStartWith(key_block_cache_,key_start_index+_number_width,key_end_index-(key_start_index+_number_width),matcher)) {
						if(doHarvest) {
							String kI = new String(key_block_cache_, key_start_index+_number_width,key_end_index-(key_start_index+_number_width), _charset);
							if(processMyText(kI).startsWith(keyword)) {
								if(combining_search_tree!=null)
									combining_search_tree.insert(kI,SelfAtIdx,(int)(keyCounter+infoI.num_entries_accumulator));
								else
									_combining_search_list.add(new myCpr(kI,(int)(keyCounter+infoI.num_entries_accumulator)));
								theta--;
							}else return;
							if(theta<=0) return;
						}else {
							scaler_[keyCounter][0] = key_start_index+_number_width;
							scaler_[keyCounter][1] = key_end_index-(key_start_index+_number_width);
						}

						key_start_index = key_end_index + delimiter_width;
						keyCounter++;
					}
					if(!doHarvest) {
						scaler=scaler_;
						key_block_Splitted_flag=blockId;
					}
				}else {
					scaler_=scaler;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}


			if(!doHarvest) {
				int idx;
				if(_encoding.startsWith("GB"))
					idx = reduce2(kAB, key_block_cache_, scaler_, 0, (int) infoI.num_entries);
				else
					idx = reduce(keyword, key_block_cache_, scaler_, 0, (int) infoI.num_entries);
				//SU.Log(new String(key_block_cache_, scaler_[idx][0],scaler_[idx][1], _charset));
				//SU.Log(new String(key_block_cache_, scaler_[idx+1][0],scaler_[idx+1][1], _charset));

				String kI = new String(key_block_cache_, scaler_[idx][0],scaler_[idx][1], _charset);
				while(true) {
					if(processMyText(kI).startsWith(keyword)) {
						if(combining_search_tree!=null)
							combining_search_tree.insert(kI,SelfAtIdx,(int)(idx+infoI.num_entries_accumulator));
						else
							_combining_search_list.add(new myCpr<>(kI,(int)(idx+infoI.num_entries_accumulator)));
						theta--;
					}else
						return;
					idx++;
					//if(idx>=infoI.num_entries) SU.Log("nono!");
					if(theta<=0)//no need to proceed. Max results fetched.
						return;
					if(idx>=infoI.num_entries) {
						break;
					}
					kI = new String(key_block_cache_, scaler_[idx][0],scaler_[idx][1], _charset);
				}
				doHarvest=true;
			}
			++blockId;
			if(_key_block_info_list.length<=blockId) return;
		}
	}

	public String processKey(byte[] in){
		return processMyText(new String(in,_charset));
	}

	public static int  binary_find_closest(long[] array,long val){
		int middle;
		int iLen ;
		if(array==null || (iLen=array.length)<1){
			return -1;
		}
		int low=0,high=iLen-1;
		if(iLen==1){
			return 0;
		}
		if(val-array[0]<=0){
			return 0;
		}else if(val-array[iLen-1]>=0){
			return iLen-1;
		}
		int counter=0;
		long cprRes1,cprRes0;
		while(low<high){
			counter+=1;
			//System.out.println(low+":"+high);
			middle = (low+high)/2;
			cprRes1=array[middle+1]-val;
			cprRes0=array[middle  ]-val;
			if(cprRes0>=0){
				high=middle;
			}else if(cprRes1<=0){
				//System.out.println("cprRes1<=0 && cprRes0<0");
				//System.out.println(houXuan1);
				//System.out.println(houXuan0);
				low=middle+1;
			}else{
				//System.out.println("asd");
				high=middle;
			}
		}
		return low;
	}


	public String getAboutString() {
		return _header_tag.get("Description");
	}

	public String getDictInfo(){
		DecimalFormat numbermachine = new DecimalFormat("#.00");
		return new StringBuilder()
				.append("Engine Version: ").append(_version).append("<BR>")
				.append("CreationDate: ").append(_header_tag.get("CreationDate")).append("<BR>")
				.append("Charset &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : ").append(this._encoding).append("<BR>")
				.append("Num Entries: ").append(_num_entries).append("<BR>")
				.append("Num Key Blocks: ").append(_num_key_blocks).append("<BR>")
				.append("Num Rec Blocks: ").append(decode_record_block_size(null)).append("<BR>")
				.append("Avg. Key Block: ").append(numbermachine.format(1.0*_key_block_size/_num_key_blocks/1024)).append(" kb, ").append(numbermachine.format(1.0*_num_entries/_num_key_blocks)).append(" items / block").append("<BR>")
				.append("Avg. Rec Block: ").append(numbermachine.format(1.0*_record_block_size/_num_record_blocks/1024)).append(" kb, ").append(numbermachine.format(1.0*_num_entries/_num_record_blocks)).append(" items / block").append("<BR>")
				.append("Compact  排序: ").append(isCompact).append("<BR>")
				.append("StripKey 排序: ").append(isStripKey).append("<BR>")
				.append("Case Sensitive: ").append(isKeyCaseSensitive).append("<BR>")
				//.append(mdd==null?"&lt;no assiciated mdRes&gt;":("MdResource count "+mdd.getNumberEntries()+","+mdd._encoding+","+mdd._num_key_blocks+","+mdd._num_record_blocks)).append("<BR>")
				.append("Internal Name: ").append(_header_tag.get("Title")).append("<BR>")
				.append("Path: ").append(getPath()).toString();
	}

	static boolean bingStartWith(byte[] source, int sourceOffset, int sourceCount,byte[] target, int targetOffset, int targetCount, int fromIndex) {
		//if (fromIndex >= sourceCount || targetCount+fromIndex >= sourceCount) { // || targetCount+fromIndex>=sourceCount || fromIndex>=sourceCount
		//	return false;
		//}
		//if(targetCount<=-1)
		//	targetCount=target.length;
		//if(sourceOffset+targetCount>=source.length)
		//	return false;
		int i = sourceOffset+fromIndex;
		int v1 = i+targetCount-1, v2=targetOffset-i;
		for (; i <= v1 && source[i] == target[i+v2]; i++);
		return i==v1+1;
	}

	public String processMyText(String input) {
		String ret = isStripKey?replaceReg.matcher(input).replaceAll(emptyStr):input;
		int KeycaseStrategy=getCaseStrategy();
		return isKeyCaseSensitive?ret:(((KeycaseStrategy>0)?(KeycaseStrategy==2):bGlobalUseClassicalKeycase)?mOldSchoolToLowerCase(ret):ret.toLowerCase());
	}

	public String mOldSchoolToLowerCase(String input) {
		StringBuilder sb = new StringBuilder(input);
		for(int i=0;i<sb.length();i++) {
			if(sb.charAt(i)>='A' && sb.charAt(i)<='Z')
				sb.setCharAt(i, (char) (sb.charAt(i)+32));
		}
		return sb.toString();
	}

	public String processStyleSheet(String input, int pos) {
		if(_stylesheet.size()==0)
			return input;
		Matcher m = markerReg.matcher(input);
		//HashSet<String> Already = new HashSet<>();
		StringBuilder transcriptor = new StringBuilder();
		String last=null;
		int lastEnd=0;
		boolean returnRaw=true;
		while(m.find()) {
			String now = m.group(1);
			String[] nowArr = _stylesheet.get(now);
			if(nowArr==null)
				if("0".equals(now)) {
					nowArr=new String[] {getCachedEntryAt(pos),""};
				}
			if(nowArr==null) {
				if(last!=null) {
					transcriptor.append(last);
					last=null;
				}
				continue;
			}
			transcriptor.append(input, lastEnd, m.start());
			if(last!=null) transcriptor.append(last);
			transcriptor.append(StringEscapeUtils.unescapeHtml3(nowArr[0]));
			lastEnd = m.end();
			last = nowArr.length==2?StringEscapeUtils.unescapeHtml3(nowArr[1]):"";
			returnRaw=false;
		}
		if(returnRaw)
			return input;
		else
			return transcriptor.append(last==null?"":last).append(input.substring(lastEnd)).toString();
	}

	@Override
	public String toString() {
		return _Dictionary_fName+"("+hashCode()+")";
	}

	public void Rebase(File newPath) {
		if(!f.equals(newPath)) {
			String OldFName = _Dictionary_fName;
			f = newPath;
			calcFName();
			HashSet<String> mddCon = new HashSet<>();
			if(mdd!=null) {
				for (mdictRes md : mdd) {
					MoveOrRenameResourceLet(md, OldFName,_Dictionary_fName, newPath);
					mddCon.add(md.getPath());
				}
			}
			try {
				loadInResourcesFiles(mddCon);
			} catch (IOException ignored) {  }
		}
	}

	protected void MoveOrRenameResourceLet(mdictRes md, String token, String pattern, File newPath) {
		File f = md.f();
		String tokee = f().getName();
		if(tokee.startsWith(token) && tokee.charAt(Math.min(token.length(), tokee.length()))=='.'){
			String suffix = tokee.substring(token.length());
			String np = f.getParent();
			File mnp;
			if(np!=null && np.equals(np=newPath.getParent())){ //重命名
				mnp=new File(np, pattern+suffix);
			} else {
				mnp=new File(np, f.getName());
			}
			if(mnp!=null && f.renameTo(mnp)){
				md.Rebase(mnp);
			}
		}
	}

	private void loadInResourcesFiles(HashSet<String> mddCon) throws IOException {
		if(!isResourceFile){
			String fnTMP = f.getName();
			File p=f.getParentFile();
			if(p!=null && _num_record_blocks>=0) {
				String fname = fnTMP;
				int idx = fnTMP.lastIndexOf(".");
				if(idx!=-1){
					fname=fnTMP.substring(0,idx);
				}
				File f2 = new File(p.getAbsolutePath(), fname + ".0.txt");
				if(f2.exists()){
					ftd = new ArrayList<>();
					try {
						BufferedReader br = new BufferedReader(new FileReader(f2));
						String line;
						while((line=br.readLine())!=null){
							handleDebugLines(line.trim());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				f2 = new File(p.getAbsolutePath(), fname + ".mdd");
				if (f2.exists() && (mddCon==null||!mddCon.contains(f2.getPath()))) {
					mdd = new ArrayList<>();
					mdd.add(new mdictRes(f2.getAbsolutePath()));
					int cc = 1;
					while ((f2 = new File(p.getAbsolutePath(), fname + "." + (cc++) + ".mdd")).exists()) {
						if(mddCon==null||!mddCon.contains(f2.getPath()))
							mdd.add(new mdictRes(f2.getAbsolutePath()));
					}
				}
				//if(_header_tag.containsKey("SharedMdd")) {
				//}
			}
		}
	}

	protected void calcFName() {
		String filename = f.getName();
		_Dictionary_fName = filename;
		int tmpIdx = filename.length()-4;
		if(tmpIdx>0){
			if(filename.charAt(tmpIdx)=='.' && filename.regionMatches(true, tmpIdx+1, "md" ,0, 2)){
				isResourceFile = Character.toLowerCase(filename.charAt(tmpIdx+3))=='d';
				if(!isResourceFile){
					_Dictionary_fName = filename.substring(0, tmpIdx);
				}
			}
		}
	}
}


