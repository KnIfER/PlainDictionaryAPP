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
package org.jcodings.util;

import static org.jcodings.ascii.AsciiTables.ToLowerCaseTable;

public final class CaseInsensitiveBytesHash<V> extends Hash<V>{

    public CaseInsensitiveBytesHash() {
        super();
    }

    public CaseInsensitiveBytesHash(int size) {
        super(size);
    }

    protected void init() {
        head = new CaseInsensitiveBytesHashEntry<V>();
    }

    public final static class CaseInsensitiveBytesHashEntry<V> extends HashEntry<V> {
        public final byte[]bytes;
        public final int p;
        public final int end;

        public CaseInsensitiveBytesHashEntry(int hash, HashEntry<V> next, V value, byte[]bytes, int p, int end, HashEntry<V> head) {
            super(hash, next, value, head);
            this.bytes = bytes;
            this.p = p;
            this.end = end;
        }

        public CaseInsensitiveBytesHashEntry() {
            super();
            bytes = null;
            p = end = 0;
        }

        public boolean equals(byte[]bytes, int p, int end) {
            return caseInsensitiveEquals(this.bytes, this.p, this.end, bytes, p, end);
        }
    }

    public static int hashCode(byte[]bytes, int p, int end) {
        int key = 0;
        while (p < end) key = ((key << 16) + (key << 6) - key) + (int)(ToLowerCaseTable[bytes[p++] & 0xff]); // & 0xff ? we have to match jruby string hash
        key = key + (key >> 5);
        return key;
    }

    public V put(byte[]bytes, V value) {
        return put(bytes, 0, bytes.length, value);
    }

    public V put(byte[]bytes, int p, int end, V value) {
        checkResize();
        int hash = hashValue(hashCode(bytes, p, end));
        int i = bucketIndex(hash, table.length);

        for (CaseInsensitiveBytesHashEntry<V> entry = (CaseInsensitiveBytesHashEntry<V>)table[i]; entry != null; entry = (CaseInsensitiveBytesHashEntry<V>)entry.next) {
            if (entry.hash == hash && entry.equals(bytes, p, end)) {
                entry.value = value;
                return value;
            }
        }

        table[i] = new CaseInsensitiveBytesHashEntry<V>(hash, table[i], value, bytes, p, end, head);
        size++;
        return null;
    }

    public void putDirect(byte[]bytes, V value) {
        putDirect(bytes, 0, bytes.length, value);
    }

    public void putDirect(byte[]bytes, int p, int end, V value) {
        checkResize();
        final int hash = hashValue(hashCode(bytes, p, end));
        final int i = bucketIndex(hash, table.length);
        table[i] = new CaseInsensitiveBytesHashEntry<V>(hash, table[i], value, bytes, p, end, head);
        size++;
    }

    public V get(byte[]bytes) {
        return get(bytes, 0, bytes.length);
    }

    public V get(byte[]bytes, int p, int end) {
        int hash = hashValue(hashCode(bytes, p, end));
         for (CaseInsensitiveBytesHashEntry<V> entry = (CaseInsensitiveBytesHashEntry<V>)table[bucketIndex(hash, table.length)]; entry != null; entry = (CaseInsensitiveBytesHashEntry<V>)entry.next) {
             if (entry.hash == hash && entry.equals(bytes, p, end)) return entry.value;
         }
         return null;
    }

    public V delete(byte[]bytes) {
        return delete(bytes, 0, bytes.length);
    }

    public V delete(byte[]bytes, int p, int end) {
        int hash = hashValue(hashCode(bytes, p, end));
        int i = bucketIndex(hash, table.length);

        CaseInsensitiveBytesHashEntry<V> entry = (CaseInsensitiveBytesHashEntry<V>)table[i];

        if (entry == null) return null;

        if (entry.hash == hash && entry.equals(bytes, p, end)) {
            table[i] = entry.next;
            size--;
            entry.remove();
            return entry.value;
        }

        for (; entry.next != null; entry = (CaseInsensitiveBytesHashEntry<V>)entry.next) {
            HashEntry<V> tmp = entry.next;
            if (tmp.hash == hash && entry.equals(bytes, p, end)) {
                entry.next = entry.next.next;
                size--;
                tmp.remove();
                return tmp.value;
            }
        }
        return null;
    }

    @Override
    public CaseInsensitiveBytesHashEntryIterator entryIterator() {
        return new CaseInsensitiveBytesHashEntryIterator();
    }

    public class CaseInsensitiveBytesHashEntryIterator extends HashEntryIterator {
        @Override
        public CaseInsensitiveBytesHashEntry<V> next() {
            return (CaseInsensitiveBytesHashEntry<V>)super.next();
        }



//        @Override
//        public Iterator<CaseInsensitiveBytesHashEntryIterator> iterator() {
//            return (CaseInsensitiveBytesHashEntryIterator)this;
//        }
//        @Override
//        public Iterator<HashEntry<V>> iterator() {
//            return this;
//        }

    }

    public static boolean caseInsensitiveEquals(byte[]bytes, int p, int end, byte[]oBytes, int oP, int oEnd) {
        if (oEnd - oP != end - p) return false;
        if (oBytes == bytes) return true;
        int q = oP;
        while (q < oEnd) if (ToLowerCaseTable[oBytes[q++] & 0xff] != ToLowerCaseTable[bytes[p++] & 0xff]) return false;
        return true;
    }

    public static boolean caseInsensitiveEquals(byte[]bytes, byte[] oBytes) {
        return caseInsensitiveEquals(bytes, 0, bytes.length, oBytes, 0, oBytes.length);
    }

}
