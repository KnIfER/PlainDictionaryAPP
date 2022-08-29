package com.knziha.plod.searchtasks.lucene;

import com.knziha.plod.plaindict.CMN;
import com.knziha.text.BreakIteratorHelper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import java.io.IOException;
import java.io.Reader;
import java.text.CharacterIterator;
import java.util.Iterator;

public class DemoAnalyzer1 extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer source = new WhiteSpaceTokenizer(reader);
        TokenStream filter = new WhileSpaceTokenFiler(source);
        return new TokenStreamComponents(source,filter);
    }
	
	//分词器:一个单词一个单词去读，有空格进行分词处理
    static class WhiteSpaceTokenizer extends  Tokenizer {
        CharAttribute attr = this.addAttribute(CharAttribute.class);
        char[] buffer = new char[255];
        String doc;
        int c ;
        int readLen = 0;
        int docLen = 0;
        BreakIteratorHelper breakIterator;
        StringBuffer stringBuffer = new StringBuffer();
		
		protected WhiteSpaceTokenizer(Reader input) {
			super(input);
		}
		
		/**
		 * Creates an {@link Iterator} of all the characters in the reader.
		 */
		private CharacterIterator createCharacterIterator(Reader reader) {
			return new CharacterIterator() {
				@Override
				public char first() {
					return 0;
				}
				@Override
				public char last() {
					return 0;
				}
				@Override
				public char current() {
					return 0;
				}
				@Override
				public char next() {
					try {
						return (char) reader.read();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				@Override
				public char previous() {
					return 0;
				}
				@Override
				public char setIndex(int position) {
					return 0;
				}
				@Override
				public int getBeginIndex() {
					return 0;
				}
				@Override
				public int getEndIndex() {
					return 0;
				}
				@Override
				public int getIndex() {
					return 0;
				}
				@Override
				public Object clone() {
					return null;
				}
			};
		}
		
		@Override
        public boolean incrementToken() throws IOException{
            clearAttributes();
            if (breakIterator==null) {
                breakIterator = new BreakIteratorHelper();
                //docLen = input.read(buffer);
                doc = new String(buffer, 0, docLen);
                CMN.Log("--- doc::", doc);
                //breakIterator.setText(createCharacterIterator(input));
                breakIterator.setText(text);
            }
            int now=readLen, nxt = breakIterator.following(now);
            if (nxt==-1) {
				CMN.Log("--- 读取完毕::", now, nxt);
                return false;
            }
            attr.setData(text.substring(now, nxt));
            readLen = nxt;
            return true;
        }
    }
    
    static class WhileSpaceTokenFiler extends TokenFilter {
        CharAttribute attr = this.addAttribute((CharAttribute.class));
        public WhileSpaceTokenFiler(TokenStream source){
            super(source);
        }
        @Override
        public boolean incrementToken() throws IOException{
            boolean res = this.input.incrementToken();
            if(res){
            }
            return res;
        }
    }
    
    //定义属性对象
    public interface CharAttribute extends Attribute{
        void setData(String substring);
    }
    
    public static class CharAttributeImpl extends AttributeImpl implements CharAttribute {
        //设置长度
        public String data;
        public CharAttributeImpl(){}
        @Override
        public void clear() {
        }
        @Override
        public void reflectWith(AttributeReflector reflector) {
        }
        @Override
        public void copyTo(AttributeImpl target) {

        }
        @Override
        public void setData(String substring) {
            data = substring;
        }
    }
	
	static String text = "自定义中文分词器， 中国高技术产业引资持续增长！I Love You ， Baby ! How are you ";
	
    public static void test(){
		CMN.Log(text);
        Analyzer analyzer = new DemoAnalyzer1();
        try {
            TokenStream tokenStream = analyzer.tokenStream("f",text);
            CharAttributeImpl attr = (CharAttributeImpl) tokenStream.addAttribute(CharAttribute.class);
            tokenStream.reset();
			String result = "";
            while (tokenStream.incrementToken()){
				result += attr.data+"|";
            }
			CMN.Log(result);
            tokenStream.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}