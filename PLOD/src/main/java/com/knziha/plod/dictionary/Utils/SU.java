/*  Copyright 2018 PlainDict author

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

package com.knziha.plod.dictionary.Utils;

import com.knziha.plod.plaindict.CMN;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;


/**
 * @author KnIfER
 * @date 2018/05/31
 */
public class  SU{
	public static Object UniversalObject;
    public static boolean debug = true;//StringUtils
	public static byte[] EmptyBytes = new byte[0];
	public final static String EmptyString = StringUtils.EMPTY;
	public static long mid;
	public static String days = "一二三四五六七";
	
	public static String trimStart(String input) {
		int len = input.length();
        int st = 0;
        while ((st < len) && (input.charAt(st) <= ' ')) {
            st++;
        }
        return st > 0 ? input.substring(st, len) : input;
    }
    
	public static String trimEnd(String input) {
		int len = input.length();
        int ed = len;
        while ((ed > 1) && (input.charAt(ed-1) <= ' ')) {
			ed--;
        }
        return ed < len ? input.substring(0, ed) : input;
    }
	
    public static int compareTo(String strA,String strB,int start, int lim) {
        int len1 = strA.length();
        int len2 = strB.length();
        int _lim = Math.min(Math.min(len1-start, len2-start),lim);

        int k = 0;
        while (k < _lim) {
            char c1 = strA.charAt(k+start);
            char c2 = strB.charAt(k+start);
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return _lim==lim?0:len1 - len2;
    }


	public static void Log(Object... o) {
		CMN.Log(o);
//		StringBuilder msg= new StringBuilder("fatal_log_mdict : ");
//		if(o!=null)
//			for (Object value : o) {
//				if (value instanceof Exception) {
//					ByteArrayOutputStream s = new ByteArrayOutputStream();
//					PrintStream p = new PrintStream(s);
//					((Exception) value).printStackTrace(p);
//					msg.append(s.toString());
//				}
//				msg.append(value).append(" ");
//			}
//		System.out.println(msg);
	}
	public static String alter_file_suffix(String path, String h) {
		int idx = path.indexOf(".");
		if (idx > 0) {
			path = path.substring(0, idx);
		}
		path += h;
		return path;
	}
	public static long stst;
	public static long ststrt;
	public static long stst_add;
	public static void rt() {
		ststrt = System.currentTimeMillis();
	}
	public static void pt(Object...args) {
		SU.Log(CMN.listToStr(0, args)+" "+(System.currentTimeMillis()-ststrt));
	}
	
	public static boolean isNotGroupSuffix(String fname) {
		return !fname.regionMatches(true,fname.length()-4, ".set", 0, 4);
	}
	
	public static String legacySetFileName(String line) {
		if(isNotGroupSuffix(line)) { //legacy
			return line+".set";
		}
		return line;
	}
	
	public static boolean isNotEmpty(CharSequence cs) {
		int len=cs.length();
		if(len>0) {
			int st = 0;
			while ((st < len) && (cs.charAt(st) <= ' ')) {
				st++;
				len--;
			}
			if(len>0) {
				while ((st < len) && (cs.charAt(len - 1) <= ' ')) {
					len--;
				}
			}
		}
		return len>0;
	}
	
	public static void pt_mins(String...args) {
		SU.Log(args,((System.currentTimeMillis()-stst)/1000.f/60)+"m");
	}
	
	public static String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}
	
	public static String toHexRGB(int color) {
		color&=0xFFFFFF;
		if(color==0) {
			return "000";
		}
		String val = Integer.toHexString(color);
		for (int i = val.length(); i < 6; i++) {
			val = "0"+val;
		}
		return val;
	}
	
	public static int hashCode(String toHash, int start, int len) {
		int h=0;
		len = Math.min(toHash.length(), len);
		for (int i = start; i < len; i++) {
			h = 31 * h + Character.toLowerCase(toHash.charAt(i));
		}
		return h;
	}
	
	public static String valueOf(CharSequence text) {
		return text == null ? null : text.toString();
	}
	//static net.jpountz.lz4.LZ4Factory factory;

	public static void Lz4_decompress(byte[] compressed, int offset, byte[] output, int out_offset, int decompressedLen) {
//		if (factory==null) {
//			factory = net.jpountz.lz4.LZ4Factory.fastestInstance();
//		}
//		factory.fastDecompressor().decompress(compressed, offset, output, out_offset, decompressedLen);
	}
	public static void Zstd_decompress(byte[] compressed, int offset, int length, byte[] output, int out_offset, int decompressedLen) {
		////new ZstdDecompressor().decompress(compressed, offset, length, output, out_offset, decompressedLen);
		//Zstd.decompressByteArray(output, out_offset, decompressedLen, compressed, offset, length);
	}

	public boolean CharsequenceEqual(CharSequence cs1, CharSequence cs2) {
		if(cs1!=null&&cs2!=null) {
			int len1=cs1.length();
			if(len1==cs2.length()) {
				for (int i = 0; i < len1; i++) {
					if(cs1.charAt(i)!=cs2.charAt(i)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public static String removeDiacritics(String text) {
		if (!Normalizer.isNormalized(text, Normalizer.Form.NFD)) {
			text = Normalizer.normalize(text, Normalizer.Form.NFD);
			char[] chars = text.toCharArray();
			int j = 0;
			for (char c : chars) {
				chars[j] = c;
				if(c>'a'&&c<'z' || c>'A'&&c<'Z' || !IsMark(c)) j++;
			}
			text = new String(chars, 0, j);
		}
		return text;
	}
	
	private static boolean IsMark(char ch) {
		int gc = Character.getType(ch);
//
//		return gc == Character.NON_SPACING_MARK
//				|| gc == Character.ENCLOSING_MARK
//				|| gc == Character.COMBINING_SPACING_MARK;
		return gc>=Character.NON_SPACING_MARK&&gc<=Character.COMBINING_SPACING_MARK;
	}
	
	public static int min(int a, int b, int c) {
		return a < b ? (a < c ? a : c) : (b < c ? b : c);
	}
	
//	public static int similar(String s, String t, int f) {
//		int n = s.length(), m = t.length(), l = Math.max(m, n);
//		var d = []
//		if(f==0)
//			f=3;
//		int i, j, si, tj, cost;
//		if (n == 0) return m;
//		if (m == 0) return n;
//		for (i = 0; i <= n; i++) {
//			d[i] = []
//			d[i][0] = i
//		}
//		for (j = 0; j <= m; j++) {
//			d[0][j] = j
//		}
//		for (i = 1; i <= n; i++) {
//			si = s.charAt(i - 1)
//			for (j = 1; j <= m; j++) {
//				tj = t.charAt(j - 1)
//				if (si === tj) {
//					cost = 0
//				} else {
//					cost = 1
//				}
//				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
//			}
//		}
//		var res = (1 - d[n][m] / l)
//		return res.toFixed(f)
//	}
public static int getTrimmedLength(CharSequence s, int start, int end) {
	int len = s.length();
	
	while (start < len && s.charAt(start) <= ' ') {
		start++;
	}
	
	if (end < len) {
		while (end > start && s.charAt(end - 1) <= ' ') {
			end--;
		}
	} else {
		return -1;
	}
	
	return end - start;
}
}
	


