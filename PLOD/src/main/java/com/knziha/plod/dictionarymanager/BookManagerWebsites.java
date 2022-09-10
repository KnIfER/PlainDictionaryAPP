package com.knziha.plod.dictionarymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.plaindict.R;

import java.util.ArrayList;

public class BookManagerWebsites extends BookManagerFolderAbs {
	public static class WebAssetDesc {
		public final mFile realPath;
		final String desc;
		final String description;
		WebAssetDesc(String realPath, String desc, String description) {
			this.realPath = new mFile(realPath);
			this.desc = desc;
			this.description = description;
		}
	}
	
	//构造
	public BookManagerWebsites()
	{
		super();
		type = 1;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(!dataPrepared) pullData();
	}

	private void pullData()
	{
		dataPrepared=true;
		data.insert(new mFile("翻译", true));
		data.insert(new mFile("翻译/谷歌翻译", new WebAssetDesc("/ASSET2/谷歌翻译.web", "通用翻译", "基于谷歌翻译国内版（translate.google.cn）")));
		data.insert(new mFile("翻译/彩云小译", new WebAssetDesc("/ASSET2/彩云小译.web", "通用翻译", "https://fanyi.caiyunapp.com/#/")));
		
		data.insert(new mFile("英语词汇", true));
		data.insert(new mFile("英语词汇/Vocabulary", new WebAssetDesc("/ASSET/Vocabulary.web", "词汇", "一个词汇查询网站，支持学习卡片。（vocabulary.com）")));
		data.insert(new mFile("英语词汇/Etymology online", new WebAssetDesc("/ASSET/Etymology online.web", "词根", "提供英语词源查询服务（etymonline.com）")));
		data.insert(new mFile("英语词汇/WantWords 反向词典", new WebAssetDesc("/ASSET/WantWords 反向词典.web", "近义词", "开源的反向词典系统，通过描述想要表达的意思来进行词语查找。（wantwords.thunlp.org）")));
		
		data.insert(new mFile("wiki", true));
		data.insert(new mFile("wiki/维基词典", new WebAssetDesc("/ASSET/维基词典.web", "", "")));
		
		
		data.insert(new mFile("其他", true));
		data.insert(new mFile("其他/应用社区", new WebAssetDesc("/ASSET2/应用社区.web", "", "")));
		
		dataTree = new ArrayList<>(data.getList());
		adapter = new MyAdapter(dataTree);
		super.setListAdapter(adapter);
	}
}
