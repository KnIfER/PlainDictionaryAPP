package com.knziha.plod.dictionarymanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.knziha.plod.PlainDict.CMN;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.plod.dictionarymodels.mdict_nonexist;
import com.knziha.plod.dictionarymodels.mdict_prempter;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.dictionarymanager.dict_manager_activity.transferRunnable;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

public class dict_manager_modules extends dict_manager_base<String> {

	String LastSelectedPlan;
	
    @Override
    public int getItemLayout() {
        return R.layout.dict_manager_dslitem;
    }
    
    @Override
    public void setListAdapter() {
        //String[] array = getResources().getStringArray(R.array.jazz_artist_names);
        //ArrayList<String> list = new ArrayList<String>(Arrays.asList(array));
        File def = new File(a.opt.pathToMain()+"CONFIG/AllModuleSets.txt");      //!!!原配
        final ArrayList<String> scanInList = new ArrayList<String>(); 
        final HashSet<String> con = new HashSet<>();
    	try {
			BufferedReader in = new BufferedReader(new FileReader(def));
	        String line = in.readLine();
	        while(line!=null){
	        	if(con.contains(line))
	        		isDirty=true;
	        	else
	        	if(new File(a.opt.pathToMain()+"CONFIG/"+line+".set").exists()) {
		        	scanInList.add(line);
	        	}else {
	        		isDirty=true;
	        	}
	        	con.add(line);
	        	line = in.readLine();
	        }
	        in.close();
		} catch (Exception e2) {
		}
	
    	new File(a.opt.pathToMain()+"CONFIG").listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				if(name.endsWith(".set")) {
					name = name.substring(0,name.length()-4);
					if(!con.contains(name)) {
			        	scanInList.add(name);
			        	con.add(name);
			        	isDirty=true;
					}
				}	
				return false;
			}});
    	
        adapter = new MyAdapter(scanInList);
        setListAdapter(adapter);
    }

    @Override
    public DragSortController buildController(DragSortListView dslv) {
        MyDSController c = new MyDSController(dslv);
        return c;
    }


    private class MyAdapter extends ArrayAdapter<String> {
      
      public MyAdapter(List<String> artists) {
        super(getActivity(), getItemLayout(), R.id.text, artists);
      }
      
      public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        //v.getBackground().setLevel(1000);
        if(getItem(position).equals(LastSelectedPlan)) {
            //((TextView)v.findViewById(R.id.text)).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorHeaderBlue));
            ((TextView)v.findViewById(R.id.text)).setTextColor(Color.BLUE);
            //((TextView)v.findViewById(R.id.text)).setText("✲"+((TextView)v.findViewById(R.id.text)).getText());
        }else
        	((TextView)v.findViewById(R.id.text)).setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);
        
        if(GlobalOptions.isDark) {
    		v.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
    	}
        
        return v;
      }
    }
 
    private class MyDSController extends DragSortController {

        DragSortListView mDslv;
        public MyDSController(DragSortListView dslv) {
            super(dslv);
            setDragHandleId(R.id.drag_handle);
            mDslv = dslv;
            //mDslv.setPadding(0, CMN.actionBarHeight, 0, 0);
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = adapter.getView(position, null, mDslv);
            //v.getBackground().setLevel(20000);
            mDslv.setFloatAlpha(1.0f);
            v.setBackgroundColor(Color.parseColor("#ffff00"));//TODO: get primary color
            isDirty=true;
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            //do nothing; block super from crashing
        }

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        a=(dict_manager_activity) getActivity();
        setListAdapter();
        
        LastSelectedPlan = a.opt.getLastPlanName();
        
        mDslv.setOnItemClickListener((parent, view, position, id) -> {
			if(position>=mDslv.getHeaderViewsCount()) {
				position = position - mDslv.getHeaderViewsCount();
				String name = adapter.getItem(position);
				File newf = new File(a.opt.pathToMain()+"CONFIG/"+name+".set");
				int cc=0;
				try {
					dict_manager_main f1 = ((dict_manager_activity)getActivity()).f1;
					f1.isDirty=true;
					BufferedReader in = new BufferedReader(new FileReader(newf));
					String line = in.readLine();
					a.md.clear();
					f1.rejector.clear();
					a.ThisIsDirty=true;
					while(line!=null){
						if(!line.trim().equals("")){
							boolean isFilter=false;
							if(line.startsWith("[:F]")){
								line = line.substring(4);
								isFilter=true;
							}
							if(!line.startsWith("/"))
								line=a.opt.lastMdlibPath+"/"+line;
							String fnId = new File(line).getAbsolutePath();
							mdict m = f1.mdict_cache.get(fnId);
							if(m==null)
							if(!new File(line).exists() && !CMN.AssetMap.containsKey(fnId)) {
								m = new mdict_nonexist(line,a.opt);
							 }else {
								 m = new mdict_prempter(line,a.opt);
							 }
							m.tmpIsFilter=isFilter;
							 a.md.add(m);
						}
						line = in.readLine();
					}
					in.close();
					f1.refreshSize();
					((dict_manager_activity)getActivity()).scrollTo(0);
					a.opt.putLastPlanName(LastSelectedPlan = name);
					File def = new File(a.getExternalFilesDir(null),"default.txt");
FileChannel inputChannel = null;
FileChannel outputChannel = null;
try {
inputChannel = new FileInputStream(newf).getChannel();
def.delete();
outputChannel = new FileOutputStream(def).getChannel();
outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
inputChannel.close();
outputChannel.close();
} catch (Exception e) {
						e.printStackTrace();
					}
					adapter.notifyDataSetChanged();
					f1.adapter.notifyDataSetChanged();
					a.show(R.string.pLoadDone,name,cc,a.md.size());
				} catch (Exception e2) {
					e2.printStackTrace();
					a.showT("加载异常!LOAD ERRO: "+e2.getLocalizedMessage());
				}
			}
		});
        
        mDslv.setOnItemLongClickListener(new OnItemLongClickListener() {
        	AlertDialog d;
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if(position>=mDslv.getHeaderViewsCount()) {
					AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                	final int actualPosition = position - mDslv.getHeaderViewsCount();
			        builder2.setTitle(R.string.setOpt);
			        builder2.setSingleChoiceItems(new String[] {}, 0,
			                new DialogInterface.OnClickListener() {
			                    @Override
			                    public void onClick(DialogInterface dialog, int pos) {
		                    		final String name = adapter.getItem(actualPosition);
			                    	switch(pos) {
				                    	case 0://模块的 重命名
				                    		((dict_manager_activity)getActivity()).showRenameDialog(name, new transferRunnable() {
				    							@Override
				    							public boolean transfer(File to) {
				    								File p=new File(a.opt.pathToMain()+"CONFIG/");
				    								try {
														if(!to.getParentFile().getCanonicalFile().getAbsolutePath().equals(p.getCanonicalFile().getAbsolutePath()))
															return false;
													} catch (IOException e) {return false;}
				    								
				    								String fn = name+".set";
				    								boolean doNothingToList=false;
				    								if(to.exists()) {//文件覆盖已预先处理。
				    									//adapter.remove(to.getName().substring(0,to.getName().length()-4));
				    									if(to.getName().equals(fn))
				                    						return true;
				    									doNothingToList=true;
				    								}
				    								
				    								boolean ret = new File(p,fn).renameTo(to);
				    								if(ret) {
				    									d.dismiss();
				    									adapter.remove(name);
				    									if(!doNothingToList) {
					    									int newPos = actualPosition;
					    									//if(newPos>1)
					    									//if(adapter.getItem(newPos-1).equals(name))
					    									//	newPos--;
					    									String name = to.getName();
					    									newPos=newPos>adapter.getCount()?adapter.getCount():newPos;
					    									adapter.insert(name.substring(0,name.length()-4), newPos);
				    									}
				    									a.show(R.string.renD);
				    								}
				    								return ret;
				    							}
												@Override
												public void afterTransfer() {
													//当架构崩盘
													if(actualPosition>=adapter.getCount() || !adapter.getItem(actualPosition).equals(name)) {
														d.dismiss();
													}
												}});
			                    		break;
				                    	case 1:
				                    		View dialog1 = getActivity().getLayoutInflater().inflate(R.layout.dialog_about,null);
				                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				                            TextView tvtv = ((TextView) dialog1.findViewById(R.id.title));
				                            tvtv.setText(getResources().getString(R.string.warnDelete,name));
				                            tvtv.setPadding(50, 50, 0, 0);
				                            builder.setView(dialog1);
				                            final AlertDialog dd = builder.create();
				                            dialog1.findViewById(R.id.cancel).setOnClickListener(new OnClickListener(){
				                            	 @Override
				                                    public void onClick(View v) {
				                                    	if(try_delete_configureLet(name)) {
				        									a.show(R.string.delD);
				        									adapter.remove(name);
				        									d.dismiss();
				        									dd.dismiss();
				        								}else {
				        									a.showT("文件删除失败_file_del_failure");
				        								}
				                                    }

				                            });
				                            if(Build.VERSION.SDK_INT<22) {//为什么：低版本不支持点击外部dimiss
				                                SpannableStringBuilder ssb = new SpannableStringBuilder(tvtv.getText());
				                                ssb.append("\n(否)");
				                                int idxNo = ssb.toString().indexOf("\n(否)");
				                                ssb.setSpan(new ClickableSpan() {
				                					@Override
				                					public void onClick(View widget) {
				                						dd.dismiss();
				                						}},idxNo,idxNo+"\n(否)".length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				                                tvtv.setText(ssb);
				                                tvtv.setMovementMethod(LinkMovementMethod.getInstance());
				        					}
				                            dd.show();
			                    		break;
				                    	case 2://复制;
				                    		File source  = new File(a.opt.pathToMain()+"CONFIG/"+name+".set");
				                    		int try_idx=0;
				                    		File dest;
				                    		while(true) {
				                    			dest = new File(source.getParent(),name+"("+try_idx+")"+".set");
				                    			if(!dest.exists() || dest.isDirectory())
				                    				break;
				                    			try_idx++;
				                    		}
				                    		FileChannel inputChannel = null;    
				                    	    FileChannel outputChannel = null;    
					                    	try {
					                    	    inputChannel = new FileInputStream(source).getChannel();
					                    	    outputChannel = new FileOutputStream(dest).getChannel();
					                    	    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
					                    	    inputChannel.close();
					                    	    outputChannel.close();
					                    	    a.show(R.string.dupicateD);
					                    	    adapter.add(name+"("+try_idx+")");
					                    	} catch (Exception e) {
												e.printStackTrace();
											} 
			                    		break;
			                    	}
			                    }
			                });
			        
			        d = builder2.create();
			        String[] Menus = getResources().getStringArray(
							R.array.module_sets_option);
			        List<String> arrMenu = Arrays.asList(Menus);
			        d.show();
			        d.getListView().setAdapter(new ArrayAdapter<String>(getActivity(),
				            R.layout.singlechoice, android.R.id.text1, arrMenu) {
			        	@Override
					    public View getView(int position,  View convertView,
					             ViewGroup parent) {
					    	View ret =  super.getView(position, convertView, parent);
					    	CheckedTextViewmy tv;
					        if(ret.getTag()==null)
					        	ret.setTag(tv = ret.findViewById(android.R.id.text1));
					        else
					        	tv = (CheckedTextViewmy)ret.getTag();
	
					    	tv.setCheckMarkDrawable(null);
					    	
					        return ret;
			        	}
			        });
				}
				return true;
			}});
    }

	private boolean try_delete_configureLet(String name) {
		return new File(a.opt.pathToMain()+"CONFIG/"+name+".set").delete();
	}

}
