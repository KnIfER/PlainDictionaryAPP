//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.knziha.plod.dictionary.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ReusableByteOutputStream extends OutputStream {
    protected byte[] buf;
    protected int count;

    public byte[] data(){
        return buf;
    }

    public ReusableByteOutputStream() {
        this(1024);
    }

	public ReusableByteOutputStream(int size) {
		this.count = 0;
		this.buf = new byte[size];
	}

	public ReusableByteOutputStream(byte[] buf, int size) {
		this.count = 0;
		this.buf = buf;
		if(buf==null || buf.length!=size)
			this.buf = new byte[size];
	}

    public void write(InputStream in, boolean breakOnZeroAvail) throws IOException {
        int cap;
        if (in instanceof ByteArrayInputStream) {
            cap = in.available();
            this.ensureCapacity(count + cap);
            this.count += in.read(this.buf, this.count, cap);
        } else {
            while(true) {
                cap = this.buf.length - this.count;
                int sz = in.read(this.buf, this.count, cap);
                if (sz < 0) {
                    return;
                }

                this.count += sz;
                if (cap == sz) {
                	int avail = in.available();
                	if (breakOnZeroAvail && avail==0) break;
                    this.ensureCapacity(this.count+Math.max(64, in.available()));
                }
            }
        }
    }
	
	public void write(byte[] b, int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0) ||
				((off + len) - b.length > 0)) {
			throw new IndexOutOfBoundsException();
		}
		this.ensureCapacity(count + len);
		System.arraycopy(b, off, this.buf, this.count, len);
		this.count += len;
	}
	
	public void write(byte[] b) {
		this.write(b, 0, b.length);
	}
	
	
	public void write(int b) {
        this.ensureCapacity(count + 1);
        this.buf[this.count] = (byte)b;
        ++this.count;
    }

    public void ensureCapacity(int minCapacity) {
        //int newcount = space + this.count;
        //if (newcount > this.buf.length) {
        //    byte[] newbuf = new byte[Math.max(this.buf.length << 1, newcount)];
        //    System.arraycopy(this.buf, 0, newbuf, 0, this.count);
        //    this.buf = newbuf;
        //}
		// overflow-conscious code
		if (minCapacity - buf.length > 0)
			grow(minCapacity);
    }
	
	
	/**
	 * The maximum size of array to allocate.
	 * Some VMs reserve some header words in an array.
	 * Attempts to allocate larger arrays may result in
	 * OutOfMemoryError: Requested array size exceeds VM limit
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	
	/**
	 * Increases the capacity to ensure that it can hold at least the
	 * number of elements specified by the minimum capacity argument.
	 *
	 * @param minCapacity the desired minimum capacity
	 */
	private void grow(int minCapacity) {
		// overflow-conscious code
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		if (newCapacity - MAX_ARRAY_SIZE > 0)
			newCapacity = hugeCapacity(minCapacity);
		buf = Arrays.copyOf(buf, newCapacity);
	}
	
	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		return (minCapacity > MAX_ARRAY_SIZE) ?
				Integer.MAX_VALUE :
				MAX_ARRAY_SIZE;
	}
	
    public void reset() {
        this.count = 0;
    }

    /** @deprecated */
    public byte[] toByteArray() {
        byte[] newbuf = new byte[this.count];
        System.arraycopy(this.buf, 0, newbuf, 0, this.count);
        return newbuf;
    }

    public int size() {
        return this.count;
    }

    public String toString() {
        return new String(this.buf, 0, this.count);
    }

    public void close() {
    }

    public byte[] getBytes() {
        return this.buf;
    }

    public byte[] getArray(int planSize) {
		//CMN.Log("getBytesLegal::", this.buf.length==count);
        return /*this.buf.length==count||this.buf.length==planSize?this.buf:*/toByteArray();
    }

    public int getCount() {
        return this.count;
    }

	public void precede(int add) {
		if(count+add<buf.length){
			count+=add;
		}
	}

	public void recess(int sub) {
		if(count-sub>0){
			count-=sub;
		}
	}
}
