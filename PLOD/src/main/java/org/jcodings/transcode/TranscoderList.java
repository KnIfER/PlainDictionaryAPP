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
package org.jcodings.transcode;

import org.jcodings.transcode.Transcoder.GenericTranscoderEntry;
import org.jcodings.transcode.specific.*;

final class TranscoderList {
    static void load() {
        TranscoderDB.declare("Big5", "UTF-8", null /*From_Big5*/);
        TranscoderDB.declare("UTF-8", "Big5", null /*To_Big5*/);
        TranscoderDB.declare("CP950", "UTF-8", null /*From_CP950*/);
        TranscoderDB.declare("UTF-8", "CP950", null /*To_CP950*/);
        TranscoderDB.declare("Big5-HKSCS", "UTF-8", null /*From_Big5_HKSCS*/);
        TranscoderDB.declare("UTF-8", "Big5-HKSCS", null /*To_Big5_HKSCS*/);
        TranscoderDB.declare("CP951", "UTF-8", null /*From_CP951*/);
        TranscoderDB.declare("UTF-8", "CP951", null /*To_CP951*/);
        TranscoderDB.declare("Big5-UAO", "UTF-8", null /*From_Big5_UAO*/);
        TranscoderDB.declare("UTF-8", "Big5-UAO", null /*To_Big5_UAO*/);
        TranscoderDB.declare("GB2312", "UTF-8", null /*From_GB2312*/);
        TranscoderDB.declare("GB12345", "UTF-8", null /*From_GB12345*/);
        TranscoderDB.declare("UTF-8", "GB2312", null /*To_GB2312*/);
        TranscoderDB.declare("UTF-8", "GB12345", null /*To_GB12345*/);
        TranscoderDB.declare("IBM037", "ISO-8859-1", null /*From_IBM037_to_ISO_8859_1*/);
        TranscoderDB.declare("ISO-8859-1", "IBM037", null /*From_ISO_8859_1_to_IBM037*/);
        TranscoderDB.declare("UTF8-DoCoMo", "UTF8-KDDI", null /*From_UTF8_DoCoMo_to_UTF8_KDDI*/);
        TranscoderDB.declare("UTF8-DoCoMo", "UTF8-SoftBank", null /*From_UTF8_DoCoMo_to_UTF8_SoftBank*/);
        TranscoderDB.declare("UTF8-DoCoMo", "UTF-8", null /*From_UTF8_DoCoMo*/);
        TranscoderDB.declare("UTF8-KDDI", "UTF8-DoCoMo", null /*From_UTF8_KDDI_to_UTF8_DoCoMo*/);
        TranscoderDB.declare("UTF8-KDDI", "UTF8-SoftBank", null /*From_UTF8_KDDI_to_UTF8_SoftBank*/);
        TranscoderDB.declare("UTF8-KDDI", "UTF-8", null /*From_UTF8_KDDI*/);
        TranscoderDB.declare("UTF8-SoftBank", "UTF8-DoCoMo", null /*From_UTF8_SoftBank_to_UTF8_DoCoMo*/);
        TranscoderDB.declare("UTF8-SoftBank", "UTF8-KDDI", null /*From_UTF8_SoftBank_to_UTF8_KDDI*/);
        TranscoderDB.declare("UTF8-SoftBank", "UTF-8", null /*From_UTF8_SoftBank*/);
        TranscoderDB.declare("UTF-8", "UTF8-DoCoMo", null /*To_UTF8_DoCoMo*/);
        TranscoderDB.declare("UTF-8", "UTF8-KDDI", null /*To_UTF8_KDDI*/);
        TranscoderDB.declare("UTF-8", "UTF8-SoftBank", null /*To_UTF8_SoftBank*/);
        TranscoderDB.declare("stateless-ISO-2022-JP-KDDI", "UTF8-KDDI", null /*From_stateless_ISO_2022_JP_KDDI_to_UTF8_KDDI*/);
        TranscoderDB.declare("UTF8-KDDI", "stateless-ISO-2022-JP-KDDI", null /*From_UTF8_KDDI_to_stateless_ISO_2022_JP_KDDI*/);
        TranscoderDB.declare("ISO-2022-JP-KDDI", "stateless-ISO-2022-JP-KDDI", "Iso2022jp_kddi_decoder");
        TranscoderDB.declare("stateless-ISO-2022-JP-KDDI", "ISO-2022-JP-KDDI", "Iso2022jp_kddi_encoder");
        TranscoderDB.declare("SJIS-DOCOMO", "UTF8-DOCOMO", null /*From_SJIS_DOCOMO_to_UTF8_DOCOMO*/);
        TranscoderDB.declare("UTF8-DOCOMO", "SJIS-DOCOMO", null /*From_UTF8_DOCOMO_to_SJIS_DOCOMO*/);
        TranscoderDB.declare("SJIS-KDDI", "UTF8-KDDI", null /*From_SJIS_KDDI_to_UTF8_KDDI*/);
        TranscoderDB.declare("UTF8-KDDI", "SJIS-KDDI", null /*From_UTF8_KDDI_to_SJIS_KDDI*/);
        TranscoderDB.declare("SJIS-SoftBank", "UTF8-SoftBank", null /*From_SJIS_SoftBank_to_UTF8_SoftBank*/);
        TranscoderDB.declare("UTF8-SoftBank", "SJIS-SoftBank", null /*From_UTF8_SoftBank_to_SJIS_SoftBank*/);
        TranscoderDB.declare("", "amp_escape", null /*From__to_amp_escape*/);
        TranscoderDB.declare("", "xml_text_escape", null /*From__to_xml_text_escape*/);
        TranscoderDB.declare("", "xml_attr_content_escape", null /*From__to_xml_attr_content_escape*/);
        TranscoderDB.declare("", "xml_attr_quote", "Escape_xml_attr_quote");
        TranscoderDB.declare("GB18030", "UTF-8", "From_GB18030");
        TranscoderDB.declare("UTF-8", "GB18030", "To_GB18030");
        TranscoderDB.declare("GBK", "UTF-8", null /*From_GBK*/);
        TranscoderDB.declare("UTF-8", "GBK", null /*To_GBK*/);
        TranscoderDB.declare("ISO-2022-JP", "stateless-ISO-2022-JP", "Iso2022jp_decoder");
        TranscoderDB.declare("stateless-ISO-2022-JP", "ISO-2022-JP", "Iso2022jp_encoder");
        TranscoderDB.declare("stateless-ISO-2022-JP", "EUC-JP", "Stateless_iso2022jp_to_eucjp");
        TranscoderDB.declare("EUC-JP", "stateless-ISO-2022-JP", "Eucjp_to_stateless_iso2022jp");
        TranscoderDB.declare("CP50220", "cp51932", "Cp50220_decoder");
        TranscoderDB.declare("CP50221", "cp51932", "Cp50221_decoder");
        TranscoderDB.declare("CP51932", "CP50221", "Cp50221_encoder");
        TranscoderDB.declare("CP51932", "CP50220", "Cp50220_encoder");
        TranscoderDB.declare("EUC-JP", "Shift_JIS", "Eucjp2sjis");
        TranscoderDB.declare("Shift_JIS", "EUC-JP", "Sjis2eucjp");
        TranscoderDB.declare("EUC-JP", "UTF-8", null /*From_EUC_JP*/);
        TranscoderDB.declare("eucJP-ms", "UTF-8", null /*From_eucJP_ms*/);
        TranscoderDB.declare("CP51932", "UTF-8", null /*From_CP51932*/);
        TranscoderDB.declare("EUC-JIS-2004", "UTF-8", null /*From_EUC_JIS_2004*/);
        TranscoderDB.declare("UTF-8", "EUC-JP", null /*To_EUC_JP*/);
        TranscoderDB.declare("UTF-8", "eucJP-ms", null /*To_eucJP_ms*/);
        TranscoderDB.declare("UTF-8", "CP51932", null /*To_CP51932*/);
        TranscoderDB.declare("UTF-8", "EUC-JIS-2004", null /*To_EUC_JIS_2004*/);
        TranscoderDB.declare("Shift_JIS", "UTF-8", null /*From_Shift_JIS*/);
        TranscoderDB.declare("Windows-31J", "UTF-8", null /*From_Windows_31J*/);
        TranscoderDB.declare("UTF-8", "Shift_JIS", null /*To_Shift_JIS*/);
        TranscoderDB.declare("UTF-8", "Windows-31J", null /*To_Windows_31J*/);
        TranscoderDB.declare("UTF-8", "EUC-KR", null /*To_EUC_KR*/);
        TranscoderDB.declare("EUC-KR", "UTF-8", null /*From_EUC_KR*/);
        TranscoderDB.declare("UTF-8", "CP949", null /*To_CP949*/);
        TranscoderDB.declare("CP949", "UTF-8", null /*From_CP949*/);
        TranscoderDB.declare("", "universal_newline", "Universal_newline");
        TranscoderDB.declare("", "crlf_newline", null /*Crlf_newline*/);
        TranscoderDB.declare("", "cr_newline", null /*Cr_newline*/);
        TranscoderDB.declare("US-ASCII", "UTF-8", null /*From_US_ASCII*/);
        TranscoderDB.declare("UTF-8", "US-ASCII", null /*To_US_ASCII*/);
        TranscoderDB.declare("ASCII-8BIT", "UTF-8", null /*From_ASCII_8BIT*/);
        TranscoderDB.declare("UTF-8", "ASCII-8BIT", null /*To_ASCII_8BIT*/);
        TranscoderDB.declare("ISO-8859-1", "UTF-8", null /*From_ISO_8859_1*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-1", null /*To_ISO_8859_1*/);
        TranscoderDB.declare("ISO-8859-2", "UTF-8", null /*From_ISO_8859_2*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-2", null /*To_ISO_8859_2*/);
        TranscoderDB.declare("ISO-8859-3", "UTF-8", null /*From_ISO_8859_3*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-3", null /*To_ISO_8859_3*/);
        TranscoderDB.declare("ISO-8859-4", "UTF-8", null /*From_ISO_8859_4*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-4", null /*To_ISO_8859_4*/);
        TranscoderDB.declare("ISO-8859-5", "UTF-8", null /*From_ISO_8859_5*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-5", null /*To_ISO_8859_5*/);
        TranscoderDB.declare("ISO-8859-6", "UTF-8", null /*From_ISO_8859_6*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-6", null /*To_ISO_8859_6*/);
        TranscoderDB.declare("ISO-8859-7", "UTF-8", null /*From_ISO_8859_7*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-7", null /*To_ISO_8859_7*/);
        TranscoderDB.declare("ISO-8859-8", "UTF-8", null /*From_ISO_8859_8*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-8", null /*To_ISO_8859_8*/);
        TranscoderDB.declare("ISO-8859-9", "UTF-8", null /*From_ISO_8859_9*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-9", null /*To_ISO_8859_9*/);
        TranscoderDB.declare("ISO-8859-10", "UTF-8", null /*From_ISO_8859_10*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-10", null /*To_ISO_8859_10*/);
        TranscoderDB.declare("ISO-8859-11", "UTF-8", null /*From_ISO_8859_11*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-11", null /*To_ISO_8859_11*/);
        TranscoderDB.declare("ISO-8859-13", "UTF-8", null /*From_ISO_8859_13*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-13", null /*To_ISO_8859_13*/);
        TranscoderDB.declare("ISO-8859-14", "UTF-8", null /*From_ISO_8859_14*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-14", null /*To_ISO_8859_14*/);
        TranscoderDB.declare("ISO-8859-15", "UTF-8", null /*From_ISO_8859_15*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-15", null /*To_ISO_8859_15*/);
        TranscoderDB.declare("ISO-8859-16", "UTF-8", null /*From_ISO_8859_16*/);
        TranscoderDB.declare("UTF-8", "ISO-8859-16", null /*To_ISO_8859_16*/);
        TranscoderDB.declare("WINDOWS-874", "UTF-8", null /*From_WINDOWS_874*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-874", null /*To_WINDOWS_874*/);
        TranscoderDB.declare("WINDOWS-1250", "UTF-8", null /*From_WINDOWS_1250*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1250", null /*To_WINDOWS_1250*/);
        TranscoderDB.declare("WINDOWS-1251", "UTF-8", null /*From_WINDOWS_1251*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1251", null /*To_WINDOWS_1251*/);
        TranscoderDB.declare("WINDOWS-1252", "UTF-8", null /*From_WINDOWS_1252*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1252", null /*To_WINDOWS_1252*/);
        TranscoderDB.declare("WINDOWS-1253", "UTF-8", null /*From_WINDOWS_1253*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1253", null /*To_WINDOWS_1253*/);
        TranscoderDB.declare("WINDOWS-1254", "UTF-8", null /*From_WINDOWS_1254*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1254", null /*To_WINDOWS_1254*/);
        TranscoderDB.declare("WINDOWS-1255", "UTF-8", null /*From_WINDOWS_1255*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1255", null /*To_WINDOWS_1255*/);
        TranscoderDB.declare("WINDOWS-1256", "UTF-8", null /*From_WINDOWS_1256*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1256", null /*To_WINDOWS_1256*/);
        TranscoderDB.declare("WINDOWS-1257", "UTF-8", null /*From_WINDOWS_1257*/);
        TranscoderDB.declare("UTF-8", "WINDOWS-1257", null /*To_WINDOWS_1257*/);
        TranscoderDB.declare("IBM437", "UTF-8", null /*From_IBM437*/);
        TranscoderDB.declare("UTF-8", "IBM437", null /*To_IBM437*/);
        TranscoderDB.declare("IBM775", "UTF-8", null /*From_IBM775*/);
        TranscoderDB.declare("UTF-8", "IBM775", null /*To_IBM775*/);
        TranscoderDB.declare("IBM737", "UTF-8", null /*From_IBM737*/);
        TranscoderDB.declare("UTF-8", "IBM737", null /*To_IBM737*/);
        TranscoderDB.declare("IBM852", "UTF-8", null /*From_IBM852*/);
        TranscoderDB.declare("UTF-8", "IBM852", null /*To_IBM852*/);
        TranscoderDB.declare("IBM855", "UTF-8", null /*From_IBM855*/);
        TranscoderDB.declare("UTF-8", "IBM855", null /*To_IBM855*/);
        TranscoderDB.declare("IBM857", "UTF-8", null /*From_IBM857*/);
        TranscoderDB.declare("UTF-8", "IBM857", null /*To_IBM857*/);
        TranscoderDB.declare("IBM860", "UTF-8", null /*From_IBM860*/);
        TranscoderDB.declare("UTF-8", "IBM860", null /*To_IBM860*/);
        TranscoderDB.declare("IBM861", "UTF-8", null /*From_IBM861*/);
        TranscoderDB.declare("UTF-8", "IBM861", null /*To_IBM861*/);
        TranscoderDB.declare("IBM862", "UTF-8", null /*From_IBM862*/);
        TranscoderDB.declare("UTF-8", "IBM862", null /*To_IBM862*/);
        TranscoderDB.declare("IBM863", "UTF-8", null /*From_IBM863*/);
        TranscoderDB.declare("UTF-8", "IBM863", null /*To_IBM863*/);
        TranscoderDB.declare("IBM865", "UTF-8", null /*From_IBM865*/);
        TranscoderDB.declare("UTF-8", "IBM865", null /*To_IBM865*/);
        TranscoderDB.declare("IBM866", "UTF-8", null /*From_IBM866*/);
        TranscoderDB.declare("UTF-8", "IBM866", null /*To_IBM866*/);
        TranscoderDB.declare("IBM869", "UTF-8", null /*From_IBM869*/);
        TranscoderDB.declare("UTF-8", "IBM869", null /*To_IBM869*/);
        TranscoderDB.declare("MACCROATIAN", "UTF-8", null /*From_MACCROATIAN*/);
        TranscoderDB.declare("UTF-8", "MACCROATIAN", null /*To_MACCROATIAN*/);
        TranscoderDB.declare("MACCYRILLIC", "UTF-8", null /*From_MACCYRILLIC*/);
        TranscoderDB.declare("UTF-8", "MACCYRILLIC", null /*To_MACCYRILLIC*/);
        TranscoderDB.declare("MACGREEK", "UTF-8", null /*From_MACGREEK*/);
        TranscoderDB.declare("UTF-8", "MACGREEK", null /*To_MACGREEK*/);
        TranscoderDB.declare("MACICELAND", "UTF-8", null /*From_MACICELAND*/);
        TranscoderDB.declare("UTF-8", "MACICELAND", null /*To_MACICELAND*/);
        TranscoderDB.declare("MACROMAN", "UTF-8", null /*From_MACROMAN*/);
        TranscoderDB.declare("UTF-8", "MACROMAN", null /*To_MACROMAN*/);
        TranscoderDB.declare("MACROMANIA", "UTF-8", null /*From_MACROMANIA*/);
        TranscoderDB.declare("UTF-8", "MACROMANIA", null /*To_MACROMANIA*/);
        TranscoderDB.declare("MACTURKISH", "UTF-8", null /*From_MACTURKISH*/);
        TranscoderDB.declare("UTF-8", "MACTURKISH", null /*To_MACTURKISH*/);
        TranscoderDB.declare("MACUKRAINE", "UTF-8", null /*From_MACUKRAINE*/);
        TranscoderDB.declare("UTF-8", "MACUKRAINE", null /*To_MACUKRAINE*/);
        TranscoderDB.declare("KOI8-U", "UTF-8", null /*From_KOI8_U*/);
        TranscoderDB.declare("UTF-8", "KOI8-U", null /*To_KOI8_U*/);
        TranscoderDB.declare("KOI8-R", "UTF-8", null /*From_KOI8_R*/);
        TranscoderDB.declare("UTF-8", "KOI8-R", null /*To_KOI8_R*/);
        TranscoderDB.declare("TIS-620", "UTF-8", null /*From_TIS_620*/);
        TranscoderDB.declare("UTF-8", "TIS-620", null /*To_TIS_620*/);
        TranscoderDB.declare("CP850", "UTF-8", null /*From_CP850*/);
        TranscoderDB.declare("UTF-8", "CP850", null /*To_CP850*/);
        TranscoderDB.declare("CP852", "UTF-8", null /*From_CP852*/);
        TranscoderDB.declare("UTF-8", "CP852", null /*To_CP852*/);
        TranscoderDB.declare("CP855", "UTF-8", null /*From_CP855*/);
        TranscoderDB.declare("UTF-8", "CP855", null /*To_CP855*/);
        TranscoderDB.declare("UTF-8", "UTF8-MAC", null /*To_UTF8_MAC*/);
        TranscoderDB.declare("UTF8-MAC", "UTF-8", "From_UTF8_MAC");
        TranscoderDB.declare("UTF-16BE", "UTF-8", "From_UTF_16BE");
        TranscoderDB.declare("UTF-8", "UTF-16BE", "To_UTF_16BE");
        TranscoderDB.declare("UTF-16LE", "UTF-8", "From_UTF_16LE");
        TranscoderDB.declare("UTF-8", "UTF-16LE", "To_UTF_16LE");
        TranscoderDB.declare("UTF-32BE", "UTF-8", "From_UTF_32BE");
        TranscoderDB.declare("UTF-8", "UTF-32BE", "To_UTF_32BE");
        TranscoderDB.declare("UTF-32LE", "UTF-8", "From_UTF_32LE");
        TranscoderDB.declare("UTF-8", "UTF-32LE", "To_UTF_32LE");
        TranscoderDB.declare("UTF-16", "UTF-8", "From_UTF_16");
        TranscoderDB.declare("UTF-32", "UTF-8", "From_UTF_32");
        TranscoderDB.declare("UTF-8", "UTF-16", "To_UTF_16");
        TranscoderDB.declare("UTF-8", "UTF-32", "To_UTF_32");
    };

    static final GenericTranscoderEntry[] GENERIC_LIST = new GenericTranscoderEntry[] {
        new GenericTranscoderEntry("Big5", "UTF-8", 57100, "Big5", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "Big5", 120168, "Big5", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP950", "UTF-8", 144368, "Big5", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP950", 172296, "Big5", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("Big5-HKSCS", "UTF-8", 195416, "Big5", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "Big5-HKSCS", 288228, "Big5", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP951", "UTF-8", 309868, "Big5", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP951", 325724, "Big5", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("Big5-UAO", "UTF-8", 351812, "Big5", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "Big5-UAO", 436940, "Big5", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("GB2312", "UTF-8", 31136, "Chinese", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("GB12345", "UTF-8", 59848, "Chinese", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "GB2312", 96820, "Chinese", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "GB12345", 130816, "Chinese", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM037", "ISO-8859-1", 948, "Ebcdic", 1, 1, 1, AsciiCompatibility.DECODER, 0),
        new GenericTranscoderEntry("ISO-8859-1", "IBM037", 1904, "Ebcdic", 1, 1, 1, AsciiCompatibility.ENCODER, 0),
        new GenericTranscoderEntry("UTF8-DoCoMo", "UTF8-KDDI", 1176, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-DoCoMo", "UTF8-SoftBank", 2148, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-DoCoMo", "UTF-8", 2616, "Emoji", 1, 4, 4, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-KDDI", "UTF8-DoCoMo", 5684, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-KDDI", "UTF8-SoftBank", 9996, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-KDDI", "UTF-8", 11544, "Emoji", 1, 4, 8, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-SoftBank", "UTF8-DoCoMo", 12784, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-SoftBank", "UTF8-KDDI", 14812, "Emoji", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-SoftBank", "UTF-8", 15460, "Emoji", 1, 4, 8, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "UTF8-DoCoMo", 17620, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "UTF8-KDDI", 21120, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "UTF8-SoftBank", 24060, "Emoji", 1, 4, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("stateless-ISO-2022-JP-KDDI", "UTF8-KDDI", 33996, "EmojiIso2022Kddi", 1, 3, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-KDDI", "stateless-ISO-2022-JP-KDDI", 76404, "EmojiIso2022Kddi", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("SJIS-DOCOMO", "UTF8-DOCOMO", 39616, "EmojiSjisDocomo", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-DOCOMO", "SJIS-DOCOMO", 84704, "EmojiSjisDocomo", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("SJIS-KDDI", "UTF8-KDDI", 39616, "EmojiSjisKddi", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-KDDI", "SJIS-KDDI", 88668, "EmojiSjisKddi", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("SJIS-SoftBank", "UTF8-SoftBank", 39616, "EmojiSjisSoftbank", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF8-SoftBank", "SJIS-SoftBank", 84704, "EmojiSjisSoftbank", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("", "amp_escape", 8, "Escape", 1, 1, 5, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("", "xml_text_escape", 32, "Escape", 1, 1, 5, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("", "xml_attr_content_escape", 60, "Escape", 1, 1, 6, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("GBK", "UTF-8", 89284, "Gbk", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "GBK", 182912, "Gbk", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("EUC-JP", "UTF-8", 54488, "JapaneseEuc", 1, 3, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("eucJP-ms", "UTF-8", 64480, "JapaneseEuc", 1, 3, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP51932", "UTF-8", 66380, "JapaneseEuc", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("EUC-JIS-2004", "UTF-8", 86460, "JapaneseEuc", 1, 3, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "EUC-JP", 145860, "JapaneseEuc", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "eucJP-ms", 164832, "JapaneseEuc", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP51932", 199520, "JapaneseEuc", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "EUC-JIS-2004", 257272, "JapaneseEuc", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("Shift_JIS", "UTF-8", 28448, "JapaneseSjis", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("Windows-31J", "UTF-8", 40648, "JapaneseSjis", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "Shift_JIS", 75704, "JapaneseSjis", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "Windows-31J", 104500, "JapaneseSjis", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "EUC-KR", 44112, "Korean", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("EUC-KR", "UTF-8", 78500, "Korean", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP949", 126176, "Korean", 1, 4, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP949", "UTF-8", 196644, "Korean", 1, 2, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("", "crlf_newline", 20, "Newline", 1, 1, 2, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("", "cr_newline", 36, "Newline", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("US-ASCII", "UTF-8", 8, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "US-ASCII", 132, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ASCII-8BIT", "UTF-8", 148, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ASCII-8BIT", 132, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-1", "UTF-8", 672, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-1", 1260, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-2", "UTF-8", 1784, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-2", 2440, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-3", "UTF-8", 2940, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-3", 3568, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-4", "UTF-8", 4092, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-4", 4748, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-5", "UTF-8", 5272, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-5", 5928, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-6", "UTF-8", 6276, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-6", 6712, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-7", "UTF-8", 7228, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-7", 7912, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-8", "UTF-8", 8296, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-8", 8808, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-9", "UTF-8", 9332, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-9", 9696, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-10", "UTF-8", 10220, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-10", 10896, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-11", "UTF-8", 11392, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-11", 11992, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-13", "UTF-8", 12516, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-13", 13192, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-14", "UTF-8", 13716, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-14", 14452, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-15", "UTF-8", 14976, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-15", 15360, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("ISO-8859-16", "UTF-8", 15884, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "ISO-8859-16", 16580, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-874", "UTF-8", 16984, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-874", 17148, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1250", "UTF-8", 17656, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1250", 18212, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1251", "UTF-8", 18736, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1251", 19372, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1252", "UTF-8", 19880, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1252", 20180, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1253", "UTF-8", 20640, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1253", 21152, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1254", "UTF-8", 21652, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1254", 21768, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1255", "UTF-8", 22208, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1255", 22984, "SingleByte", 1, 4, 3, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1256", "UTF-8", 23508, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1256", 24252, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("WINDOWS-1257", "UTF-8", 24732, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "WINDOWS-1257", 24952, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM437", "UTF-8", 25476, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM437", 26312, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM775", "UTF-8", 26836, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM775", 27480, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM737", "UTF-8", 28004, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM737", 28516, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM852", "UTF-8", 29040, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM852", 29656, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM855", "UTF-8", 30180, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM855", 30732, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM857", "UTF-8", 31248, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM857", 31760, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM860", "UTF-8", 32284, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM860", 32672, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM861", "UTF-8", 33196, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM861", 33508, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM862", "UTF-8", 34032, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM862", 34276, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM863", "UTF-8", 34800, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM863", 35180, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM865", "UTF-8", 35704, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM865", 36016, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM866", "UTF-8", 36540, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM866", 36996, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("IBM869", "UTF-8", 37488, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "IBM869", 38004, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACCROATIAN", "UTF-8", 38528, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACCROATIAN", 39360, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACCYRILLIC", "UTF-8", 39884, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACCYRILLIC", 40588, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACGREEK", "UTF-8", 41112, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACGREEK", 41812, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACICELAND", "UTF-8", 42336, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACICELAND", 43052, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACROMAN", "UTF-8", 43576, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACROMAN", 44060, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACROMANIA", "UTF-8", 44584, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACROMANIA", 44960, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACTURKISH", "UTF-8", 45480, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACTURKISH", 45836, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("MACUKRAINE", "UTF-8", 46360, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "MACUKRAINE", 46584, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("KOI8-U", "UTF-8", 47108, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "KOI8-U", 47892, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("KOI8-R", "UTF-8", 48416, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "KOI8-R", 48948, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("TIS-620", "UTF-8", 49312, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "TIS-620", 49356, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP850", "UTF-8", 49880, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP850", 50428, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP852", "UTF-8", 29040, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP852", 29656, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("CP855", "UTF-8", 30180, "SingleByte", 1, 1, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "CP855", 30732, "SingleByte", 1, 4, 1, AsciiCompatibility.CONVERTER, 0),
        new GenericTranscoderEntry("UTF-8", "UTF8-MAC", 52420, "Utf8Mac", 1, 4, 9, AsciiCompatibility.CONVERTER, 0)
    };

    public static Transcoder getInstance(String name) {
        switch (name) {
            case "Iso2022jp_kddi_decoder": return Iso2022jp_kddi_decoder_Transcoder.INSTANCE;
            case "Iso2022jp_kddi_encoder": return Iso2022jp_kddi_encoder_Transcoder.INSTANCE;
            case "Escape_xml_attr_quote": return Escape_xml_attr_quote_Transcoder.INSTANCE;
            case "From_GB18030": return From_GB18030_Transcoder.INSTANCE;
            case "To_GB18030": return To_GB18030_Transcoder.INSTANCE;
            case "Iso2022jp_decoder": return Iso2022jp_decoder_Transcoder.INSTANCE;
            case "Iso2022jp_encoder": return Iso2022jp_encoder_Transcoder.INSTANCE;
            case "Stateless_iso2022jp_to_eucjp": return Stateless_iso2022jp_to_eucjp_Transcoder.INSTANCE;
            case "Eucjp_to_stateless_iso2022jp": return Eucjp_to_stateless_iso2022jp_Transcoder.INSTANCE;
            case "Cp50220_decoder": return Cp50220_decoder_Transcoder.INSTANCE;
            case "Cp50221_decoder": return Cp50221_decoder_Transcoder.INSTANCE;
            case "Cp50221_encoder": return Cp50221_encoder_Transcoder.INSTANCE;
            case "Cp50220_encoder": return Cp50220_encoder_Transcoder.INSTANCE;
            case "Eucjp2sjis": return Eucjp2sjis_Transcoder.INSTANCE;
            case "Sjis2eucjp": return Sjis2eucjp_Transcoder.INSTANCE;
            case "Universal_newline": return Universal_newline_Transcoder.INSTANCE;
            case "From_UTF8_MAC": return From_UTF8_MAC_Transcoder.INSTANCE;
            case "From_UTF_16BE": return From_UTF_16BE_Transcoder.INSTANCE;
            case "To_UTF_16BE": return To_UTF_16BE_Transcoder.INSTANCE;
            case "From_UTF_16LE": return From_UTF_16LE_Transcoder.INSTANCE;
            case "To_UTF_16LE": return To_UTF_16LE_Transcoder.INSTANCE;
            case "From_UTF_32BE": return From_UTF_32BE_Transcoder.INSTANCE;
            case "To_UTF_32BE": return To_UTF_32BE_Transcoder.INSTANCE;
            case "From_UTF_32LE": return From_UTF_32LE_Transcoder.INSTANCE;
            case "To_UTF_32LE": return To_UTF_32LE_Transcoder.INSTANCE;
            case "From_UTF_16": return From_UTF_16_Transcoder.INSTANCE;
            case "From_UTF_32": return From_UTF_32_Transcoder.INSTANCE;
            case "To_UTF_16": return To_UTF_16_Transcoder.INSTANCE;
            case "To_UTF_32": return To_UTF_32_Transcoder.INSTANCE;
            default: return Transcoder.load(name);
        }
    }
}
