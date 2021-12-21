package org.jcodings.transcode;

import org.jcodings.transcode.specific.From_UTF8_MAC_Transcoder;

import java.util.Arrays;

/**
 * Created by headius on 3/4/14.
 */
public class TranscodeFunctions {
    public static final int BE = 1;
    public static final int LE = 2;

    public static int funSoToUTF16(byte[] statep, byte[] sBytes, int sStart, int l, byte[] o, int oStart, int osize) {
        int sp = 0;
        if (statep[sp] == 0) {
            o[oStart++] = (byte)0xFE;
            o[oStart++] = (byte)0xFF;
            statep[sp] = (byte)1;
            return 2 + funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, osize);
        }
        return funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, osize);
    }

    public static int funSoToUTF16BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1, s2, s3;

        if ((s0 & 0x80) == 0) {
            o[oStart] = 0x00;
            o[oStart + 1] = (byte)s0;
            return 2;
        } else if ((s0 & 0xE0) == 0xC0) {
            s1 = s[sStart+1] & 0xFF;
            o[oStart] = (byte)((s0 >> 2) & 0x07);
            o[oStart + 1] = (byte)(((s0 & 0x03) << 6) | (s1 & 0x3F));
            return 2;
        } else if ((s0 & 0xF0) == 0xE0) {
            s1 = s[sStart+1] & 0xFF;
            s2 = s[sStart+2] & 0xFF;
            o[oStart] = (byte)((s0 << 4) | ((s1 >> 2) ^ 0x20));
            o[oStart + 1] = (byte)((s1 << 6) | (s2 ^ 0x80));
            return 2;
        } else {
            s1 = s[sStart+1] & 0xFF;
            s2 = s[sStart+2] & 0xFF;
            s3 = s[sStart+3] & 0xFF;
            int w = (((s0 & 0x07) << 2) | ((s1 >> 4) & 0x03)) - 1;
            o[oStart] = (byte)(0xD8 | (w >> 2));
            o[oStart + 1] = (byte)((w << 6) | ((s1 & 0x0F) << 2) | ((s2 >> 4) - 8));
            o[oStart + 2] = (byte)(0xDC | ((s2 >> 2) & 0x03));
            o[oStart + 3] = (byte)((s2 << 6) | (s3 & ~0x80));
            return 4;
        }
    }

    public static int funSoToUTF16LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1;
        if ((s0 & 0x80) == 0) {
            o[oStart + 1] = (byte)0x00;
            o[oStart] = (byte)s0;
            return 2;
        } else if ((s0 & 0xE0) == 0xC0) {
            s1 = s[sStart+1] & 0xFF;
            o[oStart + 1] = (byte)((s0 >> 2) & 0x07);
            o[oStart] = (byte)(((s0 & 0x03) << 6) | (s1 & 0x3F));
            return 2;
        } else if ((s0 & 0xF0) == 0xE0) {
            s1 = s[sStart+1] & 0xFF;
            int s2 = s[sStart+2] & 0xFF;
            o[oStart + 1] = (byte)((s0 << 4) | ((s1 >> 2) ^ 0x20));
            o[oStart] = (byte)((s1 << 6) | (s2 ^ 0x80));
            return 2;
        } else {
            s1 = s[sStart+1] & 0xFF;
            int s2 = s[sStart+2] & 0xFF;
            int s3 = s[sStart+3] & 0xFF;
            int w = (((s0 & 0x07) << 2) | ((s1 >> 4) & 0x03)) - 1;
            o[oStart + 1] = (byte)(0xD8 | (w >> 2));
            o[oStart] = (byte)((w << 6) | ((s1 & 0x0F) << 2) | ((s2 >> 4) - 8));
            o[oStart + 3] = (byte)(0xDC | ((s2 >> 2) & 0x03));
            o[oStart + 2] = (byte)((s2 << 6) | (s3 & ~0x80));
            return 4;
        }
    }

    public static int funSoToUTF32(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int sp = 0;
        if (statep[sp] == 0) {
            o[oStart++] = 0x00;
            o[oStart++] = 0x00;
            o[oStart++] = (byte)0xFE;
            o[oStart++] = (byte)0xFF;
            statep[sp] = 1;
            return 4 + funSoToUTF32BE(statep, s, sStart, l, o, oStart, osize);
        }
        return funSoToUTF32BE(statep, s, sStart, l, o, oStart, osize);
    }

    public static int funSoToUTF32BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1, s2, s3;
        o[oStart] = 0;
        if ((s0 & 0x80) == 0) {
            o[oStart + 1] = o[oStart + 2] = 0x00;
            o[oStart + 3] = (byte)s0;
        } else if ((s0 & 0xE0) == 0xC0) {
            s1 = s[sStart+1] & 0xFF;
            o[oStart + 1] = 0x00;
            o[oStart + 2] = (byte)((s0 >> 2) & 0x07);
            o[oStart + 3] = (byte)(((s0 & 0x03) << 6) | (s1 & 0x3F));
        } else if ((s0 & 0xF0) == 0xE0) {
            s1 = s[sStart+1] & 0xFF;
            s2 = s[sStart+2] & 0xFF;
            o[oStart + 1] = 0x00;
            o[oStart + 2] = (byte)((s0 << 4) | ((s1 >> 2) ^ 0x20));
            o[oStart + 3] = (byte)((s1 << 6) | (s2 ^ 0x80));
        } else {
            s1 = s[sStart+1] & 0xFF;
            s2 = s[sStart+2] & 0xFF;
            s3 = s[sStart+3] & 0xFF;
            o[oStart + 1] = (byte)(((s0 & 0x07) << 2) | ((s1 >> 4) & 0x03));
            o[oStart + 2] = (byte)(((s1 & 0x0F) << 4) | ((s2 >> 2) & 0x0F));
            o[oStart + 3] = (byte)(((s2 & 0x03) << 6) | (s3 & 0x3F));
        }
        return 4;
    }

    public static int funSoToUTF32LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        o[oStart+3] = 0;
        int s0 = s[sStart] & 0xFF;
        if ((s0 & 0x80) == 0) {
            o[oStart + 2] = o[oStart+1] = 0x00;
            o[oStart] = (byte)s0;
        } else if ((s[sStart] & 0xE0) == 0xC0) {
            int s1 = s[sStart+1] & 0xFF;
            o[oStart + 2] = 0x00;
            o[oStart + 1] = (byte)((s0 >> 2) & 0x07);
            o[oStart] = (byte)(((s0 & 0x03) << 6) | (s1 & 0x3F));
        } else if ((s[sStart] & 0xF0) == 0xE0) {
            int s1 = s[sStart+1] & 0xFF;
            int s2 = s[sStart+2] & 0xFF;
            o[oStart + 2] = 0x00;
            o[oStart + 1] = (byte)((s0 << 4) | ((s1 >> 2) ^ 0x20));
            o[oStart] = (byte)((s1 << 6) | (s2 ^ 0x80));
        } else {
            int s1 = s[sStart+1] & 0xFF;
            int s2 = s[sStart+2] & 0xFF;
            int s3 = s[sStart+3] & 0xFF;
            o[oStart + 2] = (byte)(((s0 & 0x07) << 2) | ((s1 >> 4) & 0x03));
            o[oStart + 1] = (byte)(((s1 & 0x0F) << 4) | ((s2 >> 2) & 0x0F));
            o[oStart] = (byte)(((s2 & 0x03) << 6) | (s3 & 0x3F));
        }
        return 4;
    }

    public static int funSiFromUTF32(byte[] statep, byte[] s, int sStart, int l) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        int s2 = s[sStart+2] & 0xFF;
        int s3;
        byte[] sp = statep;

        switch (sp[0]) {
            case 0:
                s3 = s[sStart+3] & 0xFF;
                if (s0 == 0 && s1 == 0 && s2 == 0xFE && s3 == 0xFF) {
                    sp[0] = BE;
                    return TranscodingInstruction.ZERObt;
                } else if (s0 == 0xFF && s1 == 0xFE && s2 == 0 && s3 == 0) {
                    sp[0] = LE;
                    return TranscodingInstruction.ZERObt;
                }
                break;
            case BE:
                if (s0 == 0 && ((0 < s1 && s1 <= 0x10)) ||
                        (s1 == 0 && (s2 < 0xD8 || 0xDF < s2))) {
                    return TranscodingInstruction.FUNso;
                }
                break;
            case LE:
                s3 = s[sStart+3] & 0xFF;
                if (s3 == 0 && ((0 < s2 && s2 <= 0x10) ||
                        (s2 == 0 && (s1 < 0xD8 || 0xDF < s1))))
                    return TranscodingInstruction.FUNso;
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoFromUTF32(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        switch (statep[0]) {
            case BE:
                return funSoFromUTF32BE(statep, s, sStart, l, o, oStart, osize);
            case LE:
                return funSoFromUTF32LE(statep, s, sStart, l, o, oStart, osize);
        }
        return 0;
    }

    public static int funSoFromUTF32BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s1 = s[sStart+1] & 0xFF;
        int s2 = s[sStart+2] & 0xFF;
        int s3 = s[sStart+3] & 0xFF;
        if (s1 == 0) {
            if (s2 == 0 && s3 < 0x80) {
                o[oStart] = (byte)s3;
                return 1;
            } else if (s2 < 0x08) {
                o[oStart] = (byte)(0xC0 | (s2 << 2) | (s3 >> 6));
                o[oStart + 1] = (byte)(0x80 | (s3 & 0x3F));
                return 2;
            } else {
                o[oStart] = (byte)(0xE0 | (s2 >> 4));
                o[oStart + 1] = (byte)(0x80 | ((s2 & 0x0F) << 2) | (s3 >> 6));
                o[oStart + 2] = (byte)(0x80 | (s3 & 0x3F));
                return 3;
            }
        } else {
            o[oStart] = (byte)(0xF0 | (s1 >> 2));
            o[oStart + 1] = (byte)(0x80 | ((s1 & 0x03) << 4) | (s2 >> 4));
            o[oStart + 2] = (byte)(0x80 | ((s2 & 0x0F) << 2) | (s3 >> 6));
            o[oStart + 3] = (byte)(0x80 | (s3 & 0x3F));
            return 4;
        }
    }

    public static int funSoFromUTF32LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        int s2 = s[sStart+2] & 0xFF;
        if (s2 == 0) {
            if (s1 == 0 && s0 < 0x80) {
                o[oStart] = (byte)s0;
                return 1;
            } else if (s1 < 0x08) {
                o[oStart] = (byte)(0xC0 | (s1 << 2) | (s0 >> 6));
                o[oStart + 1] = (byte)(0x80 | (s0 & 0x3F));
                return 2;
            } else {
                o[oStart] = (byte)(0xE0 | (s1 >> 4));
                o[oStart + 1] = (byte)(0x80 | ((s1 & 0x0F) << 2) | (s0 >> 6));
                o[oStart + 2] = (byte)(0x80 | (s0 & 0x3F));
                return 3;
            }
        } else {
            o[oStart] = (byte)(0xF0 | (s2 >> 2));
            o[oStart + 1] = (byte)(0x80 | ((s2 & 0x03) << 4) | (s1 >> 4));
            o[oStart + 2] = (byte)(0x80 | ((s1 & 0x0F) << 2) | (s0 >> 6));
            o[oStart + 3] = (byte)(0x80 | (s0 & 0x3F));
            return 4;
        }
    }

    public static final int from_UTF_16BE_D8toDB_00toFF = Transcoding.WORDINDEX2INFO(39);
    public static final int from_UTF_16LE_00toFF_D8toDB = Transcoding.WORDINDEX2INFO(5);

    public static int funSiFromUTF16(byte[] statep, byte[] s, int sStart, int l) {
        int s0 = s[sStart] & 0xFF;
        int s1;
        byte[] sp = statep;

        switch (sp[0]) {
            case 0:
                s1 = s[sStart+1] & 0xFF;
                if (s0 == 0xFE && s1 == 0xFF) {
                    sp[0] = BE;
                    return TranscodingInstruction.ZERObt;
                } else if (s0 == 0xFF && s1 == 0xFE) {
                    sp[0] = LE;
                    return TranscodingInstruction.ZERObt;
                }
                break;
            case BE:
                if (s0 < 0xD8 || 0xDF < s0) {
                    return TranscodingInstruction.FUNso;
                } else if (s0 <= 0xDB) {
                    return from_UTF_16BE_D8toDB_00toFF;
                }
                break;
            case LE:
                s1 = s[sStart+1] & 0xFF;
                if (s1 < 0xD8 || 0xDF < s1) {
                    return TranscodingInstruction.FUNso;
                } else if (s1 <= 0xDB) {
                    return from_UTF_16LE_00toFF_D8toDB;
                }
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoFromUTF16(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        switch (statep[0]) {
            case BE:
                return funSoFromUTF16BE(statep, s, sStart, l, o, oStart, osize);
            case LE:
                return funSoFromUTF16LE(statep, s, sStart, l, o, oStart, osize);
        }
        return 0;
    }

    public static int funSoFromUTF16BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        int s2, s3;
        if (s0 == 0 && s1 < 0x80) {
            o[oStart] = (byte)s1;
            return 1;
        } else if (s0 < 0x08) {
            o[oStart] = (byte)(0xC0 | (s0 << 2) | (s1 >> 6));
            o[oStart + 1] = (byte)(0x80 | (s1 & 0x3F));
            return 2;
        } else if ((s0 & 0xF8) != 0xD8) {
            o[oStart] = (byte)(0xE0 | (s0 >> 4));
            o[oStart + 1] = (byte)(0x80 | ((s0 & 0x0F) << 2) | (s1 >> 6));
            o[oStart + 2] = (byte)(0x80 | (s1 & 0x3F));
            return 3;
        } else {
            s2 = s[sStart+2] & 0xFF;
            s3 = s[sStart+3] & 0xFF;
            long u = (((s0 & 0x03) << 2) | (s1 >> 6)) + 1;
            o[oStart] = (byte)(0xF0 | (u >> 2));
            o[oStart + 1] = (byte)(0x80 | ((u & 0x03) << 4) | (((s1 >> 2) & 0x0F)));
            o[oStart + 2] = (byte)(0x80 | ((s1 & 0x03) << 4) | ((s2 & 0x03) << 2) | (s3 >> 6));
            o[oStart + 3] = (byte)(0x80 | (s3 & 0x3F));
            return 4;
        }
    }

    public static int funSoFromUTF16LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        if (s1 == 0 && s0 < 0x80) {
            o[oStart] = (byte)s0;
            return 1;
        } else if (s1 < 0x08) {
            o[oStart] = (byte)(0xC0 | (s1 << 2) | (s0 >> 6));
            o[oStart + 1] = (byte)(0x80 | (s0 & 0x3F));
            return 2;
        } else if ((s1 & 0xF8) != 0xD8) {
            o[oStart] = (byte)(0xE0 | (s1 >> 4));
            o[oStart + 1] = (byte)(0x80 | ((s1 & 0x0F) << 2) | (s0 >> 6));
            o[oStart + 2] = (byte)(0x80 | (s0 & 0x3F));
            return 3;
        } else {
            int s2 = s[sStart+2] & 0xFF;
            int s3 = s[sStart+3] & 0xFF;
            long u = (((s1 & 0x03) << 2) | (s0 >> 6)) + 1;
            o[oStart] = (byte)(0xF0 | (u >> 2));
            o[oStart + 1] = (byte)(0x80 | ((u & 0x03) << 4) | ((s0 >> 2) & 0x0F));
            o[oStart + 2] = (byte)(0x80 | ((s0 & 0x03) << 4) | ((s3 & 0x03) << 2) | (s2 >> 6));
            o[oStart + 3] = (byte)(0x80 | (s2 & 0x3F));
            return 4;
        }
    }

    public static int funSoEucjp2Sjis(byte[] statep, byte[] s, int sStart, int _l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        if (s0 == 0x8e) {
            o[oStart] = (byte)s1;
            return 1;
        } else {
            int h, m, l;
            m = s0 & 1;
            h = (s0 + m) >> 1;
            h += s0 < 0xdf ? 0x30 : 0x70;
            l = s1 - m * 94 - 3;
            if (0x7f <= l) {
                l++;
            }
            o[oStart] = (byte)h;
            o[oStart+1] = (byte)l;
            return 2;
        }
    }

    public static int funSoSjis2Eucjp(byte[] statep, byte[] s, int sStart, int _l, byte[] o, int oStart, int osize) {
        int s0 = s[sStart] & 0xFF;
        if (_l == 1) {
            o[oStart] = (byte)0x8E;
            o[oStart+1] = (byte)s0;
            return 2;
        } else {
            int h, l;
            h = s0;
            l = s[sStart + 1] & 0xFF;
            if (0xe0 <= h) {
                h -= 64;
            }
            l += l < 0x80 ? 0x61 : 0x60;
            h = h * 2 - 0x61;
            if (0xfe < l) {
                l -= 94;
                h += 1;
            }
            o[oStart] = (byte)h;
            o[oStart+1] = (byte)l;
            return 2;
        }
    }

    public static int funSoFromGB18030(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize)
    {
        long s0 = s[sStart] & 0xFF;
        long s1 = s[sStart+1] & 0xFF;
        long s2 = s[sStart+2] & 0xFF;
        long s3 = s[sStart+3] & 0xFF;
        long u = ((s0 - 0x90) * 10 * 126 * 10 + (s1 - 0x30) * 126 * 10 + (s2 - 0x81) * 10 + (s3 - 0x30) + 0x10000) & 0xFFFFFFFFL;
        o[oStart] = (byte)(0xF0 | (u >>> 18));
        o[oStart+1] = (byte)(0x80 | ((u >>> 12) & 0x3F));
        o[oStart+2] = (byte)(0x80 | ((u >>> 6) & 0x3F));
        o[oStart+3] = (byte)(0x80 | (u & 0x3F));
        return 4;
    }

    public static int funSioFromGB18030(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int osize)
    {
        long s0 = s[sStart] & 0xFF;
        long s1 = s[sStart+1] & 0xFF;
        long diff = info >> 8;
        long u;    /* Unicode Scalar Value */
        if ((diff & 0x20000) != 0) { /* GB18030 4 bytes */
            long s2 = s[sStart+2] & 0xFF;
            long s3 = s[sStart+3] & 0xFF;
            u = (((s0 * 10 + s1) * 126 + s2) * 10 + s3 - diff - 0x170000) & 0xFFFFFFFFL;
        }
        else { /* GB18030 2 bytes */
            u = (s0 * 256 + s1 + 24055 - diff) & 0xFFFFFFFFL;
        }
        o[oStart] = (byte)(0xE0 | (u >>> 12));
        o[oStart+1] = (byte)(0x80 | ((u >>> 6) & 0x3F));
        o[oStart+2] = (byte)(0x80 | (u & 0x3F));
        return 3;
    }

    public static int funSoToGB18030(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize)
    {
        long s0 = s[sStart] & 0xFF;
        long s1 = s[sStart+1] & 0xFF;
        long s2 = s[sStart+2] & 0xFF;
        long s3 = s[sStart+3] & 0xFF;
        long u = ((s0 & 0x07) << 18) | ((s1 & 0x3F) << 12) | ((s2 & 0x3F) << 6) | (s3 & 0x3F);
        u -= 0x10000;
        o[oStart+3] = (byte)(0x30 + (u % 10));
        u /= 10;
        o[oStart+2] = (byte)(0x81 + (u % 126));
        u /= 126;
        o[oStart+1] = (byte)(0x30 + (u % 10));
        o[oStart] = (byte)(0x90 + u / 10);
        return 4;
    }

    public static int funSioToGB18030(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int osize)
    {
        long s0 = s[sStart] & 0xFF;
        long s1 = s[sStart+1] & 0xFF;
        long s2 = s[sStart+2] & 0xFF;
        long diff = info >>> 8;
        long u;    /* Unicode Scalar Value */

        u = ((s0 & 0x0F) << 12) | ((s1 & 0x3F) << 6) | (s2 & 0x3F);

        if ((diff & 0x20000) != 0) { /* GB18030 4 bytes */
            u += (diff + 0x170000);
            u -= 1688980;
            u += 0x2;
            o[oStart+3] = (byte)(0x30 + (u % 10));
            u /= 10;
            u += 0x32;
            o[oStart+2] = (byte)(0x81 + (u % 126));
            u /= 126;
            u += 0x1;
            o[oStart+1] = (byte)(0x30 + (u % 10));
            u /= 10;
            o[oStart] = (byte)(0x81 + u);
            return 4;
        }
        else { /* GB18030 2 bytes */
            u += (diff - 24055);
            o[oStart+1] = (byte)(u % 256);
            o[oStart] = (byte)(u / 256);
            return 2;
        }
    }

    public static int iso2022jpInit(byte[] state) {
        state[0] = G0_ASCII;
        return 0;
    }

    public static final byte G0_ASCII = 0;
    public static final byte G0_JISX0208_1978 = 1;
    public static final byte G0_JISX0208_1983 = 2;
    public static final byte G0_JISX0201_KATAKANA = 3;

    public static final int EMACS_MULE_LEADING_CODE_JISX0208_1978 = 0220;
    public static final int EMACS_MULE_LEADING_CODE_JISX0208_1983 = 0222;

    public static final byte[] tbl0208 = {
                    (byte)0x21, (byte)0x23, (byte)0x21, (byte)0x56, (byte)0x21, (byte)0x57, (byte)0x21, (byte)0x22, (byte)0x21, (byte)0x26, (byte)0x25, (byte)0x72, (byte)0x25, (byte)0x21, (byte)0x25, (byte)0x23,
                    (byte)0x25, (byte)0x25, (byte)0x25, (byte)0x27, (byte)0x25, (byte)0x29, (byte)0x25, (byte)0x63, (byte)0x25, (byte)0x65, (byte)0x25, (byte)0x67, (byte)0x25, (byte)0x43, (byte)0x21, (byte)0x3C,
                    (byte)0x25, (byte)0x22, (byte)0x25, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x25, (byte)0x28, (byte)0x25, (byte)0x2A, (byte)0x25, (byte)0x2B, (byte)0x25, (byte)0x2D, (byte)0x25, (byte)0x2F,
                    (byte)0x25, (byte)0x31, (byte)0x25, (byte)0x33, (byte)0x25, (byte)0x35, (byte)0x25, (byte)0x37, (byte)0x25, (byte)0x39, (byte)0x25, (byte)0x3B, (byte)0x25, (byte)0x3D, (byte)0x25, (byte)0x3F,
                    (byte)0x25, (byte)0x41, (byte)0x25, (byte)0x44, (byte)0x25, (byte)0x46, (byte)0x25, (byte)0x48, (byte)0x25, (byte)0x4A, (byte)0x25, (byte)0x4B, (byte)0x25, (byte)0x4C, (byte)0x25, (byte)0x4D,
                    (byte)0x25, (byte)0x4E, (byte)0x25, (byte)0x4F, (byte)0x25, (byte)0x52, (byte)0x25, (byte)0x55, (byte)0x25, (byte)0x58, (byte)0x25, (byte)0x5B, (byte)0x25, (byte)0x5E, (byte)0x25, (byte)0x5F,
                    (byte)0x25, (byte)0x60, (byte)0x25, (byte)0x61, (byte)0x25, (byte)0x62, (byte)0x25, (byte)0x64, (byte)0x25, (byte)0x66, (byte)0x25, (byte)0x68, (byte)0x25, (byte)0x69, (byte)0x25, (byte)0x6A,
                    (byte)0x25, (byte)0x6B, (byte)0x25, (byte)0x6C, (byte)0x25, (byte)0x6D, (byte)0x25, (byte)0x6F, (byte)0x25, (byte)0x73, (byte)0x21, (byte)0x2B, (byte)0x21, (byte)0x2C};

    public static int funSoCp50220Encoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0;
        int s1;
        int output0 = oStart;
        byte[] sp = statep;

        if (sp[0] == G0_JISX0201_KATAKANA) {
            int c = sp[2] & 0x7F;
            int p = (c - 0x21) * 2;
            byte[] pBytes = tbl0208;
            if (sp[1] != G0_JISX0208_1983) {
                o[oStart++] = 0x1B;
                o[oStart++] = (byte)'$';
                o[oStart++] = (byte)'B';
            }
            sp[0] = G0_JISX0208_1983;
            o[oStart++] = pBytes[p++];
            s0 = s[sStart] & 0xFF;
            s1 = s[sStart+1] & 0xFF;
            if (l == 2 && s0 == 0x8E) {
                if (s1 == 0xDE) {
                    o[oStart++] = (byte)(pBytes[p] + 1);
                    return oStart - output0;
                } else if (s1 == 0xDF && (0x4A <= c && c <= 0x4E)) {
                    o[oStart++] = (byte)(pBytes[p] + 2);
                    return oStart - output0;
                }
            }
            o[oStart++] = pBytes[p];
        }

        s0 = s[sStart] & 0xFF;
        if (l == 2 && s0 == 0x8E) {
            s1 = s[sStart+1] & 0xFF;
            int p = (s1 - 0xA1) * 2;
            byte[] pBytes = tbl0208;
            if ((0xA1 <= s1 && s1 <= 0xB5) ||
                    (0xC5 <= s1 && s1 <= 0xC9) ||
                    (0xCF <= s1 && s1 <= 0xDF)) {
                if (sp[0] != G0_JISX0208_1983) {
                    o[oStart++] = 0x1b;
                    o[oStart++] = '$';
                    o[oStart++] = 'B';
                    sp[0] = G0_JISX0208_1983;
                }
                o[oStart++] = pBytes[p++];
                o[oStart++] = pBytes[p];
                return oStart - output0;
            }

            sp[2] = (byte)s1;
            sp[1] = sp[0];
            sp[0] = G0_JISX0201_KATAKANA;
            return oStart - output0;
        }

        oStart += funSoCp5022xEncoder(statep, s, sStart, l, o, oStart, oSize);
        return oStart - output0;
    }

    public static int funSoCp5022xEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0, s1;
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1) {
            newstate = G0_ASCII;
        } else if ((s[sStart] & 0xFF) == 0x8E) {
            sStart++;
            l = 1;
            newstate = G0_JISX0201_KATAKANA;
        } else {
            newstate = G0_JISX0208_1983;
        }

        if (sp[0] != newstate) {
            if (newstate == G0_ASCII) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'B';
            }
            else if (newstate == G0_JISX0201_KATAKANA) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'I';
            }
            else {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = (byte)newstate;
        }

        s0 = s[sStart] & 0xFF;
        if (l == 1) {
            o[oStart++] = (byte)(s0 & 0x7f);
        }
        else {
            s1 = s[sStart+1] & 0xFF;
            o[oStart++] = (byte)(s0 & 0x7f);
            o[oStart++] = (byte)(s1 & 0x7f);
        }

        return oStart - output0;
    }

    public static int finishCp50220Encoder(byte[] statep, byte[] o, int oStart, int size) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII) return 0;

        if (sp[0] == G0_JISX0201_KATAKANA) {
            int c = sp[2] & 0x7F;
            int p = (c - 0x21) * 2;
            byte[] pBytes = tbl0208;
            if (sp[1] != G0_JISX0208_1983) {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = G0_JISX0208_1983;
            o[oStart++] = pBytes[p++];
            o[oStart++] = pBytes[p];
        }

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int iso2022jpEncoderResetSequenceSize(byte[] statep) {
        byte[] sp = statep;
        if (sp[0] != G0_ASCII) return 3;
        return 0;
    }

    public static final int iso2022jp_decoder_jisx0208_rest = Transcoding.WORDINDEX2INFO(16);

    public static int funSiIso50220jpDecoder(byte[] statep, byte[] s, int sStart, int l) {
        int s0 = s[sStart] & 0xFF;
        byte[] sp = statep;
        if (sp[0] == G0_ASCII)
        return TranscodingInstruction.NOMAP;
        else if (0x21 <= s0 && s0 <= 0x7e)
            return iso2022jp_decoder_jisx0208_rest;
        else
            return TranscodingInstruction.INVALID;
    }

    public static int funSoIso2022jpDecoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        byte[] sp = statep;
        if (s0 == 0x1b) {
            if (s1 == '(') {
                switch (s[sStart+l-1] & 0xFF) {
                    case 'B':
                    case 'J':
                        sp[0] = G0_ASCII;
                        break;
                }
            }
            else {
                switch (s[sStart+l-1]) {
                    case '@':
                        sp[0] = G0_JISX0208_1978;
                        break;

                    case 'B':
                        sp[0] = G0_JISX0208_1983;
                        break;
                }
            }
            return 0;
        }
        else {
            if (sp[0] == G0_JISX0208_1978) {
                o[oStart] = (byte)EMACS_MULE_LEADING_CODE_JISX0208_1978;
            } else {
                o[oStart] = (byte)EMACS_MULE_LEADING_CODE_JISX0208_1983;
            }
            o[oStart+1] = (byte)(s0 | 0x80);
            o[oStart+2] = (byte)(s1 | 0x80);
            return 3;
        }
    }

    public static int funSoStatelessIso2022jpToEucjp(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        o[oStart] = s[sStart+1];
        o[oStart+1] = s[sStart+2];
        return 2;
    }

    public static int funSoEucjpToStatelessIso2022jp(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        o[oStart] = (byte)EMACS_MULE_LEADING_CODE_JISX0208_1983;
        o[oStart+1] = s[sStart];
        o[oStart+2] = s[sStart+1];
        return 3;
    }

    public static int funSoIso2022jpEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1)
            newstate = G0_ASCII;
        else if ((s[sStart] & 0xFF) == EMACS_MULE_LEADING_CODE_JISX0208_1978)
            newstate = G0_JISX0208_1978;
        else
            newstate = G0_JISX0208_1983;

        if (sp[0] != newstate) {
            if (newstate == G0_ASCII) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'B';
            }
            else if (newstate == G0_JISX0208_1978) {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = '@';
            }
            else {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = (byte)newstate;
        }

        if (l == 1) {
            o[oStart++] = (byte)(s[sStart] & 0x7f);
        }
        else {
            o[oStart++] = (byte)(s[sStart+1] & 0x7f);
            o[oStart++] = (byte)(s[sStart+2] & 0x7f);
        }

        return oStart - output0;
    }

    public static int finishIso2022jpEncoder(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII) return 0;

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int funSiCp50221Decoder(byte[] statep, byte[] s, int sStart, int l) {
        byte[] sp = statep;
        int c;
        int s0 = s[sStart] & 0xFF;
        switch (sp[0]) {
            case G0_ASCII:
                if (0xA1 <= s0 && s0 <= 0xDF)
                    return TranscodingInstruction.FUNso;
                return TranscodingInstruction.NOMAP;
            case G0_JISX0201_KATAKANA:
                c = s0 & 0x7F;
                if (0x21 <= c && c <= 0x5f)
                    return TranscodingInstruction.FUNso;
                break;
            case G0_JISX0208_1978:
                if ((0x21 <= s0 && s0 <= 0x28) || (0x30 <= s0 && s0 <= 0x74))
                    return iso2022jp_decoder_jisx0208_rest;
                break;
            case G0_JISX0208_1983:
                if ((0x21 <= s0 && s0 <= 0x28) ||
                        s0 == 0x2D ||
                        (0x30 <= s0 && s0 <= 0x74) ||
                        (0x79 <= s0 && s0 <= 0x7C)) {
                    /* 0x7F <= s0 && s0 <= 0x92) */
                    return iso2022jp_decoder_jisx0208_rest;
                }
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoCp50221Decoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0 = s[sStart]&0xFF;
        int s1;
        byte[] sp = statep;
        switch (s0) {
            case 0x1b:
                s1 = s[sStart+1]&0xFF;
                if (s1 == '(') {
                    switch ((s[sStart+l-1]&0xFF)) {
                        case 'B':
                        case 'J':
                            sp[0] = G0_ASCII;
                            break;
                        case 'I':
                            sp[0] = G0_JISX0201_KATAKANA;
                            break;
                    }
                }
                else {
                    switch (s[sStart+l-1]&0xFF) {
                        case '@':
                            sp[0] = G0_JISX0208_1978;
                            break;
                        case 'B':
                            sp[0] = G0_JISX0208_1983;
                            break;
                    }
                }
                return 0;
            case 0x0E:
                sp[0] = G0_JISX0201_KATAKANA;
                return 0;
            case 0x0F:
                sp[0] = G0_ASCII;
                return 0;
            default:
                if (sp[0] == G0_JISX0201_KATAKANA ||
                    (0xA1 <= s0 && s0 <= 0xDF && sp[0] == G0_ASCII)) {
                o[oStart] = (byte)0x8E;
                o[oStart+1] = (byte)(s0 | 0x80);
            }
        /* else if (0x7F == s[0] && s[0] <= 0x88) { */
            /* User Defined Characters */
            /* o[n++] = s[0] | 0xE0; */
            /* o[n++] = s[1] | 0x80; */
        /* else if (0x89 <= s[0] && s[0] <= 0x92) { */
            /* User Defined Characters 2 */
            /* o[n++] = 0x8f; */
            /* o[n++] = s[0] + 0x6C; */
            /* o[n++] = s[1] | 0x80; */
        /* } */
            else {
            /* JIS X 0208 */
            /* NEC Special Characters */
            /* NEC-selected IBM extended Characters */
                s1 = s[sStart+1]&0xFF;
                o[oStart] = (byte)(s0 | 0x80);
                o[oStart+1] = (byte)(s1 | 0x80);
            }
            return 2;
        }
    }

    public static int iso2022jpKddiInit(byte[] statep) {
        statep[0] = G0_ASCII;
        return 0;
    }

    public static final int iso2022jp_kddi_decoder_jisx0208_rest = Transcoding.WORDINDEX2INFO(16);

    public static int funSiIso2022jpKddiDecoder(byte[] statep, byte[] s, int sStart, int l) {
        int s0 = s[sStart] & 0xFF;
        byte[] sp = statep;
        if (sp[0] == G0_ASCII) {
            return TranscodingInstruction.NOMAP;
        } else if (0x21 <= s0 && s0 <= 0x7e) {
            return iso2022jp_kddi_decoder_jisx0208_rest;
        } else {
            return TranscodingInstruction.INVALID;
        }
    }

    public static int funSoIso2022jpKddiDecoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0 = s[sStart] & 0xFF;
        int s1 = s[sStart+1] & 0xFF;
        byte[] sp = statep;
        if (s0 == 0x1b) {
            if (s1 == '(') {
                switch (s[sStart+l-1] & 0xFF) {
                    case 'B': /* US-ASCII */
                    case 'J': /* JIS X 0201 Roman */
                        sp[0] = G0_ASCII;
                        break;
                }
            }
            else {
                switch (s[sStart+l-1] & 0xFF) {
                    case '@':
                        sp[0] = G0_JISX0208_1978;
                        break;

                    case 'B':
                        sp[0] = G0_JISX0208_1983;
                        break;
                }
            }
            return 0;
        }
        else {
            if (sp[0] == G0_JISX0208_1978) {
                o[oStart] = (byte)EMACS_MULE_LEADING_CODE_JISX0208_1978;
            } else {
                o[oStart] = (byte)EMACS_MULE_LEADING_CODE_JISX0208_1983;
            }
            o[oStart+1] = (byte)(s0 | 0x80);
            o[oStart+2] = (byte)(s1 | 0x80);
            return 3;
        }
    }

    public static int funSoIso2022jpKddiEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0 = s[sStart] & 0xFF;
        int s1, s2;
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1)
            newstate = G0_ASCII;
        else if (s0 == EMACS_MULE_LEADING_CODE_JISX0208_1978)
            newstate = G0_JISX0208_1978;
        else
            newstate = G0_JISX0208_1983;

        if (sp[0] != newstate) {
            o[oStart++] = 0x1b;
            switch (newstate) {
                case G0_ASCII:
                    o[oStart++] = '(';
                    o[oStart++] = 'B';
                    break;
                case G0_JISX0208_1978:
                    o[oStart++] = '$';
                    o[oStart++] = '@';
                    break;
                default:
                    o[oStart++] = '$';
                    o[oStart++] = 'B';
                    break;
            }
            sp[0] = (byte)newstate;
        }

        if (l == 1) {
            o[oStart++] = (byte)(s0 & 0x7f);
        }
        else {
            s1 = s[sStart+1] & 0xFF;
            s2 = s[sStart+2] & 0xFF;
            o[oStart++] = (byte)(s1 & 0x7f);
            o[oStart++] = (byte)(s2 & 0x7f);
        }

        return oStart - output0;

    }

    public static int finishIso2022jpKddiEncoder(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII) return 0;

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int iso2022jpKddiEncoderResetSequence_size(byte[] statep) {
        byte[] sp = statep;
        if (sp[0] != G0_ASCII) return 3;
        return 0;
    }

    public static int fromUtf8MacInit(byte[] state) {
        bufClear(state);
        return 0;
    }

    private static final int STATUS_BUF_SIZE = 16;
    private static final int TOTAL_BUF_SIZE = STATUS_BUF_SIZE + 4 * 2; // status buf plus two ints

    private static final int bufBytesize(byte[] p) {
        return (bufEnd(p) - bufBeg(p) + STATUS_BUF_SIZE) % STATUS_BUF_SIZE;
    }

    private static final byte bufAt(byte[] sp, int pos) {
        pos += bufBeg(sp);
        pos %= STATUS_BUF_SIZE;
        return sp[pos];
    }

    private static void bufClear(byte[] state) {
        assert state.length >= 24 : "UTF8-MAC state not large enough";

        Arrays.fill(state, (byte)0);
    }

    public static int funSoFromUtf8Mac(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int n = 0;

        switch (l) {
            case 1:
                n = fromUtf8MacFinish(sp, o, oStart, oSize);
                break;
            case 4:
                n = fromUtf8MacFinish(sp, o, oStart, oSize);
                o[oStart+n++] = s[sStart++];
                o[oStart+n++] = s[sStart++];
                o[oStart+n++] = s[sStart++];
                o[oStart+n++] = s[sStart++];
                return n;
        }

        bufPush(sp, s, sStart, l);
        n += bufApply(sp, o, oStart);
        return n;
    }

    private static void bufPush(byte[] sp, byte[] p, int pStart, int l) {
        int pend = pStart + l;
        while (pStart < pend) {
            /* if (sp->beg == sp->end) */
            sp[bufEndPostInc(sp)] = p[pStart++];
            bufEnd(sp, bufEnd(sp) % STATUS_BUF_SIZE);
        }
    }

    private static final int from_utf8_mac_nfc2 = Transcoding.WORDINDEX2INFO(35578);

    private static int bufApply(byte[] sp, byte[] o, int oStart) {
        int n = 0;
        int next_info;
        byte[] buf = {0,0,0};
        if (bufBytesize(sp) < 3 || (bufBytesize(sp) == 3 && bufAt(sp, 0) >= 0xE0)) {
        /* char length is less than 2 */
            return 0;
        }
        next_info = getInfo(from_utf8_mac_nfc2, sp);
        switch (next_info & 0x1F) {
            case TranscodingInstruction.THREEbt:
            case TranscodingInstruction.TWObt:
                buf[n++] = Transcoding.getBT1(next_info);
                buf[n++] = Transcoding.getBT2(next_info);
                if (TranscodingInstruction.THREEbt == (next_info & 0x1F))
                    buf[n++] = Transcoding.getBT3(next_info);
                bufClear(sp);
                bufPush(sp, buf, 0, n);
                return 0;
            default:
                return bufOutputChar(sp, o, oStart);
        }
    }

    private static boolean bufEmpty(byte[] sp) {
        return bufBeg(sp) == bufEnd(sp);
    }

    private static byte bufShift(byte[] sp) {
        /* if (sp->beg == sp->end) */
        int c = sp[bufBegPostInc(sp)];
        bufBeg(sp, bufBeg(sp) % STATUS_BUF_SIZE);
        return (byte)c;
    }

    private static boolean utf8Trailbyte(byte c) {
        return (c & 0xC0) == 0x80;
    }

    private static int bufOutputChar(byte[] sp, byte[] o, int oStart) {
        int n = 0;
        while (!bufEmpty(sp)) {
            o[oStart+n++] = bufShift(sp);
            if (!utf8Trailbyte(sp[bufBeg(sp)])) break;
        }
        return n;
    }

    private static int getInfo(int nextInfo, byte[] sp) {
        int pos = 0;
        while (pos < bufBytesize(sp)) {
            int next_byte = bufAt(sp, pos++) & 0xFF;
            if (next_byte < UTF8MAC_BL_MIN_BYTE(nextInfo) || UTF8MAC_BL_MAX_BYTE(nextInfo) < next_byte)
                nextInfo = TranscodingInstruction.INVALID;
            else {
                nextInfo = UTF8MAC_BL_ACTION(nextInfo, (byte)next_byte);
            }
            if ((nextInfo & 3) == 0) continue;
            break;
        }
        return nextInfo;
    }

    public static int UTF8MAC_BL_MIN_BYTE(int nextInfo) {
        return From_UTF8_MAC_Transcoder.INSTANCE.byteArray[BL_BASE(nextInfo)] & 0xFF;
    }

    public static int UTF8MAC_BL_MAX_BYTE(int nextInfo) {
        return From_UTF8_MAC_Transcoder.INSTANCE.byteArray[BL_BASE(nextInfo) + 1] & 0xFF;
    }

    public static int UTF8MAC_BL_OFFSET(int nextInfo, int b) {
        return From_UTF8_MAC_Transcoder.INSTANCE.byteArray[BL_BASE(nextInfo) + 2 + b - UTF8MAC_BL_MIN_BYTE(nextInfo)] & 0xFF;
    }

    public static int UTF8MAC_BL_ACTION(int nextInfo, byte b) {
        return From_UTF8_MAC_Transcoder.INSTANCE.intArray[BL_INFO(nextInfo) + UTF8MAC_BL_OFFSET(nextInfo, b & 0xFF)];
    }

    private static int BL_BASE(int nextInfo) {
        return BYTE_ADDR(BYTE_LOOKUP_BASE(WORD_ADDR(nextInfo)));
    }

    private static int BL_INFO(int nextInfo) {
        return WORD_ADDR(BYTE_LOOKUP_INFO(WORD_ADDR(nextInfo)));
    }

    private static int BYTE_ADDR(int index) {
        return index;
    }

    private static int WORD_ADDR(int index) {
        return TranscodeTableSupport.INFO2WORDINDEX(index);
    }

    private static int BYTE_LOOKUP_BASE(int bl) {
        return From_UTF8_MAC_Transcoder.INSTANCE.intArray[bl];
    }

    private static int BYTE_LOOKUP_INFO(int bl) {
        return From_UTF8_MAC_Transcoder.INSTANCE.intArray[bl + 1];
    }

    private static int bufInt(byte[] statep, int base) {
        return (statep[base] << 24) | (statep[base+1] << 16) | (statep[base+2] << 8) | (statep[base+3]);
    }

    private static void bufInt(byte[] statep, int base, int val) {
        statep[base] = (byte)((val >>> 24) & 0xFF);
        statep[base+1] = (byte)((val >>> 16) & 0xFF);
        statep[base+2] = (byte)((val >>> 8) & 0xFF);
        statep[base+3] = (byte)(val & 0xFF);
    }

    private static int bufBeg(byte[] statep) {
        return bufInt(statep, 16);
    }

    private static int bufEnd(byte[] statep) {
        return bufInt(statep, 20);
    }

    private static void bufBeg(byte[] statep, int end) {
        bufInt(statep, 16, end);
    }

    private static void bufEnd(byte[] statep, int end) {
        bufInt(statep, 20, end);
    }

    private static int bufEndPostInc(byte[] statep) {
        int end = bufInt(statep, 20);
        bufInt(statep, 20, end + 1);
        return end;
    }

    private static int bufBegPostInc(byte[] statep) {
        int beg = bufInt(statep, 16);
        bufInt(statep, 16, beg + 1);
        return beg;
    }

    public static int fromUtf8MacFinish(byte[] statep, byte[] o, int oStart, int oSize) {
        return bufOutputAll(statep, o, oStart);
    }

    private static int bufOutputAll(byte[] sp, byte[] o, int oStart) {
        int n = 0;
        while (!bufEmpty(sp)) {
            o[oStart+n++] = bufShift(sp);
        }
        return n;
    }

    private static final int ESCAPE_END = 0;
    private static final int ESCAPE_NORMAL = 1;

    public static int escapeXmlAttrQuoteInit(byte[] statep) {
        statep[0] = ESCAPE_END;
        return 0;
    }

    public static int funSoEscapeXmlAttrQuote(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int n = 0;
        if (sp[0] == ESCAPE_END) {
            sp[0] = ESCAPE_NORMAL;
            o[oStart+n++] = '"';
        }
        o[oStart+n++] = s[sStart];
        return n;
    }

    public static int escapeXmlAttrQuoteFinish(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int n = 0;

        if (sp[0] == ESCAPE_END) {
            o[oStart+n++] = '"';
        }

        o[oStart+n++] = '"';
        sp[0] = ESCAPE_END;

        return n;
    }

    private static final int NEWLINE_NORMAL = 0;
    private static final int NEWLINE_JUST_AFTER_CR = 1;

    private static final int MET_LF = 0x01;
    private static final int MET_CRLF = 0x02;
    private static final int MET_CR = 0x04;

    private static byte NEWLINE_STATE(byte[] sp) {
        return sp[0];
    }

    private static void NEWLINE_STATE(byte[] sp, int b) {
        sp[0] = (byte)b;
    }

    private static void NEWLINE_NEWLINES_MET(byte[] sp, int b) {
        sp[1] = (byte)b;
    }

    private static void NEWLINE_NEWLINES_MET_or_mask(byte[] sp, int b) {
        sp[1] |= (byte)b;
    }

    public static int universalNewlineInit(byte[] statep) {
        byte[] sp = statep;
        NEWLINE_STATE(sp, NEWLINE_NORMAL);
        NEWLINE_NEWLINES_MET(sp, 0);

        return 0;
    }

    public static int funSoUniversalNewline(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int s0 = s[sStart] & 0xFF;
        byte[] sp = statep;
        int len;
        if (s0 == '\n') {
            if (NEWLINE_STATE(sp) == NEWLINE_NORMAL) {
                NEWLINE_NEWLINES_MET_or_mask(sp, MET_LF);
            }
            else { /* JUST_AFTER_CR */
                NEWLINE_NEWLINES_MET_or_mask(sp, MET_CRLF);
            }
            o[oStart] = '\n';
            len = 1;
            NEWLINE_STATE(sp, NEWLINE_NORMAL);
        }
        else {
            len = 0;
            if (NEWLINE_STATE(sp) == NEWLINE_JUST_AFTER_CR) {
                o[oStart] = '\n';
                len = 1;
                NEWLINE_NEWLINES_MET_or_mask(sp, MET_CR);
            }
            if (s0 == '\r') {
                NEWLINE_STATE(sp, NEWLINE_JUST_AFTER_CR);
            }
            else {
                o[oStart+len++] = (byte)s0;
                NEWLINE_STATE(sp, NEWLINE_NORMAL);
            }
        }

        return len;
    }

    public static int universalNewlineFinish(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int len = 0;
        if (NEWLINE_STATE(sp) == NEWLINE_JUST_AFTER_CR) {
            o[oStart] = '\n';
            len = 1;
            NEWLINE_NEWLINES_MET_or_mask(sp, MET_CR);
        }
        sp[0] = NEWLINE_NORMAL;
        return len;
    }
}
