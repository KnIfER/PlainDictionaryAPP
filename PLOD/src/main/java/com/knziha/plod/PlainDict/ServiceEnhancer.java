package com.knziha.plod.plaindict;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.knziha.plod.widgets.ViewUtils;

public class ServiceEnhancer extends Service {
	private NotificationManager notificationManager;
	
	PhoneStateListener phoneStateListener;
	TelephonyManager telephonyManager;
	String Id = "callingChannelId";
	String Name = "callingChannelName";
	private NotificationChannel channel;
	
	@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    Handler EnhanceService  = new Handler(){
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
    
    @RequiresApi(api = Build.VERSION_CODES.O)
	@Override
    public void onCreate() {
        super.onCreate();
	
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	
		if(ViewUtils.bigMountain) {
			channel = new NotificationChannel(Id, Name, NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(channel);
		}
		
		startForeground(1, getNotification());

//		MediaPlayer mMediaPlayer=MediaPlayer.create(this, R.raw.toomuch);
//		mMediaPlayer.setOnCompletionListener(MediaPlayer::start);
//		mMediaPlayer.start();
	
		//EnhanceService.sendEmptyMessage(0);
    }
	
	private Notification getNotification() {
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("后台服务 (防止自动退出)")
				//.setCustomContentView()
				//.setCustomBigContentView()
				.setContentText("▶");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			builder.setChannelId(Id);
		}
		return builder.build();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}