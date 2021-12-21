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

import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.CaseInsensitiveBytesHash;

public final class EncodingDB {
    public static final class Entry {
        private static int count;

        private final Entry base;
        private Encoding encoding;
        private final String encodingClass;
        private final int index;
        private final boolean isDummy;
        private final byte[]name;

        private Entry (byte[]name, String encodingClass, Entry base, boolean isDummy) {
            this.name = name;
            this.encodingClass = encodingClass;
            this.base = base;
            this.isDummy = isDummy;
            index = count++;
        }

        // declare
        Entry(String encodingClass) {
            this(null, encodingClass, null, false);
        }

        // replicate
        Entry(byte[]name, Entry base) {
            this(name, base.encodingClass, base, false);
        }

        // dummy
        Entry(byte[]name) {
            this(name, ascii.encodingClass, ascii, true);
        }

        // dummy with base
        Entry(byte[]name, Entry base, boolean dummy) {
            this(name, base.encodingClass, base, dummy);
        }

        @Override
        public int hashCode() {
            return encodingClass.hashCode();
        }

        public Entry getBase() {
            return base;
        }

        public Encoding getEncoding() {
            if (encoding == null) {
                if (name == null) {
                    encoding = EncodingList.getInstance(encodingClass);
                } else {
                    encoding = EncodingList.getInstance(encodingClass).replicate(name);
                    if (isDummy) encoding.setDummy();
                }
            }
            return encoding;
        }

        public String getEncodingClass() {
            return encodingClass;
        }

        public int getIndex() {
            return index;
        }

        public boolean isDummy() {
            return isDummy;
        }
    }

    static Entry ascii;
    static final CaseInsensitiveBytesHash<Entry> encodings = new CaseInsensitiveBytesHash<Entry>(50);
    static final CaseInsensitiveBytesHash<Entry> aliases = new CaseInsensitiveBytesHash<Entry>(150);

    public static final CaseInsensitiveBytesHash<Entry> getEncodings() {
        return encodings;
    }

    public static final CaseInsensitiveBytesHash<Entry> getAliases() {
        return aliases;
    }

    public static void declare(String name, String encodingClass) {
        byte[]bytes = name.getBytes();
        if (encodings.get(bytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALREADY_REGISTERED, name);
        encodings.putDirect(bytes, new Entry(encodingClass));
    }

    public static void alias(String alias, String original) {
        byte[]origBytes = original.getBytes();
        Entry originalEntry = encodings.get(origBytes);
        if (originalEntry == null) throw new InternalException(ErrorMessages.ERR_NO_SUCH_ENCODNG, original);
        byte[]aliasBytes = alias.getBytes();
        if (aliases.get(aliasBytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALIAS_ALREADY_REGISTERED, alias);
        aliases.putDirect(aliasBytes, originalEntry);
    }

    public static void replicate(String replica, String original) {
        byte[]origBytes = original.getBytes();
        Entry originalEntry = encodings.get(origBytes);
        if (originalEntry == null) throw new InternalException(ErrorMessages.ERR_NO_SUCH_ENCODNG, original);
        finishReplica(replica, originalEntry.isDummy, originalEntry);
    }

    private static void replicate(String replica, String original, boolean dummy) {
        byte[]origBytes = original.getBytes();
        Entry originalEntry = encodings.get(origBytes);
        if (originalEntry == null) throw new InternalException(ErrorMessages.ERR_NO_SUCH_ENCODNG, original);
        finishReplica(replica, dummy, originalEntry);
    }

    private static void finishReplica(String replica, boolean dummy, Entry originalEntry) {
        byte[]replicaBytes = replica.getBytes();
        if (encodings.get(replicaBytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_REPLICA_ALREADY_REGISTERED, replica);
        encodings.putDirect(replicaBytes, new Entry(replicaBytes, originalEntry, dummy));
    }

    public static void set_base(String name, String original) {
    }

    public static Entry dummy(byte[] bytes) {
        if (encodings.get(bytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALREADY_REGISTERED, new String(bytes));
        Entry entry = new Entry(bytes);
        encodings.putDirect(bytes, entry);
        return entry;
    }

    public static void dummy(String name) {
        dummy(name.getBytes());
    }

    public static void dummy_unicode(String replica) {
        replicate(replica, replica + "BE", true);
    }

    static {
        EncodingList.load();
    }
}
