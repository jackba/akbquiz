package babybear.akbquiz;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BgMusic extends Service {
	private static String TAG = "BgMusic";
	MediaPlayer player;
	AudioManager am;
	int PlayingNo = 0;
	int playlistLength = 0;
	static final int MODE_LOOP = 0, MODE_RANDOM = 1, MODE_SINGLE = 2;
	int mode = MODE_LOOP;
	Uri[] playlist;
	Random r = new Random();
	boolean isOn = true;

	static BGHandler bgHandler;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "++onCreate++");
		// defaultMusicNum=Puzzle.BGMs.length();
		// totalMusicNum=Puzzle.user.CUSTOM_MUSIC_NUM + defaultMusicNum;

		super.onCreate();
		bgHandler = new BGHandler();

	}

	OnCompletionListener CompL = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			switch (mode) {
			case MODE_LOOP:
				PlayingNo++;
				if (PlayingNo >= playlistLength)
					PlayingNo = 0;
				break;
			case MODE_RANDOM:
				PlayingNo = r.nextInt(playlistLength);
				break;
			case MODE_SINGLE:

				break;
			}
			player.release();
			Log.d(TAG, "playnext isPlayNo = " + PlayingNo);

			try {
				if (playlistLength > 0) {
					player = MediaPlayer.create(BgMusic.this,
							playlist[PlayingNo]);
					if (isOn)
						player.start();
				} else {
					player = MediaPlayer.create(
							BgMusic.this,
							Uri.parse("android.resource://" + getPackageName()
									+ "/" + R.raw.bg));
				}
			} catch (IllegalArgumentException e) {
				errorToast(e);
				e.printStackTrace();
			} catch (SecurityException e) {
				errorToast(e);
				e.printStackTrace();
			} catch (IllegalStateException e) {
				errorToast(e);
				e.printStackTrace();
			}

			player.setOnCompletionListener(CompL);
			player.setOnErrorListener(ErrL);

			Log.d(TAG, "play start");
		}

	};

	void errorToast(Exception e) {
		Toast.makeText(BgMusic.this,
				"Unexpected error in: " + TAG + " . error:" + e.getMessage(),
				Toast.LENGTH_SHORT).show();
	}

	OnErrorListener ErrL = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Toast.makeText(BgMusic.this, "Unexpected media player error.",
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Unexpected media player error.");
			return false;
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "+onStart+");
		isOn = intent.getBooleanExtra(Database.ColName_switch_bg, true);

		setPlaylist(getSharedPreferences("config", Context.MODE_PRIVATE)
				.getString(Database.ColName_playlist, ""));
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,
				intent.getIntExtra(Database.ColName_vol_bg, 10), 0);

		if (playlist!=null) {
			player = MediaPlayer.create(this, playlist[PlayingNo]);
			if (isOn)
				player.start();
		} else {
			player = MediaPlayer.create(
					this,
					Uri.parse("android.resource://" + getPackageName() + "/"
							+ R.raw.bg));
			if (isOn)
				player.start();
		}
		player.setLooping(false);
		player.setOnCompletionListener(CompL);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Log.d(TAG, "--onDestroy--");
		player.release();

	}

	public void setPlaylist(String uristr) {
		playlistLength = 0;
		// if(uristr==null) uristr="";
		// String[] uriStrings=uristr.split(regularExpression);
		// if(uriStrings.length==0)return;
		// for (int i=0;i<uriStrings.length;i++){
		// if(!uriStrings[i].equals("")){
		// playlist[playlistLength]=Uri.parse(uriStrings[i]);
		// playlistLength++;
		// }
		// }
		// for(int i=0;i<playlistLength;i++){
		// Log.d("BgMusic","playlist[" +i+"] = "+ playlist[i].toString());
		// }
		//
		try {
			JSONArray arr = new JSONArray(uristr);
			playlistLength = arr.length();
			if (playlistLength == 0)
				return;
			playlist= new Uri[playlistLength];
			for (int i = 0; i < playlistLength; i++) {
				playlist[i] = Uri.parse(arr.getString(i));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class BGHandler extends Handler {
		public static final int VOL_CHANGE = 1, SWITCH_CHANGE = 2,
				LOOP_CHANGE = 3, PLAYLIST_CHANGE = 4, PAUSE = -1, PLAY = 0;

		public BGHandler() {

		}

		public BGHandler(Looper L) {
			super(L);
		}

		@Override
		public void handleMessage(Message msg) {

			Log.d(TAG, "msg.arg1 : " + msg.arg1 + " msg.what : " + msg.what);
			switch (msg.arg1) {
			case PAUSE:
				if (player.isPlaying())
					player.pause();
				break;
			case PLAY:
				if (!player.isPlaying())
					player.start();
				break;

			case VOL_CHANGE:
				am.setStreamVolume(AudioManager.STREAM_MUSIC, msg.what, 0);
				break;
			case SWITCH_CHANGE:
				switch (msg.what) {
				case 0:
					isOn = false;
					player.pause();
					break;
				case 1:
					isOn = true;
					player.start();
					break;
				}
				break;
			case LOOP_CHANGE:
				mode = msg.what;
				break;
			case PLAYLIST_CHANGE:
				if (player != null) {
					if (player.isPlaying())
						player.stop();
					player.release();
				}

				PlayingNo = 0;
				setPlaylist(getSharedPreferences("config", Context.MODE_PRIVATE)
						.getString(Database.ColName_playlist, ""));

				if (playlistLength > 0) {
					player = MediaPlayer.create(BgMusic.this,
							playlist[PlayingNo]);
					if (isOn)
						player.start();
				} else {
					player = MediaPlayer.create(
							BgMusic.this,
							Uri.parse("android.resource://" + getPackageName()
									+ "/" + R.raw.bg));
				}

				player.setLooping(false);
				player.setOnCompletionListener(CompL);
				break;
			}
			super.handleMessage(msg);

		}
	}

}
