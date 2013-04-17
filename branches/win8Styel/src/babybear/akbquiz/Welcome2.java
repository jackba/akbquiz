package babybear.akbquiz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Welcome2 extends Activity {
	private static String envFilePath = null;

	private static final String KEY_APPVER_CODE = "app_ver_code";
	private static final String KEY_APPVER_NAME = "app_ver_name";
	private static final String KEY_DATAVER = "database_ver";
	private static final String KEY_SDK_CODE = "api_level";
	private static final String KEY_SDK_NAME = "android_ver";
	private static final String KEY_DEVICE = "device";
	private static final String KEY_BRADN = "brand";

	public static final int DATA_VER = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fanhuashe1);
		envFilePath = Environment.getExternalStorageDirectory().getPath()
				+ "/Android/data/" + getPackageName() + "/env.json";
		verCheck();
		logo();
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 在欢迎界面屏蔽BACK键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return false;
	}

	// void waring() {
	// Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_out);
	// anim.setFillEnabled(true); // 启动Fill保持
	// anim.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
	//
	// findViewById(R.id.full).setAnimation(anim);
	//
	// new Handler().postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// logo();
	// }
	// }, 3000);
	//
	// anim.startNow();
	// }

	/**
	 * 检查是否是首次运行 数据库版本是否是最新
	 */
	private void verCheck() {
		new Thread() {
			public void run() {

				File envFile = new File(envFilePath);
				if (envFile.exists()) {
					try {
						BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
						String jsonString = buffer.readLine();
						buffer.close();
						JSONObject obj = new JSONObject(jsonString);
						int dataver = obj.getInt(KEY_DATAVER);
						if (dataver < Database.quizdb_ver) {
							refreshEnvFile(envFile);
							copyDatabase();
						}

					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					catch (JSONException e) {
						e.printStackTrace();
					}

					;
				} else {
					envFile.getParentFile().mkdirs();
					refreshEnvFile(envFile);
					copyDatabase();
					firstRun();
				}
			}

		}.start();

	}

	/**
	 * 首次运行 配置设置文件
	 */
	private void firstRun() {
		SharedPreferences sp_cfg = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Editor cfg_Editor = sp_cfg.edit();
		cfg_Editor.putBoolean(Database.KEY_switch_bg, true);
		cfg_Editor.putBoolean(Database.KEY_switch_sound, true);
		cfg_Editor.putBoolean(Database.KEY_switch_vibration, true);
		cfg_Editor.putInt(Database.KEY_vol_bg, 10);
		cfg_Editor.putInt(Database.KEY_vol_sound, 10);
		cfg_Editor.putString(Database.ColName_playlist, "{}");
		cfg_Editor.putBoolean(Database.KEY_use_custom_background, false);
		cfg_Editor.putInt(Database.KEY_tips_info, 0);
		cfg_Editor.putInt(Database.KEY_tips_quiz, 0);

		cfg_Editor.commit();
	}

	/**
	 * 刷新环境配置文件
	 * 
	 * @param file 环境配置文件
	 */
	private void refreshEnvFile(File file) {

		PackageInfo info;
		try {
			info = this.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0);
			int verCode = info.versionCode;
			String verName = info.versionName;

			JSONObject obj = new JSONObject();
			obj.put(KEY_APPVER_CODE, verCode);
			obj.put(KEY_APPVER_NAME, verName);
			obj.put(KEY_DATAVER, Database.quizdb_ver);

			obj.put(KEY_DEVICE, Build.PRODUCT);
			obj.put(KEY_BRADN, Build.BRAND);
			obj.put(KEY_SDK_CODE, Build.VERSION.SDK_INT);
			obj.put(KEY_SDK_NAME, Build.VERSION.RELEASE);

			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(obj.toString());
			writer.flush();
			writer.close();
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 复制数据库
	private void copyDatabase() {
		AssetManager am = this.getAssets();
		File fileout = new File(Database.databasePath);
		if (fileout.exists()) {
			fileout.delete();
		}
		File parent = fileout.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		
		try {
			InputStream is = am.open("q.db");

			fileout.createNewFile();
			FileOutputStream os = new FileOutputStream(fileout);

			int temp = 0;
			temp = is.read();
			while (temp != -1) {
				os.write(temp);
				temp = is.read();
			};

			os.flush();
			os.close();
			is.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * LOGO的进入动画
	 */
	private void logo() {
		setContentView(R.layout.fanhuashe1);

		ImageView petal = (ImageView) findViewById(R.id.petal);
		Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.logo_petal);
		anim1.setFillEnabled(true); // 启动Fill保持
		anim1.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		petal.setAnimation(anim1);
		anim1.startNow();

		ImageView text = (ImageView) findViewById(R.id.text);
		Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.logo_text);
		anim2.setFillEnabled(true); // 启动Fill保持
		anim2.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		text.setAnimation(anim2);

		anim2.startNow();
		ImageView smalltext = (ImageView) findViewById(R.id.smalltext);
		Animation anim3 = AnimationUtils.loadAnimation(this,
				R.anim.logo_smalltext);
		anim3.setFillEnabled(true); // 启动Fill保持
		anim3.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		smalltext.setAnimation(anim3);
		anim3.startNow();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				logo_fadeout();
			}
		}, anim3.getDuration() + anim3.getStartOffset());

	}

	/**
	 * LOGO的淡出动画
	 */
	private void logo_fadeout() {
		ViewGroup layout = (ViewGroup) findViewById(R.id.full);
		Animation anim3 = AnimationUtils.loadAnimation(this,
				R.anim.wait_fade_out);
		anim3.setFillEnabled(true); // 启动Fill保持
		anim3.setFillAfter(true); // 设置动画的最后一帧是保持在View上面

		layout.setAnimation(anim3);
		anim3.startNow();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				pregame();
			}
		}, anim3.getDuration() + anim3.getStartOffset());
	}

	/**
	 * 进入游戏之前的动画
	 */
	private void pregame() {
		setContentView(R.layout.activity_welcome);
		ViewGroup layout = (ViewGroup) findViewById(R.id.full);

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

		anim.setFillEnabled(true);
		anim.setFillAfter(true);
		layout.setAnimation(anim);
		anim.startNow();

		Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.twinkling);
		TextView clickToStart = (TextView) findViewById(R.id.click_to_start);
		clickToStart.setAnimation(anim1);
		anim1.startNow();

		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				twink();
			}

		});
	}

	/**
	 * 闪烁的动画
	 */
	private void twink() {
		Animation anim2 = AnimationUtils.loadAnimation(this,
				R.anim.twinkling_quick);

		findViewById(R.id.click_to_start).setAnimation(anim2);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(Welcome2.this, MainMenu.class);
				startActivity(intent);
				Welcome2.this.finish();
			}
		}, anim2.getDuration());
		anim2.startNow();
	}
}
