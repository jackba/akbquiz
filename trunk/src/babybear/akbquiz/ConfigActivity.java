package babybear.akbquiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.sso.SsoHandler;

public class ConfigActivity extends Activity {
	final static String TAG = "ConfigActivity";
	final static String PreferenceName_cfg = "config";
	final static String SERVER_URL = "http://fanhuashe-app.stor.sinaapp.com/";
	final static String APP_VER = "appver.json";
	final static String DATABASE_VER = "databasever.json";
	final static String APP_SAVENAME = "/download/akbquiz.apk";
	static Music defaultMusic = null;

	int onModifing = -1;

	ViewFlipper cfgflipper;
	SharedPreferences sp_cfg;
	Editor cfgEditor;
	Button weibo_btn;

	List<Music> playlistList, musicList;
	ListView playlistView;

	boolean isPlaylistChanged = false;
	SsoHandler weiboSsoHandler ;

	int newVerCode, verCode;
	String newVerName, verName, updateURL;

	ProgressDialog pBar;
	private Handler handler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);

		cfgflipper = (ViewFlipper) findViewById(R.id.cfg_flipper);

		sp_cfg = getSharedPreferences(PreferenceName_cfg, Context.MODE_PRIVATE);
		cfgEditor = sp_cfg.edit();

		Music defaultbg = new Music();

		defaultbg._ID = -1;
		defaultbg.ALBUM = "ここにいたこと";
		defaultbg.ARTIST = "AKB48";
		defaultbg.DATA = "android.resource://" + getPackageName() + "/"
				+ R.raw.bg;
		defaultbg.DURATION = 273100;
		defaultbg.TITLE = "少女たちよ(默认背景音乐)";
		defaultbg.isExist = true;

		defaultMusic = defaultbg;

		init();
		weiboInit();

	}

	
	/**
	 * 初始化微博相关的对象及界面
	 */
	private void weiboInit() {
		weibo_btn = (Button) findViewById(R.id.config_weibo);
		weibo_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!MainMenu.weiboAccessToken.isSessionValid()) {
					weiboSsoHandler = new SsoHandler(ConfigActivity.this,
							MainMenu.weibo);
					weiboSsoHandler.authorize(new AuthDialogListener());
				} else {
					AccessTokenKeeper.clear(ConfigActivity.this);
					MainMenu.weiboAccessToken = AccessTokenKeeper
							.readAccessToken(ConfigActivity.this);
					weibo_btn.setText(ConfigActivity.this
							.getString(R.string.weibo_linkto));
				}
			}
		});
		if (MainMenu.weiboAccessToken.isSessionValid()) {
			weibo_btn.setText(this.getString(R.string.weibo_linked));
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (weiboSsoHandler != null) {
			weiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (cfgflipper.getDisplayedChild() == 0) {
				cfgEditor.commit();
				finish();
			} else {
				cfgflipper.showPrevious();
				return false;
			}
			break;
		}

		return super.onKeyDown(keyCode, event);

	}

	/**
	 * 初始化
	 */
	private void init() {
		ToggleButton bgm_toggle = (ToggleButton) findViewById(R.id.bgm_switch);
		ToggleButton sound_toggle = (ToggleButton) findViewById(R.id.sound_switch);
		ToggleButton vibration_toggle = (ToggleButton) findViewById(R.id.config_vibration_switch);
		SeekBar bgm_vol = (SeekBar) findViewById(R.id.bgm_volume);
		SeekBar sound_vol = (SeekBar) findViewById(R.id.sound_volume);
		Button config_playlist = (Button) findViewById(R.id.config_playlist);
		
		bgm_toggle.setChecked(sp_cfg.getBoolean(Database.ColName_switch_bg,
				true));
		sound_toggle.setChecked(sp_cfg.getBoolean(
				Database.ColName_switch_sound, true));
		vibration_toggle.setChecked(sp_cfg.getBoolean(
				Database.ColName_switch_vibration, true));
		bgm_vol.setProgress(sp_cfg.getInt(Database.ColName_vol_bg, 10));
		sound_vol.setProgress(sp_cfg.getInt(Database.ColName_vol_sound, 10));
		
		//TODO load loop mode

		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.bgm_switch:
					boolean isBgOn = ((ToggleButton) v).isChecked();
					cfgEditor.putBoolean(Database.ColName_switch_bg, isBgOn);
					Message msg = new Message();
					msg.what = isBgOn ? 1 : 0;
					msg.arg1 = BgMusic.BGHandler.SWITCH_CHANGE;
					BgMusic.bgHandler.sendMessage(msg);
					break;
				case R.id.sound_switch:
					boolean isSoundOn = ((ToggleButton) v).isChecked();
					cfgEditor.putBoolean(Database.ColName_switch_sound,
							isSoundOn);
					MainMenu.se.setSwitch(isSoundOn);
					break;
				case R.id.config_vibration_switch:
					boolean isVibOn = ((ToggleButton) v).isChecked();
					cfgEditor.putBoolean(Database.ColName_switch_vibration,
							isVibOn);
					break;
				case R.id.config_playlist:
					if (Environment.MEDIA_MOUNTED.equals(Environment
							.getExternalStorageState())) {
						if (!isPlaylistChanged) {
							loadPlaylistEditor();
						}

						cfgflipper.showNext();

					} else {
						Toast.makeText(ConfigActivity.this, "SD卡不可用",
								Toast.LENGTH_SHORT).show();
					}
					break;
				case R.id.config_ranking:
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id="
							+ getPackageName()));
					startActivity(intent);
					break;

				case R.id.config_back:
					cfgEditor.commit();
					finish();
					break;

				case R.id.config_musiclist_back:
				case R.id.config_playlist_back:
					cfgflipper.showPrevious();
					break;

				case R.id.config_update:
					verCode = getVerCode(ConfigActivity.this);
					verName = getVerName(ConfigActivity.this);
					if (getServerVer()) {
						if (newVerCode > verCode) {
							doNewVersionUpdate(); // 更新新版本
						} else {
							notNewVersionShow(); // 提示当前为最新版本
						}
					}

					break;
				case R.id.config_loopmode:
					changeLoopMode();
					break;
				case R.id.config_quiz_submit:
					Intent intent1 = new Intent(ConfigActivity.this,CollectQuiz.class);
					startActivity(intent1);
				}
			}


		};

		bgm_toggle.setOnClickListener(clickListener);
		sound_toggle.setOnClickListener(clickListener);
		vibration_toggle.setOnClickListener(clickListener);
		config_playlist.setOnClickListener(clickListener);

		((Button) findViewById(R.id.config_back)).setOnClickListener(clickListener);
		((Button) findViewById(R.id.config_musiclist_back))
				.setOnClickListener(clickListener);
		((Button) findViewById(R.id.config_playlist_back))
				.setOnClickListener(clickListener);
		((Button) findViewById(R.id.config_update))
				.setOnClickListener(clickListener);
		((Button) findViewById(R.id.config_quiz_submit))
				.setOnClickListener(clickListener);
		((Button) findViewById(R.id.config_ranking))
				.setOnClickListener(clickListener);
		
		OnSeekBarChangeListener l_seekbar = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				switch (arg0.getId()) {
				case R.id.bgm_volume:
					cfgEditor.putInt(Database.ColName_vol_bg, arg1);
					Message msg = new Message();
					msg.what = arg1;
					msg.arg1 = BgMusic.BGHandler.VOL_CHANGE;
					BgMusic.bgHandler.sendMessage(msg);
					break;
				case R.id.sound_volume:
					cfgEditor.putInt(Database.ColName_vol_sound, arg1);
					MainMenu.se.setVolume(arg1);
					break;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {

			}

		};
		bgm_vol.setOnSeekBarChangeListener(l_seekbar);
		sound_vol.setOnSeekBarChangeListener(l_seekbar);
	}

	
	/**
	 * 发现新版本要做的
	 */
	private void doNewVersionUpdate() {
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本:");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(", 发现新版本:");
		sb.append(newVerName);
		sb.append(" Code:");
		sb.append(newVerCode);
		sb.append(", 是否更新?");
		Dialog dialog = new AlertDialog.Builder(this)
				.setTitle("软件更新")
				.setMessage(sb.toString())
				// 设置内容
				.setPositiveButton("更新",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pBar = new ProgressDialog(ConfigActivity.this);
								pBar.setTitle("正在下载");
								pBar.setMessage("请稍候...");
								pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
								pBar.setMax(100);
								downFile(updateURL);
							}
						})
				.setNegativeButton("暂不更新",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								
							}
						}).create();// 创建
		// 显示对话框
		dialog.show();
	}

	/**
	 * 没有发现新版本要做的
	 */
	private void notNewVersionShow() {
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本:");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(",\n已是最新版,无需更新!");
		Dialog dialog = new AlertDialog.Builder(this).setTitle("软件更新")
				.setMessage(sb.toString())// 设置内容
				.setPositiveButton("确定",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
							}
						}).create();
		// 显示对话框
		dialog.show();
	}

	/**
	 * 下载文件
	 * @param url 文件URL
	 */
	void downFile(final String url) {
		pBar.show();
		new Thread() {

			private long length;
			private long count = 0;
			public void run() {
				HttpClient client = new DefaultHttpClient();
				//HttpGet get = ;
				HttpResponse response;
				try {
					response = client.execute(new HttpGet(url));
					HttpEntity entity = response.getEntity();
					length = entity.getContentLength();
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File file = new File(
								Environment.getExternalStorageDirectory(),
								APP_SAVENAME);
						fileOutputStream = new FileOutputStream(file);
						byte[] buf = new byte[1024];
						int ch = -1;
						count = 0;
						while ((ch = is.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, ch);
							count += ch;
							handler.post(new Runnable() {
								public void run() {
									pBar.setProgress((int) (count*100/length));
								}
							});
						}
					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					{
						handler.post(new Runnable() {
							public void run() {
								pBar.cancel();
								update();
							}
						});
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 执行升级
	 */
	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), APP_SAVENAME)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	/**
	 * 获取当前版本号信息
	 * @param context
	 * @return 当前版本号
	 */
	public int getVerCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verCode;
	}

	/**
	 * 获取当前版本名信息
	 * @param context
	 * @return 版本名
	 */
	public String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verName;
	}

	/**
	 * 获取服务端版本号和版本名
	 * @return 成功获取返回true
	 */
	private boolean getServerVer() {
		try {
			String verjson = getURLContent(SERVER_URL + APP_VER);
			JSONArray array = new JSONArray(verjson);
			if (array.length() > 0) {
				JSONObject obj = array.getJSONObject(0);
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
					newVerName = obj.getString("verName");
					updateURL = obj.getString("apkurl");
				} catch (Exception e) {
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * 获取http内容
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String getURLContent(String url) throws Exception {
		StringBuilder sb = new StringBuilder();

		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		// 设置网络超时参数
		HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
		HttpConnectionParams.setSoTimeout(httpParams, 5000);
		HttpResponse response = client.execute(new HttpGet(url));
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent(), "UTF-8"), 8192);

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			reader.close();
		}
		return sb.toString();
	}

	
	/**
	 * 加载设置播放列表编辑器
	 */
	private void loadPlaylistEditor() {

		musicList = queryMusics();
		playlistList = loadPlaylist();

		PlaylistAdapter playlistAdapter = new PlaylistAdapter(this,
				playlistList, new OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = Integer.decode((String) v.getTag());
						// Toast.makeText(Config.this, "a click on : "+position,
						// Toast.LENGTH_SHORT).show();
						onModifing = position;
						cfgflipper.showNext();
						((TextView) findViewById(R.id.current))
								.setText("正在操作第 " + (onModifing + 1) + " 项 \""
										+ playlistList.get(onModifing).TITLE
										+ " \":");
						((Button) findViewById(R.id.config_playlist_remove))
								.setText("移除");
					}
				});
		playlistView = (ListView) findViewById(R.id.playlist);
		playlistView.setAdapter(playlistAdapter);

		PlaylistAdapter musiclistAdapter = new PlaylistAdapter(this, musicList,
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						int position = Integer.parseInt((String) arg0.getTag());
						Log.d("", "set " + onModifing + " in Playlist with "
								+ position + " in Musiclist");
						Music temp = musicList.get(position);
						// playlistAdapter.;
						if (onModifing == playlistList.size())
							playlistList.add(temp);
						else
							playlistList.set(onModifing, temp);
						PlaylistAdapter adapter = (PlaylistAdapter) playlistView
								.getAdapter();
						adapter.remove(adapter.getItem(onModifing));
						adapter.insert(temp, onModifing);
						savePlaylist();

						isPlaylistChanged = true;
						cfgflipper.showPrevious();
					}

				});
		ListView musiclistView = (ListView) findViewById(R.id.musiclist);
		musiclistView.setAdapter(musiclistAdapter);

		((Button) findViewById(R.id.config_playlist_add))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int position = playlistList.size();
						onModifing = position;
						cfgflipper.showNext();
						((TextView) findViewById(R.id.current)).setText("添加:");
						((Button) findViewById(R.id.config_playlist_remove))
								.setText("取消");
					}

				});

		((Button) findViewById(R.id.config_playlist_remove))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (onModifing != playlistList.size()) {

							playlistList.remove(onModifing);

							// PlaylistAdapter adapter = (PlaylistAdapter)
							// playlistView
							// .getAdapter();
							// adapter.remove(adapter.getItem(onModifing));
							savePlaylist();

							isPlaylistChanged = true;
						}
						cfgflipper.showPrevious();
					}

				});

	}

	/**
	 * 获取本机的音乐
	 * @return 本机的音乐列表
	 */
	private ArrayList<Music> queryMusics() {
		ArrayList<Music> musiclistResult = new ArrayList<Music>();
		ContentResolver cr = this.getContentResolver();
		Cursor musics = cr.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] {
						MediaStore.Audio.Media._ID, // int
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.DATA, // String
						MediaStore.Audio.Media.DISPLAY_NAME, // String
						MediaStore.Audio.Media.MIME_TYPE // String
				}, MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
						+ MediaStore.Audio.Media.DURATION + " > 10000", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		Log.d("", "musics : " + musics.getCount());

		musiclistResult.add(defaultMusic);

		musics.moveToFirst();
		while (!musics.isAfterLast()) {
			Music temp = new Music();
			temp._ID = musics.getInt(musics
					.getColumnIndex(MediaStore.Audio.Media._ID));
			temp.ALBUM = musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			temp.ARTIST = musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			temp.DATA = musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.DATA));
			temp.DURATION = musics.getLong(musics
					.getColumnIndex(MediaStore.Audio.Media.DURATION));
			temp.TITLE = musics.getString(musics
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			temp.isExist = true;

			musiclistResult.add(temp);
			musics.moveToNext();
		}
		musics.close();
		return musiclistResult;
	}

	/**
	 * 从文件获取保存的播放列表
	 * @return 播放列表
	 */
	private ArrayList<Music> loadPlaylist() {
		ArrayList<Music> playlist = new ArrayList<Music>();
		try {
			JSONArray arr = new JSONArray(sp_cfg.getString("playlist",
					"[\"default\"]"));
			for (int i = 0, length = arr.length(); i < length; i++) {
				Log.d("", " i = " + i);
				Music temp = matchMusic(arr.getString(i));
				playlist.add(temp);
			}

			Log.d("", "playlist.size() = " + playlist.size());

		} catch (JSONException e) {
			e.printStackTrace();
			playlist.add(defaultMusic);
			// return playlist;
		}
		return playlist;
	}

	/**
	 * 从路径获取音乐信息
	 * @param DATA
	 * @return 音乐信息
	 */
	private Music matchMusic(String DATA) {
		if (DATA.equals("default"))
			return defaultMusic;
		for (int i = 0, length = musicList.size(); i < length; i++) {
			if (DATA.equals(musicList.get(i).DATA))
				return musicList.get(i);
		}
		return null;
	}

	/**
	 * 保存播放列表
	 */
	private void savePlaylist() {
		JSONArray arr = new JSONArray();
		for (int i = 0, length = playlistList.size(); i < length; i++) {
			arr.put(playlistList.get(i).DATA);
		}
		cfgEditor.putString("playlist", arr.toString());
	}
	
	/**
	 * 切换背景音乐播放模式
	 */
	private void changeLoopMode() {
		// TODO 背景音乐模式切换
		
	}
	
	/**
	 * ListView的适配器
	 * @author BabyBeaR
	 *
	 */
	private class PlaylistAdapter extends ArrayAdapter<Music> {
		OnClickListener l;

		public PlaylistAdapter(Context context, List<Music> objects,
				OnClickListener listener) {
			super(context, 0, objects);
			l = listener;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(
						R.layout.playlist_item, null);
			}
			Music m = getItem(position);

			((TextView) view.findViewById(R.id.no)).setText((position + 1)
					+ ".");
			((TextView) view.findViewById(R.id.album)).setText(m.ALBUM);
			((TextView) view.findViewById(R.id.artist)).setText(m.ARTIST);
			((TextView) view.findViewById(R.id.title)).setText(m.TITLE);
			((TextView) view.findViewById(R.id.length)).setText(m.DURATION
					/ 60000 + ":" + (m.DURATION / 1000) % 60);

			view.setFocusable(true);
			view.setClickable(true);
			view.setTag("" + position);
			view.setOnClickListener(l);
			return view;
		}

	}

	/**
	 * 音乐的信息
	 * @author BabyBeaR
	 *
	 */
	@SuppressWarnings("unused")
	private class Music {
		int _ID;
		long DURATION;
		boolean isExist;
		String TITLE, ARTIST, ALBUM, DATA;
	}

	/**
	 * 新浪微博认证的回调对象
	 * @author BabyBeaR
	 *
	 */
	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			MainMenu.weiboAccessToken = new Oauth2AccessToken(token, expires_in);
			if (MainMenu.weiboAccessToken.isSessionValid()) {

				((Button) findViewById(R.id.config_weibo))
						.setText(ConfigActivity.this
								.getString(R.string.weibo_linked));
				AccessTokenKeeper.keepAccessToken(ConfigActivity.this,
						MainMenu.weiboAccessToken);
				Toast.makeText(ConfigActivity.this, "认证成功", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			Toast.makeText(getApplicationContext(),
					"认证错误 : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "认证取消",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"认证异常 : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

}