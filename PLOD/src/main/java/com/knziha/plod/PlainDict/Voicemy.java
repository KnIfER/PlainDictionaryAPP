package com.knziha.plod.PlainDict;

import android.os.Build;
import android.speech.tts.Voice;

import androidx.annotation.RequiresApi;

import java.util.Locale;
import java.util.Set;

@Deprecated
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Voicemy extends Voice {
	public int shrinked;

	public Voicemy(String name, Locale locale, int quality, int latency, boolean requiresNetworkConnection, Set<String> features) {
		super(name, locale, quality, latency, requiresNetworkConnection, features);
	}


	public static boolean isDirScionOf(Voice voice, Voicemy mdTmp) {
		return voice.getLocale().getLanguage().equals(mdTmp.getLocale().getLanguage());
	}
}
