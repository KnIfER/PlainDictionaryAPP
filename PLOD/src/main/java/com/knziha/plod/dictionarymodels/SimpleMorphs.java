package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.UniversalDictionaryInterface;
import com.knziha.plod.plaindict.CMN;

public class SimpleMorphs extends DictionaryAdapter{
	public final int lookUp(UniversalDictionaryInterface d, String keyword) {
		return d.lookUp(keyword, true);
	}
	
	public SimpleMorphs() {
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
		return -1;
	}
	
}
