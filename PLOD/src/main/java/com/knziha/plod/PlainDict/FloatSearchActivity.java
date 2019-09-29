package com.knziha.plod.PlainDict;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.androidadvance.topsnackbar.TSnackbar;
import com.knziha.plod.dictionary.Flag;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.searchtasks.CombinedSearchTask;
import com.knziha.plod.widgets.RLContainerSlider;
import com.knziha.rbtree.additiveMyCpr1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;


/**
 * Created by KnIfER on 2018
 */
public class FloatSearchActivity extends MainActivityUIBase {
    private ViewGroup mainfv;
	private long exitTime = 0;

	boolean FVDOCKED=false;
	int FVW,FVH,FVTX,FVTY,FVW_UNDOCKED,FVH_UNDOCKED;
	final static int FVMINWIDTH=133;
	final static int FVMINHEIGHT=50;

	protected float _50_;
	
	private final TextWatcher tw1 = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	String keyTmp = mdict.processText(s.toString());
			if(keyTmp.length()>0){
				etSearch_ToToolbarMode(3);
				//webcontentlist.setVisibility(View.INVISIBLE);
				if(!bWantsSelection)
					webholder.removeAllViews();
				if(isCombinedSearching){
					if(lianHeTask!=null) {
						lianHeTask.cancel(false);
					}
					if(!checkDics()) return;

					if(lv2.getVisibility()==View.INVISIBLE)
						lv2.setVisibility(View.VISIBLE);
					lianHeTask = new CombinedSearchTask(FloatSearchActivity.this).execute(s.toString());
				}else
				try {
					if(!checkDics()) return;
					int res=currentDictionary.lookUp(""+s);
					if(res!=-1){ 
						lv.setSelection(res);
						//showT(proceed+""+bWantsSelection+" "+mdict.processText(currentDictionary.getEntryAt(res))+"=="+mdict.processText(keyTmp));

						if(bIsFirstLaunch||bWantsSelection) {
				        	if(mdict.processText(currentDictionary.getEntryAt(res)).equals(keyTmp)) {
				        		boolean proceed = true;
				        		if(webcontentlist.getVisibility()==View.VISIBLE) {//webSingleholder.getChildCount()!=1
				        			proceed = (adaptermy.currentKeyText == null || !keyTmp.equals(adaptermy.currentKeyText.trim()));
				        		}

					        	if(proceed) {
									adaptermy.onItemClick(res);
					        	}
				        	}
				        }
						
	        			bIsFirstLaunch=false;
					}
				} catch (Exception e) {e.printStackTrace();}
			}else if(lv2.getVisibility()==View.VISIBLE) 
				lv2.setVisibility(View.INVISIBLE);
        }  
          
        public void beforeTextChanged(CharSequence s, int start, int count,  
                int after) {  
              
        }  
          
        public void afterTextChanged(Editable s) {  
            //if (s.length() == 0) ivDeleteText.setVisibility(View.GONE);
            //else  ivDeleteText.setVisibility(View.VISIBLE);  
        	if (s.length() != 0) ivDeleteText.setVisibility(View.VISIBLE);
        }  
    };
	private int touch_id;

	@Override
	public void NotifyComboRes(int size) {
		if(opt.getNotifyComboRes()) {
			float fval = 0.5f;
			if(bIsFirstLaunch||bWantsSelection) {
				fval=1f;
			}
			Snack(main_succinct, fval, getResources().getString(R.string.cbflowersnstr,opt.lastMdPlanName,md.size(),size),TSnackbar.LENGTH_LONG);
		}
	}

	@Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
        // Checks the orientation of the screen




        if(chooseDFragment!=null) {
	        chooseDFragment.width=(int) (dm.widthPixels-2.5*getResources().getDimension(R.dimen.diagMarginHor));
	        chooseDFragment.height=(int) (dm.heightPixels-2*getResources().getDimension(R.dimen.diagMarginVer));
	        chooseDFragment.onResume();
        }
    	if(mainfv.getTranslationY()<0)
    		mainfv.setTranslationY(0);
    	if(mainfv.getTranslationX()<0)
    		mainfv.setTranslationX(0);
    	if(newConfig.screenHeightDp!=Configuration.SCREEN_WIDTH_DP_UNDEFINED)
    	if(mainfv.getTranslationY()>dm.heightPixels-_50_)
    		mainfv.setTranslationY(dm.heightPixels-_50_);
    	if(newConfig.screenWidthDp!=Configuration.SCREEN_WIDTH_DP_UNDEFINED)
    	if(mainfv.getTranslationX()>dm.widthPixels-_50_)
    		mainfv.setTranslationX(dm.widthPixels-_50_);
    	
		if(FVDOCKED) {
        	ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
			lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
			lpmy.height=(int) (dm.heightPixels-mainfv.getTranslationY())-(DockerMarginB+DockerMarginT);
    		mainfv.setLayoutParams(lpmy);
		}
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	long cur = System.currentTimeMillis();
    	if(cur-CMN.FloatLastInvokerTime<524) {
    		super.onCreate(savedInstanceState);
    		finish();
    		return;
    	}
    	CMN.FloatLastInvokerTime=cur;
        bShowLoadErr=false;
        
        super.onCreate(savedInstanceState);

    	overridePendingTransition(R.anim.budong,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        setContentView(R.layout.float_main);
		_50_= (FVMINHEIGHT*dm.density);
        wm = getWindowManager();

        mainfv = findViewById(R.id.main);

		FVDOCKED=opt.getFVDocked();
		//showT("FVDOCKED"+FVDOCKED);
		FVH=opt.defaultReader.getInt("FVH",(int) (500*dm.density));
		FVW=opt.defaultReader.getInt("FVW",dm.widthPixels);
		FVH_UNDOCKED=opt.defaultReader.getInt("UDFVH",-1);
		FVW_UNDOCKED=opt.defaultReader.getInt("UDFVW",-1);
		FVTX=Math.min(Math.max(opt.defaultReader.getInt("FVTX",0), 0), (int) (dm.widthPixels-_50_));
		FVTY=Math.min(Math.max(opt.defaultReader.getInt("FVTY",(int) (dm.heightPixels-500*dm.density)), 0), (int) (dm.heightPixels-_50_));
		
		mainfv.setTranslationY(FVTY);
		mainfv.setTranslationX(FVTX);

		checkLog(savedInstanceState);
    }
   
    private void setDocked(boolean docked) {
    	LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) main_succinct.getLayoutParams();
		if(docked) {
			lp.setMargins(0, 0, 0, 0);
		}else {
	    	int margin = (int) (2*dm.density);
			int margin2 = (int) (1*dm.density);
			lp.setMargins(margin2, 0, margin2, margin);
		}
		main_succinct.setLayoutParams(lp);
	}

	@Override
	protected void onDestroy(){
    	if(systemIntialized) {
    		dumpSettings();
        	root.getViewTreeObserver().removeOnGlobalLayoutListener(keyObserver);
        	keyObserver=null;
    	}
    	super.onDestroy();
    }

	@Override
	protected void scanSettings(){
		super.scanSettings();
		new File(opt.pathTo().toString()).mkdirs();
		CMN.FloatBackground = MainBackground = opt.getFloatBackground();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(MainBackground);
		}
		opt.getLastMdlibPath();
		if(opt.lastMdlibPath==null || !new File(opt.lastMdlibPath).exists()) {
			opt.lastMdlibPath = opt.pathToMain()+"mdicts";
	    	new File(opt.lastMdlibPath).mkdirs();
		}
	}

    View IMPageCover;
	private OnGlobalLayoutListener keyObserver;
	@Override
    protected void further_loading(final Bundle savedInstanceState) {
        contentview = findViewById(R.id.cover);
        PageSlider = (RLContainerSlider)  contentview;
        IMPageCover = findViewById(R.id.IMPageCover);
        toolbar = findViewById(R.id.toolbar);
        webcontentlist = findViewById(R.id.webcontentlister);
        bottombar2 = webcontentlist.findViewById(R.id.bottombar2);
        toolbar.inflateMenu(R.menu.float_menu);
        CachedBBSize=opt.getFloatBottombarSize((int) (20*dm.density));
    	super.further_loading(savedInstanceState);
    	
        main = main_succinct;
    	
        lv = findViewById(R.id.main_list);
        lv2 = findViewById(R.id.sub_list);
        lv.setAdapter(adaptermy = new ListViewAdapter()); 
        lv.setOnItemClickListener(adaptermy);
        lv2.setAdapter(adaptermy2 = new ListViewAdapter2());
        lv2.setOnItemClickListener(adaptermy2);

			Intent intent = getIntent();
	        String keytmp =	intent.getStringExtra("EXTRA_QUERY");
	        if(keytmp==null) {
	            String type = intent.getType();
		        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
		        	keytmp = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
		        }
	        }
	        
			etSearch.setText(keytmp);
	        etSearch.addTextChangedListener(tw1);  
	        bWantsSelection=true;
	    	tw1.onTextChanged(keytmp, 0, 0, 0);
		
        //manifestTV = (TextViewmy) findViewById(R.id.MANITV);
        //manifestTV.doit();
		
        //mainfv.getBackground().setTint(FloatBackground);
        //IMPageCover.getBackground().setTint(FloatBackground);
        mainfv.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);

        //键盘监听器
        root.getViewTreeObserver().addOnGlobalLayoutListener(keyObserver=new OnGlobalLayoutListener(){
			boolean keyBoardFlipper=false;
        	@Override
			public void onGlobalLayout() {
				//showT("onGlobalLayout");
        		int kb_height=isKeyboardShown(root);
        		if(keyBoardFlipper) {
        			if(kb_height<=0){
						keyBoardFlipper=false;
						//showT("onGlobalLayout_kn_hide");
					}
        		}else {
					if(kb_height>0) {
						keyBoardFlipper=true;
						//showT("onGlobalLayout_isKeyboardShown");
						FrameLayout.LayoutParams  lpmy = (android.widget.FrameLayout.LayoutParams) mainfv.getLayoutParams();
						wm.getDefaultDisplay().getMetrics(dm);
						if(mainfv.getTranslationY()>dm.heightPixels - kb_height - 50*dm.density) {
							int newTransY = (int) (dm.heightPixels - kb_height - 100*dm.density);
							mainfv.setTranslationY(newTransY);
							if(FVDOCKED) {
								lpmy.height=dm.heightPixels-newTransY-(DockerMarginB+DockerMarginT);
								mainfv.setLayoutParams(lpmy);
							}
							//showT("sdjusted"+dm.heightPixels);
						}
					}
        		}
			}});

		GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
			public boolean onDoubleTap(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (true && touch_id!=R.id.move0) {
					finish();
					return true;
				}
				return super.onSingleTapUp(e);
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (false) {
					finish();
					return true;
				}
				return super.onSingleTapConfirmed(e);
			}
		});

		OnTouchListener Toucher = new OnTouchListener(){
        	float lastX;
        	float lastY;
        	boolean wantsMaximize=false;
        	boolean wantedMaximize=false;
        	float DedockTheta;
        	float DedockAcc;
			@Override 
			public boolean onTouch(View v, MotionEvent e) {
				DedockTheta=_50_/2;
				touch_id=v.getId();
				mGestureDetector.onTouchEvent(e);
				ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				switch(e.getAction()){
					case MotionEvent.ACTION_DOWN:{
						lastX = e.getRawX();
						lastY = e.getRawY();
						DedockAcc=0;
					} break;
					case MotionEvent.ACTION_MOVE:{
						int dy = (int) (e.getRawY() - lastY);
						int dx = (int) (e.getRawX() - lastX);
						boolean bProceed=true;
						boolean MOT=false,MOB=false,MOL=false,MOR=false;
						wantsMaximize=false;
						if (touch_id == R.id.move0) {
							MOT = true;
							if (FVDOCKED) {//解Dock
								DedockAcc += dx;
							}
							if (DedockAcc > DedockTheta) {
								if (FVDOCKED)
									//if (bREMUDSIZE) {
									if (FVW_UNDOCKED != -1 && FVH_UNDOCKED != -1) {
										lpmy.width = FVW_UNDOCKED;
										lpmy.height = FVH_UNDOCKED;
										mainfv.setLayoutParams(lpmy);
									}
								opt.setFVDocked(FVDOCKED = false);
							}

							if (!FVDOCKED) {//未停靠
								bProceed = false;
								mainfv.setTranslationY(Math.min(dm.heightPixels - _50_, Math.max(mainfv.getTranslationY() + dy, 0)));
								mainfv.setTranslationX(Math.min(dm.widthPixels - _50_, Math.max(mainfv.getTranslationX() + dx + DedockAcc, 0)));//应用累积项
								DedockAcc = 0;
								setDocked(false);
								if (mainfv.getTranslationX() <= 1.45) {
									wantsMaximize = true;
									if (!wantedMaximize) {
										lpmy.width = (int) (lpmy.width + _50_);
										lpmy.height = (int) (lpmy.height + _50_);
										mainfv.setLayoutParams(lpmy);
										wantedMaximize = true;
									}
								} else if (wantedMaximize) {
									lpmy.width = (int) (lpmy.width - _50_);
									lpmy.height = (int) (lpmy.height - _50_);
									setDocked(true);
									mainfv.setLayoutParams(lpmy);
									wantedMaximize = false;
								}
							}
						}
						if(bProceed){
							if(MOT) {

							}else {
								if(lastY<=mainfv.getTranslationY())
									MOT=true;
								if(lastY>=mainfv.getTranslationY()+lpmy.height)
									MOB=true;
								if(lastX<=mainfv.getTranslationX())
									MOL=true;
								if(lastX>=mainfv.getTranslationX()+lpmy.width)
									MOR=true;
							}

							if(MOT) {//move on the top
								if(lpmy.height-dy<=_50_ && dy>0) {//size trim
									dy=(int) (lpmy.height-_50_);
								}
								if(lpmy.height-dy>dm.heightPixels) dy=0;
								int newTransY = (int) (mainfv.getTranslationY()+dy);
								lpmy.height=Math.min(lpmy.height-dy, dm.heightPixels-newTransY-(DockerMarginB+DockerMarginT));//screen culling
								mainfv.setLayoutParams(lpmy);

								//int newTop = (int) (mainfv.getTop() + dy);
								mainfv.setTranslationY(newTransY);
							}else if(MOB){//move on the bottom
								if(lpmy.height+dy<=_50_ && dy<0) {//size trim
									dy=(int) (_50_-lpmy.height);
								}
								lpmy.height=lpmy.height+dy;
								mainfv.setLayoutParams(lpmy);
							}

							if(MOL){//move on the left
								if(lpmy.width-dx<=FVMINWIDTH*dm.density) {//size trim
									dx=(int) (lpmy.width-FVMINWIDTH*dm.density);
								}
								int newTransX = (int) (mainfv.getTranslationX()+dx);
								lpmy.width=Math.min(lpmy.width-dx, dm.widthPixels-newTransX-(DockerMarginR+DockerMarginL));
								mainfv.setLayoutParams(lpmy);
								mainfv.setTranslationX(newTransX);
							}else if(MOR){//move on the right
								if(lpmy.width+dx<=FVMINWIDTH*dm.density) {//size trim
									dx=(int) (FVMINWIDTH*dm.density-lpmy.width);
								}
								lpmy.width=lpmy.width+dx;
								mainfv.setLayoutParams(lpmy);
							}
						}
						//mainfv.setBottom(dm.heightPixels);
						//ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
						//lpmy.height=newTop;
						//mainfv.setLayoutParams(lpmy);
						//mainfv.postInvalidate();
						lastX = e.getRawX();
						lastY = e.getRawY();
					} break;
					case MotionEvent.ACTION_UP:{
						if(wantsMaximize) {
							FVW_UNDOCKED=(int) (lpmy.width-_50_);
							FVH_UNDOCKED=(int) (lpmy.height-_50_);
							lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
							lpmy.height=(int) (dm.heightPixels-mainfv.getTranslationY()-(DockerMarginB+DockerMarginT));
							mainfv.setTranslationX(0);
							mainfv.setLayoutParams(lpmy);
							setDocked(true);
							wantsMaximize=false;
							wantedMaximize=false;
							opt.setFVDocked(FVDOCKED=true);
						}
					} break;
					default:
					break;
				}
				return true;
			
		}};
		findViewById(R.id.move0).setOnTouchListener(Toucher);
        root.setOnTouchListener(Toucher);

        findViewById(R.id.toolbar_action1).setOnLongClickListener(this);
        
        
    	systemIntialized=true;
    	
		File additional_config = new File(opt.pathToMain()+"appsettings.txt");
		if(additional_config.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(additional_config));
				String line;
					while((line=in.readLine())!=null) {
						String[] arr = line.split(":", 2);
						if(arr.length==2) {
							if(arr[0].equals("float window margin")||arr[0].equals("浮动窗体边框")) {
								arr = arr[1].split(" ");
								if(arr.length==4) {
									try {
										MarginLayoutParams lp = (MarginLayoutParams) root.getLayoutParams();
										DockerMarginL = lp.leftMargin=Integer.valueOf(arr[2]);
										DockerMarginR = lp.rightMargin=Integer.valueOf(arr[3]);
										DockerMarginT = lp.topMargin=Integer.valueOf(arr[0]);
										DockerMarginB = lp.bottomMargin=Integer.valueOf(arr[1]);
										root.setLayoutParams(lp);
									} catch (Exception ignored) {}
								}
							}
						}
					}
			} catch (Exception ignored) {}
		}


		ViewGroup.LayoutParams  lpmy = mainfv.getLayoutParams();
		if(!FVDOCKED) {
			lpmy.width=FVW_UNDOCKED;
			lpmy.height=FVH_UNDOCKED;
			setDocked(false);
			mainfv.setLayoutParams(lpmy);
		}else {
			lpmy.width=dm.widthPixels-(DockerMarginR+DockerMarginL);
			lpmy.height=dm.heightPixels-FVTY-(DockerMarginB+DockerMarginT);
			mainfv.setLayoutParams(lpmy);
		}
        if(!opt.getFloatBottombarOnBottom())
        	webcontentlist.SwitchingSides();
    }

	

	private int isKeyboardShown(View rootView) {
		Rect r = new Rect();
		rootView.getWindowVisibleDisplayFrame(r);
		return rootView.getBottom() - r.bottom;// > softKeyboardHeight * CMN.dm_density;
	}
	

	private void dumpSettings(){
		if(systemIntialized) {
			android.view.ViewGroup.LayoutParams lp = mainfv.getLayoutParams();

			opt.setFloatBottombarOnBottom(webcontentlist.multiplier==-1);
			Editor putter = opt.defaultputter();
			putter.putLong("MFF", opt.FirstFlag())//FVDOCKED
			.putInt("FVH",lp.height)
			.putInt("FVW",lp.width)
			.putInt("FVTX",(int) mainfv.getTranslationX())
			.putInt("FVTY",(int) mainfv.getTranslationY())
			.putInt("UDFVW",FVW_UNDOCKED)
			.putInt("UDFVH",FVH_UNDOCKED)
			.putInt("FBBS",webcontentlist.getPrimaryContentSize())//FloatBottombarSize
			.commit();
		}
	} 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
	//private void invalid_toolBarIcons() {
	//	if(isCombinedSearching){
	//		((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action1)).setIcon((ContextCompat.getDrawable(this,R.drawable.ic_btn_multimode)));
	//	}else{
	//		((MenuItem)toolbar.getMenu().findItem(R.id.toolbar_action1)).setIcon((ContextCompat.getDrawable(this,R.drawable.ic_btn_siglemode)));
	//	}				
	//}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.toolbar_action1) {// do something
			return true;
		}// If we got here, the user's action was not recognized.
		// Invoke the superclass to handle it.
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onPause() {
    	Log.e("onPause","onPause");
        super.onPause();
    }
	
	@Override
    protected void onResume() {
		Log.e("onResume","onResume");
        super.onResume();
        if(systemIntialized) {
	        if(CMN.FloatBackground != MainBackground) {
	        	MainBackground=CMN.FloatBackground;
                mainfv.getBackground().setColorFilter(MainBackground, PorterDuff.Mode.SRC_IN);
	        }
        }
    }
	
	@Override
    protected void onRestart() {
		Log.e("onRestart","onRestart");
        super.onRestart();
    }
	@Override
    protected void onStart() {
		//scanSettings();
		Log.e("onStart","onStart");
        super.onStart();
    }	
	@Override
    protected void onStop() {
		super.onStop();
		Log.e("onStop", "onStop");
		//webholder.removeAllViews();
		super.onStart();
	}

    public class ListViewAdapter extends BasicAdapter {
        //构造
        public ListViewAdapter() 
        {  
        }
        
        @Override
        public int getCount() {  
        	if(md.size()>0 && currentDictionary!=null)
        		return (int) currentDictionary.getNumberEntries();
        	else
        		return 0;
        }  
        @Override
        public View getItem(int position) {
			return null;
			}    
        @Override
        public long getItemId(int position) {    
          return position;    
        }  
        Flag mFlag = new Flag();
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	viewHolder vh;
        	String currentKeyText = currentDictionary.getEntryAt(position, mFlag);
	        //String keyText = md.get(adapter_idx).getEntryAt(position);
	        if(convertView!=null){
        		vh=(viewHolder)convertView.getTag();
        	}else{
        		convertView = View.inflate(getApplicationContext(), R.layout.listview_item0, null);
        		vh=new viewHolder();
        		vh.title = convertView.findViewById(R.id.text);
        		vh.subtitle = convertView.findViewById(R.id.subtext);
                convertView.setTag(vh);
        	}
	        

            vh.title.setText(currentKeyText);
            if(mFlag.data!=null)
                vh.subtitle.setText(Html.fromHtml(currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+ mFlag.data+" ></font >"));
            else
            	vh.subtitle.setText(currentDictionary._Dictionary_fName);
        	//convertView.setTag(R.id.position,position);
        	return convertView;
        }
    
        @Override
        public void onItemClick(int position) {//lv1

        	super.onItemClick(position);
        	ActivedAdapter=this;
        	if(position<-1)
        		return;
        	//-1放行
        	if(position>=getCount()) {
        		lastClickedPos = getCount()-1;
        		show(R.string.endendr);
        		return;
    		}
        	
        	if(webSingleholder.getVisibility()!=View.VISIBLE)webSingleholder.setVisibility(View.VISIBLE);
	    	if(WHP.getVisibility()==View.VISIBLE) {
			    if(webholder.getChildCount()!=0)
			    	webholder.removeAllViews();
			    WHP.setVisibility(View.GONE);
	    	}
        	if(widget14.getVisibility()==View.VISIBLE) {
	        	widget13.setVisibility(View.GONE);
	        	widget14.setVisibility(View.GONE);
        	}
        	
        	webcontentlist.setVisibility(View.VISIBLE);
			etSearch_ToToolbarMode(1);
        	//CMN.show("onItemClick"+position);
			ViewGroup someView = currentDictionary.rl;
			if(someView!=null && someView.getParent()!=null)
				((ViewGroup)someView.getParent()).removeView(someView);
			
			webholder.removeAllViews();

        	if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(mainfv.getWindowToken(),0);
				etSearch.clearFocus();
        	}
        	
			currentDictionary.initViewsHolder(FloatSearchActivity.this);
			webSingleholder.addView(md.get(adapter_idx).rl);
			currentDictionary.renderContentAt(-1,adapter_idx,null,position);
			
        	
			currentKeyText = currentDictionary.getEntryAt(position);
			bWantsSelection=true;

			decorateContentviewByKey(null,currentKeyText);
			historyCon.insertUpdate(currentKeyText);
			currentDictionary.rl.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			currentDictionary.mWebView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    
    }
    
    class ListViewAdapter2 extends  BasicAdapter{
    	int itemId = R.layout.listview_item1;
        //构造函数
        
        @Override
        public int getCount() {
        	if(combining_search_result==null)
        		return 0;
            return combining_search_result.size();
        }  
        @Override
        public View getItem(int position) {
			return null;
			}    
        @Override
        public long getItemId(int position) {    
          return position;    
        }  
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
        	viewHolder vh;

        	CharSequence currentKeyText = combining_search_result.getResAt(position);
	        
	        if(convertView!=null){
	        		//标题
	        		vh=(viewHolder)convertView.getTag();
	        	}else{
	                //标题
	        		convertView = View.inflate(getApplicationContext(),itemId, null);
	    			//convertView.setOnClickListener(this);
	        		vh=new viewHolder();
	        		vh.title = convertView.findViewById(R.id.text);
	        		vh.subtitle = convertView.findViewById(R.id.subtext);
                	vh.cc = convertView.findViewById(R.id.counter);
	                convertView.setTag(vh);
	        	}
	        
	        if(combining_search_result.dictIdx>=md.size()) return convertView;//不要Crash哇
        	
	        vh.title.setText(currentKeyText);
            mdict _currentDictionary = md.get(combining_search_result.dictIdx);
            if(combining_search_result.mflag.data!=null)
                vh.subtitle.setText(Html.fromHtml(_currentDictionary._Dictionary_fName+"<font color='#2B4391'> < "+combining_search_result.mflag.data+" ></font >"));
            else
                vh.subtitle.setText(_currentDictionary._Dictionary_fName);

        	vh.cc.setText(combining_search_result.count);

	        return convertView;
        }
        
        @Override
        public void onItemClick(int pos){//lv2
        	super.onItemClick(pos);
        	ActivedAdapter=this;
        	webcontentlist.setVisibility(View.VISIBLE);
			etSearch_ToToolbarMode(1);
        	if(pos<0 )
        		return;
        	if(pos>=getCount()) {
        		lastClickedPos = getCount()-1;
        		show(R.string.endendr);
        		return;
    		}
        	ActivedAdapter=this;
        	
        	if(WHP.getVisibility()!=View.VISIBLE)WHP.setVisibility(View.VISIBLE);
        	WHP.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
	    	if(webSingleholder.getVisibility()==View.VISIBLE) {
			    if(webSingleholder.getChildCount()!=0)
			    	webSingleholder.removeAllViews();
			    webSingleholder.setVisibility(View.GONE);
	    	}
	    	
        	if(widget14.getVisibility()!=View.VISIBLE) {
	        	widget13.setVisibility(View.VISIBLE);
	        	widget14.setVisibility(View.VISIBLE);
        	}
        	
        	lastClickedPos = pos;
        	if(!bWantsSelection) {
				imm.hideSoftInputFromWindow(mainfv.getWindowToken(),0);
				etSearch.clearFocus();
        	}
        	
        	
			combining_search_result.renderContentAt(lastClickedPos,FloatSearchActivity.this,this);//webholder
			
			decorateContentviewByKey(null,currentKeyText);
			historyCon.insertUpdate(currentKeyText);
			//CMN.lastHisLexicalEntry = null;
			//historyCon.insertUpdate(currentKeyText);
			//showT("查时: "+(System.currentTimeMillis()-stst));
			bWantsSelection=true;
        }
    };
    
    static class viewHolder{
    	private TextView title;
    	private TextView subtitle;
    	private TextView cc;
    }

	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch(id) {
			case R.id.toolbar_action1:{
				opt.setCombinedSearching(isCombinedSearching = !isCombinedSearching);
				if(isCombinedSearching){
					if(webcontentlist.getVisibility()==View.VISIBLE)
						adaptermy2.currentKeyText=null;
					toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((getResources().getDrawable(R.drawable.ic_btn_multimode)));
					lv2.setVisibility(View.VISIBLE);
				}else{
					if(webcontentlist.getVisibility()==View.VISIBLE)
						adaptermy.currentKeyText=null;
					toolbar.getMenu().findItem(R.id.toolbar_action1).setIcon((getResources().getDrawable(R.drawable.ic_btn_siglemode)));
					lv2.setVisibility(View.GONE);
				}
				if(opt.auto_seach_on_switch)
					tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
			} break;
			case R.id.ivDeleteText:{
				if((etSearch_toolbarMode&2)==0) {//delete
					String SearchTmp = etSearch.getText().toString().trim();
					if(SearchTmp.equals("")) {
						ivDeleteText.setVisibility(View.GONE);
					}else {
						lastEtString=SearchTmp;
						etSearch.setText(null);
						etSearch_ToToolbarMode(4);
					}
				}else {//undo
					etSearch.setText(lastEtString);
					//etSearch_ToToolbarMode(3);
				}
			} break;
			case R.id.ivBack:{
				if((etSearch_toolbarMode&1)==0) {//search
					//bWantsSelection=true;
					if(etSearch.getText().toString().trim().length()>0) {
						bIsFirstLaunch=true;
						tw1.onTextChanged(etSearch.getText(), 0, 0, 0);
					}
				}else {//back
					webcontentlist.setVisibility(View.GONE);
					bWantsSelection=false;
					if(webSingleholder.getChildCount()!=0) {
						webSingleholder.removeAllViews();
					}
					webholder.removeAllViews();
					etSearch_ToToolbarMode(0);
				}
			} break;
			case R.id.browser_widget7:
				exitTime=0;
				etSearch_ToToolbarMode(0);
	        	webcontentlist.setVisibility(View.GONE);
			break;
			case R.id.browser_widget8:{//favorite
				if(star_ic==null) {
					star_ic = getResources().getDrawable(R.drawable.star_ic_solid);
					star = favoriteBtn.getDrawable();
				}
				favoriteBtn.setImageDrawable(star);
				String key = ActivedAdapter.currentKeyText;
				if(ActivedAdapter==adaptermy) {
					int pos = currentDictionary.currentPos;
					while(pos-1>=0 && currentDictionary.getEntryAt(pos-1).equals(key)) {
						pos--;
					}
					pos = currentDictionary.currentPos - pos;
					if(pos>0) {
						for(int i=0;i<pos;i++)
							key += "\n";
					}
					//CMN.show("pos"+pos);
				}
				favoriteCon.prepareContain();
				if(favoriteCon.contains(key)) {
					favoriteCon.remove(key);
					favoriteBtn.setImageDrawable(star);
					show(R.string.removed);
				}else {
					favoriteCon.insert(key);
					favoriteBtn.setImageDrawable(star_ic);
					show(R.string.added);
				}
			} break;
			case R.id.browser_widget9:{//view outline
				if(ActivedAdapter==adaptermy2) {
					final resultRecorderCombined res;
					int idx;
					res = (resultRecorderCombined) adaptermy2.combining_search_result;
					idx = adaptermy2.lastClickedPos;
					if(idx<0 || idx>=res.list().size())
						return;

					additiveMyCpr1 contentIndexes = res.list().get(idx);
					List<Integer> vals = (List<Integer>)contentIndexes.value;
					CharSequence[] items = new CharSequence[webholder.getChildCount()];
					int c=0;
					int totalHeight=0;
					int selectedPos=-1;
					final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
					for(int i=0;i<vals.size();i+=2) {
						int lastIdx = vals.get(i);
						i+=2;
						while(i<vals.size() && lastIdx==vals.get(i))
							i+=2;
						i-=2;
						if(selectedPos==-1) {
							totalHeight+=webholder.getChildAt(c).getMeasuredHeight();
							if(totalHeight>currentHeight)
								selectedPos=c;
						}
						mdict mdTmp = md.get(lastIdx);
						if(mdTmp.cover!=null) {
							mdTmp.cover.setBounds(0, 0, 50, 50);
							SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(mdTmp._Dictionary_fName);
							ssb.setSpan(new ImageSpan(mdTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							items[c] = ssb;
						}
						else
							items[c] = mdTmp._Dictionary_fName;
						c++;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.DialogStyle);
					builder.setTitle("跳转");
					builder.setSingleChoiceItems(items, selectedPos, (dialog, pos) -> {
						int totalHeight1 =0;
						for(int i=0;i<pos;i++) {
							totalHeight1 +=webholder.getChildAt(i).getHeight();
						}

						//((ScrollView)webholder.getParent()).setScrollY(totalHeight);
						((ScrollView)webholder.getParent()).smoothScrollTo(0, totalHeight1);
						d.dismiss();
					});
					d=builder.create();
					d.setCanceledOnTouchOutside(true);
					d.show();
				}else
					showX(R.string.try_longpress,0);
			} break;
			case R.id.browser_widget13:
			case R.id.browser_widget14:{
				boolean is_14=id==R.id.browser_widget14;
				final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
				int totalHeight=0;
				for(int i=0;i<webholder.getChildCount();i++) {
					totalHeight+=webholder.getChildAt(i).getHeight();
					if(totalHeight+(is_14?1:0)>currentHeight) {
						if(is_14)
							totalHeight-=webholder.getChildAt(i).getHeight();
						break;
					}
				}

				//((ScrollView)webholder.getParent()).setScrollY(totalHeight);
				((ScrollView)webholder.getParent()).smoothScrollTo(0, totalHeight);

			} break;
			case R.id.browser_widget10:
			case R.id.browser_widget11:{//左zuo
				int toPos = ActivedAdapter.lastClickedPos+(v.getId()==R.id.browser_widget10?-1:1);
				if(toPos<-1) {
					show(R.string.coverr);
					break;
				}
				if(toPos==-1 && adaptermy2==ActivedAdapter) {
					show(R.string.toptopr);
					break;
				}
				if(lv2.getVisibility()==View.VISIBLE){
					for(int i=0;i<md.size();i++){
						md.get(i).clearWebview();
					}
				}else{
					webholder.removeAllViews();
					currentDictionary.clearWebview();
				}
				//Log.e("browser_widget10","browser_widget10"+ActivedAdapter.lastClickedPos);
				//webholder.removeViews(1, webholder.getChildCount()-1);
				ActivedAdapter.onItemClick(toPos);
			} break;
			case R.id.browser_widget12:{
				if(currentDictionary!=null) {
					boolean played=false;
					mdict Actor;
					if(ActivedAdapter!=adaptermy2) {
						Actor=currentDictionary;
					}else {
						Actor=md.get(adaptermy2.combining_search_result.getRecordAt(adaptermy2.lastClickedPos).get(0));
					}
					if(Actor.mdd!=null) {
						String sKey = ActivedAdapter.currentKeyText+".mp3";
						int idx = Actor.mdd.lookUp(sKey);
						if(idx!=-1) {
							Actor.mWebView.evaluateJavascript("var audio = new Audio(\""+sKey+"\");audio.play();", null);
							played=true;
						}
						if(!played) {
							Log.e("dsa_evaluateJavascript","asd");
							Actor.mWebView.evaluateJavascript("(function(){var hrefs = document.getElementsByTagName('a'); for(var i=0;i<hrefs.length;i++){ if(hrefs[i].attributes['href']){ if(hrefs[i].attributes['href'].value.indexOf('sound')!=-1){ hrefs[i].click(); return 10; } } }return null;})();", value -> {
								if(!value.equals("10")) {
									showT("找不到音频");
								}
							});
						}
					}
				}
			} break;
		}
	}

	DictPicker chooseDFragment;
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MenuItemImpl mmi = (MenuItemImpl)item;
		boolean isLongClicked=mmi.isLongClicked;
		switch (item.getItemId()) {
            case R.id.toolbar_action2://切换词典
            	if(chooseDFragment==null) {
            		chooseDFragment = new DictPicker(FloatSearchActivity.this);
    		        //chooseDFragment.setStyle(R.style.DialogStyle, 0);//DialogFragment.STYLE_NO_TITLE
    		        chooseDFragment.bShouldCloseAfterChoose=true;
    		        //chooseDFragment.setCancelable(true);
    		        //chooseDFragment.setOnViewCreatedListener(new OnViewCreatedListener() {
    				//	@Override
    				//	public void OnViewCreated(Dialog dialog) {
    				//		dialog.setCanceledOnTouchOutside(true);
    				//		Window window = dialog.getWindow();
    				//	}});
    		        chooseDFragment.width=(int) (dm.widthPixels-2*getResources().getDimension(R.dimen.diagMarginHor));
    		        chooseDFragment.mMaxH=(int) (dm.heightPixels-2*getResources().getDimension(R.dimen.diagMarginVer));
    		        chooseDFragment.height=-2;
            	}
		        chooseDFragment.show(getSupportFragmentManager(), "PickDictDialog");

		        /*DidialogHolder = (ViewGroup) findViewById(R.id.dialog_); 
            	if(dialogHolder.getVisibility()==View.VISIBLE) {
					dialogHolder.setVisibility(View.GONE);
					break;
				}
				if(!isFragInitiated) {
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					pickDictDialog = new DialogFragment1(this);  
		            transaction.add(R.id.dialog_, pickDictDialog);
		            transaction.commit();
		            isFragInitiated=true;
		            //pickDictDialog.mRecyclerView.scrollToPosition(adapter_idx);
				}
				else//没办法..
					pickDictDialog.refresh();*/
				break;
            case R.id.toolbar_action3://切换分组
            	showChooseSetDialog();
				break;
            case R.id.toolbar_action4:
            	String keyword = etSearch.getText().toString().trim();
            	if(favoriteCon.insertUpdate(keyword)>0)
            		showT("已收藏！");
            break;
        }
		closeIfNoActionView(mmi);
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
			case R.id.toolbar_action1:
	            //dumpSettings();
				finish();
				//System.exit(0);
			break;
			case R.id.browser_widget9://long-click view outline
				if(ActivedAdapter==adaptermy2) {
					resultRecorderCombined res;
					int idx;
					
					res = (resultRecorderCombined) adaptermy2.combining_search_result;					
					idx = adaptermy2.lastClickedPos;
					if(idx<0 || idx>=res.list().size())
						return true;
	
					int totalHeight=0;
					int selectedPos=-1;
					final int currentHeight=((ScrollView)webholder.getParent()).getScrollY();
					View itemTo = null;
					for(int i=0;i<webholder.getChildCount();i+=1) {
						itemTo = webholder.getChildAt(i);
						totalHeight+=itemTo.getMeasuredHeight();
						if(totalHeight>currentHeight) {
							selectedPos=i;
							break;
						}
					}
					if(selectedPos!=-1)
						itemTo.findViewById(R.id.cover).performClick();
					
				}else
					currentDictionary.rl.findViewById(R.id.cover).performClick();
			return true;
			}
		return false;
	}
}