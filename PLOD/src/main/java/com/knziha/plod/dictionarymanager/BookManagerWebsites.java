package com.knziha.plod.dictionarymanager;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionarymanager.files.mAssetFile;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.widgets.ViewUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class BookManagerWebsites extends BookManagerFolderAbs {
	public static class WebAssetDesc {
		public final mFile realPath;
		public final boolean isFolder;
		final String desc;
		final String description;
		public WebAssetDesc(String realPath, String desc, String description) {
			this.realPath = new mFile(realPath);
			this.desc = desc;
			this.description = description;
			isFolder = realPath.length()==0;
		}
	}
	
	//构造
	public BookManagerWebsites()
	{
		super();
		type = 1;
		mName = "网络词典";
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(!dataPrepared) pullData();
	}

	private void pullData()
	{
		try {
			PDICMainAppOptions opt = getBookManager().opt;
			topParent.add(new mFile(opt.lastMdlibPath));
			topParent.add(new mFile(opt.lastMdlibPath.getPath().replace(GlobalOptions.extPath, "/sdcard")));
		} catch (Exception e) {
			CMN.debug(e);
		}
		dataPrepared=true;
		WebAssetDesc webp = new WebAssetDesc("", "", "");
		data.insert(new mFile("在线翻译", webp));
		data.insert(new mFile("在线翻译/谷歌翻译", new WebAssetDesc("/ASSET2/谷歌翻译.web", "在线翻译", "谷歌翻译国内版（translate.google.cn）")));
		data.insert(new mFile("在线翻译/彩云小译", new WebAssetDesc("/ASSET2/彩云小译.web", "在线翻译", "https://fanyi.caiyunapp.com/#/")));
		data.insert(new mFile("在线翻译/百度翻译", new WebAssetDesc("/ASSET2/百度翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		data.insert(new mFile("在线翻译/有道翻译", new WebAssetDesc("/ASSET2/有道翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		data.insert(new mFile("在线翻译/必应翻译", new WebAssetDesc("/ASSET2/必应翻译.web", "在线翻译", "https://fanyi.baidu.com/")));
		
		data.insert(new mFile("词汇", webp));
		data.insert(new mFile("词汇/Vocabulary", new WebAssetDesc("/ASSET2/Vocabulary.web", "词汇", "一个词汇查询网站。（vocabulary.com）")));
		data.insert(new mFile("词汇/Etymology online", new WebAssetDesc("/ASSET2/Etymology online.web", "词根", "提供英语词源查询服务（etymonline.com）")));
		data.insert(new mFile("词汇/WantWords 反向词典", new WebAssetDesc("/ASSET2/WantWords 反向词典.web", "近义词", "开源的反向词典系统。（wantwords.thunlp.org）")));
		data.insert(new mFile("词汇/无限自由词典", new WebAssetDesc("/ASSET2/无限自由词典.web", "聚合搜索", "免费的英语词典聚合搜索引擎，集成维基百科等。（www.thefreedictionary.com）")));
		
		data.insert(new mFile("wiki", webp));
		data.insert(new mFile("wiki/维基词典", new WebAssetDesc("/ASSET2/维基词典.web", "", "")));
		
		data.insert(new mFile("新闻资讯", webp));
		data.insert(new mFile("新闻资讯/人民网", new WebAssetDesc("/ASSET2/人民网.web", "", "")));
		data.insert(new mFile("新闻资讯/百度新闻", new WebAssetDesc("/ASSET2/百度新闻.web", "", "")));
		
		
		data.insert(new mFile("其他内置词典", webp));
		data.insert(new mFile("其他内置词典/应用社区", new WebAssetDesc("/ASSET2/应用社区.web", "", "")));
		data.insert(new mFile("其他内置词典/李白全集", new WebAssetDesc("/ASSET/李白全集.mdx", "", "")));
		//data.insert(new mAssetFile("/ASSET/李白全集.mdx").init(a.opt));
		
		File rec = a.fRecord;
		try {
			BufferedReader in = new BufferedReader(new FileReader(rec));
			String line;
			//int idx=0;
			String lastMdlibPath = a.opt.lastMdlibPath.getPath();
			while((line=in.readLine())!=null){
				if (line.endsWith(".web")) {
					if (line.startsWith("/sdcard/")) {
						line = GlobalOptions.extPath + line.substring(7);
					}
					boolean isForeign=false;
					mFile fI;
					if(!line.startsWith("/")) {//是相对路径
						if(line.contains("/")) {
							//wtf???
							if(line.startsWith("/")) {
								fI = new mFile(line);
							} else {
								fI = new mFile(a.opt.lastMdlibPath, line);
								fI.bInIntrestedDir=true;
							}
							isForeign=true;
						} else {
							fI = new mFile(a.opt.lastMdlibPath, line);
							fI.bInIntrestedDir=true;
						}
					} else {
						if(!line.startsWith(lastMdlibPath))
							isForeign=true;
						fI = new mFile(line);
					}
					
					if(data.insert(fI.init(a.opt))==-1)
						isDirty=true;
					else if(isForeign)
						data.insertOverFlow(fI.getParentFile().init(a.opt));
					//idx++;
				}
			}
			in.close();
		}
		catch (Exception e) {
			CMN.debug(e);
		}
		
		dataTree = new ArrayList<>(data.getList());
		adapter = new MyAdapter(dataTree);
		adapterAll = new MyAdapter(data.getList());
		super.setListAdapter(adapter);
	}
}
