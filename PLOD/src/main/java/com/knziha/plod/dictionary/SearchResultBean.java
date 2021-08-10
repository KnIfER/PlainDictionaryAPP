package com.knziha.plod.dictionary;

public class SearchResultBean{
	public int position;
	public String preview;
	
	public final static int SEARCHTYPE_SEARCHINNAMES=1;
	public final static int SEARCHTYPE_SEARCHINTEXTS=2;
	
	public final static int SEARCHENGINETYPE_WILDCARD=0;
	public final static int SEARCHENGINETYPE_REGEX=1;
	public final static int SEARCHENGINETYPE_PLAIN=2;
	
	public SearchResultBean(int pos) {
		position = pos;
	}
}