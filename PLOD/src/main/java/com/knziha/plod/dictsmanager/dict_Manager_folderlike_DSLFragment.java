package com.knziha.plod.dictsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictsmanager.files.ArrayListTree;
import com.knziha.plod.dictsmanager.files.mAssetFile;
import com.knziha.plod.dictsmanager.files.mFile;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.rbtree.RashSet;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class dict_Manager_folderlike_DSLFragment extends ListFragment {
	String parentFile;
	ArrayListTree<mFile> data=new ArrayListTree<>();
	ArrayListTree<mFile> hiddenParents=new ArrayListTree<>();
	protected ListView mDslv;
	ArrayAdapter<mFile> adapter;
	boolean isDirty = false;
	dict_manager_activity a;
	
	public interface OnEnterSelectionListener{
		public void onEnterSelection();
		public int addIt(File fn);
	}OnEnterSelectionListener oes;
	
    public boolean SelectionMode=false;
    public RashSet<String> Selection = new RashSet<>();
    
	public boolean alreadySelectedAll;
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    Log.d("setUserVisibleHint"," setUserVisibleHint() --> isVisibleToUser = " + isVisibleToUser);

	    if (isVisibleToUser) {
	    	pullData();
	    }
	    super.setUserVisibleHint(isVisibleToUser);
	}
	boolean dataPrepared;
	private void pullData() {
		if(!dataPrepared) {
			File rec = new File(a.opt.pathToMain()+"CONFIG/mdlibs.txt");
	        try {
				BufferedReader in = new BufferedReader(new FileReader(rec));
		        String line;
		        //int idx=0;
		        StringBuilder sb = new StringBuilder(a.opt.lastMdlibPath).append("/");
		        int baseL = a.opt.lastMdlibPath.length()+1;
		        while((line=in.readLine())!=null){
		        	boolean isForeign=false;
		        	mFile fI;
	        		if(!line.startsWith("/")) {//是相对路径
	        			if(line.contains("/")) {
		        			sb.setLength(baseL);
		        			line=sb.append(line).toString();
		        			fI = new mFile(line);
		        			fI.bInIntrestedDir=true;
	        				isForeign=true;
	        			}else {
	        				fI = new mFile(a.opt.lastMdlibPath,line);
	        				fI.bInIntrestedDir=true;
	        			}
	        		}else {
	        			 if(!line.startsWith(a.opt.lastMdlibPath)) 
	 	        			isForeign=true;
	        			 fI = new mFile(line);
	        		}
	        		
	        		if(data.insert(fI.init())==-1)
	        			isDirty=true;
	        		else if(isForeign)
	        			data.insertOverFlow(fI.getParentFile().init());
	        		//idx++;
		        }
		        in.close();
		        
		        data.insert(new mAssetFile("/ASSET/liba.mdx").init());
		        
			} catch (IOException e2) {
				e2.printStackTrace();
			}
	        if(adapter!=null)//strange here.
	        	adapter.notifyDataSetChanged();
		}
		dataPrepared=true;
	}
	
	
	
	//构造
	public dict_Manager_folderlike_DSLFragment(){
		super();
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        mDslv = (ListView) inflater.inflate(R.layout.dict_dsl_main2, null);
        mDslv.setDividerHeight(0);
        //mDslv.setOnTouchListener(mController);
        //mDslv.setDragEnabled(dragEnabled);
        //CMN.show("onCreateView");
        return mDslv;
    }
	
	
	
	

    public int getItemLayout() {
        return R.layout.dict_manager_dslitem2;
    }


    public void setListAdapter() {
        List<mFile> list = data.getList();
        adapter = new MyAdapter(list);
        super.setListAdapter(adapter);
    }



    private class MyAdapter extends ArrayAdapter<mFile> {
      
      public MyAdapter(List<mFile> mdicts) {
    	  super(getActivity(), getItemLayout(), R.id.text, mdicts);
      }

      @Override
      public int getCount() {
    	int ret=data.size();
    	for(mFile mdTmp:hiddenParents.getList())
    		ret-=mdTmp.shrinked;
		return ret;
      }
      
      public View getView(int position, View convertView, ViewGroup parent) {
    	mFile indexor = data.getList().get(position);
    	ArrayList<mFile> pL = hiddenParents.getList();
    	int acc=0;
    	int pIdx=0;
    	while(true) {
        	int accLet=0;
	    	while(pIdx<pL.size() && pL.get(pIdx).compareTo(indexor)<0) {
	    		accLet+=pL.get(pIdx++).shrinked;
	    	}
	    	if(accLet==0)
	    		break;
	    	acc+=accLet;
	    	indexor=data.getList().get(position+acc);
    	}
    	final int pos = position+acc;
    	View v = super.getView(pos, convertView, parent);
    	if(v.getTag()==null) {
    		//Log.e("新建",""+pos);
    		final ViewHolder vh=new ViewHolder();
    		vh.ck = (CheckBox)v.findViewById(R.id.ck);
        	vh.ck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    			@Override
    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    				//CMN.show("onCheckedChanged");
    				
    			}});
    		vh.folderIcon = (v.findViewById(R.id.folderIcon));
        	vh.folderIcon.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				mFile mdTmp = vh.dataLet;
    				int pos = data.indexOf(mdTmp);
    				if(mdTmp.shrinked==0) {
	    				hiddenParents.insert(mdTmp);
						mdTmp.shrinked=0;
						for(int i=pos+1;i<data.getList().size();i++) {
							if(!mFile.isDirScionOf(data.getList().get(i), mdTmp))
								break;
							if(data.getList().get(i).isDirectory())
								break;
							mdTmp.shrinked++;
						}
	    				notifyDataSetChanged();
	    				//CMN.show(""+adapter.getCount());
    				}else {
	    				mdTmp.shrinked=0;
	    				hiddenParents.remove(mdTmp);
	    				notifyDataSetChanged();
	    				//CMN.show("22 "+adapter.getCount());
    				}
    			}});
    		vh.text=((TextView)v.findViewById(R.id.text));
    		vh.splitterIcon=((View)v.findViewById(R.id.splitterIcon));
    		vh.drag_handle=v.findViewById(R.id.drag_handle);
    		vh.drag_handle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mFile mdTmp = vh.dataLet;
					if(!mdTmp.isDirectory()) {
						if(oes.addIt(mdTmp)==1)
							a.showT("添加成功!");
						else if(oes.addIt(mdTmp)==0)
							a.showT("已存在");
					}
				}});
            
    		v.setTag(vh);
    	}
    	
    	ViewHolder vh=(ViewHolder) v.getTag();
    	//vh.position=pos;
    	//vh.dataLet=data.getList().get(pos);
    	if(SelectionMode) {
    		vh.ck.setVisibility(View.VISIBLE);
        	if(Selection.contains(getItem(pos).getAbsolutePath()))
        		vh.ck.setChecked(true);
        	else
        		vh.ck.setChecked(false);
        }else {
        	vh.ck.setChecked(false);
        	vh.ck.setVisibility(View.GONE);
        }

    	final mFile mdTmp = adapter.getItem(pos);
    	vh.dataLet=mdTmp;
    	
    	//if(mdTmp.cover!=null) {
		//	SpannableStringBuilder ssb = new SpannableStringBuilder("| ").append(mdTmp._Dictionary_fName);
		//	mdTmp.cover.setBounds(0, 0, 50, 50);
		//	ssb.setSpan(new ImageSpan(mdTmp.cover), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//	((TextView)v.findViewById(R.id.text)).setText(ssb);
		//}else
    	String AssetInternalname = null;
    	if(mdTmp.getClass() == mAssetFile.class)
    		AssetInternalname = CMN.AssetMap.get(mdTmp.getAbsolutePath());
    	if(mdTmp.exists() || (AssetInternalname!=null))
    		vh.text.setTextColor(a.opt.getInDarkMode()?Color.WHITE:Color.BLACK);
    	else
    		vh.text.setTextColor(Color.RED);
    	if(a.isSearching && mdTmp.getName().toLowerCase().contains(a.dictQueryWord))
    		vh.text.setBackgroundResource(R.drawable.xuxian2);
    	else
    		vh.text.setBackground(null);
    	if(mdTmp.isDirectory()) {//目录
    		if(AssetInternalname!=null) {
    			vh.text.setText(AssetInternalname);
    		}else
    			vh.text.setText(mFile.tryDeScion(mdTmp,parentFile));
	    	vh.folderIcon.setVisibility(View.VISIBLE);
	    	vh.drag_handle.setVisibility(View.GONE);
			vh.splitterIcon.setVisibility(View.GONE); 
			vh.text.setSingleLine(false);
    	}else {//路径
    		if(AssetInternalname!=null) {
    			vh.text.setText(AssetInternalname);
    		}else if(mFile.isScionOf(mdTmp,parentFile)) {
	    		vh.text.setPadding((int) (9*getActivity().getResources().getDisplayMetrics().density), 0, 0, 0);
	    		vh.text.setText(a.isDebug?mdTmp.getPath():BU.unwrapMdxName(mdTmp.getName()));//BU.unwrapMdxName(mdTmp.getName())
	    	}else {
	    		vh.text.setPadding(0, 0, 0, 0);
	    		vh.text.setText(mdTmp.getAbsolutePath());
	    	}
	    	mFile p = data.get(mdTmp.getParentFile().init());
    		if(p!=null) {//有父文件夹节点
    			vh.text.setPadding(5, 0, 0, 0);
    			vh.text.setText(BU.unwrapMdxName(mdTmp.getName()));
    			vh.splitterIcon.setVisibility(View.VISIBLE);
    		}else {
    			//((TextView)v.findViewById(R.id.text)).setPadding((int) (9*getActivity().getResources().getDisplayMetrics().density), 0, 0, 0);
    			((View)v.findViewById(R.id.splitterIcon)).setVisibility(View.GONE);
    		}
    		vh.drag_handle.setVisibility(View.VISIBLE);
			vh.folderIcon.setVisibility(View.GONE);
    	}
        //((TextView)v.findViewById(R.id.text)).setTextColor(Color.parseColor("#000000"));

        if(a.opt.getInDarkMode()) {
    		v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
    	}
		return v;

      }
    }
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        a = (dict_manager_activity) getActivity();
        parentFile=a.opt.lastMdlibPath;
        mDslv = getListView(); 
        mDslv.setChoiceMode(mDslv.CHOICE_MODE_MULTIPLE);
        View v = getActivity().getLayoutInflater().inflate(R.layout.pad_five_dp, null);
        mDslv.addHeaderView(v);
        
        mDslv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position>=mDslv.getHeaderViewsCount()) {
			    	//position = position - mDslv.getHeaderViewsCount();
					//mFile p = data.getList().get(position);
					ViewHolder vh = (ViewHolder)view.getTag();
					mFile mdTmp = vh.dataLet;
					if(SelectionMode) {
	    				int pos = data.indexOf(mdTmp);
	    				//CMN.Log(pos+" ?= "+position);
						if(Selection.contains(mdTmp.getAbsolutePath())) {
							Selection.removeLastSelected();
							if(mdTmp.isDirectory()){
	    						for(int i=pos+1;i<data.getList().size();i++) {
	    		    				mFile item = data.getList().get(i);
	    							if(!mFile.isDirScionOf(item, mdTmp))
	    								break;
	    							if(item.isDirectory())
	    								break;
	    							Selection.remove(item.getAbsolutePath());
	    						}
		    				}
						}else {
							Selection.put(mdTmp.getAbsolutePath());
		    				if(mdTmp.isDirectory()){
	    						hiddenParents.remove(mdTmp);
	    						mdTmp.shrinked=0;
	    						for(int i=pos+1;i<data.getList().size();i++) {
	    		    				mFile item = data.getList().get(i);
	    							if(!mFile.isDirScionOf(item, mdTmp))
	    								break;
	    							if(item.isDirectory())
	    								break;
	    							Selection.put(item.getAbsolutePath());
	    						}
		    				}
						}
						alreadySelectedAll=false;
						adapter.notifyDataSetChanged();
					}else {
						if(mdTmp.isDirectory()) {
							((ViewHolder)view.getTag()).folderIcon.performClick();
							//adapter.notifyDataSetChanged();
						}
					}
				}
			}});
        mDslv.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if(position>=mDslv.getHeaderViewsCount()) {
			    	position = position - mDslv.getHeaderViewsCount();
					SelectionMode=true;
					if(oes!=null) oes.onEnterSelection();
					//Selection.put(adapter.getItem(position).getAbsolutePath());
					mDslv.getOnItemClickListener().onItemClick(parent, view, position+mDslv.getHeaderViewsCount(), id);
					adapter.notifyDataSetChanged();
				}
				return true;
			}});
        setListAdapter();
    }
    

    private static class ViewHolder{
		//int position;
    	mFile dataLet;
    	View folderIcon;
    	View splitterIcon;
    	View drag_handle;
    	private TextView text;
    	private CheckBox ck;
    }


}
