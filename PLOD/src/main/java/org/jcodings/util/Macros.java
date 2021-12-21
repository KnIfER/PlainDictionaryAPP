package org.jcodings.util;

/**
 * ONIGENC macros from Ruby
 */
public class Macros {
    public static final int MBCLEN_INVALID = -1;

    // CONSTRUCT_MBCLEN_INVALID, ONIGENC_CONSTRUCT_MBCLEN_INVALID
    public static int CONSTRUCT_MBCLEN_INVALID() {
        return MBCLEN_INVALID;
    }

    // MBCLEN_NEEDMORE_P, ONIGENC_MBCLEN_NEEDMORE_P
    public static boolean MBCLEN_NEEDMORE_P(int r) {
        return r < -1;
    }

    // CONSTRUCT_MBCLEN_NEEDMORE, CONSTRUCT_ONIGENC_MBCLEN_NEEDMORE
    public static int CONSTRUCT_MBCLEN_NEEDMORE(int n) {
        return -1 - n;
    }

    // MBCLEN_NEEDMORE_LEN, ONIGENC_MBCLEN_NEEDMORE_LEN
    public static int MBCLEN_NEEDMORE_LEN(int r) {
        return -1 - r;
    }

    // MBCLEN_INVALID_P, ONIGENC_MBCLEN_INVALID_P
    public static boolean MBCLEN_INVALID_P(int r) {
        return r == MBCLEN_INVALID;
    }

    // MBCLEN_CHARFOUND_LEN, ONIGENC_MBCLEN_CHARFOUND_LEN
    public static int MBCLEN_CHARFOUND_LEN(int r) {
        return r;
    }

    // MBCLEN_CHARFOUND_P, ONIGENC_MBCLEN_CHARFOUND_P
    public static boolean MBCLEN_CHARFOUND_P(int r) {
        return 0 < r;
    }

    // CONSTRUCT_MBCLEN_CHARFOUND, ONIGENC_CONSTRUCT_MBCLEN_CHARFOUND
    public static int CONSTRUCT_MBCLEN_CHARFOUND(int n) {
        return n;
    }

    // UNICODE_VALID_CODEPOINT_P
    public static boolean UNICODE_VALID_CODEPOINT_P(int c) {
        return ((c) <= 0x10ffff) &&
            !((c) < 0x10000 && UTF16_IS_SURROGATE((c) >> 8));
    }

    // UTF16_IS_SURROGATE_FIRST
    public static boolean UTF16_IS_SURROGATE_FIRST(int c)  {
        return ((c) & 0xfc) == 0xd8;
    }

     // UTF16_IS_SURROGATE_SECOND
     public static boolean UTF16_IS_SURROGATE_SECOND(int c) {
        return ((c) & 0xfc) == 0xdc;
     }

     // UTF16_IS_SURROGATE
     public static boolean UTF16_IS_SURROGATE(int c) {
        return ((c) & 0xf8) == 0xd8;
     }
}
