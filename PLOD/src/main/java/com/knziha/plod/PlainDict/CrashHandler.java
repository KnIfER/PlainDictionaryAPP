package com.knziha.plod.plaindict;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.dictionary.Utils.SU;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//美滋滋
@SuppressLint({"WrongConstant"})
public class CrashHandler implements UncaughtExceptionHandler {
	public static final String TAG = "FatalHandler";
	public static Object hotTracingObject;
	public static String hotDebugMessage;
	/** System default handler */
	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler instance;
	//private Context mContext;

	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd, HH-mm-ss", Locale.CHINA);
	StringBuilder info_builder;
	private String log_path;
	private final boolean bSilentExitBypassingSystem;
	private final boolean bLogToFile;
	private boolean registered;
	private boolean turnedon;

	public static CrashHandler getInstance(Context contex, PDICMainAppOptions opt) {
		if(instance == null)
			instance = new CrashHandler(contex,opt);
		return instance;
	}

	public String getLogFile(){
		return log_path;
	}

	private CrashHandler(Context contex, PDICMainAppOptions opt){
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		bSilentExitBypassingSystem = true;//opt.getSilentExitBypassingSystem();
		bLogToFile = opt.getLogToFile();
		info_builder=new StringBuilder();
		info_builder.setLength(0);
		info_builder.append(contex.getResources().getString(R.string.app_name)).append(" [").append(Build.MANUFACTURER)
				.append(", v").append(Build.VERSION.SDK_INT).append("_").append(Build.VERSION.CODENAME);
	}

	public void register(Context context) {
		if(registered) return;
		Thread.setDefaultUncaughtExceptionHandler(this);
		log_path=context.getExternalFilesDir("").getAbsolutePath()+"/logs/crash.txt";
		PackageManager pm = context.getPackageManager();
		String packageName = context.getPackageName();
		PackageInfo program_info=null;
		info_builder.append("] [v");
		try {
			program_info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES|PackageManager.GET_SIGNATURES);
		} catch (Exception e) { CMN.Log(e); }
		if (program_info != null) {
			info_builder.append(program_info.versionName).append(", ").append(program_info.versionCode);
			try{
				Signature[] signatures = program_info.signatures;
				if(signatures!=null && signatures.length>0) {
					byte[] cert = signatures[0].toByteArray();
					InputStream input = new ByteArrayInputStream(cert);
					byte[] publicKey = MessageDigest.getInstance("SHA1").digest(((X509Certificate)CertificateFactory.getInstance("X509").generateCertificate(input)).getEncoded());
//					BU.printBytes(publicKey);
//					CMN.Log(111);
//					CMN.Log(new String(publicKey)); //todo ???
				}
			} catch (Exception e){ CMN.Log(e); }
		}
		info_builder.append("]\n");
		ApplicationInfo app_info = context.getApplicationInfo();
		GlobalOptions.debug=SU.debug=(app_info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		registered=true;
	}

	public void unRegister(){
		registered=false;
		if(mDefaultHandler!=null)
			Thread.setDefaultUncaughtExceptionHandler(mDefaultHandler);
	}

	public void TurnOn(){
		turnedon=true;
	}

	public void TurnOff(){
		turnedon=false;
	}

	@Override
	public void uncaughtException(@NonNull Thread thread, @NonNull Throwable exception) {
		Log.e("ODPlayer","::fatal exception : "+exception.toString());
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		exception.printStackTrace(printWriter);
		Throwable cause = exception.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		String time = formatter.format(new Date());
		info_builder.append("crash-=====Log-start=====")
				.append(time).append("\n");
		
		if(hotTracingObject!=null) info_builder.append("at : ").append(hotTracingObject).append("\n");
		if(hotDebugMessage!=null) info_builder.append("at : ").append(hotDebugMessage).append("\n");
		;
		info_builder.append(result);
		String trace = null;
		if(bLogToFile || GlobalOptions.debug){
			try {
				File log=new File(log_path);
				File dir = log.getParentFile();
				dir.mkdirs();
				if(log.isDirectory()) log.delete();
				FileOutputStream fos = new FileOutputStream(log_path);
				trace = info_builder.toString();
				fos.write(trace.getBytes()); fos.close();
				new File(dir, "lock").mkdirs();
				if (GlobalOptions.debug) {
					fos = new FileOutputStream(new File(new File(log_path).getParentFile(), "crash_"+CMN.now()+".log"));
					fos.write(trace.getBytes()); fos.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "an error occured while writing file...", e);
			}
		}
		CMN.Log("crash catched", GlobalOptions.debug?trace:"_"+exception);
		postUncaughtException(thread, exception);
	}

	private void postUncaughtException(@NonNull Thread thread, @NonNull Throwable exception) {
		if(!turnedon || !registered || !bSilentExitBypassingSystem){
			if(mDefaultHandler != null)
				mDefaultHandler.uncaughtException(thread, exception);
			unRegister();
		}else {
			unRegister();
			System.exit(1);
		}
	}
	
	public void showErrorMessage(Context context, DialogInterface.OnClickListener btnLis, boolean simulated) {
		String message = null;
		String title = "天哪，崩溃了……";
		if (simulated) {
			title = "[模拟] "+title;
			try{
				throw new RuntimeException(title);
			} catch (Exception e) {
				message = CMN.Log(e);
			}
		} else {
			File log = new File(getLogFile());
			if (log.exists()) {
				try {
					byte[] buffer = new byte[Math.min((int) log.length(), 4096)];
					int len = new FileInputStream(log).read(buffer);
					message=new String(buffer,0,len);
					if(GlobalOptions.debug||btnLis==null) {
						CMN.Log(message);
					}
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
		}
		if (message!=null) {
			String finalMessage = message;
			View btn = new AlertDialog.Builder(context)
					.setMessage(message)
					.setPositiveButton(android.R.string.yes, btnLis)
					.setNeutralButton(android.R.string.copy, null)
					.setTitle(title)
					.setCancelable(btnLis == null)
					.show()
					.findViewById(android.R.id.button3);
			btn.setOnClickListener(v -> {
				if (context instanceof Toastable_Activity) {
					((Toastable_Activity) context).FuzhiText(finalMessage);
				}
			});
			if (simulated) {
				String finalTitle = title;
				btn.setOnLongClickListener(v -> {
					throw new RuntimeException(finalTitle);
				});
			}
		}
	}
}
