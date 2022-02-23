package com.knziha.plod.dictionarymodels;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class WikiWordOfTheDay {
	final static String[] months=new String[]{
		"January"
		,"February"
		,"March"
		,"April"
		,"May"
		,"June"
		,"July"
		,"August"
		,"September"
		,"October"
		,"November"
		,"December"
	};
	final static int yearFrom=2016;
	public static String getRandomPage(int seed) {
		int yearNow = new Date().getYear();
		int yd=yearNow-yearFrom;
		Random rand = new Random(seed);
		yearNow = yearFrom + rand.nextInt(yd);
		return "https://en.wiktionary.org/wiki/Wiktionary:Word_of_the_day/Archive/"+yearNow+"/"+months[rand.nextInt(12)];
	}
}
