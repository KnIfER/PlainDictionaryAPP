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

import org.jcodings.Ptr;

public class Transcoding implements TranscodingInstruction {
    public Transcoding(Transcoder transcoder, int flags) {
        this.transcoder = transcoder;
        this.flags = flags;
        this.resumePosition = START;
        this.recognizedLength = 0;
        this.readAgainLength = 0;
        this.writeBuffLen = 0;
        this.writeBuffOff = 0;
        this.readBuf = new byte[transcoder.maxInput];
        this.writeBuf = new byte[transcoder.maxOutput];
        this.state = new byte[transcoder.stateSize];
        transcoder.stateInit(state);
    }

    public final Transcoder transcoder;
    private int flags;

    private int resumePosition;
    private int nextTable;
    private int nextInfo;
    private byte nextByte;
    private int outputIndex;

    int recognizedLength, readAgainLength;

    final byte[] readBuf;

    private int writeBuffOff, writeBuffLen;
    private final byte[] writeBuf;
    final byte[] state;

    private EConvResult suspendResult;

    void close() {
        transcoder.stateFinish(state);
    }

    private int charStart;
    private byte[] charStartBytes;

    @Override
    public String toString() {
        return "Transcoding for transcoder " + transcoder.toString();
    }

    /* transcode_char_start */
    int charStart() {
        if (recognizedLength > inCharStart - inPos.p) {
            System.arraycopy(inBytes, inCharStart, readBuf, recognizedLength, inP - inCharStart);
            charStart = 0;
            charStartBytes = readBuf;
        } else {
            charStart = inCharStart - recognizedLength;
            charStartBytes = inBytes;
        }

        return recognizedLength + (inP - inCharStart);
    }

    /* rb_transcoding_convert */
    EConvResult convert(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags) {
        return transcodeRestartable(in, inPtr, inStop, out, outPtr, outStop, flags);
    }

    private EConvResult transcodeRestartable(byte[] in, Ptr inStart, int inStop, byte[] out, Ptr outStart, int outStop, int opt) {
        if (readAgainLength != 0) {
            byte[] readAgainBuf = new byte[readAgainLength];
            Ptr readAgainPos = new Ptr(0);
            int readAgainStop = readAgainLength;
            System.arraycopy(readBuf, recognizedLength, readAgainBuf, readAgainPos.p, readAgainLength);
            readAgainLength = 0;

            System.arraycopy(readAgainBuf, 0, TRANSCODING_READBUF(this), recognizedLength, readAgainLength);
            readAgainLength = 0;
            EConvResult res = transcodeRestartable0(readAgainBuf, readAgainPos, out, outStart, readAgainStop, outStop, opt | EConv.PARTIAL_INPUT);
            if (!res.isSourceBufferEmpty()) {
                System.arraycopy(readAgainBuf, readAgainPos.p, readBuf, recognizedLength + readAgainLength, readAgainStop - readAgainPos.p);
                readAgainLength += readAgainStop - readAgainPos.p;
            }
        }
        return transcodeRestartable0(in, inStart, out, outStart, inStop, outStop, opt);
    }

    private int inCharStart;
    private byte[] inBytes;
    private int inP;

    private Ptr inPos;

    private static int STR1_LENGTH(byte[] bytes, int byteaddr) {
        return bytes[byteaddr] + 4;
    }

    private static int STR1_BYTEINDEX(int w) {
        return w >>> 6;
    }

    private EConvResult transcodeRestartable0(final byte[] in_bytes, Ptr in_pos, final byte[] out_bytes, Ptr out_pos, int in_stop, int out_stop, int opt) {
        Transcoder tr = transcoder;
        int unitlen = tr.inputUnitLength;
        int readagain_len = 0;

        int inchar_start = in_pos.p;
        int in_p = inchar_start;

        int out_p = out_pos.p;

        int[] char_len = null;
        byte[][] outByteParam = null;

        int ip = resumePosition;

        MACHINE: while (true) {
            switch (ip) {
                case START:
                    inchar_start = in_p;
                    recognizedLength = 0;
                    nextTable = tr.treeStart;

                    if (SUSPEND == SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_AFTER_OUTPUT)) return suspendResult;
                    // fall through
                case RESUME_AFTER_OUTPUT:
                    if (in_stop <= in_p) {
                        if ((opt & EConvFlags.PARTIAL_INPUT) == 0) {
                            ip = CLEANUP;
                            continue;
                        }
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, START);
                        return suspendResult;
                    }
                    // fall through
                case NEXTBYTE:
                    nextByte = in_bytes[in_p++];
                    // fall through
                case FOLLOW_BYTE: // follow_byte:
                    if ((nextByte & 0xFF) < BL_MIN_BYTE(this) || BL_MAX_BYTE(this) < (nextByte & 0xFF)) {
                        nextInfo = INVALID;
                    } else {
                        nextInfo = BL_ACTION(this, nextByte);
                    }
                    // fall through
                case FOLLOW_INFO: // follow_info:
                    switch (nextInfo & 0x1F) {
                        case NOMAP:
                            int p = inchar_start;
                            writeBuffOff = 0;
                            while (p < in_p) {
                                writeBuf[writeBuffOff++] = in_bytes[p++];
                            }
                            writeBuffLen = writeBuffOff;
                            writeBuffOff = 0;
                            ip = NOMAP_TRANSFER;
                            continue;
                        case 0x00: case 0x04: case 0x08: case 0x0C: case 0x10: case 0x14: case 0x18: case 0x1C:
                            if (SUSPEND == SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, SELECT_TABLE)) return suspendResult;
                            ip = SELECT_TABLE;
                            continue;
                        case ZERObt: // drop input
                            ip = START;
                            continue;
                        case ONEbt:
                            ip = ONE_BYTE_1;
                            continue;
                        case TWObt:
                            ip = TWO_BYTE_1;
                            continue;
                        case THREEbt:
                            ip = FOUR_BYTE_1;
                            continue;
                        case FOURbt:
                            ip = FOUR_BYTE_0;
                            continue;
                        case GB4bt:
                            ip = GB_FOUR_BYTE_0;
                            continue;
                        case STR1:
                            outputIndex = 0;
                            ip = STRING;
                            continue;
                        case FUNii:
                            nextInfo = tr.infoToInfo(state, nextInfo);
                            ip = FOLLOW_INFO;
                            continue;
                        case FUNsi:
                        {
                            int char_start;
                            char_len = PREPARE_CHAR_LEN(char_len);
                            outByteParam = PREPARE_OUT_BYTES(outByteParam);
                            char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len, outByteParam);
                            nextInfo = tr.startToInfo(state, outByteParam[0], char_start, char_len[0]);
                            ip = FOLLOW_INFO;
                            continue;
                        }
                        case FUNio:
                            ip = CALL_FUN_IO;
                            continue;
                        case FUNso:
                            ip = RESUME_CALL_FUN_SO;
                            continue;
                        case FUNsio:
                            if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_CALL_FUN_SIO)) return suspendResult;
                            ip = CALL_FUN_SIO;
                            continue;
                        case INVALID:
                            if (recognizedLength + (in_p - inchar_start) <= unitlen) {
                                if (recognizedLength + (in_p - inchar_start) < unitlen) {
                                    if (SUSPEND == SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, READ_MORE)) return suspendResult;
                                }
                                ip = READ_MORE;
                                continue;
                            } else {
                                int invalid_len;
                                int discard_len;
                                invalid_len = recognizedLength + (in_p - inchar_start);
                                discard_len = ((invalid_len - 1) / unitlen) * unitlen;
                                readagain_len = invalid_len - discard_len;
                                ip = REPORT_INVALID;
                                continue;
                            }
                        case UNDEF:
                            ip = REPORT_UNDEF;
                            continue;
                        default:
                            throw new RuntimeException("unknown transcoding instruction");
                    }

                // read more for character

                case READ_MORE:
                    while ((opt & EConvFlags.PARTIAL_INPUT) != 0 && recognizedLength + (in_stop - inchar_start) < unitlen) {
                        in_p = in_stop;
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, READ_MORE);
                        return suspendResult;
                    }
                    if (recognizedLength + (in_stop - inchar_start) <= unitlen) {
                        in_p = in_stop;
                    } else {
                        in_p = inchar_start + (unitlen - recognizedLength);
                    }
                    ip = REPORT_INVALID;
                    continue;

                // func_sio logic

                case RESUME_CALL_FUN_SIO:
                    // continue loop from SUSPEND_OBUF
                    while (out_stop - out_p < 1) {
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.DestinationBufferFull, RESUME_CALL_FUN_SIO);
                        return suspendResult;
                    }
                    // fall through
                case CALL_FUN_SIO:
                {
                    int char_start;
                    char_len = PREPARE_CHAR_LEN(char_len);
                    outByteParam = PREPARE_OUT_BYTES(outByteParam);
                    if (tr.maxOutput <= out_stop - out_p) {
                        char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len, outByteParam);
                        out_p += tr.startInfoToOutput(state, outByteParam[0], char_start, char_len[0], nextInfo, out_bytes, out_p, out_stop - out_p);
                        ip = START;
                        continue;
                    } else {
                        char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len, outByteParam);
                        writeBuffLen = tr.startInfoToOutput(state, outByteParam[0], char_start, char_len[0], nextInfo, writeBuf, 0, writeBuf.length);
                        writeBuffOff = 0;
                        ip = TRANSFER_WRITEBUF;
                        continue;
                    }
                }

                // func_so logic

                case RESUME_CALL_FUN_SO:
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_CALL_FUN_SO)) return suspendResult;
                    // fall through
                case CALL_FUN_SO:
                {
                    int char_start;
                    char_len = PREPARE_CHAR_LEN(char_len);
                    outByteParam = PREPARE_OUT_BYTES(outByteParam);
                    if (tr.maxOutput <= out_stop - out_p) {
                        char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len, outByteParam);
                        out_p += tr.startToOutput(state, outByteParam[0], char_start, char_len[0], out_bytes, out_p, out_stop - out_p);
                        ip = START;
                        continue;
                    } else {
                        char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len, outByteParam);
                        writeBuffLen = tr.startToOutput(state, outByteParam[0], char_start, char_len[0], writeBuf, 0, writeBuf.length);
                        writeBuffOff = 0;
                        ip = TRANSFER_WRITEBUF;
                        continue;
                    }
                }

                // func_io logic

                case CALL_FUN_IO:
                    if (tr.maxOutput <= out_stop - out_p) {
                        out_p += tr.infoToOutput(state, nextInfo, out_bytes, out_p, out_stop - out_p);
                        ip = START;
                        continue;
                    } else {
                        writeBuffLen = tr.infoToOutput(state, nextInfo, writeBuf, 0, writeBuf.length);
                        writeBuffOff = 0;
                        ip = TRANSFER_WRITEBUF;
                        continue;
                    }

                // write buffer transfer logic

                case RESUME_TRANSFER_WRITEBUF:
                    // continue loop from SUSPEND_OBUF
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_TRANSFER_WRITEBUF)) return suspendResult;
                    out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    // fall through
                case TRANSFER_WRITEBUF:
                    while (writeBuffOff < writeBuffLen) {
                        if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_TRANSFER_WRITEBUF)) return suspendResult;
                        out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    }
                    ip = START;
                    continue;

                // bytes from info logic

                case ONE_BYTE_1: // byte 1
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, ONE_BYTE_1)) return suspendResult;
                    out_bytes[out_p++] = getBT1(nextInfo);
                    ip = START;
                    continue;

                case TWO_BYTE_1: // bytes 1, 2
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, TWO_BYTE_1)) return suspendResult;
                    out_bytes[out_p++] = getBT1(nextInfo);
                    // fall through
                case TWO_BYTE_2: // byte 2
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, TWO_BYTE_2)) return suspendResult;
                    out_bytes[out_p++] = getBT2(nextInfo);
                    ip = START; // continue
                    continue;

                case FOUR_BYTE_0: // bytes 0, 1, 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_0)) return suspendResult;
                    out_bytes[out_p++] = getBT0(nextInfo);
                    // fall through
                case FOUR_BYTE_1: // bytes 1, 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_1)) return suspendResult;
                    out_bytes[out_p++] = getBT1(nextInfo);
                    // fall through
                case FOUR_BYTE_2: // bytes 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_2)) return suspendResult;
                    out_bytes[out_p++] = getBT2(nextInfo);
                    // fall through
                case FOUR_BYTE_3: // byte 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_3)) return suspendResult;
                    out_bytes[out_p++] = getBT3(nextInfo);
                    ip = START;
                    continue;

                case GB_FOUR_BYTE_0: // GB4 bytes 0, 1, 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_0)) return suspendResult;
                    out_bytes[out_p++] = getGB4bt0(nextInfo);
                    // fall through
                case GB_FOUR_BYTE_1: // GB4 bytes 1, 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_1)) return suspendResult;
                    out_bytes[out_p++] = getGB4bt1(nextInfo);
                    // fall through
                case GB_FOUR_BYTE_2: // GB4 bytes 2, 3
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_2)) return suspendResult;
                    out_bytes[out_p++] = getGB4bt2(nextInfo);
                    // fall through
                case GB_FOUR_BYTE_3: // GB4 bytes 3
                    out_bytes[out_p++] = getGB4bt3(nextInfo);
                    ip = START;
                    continue;

                // for string table mappings, like xml escaping

                case RESUME_STRING:
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_STRING)) return suspendResult;
                    out_bytes[out_p++] = transcoder.byteArray[BYTE_ADDR(STR1_BYTEINDEX(nextInfo)) + 1 + outputIndex];
                    outputIndex++;
                    // fall through
                case STRING:
                    while (outputIndex < STR1_LENGTH(transcoder.byteArray, BYTE_ADDR(STR1_BYTEINDEX(nextInfo)))) {
                        if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_STRING)) return suspendResult;
                        out_bytes[out_p++] = transcoder.byteArray[BYTE_ADDR(STR1_BYTEINDEX(nextInfo)) + 1 + outputIndex];
                        outputIndex++;
                    }
                    ip = START;
                    continue;

                case RESUME_NOMAP:
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_NOMAP)) return suspendResult;
                    out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    // fall through
                case NOMAP_TRANSFER:
                    while (writeBuffOff < writeBuffLen) {
                        if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_NOMAP)) return suspendResult;
                        out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    }
                    ip = START;
                    continue;

                // table selection

                case SELECT_TABLE:
                    while (in_p >= in_stop) {
                        if ((opt & EConvFlags.PARTIAL_INPUT) == 0) {
                            ip = REPORT_INCOMPLETE; // incomplete
                            continue MACHINE;
                        }
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, SELECT_TABLE);
                        return suspendResult;
                    }
                    nextByte = in_bytes[in_p++];
                    nextTable = nextInfo;
                    ip = FOLLOW_BYTE;
                    continue;

                // error reporting

                case REPORT_INVALID: // invalid:
                    SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.InvalidByteSequence, START);
                    return suspendResult;
                case REPORT_INCOMPLETE: // incomplete:
                    SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.IncompleteInput, START);
                    return suspendResult;
                case REPORT_UNDEF: // undef:
                    SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.UndefinedConversion, START);
                    return suspendResult;

                // tidying up

                case RESUME_FINISH_WRITEBUF:
                    if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_FINISH_WRITEBUF)) return suspendResult;
                    out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    while (writeBuffOff <= writeBuffLen) {
                        if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_FINISH_WRITEBUF)) return suspendResult;
                        out_bytes[out_p++] = writeBuf[writeBuffOff++];
                    }
                    ip = FINISHED;
                    continue;
                case FINISH_FUNC:
                    if (tr.maxOutput <= out_stop - out_p) {
                        out_p += tr.finish(state, out_bytes, out_p, out_stop - out_p);
                    } else {
                        writeBuffLen = tr.finish(state, writeBuf, 0, writeBuf.length);
                        writeBuffOff = 0;
                        while (writeBuffOff < writeBuffLen) {
                            if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_FINISH_WRITEBUF)) return suspendResult;
                            out_bytes[out_p++] = writeBuf[writeBuffOff++];
                        }
                    }
                    ip = FINISHED;
                    continue;
                case CLEANUP:
                    if (tr.hasFinish()) {
                        if (SUSPEND == SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, CLEANUP)) return suspendResult;
                        ip = FINISH_FUNC;
                        continue;
                    }
                    // fall through
                case FINISHED:
                    SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.Finished, FINISHED);
                    return suspendResult;
            }
        }
    }

    private int[] PREPARE_CHAR_LEN(int[] char_len) {
        if (char_len == null) {
            char_len = new int[]{0};
        } else {
            char_len[0] = 0;
        }
        return char_len;
    }

    private byte[][] PREPARE_OUT_BYTES(byte[][] outBytes) {
        if (outBytes == null) {
            outBytes = new byte[1][];
        } else {
            outBytes[0] = null;
        }
        return outBytes;
    }

    private int transcode_char_start(byte[] in_bytes, int in_start, int inchar_start, int in_p, int[] char_len_ptr, byte[][] retBytes) {
        int ptr;
        byte[] bytes;
        if (inchar_start - in_start < recognizedLength) {
            System.arraycopy(in_bytes, inchar_start, TRANSCODING_READBUF(this), recognizedLength, in_p - inchar_start);
            ptr = 0;
            bytes = TRANSCODING_READBUF(this);
        }
        else {
            ptr = inchar_start - recognizedLength;
            bytes = in_bytes;
        }
        char_len_ptr[0] = recognizedLength + (in_p - inchar_start);
        retBytes[0] = bytes;
        return ptr;
    }

    private static int SUSPEND(Transcoding tc, byte[] in_bytes, int in_p, int inchar_start, Ptr in_pos, Ptr out_pos, int out_p, int readagain_len, EConvResult ret, int ip) {
        prepareToSuspend(tc, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, ip);
        tc.suspendResult = ret;
        return SUSPEND;
    }

    private static void prepareToSuspend(Transcoding tc, byte[] in_bytes, int in_p, int inchar_start, Ptr in_pos, Ptr out_pos, int out_p, int readagain_len, int ip) {
        tc.resumePosition = ip;
        int recognizedLength = tc.recognizedLength;
        if (in_p - inchar_start > 0) System.arraycopy(in_bytes, inchar_start, tc.readBuf, recognizedLength, in_p - inchar_start);
        in_pos.p = in_p;
        out_pos.p = out_p;
        recognizedLength += in_p - inchar_start;
        if (readagain_len != 0) {
            recognizedLength -= readagain_len;
            tc.readAgainLength = readagain_len;
        }
        tc.recognizedLength = recognizedLength;
    }

    private static int SUSPEND_OBUF(Transcoding tc, int out_stop, byte[] in_bytes, int in_p, int inchar_start, Ptr in_pos, Ptr out_pos, int out_p, int readagain_len, int ip) {
        while (out_stop - out_p < 1) {
            return SUSPEND(tc, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.DestinationBufferFull, ip);
        }
        return ip;
    }

    private static int SUSPEND_AFTER_OUTPUT(Transcoding tc, int opt, byte[] in_bytes, int in_p_offset, int inchar_start_offset, Ptr in_pos, Ptr out_pos, int out_p_offset, int readagain_len, int ip) {
        if (checkAfterOutput(opt, out_pos, out_p_offset)) {
            return SUSPEND(tc, in_bytes, in_p_offset, inchar_start_offset, in_pos, out_pos, out_p_offset, readagain_len, EConvResult.AfterOutput, ip);
        }
        return ip;
    }

    private static boolean checkAfterOutput(int opt, Ptr out_pos, int out_p_offset) {
        return (opt & EConvFlags.AFTER_OUTPUT) != 0 && out_pos.p != out_p_offset;
    }

    private static final int SUSPEND = 0;
    private static final int START = 1;
    private static final int RESUME_AFTER_OUTPUT = 2;
    private static final int NEXTBYTE = 3;
    private static final int FOLLOW_BYTE = 4;
    private static final int FOLLOW_INFO = 5;
    private static final int NOMAP_TRANSFER = 35;
    private static final int READ_MORE = 6;
    private static final int CALL_FUN_SIO = 8;
    private static final int RESUME_CALL_FUN_SIO = 44;
    private static final int CALL_FUN_SO = 9;
    private static final int RESUME_CALL_FUN_SO = 43;
    private static final int CALL_FUN_IO = 10;
    private static final int TRANSFER_WRITEBUF = 11;
    private static final int RESUME_TRANSFER_WRITEBUF = 12;
    private static final int ONE_BYTE_1 = 13;
    private static final int TWO_BYTE_1 = 14;
    private static final int TWO_BYTE_2 = 15;
    private static final int FOUR_BYTE_1 = 16;
    private static final int FOUR_BYTE_2 = 17;
    private static final int FOUR_BYTE_3 = 18;
    private static final int FOUR_BYTE_0 = 19;
    private static final int GB_FOUR_BYTE_0 = 20;
    private static final int GB_FOUR_BYTE_1 = 21;
    private static final int GB_FOUR_BYTE_2 = 22;
    private static final int GB_FOUR_BYTE_3 = 23;
    private static final int STRING = 24;
    private static final int RESUME_STRING = 25;
    private static final int RESUME_NOMAP = 26;
    private static final int SELECT_TABLE = 27;
    private static final int REPORT_INVALID = 28;
    private static final int REPORT_INCOMPLETE = 29;
    private static final int REPORT_UNDEF = 30;
    private static final int FINISH_FUNC = 31;
    private static final int RESUME_FINISH_WRITEBUF = 32;
    private static final int FINISHED = 33;
    private static final int CLEANUP = 34;

    private static byte[] TRANSCODING_READBUF(Transcoding tc) {
        return tc.readBuf;
    }

    private static final int WORDINDEX_SHIFT_BITS = 2;

    public static int WORDINDEX2INFO(int widx) {
        return widx << WORDINDEX_SHIFT_BITS;
    }

    private static int INFO2WORDINDEX(int info) {
        return info >>> WORDINDEX_SHIFT_BITS;
    }

    private static int BYTE_ADDR(int index) {
        return index;
    }

    private static int WORD_ADDR(int index) {
        return INFO2WORDINDEX(index);
    }

    private static int BL_BASE(Transcoding tc) {
        return BYTE_ADDR(BYTE_LOOKUP_BASE(tc, WORD_ADDR(tc.nextTable)));
    }

    private static int BL_INFO(Transcoding tc) {
        return WORD_ADDR(BYTE_LOOKUP_INFO(tc, WORD_ADDR(tc.nextTable)));
    }

    private static int BYTE_LOOKUP_BASE(Transcoding tc, int bl) {
        return tc.transcoder.intArray[bl];
    }

    private static int BYTE_LOOKUP_INFO(Transcoding tc, int bl) {
        return tc.transcoder.intArray[bl + 1];
    }

    public static int BL_MIN_BYTE(Transcoding tc) {
        return tc.transcoder.byteArray[BL_BASE(tc)] & 0xFF;
    }

    public static int BL_MAX_BYTE(Transcoding tc) {
        return tc.transcoder.byteArray[BL_BASE(tc) + 1] & 0xFF;
    }

    public static int BL_OFFSET(Transcoding tc, int b) {
        return tc.transcoder.byteArray[BL_BASE(tc) + 2 + b - BL_MIN_BYTE(tc)] & 0xFF;
    }

    public static int BL_ACTION(Transcoding tc, byte b) {
        return tc.transcoder.intArray[BL_INFO(tc) + BL_OFFSET(tc, b & 0xFF)];
    }

    public static byte getGB4bt0(int a) {
        return (byte)(a >>> 8);
    }

    public static byte getGB4bt1(int a) {
        return (byte)(((a >>> 24) & 0xf) | 0x30);
    }

    public static byte getGB4bt2(int a) {
        return (byte)(a >>> 16);
    }

    public static byte getGB4bt3(int a) {
        return (byte)(((a >>> 28) & 0x0f) | 0x30);
    }

    public static byte getBT1(int a) {
        return (byte)(a >>> 8);
    }

    public static byte getBT2(int a) {
        return (byte)(a >>> 16);
    }

    public static byte getBT3(int a) {
        return (byte)(a >>> 24);
    }

    public static byte getBT0(int a) {
        return (byte)(((a >>> 5) & 0x0F) | 0x30);
    }

}