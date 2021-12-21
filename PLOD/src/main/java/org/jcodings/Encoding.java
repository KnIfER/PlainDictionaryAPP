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

import java.nio.charset.Charset;

import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.CharacterType;
import org.jcodings.exception.EncodingException;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.BytesHash;

public abstract class Encoding implements Cloneable {
    public static final int CHAR_INVALID = -1;
    private static int count;

    protected final int minLength, maxLength;
    private final boolean isFixedWidth, isSingleByte;
    private boolean isAsciiCompatible;
    protected boolean isUnicode = false, isUTF8 = false;

    private byte[]name;
    private int hashCode;
    private int index;
    private Charset charset = null;
    private boolean isDummy = false;
    private String stringName;

    protected Encoding(String name, int minLength, int maxLength) {
        setName(name);

        this.minLength = minLength;
        this.maxLength = maxLength;
        this.isFixedWidth = minLength == maxLength;
        this.isSingleByte = isFixedWidth && minLength == 1;
        this.index = count++;

        this.isAsciiCompatible = minLength == 1;
    }

    protected final void setName(String name) {
        this.name = name.getBytes();
        this.hashCode = BytesHash.hashCode(this.name, 0, this.name.length);
        this.stringName = name;
    }

    protected final void setName(byte[]name) {
        this.name = name;
        this.hashCode = BytesHash.hashCode(this.name, 0, this.name.length);
        this.stringName = new String(name);
    }

    protected final void setDummy() {
        isDummy = true;
        isAsciiCompatible = false;
    }

    @Override
    public final String toString() {
        return stringName;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    public final int getIndex() {
        return index;
    }

    public final byte[]getName() {
        return name;
    }

    public final boolean isDummy() {
        return isDummy;
    }

    public final boolean isAsciiCompatible() {
        return isAsciiCompatible;
    }

    public final boolean isUnicode() {
        return isUnicode;
    }

    public final boolean isUTF8() {
        return isUTF8;
    }

    /**
     * If this encoding is capable of being represented by a Java Charset
     * then provide it. Otherwise this will raise a CharsetNotFound error via the JDK APIs.
     *
     * To reduce cases like jruby/jruby#4716, we always attempt to find a charset here, and default to using the
     * encoding name which is never null. Either the encoding will exist in the JDK or it will fail hard, rather
     * than propagating a null Charset. Encodings with names different than those found in the JDK can override
     * this getCharsetName to provide that name or getCharset to return the right Charset.
     */
    public Charset getCharset() {
        if (charset == null) {
            charset = Charset.forName(getCharsetName());
        }

        return charset;
    }

    /**
     * The name of the equivalent Java Charset for this encoding.
     *
     * Defaults to the name of the encoding. Subclasses can override this to provide a different name.
     *
     * @return the name of the equivalent Java Charset for this encoding
     */
    public String getCharsetName() {
        return stringName;
    }

    Encoding replicate(byte[]name) {
        try {
            Encoding clone = (Encoding)clone();
            clone.setName(name);
            clone.index = count++;
            return clone;
        } catch (CloneNotSupportedException cnse){
            throw new EncodingException(ErrorMessages.ERR_COULD_NOT_REPLICATE, new String(name));
        }
    }

    /**
     * Returns character length given character head
     * returns <code>1</code> for singlebyte encodings or performs direct length table lookup for multibyte ones.
     *
     * @param   c
     *          Character head
     * Oniguruma equivalent: <code>mbc_enc_len</code>
     *
     * To be deprecated very soon (use length(byte[]bytes, int p, int end) version)
     */
    public abstract int length(byte c);

    /**
     * Returns character length given stream, character position and stream end
     * returns <code>1</code> for singlebyte encodings or performs sanity validations for multibyte ones
     * and returns the character length, missing characters in the stream otherwise
     *
     * @return
     *  0               Never
     *  &gt; 0             Valid character, length returned
     *  -1              Illegal/malformed character
     *  &lt; -1 (-1 - n)   Number of missing bytes for character in p...end range
     *
     * Oniguruma equivalent: <code>mbc_enc_len</code>
     * modified for 1.9 purposes,
     */
    public abstract int length(byte[]bytes, int p, int end);

    /**
     * Returns maximum character byte length that can appear in an encoding
     *
     * Oniguruma equivalent: <code>max_enc_len</code>
     */
    public final int maxLength() {
        return maxLength;
    }

    /* ONIGENC_MBC_MAXLEN_DIST */
    @Deprecated
    public final int maxLengthDistance() {
        return maxLength();
    }

    /**
     * Returns minimum character byte length that can appear in an encoding
     *
     * Oniguruma equivalent: <code>min_enc_len</code>
     */
    public final int minLength() {
        return minLength;
    }

    /**
     * Returns true if <code>bytes[p]</code> is a head of a new line character
     *
     * Oniguruma equivalent: <code>is_mbc_newline</code>
     */
    public abstract boolean isNewLine(byte[]bytes, int p, int end);

    /**
     * Returns code point for a character
     *
     * Oniguruma equivalent: <code>mbc_to_code</code>
     */
    public abstract int mbcToCode(byte[]bytes, int p, int end);

    /**
     * Returns character length given a code point
     *
     * Oniguruma equivalent: <code>code_to_mbclen</code>
     */
    public abstract int codeToMbcLength(int code);

    /**
     * Extracts code point into it's multibyte representation
     *
     * @return character length for the given code point
     *
     * Oniguruma equivalent: <code>code_to_mbc</code>
     */
    public abstract int codeToMbc(int code, byte[]bytes, int p);

    /**
     * Performs case folding for a character at <code>bytes[pp.value]</code>
     *
     * @param   flag    case fold flag
     * @param   pp      an <code>IntHolder</code> that points at character head
     * @param   to      a buffer where to extract case folded character
     *
     * Oniguruma equivalent: <code>mbc_case_fold</code>
     */
    public abstract int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]to);

    /**
     * Returns lower case table if it's safe to use it directly, otherwise <code>null</code>
     * Used for fast case insensitive matching for some singlebyte encodings
     *
     * @return lower case table
     */
    public byte[] toLowerCaseTable() {return null;}

    /**
     * Expand case folds given a character class (used for case insensitive matching)
     *
     * @param   flag    case fold flag
     * @param   fun     case folding functor (look at: <code>ApplyCaseFold</code>)
     * @param   arg     case folding functor argument (look at: <code>ApplyCaseFoldArg</code>)
     *
     * Oniguruma equivalent: <code>apply_all_case_fold</code>
     */
    public abstract void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg);

    /**
     * Expand AST string nodes into their folded alternatives (look at: <code>Analyser.expandCaseFoldString</code>)
     *
     * Oniguruma equivalent: <code>get_case_fold_codes_by_str</code>
     */
    public abstract CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end);

    /**
     * Returns character type given character type name (used when e.g. \p{Alpha})
     *
     * Oniguruma equivalent: <code>property_name_to_ctype</code>
     */
    public abstract int propertyNameToCType(byte[]bytes, int p, int end);

    /**
     * Perform a check whether given code is of given character type (e.g. used by isWord(someByte) and similar methods)
     *
     * @param   code    a code point of a character
     * @param   ctype   a character type to check against
     *
     * Oniguruma equivalent: <code>is_code_ctype</code>
     */
    public abstract boolean isCodeCType(int code, int ctype);

    /**
     * Returns code range for a given character type
     *
     * Oniguruma equivalent: <code>get_ctype_code_range</code>
     */
    public abstract int[]ctypeCodeRange(int ctype, IntHolder sbOut);

    /**
     * Seeks the previous character head in a stream
     *
     * Oniguruma equivalent: <code>left_adjust_char_head</code>
     *
     * @param   bytes   byte stream
     * @param   p       position
     * @param   s       stop
     * @param   end     end
     */
    public abstract int leftAdjustCharHead(byte[]bytes, int p, int s, int end);

    /**
     * Returns true if it's safe to use reversal Boyer-Moore search fail fast algorithm
     *
     * Oniguruma equivalent: <code>is_allowed_reverse_match</code>
     */
    public abstract boolean isReverseMatchAllowed(byte[]bytes, int p, int end);

    /**
     *
     * Oniguruma equivalent: <code>case_map</code>
     */
    public abstract int caseMap(IntHolder flagP, byte[]bytes, IntHolder pp, int end, byte[]to, int toP, int toEnd);

    /* onigenc_get_right_adjust_char_head / ONIGENC_LEFT_ADJUST_CHAR_HEAD */
    public final int rightAdjustCharHead(byte[]bytes, int p, int s, int end) {
        int p_ = leftAdjustCharHead(bytes, p, s, end);
        if (p_ < s) p_ += length(bytes, p_, end);
        return p_;
    }

    /* onigenc_get_right_adjust_char_head_with_prev */
    public final int rightAdjustCharHeadWithPrev(byte[]bytes, int p, int s, int end, IntHolder prev) {
        int p_ = leftAdjustCharHead(bytes, p, s, end);
        if (p_ < s) {
            if (prev != null) prev.value = p_;
            p_ += length(bytes, p_, end);
        } else {
            if (prev != null) prev.value = -1; /* Sorry */
        }
        return p_;
    }

    /* onigenc_get_prev_char_head */
    public final int prevCharHead(byte[]bytes, int p, int s, int end) {
        if (s <= p) return -1; // ??
        return leftAdjustCharHead(bytes, p, s - 1, end);
    }

    /* onigenc_step_back */
    public final int stepBack(byte[]bytes, int p, int s, int end, int n) {
        while (s != -1 && n-- > 0) {
            if (s <= p) return -1;
            s = leftAdjustCharHead(bytes, p, s - 1, end);
        }
        return s;
    }

    /* onigenc_step */
    public final int step(byte[]bytes, int p, int end, int n) {
        int q = p;
        while (n-- > 0) {
            q += length(bytes, q, end);
        }
        return q <= end ? q : -1;
    }

    /* onigenc_strlen */
    public abstract int strLength(byte[]bytes, int p, int end);

    public abstract int strCodeAt(byte[]bytes, int p, int end, int index);

    /* onigenc_strlen_null */
    public final int strLengthNull(byte[]bytes, int p, int end) {
        int n = 0;

        while (true) {
            if (bytes[p] == 0) {
                int len = minLength();

                if (len == 1) return n;
                int q = p + 1;

                while (len > 1) {
                    if (bytes[q] != 0) break;
                    q++;
                    len--;
                }
                if (len == 1) return n;
            }
            p += length(bytes, p, end);
            n++;
        }
    }

    /* onigenc_str_bytelen_null */
    public final int strByteLengthNull(byte[]bytes, int p, int end) {
        int p_, start;
        p_ = start = 0;

        while (true) {
            if (bytes[p_] == 0) {
                int len = minLength();
                if (len == 1) return p_ - start;
                int q = p_ + 1;
                while (len > 1) {
                    if (q >= bytes.length) return p_ - start;
                    if (bytes[q] != 0) break;
                    q++;
                    len--;
                }
                if (len == 1) return p_ - start;
            }
            p_ += length(bytes, p_, end);
        }
    }

    /* onigenc_with_ascii_strncmp */
    public final int strNCmp(byte[]bytes, int p, int end, byte[]ascii, int asciiP, int n) {
        while (n-- > 0) {
            if (p >= end) return ascii[asciiP];
            int c = mbcToCode(bytes, p, end);
            int x = ascii[asciiP] - c;
            if (x != 0) return x;

            asciiP++;
            p += length(bytes, p, end);
        }
        return 0;
    }

    public final boolean isNewLine(int code) {
        return isCodeCType(code, CharacterType.NEWLINE);
    }

    public final boolean isGraph(int code) {
        return isCodeCType(code, CharacterType.GRAPH);
    }

    public final boolean isPrint(int code) {
        return isCodeCType(code, CharacterType.PRINT);
    }

    public final boolean isAlnum(int code) {
        return isCodeCType(code, CharacterType.ALNUM);
    }

    public final boolean isAlpha(int code) {
        return isCodeCType(code, CharacterType.ALPHA);
    }

    public final boolean isLower(int code) {
        return isCodeCType(code, CharacterType.LOWER);
    }

    public final boolean isUpper(int code) {
        return isCodeCType(code, CharacterType.UPPER);
    }

    public final boolean isCntrl(int code) {
        return isCodeCType(code, CharacterType.CNTRL);
    }

    public final boolean isPunct(int code) {
        return isCodeCType(code, CharacterType.PUNCT);
    }

    public final boolean isSpace(int code) {
        return isCodeCType(code, CharacterType.SPACE);
    }

    public final boolean isBlank(int code) {
        return isCodeCType(code, CharacterType.BLANK);
    }

    public final boolean isDigit(int code) {
        return isCodeCType(code, CharacterType.DIGIT);
    }

    public final boolean isXDigit(int code) {
        return isCodeCType(code, CharacterType.XDIGIT);
    }

    public final boolean isWord(int code) {
        return isCodeCType(code, CharacterType.WORD);
    }

    // ONIGENC_IS_MBC_WORD
    public final boolean isMbcWord(byte[]bytes, int p, int end) {
        return isWord(mbcToCode(bytes, p, end));
    }

    // IS_CODE_SB_WORD
    public final boolean isSbWord(int code) {
        return isAscii(code) && isWord(code);
    }

    // ONIGENC_IS_MBC_HEAD
    public final boolean isMbcHead(byte[]bytes, int p, int end) {
        return length(bytes, p, end) != 1;
    }

    public boolean isMbcCrnl(byte[]bytes, int p, int end) {
        return mbcToCode(bytes, p, end) == 13 && isNewLine(bytes, p + length(bytes, p, end), end);
    }

    // ============================================================
    // helpers
    // ============================================================
    public static int digitVal(int code) {
        return code - '0';
    }

    public static int odigitVal(int code) {
        return digitVal(code);
    }

    public final int xdigitVal(int code) {
        if (isDigit(code)) {
            return digitVal(code);
        } else {
            return isUpper(code) ? code - 'A' + 10 : code - 'a' + 10;
        }
    }

    // ONIGENC_IS_MBC_ASCII
    public static boolean isMbcAscii(byte b) {
        return (b & 0xff) < 128; // b > 0 ?
    }

    // ONIGENC_IS_CODE_ASCII
    public static boolean isAscii(int code) {
        return code < 128;
    }

    public static boolean isAscii(byte b) {
        return b >= 0;
    }

    public static byte asciiToLower(int c) {
        return AsciiTables.ToLowerCaseTable[c];
    }

    public static byte asciiToUpper(int c) {
        return AsciiTables.ToUpperCaseTable[c];
    }

    public static boolean isWordGraphPrint(int ctype) {
        return ctype == CharacterType.WORD ||
               ctype == CharacterType.GRAPH ||
               ctype == CharacterType.PRINT;
    }

    @Deprecated
    public final int mbcodeStartPosition() {
        return minLength() > 1 ? 0 : 0x80;
    }

    public final boolean isSingleByte() {
        return isSingleByte;
    }

    public final boolean isFixedWidth() {
        return isFixedWidth;
    }

    public static final byte NEW_LINE = (byte)0x0a;

    public static Encoding load(String name) {
        return load(name, "org.jcodings.specific");
    }

    public static Encoding load(String name, String pkg) {
        String encClassName = pkg + "." + name + "Encoding";
        Class<?> encClass;
        try {
            encClass = Class.forName(encClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new InternalException(ErrorMessages.ERR_ENCODING_CLASS_DEF_NOT_FOUND, encClassName);
        }

        try {
            return (Encoding)encClass.getField("INSTANCE").get(encClass);
        } catch (Exception e2) {
            throw new InternalException(ErrorMessages.ERR_ENCODING_LOAD_ERROR, encClassName);
        }
    }
}
