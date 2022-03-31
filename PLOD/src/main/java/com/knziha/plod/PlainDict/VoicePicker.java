package com.knziha.plod.plaindict;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;
import androidx.fragment.app.DialogFragment;

import com.knziha.plod.dictionarymanager.files.ArrayTreeList;
import com.knziha.plod.settings.DevOpt;
import com.knziha.plod.widgets.Framer;
import com.knziha.plod.widgets.ViewUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/** Pick voice for a tts engine.
 @deprecated TTS settings sucks. don't use this! */
@Deprecated
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VoicePicker extends DialogFragment
{
	private final Comparator<Voice> mComparator;
	MainActivityUIBase a;
	HashMap<String, Voicemy> Overflow = new HashMap<>();
	ArrayTreeList<Voice> data;
	ArrayTreeList<Voicemy> hiddenParents;
	private MyAdapter mAdapter;

	public VoicePicker() {
		this(null);
	}
	VoicePicker(MainActivityUIBase a_){
		super();
		mComparator = new Comparator<Voice>() {
			@Override
			public int compare(Voice o1, Voice o2) {
				boolean b1=o2 instanceof Voicemy,b2=o1 instanceof Voicemy;
				Locale l1 =  o1.getLocale();
				Locale l2 =  o2.getLocale();
				String THIS=l1.getLanguage();
				String other=l2.getLanguage();
				int ret=THIS.compareTo(other);
				if(!b1 && !b2 ){
					if(ret!=0)
						return ret;//先比较父文件夹
				}else if(b1 && ! b2) {
					if(ret==0)//有无父子关系1，父在前
						return 1;
				}else if(!b1 && b2) {//有无父子关系2，父在前
					if(ret==0)
						return -1;
				}
				if(ret==0) ret = o1.getLocale().getCountry().compareTo(o2.getLocale().getCountry());
				if(ret==0) ret = o1.getName().compareTo(o2.getName());
				return ret;
			}
		};
		data=new ArrayTreeList<>(mComparator);
		hiddenParents=new ArrayTreeList<>(mComparator);
		if(a_!=null){
			a = a_;
			//ArrayList<Voice> arrayList = data.getList();
			/* pull data */
			Set<Voice> all = a.TTSController_engine.getVoices();
			data.getList().ensureCapacity(all.size()*2);
			for (Voice vI:all) {
				if(!vI.getFeatures().contains("notInstalled")) {
					data.insert(vI);
					String language = vI.getLocale().getLanguage();
					Voicemy parent = Overflow.get(language);
					if (parent == null)
						Overflow.put(language, parent = new Voicemy(vI.getName(), vI.getLocale(), 110, 110, false, vI.getFeatures()));
					data.insertOverFlow(parent);
				}
			}
			ArrayList<Voice> list = data.getList();
			for(int i=0;i<list.size();i++) {
				Voice vcTmp = list.get(i);
				if(vcTmp instanceof Voicemy) {
					Voicemy vcTmpmy = ((Voicemy) vcTmp);
					hiddenParents.insert((Voicemy) vcTmp);
					vcTmpmy.shrinked=0;
					for(i++;i<list.size();i++) {
						if(!Voicemy.isDirScionOf(list.get(i), vcTmpmy)) {break;}
						if(list.get(i) instanceof Voicemy) {break;}
						vcTmpmy.shrinked++;
					}
					i--;
				}
			}
		}
	}

	@Override
	public void onAttach(Context context){
		super.onAttach(context);
		//CMN.Log("dict picker onAttach");
	}
	ListView mListView;

	private AdapterView.OnItemClickListener OIC = new AdapterView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CMN.Log(data.getList().get(position).getLocale().getDisplayLanguage());
			CMN.Log(data.getList().get(position).getLocale().getLanguage());
		}
	};

	public boolean isDirty=false;
	private Framer root;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		root = (Framer) inflater.inflate(R.layout.dialog_2_tts_fc,
				a==null?null:a.root, false);
		//view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
		//view.setLayoutParams(new LayoutParams(-2,-1));
		//getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mListView = root.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter = new MyAdapter());
		mListView.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
		mListView.setVerticalScrollBarEnabled(true);
		mListView.setOnItemClickListener(OIC);
		//mAdapter.setOnItemClickListener(OIC);
		//view.setBackgroundResource(R.drawable.popup_shadow_l);
		return root;
	}

	int  width=-1,height=-1,mMaxH=-1;
	public void onResume()
	{
		super.onResume();
		if(width!=-1 || height!=-1)
		if(getDialog()!=null) {
			Window window = getDialog().getWindow();
			if(window!= null) {
				WindowManager.LayoutParams  attr = window.getAttributes();
				if(attr.width!=width || attr.height!=height) {
					//CMN.Log("onResume_");
					window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					window.setDimAmount(0.1f);
					//window.setBackgroundDrawableResource(R.drawable.popup_shadow_l);
					root.mMaxHeight=mMaxH;
					window.setLayout(width,height);
				}
			}
			getDialog().setCanceledOnTouchOutside(true);
		}
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide title of the dialog
		setStyle(STYLE_NO_FRAME, 0);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(MainActivityUIBase) getActivity();
		if(GlobalOptions.isDark) {
			ViewUtils.setListViewScrollbarColor(mListView, true);
		}
	}

	public int getItemLayout() {
		return R.layout.dict_manager_dslitem3;
	}

	private class MyAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			int ret=data.size();
			for(Voicemy vcTmp:hiddenParents.getList())
				ret-=vcTmp.shrinked;
			return ret;
		}

		@Override
		public Voice getItem(int position) {
			return data.getList().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Voice indexor = data.getList().get(position);
			ArrayList<Voicemy> pL = hiddenParents.getList();
			int acc=0;
			int pIdx=0;
			while(true) {
				int accLet=0;
				while(pIdx<pL.size() && mComparator.compare(pL.get(pIdx), indexor)<0) {
					accLet+=pL.get(pIdx++).shrinked;
				}
				if(accLet==0)
					break;
				acc+=accLet;
				indexor=data.getList().get(position+acc);
			}
			final int pos = position+acc;
			if(convertView==null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
				final ViewHolder vh=new ViewHolder();
				vh.ck = convertView.findViewById(R.id.ck);
				vh.ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						//CMN.show("onCheckedChanged");

					}});
				vh.folderIcon = (convertView.findViewById(R.id.folderIcon));
				vh.folderIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Voicemy vcTmp = (Voicemy) vh.dataLet;
						int pos = data.indexOf(vcTmp);
						if(vcTmp.shrinked==0) {
							hiddenParents.insert(vcTmp);
							vcTmp.shrinked=0;
							for(int i=pos+1;i<data.getList().size();i++) {
								if(!Voicemy.isDirScionOf(data.getList().get(i), vcTmp))
									break;
								if(data.getList().get(i) instanceof Voicemy)
									break;
								vcTmp.shrinked++;
							}
							notifyDataSetChanged();
							//CMN.show(""+adapter.getCount());
						}else {
							vcTmp.shrinked=0;
							hiddenParents.remove(vcTmp);
							notifyDataSetChanged();
							//CMN.show("22 "+adapter.getCount());
						}
					}});
				vh.text= convertView.findViewById(R.id.text1);
				vh.subtext= convertView.findViewById(R.id.text2);
				vh.text.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
				convertView.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
				convertView.setLayoutParams(convertView.getLayoutParams()) ;
				vh.splitterIcon= convertView.findViewById(R.id.splitterIcon);
				vh.drag_handle=convertView.findViewById(R.id.drag_handle);
				vh.drag_handle.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					}});

				convertView.setTag(vh);
			}

			ViewHolder vh=(ViewHolder) convertView.getTag();
			//vh.position=pos;
			//vh.dataLet=data.getList().get(pos);
			if(false) {
//				vh.ck.setVisibility(View.VISIBLE);
//				if(Selection.contains(getItem(pos).getAbsolutePath()))
//					vh.ck.setChecked(true);
//				else
//					vh.ck.setChecked(false);
			}else {
				vh.ck.setChecked(false);
				vh.ck.setVisibility(View.GONE);
			}

			final Voice vcTmp = getItem(pos);
			vh.dataLet=vcTmp;

			//if(vcTmp.cover!=null) {
			//	SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(vcTmp._Dictionary_fName);
			//	vcTmp.cover.setBounds(0, 0, 50, 50);
			//	ssb.setSpan(new ImageSpan(vcTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			//	((TextView)v.findViewById(R.id.text)).setText(ssb);
			//}else
//			String AssetInternalname = null;
//			if(vcTmp.getClass() == mAssetFile.class)
//				AssetInternalname = CMN.AssetMap.get(vcTmp.getAbsolutePath());
//			if(vcTmp.exists() || (AssetInternalname!=null))
//				vh.text.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
//			else
//				vh.text.setTextColor(Color.RED);
//			if(a.isSearching && vcTmp.getName().toLowerCase().contains(a.dictQueryWord))
//				vh.text.setBackgroundResource(R.drawable.xuxian2);
//			else
//				vh.text.setBackground(null);
			Locale locale = vcTmp.getLocale();
			if(vcTmp instanceof Voicemy) {//目录
				vh.text.setText(locale.getDisplayLanguage());
				vh.folderIcon.setVisibility(View.VISIBLE);
				vh.drag_handle.setVisibility(View.GONE);
				vh.splitterIcon.setVisibility(View.GONE);
				vh.text.setSingleLine(false);
				vh.subtext.setVisibility(View.GONE);
			}else {//路径
				int p = data.indexOf(Overflow.get(locale.getLanguage()));
				if(p!=-1) {//有父文件夹节点
					vh.text.setText(locale.getDisplayLanguage()+" ["+(pos-p)+"] ");
					vh.text.setPadding(5, 0, 0, 0);
					vh.splitterIcon.setVisibility(View.VISIBLE);
				} else {
					vh.text.setText(locale.getDisplayLanguage());
					//((TextView)v.findViewById(R.id.text)).setPadding((int) (9*getActivity().getResources().getDisplayMetrics().density), 0, 0, 0);
					convertView.findViewById(R.id.splitterIcon).setVisibility(View.GONE);
				}
				vh.subtext.setText(DevOpt.getCountryFlag(new StringBuilder(), locale.getCountry())+locale.getDisplayCountry()+vcTmp.getName());
				vh.subtext.setVisibility(View.VISIBLE);
				vh.drag_handle.setVisibility(View.VISIBLE);
				vh.folderIcon.setVisibility(View.GONE);
			}

			//((TextView)v.findViewById(R.id.text)).setTextColor(Color.parseColor("#000000"));

			if(GlobalOptions.isDark) {
				convertView.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
			}
			return convertView;

		}
	}

	public static class ViewHolder{
		//int position;
		public Voice dataLet;
		public View folderIcon;
		public View splitterIcon;
		public View drag_handle;
		public TextView text;
		public TextView subtext;
		public CheckBox ck;
	}
}  