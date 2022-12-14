package com.knziha.plod.dictionarymanager;

import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.widgets.ViewUtils;

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
		data.insert(new mFile("在线翻译", true));
		data.insert(new mFile("在线翻译/谷歌翻译", new WebAssetDesc("/ASSET2/谷歌翻译.web", "在线翻译", "谷歌翻译国内版（translate.google.cn）")));
		data.insert(new mFile("在线翻译/彩云小译", new WebAssetDesc("/ASSET2/彩云小译.web", "在线翻译", "https://fanyi.caiyunapp.com/#/")));
		data.insert(new mFile("在线翻译/百度翻译", new WebAssetDesc("/ASSET2/百度翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		data.insert(new mFile("在线翻译/有道翻译", new WebAssetDesc("/ASSET2/有道翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		data.insert(new mFile("在线翻译/必应翻译", new WebAssetDesc("/ASSET2/必应翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		
		data.insert(new mFile("词汇", true));
		data.insert(new mFile("词汇/Vocabulary", new WebAssetDesc("/ASSET2/Vocabulary.web", "词汇", "一个词汇查询网站。（vocabulary.com）")));
		data.insert(new mFile("词汇/Etymology online", new WebAssetDesc("/ASSET2/Etymology online.web", "词根", "提供英语词源查询服务（etymonline.com）")));
		data.insert(new mFile("词汇/WantWords 反向词典", new WebAssetDesc("/ASSET2/WantWords 反向词典.web", "近义词", "开源的反向词典系统。（wantwords.thunlp.org）")));
		data.insert(new mFile("词汇/无限自由词典", new WebAssetDesc("/ASSET2/无限自由词典.web", "聚合搜索", "免费的英语词典聚合搜索引擎，集成维基百科等。（www.thefreedictionary.com）")));
		
		data.insert(new mFile("wiki", true));
		data.insert(new mFile("wiki/维基词典", new WebAssetDesc("/ASSET2/维基词典.web", "", "")));
		
		data.insert(new mFile("新闻资讯", true));
		data.insert(new mFile("新闻资讯/人民网", new WebAssetDesc("/ASSET2/人民网.web", "", "")));
		data.insert(new mFile("新闻资讯/百度新闻", new WebAssetDesc("/ASSET2/百度新闻.web", "", "")));
		
		
		data.insert(new mFile("其他", true));
		data.insert(new mFile("其他/应用社区", new WebAssetDesc("/ASSET2/应用社区.web", "", "")));
		
		dataTree = new ArrayList<>(data.getList());
		adapter = new MyAdapter(dataTree);
		super.setListAdapter(adapter);
	}
}
