package com.knziha.plod.searchtasks.lucene;

import com.knziha.text.BreakIteratorHelper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKBigramFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.Reader;

public final class WordBreakFilter extends TokenFilter {
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
	private final PositionLengthAttribute posLengthAtt = addAttribute(PositionLengthAttribute.class);
	public Analyzer.TokenStreamComponents component;

	public WordBreakFilter(TokenStream in) {
		super(in);
	}

	String text;
	BreakIteratorHelper breakIterator = new BreakIteratorHelper();
	int start = 0;
	@Override
	public boolean incrementToken() throws IOException {
		//CMN.Log("incrementToken", input);
		if(component.text!=null) {
			breakIterator.setText(text = component.text);
			component.text = null;
			start = 0;
		}
		int end = breakIterator.next();
		while (end != java.text.BreakIterator.DONE) {
			String term = text.substring(start, end).trim();
			int len = term.length();
			boolean deBigram = true;
			if (len>1 && len<termAtt.buffer().length) {
				//CMN.Log("term::", term);
				termAtt.setLength(len);
				char c;
				for (int i = 0; i < len; i++) {
					c = termAtt.buffer()[i] = term.charAt(i);
					if (deBigram 
							&& isBigram(c)
					) {
						deBigram = false;
					}
				}
			}
			if (!deBigram) {
				offsetAtt.setOffset(start, end);
				posIncAtt.setPositionIncrement(1);
				//posLengthAtt.setPositionLength(len);
				//typeAtt.setType("word");
				start = end;
				return true;
			}
			start = end;
			end = breakIterator.next();
		}
//		return false;
		return input.incrementToken();
	}

	/** 判断是否是合写语言，即中文那样不用空格断词的语言 */
	private boolean isBigram(char c) {
		final String block = Character.UnicodeBlock.of(c).toString();
		if (block.startsWith("CJK")) {
			return true;
		}
		switch (block) {
			case "HIRAGANA":
			case "KATAKANA":
			case "HANGUL_SYLLABLES":
			case "HANGUL_JAMO_EXTENDED_B":
			case "EGYPTIAN_HIEROGLYPHS":
			case "OLD_SOGDIAN":
			case "SOGDIAN":
			case "THAI":
			case "TAMIL":
			case "TAMIL_SUPPLEMENT":
			case "TIBETAN":
			case "BRAHMI":
			case "YI_SYLLABLES":
			case "YI_RADICALS":
				return true;
		}
		return false;
	}

	@Override
	public void end() throws IOException {
		super.end();
		breakIterator.setText("");
	}
	
	public static Analyzer newAnalyzer() {
		//if(true) return new IKAnalyzer(false);
		return new StopwordAnalyzerBase(Version.LUCENE_47){
			protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
				//CMN.Log("createComponents...", fieldName, reader);
				Tokenizer source = new StandardTokenizer(this.matchVersion, reader);
				TokenStream result = new LowerCaseFilter(this.matchVersion, source);
				WordBreakFilter bwf = new WordBreakFilter(result);
				TokenStreamComponents ret = new TokenStreamComponents(source, new StopFilter(this.matchVersion, bwf, this.stopwords));
				bwf.component = ret;
				return ret;
				//return new TokenStreamComponents(source, new StopFilter(this.matchVersion, result, this.stopwords));
			}
		};
	}
	
	public static Analyzer newCjkAnalyzer() {
		return new StopwordAnalyzerBase(Version.LUCENE_47){
			protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
				//CMN.Log("createComponents...", fieldName, reader);
				Tokenizer source = new StandardTokenizer(this.matchVersion, reader);
				TokenStream result = new LowerCaseFilter(this.matchVersion, source);
				result = new CJKBigramFilter(result, 15, true);
				return new TokenStreamComponents(source, new StopFilter(this.matchVersion, result, this.stopwords));
			}
		};
	}
}
