package com.fenwjian.sdcardutil;
import java.io.File;
import android.os.Environment;
import android.util.Log;

public class SdCardUtilClass {
   // 项目文件根目录
   public static final String FILEDIR = "/back";
   // 照相机照片目录
   public static final String FILEPHOTO = "/photos";
   // 应用程序图片存放
   public static final String FILEIMAGE = "/images";
   // 应用程序缓存
   public static final String FILECACHE = "/cache";
   // 用户信息目录
   public static final String FILEUSER = "user";
  
   /*
    * 检查sd卡是否可用
    * getExternalStorageState 获取状态
    * Environment.MEDIA_MOUNTED 直译  环境媒体登上  表示，当前sd可用
    */
   public static boolean checkSdCard() {
      if (Environment.getExternalStorageState().equals(
           Environment.MEDIA_MOUNTED))
        //sd卡可用
        return true;
      else
        //当前sd卡不可用
        return false;
   }
   /*
    * 获取sd卡的文件路径
    * getExternalStorageDirectory 获取路径
    */
   public static String getSdPath(){  
      return Environment.getExternalStorageDirectory()+"/";  
   }
   /*
    * 创建一个文件夹
    */
   public static  void  createFileDir(String fileDir){
   String path=getSdPath()+fileDir;
   File path1=new File(path);
      if(!path1.exists())
      {
        path1.mkdirs();
        Log.i("logmy_sdcardUtil", "我被创建了");
      }
   }
}