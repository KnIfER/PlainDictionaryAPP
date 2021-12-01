package com.knziha.plod.dictionaryBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.rbtree.RBTNode;
import com.knziha.rbtree.RBTree_duplicative;
import com.knziha.rbtree.myAbsCprKey;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;


/**
 * Author KnIfER
 * Date 2018/05/31
 */

public class mdictBuilder extends mdictBuilderBase {
	public String _stylesheet = "";
	private boolean isKeyCaseSensitive=false;
	private boolean isKeyCaseInsensitive=true;
	private boolean isDiacriticsInsensitive=false;
	private boolean isStripKey=true;
	String sharedMdd;
	private final String nullStr=null;
	private boolean hasSlavery;
	private boolean debuggingVI;
	private boolean bContentEditable;

	public mdictBuilder(String Dictionary_Name, String about,String charset) {
		//data_tree= new ArrayListTree<>();
		data_tree = new RBTree_duplicative<>();
		privateZone = new IntervalTree();
		_Dictionary_Name=Dictionary_Name;
		_about=StringEscapeUtils.escapeHtml4(about);
		if(charset.toUpperCase().startsWith("UTF-16")){
			charset="UTF-16LE";
		}
		_encoding=charset;
		_charset=Charset.forName(_encoding);
		mContentDelimiter="\r\n\0".getBytes(_charset);
	}

	public int insert(String key,String data) {
		data_tree.insertNode(new myCprKey(key,data));
		return 0;
	}
	
	/** WARNING: 设置后可能无法被其他软件解析！ */
	public void setContentDelimiter(@NonNull byte[] contentDelimiter) {
		mContentDelimiter = contentDelimiter;
	}

	/**
	 * @param fileStr file or string object
	 * @param contentStartExt Hardcode the content start & end & ext numbers, typically for index-only mdx.
	 * @return  the inserted entry node */
	public myCprKey insert(String key, @Nullable Object fileStr, long...contentStartExt) {
		if(contentStartExt.length<2) throw new IllegalStateException("where's start and ext??");
		if (extraNumbersDummy==null) {
			extraNumbersDummy = new long[contentStartExt.length];
		} else if(extraNumbersDummy.length!=contentStartExt.length) {
			throw new IllegalStateException("size not match!");
		}
		String data = null;
		if(fileStr instanceof String)
			data = (String) fileStr;
		myCprKey keyNode = new myCprKey(key, data);
		data_tree.insertNode(keyNode);
		if(fileStr instanceof File) {
			fileTree.put(keyNode, (File) fileStr);
		}
		hasHardcodedKeyId = true;
		keyNode.contentStartExt = contentStartExt;
		return keyNode;
	}

	public void insert(String key, File file) {
		myCprKey keyNode = new myCprKey(key, nullStr);
		data_tree.insertNode(keyNode);
		fileTree.put(keyNode, file);
	}

	public void insert(String key, ArrayList<myCprKey> bioc) {
		data_tree.insertNode(new myCprKey(key+"[<>]",nullStr));
		bookTree.put(key, bioc);
	}
	public void append(String key, File inhtml) {
		myCprKey keyNode = new myCprKey(key,nullStr);
		((ArrayListTree<myAbsCprKey>)data_tree).add(keyNode);
		fileTree.put(keyNode, inhtml);
	}
	
//	public void setHasNoRecords() {
//		bNoRecords = true;
//	}

	@Override
	String constructHeader() {
		if(debuggingVI || WriteOffset>0){
			return "<Dictionary PLODEXT=\"1\"/>";
		}
		String encoding = _encoding;
		if(encoding.equals("UTF-16LE"))
			encoding = "UTF-16"; //INCONGRUENT java charset
		if (encoding.equals(""))
			encoding = "UTF-8";
		final float _version = 2.0f;
		SimpleDateFormat timemachine = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		StringBuilder sb = new StringBuilder()//.append(new String(new byte[] {(byte) 0xff,(byte) 0xfe}, StandardCharsets.UTF_16LE))
				.append("<Dictionary GeneratedByEngineVersion=")//v
				.append("\"").append(_version).append("\"")
				.append(" RequiredEngineVersion=")//v
				.append("\"").append(_version).append("\"")
				.append(" PLOD=")//v
				.append("\"").append(1).append("\"")
				.append(" Encrypted=")
				.append("\"").append("0").append("\"")//is NO valid?
				.append(" Encoding=")
				.append("\"").append(encoding).append("\"")
				.append(" Format=")//Format
				.append("\"").append("Html").append("\"")
				.append(" CreationDate=")
				.append("\"").append(timemachine.format(new Date(System.currentTimeMillis()))).append("\"")
				.append(" Compact=")//c
				.append("\"").append("Yes").append("\"")
				//.append(" Compat=")//c
				//.append("\"").append("No").append("\"")
				.append(" KeyCaseSensitive=")//k
				.append("\"").append(isKeyCaseSensitive?"Yes":"No").append("\"")
				//.append(" StripKey=")//k
				//.append("\"").append(isStripKey?"Yes":"No").append("\"")
				.append(" KeyCaseInsensitive=")
				.append("\"").append(isKeyCaseInsensitive?"Yes":"No").append("\"")
				.append(" DiacriticsInsensitive=")
				.append("\"").append(isDiacriticsInsensitive?"Yes":"No").append("\"")
				.append(" Description=")
				.append("\"").append(_about).append("\"")
				.append(sharedMdd!=null?" SharedMdd='"+sharedMdd+"\"":"")
				.append(" Title=")
				.append("\"").append(_Dictionary_Name).append("\"")
				//.append(" DataSourceFormat=")
				//.append("\"").append(106).append("\"")
				.append(" StyleSheet=")
				.append("\"").append(_stylesheet).append("\"")
				//.append(" RegisterBy=")
				//.append("\"").append("\"")
				//.append(" RegCode=")
				//.append("\"").append("\"")
				;
		
		if(hasHardcodedKeyId)
			sb.append(" entryNumExt=").append("\"").append(extraNumbersDummy.length-1).append("\"");
		if(fields!=null) {
			for(String key:fields.keySet()) {
				sb.append(" ").append(key.replace(" ", "_")).append("=").append("\"").append(fields.get(key)).append("\"");
			}
		}
		
		if(hasSlavery)
			sb.append(" hasSlavery=").append("\"").append("\"");
		if(bContentEditable)
			sb.append(" editable=").append("\"yes\"");
		sb.append("/>");
		return sb.toString();
	}
	
	HashMap<String, String> fields;
	
	public String field(String fieldName, Object value) {
		if(fields==null) fields = new HashMap<>();
		return fields.put(fieldName, String.valueOf(value));
	}
	
	@Override
	byte[] bakeMarginKey(String key){
		return key.toLowerCase().replace(" ","").replace("-","").getBytes(_charset);
	}

	private int bookGetRecordLen(String key) {
		int len =0;
		ArrayList<myCprKey> bookc = bookTree.get(key.substring(0, key.length()-4));
		for(myCprKey xx:bookc) {
			if(xx.value!=null) {
				try {
					len+=xx.value.getBytes(_encoding).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			else
				len+=fileTree.get(xx.key).length();
		}
		return len;
	}

	private int bookGetNumKeys(String key) {
		ArrayList<myCprKey> bookc = bookTree.get(key.substring(0, key.length()-4));
		return bookc.size();
	}

	public int getCountOf(String key) {
		return ((ArrayListTree<myAbsCprKey>)data_tree).getCountOf(new myCprKey(key,""));
	}

	public void setSharedMdd(String name) {
		sharedMdd=name;
	}
	
	/** default to false. must be set to true if you what a KeyCase-Sensitive result. */
	public void setKeyCaseSensitive(boolean value) {
		isKeyCaseSensitive=value;
	}
	
	/** default to true. invalid if setKeyCaseSensitive was set to the same value */
	public void setKeyCaseInsensitive(boolean value) {
		isKeyCaseInsensitive=value;
	}
	
	/** default to false. */
	public void setDiacriticsInsensitive(boolean value) {
		isDiacriticsInsensitive=value;
	}

	protected String processMyText(String input) {
//		String ret = isStripKey?mdict.replaceReg.matcher(input).replaceAll(""):input;
//		return isKeyCaseSensitive?ret:ret.toLowerCase();
		String ret;
		try {
			ret = isStripKey?mdict.replaceReg.matcher(input).replaceAll(""):input;
		} catch (StackOverflowError e) {
			ret = input;
		}
		if(isDiacriticsInsensitive) {
			ret = SU.removeDiacritics(ret);
		}
		return isKeyCaseSensitive?ret:isKeyCaseInsensitive?ret.toLowerCase():mdict.AngloToLowerCase(ret);
	}

	public void setContentEditable(boolean val) {
		bContentEditable=val;
	}
	
	public void testInsert() {
		RBTree_duplicative<myAbsCprKey> data_tree = new RBTree_duplicative<>();
		
		mdictBuilder.myCprKey keyNode = new mdictBuilder.myCprKey("approximate", null);
		data_tree.insertNode(keyNode);
		
		keyNode = new mdictBuilder.myCprKey("ABOUT/APPROXIMATELY", null);
		data_tree.insertNode(keyNode);
		
		keyNode = new mdictBuilder.myCprKey("above", null);
		data_tree.insertNode(keyNode);
		
		keyNode = new mdictBuilder.myCprKey("ABOVE", null);
		data_tree.insertNode(keyNode);
		
		
		ArrayList<myAbsCprKey> _keys = data_tree.flatten();
		CMN.Log(_keys.toArray());
		
	}
	
	public void setName(String name) {
		_Dictionary_Name = name;
	}
	
	public interface PostEntryValueProcessor {
		void processEntry(mdictBuilder mdictBuilder, myCprKey entry);
	}
	
	public class myCprKey extends myAbsCprKey {
		public String value;
		public String compareKey;
		public PostEntryValueProcessor postProcessor;
		
		public myCprKey(String vk, String v) {
			super(vk);
			value=v;
			compareKey = processMyText(key);
		}
		@Override
		public int compareTo(myAbsCprKey other) {
			//if(other instanceof myCprKey)
			{
				if(key.endsWith(">") && other.key.endsWith(">")) {
					int idx2 = key.lastIndexOf("<",key.length()-2);
					if(idx2!=-1) {
						int idx3 = other.key.lastIndexOf("<",key.length()-2);
						if(idx3!=-1) {
							if(key.startsWith(other.key.substring(0,idx3))) {
								String itemA=key.substring(idx2+1,key.length()-1);
								String itemB=other.key.substring(idx2+1,other.key.length()-1);
								idx2=-1;idx3=-1;
								if(IU.shuzi.matcher(itemA).find()) {
									idx2=IU.parsint(itemA);
								}else if(IU.hanshuzi.matcher(itemA).find()) {
									idx2=IU.recurse1wCalc(itemA, 0, itemA.length()-1, 1);
								}
								if(idx2!=-1) {
									if(IU.shuzi.matcher(itemB).find()) {
										idx3=IU.parsint(itemB);
									}else if(IU.hanshuzi.matcher(itemB).find()) {
										idx3=IU.recurse1wCalc(itemB, 0, itemB.length()-1, 1);
									}
									if(idx3!=-1)
										return idx2-idx3;
								}

							}
						}
					}
				}
				//return (processMyText(key).compareTo(processMyText(other.key)));
				return (compareKey.compareTo(((myCprKey) other).compareKey));
				//return (key.compareToIgnoreCase(((myCprKey) other).key));
			}
			//return 111;
		}

		@Override
		public Object value() {
			return value;
		}

		@Override
		public byte[] getBinVals() {
			if(postProcessor!=null) {
				postProcessor.processEntry(mdictBuilder.this, this);
			}
			return value==null?null:value.getBytes(_charset);
		}
	}


	public mdictBuilder setAppendAfter(long offset){
		WriteOffset=offset;
		return this;
	}
	public mdictBuilder setAppendDebug(){
		WriteOffset=0;
		debuggingVI=true;
		return this;
	}
	public mdictBuilder setAsMaster(){
		hasSlavery=true;
		return this;
	}
	
	public myCprKey lookUp(String rawName) {
		//rawName = processMyText(rawName);
		RBTree_duplicative<myAbsCprKey> rbTree = (RBTree_duplicative<myAbsCprKey>) data_tree;
		myCprKey search = new myCprKey(rawName, null);
		RBTNode<myAbsCprKey> node = rbTree.sxing(search);
		myCprKey ret = null;
		if(node!=null) {
			ret = (myCprKey) node.getKey();
			//SU.Log(">>>"+ret.key+"<<<", ret.key.length());
			if(ret.compareTo(search)!=0) return null;
		}
		return ret;
	}
}