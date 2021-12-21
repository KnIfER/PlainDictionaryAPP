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

import static org.jcodings.transcode.EConv.NULL_STRING;
import static org.jcodings.util.CaseInsensitiveBytesHash.caseInsensitiveEquals;

import java.util.Arrays;

import org.jcodings.ObjPtr;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.TranscoderException;
import org.jcodings.transcode.Transcoder.GenericTranscoderEntry;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;

public class TranscoderDB implements EConvFlags {

    public static final class Entry {
        private String transcoderClass;
        private final byte[] source, destination;
        private Transcoder transcoder;

        private Entry(byte[] source, byte[] destination) {
            this.source = source;
            this.destination = destination;
        }

        public byte[] getSource() {
            return source;
        }

        public byte[] getDestination() {
            return destination;
        }

        /* load_transcoder_entry */
        public Transcoder getTranscoder() {
            if (transcoder == null) {
                if (transcoderClass != null) {
                    transcoder = TranscoderList.getInstance(transcoderClass);
                } else {
                    GenericTranscoderEntry[] list = TranscoderList.GENERIC_LIST;
                    for (int i = 0; i < list.length; i++) {
                        GenericTranscoderEntry entry = list[i];
                        if (Arrays.equals(source, entry.source) && Arrays.equals(destination, entry.destination)) {
                            transcoder = entry.createTranscoder();
                            break;
                        }
                    }
                }
            }
            return transcoder;
        }
    }

    public static final CaseInsensitiveBytesHash<CaseInsensitiveBytesHash<Entry>> transcoders = new CaseInsensitiveBytesHash<CaseInsensitiveBytesHash<Entry>>();

    /* make_transcoder_entry */
    static Entry makeEntry(byte[] source, byte[] destination) {
        CaseInsensitiveBytesHash<Entry> sHash = transcoders.get(source);
        if (sHash == null) {
            sHash = new CaseInsensitiveBytesHash<Entry>();
            transcoders.putDirect(source, sHash);
        }
        Entry entry = sHash.get(destination);
        if (entry == null) {
            entry = new Entry(source, destination);
            sHash.putDirect(destination, entry);
        } else {
            throw new TranscoderException(ErrorMessages.ERR_TRANSCODER_ALREADY_REGISTERED, new String(source + " to " + new String(destination)));
        }

        return entry;
    }

    /* get_transcoder_entry */
    public static Entry getEntry(byte[] source, byte[] destination) {
        CaseInsensitiveBytesHash<Entry> sHash = transcoders.get(source);
        return sHash == null ? null : sHash.get(destination);
    }

    /* rb_register_transcoder */
    static void register(Transcoder transcoder) {
        Entry entry = makeEntry(transcoder.source, transcoder.destination);
        if (entry.transcoder != null)
            throw new TranscoderException(ErrorMessages.ERR_TRANSCODER_ALREADY_REGISTERED, new String(transcoder.source + " to "
                    + new String(transcoder.destination)));
        entry.transcoder = transcoder;
    }

    /* declare_transcoder */
    static void declare(String source, String destination, String transcoderClass) {
        Entry entry = makeEntry(source.getBytes(), destination.getBytes());
        entry.transcoderClass = transcoderClass;
    }

    static final class SearchPathQueue {
        ObjPtr<SearchPathQueue> next = new ObjPtr<TranscoderDB.SearchPathQueue>();
        byte[] encoding;
    }

    public interface SearchPathCallback {
        void call(byte[] source, byte[] destination, int depth);
    }

    /* transcode_search_path */
    public static int searchPath(byte[] source, byte[] destination, SearchPathCallback callback) {
        if (caseInsensitiveEquals(source, destination)) return -1;

        ObjPtr<SearchPathQueue> bfsQueue = new ObjPtr<SearchPathQueue>();
        SearchPathQueue queue = new SearchPathQueue();
        queue.encoding = source;

        ObjPtr<SearchPathQueue> bfsLastQueue = queue.next;
        bfsQueue.p = queue;

        CaseInsensitiveBytesHash<byte[]> bfsVisited = new CaseInsensitiveBytesHash<byte[]>();
        bfsVisited.put(source, NULL_STRING);

        while (bfsQueue.p != null) {
            queue = bfsQueue.p;
            bfsQueue.p = queue.next.p;
            if (bfsQueue.p == null) bfsLastQueue = bfsQueue;

            CaseInsensitiveBytesHash<Entry> table2 = transcoders.get(queue.encoding);
            if (table2 == null) continue;

            Entry entry = table2.get(destination);
            if (entry != null) {
                bfsVisited.put(destination, queue.encoding);
                byte[] enc = destination;
                int depth, pathLength = 0;
                while (true) {
                    byte[] tmp = bfsVisited.get(enc);
                    if (tmp == NULL_STRING) break;
                    pathLength++;
                    enc = tmp;

                }
                depth = pathLength;
                enc = destination;
                while (true) {
                    byte[] tmp = bfsVisited.get(enc);
                    if (tmp == NULL_STRING) break;
                    callback.call(tmp, enc, --depth);
                    enc = tmp;
                }
                return pathLength;
            } else {
                byte[] bfsBaseEnc = queue.encoding;

                for (Hash.HashEntry<Entry> o : (Iterable<Hash.HashEntry<Entry>>)table2.entryIterator()) {
                    CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<Entry> e = (CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<Entry>) o;
                    byte[] dname = e.bytes;
                    if (bfsVisited.get(dname) == null) {
                        SearchPathQueue q = new SearchPathQueue();
                        q.encoding = dname;
                        q.next.p = null;
                        bfsLastQueue.p = q;
                        bfsLastQueue = q.next;

                        bfsVisited.putDirect(dname, bfsBaseEnc);
                    }
                }

                bfsBaseEnc = null;
            }

        } // while
        return -1;
    }

    /* rb_econv_alloc */
    public static EConv alloc(int n) {
        return new EConv(n);
    }

    /* rb_econv_open_by_transcoder_entries */
    private static EConv openByTranscoderEntries(int n, Entry[] entries) {
        EConv econv = new EConv(n);

        for (int i = 0; i < n; i++) {
            Transcoder transcoder = entries[i].getTranscoder();
            econv.addTranscoderAt(transcoder, econv.numTranscoders);
        }
        return econv;
    }

    /* rb_econv_open0 */
    private static EConv open0(byte[] source, byte[] destination, int ecflags) {
        // final Encoding senc = EncodingDB.getEncodings().get(source).getEncoding();
        // final Encoding denc = EncodingDB.getEncodings().get(destination).getEncoding();

        final int numTrans;
        final Entry[] entries;
        if (source.length == 0 && destination.length == 0) {
            numTrans = 0;
            entries = null;
        } else {
            final ObjPtr<Entry[]> lentries = new ObjPtr<Entry[]>();
            numTrans = searchPath(source, destination, new SearchPathCallback() {
                int additional = 0;

                public void call(byte[] source, byte[] destination, int depth) {
                    if (lentries.p == null) lentries.p = new Entry[depth + 1 + additional];
                    lentries.p[depth] = getEntry(source, destination);

                }
            });
            entries = lentries.p;
            if (numTrans < 0) {
                return null;
            }
        }

        EConv ec = openByTranscoderEntries(numTrans, entries);
        if (ec == null) return null;

        ec.flags = ecflags;
        ec.source = source;
        ec.destination = destination;
        return ec;
    }

    /* decorator_names */
    public static int decoratorNames(int ecflags, byte[][] decorators) {
        switch (ecflags & NEWLINE_DECORATOR_MASK) {
        case UNIVERSAL_NEWLINE_DECORATOR:
        case CRLF_NEWLINE_DECORATOR:
        case CR_NEWLINE_DECORATOR:
        case 0:
            break;
        default:
            return -1;
        }

        if (((ecflags & XML_TEXT_DECORATOR) != 0) && ((ecflags & XML_ATTR_CONTENT_DECORATOR) != 0)) return -1;

        int numDecorators = 0;

        if ((ecflags & XML_TEXT_DECORATOR) != 0) decorators[numDecorators++] = "xml_text_escape".getBytes();
        if ((ecflags & XML_ATTR_CONTENT_DECORATOR) != 0) decorators[numDecorators++] = "xml_attr_content_escape".getBytes();
        if ((ecflags & XML_ATTR_QUOTE_DECORATOR) != 0) decorators[numDecorators++] = "xml_attr_quote".getBytes();

        if ((ecflags & CRLF_NEWLINE_DECORATOR) != 0) decorators[numDecorators++] = "crlf_newline".getBytes();
        if ((ecflags & CR_NEWLINE_DECORATOR) != 0) decorators[numDecorators++] = "cr_newline".getBytes();
        if ((ecflags & UNIVERSAL_NEWLINE_DECORATOR) != 0) decorators[numDecorators++] = "universal_newline".getBytes();

        return numDecorators;
    }

    public static EConv open(String source, String destination, int ecflags) {
        return open(source.getBytes(), destination.getBytes(), ecflags);
    }

    /* rb_econv_open */
    public static EConv open(byte[] source, byte[] destination, int ecflags) {
        byte[][] decorators = new byte[MAX_ECFLAGS_DECORATORS][];

        int numDecorators = decoratorNames(ecflags, decorators);
        if (numDecorators == -1) return null;

        EConv ec = open0(source, destination, ecflags & ERROR_HANDLER_MASK);
        if (ec == null) return null;

        for (int i = 0; i < numDecorators; i++) {
            if (!ec.decorateAtLast(decorators[i])) {
                ec.close();
                return null;
            }
        }

        ec.flags |= ecflags & ~ERROR_HANDLER_MASK;
        return ec;
    }

    /* rb_econv_asciicompat_encoding */// ?? to transcoderdb ?
    static byte[] asciiCompatibleEncoding(byte[] asciiCompatName) {
        CaseInsensitiveBytesHash<TranscoderDB.Entry> dTable = TranscoderDB.transcoders.get(asciiCompatName);
        if (dTable == null || dTable.size() != 1) return null;

        byte[] asciiCN = null;
        for (Entry e : dTable) {
            if (!EConv.decorator(e.source, e.destination)) {
                Transcoder transcoder = e.getTranscoder();
                if (transcoder != null && transcoder.compatibility.isDecoder()) {
                    asciiCN = transcoder.destination;
                    break;
                }
            }
        }
        return asciiCN;
    }

    static {
        TranscoderList.load();
    }
}
