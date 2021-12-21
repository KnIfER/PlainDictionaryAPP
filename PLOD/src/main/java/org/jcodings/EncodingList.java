/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jcodings;
import org.jcodings.specific.*;

final class EncodingList {
    static final void load() {
        EncodingDB.declare("ASCII-8BIT", "ASCII");
        EncodingDB.declare("UTF-8", "UTF8");
        EncodingDB.declare("US-ASCII", "USASCII");
        EncodingDB.declare("Big5", "BIG5");
        EncodingDB.declare("Big5-HKSCS", "Big5HKSCS");
        EncodingDB.declare("Big5-UAO", "Big5UAO");
        EncodingDB.declare("CP949", "CP949");
        EncodingDB.declare("Emacs-Mule", "EmacsMule");
        EncodingDB.declare("EUC-JP", "EUCJP");
        EncodingDB.declare("EUC-KR", "EUCKR");
        EncodingDB.declare("EUC-TW", "EUCTW");
        EncodingDB.declare("GB18030", "GB18030");
        EncodingDB.declare("GBK", "GBK");
        EncodingDB.declare("ISO-8859-1", "ISO8859_1");
        EncodingDB.declare("ISO-8859-2", "ISO8859_2");
        EncodingDB.declare("ISO-8859-3", "ISO8859_3");
        EncodingDB.declare("ISO-8859-4", "ISO8859_4");
        EncodingDB.declare("ISO-8859-5", "ISO8859_5");
        EncodingDB.declare("ISO-8859-6", "ISO8859_6");
        EncodingDB.declare("ISO-8859-7", "ISO8859_7");
        EncodingDB.declare("ISO-8859-8", "ISO8859_8");
        EncodingDB.declare("ISO-8859-9", "ISO8859_9");
        EncodingDB.declare("ISO-8859-10", "ISO8859_10");
        EncodingDB.declare("ISO-8859-11", "ISO8859_11");
        EncodingDB.declare("ISO-8859-13", "ISO8859_13");
        EncodingDB.declare("ISO-8859-14", "ISO8859_14");
        EncodingDB.declare("ISO-8859-15", "ISO8859_15");
        EncodingDB.declare("ISO-8859-16", "ISO8859_16");
        EncodingDB.declare("KOI8-R", "KOI8R");
        EncodingDB.declare("KOI8-U", "KOI8U");
        EncodingDB.declare("Shift_JIS", "SJIS");
        EncodingDB.declare("UTF-16BE", "UTF16BE");
        EncodingDB.declare("UTF-16LE", "UTF16LE");
        EncodingDB.declare("UTF-32BE", "UTF32BE");
        EncodingDB.declare("UTF-32LE", "UTF32LE");
        EncodingDB.declare("Windows-31J", "Windows_31J");
        EncodingDB.declare("Windows-1250", "Windows_1250");
        EncodingDB.declare("Windows-1251", "Windows_1251");
        EncodingDB.declare("Windows-1252", "Windows_1252");
        EncodingDB.declare("Windows-1253", "Windows_1253");
        EncodingDB.declare("Windows-1254", "Windows_1254");
        EncodingDB.declare("Windows-1257", "Windows_1257");
        EncodingDB.ascii = EncodingDB.encodings.get("ASCII-8BIT".getBytes());
        EncodingDB.alias("BINARY", "ASCII-8BIT");
        EncodingDB.replicate("IBM437", "ASCII-8BIT");
        EncodingDB.alias("CP437", "IBM437");
        EncodingDB.replicate("IBM737", "ASCII-8BIT");
        EncodingDB.alias("CP737", "IBM737");
        EncodingDB.replicate("IBM775", "ASCII-8BIT");
        EncodingDB.alias("CP775", "IBM775");
        EncodingDB.replicate("CP850", "ASCII-8BIT");
        EncodingDB.alias("IBM850", "CP850");
        EncodingDB.replicate("IBM852", "ASCII-8BIT");
        EncodingDB.replicate("CP852", "IBM852");
        EncodingDB.replicate("IBM855", "ASCII-8BIT");
        EncodingDB.replicate("CP855", "IBM855");
        EncodingDB.replicate("IBM857", "ASCII-8BIT");
        EncodingDB.alias("CP857", "IBM857");
        EncodingDB.replicate("IBM860", "ASCII-8BIT");
        EncodingDB.alias("CP860", "IBM860");
        EncodingDB.replicate("IBM861", "ASCII-8BIT");
        EncodingDB.alias("CP861", "IBM861");
        EncodingDB.replicate("IBM862", "ASCII-8BIT");
        EncodingDB.alias("CP862", "IBM862");
        EncodingDB.replicate("IBM863", "ASCII-8BIT");
        EncodingDB.alias("CP863", "IBM863");
        EncodingDB.replicate("IBM864", "ASCII-8BIT");
        EncodingDB.alias("CP864", "IBM864");
        EncodingDB.replicate("IBM865", "ASCII-8BIT");
        EncodingDB.alias("CP865", "IBM865");
        EncodingDB.replicate("IBM866", "ASCII-8BIT");
        EncodingDB.alias("CP866", "IBM866");
        EncodingDB.replicate("IBM869", "ASCII-8BIT");
        EncodingDB.alias("CP869", "IBM869");
        EncodingDB.replicate("Windows-1258", "ASCII-8BIT");
        EncodingDB.alias("CP1258", "Windows-1258");
        EncodingDB.replicate("GB1988", "ASCII-8BIT");
        EncodingDB.replicate("macCentEuro", "ASCII-8BIT");
        EncodingDB.replicate("macCroatian", "ASCII-8BIT");
        EncodingDB.replicate("macCyrillic", "ASCII-8BIT");
        EncodingDB.replicate("macGreek", "ASCII-8BIT");
        EncodingDB.replicate("macIceland", "ASCII-8BIT");
        EncodingDB.replicate("macRoman", "ASCII-8BIT");
        EncodingDB.replicate("macRomania", "ASCII-8BIT");
        EncodingDB.replicate("macThai", "ASCII-8BIT");
        EncodingDB.replicate("macTurkish", "ASCII-8BIT");
        EncodingDB.replicate("macUkraine", "ASCII-8BIT");
        EncodingDB.replicate("CP950", "Big5");
        EncodingDB.set_base("Big5-HKSCS", "Big5");
        EncodingDB.alias("Big5-HKSCS:2008", "Big5-HKSCS");
        EncodingDB.replicate("CP951", "Big5-HKSCS");
        EncodingDB.set_base("Big5-UAO", "Big5");
        EncodingDB.dummy("IBM037");
        EncodingDB.alias("ebcdic-cp-us", "IBM037");
        EncodingDB.replicate("stateless-ISO-2022-JP", "Emacs-Mule");
        EncodingDB.alias("eucJP", "EUC-JP" /* UI-OSF Application Platform Profile for Japanese Environment Version 1.1 */);
        EncodingDB.replicate("eucJP-ms", "EUC-JP" /* TOG/JVC CDE/Motif Technical WG */);
        EncodingDB.alias("euc-jp-ms", "eucJP-ms");
        EncodingDB.replicate("CP51932", "EUC-JP");
        EncodingDB.replicate("EUC-JIS-2004", "EUC-JP" /* defined at JIS X 0213:2004 */);
        EncodingDB.alias("EUC-JISX0213", "EUC-JIS-2004" /* defined at JIS X 0213:2000, and obsolete at JIS X 0213:2004 */);
        EncodingDB.alias("eucKR", "EUC-KR");
        EncodingDB.alias("eucTW", "EUC-TW");
        EncodingDB.replicate("GB2312", "EUC-KR");
        EncodingDB.alias("EUC-CN", "GB2312");
        EncodingDB.alias("eucCN", "GB2312");
        EncodingDB.replicate("GB12345", "GB2312");
        EncodingDB.alias("CP936", "GBK");
        EncodingDB.dummy("ISO-2022-JP");
        EncodingDB.alias("ISO2022-JP", "ISO-2022-JP");
        EncodingDB.replicate("ISO-2022-JP-2", "ISO-2022-JP");
        EncodingDB.alias("ISO2022-JP2", "ISO-2022-JP-2");
        EncodingDB.replicate("CP50220", "ISO-2022-JP");
        EncodingDB.replicate("CP50221", "ISO-2022-JP");
        EncodingDB.alias("ISO8859-1", "ISO-8859-1");
        EncodingDB.alias("ISO8859-2", "ISO-8859-2");
        EncodingDB.alias("ISO8859-3", "ISO-8859-3");
        EncodingDB.alias("ISO8859-4", "ISO-8859-4");
        EncodingDB.alias("ISO8859-5", "ISO-8859-5");
        EncodingDB.alias("ISO8859-6", "ISO-8859-6");
        EncodingDB.replicate("Windows-1256", "ISO-8859-6");
        EncodingDB.alias("CP1256", "Windows-1256");
        EncodingDB.alias("ISO8859-7", "ISO-8859-7");
        EncodingDB.alias("ISO8859-8", "ISO-8859-8");
        EncodingDB.replicate("Windows-1255", "ISO-8859-8");
        EncodingDB.alias("CP1255", "Windows-1255");
        EncodingDB.alias("ISO8859-9", "ISO-8859-9");
        EncodingDB.alias("ISO8859-10", "ISO-8859-10");
        EncodingDB.alias("ISO8859-11", "ISO-8859-11");
        EncodingDB.replicate("TIS-620", "ISO-8859-11");
        EncodingDB.replicate("Windows-874", "ISO-8859-11");
        EncodingDB.alias("CP874", "Windows-874");
        EncodingDB.alias("ISO8859-13", "ISO-8859-13");
        EncodingDB.alias("ISO8859-14", "ISO-8859-14");
        EncodingDB.alias("ISO8859-15", "ISO-8859-15");
        EncodingDB.alias("ISO8859-16", "ISO-8859-16");
        EncodingDB.alias("CP878", "KOI8-R");
        EncodingDB.replicate("MacJapanese", "Shift_JIS");
        EncodingDB.alias("MacJapan", "MacJapanese");
        EncodingDB.alias("ASCII", "US-ASCII");
        EncodingDB.alias("ANSI_X3.4-1968", "US-ASCII");
        EncodingDB.alias("646", "US-ASCII");
        EncodingDB.dummy("UTF-7");
        EncodingDB.alias("CP65000", "UTF-7");
        EncodingDB.alias("CP65001", "UTF-8");
        EncodingDB.replicate("UTF8-MAC", "UTF-8");
        EncodingDB.alias("UTF-8-MAC", "UTF8-MAC");
        EncodingDB.alias("UTF-8-HFS", "UTF8-MAC" /* Emacs 23.2 */);
        EncodingDB.dummy_unicode("UTF-16");
        EncodingDB.dummy_unicode("UTF-32");
        EncodingDB.alias("UCS-2BE", "UTF-16BE");
        EncodingDB.alias("UCS-4BE", "UTF-32BE");
        EncodingDB.alias("UCS-4LE", "UTF-32LE");
        EncodingDB.alias("CP932", "Windows-31J");
        EncodingDB.alias("csWindows31J", "Windows-31J" /* IANA.  IE6 don't accept Windows-31J but csWindows31J. */);
        EncodingDB.alias("SJIS", "Windows-31J");
        EncodingDB.alias("PCK", "Windows-31J");
        EncodingDB.alias("CP1250", "Windows-1250");
        EncodingDB.alias("CP1251", "Windows-1251");
        EncodingDB.alias("CP1252", "Windows-1252");
        EncodingDB.alias("CP1253", "Windows-1253");
        EncodingDB.alias("CP1254", "Windows-1254");
        EncodingDB.alias("CP1257", "Windows-1257");
        EncodingDB.replicate("UTF8-DoCoMo", "UTF-8");
        EncodingDB.replicate("SJIS-DoCoMo", "Windows-31J");
        EncodingDB.replicate("UTF8-KDDI", "UTF-8");
        EncodingDB.replicate("SJIS-KDDI", "Windows-31J");
        EncodingDB.replicate("ISO-2022-JP-KDDI", "ISO-2022-JP");
        EncodingDB.replicate("stateless-ISO-2022-JP-KDDI", "stateless-ISO-2022-JP");
        EncodingDB.replicate("UTF8-SoftBank", "UTF-8");
        EncodingDB.replicate("SJIS-SoftBank", "Windows-31J");
        EncodingDB.alias("MS932", "Windows-31J");
        EncodingDB.alias("UTF8", "UTF-8");
    }

    public static Encoding getInstance(String name) {
        switch (name) {
            case "ASCII": return ASCIIEncoding.INSTANCE;
            case "UTF8": return UTF8Encoding.INSTANCE;
            case "USASCII": return USASCIIEncoding.INSTANCE;
            case "BIG5": return BIG5Encoding.INSTANCE;
            case "Big5HKSCS": return Big5HKSCSEncoding.INSTANCE;
            case "Big5UAO": return Big5UAOEncoding.INSTANCE;
            case "CP949": return CP949Encoding.INSTANCE;
            case "EmacsMule": return EmacsMuleEncoding.INSTANCE;
            case "EUCJP": return EUCJPEncoding.INSTANCE;
            case "EUCKR": return EUCKREncoding.INSTANCE;
            case "EUCTW": return EUCTWEncoding.INSTANCE;
            case "GB18030": return GB18030Encoding.INSTANCE;
            case "GBK": return GBKEncoding.INSTANCE;
            case "ISO8859_1": return ISO8859_1Encoding.INSTANCE;
            case "ISO8859_2": return ISO8859_2Encoding.INSTANCE;
            case "ISO8859_3": return ISO8859_3Encoding.INSTANCE;
            case "ISO8859_4": return ISO8859_4Encoding.INSTANCE;
            case "ISO8859_5": return ISO8859_5Encoding.INSTANCE;
            case "ISO8859_6": return ISO8859_6Encoding.INSTANCE;
            case "ISO8859_7": return ISO8859_7Encoding.INSTANCE;
            case "ISO8859_8": return ISO8859_8Encoding.INSTANCE;
            case "ISO8859_9": return ISO8859_9Encoding.INSTANCE;
            case "ISO8859_10": return ISO8859_10Encoding.INSTANCE;
            case "ISO8859_11": return ISO8859_11Encoding.INSTANCE;
            case "ISO8859_13": return ISO8859_13Encoding.INSTANCE;
            case "ISO8859_14": return ISO8859_14Encoding.INSTANCE;
            case "ISO8859_15": return ISO8859_15Encoding.INSTANCE;
            case "ISO8859_16": return ISO8859_16Encoding.INSTANCE;
            case "KOI8R": return KOI8REncoding.INSTANCE;
            case "KOI8U": return KOI8UEncoding.INSTANCE;
            case "SJIS": return SJISEncoding.INSTANCE;
            case "UTF16BE": return UTF16BEEncoding.INSTANCE;
            case "UTF16LE": return UTF16LEEncoding.INSTANCE;
            case "UTF32BE": return UTF32BEEncoding.INSTANCE;
            case "UTF32LE": return UTF32LEEncoding.INSTANCE;
            case "Windows_31J": return Windows_31JEncoding.INSTANCE;
            case "Windows_1250": return Windows_1250Encoding.INSTANCE;
            case "Windows_1251": return Windows_1251Encoding.INSTANCE;
            case "Windows_1252": return Windows_1252Encoding.INSTANCE;
            case "Windows_1253": return Windows_1253Encoding.INSTANCE;
            case "Windows_1254": return Windows_1254Encoding.INSTANCE;
            case "Windows_1257": return Windows_1257Encoding.INSTANCE;
            default: return Encoding.load(name);
        }
    }
}
