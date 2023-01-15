package com.knziha.plod.PlainUI;

import static androidx.appcompat.app.GlobalOptions.NEGATIVE_1;
import static androidx.appcompat.app.GlobalOptions.density;
import static androidx.appcompat.app.GlobalOptions.isDark;

import static com.knziha.plod.PlainUI.WordPopupTask.TASK_TTS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.ankislicer.customviews.ShelfLinearLayout;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.WebViewListHandler;
import com.knziha.plod.plaindict.databinding.ContentviewBinding;
import com.knziha.plod.preference.RadioSwitchButton;
import com.knziha.plod.preference.SettingsPanel;
import com.knziha.plod.widgets.DescriptiveImageView;
import com.knziha.plod.widgets.NoScrollViewPager;
import com.knziha.plod.widgets.SpeedTagShape;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.text.ColoredHighLightSpan;
import com.knziha.text.ScrollViewHolder;
import com.knziha.text.SelectableTextView;
import com.knziha.text.SelectableTextViewBackGround;
import com.knziha.text.SelectableTextViewCover;
import com.knziha.text.TTSMoveToucher;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressLint("ResourceType")
public class TTSHub extends PlainAppPanel implements PopupMenuHelper.PopupMenuListener, View.OnLongClickListener {
	MainActivityUIBase a;
	NoScrollViewPager viewPager;
	View[] viewList;
	ViewGroup bar;
	ShelfLinearLayout bottomShelf;
	BookPresenter invoker;
	
	public View floatBasic;
	
	public SelectableTextView textView;
	public TextView tvHandle;
	public TTSMoveToucher TTSController_moveToucher;
	public ImageView playBtn;
	public ViewGroup TTSController_;
	public ViewGroup toolbar;
//	public CircleCheckBox TTSController_ck1;
//	public CircleCheckBox TTSController_ck2;
	
	public ColoredHighLightSpan timeHLSpan;
	public static final float[] TTS_LEVLES_SPEED = new float[]{0.5f, 0.75f, 1f, 1.5f, 2f};
	public int TTSSpeed = 2;
	public int TTSPitch = 2;
	public float TTSVolume = 1.f;
	public ViewGroup controlBar;
	
	public static String delimiter = "[.?!;。？！；\\[\\]【】\r\n]";
	public int utteranceCacheSize = 9;
	public int highLightBG = Color.YELLOW;//Color.YELLOW;
	
	public TextToSpeech tts;
	public volatile boolean TTSReady;
	public @NonNull String[] speakPool = ArrayUtils.EMPTY_STRING_ARRAY;
	public int[] speakScaler;
	public Object speakText;
	public volatile int speakPoolIndex;
	public volatile int doneEndIndex;
	public volatile int cachedEndIndex;
	private WebViewmy mCurrentReadContext;
	
	private SettingsPanel ttsTweaker;
	private static int ttsChoiceVer;
	private int mTtsChoiceVer;
	private boolean hubExpanded;
	private boolean playing;
	private View tiaoJieView;
	private Runnable tiaoJieRun;
	private boolean dirty;
	
	public TTSHub(MainActivityUIBase a) {
		super(a, false);
		this.bottomPadding = 0;
		this.bPopIsFocusable = true;
		this.bFadeout = -2;
		this.bAnimate = false;
		this.tweakDlgScreen = false;
		this.a = a;
		setShowInDialog();
	}
	
	public enum YuZhong{
		zho
		, eng
		, kor
		, jpn
		, rus
		, fra
		, deu
		, ara
		, hin
	}
	
	public static String guessLanguage(CharSequence text) {
		int size = text.length();
		if(size > 14) size = 13;
		SparseIntArray weights = new SparseIntArray();
		for (int i = 0; i < size; i++) {
			char c = text.charAt(i);
			int yz = -1;
			if ('a' <= c && c <= 'z' || ('A' <= c && c <= 'Z')) {
				yz = YuZhong.eng.ordinal();
			}
			else if (c>=192 && c<=339) {
				switch (c) {
					case 'À': case 'Â': case 'È': case 'É': case 'Ê': case 'Ë': case 'Î': case 'Ï': case 'Ô': case 'Ö': case 'Ù': case 'Û': case 'Ç': case 'Œ': case 'Æ': case 'à': case 'â': case 'è'
							: case 'é': case 'ê': case 'î': case 'ï': case 'ô': /*case 'ö':*/ case 'ù': case 'û': /*case 'ü':*/ case 'ç': case 'œ': case 'æ':
						yz = YuZhong.fra.ordinal();
						break;
					case 'ä': case 'ö': case 'ü': case 'ß':
						yz = YuZhong.deu.ordinal();
						break;
				}
			}
			if(yz==-1)  {
				int gc = Character.getType(c);
				Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
				switch (block.toString()) {
					case "CJK_UNIFIED_IDEOGRAPHS":
						yz = YuZhong.zho.ordinal();
						break;
					case "HANGUL_JAMO":
					case "HANGUL_COMPATIBILITY_JAMO":
					case "HANGUL_SYLLABLES":
						yz = YuZhong.kor.ordinal();
						break;
					case "HIRAGANA":
					case "KATAKANA":
						yz = YuZhong.jpn.ordinal();
						break;
					case "LATIN_EXTENDED_A":
						yz = YuZhong.hin.ordinal();
						break;
					case "CYRILLIC":
					case "CYRILLIC_EXTENDED_A":
					case "CYRILLIC_EXTENDED_B":
					case "CYRILLIC_SUPPLEMENTARY":
						yz = YuZhong.rus.ordinal();
						break;
					case "ARABIC":
					case "ARABIC_EXTENDED_A":
					case "ARABIC_SUPPLEMENT":
						yz = YuZhong.ara.ordinal();
						break;
				}
				if (gc >= Character.UPPERCASE_LETTER && gc <= Character.OTHER_LETTER) {
					//yz = YuZhong.RUS.ordinal();
				}
			}
			if (yz >= 0) {
				//CMN.debug("put::", c, YuZhong.values()[yz]);
				weights.put(yz, weights.get(yz) + 1);
			}
		}
		// weights.keyAt()
		int max=0, guess=0;
		for (int i = 0; i < weights.size(); i++) {
			if (weights.valueAt(i) > max) {
				max = weights.valueAt(i);
				guess = weights.keyAt(i);
			}
		}
		if (guess==YuZhong.eng.ordinal()) {
			if (weights.get(YuZhong.fra.ordinal())>0) {
				return "fra";
			}
			if (weights.get(YuZhong.deu.ordinal())>0) {
				return "deu";
			}
		}
		return YuZhong.values()[guess].name();
	}
	
	Locale defYZ = new Locale("zho", "");
	
	void runSendText(boolean setTo) {
		if(tts==null)
			return;
		String[] pool = TTSHub.this.speakPool;
		int cached = cachedEndIndex;
		int start = Math.max(0 ,Math.min(pool.length, doneEndIndex +1));
		int end = Math.min(start+(setTo?1:utteranceCacheSize), pool.length);
		cachedEndIndex = end;
		if(start >= pool.length){
			pauseTTSCtrl(true);
		}
		else {
			pauseTTSCtrl(false);
			String yz = null, lanuage;
			final HashMap<String, String> parms = new HashMap<>();
			parms.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, Float.toString(TTSVolume));
			for (int i = cached==-1?start:Math.max(0 ,Math.min(pool.length, cached)); i < end; i++) {
				parms.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(i));
				String text = pool[i].trim();
				lanuage = guessLanguage(text);
				if (!TextUtils.equals(lanuage, yz)) {
					int res = tts.setLanguage(new Locale(lanuage, ""));
					CMN.debug("TTSController_engine.setLanguage::", res);
					yz = lanuage;
				}
				tts.speak(text,setTo?TextToSpeech.QUEUE_FLUSH:TextToSpeech.QUEUE_ADD, parms);
				CMN.debug("缓存了句子", i, text, lanuage);
				if(!a.AutoBrowsePaused){
					a.root.postDelayed(forceNextTextRunnable, 800);
				}
			}
		}
	}
	
	
	/** 提交语句给TTS引擎 */
	Runnable mPullReadTextRunnable = new Runnable() {
		@Override
		public void run() {
			runSendText(false);
		}
	};
	
	public void refreshReadLight(int start) {
		if(timeHLSpan==null){
			timeHLSpan = new ColoredHighLightSpan(highLightBG, 9f, 1);
		}
		try {
			SpannableString baseSpan = (SpannableString) speakText;
			int end = 1 + speakScaler[start];
			start = start>0? 1 + speakScaler[start-1]:0;
			baseSpan.setSpan(timeHLSpan, Math.min(start, baseSpan.length()-1), Math.min(end, baseSpan.length()), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public boolean anyVisible() {
		return TTSController_!=null && TTSController_.getParent()!=null
				|| isVisible();
	}
	
	public boolean fltVisible() {
		return ViewUtils.isVisibleV2(TTSController_);
	}
	
	public void pauseTTSCtrl(boolean pause) {
		if(playBtn !=null) {
			if (pause) {
				playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				playing = false;
			} else if(playBtn.getTag()==null){
				playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
				playing = true;
			}
		}
	}
	
	Runnable mUpdateTextRunnable = new Runnable() {
		@Override
		public void run() {
			if (textView != null && speakText instanceof String) {
				textView.setText(new SpannableStringBuilder((String) speakText).append("\n\n\n\n"), TextView.BufferType.SPANNABLE);
				speakText = textView.getText();
				textView.clearSelection();
			} else if(textView == null){
				dirty = true;
			}
		}
	};
	
	/** 重新开始TTS读网页句子。 */
	public void ReadText(String text, WebViewmy mWebView){
		opt = a.opt;
		if(!a.AutoBrowsePaused && PDICMainAppOptions.getAutoBrowsingReadSomething())
			a.interruptAutoReadProcess(true);
		if(text!=null) {
			speakText = text;
			if (text.length() > 1000) {
				speakPool = text.split(delimiter);
			} else {
				//speakPool = text.split("[\r\n]");
				speakPool = text.split(delimiter);
			}
			speakPoolIndex = 0;
			cachedEndIndex = doneEndIndex = -1;
			speakScaler = null;
			if (anyVisible()) {
				if (Thread.currentThread() != Looper.getMainLooper().getThread())
					a.root.post(mUpdateTextRunnable);
				else
					mUpdateTextRunnable.run();
			} else {
				dirty = true;
			}
			mCurrentReadContext = mWebView;
		}
		if(tts != null) {
			tts.stop();
		}
		pauseTTS();
		boolean mTTSReady = TTSReady;
		
		if(true || tts ==null) {
			mTTSReady = TTSReady = false;
			tts = new TextToSpeech(a, status -> {
				TTSReady = true;
				sendText();
			}, PDICMainAppOptions.sysTTS()?null:opt.getString("ttsEngine", null)); //"com.google.android.tts"
			tts.setSpeechRate(TTS_LEVLES_SPEED[TTSSpeed]);
			tts.setPitch(TTS_LEVLES_SPEED[TTSPitch]);
			
			//TTSController_engine.speak(text, TextToSpeech.QUEUE_ADD, null);
			tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) {
					if (playing) {
						speakPoolIndex = IU.parsint(utteranceId);
						a.root.removeCallbacks(forceNextTextRunnable);
						CMN.debug("tts onStart" ,speakPoolIndex);
						if(opt.getTTSHighlightWebView()) {
//							WebViewmy CRC = mCurrentReadContext;
//							if(CRC!=null)
//								CRC.post(() -> CRC.findAllAsync(speakPool[speakPoolIndex]));
						}
						a.onAudioPlay();
						
						String[] pool = TTSHub.this.speakPool;
						if(anyVisible() && speakText instanceof SpannableString){
							tvHandle.setText(pool[speakPoolIndex]);
							if(speakScaler ==null){
								speakScaler = new int[pool.length];
								for (int j = 0; j < pool.length; j++) {
									speakScaler[j]=(j>0?speakScaler[j-1]+1:0)+ pool[j].length();
								}
							}
							refreshReadLight(speakPoolIndex);
						}
					}
				}
				@Override
				public void onDone(String utteranceId) {
					doneEndIndex = IU.parsint(utteranceId);
					int speakPoolIndex = doneEndIndex +1;
					CMN.debug("tts onDone" ,speakPoolIndex, cachedEndIndex);
					if (speakPoolIndex >= speakPool.length || !playing) {
						a.onAudioPause();
						CMN.debug("tts onPause");
					} else if(speakPoolIndex>= cachedEndIndex){
						doSendText(); // 循环
					}
					//onAudioPause();
				}
				long lastError=0;
				@Override
				public void onError(String utteranceId) {
					CMN.debug("9 onError" ,utteranceId);
//					long errId = CMN.now();
//					//if (errId - lastError > 256)
//					{
//						lastError = errId;
//						onDone(utteranceId);
//					}
				}
				@Override
				public void onError(String utteranceId, int code) {
					CMN.debug("tts onError" ,utteranceId, code);
				}
			});
		}
		
		if(text!=null) {
			speakPoolIndex = 0;
			cachedEndIndex = doneEndIndex = -1;
		}
		
		if(mTTSReady)
			sendText();
		playing = true;
	}
	
	private void sendText() {
		//mPullReadTextRunnable.run();
		a.wordPopup.startTask(TASK_TTS);
	}
	
	public void doSendText() {
		if (playing) {
			try {
				mPullReadTextRunnable.run();
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
	
	/** 暂停TTS */
	public void pauseTTS(){
		if(tts !=null){
			tts.stop();
			cachedEndIndex = -1;
			doneEndIndex =speakPoolIndex-1;
		}
	}
	
	/**
	 engines  | voices  | Languages
	 speed | pitch
	 */
	/** TTS controller */
	public void showTTS() {
		ViewGroup targetRoot = a.root;
		if(a.PeruseViewAttached())
			targetRoot = a.peruseView.root;
		boolean isNewHolder=false;
		boolean isInit=false;
		// 初始化核心组件
		if(textView == null){
			isInit=isNewHolder=true;
		}
		
//		TTSController_ck1.drawInnerForEmptyState=
//				TTSController_ck2.drawInnerForEmptyState=GlobalOptions.isDark;
//		if(!GlobalOptions.isDark){
//			TTSController_ck1.circle_shrinkage=2;
//			TTSController_ck2.circle_shrinkage=2;
//		}
//		TTSController_ck1.setChecked(opt.getTTSCtrlPinned());
//		TTSController_ck2.setChecked(opt.getTTSHighlightWebView());
		
		if(isNewHolder){
			a.fix_pw_color();
			FrameLayout.LayoutParams lp = ((FrameLayout.LayoutParams) TTSController_.getLayoutParams());
			lp.height = TTSController_moveToucher.FVH_UNDOCKED=(int)(a.dm.heightPixels*5.0/12-a.getResources().getDimension(R.dimen._20_));
			if(!opt.getTTSExpanded()) lp.height = TTSController_moveToucher._45_;
			TTSController_.setTranslationY(targetRoot.getHeight()-a.getResources().getDimension(R.dimen._50_)-a.getResources().getDimension(R.dimen._50_));
			lp.height= TTSController_.getLayoutParams().height;
		}
		
		ViewGroup svp = (ViewGroup) TTSController_.getParent();
		if(svp!=targetRoot){
			if(svp!=null) svp.removeView(a.wordPopup.popupGuarder);
			if(TTSController_moveToucher.FVDOCKED && TTSController_moveToucher.Maximized){
				TTSController_moveToucher.Dedock();
			}
			
			targetRoot.addView(TTSController_);
			a.fix_full_screen(null);
		}
		mUpdateTextRunnable.run();
	}
	
	public void hideTTS() {
		ViewUtils.removeView(TTSController_);
	}
	
	public void toggleFloatBtn() {
		if(TTSController_==null || TTSController_.getParent()==null)
			showTTS();
		else {
			hideTTS();
		}
	}
	
	//Runnable forceNextTextRunnable = this::onAudioPause;
	Runnable forceNextTextRunnable = new Runnable() {
		@Override
		public void run() {
			CMN.debug("forceNextTextRunnable");
			a.onAudioPause();
		}
	};
	
	ShapeDrawable da, xiao;
	DescriptiveImageView[] volPitSpd;
	
	private View initFloatBasic() {
		if (floatBasic == null) {
			volPitSpd = new DescriptiveImageView[3];
			floatBasic = a.getLayoutInflater().inflate(R.layout.float_tts_basic, a.root, false);
			TTSController_ = (ViewGroup) floatBasic;
			TTSController_.setOnClickListener(ViewUtils.DummyOnClick);
			toolbar = (ViewGroup) TTSController_.getChildAt(0);
			ImageView TTSController_expand = toolbar.findViewById(R.id.tts_expand);
			TTSController_expand.setOnClickListener(this);
			if(opt.getTTSExpanded()) {
				TTSController_expand.setImageResource(R.drawable.ic_substrct_black_24dp);
			}
			ViewGroup middle = (ViewGroup) TTSController_.getChildAt(1);
			SelectableTextView tv = TTSController_.findViewById(R.id.text1);
			controlBar = (ViewGroup) TTSController_.getChildAt(2);
			if(!opt.getTTSCtrlPinned())
				controlBar.setVisibility(View.GONE);
//			SeekBar.OnSeekBarChangeListener controller_controller=new SeekBar.OnSeekBarChangeListener() {
//				@Override
//				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//					seekBar.setTag(false);
//					switch (seekBar.getId()){
//						case R.id.sb1:
//							TTSVolume=progress*1.f/100;
//							tvHandle.setText("音量："+ progress);
//							break;
//						case R.id.sb2:
//							float _TTS_Pitch=TTS_LEVLES_SPEED[TTSPitch=progress];
//							if (tts != null) tts.setPitch(_TTS_Pitch);
//							tvHandle.setText("音调："+_TTS_Pitch);
//							break;
//						case R.id.sb3:
//							float _TTS_Speed=TTS_LEVLES_SPEED[TTSSpeed=progress];
//							if (tts != null) tts.setSpeechRate(_TTS_Speed);
//							tvHandle.setText("语速："+_TTS_Speed);
//							break;
//					}
//				}
//				@Override public void onStartTrackingTouch(SeekBar seekBar) { }
//				@Override
//				public void onStopTrackingTouch(SeekBar seekBar) {
//					if(seekBar.getTag()==null){
//						onProgressChanged(seekBar, seekBar.getProgress(), false);
//					} else {
//						pauseTTS();
//						switch (seekBar.getId()) {
//							case R.id.sb1:
//								if(playing) sendText();
//								break;
//							case R.id.sb2:
//							case R.id.sb3:
//								if(playing) ReadText(null, mCurrentReadContext);
//								break;
//						}
//					}
//					seekBar.setTag(null);
//				}
//			};
//			SeekBar seekBar = (SeekBar) controlBar.getChildAt(0);
//			seekBar.setProgress((int) (TTSVolume *100));
//			seekBar.setOnSeekBarChangeListener(controller_controller);
//			seekBar = (SeekBar) controlBar.getChildAt(2);
//			seekBar.setProgress(TTSPitch);
//			seekBar.setMax(TTS_LEVLES_SPEED.length-1);
//			seekBar.setOnSeekBarChangeListener(controller_controller);
//			seekBar = (SeekBar) controlBar.getChildAt(4);
//			seekBar.setProgress(TTSSpeed);
//			seekBar.setMax(TTS_LEVLES_SPEED.length-1);
//			seekBar.setOnSeekBarChangeListener(controller_controller);
			TextPaint textPainter = DescriptiveImageView.newTextPainter();
			textPainter.setColor(0xFF44A0D3);
			boolean isLTR = controlBar.getContext().getResources().getConfiguration().getLayoutDirection()==
					View.LAYOUT_DIRECTION_LTR;
			float height = 0.75f;
			da = new ShapeDrawable(new SpeedTagShape(isLTR?0:1, height));
			xiao = new ShapeDrawable(new SpeedTagShape(!isLTR?0:1, height));
			da.getPaint().setColor(isDark?0xff6699bb:0xFF44A0D3);
			xiao.getPaint().setColor(isDark?0xff6699bb:0xFF44A0D3);
			for (int i = 0, sz = controlBar.getChildCount(), cc=0; i < sz; i++) {
				View child = controlBar.getChildAt(i);
				if (child instanceof DescriptiveImageView) {
					((DescriptiveImageView) child).textPainter = textPainter;
					final DescriptiveImageView v = (DescriptiveImageView) child;
					volPitSpd[cc++] = v;
					//v.tintListFilter = a.tintListFilter;
				} else if (child instanceof TextView) {
					LayerDrawable ld = new LayerDrawable(new Drawable[]{child.getBackground(), child.getId()==R.id.spddn?xiao:da
							});
					child.setBackground(ld);
					child.setOnLongClickListener(this);
				}
				child.setOnClickListener(this);
			}
			
			tv.setBackgroundColor(Color.TRANSPARENT);
			ScrollViewHolder svmy = middle.findViewById(R.id.sv);
			SelectableTextViewCover textCover = middle.findViewById(R.id.cover);
			SelectableTextViewBackGround textCover2 = middle.findViewById(R.id.cover2);
			tv.instantiate(textCover, textCover2, svmy, null);
			tv.setTextViewListener(selectableTextView -> {
				//TimetaskHolder tk = new TimetaskHolder(2);
				//timer.schedule(tk, 110);
			});
			playBtn = toolbar.findViewById(R.id.tts_play);
			ViewUtils.setOnClickListenersOneDepth(toolbar, this, 2, null);
			
			//TTSController_bottombar.findViewById(R.id.popIvRecess).setOnClickListener(MainActivityUIBase.this);
			//TTSController_bottombar.findViewById(R.id.popIvForward).setOnClickListener(MainActivityUIBase.this);
			
//			TTSController_ck1 = TTSController_bottombar.findViewById(R.id.ttsPin);
//			TTSController_ck1.setOnClickListener(a);
//			TTSController_ck2 = TTSController_bottombar.findViewById(R.id.ttsHighlight);
//			TTSController_ck2.setOnClickListener(a);
			tvHandle = toolbar.findViewById(R.id.popupText1);
			
			// 移动逻辑
//			TTSController_tvHandle.setOnTouchListener(TTSController_moveToucher = new TTSMoveToucher(a, TTSController_tvHandle, TTSController_, opt));
//			popupNxtD.setOnTouchListener(TTSController_moveToucher);
//			popupLstD.setOnTouchListener(TTSController_moveToucher);
			// 缩放逻辑
			tv.viewPager = viewPager;
			tv.setTheme(a.AppWhite, a.AppBlack, 0x883b53f1, 0x883b53f1);
			//textCover2.highLightBg = isDark ? 0x77c17d33 : Color.YELLOW;
			highLightBG = isDark ? 0x77c17d33 : Color.YELLOW;
			if(timeHLSpan!=null) timeHLSpan.mColor = highLightBG;
			this.textView = tv;
		}
		return floatBasic;
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		if (a!=null && settingsLayout==null) {
			opt = a.opt;
			hubExpanded = opt.ttsHubExpanded();
			initFloatBasic();
			View layout = a.getLayoutInflater().inflate(R.layout.tts_sound_control, a.root, false);
			viewPager = layout.findViewById(R.id.viewpager);
			bottomShelf = layout.findViewById(R.id.btns);
			viewList = new View[2];
			bar = layout.findViewById(R.id.bar);
			ViewUtils.setOnClickListenersOneDepth(bottomShelf, this, 2, null);
			for (int i = 0; i < 2; i++) {
//				RecyclerView lv = viewList[i] = new RecyclerView(new ContextThemeWrapper(a, R.style.RecyclerViewStyle));
//				lv.setLayoutManager(new LinearLayoutManager(a));
//				//RecyclerView.RecycledViewPool pool = viewList[i].getRecycledViewPool();
//				//pool.setMaxRecycledViews(0,10);
//
//				DividerItemDecoration divider = new DividerItemDecoration(a, LinearLayout.VERTICAL);
//				divider.setDrawable(a.mResource.getDrawable(R.drawable.divider4));
//				lv.addItemDecoration(divider);
//
//				//取消更新item时闪烁
//				RecyclerView.ItemAnimator anima = lv.getItemAnimator();
//				if(anima instanceof DefaultItemAnimator)
//					((DefaultItemAnimator)anima).setSupportsChangeAnimations(false);
//				anima.setChangeDuration(0);
//				anima.setAddDuration(0);
//				anima.setMoveDuration(0);
//				anima.setRemoveDuration(0);
//
//				bottomShelf.getChildAt(i).setOnClickListener(this);
			}
			viewPager.setAdapter(new PagerAdapter() {
				@Override public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
					return arg0 == arg1;
				}
				@Override public int getCount() { return 2; }
				@Override public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
					View child = viewList[position];
					ViewUtils.removeView(child);
					//child.stopScroll();
				}
				@NonNull @Override
				public Object instantiateItem(ViewGroup container, int position) {
					View child = viewList[position];
					if (child == null) {
						if (position == 0) {
							ttsTweaker = new SettingsPanel(a, opt
									, new String[][]{new String[]{"<font color='#3185F7'>朗读设置：</font>", "自动播放词条", "然后自动朗读全文", "提示TTS播报", "后台播放TTS", "音量键优先调整音量"}
									, new String[]{"<font color='#3185F7'>朗读引擎：</font>", "使用系统设置"}
									}
									, new int[][]{new int[]{Integer.MAX_VALUE
											, makeInt(3, 38, false) // getAutoReadEntry
											, makeInt(4, 48, false) // getThenAutoReadContent
											, makeInt(3, 37, false) // getHintTTSReading
											, makeInt(3, 42, true) // getTTSBackgroundPlay
											, makeInt(3, 39, true) // getMakeWayForVolumeAjustmentsWhenAudioPlayed
										}
										, new int[]{Integer.MAX_VALUE
											, makeInt(8, 44, true) // sysTTS
										}
									}
									, null);
							//ttsTweaker.setEmbedded(this);
							ttsTweaker.init(a, root);
							child = ttsTweaker.settingsLayout;
							ViewUtils.setPadding(child, (int) (density*15),-1,-1,-1);
							ttsTweaker.setInnerBottomPadding((int) (density*15));
							refreshTTSEngLst(true);
						}
						if (position == 1) {
							child = floatBasic;
						}
						viewList[position] = child;
					}
					ViewUtils.addViewToParent(child, container);
					return child;
				}
			});
			bottomShelf.post(() -> bottomShelf.selectToolIndex(1));
			//viewPager.setNoScroll(true);
			viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				int lastPos;
				@Override public void onPageScrollStateChanged(int arg0) { }
				@Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
				@Override
				public void onPageSelected(int i) {
					CMN.debug("onPageSelected::", i);
					bottomShelf.selectToolIndex(lastPos = i);
					if (i == 0) {
					}
				}
			});
			viewPager.setCurrentItem(1);
			settingsLayout = (ViewGroup) layout;
		}
	}
	
	public List<TextToSpeech.EngineInfo> getEngines() {
		engines.clear();
		try {
			PackageManager pm = a.getPackageManager();
			Intent intent = new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE);
			List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (resolveInfos == null) return engines;
			for (ResolveInfo resolve : resolveInfos) {
				ServiceInfo service = resolve.serviceInfo;
				if (service.packageName != null) {
					TextToSpeech.EngineInfo engine = new TextToSpeech.EngineInfo();
					engine.name = service.packageName;
					String name = service.loadLabel(pm).toString();
					engine.label = TextUtils.getTrimmedLength(name) > 0 ? name : engine.name;
					engines.add(engine);
				}
			}
		} catch (Exception e) {
			CMN.debug(e);
		}
		return engines;
	}
	
	List<TextToSpeech.EngineInfo> engines = new ArrayList<>();
	
	private void refreshTTSEngLst(boolean init) {
		if (ttsTweaker != null) {
			ViewGroup layout = ttsTweaker.settingsLayout;
			View v = null;
			if (init) {
				v = layout.findViewById(makeInt(8, 44, true));
				if (v != null) {
					v.setId(R.id.voice);
					v.setTag(ttsTweaker);
				}
			}
			if (v == null) {
				v = layout.findViewById(R.id.voice);
			}
			v.setOnClickListener(this);
			((RadioSwitchButton)v).setChecked(PDICMainAppOptions.sysTTS());
			ViewGroup vp = (ViewGroup) v.getParent();
			int idx = vp.indexOfChild(v);
			View child;
			while ((child = vp.getChildAt(idx + 1)) != null && child.getTag() == ttsTweaker) {
				ViewUtils.removeView(child);
			}
			getEngines();
			String engine = opt.getString("ttsEngine", null);
			for (int i = 0; i < engines.size(); i++) {
				RadioSwitchButton button = new RadioSwitchButton(a);
				if(GlobalOptions.isDark) button.setTextColor(Color.WHITE);
				button.setText(engines.get(i).label);
				button.setButtonDrawable(R.drawable.radio_selector);
				button.setPadding((int) (mItemPaddingLeft*density), (int) (mItemPaddingTop*density), 0, (int) (mItemPaddingBottom*density));
				button.setOnClickListener(this);
				button.setChecked(!PDICMainAppOptions.sysTTS() && engines.get(i).name.equals(engine));
				button.setId(R.id.voice);
				button.setTag(ttsTweaker);
				vp.addView(button, idx+i+1, v.getLayoutParams());
			}
		}
	}
	
	private void refreshExpand() {
		View v = settingsLayout;
		DisplayMetrics dm2 = a.dm;
		if (hubExpanded)
			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * 0.85f);
		else
			v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * ((BottomSheetDialog) dialog).getBehavior().getHalfExpandedRatio() + a.getResources().getDimension(R.dimen._45_) * 1.75);
		v.requestLayout();
	}
	
	@Override
	protected void onShow() {
		refresh();
		if (ViewUtils.addViewToParent(toolbar, bar)) {
			for (int i = 0; i < toolbar.getChildCount(); i++) {
				View child = toolbar.getChildAt(i);
				if (child.getId() != R.id.popupText1) {
					((ImageView) child).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				} else {
					((TextView)child).setTextColor(Color.WHITE);
				}
			}
		}
		if (ttsTweaker != null) {
			ttsTweaker.refresh();
			if (mTtsChoiceVer < ttsChoiceVer) {
				refreshTTSEngLst(false);
			}
		}
		if (dirty) {
			initFloatBasic();
			mUpdateTextRunnable.run();
			dirty = false;
		}
	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
	}
	
	@Override
	public void refresh() {
		CMN.debug("TTSHub::refresh");
		if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			//toolbar.setTitleTextColor(a.AppWhite);
			MainAppBackground = a.MainAppBackground;
			bar.setBackgroundColor(MainAppBackground);
			bottomShelf.setBackgroundColor(MainAppBackground);
			viewPager.setBackgroundColor(a.AppWhite);
			//for (int i = 0; i < 3; i++)  ((TextView) bottomShelf.getChildAt(i)).setTextColor(a.AppWhite);
			int gray = 0x55888888;
			//if(Math.abs(0x888888-(a.MainAppBackground&0xffffff)) < 0x100000)
				gray = ColorUtils.blendARGB(a.MainAppBackground, Color.WHITE, 0.1f);
			bottomShelf.setSCC(bottomShelf.ShelfDefaultGray=gray);
			if (textView != null) {
				textView.setTheme(a.AppWhite, a.AppBlack, 0x883b53f1, 0x883b53f1);
				//textView.textCover2.highLightBg = isDark ? 0x77c17d33 : Color.YELLOW;
				highLightBG = isDark ? 0x77c17d33 : Color.YELLOW;
				floatBasic.getBackground().setColorFilter(isDark?NEGATIVE_1:null);
				
				da.getPaint().setColor(isDark?0xff6699bb:0xFF44A0D3);
				xiao.getPaint().setColor(isDark?0xff6699bb:0xFF44A0D3);
			}
			if (ttsTweaker != null) {
				ttsTweaker.refresh();
			}
		}
		if (ViewUtils.ensureTopmost(dialog, a, dialogDismissListener)
				|| ViewUtils.ensureWindowType(dialog, a, dialogDismissListener)) {
			ViewUtils.makeFullscreenWnd(dialog.getWindow());
		}
	}
	
	
	// longclick
	@Override
	public boolean onLongClick(View v) {
		tiaoJieView = v;
		if (tiaoJieRun == null) {
			tiaoJieRun = new Runnable() {
				@Override
				public void run() {
					if (tiaoJieView != null) {
						tiaoJie(tiaoJieView);
						a.root.postDelayed(this, 350);
					}
				}
			};
		}
		if (controlBar.getTag()==null) {
			controlBar.setTag(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int act = event.getActionMasked();
					if (act==MotionEvent.ACTION_UP || act==MotionEvent.ACTION_CANCEL) {
						tiaoJieView = null;
					}
					return false;
				}
			});
		}
		v.setOnTouchListener((View.OnTouchListener) controlBar.getTag());
		tiaoJieRun.run();
		return true;
	}
	
	// click
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//			case R.id.ttsPin: {
//				CircleCheckBox checker = (CircleCheckBox) v;
//				checker.toggle();
//				opt.setTTSCtrlPinned(checker.isChecked());
//				TTSController_controlBar.setVisibility(checker.isChecked()?View.VISIBLE:View.GONE);
//			} break;
//			case R.id.ttsHighlight: {
//				CircleCheckBox checker = (CircleCheckBox) v;
//				checker.toggle(false);
//				opt.setTTSHighlightWebView(checker.isChecked());
//			} break;
			case R.id.volumn:
			case R.id.pitch:
			case R.id.speed: {
				a.showT("未实现");
			} break;
			case R.id.spddn:
			case R.id.spdup: {
				tiaoJie(v);
			} break;
			case R.id.voice: {
				if (ttsTweaker != null) {
					mTtsChoiceVer = ++ttsChoiceVer;
					ViewGroup layout = ttsTweaker.settingsLayout;
					View first = layout.findViewById(v.getId());
					ViewGroup vp = (ViewGroup) v.getParent();
					int idx = vp.indexOfChild(first), fvp=idx;
					View child;
					String engine = opt.getString("ttsEngine", null);
					while ((child = vp.getChildAt(idx)) != null && child.getTag()==ttsTweaker) {
						RadioSwitchButton button = (RadioSwitchButton) child;
						if (button==v) {
							if (v == first) {
								if (PDICMainAppOptions.sysTTS()) {
									refreshTTSEngLst(false);
									break;
								} else {
									PDICMainAppOptions.sysTTS(true);
								}
							} else {
								PDICMainAppOptions.sysTTS(false);
								String newEng = engines.get(idx - fvp - 1).name;
								if (!newEng.equals(engine)){
									opt.putString("ttsEngine", newEng);
								}
							}
						}
						button.setChecked(button==v);
						idx++;
					}
					ReadText(null, mCurrentReadContext);
				}
			} break;
			case R.id.tts_popIvBack: {
				if (isVisible()) {
					dismiss();
				} else if(anyVisible()){
					hideTTS();
				}
			} break;
			case R.id.ivBack: {
				dismiss();
			} break;
			case R.id.tts_settings: {
				try {
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					a.startActivity(intent);
				} catch (Exception e) {
					CMN.debug(e);
				}
			} break;
			case R.id.tts1:
			case R.id.tts2: {
				viewPager.setCurrentItem(v.getId()==R.id.tts1?0:1);
			} break;
			case R.id.tts_expand: {
				if (isVisible()) {
					if(hubExpanded=!hubExpanded){
						((ImageView)v).setImageResource(R.drawable.ic_substrct_black_24dp);
					} else {
						((ImageView)v).setImageResource(R.drawable.ic_add_black_24dp);
					}
					refreshExpand();
					opt.ttsHubExpanded(hubExpanded);
				} else if (anyVisible()) {
					if (opt.setTTSExpanded(!opt.getTTSExpanded())) {
						TTSController_.getLayoutParams().height = TTSController_moveToucher.FVH_UNDOCKED;
						((ImageView) v).setImageResource(R.drawable.ic_substrct_black_24dp);
					} else {
						TTSController_.getLayoutParams().height = (int) a.getResources().getDimension(R.dimen._45_);
						((ImageView) v).setImageResource(R.drawable.ic_add_black_24dp);
					}
					TTSController_.requestLayout();
				}
			} break;
			case R.id.tts_play: {
				if(speakPool.length==0) break;
				if(playing=!playing){
					if(doneEndIndex +1>=speakPool.length){
						doneEndIndex = -1;
					}
					cachedEndIndex = -1;
					sendText();
				} else {
					v.setTag(null);
					tts.stop();
					cachedEndIndex = -1;
					playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				}
			} break;
			case R.id.tts_NxtUtterance:
			case R.id.tts_LstUtterance: {
				if(speakPool.length==0) break;
				int delta = (v.getId()==R.id.tts_LstUtterance?-1:1);
				tts.stop();
				int target = speakPoolIndex + delta;
				while(target<speakPool.length && target>=0 && speakPool[target].trim().length()==0){
					target += delta;
				}
				speakPoolIndex = target;
				cachedEndIndex = -1;
				doneEndIndex = target - 1;
				refreshReadLight(target);
				runSendText(true);
				//sendText();
			} break;
		}
	}
	
	private void tiaoJie(View v) {
		DescriptiveImageView tweak = null;
		boolean b1 = v.getId()==R.id.spdup;
		int i = controlBar.indexOfChild(v), d=b1?1:-1;
		View tag = controlBar.getChildAt(i - d);
		for (i = 0; i < 3; i++) {
			if(volPitSpd[i]==tag) { tweak=volPitSpd[i]; break; }
		}
		float value;
		if (tweak != null) {
			if (tweak.getId()==R.id.volumn) {
				value=Math.max(0, Math.min(TTSVolume*100+d*25, 100));
				TTSVolume = value/100;
				tweak.setText(""+((int)value));
				a.showT("音量" + ((int)value) + "%");
			}
			else if (tweak.getId()==R.id.pitch) {
				TTSPitch=Math.max(0, Math.min(TTSPitch+d, TTS_LEVLES_SPEED.length-1));
				value=TTS_LEVLES_SPEED[TTSPitch];
				a.showT("音调 " + value);
				tweak.setText(""+value);
				if (tts != null) tts.setPitch(value);
			}
			else if (tweak.getId()==R.id.speed) {
				TTSSpeed=Math.max(0, Math.min(TTSSpeed+d, TTS_LEVLES_SPEED.length-1));
				value=TTS_LEVLES_SPEED[TTSSpeed];
				tweak.setText(""+value);
				a.showT("语速"+ value + "倍");
				if (tts != null) tts.setSpeechRate(value);
			}
			if (playing && tts != null) {
				tts.stop();
				cachedEndIndex = -1;
				//sendText();
				ReadText(null, mCurrentReadContext);
			}
			if (a.m_currentToast != null) {
				a.m_currentToast.setGravity(Gravity.BOTTOM, 0, 175*2);
			}
		}
	}
	
	WebViewListHandler weblistHandler;
	ContentviewBinding contentUIData;
	
	public final SparseArray<ScrollerRecord> avoyager = new SparseArray<>();
	int avoyagerIdx=0;
	int adelta=0;
	long pressedRowId;
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (!isLongClick) {
			popupMenuHelper.dismiss();
			switch (v.getId()) {
			}
			return true;
		}
		return false;
	}
	
	public void setInvoker(BookPresenter invoker) {
		//CMN.debug("setInvoker::", invoker);
		this.invoker = invoker;
	}
	
	public void show() {
		if (!isVisible()) {
			toggle(a.root, null, -1);
		} else if (getLastShowType()==2) {
			ViewUtils.ensureTopmost(dialog, a, dialogDismissListener);
		}
	}
	
	@Override
	protected void showDialog() {
		//super.showDialog();
		BottomSheetDialog bPane = (BottomSheetDialog) dialog;
		if(bPane==null) {
			CMN.debug("重建底部弹出");
			dialog = bPane = new BottomSheetDialog(a);
			bPane.setContentView(settingsLayout);
			bPane.getWindow().setDimAmount(0.2f);
			//CMN.recurseLogCascade(lv);
		}
		bPane.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
		refreshExpand();
		super.showDialog();
	}
}