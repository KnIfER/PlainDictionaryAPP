package com.knziha.plod.dictionarymodels;

import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.plaindict.CMN;

import org.apache.commons.lang3.ArrayUtils;
import org.knziha.metaline.Metaline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class DictInputStream extends InputStream {
	protected byte[] data = ArrayUtils.EMPTY_BYTE_ARRAY;
	protected final GZIPDictReader dictReader;
	protected byte[] buffer;
	protected Inflater inf = new Inflater(true);
	protected int blockIndex;
	protected int blockSize;
	protected int pos;
	InputStream linearInputStream;
	
	public static class GZIPDictReader {
		byte[] buffer_cache = new byte[64*1024];
		int buffer_cache_index=-1;
		/** 文件。 */
		final File f;
		/** GZIP标志。 */
		long flag;
		@Metaline(flagPos=0) boolean isText(){flag=flag; throw new RuntimeException();}
		@Metaline(flagPos=1) boolean hasCRC(){flag=flag; throw new RuntimeException();}
		@Metaline(flagPos=2) boolean hasExtra(){flag=flag; throw new RuntimeException();}
		@Metaline(flagPos=3) boolean hasName(){flag=flag; throw new RuntimeException();}
		@Metaline(flagPos=4) boolean hasComment(){flag=flag; throw new RuntimeException();}
		/** 每个块解压后的大小。 */
		final int blockDecLength;
		/** 分块总数。 */
		final int blockCount;
		/** 每个块各自的偏移量（压缩状态下），以第一块为零记起。 */
		final long[] blockOffsets;
		/** GZIP文件头大小。 */
		final long blockOffset;
		/** 所有块加起来的总大小（压缩状态下）。 */
		long totalLength;
		public GZIPDictReader(File f) throws IOException {
			this.f = f;
			// |ID1|ID2| CM |FLG|     MTIME     |XFL|OS | (more-->)
			//  1    1   1   1           4        1   1
			// 0x1F 0x8B
			InputStream data_in = new FileInputStream(f);
			BU.SafeSkipReam(data_in, 3);
			this.flag = data_in.read();
			if (!hasExtra()) throw new IOException("DictZip Format Invalid");
			int blockOffset = 10;
			BU.SafeSkipReam(data_in, 6);
			int xlen = BU.readShortLE(data_in);
			CMN.Log(xlen);
			//byte[] buffer = new byte[xlen];
			//data_in.read(buffer);
			//CMN.Log("extra::", new String(buffer));
			// |ID1|ID2| LEN |   DATA  |
			int ID1 = data_in.read(); // ID1
			int ID2 = data_in.read(); // ID2
			//CMN.Log(ID1, ID2, (int)'R', (int)'A');
			int LEN = BU.readShortLE(data_in);
			int VER = BU.readShortLE(data_in);
			// blockSize<64K，但不一定是偶数，所以无法直接用作UTF16的分块依据。
			blockDecLength =  BU.readShortLE(data_in);
			blockCount = BU.readShortLE(data_in);
			CMN.Log("blockSize="+ blockDecLength, "blockCount="+blockCount);
			// blockIndexes，store uncompressed size
			blockOffsets = new long[blockCount];
			long total=0, accumulator=0;
			for (int i = 0; i < blockCount; i++) {
				blockOffsets[i] = accumulator;
				accumulator += BU.readShortLE(data_in);
			}
			totalLength = accumulator;
			blockOffset += 4 + LEN;
			CMN.Log("block#", total, total/blockCount, blockCount*2, LEN);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int value;
			if (hasName()) {
				bos.reset();
				while((value=data_in.read())!=0) {
					bos.write(value);
					blockOffset++;
				}
				CMN.Log("name="+new String(bos.toByteArray()));
				blockOffset++;
			}
			if (hasComment()) {
				bos.reset();
				while((value=data_in.read())!=0) {
					bos.write(value);
					blockOffset++;
				}
				CMN.Log("words="+new String(bos.toByteArray()));
				blockOffset++;
			}
			if (hasCRC()) {
				BU.readShortLE(data_in);
				blockOffset+=2;
			}
			this.blockOffset = blockOffset+2;
//			RandomAccessFile raf = new RandomAccessFile(f, "r");
//			raf.seek(blockOffset+2);
//			byte[] data = new byte[blockOffsets[0]];
//			raf.read(data);
//			//data_in.read(data);
////				CMN.Log(new String(BU.zlib_decompress(data,0,data.length), StandardCharsets.UTF_16LE));
//
//			//InflaterInputStream input = new InflaterInputStream(new ByteArrayInputStream(data), new Inflater(true), 8192);
//			byte[] uncompressed = new byte[64*1024];
//
////				byte[] uncompressed = new byte[blockSize];
////				input.read(uncompressed, 0, blockSize);
//
//			try {
//				Inflater inf = new Inflater(true);
//				inf.setInput(data);
//				inf.inflate(uncompressed, 0, blockSize);
//			} catch (DataFormatException e) {
//				CMN.Log(e);
//			}
//
//			CMN.Log(new String(uncompressed, 0, uncompressed.length, StandardCharsets.UTF_16LE));
//			//BU.printFile(uncompressed, "/sdcard/tmpBlck"+0);
			data_in.close();
		}
		
		/** fileOffset解压后的文件偏移量。 */
		public long getReadStartForOffset(long fileOffset) {
			int blockIndex = (int) (fileOffset/blockDecLength);
			return blockOffsets[blockIndex];
		}
		
		InputStream OpenStreamAt(long fileOffset) throws IOException {
			FileInputStream fin = new FileInputStream(f);
			return BU.SafeSkipReam(fin, blockOffset+getReadStartForOffset(fileOffset));
		}
	}
	
	public static int findBlockIndexForOffset(long[] data, long val, int start, int end) {
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			return val-data[start + len - 1]>0
					? findBlockIndexForOffset(data, val,start+len,end)
					: findBlockIndexForOffset(data, val,start,start+len);
		} else {
			return start;
		}
	}
	
	boolean delayedStream;
	long delayedStreamOpenOffset;
	
	public DictInputStream(GZIPDictReader dictReader, long fileOffset) throws IOException {
		this.dictReader = dictReader;
		blockIndex = (int) (fileOffset/dictReader.blockDecLength);
		long tid = Thread.currentThread().getId();
		if(tid==CMN.mid) { // false
			buffer = dictReader.buffer_cache;
			if(blockIndex==dictReader.buffer_cache_index) {
				if(false) SU.Log("lazy::真的好懒哦！");
				delayedStream=true;
				blockSize=dictReader.blockDecLength;
				delayedStreamOpenOffset=fileOffset+dictReader.blockDecLength;
			}
		} else {
			buffer = new byte[64*1024];
		}
		if(!delayedStream) {
			linearInputStream = dictReader.OpenStreamAt(fileOffset);
			//CMN.Log("DictInputStream::", blockIndex, dictReader.blockOffsets[blockIndex], fileOffset);
			blockIndex--;
			fillNewBlock();
		}
		pos = (int) (fileOffset-blockIndex*dictReader.blockDecLength);
		//CMN.Log("DictInputStream::", pos+"/"+blockSize);
	}
	
	@Override
	public int read() throws IOException {
		if (pos >= blockSize) {
			fillNewBlock();
			if (pos >= blockSize) return -1;
		}
		return buffer[pos++] & 0xff;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		//SU.Log("read::", blockIndex, off, len, linearInputStream, pos);
		if((off|(b.length-(off+len)))<0) {
			throw new IndexOutOfBoundsException(b.length+","+off+","+len);
		}
		int read=0, maxRead;
		while(len>read) {
			maxRead = readMax(b, off+read, len-read);
			if(maxRead<=0) {
				if(read==0) return maxRead;
				break;
			}
			read += maxRead;
			if(available()<=0)
				break;
		}
		return read;
	}
	
	private int readMax(byte[] b, int off, int len) throws IOException {
		int available = blockSize-pos;
		if(available<=0) {
			fillNewBlock();
			available = blockSize-pos;
			if(available<=0) return -1;
		}
		//SU.Log("readMax::", blockIndex, off, len, linearInputStream, pos);
		if(len>available) len = available;
		System.arraycopy(buffer, pos, b, off, len);
		pos += len;
		return len;
	}
	
	@Override
	public int available() throws IOException {
		return (dictReader.blockCount - blockIndex)*dictReader.blockDecLength - pos;
	}
	
	public int tellPosition() {
		return blockIndex*dictReader.blockDecLength + pos;
	}
	
	/** 读取一整块新数据至buffer */
	void fillNewBlock() throws IOException {
		pos = 0;
		blockSize = 0;
		blockIndex++;
		//CMN.Log("filling new block::", blockIndex, linearInputStream, delayedStreamOpenOffset);
		if(blockIndex<dictReader.blockCount) {
			long blockOffset = dictReader.blockOffsets[blockIndex];
			long blockOffsetNxt = blockIndex+1<dictReader.blockCount?dictReader.blockOffsets[blockIndex+1]:dictReader.totalLength;
			int compressedLength = (int) (blockOffsetNxt-blockOffset);
			if(data.length<compressedLength) {
				data = new byte[(int) (compressedLength*1.2)];
			}
			//CMN.Log("compressedLength::@"+blockIndex, compressedLength);
			if(delayedStream) {
				if(linearInputStream==null) {
					if(false) SU.Log("lazy::给我勤快一点！");
					linearInputStream = dictReader.OpenStreamAt(delayedStreamOpenOffset);
				}
				delayedStream=false;
			}
			if(linearInputStream!=null) {
				// todo while-read
				linearInputStream.read(data, 0, compressedLength);
				inf.setInput(data, 0, compressedLength);
				try {
					inf.inflate(buffer, 0, blockSize = dictReader.blockDecLength);
					if (buffer==dictReader.buffer_cache) {
						dictReader.buffer_cache_index=blockIndex;
					}
				} catch (DataFormatException e) {
					throw new IOException(e);
				}
			}
		}
	}
}
