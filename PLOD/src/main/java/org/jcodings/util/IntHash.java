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

public class IntHash<V> extends Hash<V> {

    public IntHash() {
        super();
    }

    public IntHash(int size) {
        super(size);
    }

    protected void init() {
        head = new IntHashEntry<V>();
    }

    public static final class IntHashEntry<V> extends HashEntry<V> {
        public IntHashEntry(int hash, HashEntry<V> next, V value, HashEntry<V> head) {
            super(hash, next, value, head);
        }

        public IntHashEntry() {
            super();
        }
    }

    public V put(int key, V value) {
        checkResize();
        int hash = hashValue(key);
        int i = bucketIndex(hash, table.length);

        for (IntHashEntry<V> entry = (IntHashEntry<V>)table[i]; entry != null; entry = (IntHashEntry<V>)entry.next) {
            if (entry.hash == hash) {
                entry.value = value;
                return value;
            }
        }

        table[i] = new IntHashEntry<V>(hash, table[i], value, head);
        size++;
        return null;
    }

    public void putDirect(int key, V value) {
        checkResize();
        final int hash = hashValue(key);
        final int i = bucketIndex(hash, table.length);
        table[i] = new IntHashEntry<V>(hash, table[i], value, head);
        size++;
    }

    public V get(int key) {
        int hash = hashValue(key);
        for (IntHashEntry<V> entry = (IntHashEntry<V>)table[bucketIndex(hash, table.length)]; entry != null; entry = (IntHashEntry<V>)entry.next) {
            if (entry.hash == hash) return entry.value;
        }
        return null;
    }

    public V delete(int key) {
        int hash = hashValue(key);
        int i = bucketIndex(hash, table.length);

        IntHashEntry<V> entry = (IntHashEntry<V>)table[i];

        if (entry == null) return null;

        if (entry.hash == hash) {
            table[i] = entry.next;
            size--;
            entry.remove();
            return entry.value;
        }

        for (; entry.next != null; entry = (IntHashEntry<V>)entry.next) {
            HashEntry<V> tmp = entry.next;
            if (tmp.hash == hash && entry.equals(key)) {
                entry.next = entry.next.next;
                size--;
                tmp.remove();
                return tmp.value;
            }
        }
        return null;
    }
}
