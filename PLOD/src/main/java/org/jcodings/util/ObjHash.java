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

public final class ObjHash<K, V> extends Hash<V> {

    protected void init() {
        head = new ObjHashEntry<K, V>();
    }

    public final static class ObjHashEntry<K, V> extends HashEntry<V> {
        public final K key;

        public ObjHashEntry(int hash, HashEntry<V> next, V value, K key, HashEntry<V> head) {
            super(hash, next, value, head);
            this.key = key;
        }

        public ObjHashEntry() {
            super();
            key = null;
        }

        public boolean equals(Object key) {
            if (this.key == key) return true;
            return this.key.equals(key);
        }
    }

    public V put(K key, V value) {
        checkResize();
        int hash = hashValue(key.hashCode());
        int i = bucketIndex(hash, table.length);

        K k;
        for (ObjHashEntry<K, V> entry = (ObjHashEntry<K, V>)table[i]; entry != null; entry = (ObjHashEntry<K, V>)entry.next) {
            if (entry.hash == hash && ((k = entry.key) == key || key.equals(k))) {
                entry.value = value;
                return value;
            }
        }

        table[i] = new ObjHashEntry<K, V>(hash, table[i], value, key, head);
        size++;
        return null;
    }

    public void putDirect(K key, V value) {
        checkResize();
        final int hash = hashValue(key.hashCode());
        final int i = bucketIndex(hash, table.length);
        table[i] = new ObjHashEntry<K, V>(hash, table[i], value, key, head);
        size++;
    }


    public V get(K key) {
        int hash = hashValue(key.hashCode());
        K k;
        for (ObjHashEntry<K, V> entry = (ObjHashEntry<K, V>)table[bucketIndex(hash, table.length)]; entry != null; entry = (ObjHashEntry<K, V>)entry.next) {
            if (entry.hash == hash && ((k = entry.key) == key || key.equals(k))) return entry.value;
        }
        return null;
    }

    public V delete(K key) {
        int hash = hashValue(key.hashCode());
        int i = bucketIndex(hash, table.length);

        ObjHashEntry<K, V> entry = (ObjHashEntry<K, V>)table[i];

        if (entry == null) return null;

        K k;
        if (entry.hash == hash && ((k = entry.key) == key || key.equals(k))) {
            table[i] = entry.next;
            size--;
            entry.remove();
            return entry.value;
        }

        for (; entry.next != null; entry = (ObjHashEntry<K, V>)entry.next) {
            HashEntry<V> tmp = entry.next;
            if (tmp.hash == hash && ((k = entry.key) == key || key.equals(k))) {
                entry.next = entry.next.next;
                size--;
                tmp.remove();
                return tmp.value;
            }
        }
        return null;
    }

}
