package babybear.akbquiz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
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

public class ConfigActivity extends Activity {

	final static String PreferenceName_cfg = "config";
	static Music defaultMusic = null;

	int onModifing = -1;

	ViewFlipper cfgflipper;
	SharedPreferences sp_cfg;
	Editor cfgEditor;

	List<Music> playlistList, musicList;
	ListView playlistView;

	boolean isPlaylistChanged = false;

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

		OnClickListener l_switch = new OnClickListener() {

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
					cfgEditor.putBoolean(Database.ColName_switch_bg, isSoundOn);
					MainMenu.se.setSwitch(isSoundOn);
					break;
				case R.id.config_vibration_switch:
					boolean isVibOn = ((ToggleButton) v).isChecked();
					cfgEditor.putBoolean(Database.ColName_switch_bg, isVibOn);
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
				}
			}

		};

		bgm_toggle.setOnClickListener(l_switch);
		sound_toggle.setOnClickListener(l_switch);
		vibration_toggle.setOnClickListener(l_switch);
		config_playlist.setOnClickListener(l_switch);

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
						((Button) findViewById(R.id.config_playlist_remove)).setText("移除");
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
						((Button) findViewById(R.id.config_playlist_remove)).setText("取消");
					}

				});

		((Button) findViewById(R.id.config_playlist_remove))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(onModifing != playlistList.size()){
						
							playlistList.remove(onModifing);
	
	//						PlaylistAdapter adapter = (PlaylistAdapter) playlistView
	//								.getAdapter();
	//						adapter.remove(adapter.getItem(onModifing));
							savePlaylist();
	
							isPlaylistChanged = true;
						}
						cfgflipper.showPrevious();
					}

				});

	}

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

	private Music matchMusic(String DATA) {
		if (DATA.equals("default"))
			return defaultMusic;
		for (int i = 0, length = musicList.size(); i < length; i++) {
			if (DATA.equals(musicList.get(i).DATA))
				return musicList.get(i);
		}
		return null;
	}

	private void savePlaylist() {
		JSONArray arr = new JSONArray();
		for (int i = 0, length = playlistList.size(); i < length; i++) {
			arr.put(playlistList.get(i).DATA);
		}
		cfgEditor.putString("playlist", arr.toString());
	}

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
	
	@SuppressWarnings("unused")
	private class Music {
		int _ID;
		long DURATION;
		boolean isExist;
		String TITLE, ARTIST, ALBUM, DATA;
	}
}