package com.knziha.plod.widgets;

import static com.knziha.plod.plaindict.PDICMainAppOptions.FirstFlag;
import static com.knziha.plod.plaindict.PDICMainAppOptions.SecondFlag;
import static com.knziha.plod.plaindict.PDICMainAppOptions.getRoot;

import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;

import org.knziha.metaline.Metaline;

public class FIlePickerOptions extends FilePickerOptions {
//	@Metaline(flagPos=21) public static boolean getFFmpegThumbsGeneration() { SecondFlag=SecondFlag; throw new RuntimeException();}
//	@Metaline(flagPos=21) public static void setFFmpegThumbsGeneration(boolean val) { SecondFlag=SecondFlag; throw new RuntimeException();}
	
	
	@Metaline(flagPos=51) public static boolean getBkmkShown() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=51) public static void setBkmkShown(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=52) public static boolean getEnableTumbnails() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=52) public static void setEnableTumbnails(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=53) public static boolean getCropTumbnails() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=53) public static void setCropTumbnails(boolean val) { FirstFlag=FirstFlag; throw new RuntimeException();}

	@Metaline(flagPos=54, flagSize=4) public static int getListIconSize() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=54, flagSize=4) public static void setListIconSize(int val) { FirstFlag=FirstFlag; throw new RuntimeException();}

	@Metaline(flagPos=58, flagSize=3, shift=7, max=8) public static int getSortMode() { FirstFlag=FirstFlag; throw new RuntimeException();}
	@Metaline(flagPos=58, flagSize=3, shift=7, max=8) public static void setSortMode(int val) { FirstFlag=FirstFlag; throw new RuntimeException();}


//	@Metaline(flagPos=) public static boolean getBottombarShown() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
//	@Metaline(flagPos=) public static void setBottombarShown(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}

	@Metaline(flagPos=43) public static boolean getCreatingFile() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=43) public static void setCreatingFile(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}

//	@Metaline(flagPos=44) public static boolean getPinSortDialog() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
//	@Metaline(flagPos=44) public static void setPinSortDialog(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=44) public static boolean getSlideShowMode() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=44) public static void setSlideShowMode(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=45) public static boolean getRegexSearch() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=45) public static void setRegexSearch(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	@Metaline(flagPos=46) public static boolean getFFmpegThumbsGeneration() { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	@Metaline(flagPos=46) public static void setFFmpegThumbsGeneration(boolean val) { MainActivityUIBase.SessionFlag=MainActivityUIBase.SessionFlag; throw new RuntimeException();}
	
	
	public int getOpt(FilePickerOption opt, boolean get, int val) {
		switch (opt) {
			case bShowBookmarks:
				if (!get) setBkmkShown(val==1);
				return getBkmkShown()?1:0;
			case bShowBottombar:
				if (true) return 1;
			break;
			case bShowCreateFile:
				if (!get) setCreatingFile(val==1);
				return getCreatingFile()?1:0;
			case bPinSortDialog:
				if (true) return 0;
			break;
			case bShowThumbnails:
				if (!get) setEnableTumbnails(val==1);
				return getEnableTumbnails()?1:0;
			case bListMode:
			case bAutoThumbsHeight:
			case bFFMRThumbnails:
				if (!get) setFFmpegThumbsGeneration(val==1);
				return getFFmpegThumbsGeneration()?1:0;
			case bCropThumbnails:
				if (!get) setCropTumbnails(val==1);
				return getCropTumbnails()?1:0;
			case bLRU:
				if (!get) PDICMainAppOptions.setUseLruDiskCache(val==1);
				return PDICMainAppOptions.getUseLruDiskCache()?1:0;
			case bRegexSch:
				if (!get) setRegexSearch(val==1);
				return getRegexSearch()?1:0;
			case bRoot: return getRoot()?1:0;
			case bSlideshowMode:
				if (!get) setSlideShowMode(val==1);
				return getSlideShowMode()?1:0;
			case nIconSize:
				if (!get) setListIconSize(val);
				return getListIconSize();
			case nGridSize:
				return 3;
			case nSortMode:
				if (!get) setSortMode(val);
				return getSortMode();
		}
		return 0;
	}
}
