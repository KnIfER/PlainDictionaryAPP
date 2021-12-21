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

import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.PosixBracket;
import org.jcodings.exception.CharacterPropertyException;
import org.jcodings.exception.EncodingError;

abstract class AbstractEncoding extends Encoding {

    private final short CTypeTable[];

    protected AbstractEncoding(String name, int minLength, int maxLength, short[]CTypeTable) {
        super(name, minLength, maxLength);
        this.CTypeTable = CTypeTable;
    }

    /** CTYPE_TO_BIT
     */
    private static int CTypeToBit(int ctype) {
        return 1 << ctype;
    }

    /** ONIGENC_IS_XXXXXX_CODE_CTYPE
     */
    protected final boolean isCodeCTypeInternal(int code, int ctype) {
        return (CTypeTable[code] & CTypeToBit(ctype)) != 0;
    }

    /** onigenc_is_mbc_newline_0x0a / used also by multibyte encodings
     *
     */
    @Override
    public boolean isNewLine(byte[]bytes, int p, int end) {
        return p < end ? bytes[p] == Encoding.NEW_LINE : false;
    }

    protected final int asciiMbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        lower[0] = AsciiTables.ToLowerCaseTable[bytes[pp.value] & 0xff];
        pp.value++;
        return 1;
    }

    /** onigenc_ascii_mbc_case_fold
     */
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return asciiMbcCaseFold(flag, bytes, pp, end, lower);
    }

    protected final void asciiApplyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        int[]code = new int[]{0};

        for (int i=0; i<AsciiTables.LowerMap.length; i++) {
            code[0] = AsciiTables.LowerMap[i][1];
            fun.apply(AsciiTables.LowerMap[i][0], code, 1, arg);

            code[0] = AsciiTables.LowerMap[i][0];
            fun.apply(AsciiTables.LowerMap[i][1], code, 1, arg);
        }
    }

    /** onigenc_ascii_apply_all_case_fold / used also by multibyte encodings
     */
    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        asciiApplyAllCaseFold(flag, fun, arg);
    }

    protected final CaseFoldCodeItem[]asciiCaseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        int b = bytes[p] & 0xff;

        if (0x41 <= b && b <= 0x5a) {
            return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(1, b + 0x20)};
        } else if (0x61 <= b && b <= 0x7a) {
            return new CaseFoldCodeItem[]{CaseFoldCodeItem.create(1, b - 0x20)};
        } else {
            return CaseFoldCodeItem.EMPTY_FOLD_CODES;
        }
    }

    /** onigenc_ascii_get_case_fold_codes_by_str / used also by multibyte encodings
     */
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        return asciiCaseFoldCodesByString(flag, bytes, p, end);
    }

    /** onigenc_ascii_only_case_map / onigenc_single_byte_ascii_only_case_map
     */
    int asciiOnlyCaseMap(IntHolder flagP, byte[]bytes, IntHolder pp, int end, byte[]to, int toP, int toEnd) {
        int toStart = toP;
        int flags = flagP.value;

        while (pp.value < end && toP < toEnd) {
            // specialize for singlebyte ?
            int length = length(bytes, pp.value, end);
            if (length < 0) return length;
            int code = mbcToCode(bytes, pp.value, end);
            pp.value += length;

            if (code >= 'a' && code <= 'z' && ((flags & Config.CASE_UPCASE) != 0)) {
                flags |= Config.CASE_MODIFIED;
                code += 'A' - 'a';
            } else if (code >= 'A' && code <= 'Z' && ((flags & (Config.CASE_DOWNCASE | Config.CASE_FOLD)) != 0)) {
                flags |= Config.CASE_MODIFIED;
                code += 'a' - 'A';
            }
            toP += codeToMbc(code, to, toP);
            if ((flags & Config.CASE_TITLECASE) != 0) {
                flags ^= (Config.CASE_UPCASE | Config.CASE_DOWNCASE | Config.CASE_TITLECASE);
            }
        }
        flagP.value = flags;
        return toP - toStart;
    }

    int singleByteAsciiOnlyCaseMap(IntHolder flagP, byte[]bytes, IntHolder pp, int end, byte[]to, int toP, int toEnd) {
        int toStart = toP;
        int flags = flagP.value;

        while (pp.value < end && toP < toEnd) {
            int code = bytes[pp.value++] & 0xff;

            if (code >= 'a' && code <= 'z' && ((flags & Config.CASE_UPCASE) != 0)) {
                flags |= Config.CASE_MODIFIED;
                code += 'A' - 'a';
            } else if (code >= 'A' && code <= 'Z' && ((flags & (Config.CASE_DOWNCASE | Config.CASE_FOLD)) != 0)) {
                flags |= Config.CASE_MODIFIED;
                code += 'a' - 'A';
            }
            to[toP++] = (byte)code;
            if ((flags & Config.CASE_TITLECASE) != 0) {
                flags ^= (Config.CASE_UPCASE | Config.CASE_DOWNCASE | Config.CASE_TITLECASE);
            }
        }
        flagP.value = flags;
        return toP - toStart;
    }

    /** onigenc_minimum_property_name_to_ctype
     *  notably overridden by unicode encodings
     */
    @Override
    public int propertyNameToCType(byte[]bytes, int p, int end) {
        Integer ctype = PosixBracket.PBSTableUpper.get(bytes, p, end);
        if (ctype != null) return ctype;
        throw new CharacterPropertyException(EncodingError.ERR_INVALID_CHAR_PROPERTY_NAME, bytes, p, end - p);
    }
}
