package com.knziha.plod.plaindict;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.Framer;
import com.knziha.plod.widgets.Utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DictPicker extends DialogFragment implements View.OnClickListener
{
	public boolean PostEnabled=true;
	MainActivityUIBase a;
	private AnimationSet animation;
	private Drawable mActiveDrawable;
	
	public boolean isDirty=false;
	private Framer root;
	
	RecyclerView mRecyclerView;
	LinearLayoutManager lman;
	public boolean bShouldCloseAfterChoose=false;
	private HomeAdapter mAdapter;
	
	public String SearchIncantation;
	public Pattern SearchPattern;
	public int LastSearchScrollItem;
	
	public DictPicker() {
		this(null);
	}
	DictPicker(MainActivityUIBase a_){
		super();
		if(a_!=null){
			a = a_;
			mActiveDrawable = a.getActiveStarDrawable();
		}
	}

	@Override
	public void onAttach(@NonNull Context context){
		//CMN.Log("dict picker onAttach");
		super.onAttach(context);
		refresh(true);
	}
	
	public HomeAdapter adapter(){
		return mAdapter;
	}
	
	public void refresh(boolean firstAttach) {
		//if(!isDirty) return;
		//isDirty=false;
		int adapter_idx=a.pickTarget==1?a.currentClick_adapter_idx:a.adapter_idx;
		if(lman!=null)
			if(adapter_idx>lman.findLastVisibleItemPosition() || adapter_idx<lman.findFirstVisibleItemPosition()) {
				int target = Math.max(0, adapter_idx-5);
				lman.scrollToPositionWithOffset(a.pickTarget==1?a.CCD_ID:target, 0);
				CMN.Log("scrolled");
			}
		if(a.thisActType==MainActivityUIBase.ActType.PlainDict) {
			/*  词典选择器的动画效果(显示)  */
			if(animation==null)
				animation = (AnimationSet) AnimationUtils.loadAnimation(a, R.anim.dp_dialog_enter);
			animation.getAnimations().get(0).setDuration(firstAttach ? 200 : 200);
			a.dialogHolder.startAnimation(animation);
			a.dialogHolder.setVisibility(View.VISIBLE);
		}
		//mAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		root = (Framer) inflater.inflate(R.layout.dialog_1_fc, container, false);
		//view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
		//view.setLayoutParams(new LayoutParams(-2,-1));
		//getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mRecyclerView = root.findViewById(R.id.choose_dict);
		lman = new LinearLayoutManager(a.getApplicationContext());
		mRecyclerView.setLayoutManager(lman);
		mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
		//RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
		//if(itemAnimator instanceof SimpleItemAnimator)
		//	((SimpleItemAnimator)itemAnimator).setSupportsChangeAnimations(false);
		mRecyclerView.setItemAnimator(null);
		
		mRecyclerView.setMinimumWidth(getResources().getDisplayMetrics().widthPixels*2/3);
		mRecyclerView.setVerticalScrollBarEnabled(true);
		int LIP = lman.findLastVisibleItemPosition();
		int adapter_idx=a.pickTarget==1?a.currentClick_adapter_idx:a.adapter_idx;
		if(adapter_idx>LIP) {
			int target = Math.max(0, adapter_idx-5);
			lman.scrollToPositionWithOffset(target, 0);
		}
		//view.setBackgroundResource(R.drawable.popup_shadow_l);
		return root;
	}

	public void notifyDataSetChanged() {
		if(mAdapter!=null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	int  width=-1,height=-1,mMaxH=-1;
	public void onResume()
	{
		super.onResume();
		if(width!=-1 || height!=-1)//
			if(getDialog()!=null) {
				Window window = getDialog().getWindow();
				if(window!= null) {
					WindowManager.LayoutParams  attr = window.getAttributes();
					if(attr.width!=width || attr.height!=height) {
						//CMN.Log("onResume_");
						window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
						window.setDimAmount(0.1f);
						window.setBackgroundDrawableResource(R.drawable.popup_shadow_l);
						root.mMaxHeight=mMaxH;
						window.setLayout(width,height);

						getView().post(() -> refresh(false));
					}
				}
				getDialog().setCanceledOnTouchOutside(true);
			}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, 0);
	}
	
	class HomeAdapter extends RecyclerView.Adapter<MyViewHolder>
	{
		@Override
		public int getItemCount() {
			return a.md.size();
		}
		
		@NonNull
		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			//CMN.Log("onCreateViewHolder...");
			MyViewHolder ret = new MyViewHolder(LayoutInflater.from(parent.getContext())
							.inflate(R.layout.diag1_fc_list_item, parent, false), DictPicker.this);
			ret.tv.bNeedPostLayout = true;
			return ret;
		}

		@Override
		public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
			holder.position = position;
			
			int adapter_idx=a.pickTarget==1?a.currentClick_adapter_idx:a.adapter_idx;
			
			FlowTextView tv = holder.tv;
			
			boolean isThisSelected = adapter_idx==position;
			
			holder.itemView.setBackgroundColor(isThisSelected?0xff4F7FDF:Color.TRANSPARENT);
			
			tv.setTextColor(GlobalOptions.isDark||isThisSelected?Color.WHITE:Color.BLACK);
			
			tv.PostEnabled = PostEnabled;
			
			tv.setStarLevel(a.md_get_StarLevel(position));
			
			tv.setCompoundDrawables(mActiveDrawable, null, null, null);
			
			String text = a.md_getName(position);
			
			tv.SetSearchPattern(SearchPattern, text);
			
			tv.setText(text);
			
			holder.cover.setImageDrawable(a.md_getCover(position));
		}
	}
	
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if(tag instanceof MyViewHolder){
			int position = ((MyViewHolder) tag).position;
			if(v.getId()==R.id.coverp) {
				a.showAboutDictDialogAt(position);
			} else {
				if(a.dismissing_dh) return;
				if(a.pickTarget==1){//点译上游
					int tmpPos = a.currentClick_adapter_idx;
					a.CCD=a.md_get(position);
					a.CCD_ID=a.currentClick_adapter_idx = position;
					a.popupWord(Utils.getTextInView(a.popupTextView), null, -1);
					mAdapter.notifyItemChanged(tmpPos);
					mAdapter.notifyItemChanged(position);
					if(a instanceof PDICMainActivity){
						((PDICMainActivity)a).dismissDictPicker(R.anim.dp_dialog_exit);
					}else {
						dismiss();
					}
				}
				else {//当前词典
					int tmpPos = a.adapter_idx;
					a.adapter_idx = position;
					a.switch_To_Dict_Idx(position, true, true, null);
					mAdapter.notifyItemChanged(tmpPos);
					if(tmpPos!=position){
						mAdapter.notifyItemChanged(position);
					}
					if (bShouldCloseAfterChoose) {
						v.post(this::dismiss);
					}
				}
			}
		}
	}

	public void SetSearchIncantation(String pattern) {
		if(!pattern.equals(SearchIncantation)){
			SearchIncantation = pattern;
			if(pattern.length()==0) {
				SearchPattern=null;
			} else {
				try {
					SearchPattern = Pattern.compile(SearchIncantation,Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					SearchPattern = Pattern.compile(SearchIncantation, Pattern.CASE_INSENSITIVE|Pattern.LITERAL);
				}
			}
			LastSearchScrollItem=-1;
		}
	}
	
	static class MyViewHolder extends ViewHolder
	{
		public int position;
		FlowTextView tv;
		ImageView cover;
		public MyViewHolder(View view, View.OnClickListener onclick)
		{
			super(view);
			tv = view.findViewById(R.id.id_num);
			cover = view.findViewById(R.id.cover);
			itemView.setTag(this);
			itemView.setOnClickListener(onclick);
			View coveronclick = view.findViewById(R.id.coverp);
			coveronclick.setTag(this);
			coveronclick.setOnClickListener(onclick);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		a=(MainActivityUIBase) getActivity();
		if(GlobalOptions.isDark) {
			Utils.setListViewScrollbarColor(mRecyclerView, true);
		}
	}
}  