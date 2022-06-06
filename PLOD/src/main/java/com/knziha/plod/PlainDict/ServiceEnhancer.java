package com.knziha.plod.plaindict;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import static com.knziha.plod.plaindict.MdictServer.isServerRunning;
import static com.knziha.plod.widgets.RomUtils._HX;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.knziha.plod.PlainUI.FloatBtn;
import com.knziha.plod.settings.NotificationSettings;
import com.knziha.plod.settings.SettingsActivity;

import java.lang.ref.WeakReference;
import java.util.Properties;

public class ServiceEnhancer extends Service implements MediaPlayer.OnCompletionListener {
	public static boolean isRunning;
	private NotificationManager notificationManager;
	
	String Id = "PdnTid";
	String Name = "SerPLOD";
	private NotificationChannel channel;
	
	MediaPlayer mMediaPlayer;
	MdictServer mServer;
	private WifiManager.WifiLock wifiLock;
	
	@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    Handler EnhanceService = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
//			startActivity(new Intent(ServiceEnhancer.this, PDICMainActivity.class)
//					.setFlags(MainShareActivity.SingleTaskFlags|Intent.FLAG_FROM_BACKGROUND)
//					.setAction("lock")
//			);
			
			Intent intent = new Intent("plodlock");
			Bundle bundle = new Bundle();
			intent.putExtras(bundle);
			sendBroadcast(intent);
			
			EnhanceService.sendEmptyMessageDelayed(0, 1000);
		}
	};
	
	BroadcastReceiver mbatteryReceiver;
	
	static class BatteryStatesListener extends BroadcastReceiver{
		final WeakReference<ServiceEnhancer> ref;
		BatteryStatesListener(ServiceEnhancer s) {
			this.ref = new WeakReference<>(s);
		}
		
		public void onReceive(Context context, Intent intent)
		{
			try {
				if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()))
				{
					int status=intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
					CMN.Log("状态变化::", status, status==BatteryManager.BATTERY_STATUS_CHARGING);
					ServiceEnhancer s = ref.get();
					if(s!=null) {
						s.setupDaemon(status==BatteryManager.BATTERY_STATUS_CHARGING?1:0);
					} else {
						context.unregisterReceiver(this);
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
	}
    
    // 设备充电时，即使处于处于省电模式，也取消后台播放空白音频、防止wifi休眠的保活措施。
	boolean bSkipCharing = true;
	
	PowerManager powerManager;
	BatteryManager batteryManager;
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
    public void onCreate() {
        super.onCreate();
		powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		batteryManager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
		mServer = ((AgentApplication)getApplication()).mServer;
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			channel = new NotificationChannel(Id, Name, NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(channel);
		}
		startForeground(1, getNotification());
		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if(PDICMainAppOptions.getAutoDaemonMW() && bSkipCharing) {
				mbatteryReceiver = new BatteryStatesListener(this);
				registerReceiver(mbatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (batteryManager.isCharging()) {
				CMN.Log("isCharging::");
			}
		}
		CMN.Log("isPowerSaveMode::", isPowerSaveModeCompat());
		setupDaemon(-1);
		isRunning = true;
	}
	
	public static int isMusicPlaying = 0;
	
	private void setupDaemon(int charging) {
		CMN.Log("setupDaemon::", getShouldPlayMusic(charging), getShouldLockWifi(charging));
		if (getShouldPlayMusic(charging)) {
			playMusic();
		} else {
			pauseMusic();
		}
		if (getShouldLockWifi(charging)) {
			acquireWifiLock();
		} else {
			releaseWifiLock();
		}
		//if (((AgentApplication)getApplication()).floatBtn!=null) {
		//	((AgentApplication)getApplication()).floatBtn.reInitBtn(this, 0);
		//}
	}
	
	public static void SendSetUpDaemon(Context context) {
		Intent intent = new Intent(context, ServiceEnhancer.class);
		intent.putExtra("realm", true);
		context.startService(intent);
	}
	
	private boolean getShouldPlayMusic(int charging) {
		return PDICMainAppOptions.getForceDaemonMusic() || getAutoDaemonJudgeResult(charging);
	}
	
	private boolean getShouldLockWifi(int charging) {
		return PDICMainAppOptions.getForceDaemonWifi() || getAutoDaemonJudgeResult(charging);
	}
	
	private boolean getAutoDaemonJudgeResult(int charging) {
		if(!isServerRunning) {
			return false;
		}
		boolean ret = PDICMainAppOptions.getAutoDaemonMW()
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& isPowerSaveModeCompat() // 自动判断，省电模式下需要播放音频、保活wifi
				;
		//CMN.Log("getAutoDaemonJudgeResult", ret, charging, batteryManager.isCharging());
		// 充电时跳过
		if(ret && bSkipCharing) {
			if(charging==1 || charging==-1
					&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
					&& batteryManager.isCharging()
			) {
				ret = false;
			}
		}
		return ret;
	}
	
	
	private void playMusic() {
		if (mMediaPlayer==null) {
			mMediaPlayer=MediaPlayer.create(this, R.raw.s2);
//			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setLooping(true);
//			Equalizer eq = new Equalizer(0, mMediaPlayer.getAudioSessionId());
//			for (int i = 0; i < eq.getNumberOfBands(); i++) {
//				eq.setBandLevel((short)i, (short) 0);
//			}
//			eq.setEnabled(true);
//			mMediaPlayer.setVolume(0.1f, 0.1f);
//			mMediaPlayer.setVolume(0.f, 0.f);
			//mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		}
		if(!mMediaPlayer.isPlaying()) {
			// https://stackoverflow.com/questions/1283499/setting-data-source-to-an-raw-id-in-mediaplayer
			mMediaPlayer.start();
			isMusicPlaying |= 0x1;
		}
		//EnhanceService.sendEmptyMessage(0);
	}
	
	private void pauseMusic() {
		if(mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			isMusicPlaying &= ~0x1;
		}
	}
	
	private void stopMusic() {
		if(mMediaPlayer!=null) {
			mMediaPlayer.pause();
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			isMusicPlaying &= ~0x1;
		}
	}
	
	private void acquireWifiLock() {
		if(wifiLock==null) {
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
			// WifiManager.WIFI_MODE_FULL_HIGH_PERF
		}
		if(wifiLock!=null && !wifiLock.isHeld()){
			CMN.Log("wifi lock acquireed..");
			wifiLock.acquire();
			isMusicPlaying |= 0x2;
		}
	}
	
	private void releaseWifiLock() {
		if(wifiLock!=null && wifiLock.isHeld()){
			wifiLock.release();
			isMusicPlaying &= ~0x2;
		}
	}
	
	int cc=0;
    int theta=0;
    
	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.start();
		
		CMN.Log("onCompletion", cc++, mServer!=null && mServer.isAlive());
		
		
		wifiLock.release();
		wifiLock.acquire();
		
		if(cc/100>theta) {
			CMN.Log("wakeUpAndUnlock", wifiLock.isHeld());
			if(!wifiLock.isHeld()){
				wifiLock.acquire();
			}
			
			//TestHelper.wakeUpAndUnlock(this);
			theta++;
		}
	}
	
	private Notification getNotification() {
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("点击查词")
				//.setCustomContentView()
				//.setCustomBigContentView()
				//.setContentText("▶")
				;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(Id);
		}
		
		if(PDICMainAppOptions.getShowNotificationSubtitle())
		{
			builder.setContentText("冬天来了,春天还会远吗");
		}
		if(PDICMainAppOptions.getShowNotificationSettings())
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			settingsIntent.putExtra("realm", NotificationSettings.id);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, settingsIntent, FLAG_UPDATE_CURRENT);
			
			builder.addAction(R.drawable.ic_settings_noti_action, "设置", pendingIntent);
		}
		
		if(PDICMainAppOptions.getShowNotificationExitBtn())
		{
			Intent settingsIntent = new Intent(this, ServiceEnhancer.class);
			settingsIntent.putExtra("close", true);
			PendingIntent pendingIntent = PendingIntent.getService(this, 0, settingsIntent, FLAG_UPDATE_CURRENT);
			
			builder.addAction(R.drawable.ic_close_noti_action, "退出", pendingIntent);
		}
		
		
		Intent shareIntent = new Intent(this, MainShareActivity.class);
		PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0, shareIntent, 0);
		
		builder.setContentIntent(pendingShareIntent);
		
		return builder.build();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null) {
			if(intent.hasExtra("exit")) {
				stopMusic();
				releaseWifiLock();
			} else if(intent.hasExtra("realm")) {
				setupDaemon(-1);
			} else if(intent.hasExtra("close")) {
				stopSelf();
			}
		}
        return START_STICKY;
    }
	
	@Override
	public void onDestroy() {
		CMN.Log("onDestroy!!!");
		super.onDestroy();
		isRunning = false;
		if(mbatteryReceiver!=null) {
			unregisterReceiver(mbatteryReceiver);
			mbatteryReceiver = null;
		}
		stopMusic();
		releaseWifiLock();
	}
	
	private boolean isPowerSaveModeCompat() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& powerManager.isPowerSaveMode()) { // hopefully...
			return true;
		}
		if (_HX==0) {
			return false;
		}
		else if (_HX==1) {
			try {
				int value = Settings.System.getInt(getContentResolver(), "SmartModeStatus");
				//CMN.debug("isPowerSaveModeCompat::huawei::", value);
				// value 4==Save Mode; 1==Ultra Save Mode==Normal Mode;
				//  ( tested on my huawei vtr-al00 )
				if(value==4) {
					return true;
				}
				if(value==1) {
					// what if Ultra save mode???
					// https://github.com/huaweigerrit
					// https://github.com/SivanLiu/HwFrameWorkSource
					
					// https://stackoverflow.com/questions/2641111/where-is-android-os-systemproperties
//					Class sysProp= Class.forName("android.os.SystemProperties");
//					Method sysProp_getBool = sysProp.getMethod("getBoolean", new Class[]{String.class, boolean.class});
//					Object[] parms = new Object[]{"sys.super_power_save", false};
//					CMN.debug("huawei::UltraPowerSave::", sysProp_getBool.invoke(null, parms));
//					CMN.debug("huawei::UltraPowerSave::", getSystemProperty("sys.super_power_save"));
					return "true".equals(getSystemProperty("sys.super_power_save"));
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		else if (_HX==2){
			try {
				int value = Settings.System.getInt(getContentResolver(), "POWER_SAVE_MODE_OPEN");
				CMN.debug("isPowerSaveModeCompat::xiaomi::", value);
				// dont have xiaomi. not tested.
				return value==1;
			} catch (Exception e) {
				CMN.debug(e);
			}
		}
		// else if...
		return false;
	}
	
	// https://stackoverflow.com/questions/9937099/how-to-get-the-build-prop-values
	public String getSystemProperty(String key) {
		String value = null;
		
		try {
			value = (String) Class.forName("android.os.SystemProperties")
					.getMethod("get", String.class).invoke(null, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
}