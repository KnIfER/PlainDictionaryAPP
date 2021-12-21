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
package org.jcodings.unicode;

import java.io.DataInputStream;
import java.io.IOException;

import org.jcodings.ApplyAllCaseFoldFunction;
import org.jcodings.CaseFoldCodeItem;
import org.jcodings.CodeRange;
import org.jcodings.Config;
import org.jcodings.IntHolder;
import org.jcodings.MultiByteEncoding;
import org.jcodings.constants.CharacterType;
import org.jcodings.exception.CharacterPropertyException;
import org.jcodings.exception.EncodingError;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.util.ArrayReader;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.IntArrayHash;
import org.jcodings.util.IntHash;

public abstract class UnicodeEncoding extends MultiByteEncoding {
    private static final int PROPERTY_NAME_MAX_SIZE = UnicodeCodeRange.MAX_WORD_LENGTH + 1;
    static final int I_WITH_DOT_ABOVE = 0x0130;
    static final int DOTLESS_i = 0x0131;
    static final int DOT_ABOVE = 0x0307;

    protected UnicodeEncoding(String name, int minLength, int maxLength, int[]EncLen, int[][]Trans) {
        // ASCII type tables for all Unicode encodings
        super(name, minLength, maxLength, EncLen, Trans, UNICODE_ISO_8859_1_CTypeTable);
        isUnicode = true;
    }

    protected UnicodeEncoding(String name, int minLength, int maxLength, int[]EncLen) {
        this(name, minLength, maxLength, EncLen, null);
    }

    @Override
    public String getCharsetName() {
        return new String(getName());
    }

    // onigenc_unicode_is_code_ctype
    @Override
    public boolean isCodeCType(int code, int ctype) {
        if (Config.USE_UNICODE_PROPERTIES) {
            if (ctype <= CharacterType.MAX_STD_CTYPE && code < 256)
                return isCodeCTypeInternal(code, ctype);
        } else {
            if (code < 256) return isCodeCTypeInternal(code, ctype);
        }

        if (ctype > UnicodeCodeRange.CodeRangeTable.length) throw new InternalError(ErrorMessages.ERR_TYPE_BUG);

        return CodeRange.isInCodeRange(UnicodeCodeRange.CodeRangeTable[ctype].getRange(), code);

    }

    public static boolean isInCodeRange(UnicodeCodeRange range, int code) {
        return CodeRange.isInCodeRange(range.getRange(), code);
    }

    // onigenc_unicode_ctype_code_range
    protected final int[]ctypeCodeRange(int ctype) {
        if (ctype >= UnicodeCodeRange.CodeRangeTable.length) throw new InternalError(ErrorMessages.ERR_TYPE_BUG);

        return UnicodeCodeRange.CodeRangeTable[ctype].getRange();
    }

    // onigenc_unicode_property_name_to_ctype
    @Override
    public int propertyNameToCType(byte[]name, int p, int end) {
        byte[]buf = new byte[PROPERTY_NAME_MAX_SIZE];
        int len = 0;

        for(int p_ = p; p_ < end; p_+= length(name, p_, end)) {
            int code = mbcToCode(name, p_, end);
            if (code == ' ' || code == '-' || code == '_') continue;
            if (code >= 0x80) throw new CharacterPropertyException(EncodingError.ERR_INVALID_CHAR_PROPERTY_NAME, name, p, end);
            buf[len++] = (byte)code;
            if (len >= PROPERTY_NAME_MAX_SIZE) throw new CharacterPropertyException(EncodingError.ERR_INVALID_CHAR_PROPERTY_NAME, name, p, end);
        }

        Integer ctype = CTypeName.Values.get(buf, 0, len);
        if (ctype == null) throw new CharacterPropertyException(EncodingError.ERR_INVALID_CHAR_PROPERTY_NAME, name, p, end);
        return ctype;
    }

    // onigenc_unicode_mbc_case_fold
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]fold) {
        int p = pp.value;
        int foldP = 0;

        int code = mbcToCode(bytes, p, end);
        int len = length(bytes, p, end);
        pp.value += len;

        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
            if ((flag & Config.CASE_FOLD_TURKISH_AZERI) != 0) {
                if (code == 'I') {
                    return codeToMbc(DOTLESS_i, fold, foldP);
                } else if (code == I_WITH_DOT_ABOVE) {
                    return codeToMbc('i', fold, foldP);
                }
            }
        }

        CodeList to = CaseFold.Values.get(code);
        if (to != null) {
            if (to.codes.length == 1) {
                return codeToMbc(to.codes[0], fold, foldP);
            } else {
                int rlen = 0;
                for (int i=0; i<to.codes.length; i++) {
                    len = codeToMbc(to.codes[i], fold, foldP);
                    foldP += len;
                    rlen += len;
                }
                return rlen;
            }
        }

        for (int i=0; i<len; i++) {
            fold[foldP++] = bytes[p++];
        }
        return len;
    }

    // onigenc_unicode_apply_all_case_fold
    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        /* if (CaseFoldInited == 0) init_case_fold_table(); */

        int[]code = new int[]{0};
        for (int i=0; i<CaseUnfold11.From.length; i++) {
            int from = CaseUnfold11.From[i];
            CodeList to = CaseUnfold11.To[i];

            for (int j=0; j<to.codes.length; j++) {
                code[0] = from;
                fun.apply(to.codes[j], code, 1, arg);

                code[0] = to.codes[j];
                fun.apply(from, code, 1, arg);

                for (int k=0; k<j; k++) {
                    code[0] = to.codes[k];
                    fun.apply(to.codes[j], code, 1, arg);

                    code[0] = to.codes[j];
                    fun.apply(to.codes[k], code, 1, arg);
                }

            }
        }

        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI && (flag & Config.CASE_FOLD_TURKISH_AZERI) != 0) {
            code[0] = DOTLESS_i;
            fun.apply('I', code, 1, arg);
            code[0] = 'I';
            fun.apply(DOTLESS_i, code, 1, arg);
            code[0] = I_WITH_DOT_ABOVE;
            fun.apply('i', code, 1, arg);
            code[0] = 'i';
            fun.apply(I_WITH_DOT_ABOVE, code, 1, arg);
        } else {
            for (int i=0; i<CaseUnfold11.Locale_From.length; i++) {
                int from = CaseUnfold11.Locale_From[i];
                CodeList to = CaseUnfold11.Locale_To[i];

                for (int j=0; j<to.codes.length; j++) {
                    code[0] = from;
                    fun.apply(to.codes[j], code, 1, arg);

                    code[0] = to.codes[j];
                    fun.apply(from, code, 1, arg);

                    for (int k = 0; k<j; k++) {
                        code[0] = to.codes[k];
                        fun.apply(to.codes[j], code, 1, arg);

                        code[0] = to.codes[j];
                        fun.apply(to.codes[k], code, 1, arg);
                    }
                }
            }
        } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI

        if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
            for (int i=0; i<CaseUnfold12.From.length; i++) {
                int[]from = CaseUnfold12.From[i];
                CodeList to = CaseUnfold12.To[i];
                for (int j=0; j<to.codes.length; j++) {
                    fun.apply(to.codes[j], from, 2, arg);

                    for (int k=0; k<to.codes.length; k++) {
                        if (k == j) continue;
                        code[0] = to.codes[k];
                        fun.apply(to.codes[j], code, 1, arg);
                    }
                }
            }

            if (!Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI || (flag & Config.CASE_FOLD_TURKISH_AZERI) == 0) {
                for (int i=0; i<CaseUnfold12.Locale_From.length; i++) {
                    int[]from = CaseUnfold12.Locale_From[i];
                    CodeList to = CaseUnfold12.Locale_To[i];
                    for (int j=0; j<to.codes.length; j++) {
                        fun.apply(to.codes[j], from, 2, arg);

                        for (int k=0; k<to.codes.length; k++) {
                            if (k == j) continue;
                            code[0] = to.codes[k];
                            fun.apply(to.codes[j], code, 1, arg);
                        }
                    }
                }
            } // !USE_UNICODE_CASE_FOLD_TURKISH_AZERI

            for (int i=0; i<CaseUnfold13.From.length; i++) {
                int[]from = CaseUnfold13.From[i];
                CodeList to = CaseUnfold13.To[i];

                for (int j=0; j<to.codes.length; j++) {
                    fun.apply(to.codes[j], from, 3, arg); //// ????

                    for (int k=0; k<to.codes.length; k++) {
                        if (k == j) continue;
                        code[0] = to.codes[k];
                        fun.apply(to.codes[j], code, 1, arg);
                    }
                }
            }

        } // INTERNAL_ENC_CASE_FOLD_MULTI_CHAR
    }

    // onigenc_unicode_get_case_fold_codes_by_str
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        int code = mbcToCode(bytes, p, end);
        int len = length(bytes, p, end);

        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
            if ((flag & Config.CASE_FOLD_TURKISH_AZERI) != 0) {
                if (code == 'I') {
                    return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(len, DOTLESS_i)};
                } else if(code == I_WITH_DOT_ABOVE) {
                    return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(len, 'i')};
                } else if(code == DOTLESS_i) {
                    return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(len, 'I')};
                } else if(code == 'i') {
                    return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(len, I_WITH_DOT_ABOVE)};
                }
            }
        } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI

        int n = 0;
        int fn = 0;
        CodeList to = CaseFold.Values.get(code);
        CaseFoldCodeItem[]items = null;
        if (to != null) {
            items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];

            if (to.codes.length == 1) {
                int origCode = code;

                items[0] = CaseFoldCodeItem.create(len, to.codes[0]);
                n++;

                code = to.codes[0];
                to = CaseUnfold11.Values.get(code);

                if (to != null) {
                    for (int i=0; i<to.codes.length; i++) {
                        if (to.codes[i] != origCode) {
                            items[n] = CaseFoldCodeItem.create(len, to.codes[i]);
                            n++;
                        }
                    }
                }
            } else if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
                int[][]cs = new int[3][4];
                int[]ncs = new int[3];

                for (fn=0; fn<to.codes.length; fn++) {
                    cs[fn][0] = to.codes[fn];
                    CodeList z3 = CaseUnfold11.Values.get(cs[fn][0]);
                    if (z3 != null) {
                        for (int i=0; i<z3.codes.length; i++) {
                            cs[fn][i+1] = z3.codes[i];
                        }
                        ncs[fn] = z3.codes.length + 1;
                    } else {
                        ncs[fn] = 1;
                    }
                }

                if (fn == 2) {
                    for (int i=0; i<ncs[0]; i++) {
                        for (int j=0; j<ncs[1]; j++) {
                            items[n] = CaseFoldCodeItem.create(len, cs[0][i], cs[1][j]);
                            n++;
                        }
                    }

                    CodeList z2 = CaseUnfold12.Values.get(to.codes);
                    if (z2 != null) {
                        for (int i=0; i<z2.codes.length; i++) {
                            if (z2.codes[i] == code) continue;
                            items[n] = CaseFoldCodeItem.create(len, z2.codes[i]);
                            n++;
                        }
                    }
                } else {
                    for (int i=0; i<ncs[0]; i++) {
                        for (int j=0; j<ncs[1]; j++) {
                            for (int k=0; k<ncs[2]; k++) {
                                items[n] = CaseFoldCodeItem.create(len, cs[0][i], cs[1][j], cs[2][k]);
                                n++;
                            }
                        }
                    }
                    CodeList z2 = CaseUnfold13.Values.get(to.codes);
                    if (z2 != null) {
                        for (int i=0; i<z2.codes.length; i++) {
                            if (z2.codes[i] == code) continue;
                            items[n] = CaseFoldCodeItem.create(len, z2.codes[i]);
                            n++;
                        }
                    }
                }
                /* multi char folded code is not head of another folded multi char */
                flag = 0; /* DISABLE_CASE_FOLD_MULTI_CHAR(flag); */
            }
        } else {
            to = CaseUnfold11.Values.get(code);
            if (to != null) {
                items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];
                for (int i=0; i<to.codes.length; i++) {
                    items[n] = CaseFoldCodeItem.create(len, to.codes[i]);
                    n++;
                }
            }
        }

        if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
            if (items == null) items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];

            p += len;
            if (p < end) {
                final int codes0 = code;
                final int codes1;
                code = mbcToCode(bytes, p, end);
                to = CaseFold.Values.get(code);
                if (to != null && to.codes.length == 1) {
                    codes1 = to.codes[0];
                } else {
                    codes1 = code;
                }

                int clen = length(bytes, p, end);
                len += clen;
                CodeList z2 = CaseUnfold12.Values.get(codes0, codes1);
                if (z2 != null) {
                    for (int i=0; i<z2.codes.length; i++) {
                        items[n] = CaseFoldCodeItem.create(len, z2.codes[i]);
                        n++;
                    }
                }

                p += clen;
                if (p < end) {
                    final int codes2;
                    code = mbcToCode(bytes, p, end);
                    to = CaseFold.Values.get(code);
                    if (to != null && to.codes.length == 1) {
                        codes2 = to.codes[0];
                    } else {
                        codes2 = code;
                    }
                    clen = length(bytes, p, end);
                    len += clen;
                    z2 = CaseUnfold13.Values.get(codes0, codes1, codes2);
                    if (z2 != null) {
                        for (int i=0; i<z2.codes.length; i++) {
                            items[n] = CaseFoldCodeItem.create(len, z2.codes[i]);
                            n++;
                        }
                    }
                }
            }
        }

        if (items == null || n == 0) return CaseFoldCodeItem.EMPTY_FOLD_CODES;
        if (n < items.length) {
            CaseFoldCodeItem [] tmp = new CaseFoldCodeItem[n];
            System.arraycopy(items, 0, tmp, 0, n);
            return tmp;
        } else {
            return items;
        }
    }

    static final int CASE_MAPPING_SLACK = 12;

    @Override
    public final int caseMap(IntHolder flagP, byte[] bytes, IntHolder pp, int end, byte[] to, int toP, int toEnd) {
        int flags = flagP.value;
        int toStart = toP;
        toEnd -= CASE_MAPPING_SLACK;
        flags |= (flags & (Config.CASE_UPCASE | Config.CASE_DOWNCASE)) << Config.CASE_SPECIAL_OFFSET;


        while (pp.value < end && toP <= toEnd) {
            int length = length(bytes, pp.value, end);
            if (length < 0) return length;
            int code = mbcToCode(bytes, pp.value, end);
            pp.value += length;

            if (code <= 'z') {
                if (code >= 'a' && code <= 'z') {
                    if ((flags & Config.CASE_UPCASE) != 0) {
                        flags |= Config.CASE_MODIFIED;
                        if ((flags & Config.CASE_FOLD_TURKISH_AZERI) != 0 && code == 'i') code = I_WITH_DOT_ABOVE; else code += 'A' - 'a';
                    }
                } else if (code >= 'A' && code <= 'Z') {
                    if ((flags & (Config.CASE_DOWNCASE | Config.CASE_FOLD)) != 0) {
                        flags |= Config.CASE_MODIFIED;
                        if ((flags & Config.CASE_FOLD_TURKISH_AZERI) != 0 && code == 'I') code = DOTLESS_i; else code += 'a' - 'A';
                    }
                }
            } else if ((flags & Config.CASE_ASCII_ONLY) == 0 && code >= 0x00B5) {
                CodeList folded;
                if (code == I_WITH_DOT_ABOVE) {
                    if ((flags & (Config.CASE_DOWNCASE | Config.CASE_FOLD)) != 0) {
                        flags |= Config.CASE_MODIFIED;
                        code = 'i';
                        if ((flags & Config.CASE_FOLD_TURKISH_AZERI) == 0) {
                            toP += codeToMbc(code, to, toP);
                            code = DOT_ABOVE;
                        }
                    }
                } else if (code == DOTLESS_i) {
                    if ((flags & Config.CASE_UPCASE) != 0) {
                        flags |= Config.CASE_MODIFIED;
                        code = 'I';
                    }
                } else if ((folded = CaseFold.Values.get(code)) != null) { /* data about character found in CaseFold_Table */
                    if ((flags & Config.CASE_TITLECASE) != 0 && code >= 0x1C90 && code <= 0x1CBF) { /* Georgian MTAVRULI */
                        flags |= Config.CASE_MODIFIED;
                        code += 0x10D0 - 0x1C90;
                    } else if ((flags & Config.CASE_TITLECASE) != 0 && (folded.flags & Config.CASE_IS_TITLECASE) != 0) { /* Titlecase needed, but already Titlecase */
                        /* already Titlecase, no changes needed */
                    } else if ((flags & folded.flags) != 0) {
                        final int[]codes;
                        final int start;
                        final int finish;
                        boolean specialCopy = false;
                        flags |= Config.CASE_MODIFIED;
                        if ((flags & folded.flags & Config.CASE_SPECIALS) != 0) {
                            codes = CaseMappingSpecials.Values;
                            int specialStart = (folded.flags & Config.SpecialIndexMask) >>> Config.SpecialIndexShift;
                            if ((folded.flags & Config.CASE_IS_TITLECASE) != 0) {
                                if ((flags & (Config.CASE_UPCASE | Config.CASE_DOWNCASE)) == (Config.CASE_UPCASE | Config.CASE_DOWNCASE))
                                    specialCopy = true;
                                else
                                    specialStart += extractLength(codes[specialStart]);
                            }
                            if (!specialCopy && (folded.flags & Config.CASE_TITLECASE) != 0) {
                                if ((flags & Config.CASE_TITLECASE) != 0)
                                    specialCopy = true;
                                else
                                    specialStart += extractLength(codes[specialStart]);
                            }
                            if (!specialCopy && (folded.flags & Config.CASE_DOWN_SPECIAL) != 0) {
                                if ((flags & Config.CASE_DOWN_SPECIAL) == 0)
                                    specialStart += extractLength(codes[specialStart]);
                            }
                            start = specialStart;
                            finish = start + extractLength(codes[specialStart]);
                            code =  extractCode(codes[specialStart]);
                        } else {
                            codes = folded.codes;
                            start = 0;
                            finish = folded.codes.length;
                            code = codes[0];
                        }

                        for (int i = start + 1; i < finish; i++) {
                            toP += codeToMbc(code, to, toP);
                            code = codes[i];
                        }
                    }
                } else if ((folded = CaseUnfold11.Values.get(code)) != null) { /* data about character found in CaseUnfold_11_Table */
                    if ((flags & Config.CASE_TITLECASE) != 0 && (folded.flags & Config.CASE_IS_TITLECASE) != 0) { /* Titlecase needed, but already Titlecase */
                        /* already Titlecase, no changes needed */
                    } else if ((flags & folded.flags) != 0) { /* needs and data availability match */
                        flags |= Config.CASE_MODIFIED;
                        code = folded.codes[(flags & folded.flags & Config.CASE_TITLECASE) != 0 ? 1 : 0];
                    }
                }
            }
            toP += codeToMbc(code, to, toP);
            if ((flags & Config.CASE_TITLECASE) != 0) {
                flags ^= (Config.CASE_UPCASE | Config.CASE_DOWNCASE | Config.CASE_TITLECASE | Config.CASE_UP_SPECIAL | Config.CASE_DOWN_SPECIAL);
            }

        } // while
        flagP.value = flags;
        return toP - toStart;
    }
    static final short UNICODE_ISO_8859_1_CTypeTable[] = {
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4008, 0x420c, 0x4209, 0x4208, 0x4208, 0x4208, 0x4008, 0x4008,
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4284, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0,
          0x78b0, 0x78b0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x41a0, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x51a0,
          0x41a0, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x4008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0288, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0284, 0x01a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0,
          0x00a0, 0x00a0, 0x30e2, 0x01a0, 0x00a0, 0x00a8, 0x00a0, 0x00a0,
          0x00a0, 0x00a0, 0x10a0, 0x10a0, 0x00a0, 0x30e2, 0x00a0, 0x01a0,
          0x00a0, 0x10a0, 0x30e2, 0x01a0, 0x10a0, 0x10a0, 0x10a0, 0x01a0,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x00a0,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x00a0,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2
    };

    static class CTypeName {
        private static final CaseInsensitiveBytesHash<Integer> Values = initializeCTypeNameTable();

        private static CaseInsensitiveBytesHash<Integer> initializeCTypeNameTable() {
            CaseInsensitiveBytesHash<Integer> table = new CaseInsensitiveBytesHash<Integer>();
            for (int i = 0; i < UnicodeCodeRange.CodeRangeTable.length; i++) {
                table.putDirect(UnicodeCodeRange.CodeRangeTable[i].name, i);
            }
            return table;
        }
    }

    private static class CodeList {
        CodeList(DataInputStream dis) throws IOException {
            int packed = dis.readInt();
            flags = packed & ~Config.CodePointMask;
            int length = packed & Config.CodePointMask;
            codes = new int[length];
            for (int j = 0; j < length; j++) {
                codes[j] = dis.readInt();
            }
        }
        final int[]codes;
        final int flags;
    }

    private static class CaseFold {
        static IntHash<CodeList> read(String table) {
            try {
                DataInputStream dis = ArrayReader.openStream(table);
                int size = dis.readInt();
                IntHash<CodeList> hash = new IntHash<CodeList>(size);
                for (int i = 0; i < size; i++) {
                    hash.putDirect(dis.readInt(), new CodeList(dis));
                }
                dis.close();
                return hash;
            } catch (IOException iot) {
                throw new RuntimeException(iot);
            }
        }

        static final IntHash<CodeList>Values = read("CaseFold");
    }

    private static class CaseUnfold11 {
        private static final int From[];
        private static final CodeList To[];
        private static final int Locale_From[];
        private static final CodeList Locale_To[];

        static Object[] read(String table) {
            try {
                DataInputStream dis = ArrayReader.openStream(table);
                int size = dis.readInt();
                int[]from = new int[size];
                CodeList[]to = new CodeList[size];
                for (int i = 0; i < size; i++) {
                    from[i] = dis.readInt();
                    to[i] = new CodeList(dis);
                }
                dis.close();
                return new Object[] {from, to};
            } catch (IOException iot) {
                throw new RuntimeException(iot);
            }
        }

        static {
            Object[]unfold;
            unfold = read("CaseUnfold_11");
            From = (int[])unfold[0];
            To = (CodeList[])unfold[1];
            unfold = read("CaseUnfold_11_Locale");
            Locale_From = (int[])unfold[0];
            Locale_To = (CodeList[])unfold[1];
        }

        static IntHash<CodeList> initializeUnfold1Hash() {
            IntHash<CodeList> hash = new IntHash<CodeList>(From.length + Locale_From.length);
            for (int i = 0; i < From.length; i++) {
                hash.putDirect(From[i], To[i]);
            }
            for (int i = 0; i < Locale_From.length; i++) {
                hash.putDirect(Locale_From[i], Locale_To[i]);
            }
            return hash;
        }
        static final IntHash<CodeList> Values = initializeUnfold1Hash();
    }

    private static Object[] readFoldN(int fromSize, String table) {
        try {
            DataInputStream dis = ArrayReader.openStream(table);
            int size = dis.readInt();
            int[][]from = new int[size][];
            CodeList[]to = new CodeList[size];
            for (int i = 0; i < size; i++) {
                from[i] = new int[fromSize];
                for (int j = 0; j < fromSize; j++) {
                    from[i][j] = dis.readInt();
                }
                to[i] = new CodeList(dis);
            }
            dis.close();
            return new Object[] {from, to};
        } catch (IOException iot) {
            throw new RuntimeException(iot);
        }
    }

    private static class CaseUnfold12 {
        private static final int From[][];
        private static final CodeList To[];
        private static final int Locale_From[][];
        private static final CodeList Locale_To[];

        static {
            Object[]unfold;
            unfold = readFoldN(2, "CaseUnfold_12");
            From = (int[][])unfold[0];
            To = (CodeList[])unfold[1];
            unfold = readFoldN(2, "CaseUnfold_12_Locale");
            Locale_From = (int[][])unfold[0];
            Locale_To = (CodeList[])unfold[1];
        }

        private static IntArrayHash<CodeList> initializeUnfold2Hash() {
            IntArrayHash<CodeList> unfold2 = new IntArrayHash<CodeList>(From.length + Locale_From.length);
            for (int i = 0; i < From.length; i++) {
                unfold2.putDirect(From[i], To[i]);
            }
            for (int i = 0; i < Locale_From.length; i++) {
                unfold2.putDirect(Locale_From[i], Locale_To[i]);
            }
            return unfold2;
        }

        static final IntArrayHash<CodeList> Values = initializeUnfold2Hash();
    }

    private static class CaseUnfold13 {
        private static final int From[][];
        private static final CodeList To[];

        static {
            Object[]unfold;
            unfold = readFoldN(3, "CaseUnfold_13");
            From = (int[][])unfold[0];
            To = (CodeList[])unfold[1];
        }

        private static IntArrayHash<CodeList> initializeUnfold3Hash() {
            IntArrayHash<CodeList> unfold3 = new IntArrayHash<CodeList>(From.length);
            for (int i = 0; i < From.length; i++) {
                unfold3.putDirect(From[i], To[i]);
            }
            return unfold3;
        }

        static final IntArrayHash<CodeList> Values = initializeUnfold3Hash();
    }

    private static int extractLength(int packed) {
        return packed >>> Config.SpecialsLengthOffset;
    }

    private static int extractCode(int packed) {
        return packed & ((1 << Config.SpecialsLengthOffset) - 1);
    }

    private static class CaseMappingSpecials {
        static final int[] Values = ArrayReader.readIntArray("CaseMappingSpecials");
    }
}
