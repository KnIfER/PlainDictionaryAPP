package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import java.util.ArrayList;

// 去后缀猜测单词原型
public class SimpleMorphs extends DictionaryAdapter {
	final MainActivityUIBase a;
	public final int lookUp(UniversalDictionaryInterface d, String keyword) {
		return d.lookUp(keyword, true);
	}
	
	public final int lookUp(UniversalDictionaryInterface d, String keyword, boolean str) {
		return d.lookUp(keyword, str);
	}
	
	public SimpleMorphs(MainActivityUIBase a) {
		this.a = a;
	}
	
	@Override
	public int guessRootWord(UniversalDictionaryInterface d, String keyword) {
		//CMN.debug("guessRootWord::", keyword);
		char ch = keyword.charAt(0);
		if(ch>='A' && ch<='z')
		try {
			final int len = keyword.length();
			ch = keyword.charAt(len - 1);
			if (ch == 's') {
				final boolean b1 = keyword.endsWith("'s");
				if (b1 || keyword.endsWith("es")) {
					int ret = lookUp(d, keyword.substring(0, len - 2));
					if (ret>=0||b1) {
						return ret;
					}
				}
				return lookUp(d, keyword.substring(0, len - 1));
			}
			else if (ch == 'g') {
				if (keyword.endsWith("ing")) {
					int ret;
					String k1 = keyword.substring(0, len - 3);
					ret = lookUp(d, k1);
					if(ret>=0) return ret;
					ret = lookUp(d, k1+'e');
					if(ret>=0) return ret;
					ret = lookUp(d, keyword.substring(0, len - 4));
					return ret;
				}
			}
			else if (ch == 'd') {
				if (keyword.endsWith("ed")) {
					int ret;
					ret = lookUp(d, keyword.substring(0, len - 3));
					if(ret>=0) return ret;
					ret = lookUp(d, keyword.substring(0, len - 2));
					if(ret>=0) return ret;
					ret = lookUp(d, keyword.substring(0, len - 1));
					return ret;
				}
			}
			else if (ch == 'r') {
				if (keyword.endsWith("er")) {
					if (keyword.endsWith("ier")) {
						return lookUp(d, keyword.substring(0, len - 3) + "y");
					}
					return lookUp(d, keyword.substring(0, len - 2));
				}
			}
			else if (ch == 't') {
				if (keyword.endsWith("est")) {
					return lookUp(d, keyword.substring(0, len - 3));
				}
			}
			else if (ch == 'y') {
				if (len>3) {
					if (keyword.endsWith("ly")) {
						if (keyword.endsWith("ily")) {
							int ret = lookUp(d, keyword.substring(0, len - 3) + "y");
							if (ret>=0) return ret;
						}
						return lookUp(d, keyword.substring(0, len - 2), false);
					}
					if (keyword.endsWith("ity")) {
						return lookUp(d, keyword.substring(0, len - 3), false);
					}
					//return lookUp(d, keyword.substring(0, len - 1));
				}
			}
			else if (ch>='0'&&ch<='9') {
				int ln = len - 1;
				do {
					if (ln-1 > 0) {
						ln--;
						ch = keyword.charAt(ln - 1);
					} else {
						break;
					}
				} while(ch>='0'&&ch<='9');
				return lookUp(keyword.substring(0, ln));
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		else if (PDICMainAppOptions.jnFanTongSou()) {
			final int len = keyword.length();
			boolean altering = false;
			if (len>0 && len<7) {
				a.ensureTSHanziSheet(null);
				for (int i = 0; i < len; i++) {
					char c = keyword.charAt(i);
					if (a.fanJnMap.get(c)!=null || a.jnFanMap.get(c)!=null) {
						altering = true;
						break;
					}
				}
			}
			//CMN.debug("altering", altering);
			if (altering) {
				ArrayList<StringBuilder> folders = new ArrayList<>();
				folders.add(new StringBuilder()); // 原始构造
				for (int i = 0; i < len; i++) {
					char c = keyword.charAt(i);
					String str1 = a.fanJnMap.get(c); // 寻找替代
					String str2 = a.jnFanMap.get(c); // 寻找替代
					boolean b1=str1!=null, b2=str2!=null;
					{
						String str = "";
						if(b1) str+=str1;
						if(b2) str+=str2;
						if(!b1 && !b2) str+=c;
						for (int k = 0, sz=folders.size(); k < sz; k++) {
							StringBuilder sb = folders.get(k);
							final int iniLen = sb.length();
							boolean advc = false;
							for (int j = 0, allLen=str.length(); j < allLen; j++) { // test all variants
								char alter = str.charAt(j);
								sb.setLength(iniLen);
								sb.append(alter);
								String testKey = sb.toString();
								int idx = lookUp(d, testKey, false);
								if (idx>=0 && mdict.processText(d.getEntryAt(idx)).startsWith(mdict.processText(testKey))) {
									if (i==len-1) {
										if (!testKey.equals(keyword)) {
											return idx;
										}
										CMN.debug("不陪你玩了");
									}
									if (!advc) { // 入栈
										advc = true;
										if(j<allLen-1) sb = new StringBuilder(sb);
									} else {
										folders.add(sb); // 新来的
									}
								}
							}
							if (!advc) { // 出栈
								folders.remove(k);
								if (folders.size()==0) {
									return -1;
								}
								k--;
								sz--;
							}
						}
					}
				}
			}
		}
		return -1;
	}
	
}
