## 0.1 免责声明
作为休闲学习向的独立应用，本APP虽力求完备，但不可避免地存在一些开发者尚未发现的缺陷，因此使用风险自负。
## 0.2 隐私协议
本APP承诺不上传任何用户隐私，不收集非运行必要的设备信息。
## 0.3 获取源代码
本APP全部代码可在Github或Gitee获取。

--- 

### 1. WRITE_EXTERNAL_STORAGE
存储/写入权限。（推荐）用于保存配置与词条历史于/sdcard/PLOD/，以避免重装时丢失重要数据。
--- 

### 2. READ_EXTERNAL_STORAGE
存储/读取权限。（推荐）用于读取外部词库。
--- 

### 3. GET_TASKS、PACKAGE_USAGE_STATS
最近应用使用情况权限。（可选）用于在被其他APP调用到时，记录调用信息，以在历史记录界面中追踪来源、显示图标。
安卓5以上需要用户在「词条历史记录界面-右上更多菜单-设置」中手动申请“使用情况访问权限”。
--- 

### 4. INTERNET
联网权限。用于开启局域网服务器与访问网络词典。网络词典的名称以.web结尾。
--- 

### 5. FOREGROUND_SERVICE
（可选）前台服务。用于显示通知横幅。可点击查词。
--- 

### 6. WAKE_LOCK、DISABLE_KEYGUARD
（可选）用于保持屏幕常亮
--- 

### 7. REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
（可选）用于申请后台免杀
--- 

### 8. SYSTEM_ALERT_WINDOW、SYSTEM_OVERLAY_WINDOW、REORDER_TASKS
（可选）用于特色小窗模式，可在「主窗口-右下宫格菜单-右下浏览选项-悬浮按钮与小窗」中开启
，届时用户需手动申请“显示在其他应用上层”或“悬浮窗”权限。
--- 

### 9. android.permission.CAMERA、android.permission.FLASHLIGHT
（可选）相机权限，用于相机取词插件。
--- 
