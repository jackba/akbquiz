package babybear.akbquiz;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ViewFlipper;

public class Config extends Activity{
	int vol_bg,vol_sound,onModifing=10;
	boolean switch_bg,switch_sound,switch_vibration,isPlaylistChanged=false;
	Cursor musics = null;
	String playlistStr="";
	ListView playlistView,musiclistView;
	ListAdapter playlistAdapter ,musiclistAdapter;
	//CursorAdapter musiclistAdapter;
	List <Music> playlist,musiclist;
	ViewFlipper cfgflipper;
	TextView musiclistHint;
	
	Music defaultbg = new Music();
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		//intent.getBooleanExtra(name, defaultValue)
		
		defaultbg._ID=-1;
		defaultbg.ALBUM="ここにいたこと";
		defaultbg.ARTIST="AKB48";
		defaultbg.DATA="android.resource://"+ getPackageName() +"/"+ R.raw.bg;
		defaultbg.DURATION=273100;
		defaultbg.TITLE="少女たちよ(默认背景音乐)";
		defaultbg.isExist=true;
		
		
		switch_bg=bundle.getBoolean(Database.ColName_switch_bg);
		switch_sound=bundle.getBoolean(Database.ColName_switch_sound);
		switch_vibration=bundle.getBoolean(Database.ColName_switch_vibration);
		vol_bg=bundle.getInt(Database.ColName_vol_bg);
		vol_sound=bundle.getInt(Database.ColName_vol_sound);
		playlistStr=bundle.getString(Database.ColName_extend);
		
		cfgflipper=(ViewFlipper)findViewById(R.id.cfg_flipper);
		
		init();
	}
	public void onStop(){
		super.onStop();
	}
	
	public void onDestroy(){
		super.onDestroy();
		if(musics!=null)musics.close();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			if(cfgflipper.getDisplayedChild()==0){;
				Intent intent = new Intent();
				intent.putExtra( Database.ColName_switch_bg , switch_bg);
				intent.putExtra( Database.ColName_switch_sound , switch_sound);
				intent.putExtra( Database.ColName_switch_vibration , switch_vibration);
				intent.putExtra( Database.ColName_vol_bg , vol_bg);
				intent.putExtra( Database.ColName_vol_sound , vol_sound);
				if(isPlaylistChanged){
					encodePlaylist();
					intent.putExtra( Database.ColName_extend , playlistStr);
					Message msg = new Message();
					msg.setData(intent.getExtras());
					msg.arg1=BgMusic.BGHandler.PLAYLIST_CHANGE;
					BgMusic.bgHandler.sendMessage(msg);
				}
				setResult(RESULT_OK, intent);
				
				finish();
				
			}else{
				cfgflipper.showPrevious();
				return false;
			}
			
			break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	void init(){
		ToggleButton bgm_toggle = (ToggleButton)findViewById(R.id.bgm_switch);
		ToggleButton sound_toggle = (ToggleButton)findViewById(R.id.sound_switch);
		ToggleButton vibration_toggle = (ToggleButton)findViewById(R.id.config_vibration_switch);
		SeekBar bgm_vol=(SeekBar)findViewById(R.id.bgm_volume);
		SeekBar sound_vol=(SeekBar)findViewById(R.id.sound_volume);
		Button  config_playlist= (Button)findViewById(R.id.config_playlist);

		
		bgm_toggle.setChecked(switch_bg);
		sound_toggle.setChecked(switch_sound);
		vibration_toggle.setChecked(switch_vibration);
		bgm_vol.setProgress(vol_bg);
		sound_vol.setProgress(vol_sound);
		
		
		OnClickListener l_switch = new OnClickListener(){

			@Override
			public void onClick(View v) {
				switch(v.getId()){
				case R.id.bgm_switch:
					switch_bg=((ToggleButton)v).isChecked();
					Message msg = new Message();
					msg.what=switch_bg?1:0;
					msg.arg1=BgMusic.BGHandler.SWITCH_CHANGE;
					BgMusic.bgHandler.sendMessage(msg);
					break;
				case R.id.sound_switch:
					switch_sound=((ToggleButton)v).isChecked();
					MainMenu.se.setSwitch(switch_sound);
					break;
				case R.id.config_vibration_switch:
					switch_vibration = ((ToggleButton)v).isChecked();
					break;
				case R.id.config_playlist:
					if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
						if(!isPlaylistChanged)loadPlaylistEditor();
						cfgflipper.showNext();
						
					}else{
						Toast.makeText(Config.this, "SD卡不可用", Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
			
		};
		
		
		bgm_toggle.setOnClickListener(l_switch);
		sound_toggle.setOnClickListener(l_switch);
		vibration_toggle.setOnClickListener(l_switch);
		config_playlist.setOnClickListener(l_switch);
		
		OnSeekBarChangeListener l_seekbar =new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				switch(arg0.getId()){
				case R.id.bgm_volume:
					vol_bg=arg1;
					Message msg = new Message();
					msg.what=vol_bg;
					msg.arg1=BgMusic.BGHandler.VOL_CHANGE;
					BgMusic.bgHandler.sendMessage(msg);
					break;
				case R.id.sound_volume:
					vol_sound=arg1;
					
					MainMenu.se.setVolume(vol_sound);
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

	void loadPlaylistEditor(){
		queryMusics();
		
		musiclistHint=(TextView)findViewById(R.id.current);
		
		playlist=new ArrayList <Music>();
		musiclist=new ArrayList <Music>();
		decodePlaylist();
		
		playlistAdapter= new PlaylistAdapter(this, playlist,new OnClickListener(){
			@Override
			public void onClick(View v) {
				int position = Integer.decode((String) v.getTag());
				//Toast.makeText(Config.this, "a click on : "+position, Toast.LENGTH_SHORT).show();
				onModifing=position;
				cfgflipper.showNext();
				musiclistHint.setText("修改播放列表第("+(onModifing+1)+")首 \""+playlist.get(onModifing).TITLE+" \"为:");
			}
		});
		playlistView=(ListView)findViewById(R.id.playlist);
		playlistView.setAdapter(playlistAdapter);
		
		
		musiclist.add(defaultbg);
		
		musics.moveToFirst();
		while(!musics.isAfterLast()){
			Music temp = new Music();
			temp._ID=musics.getInt(musics.getColumnIndex(MediaStore.Audio.Media._ID));
			temp.ALBUM=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			temp.ARTIST=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			temp.DATA=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA));
			temp.DURATION=musics.getLong(musics.getColumnIndex(MediaStore.Audio.Media.DURATION));
			temp.TITLE=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.TITLE));
			temp.isExist=true;
			
			musiclist.add(temp);
			musics.moveToNext();
		}
		
		musiclistAdapter = new PlaylistAdapter(Config.this, musiclist,new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				int position = Integer.parseInt((String) arg0.getTag())-1;
				Log.d("", "set "+ onModifing +" in Playlist with "+position+" in Musiclist");
				Music temp=new Music();
				if(position==-1){
					temp=defaultbg;
				}else{
					musics.moveToPosition(position);
					temp._ID=musics.getInt(musics.getColumnIndex(MediaStore.Audio.Media._ID));
					temp.ALBUM=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ALBUM));
					temp.ARTIST=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					temp.DATA=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA));
					temp.TITLE=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.TITLE));
					temp.DURATION=musics.getLong(musics.getColumnIndex(MediaStore.Audio.Media.DURATION));
					
					
				}
				//playlistAdapter.;
				playlist.set(onModifing, temp);
				Log.d("",playlist.get(onModifing).TITLE);
				PlaylistAdapter adapter=(PlaylistAdapter)playlistView.getAdapter();
				adapter.remove(adapter.getItem(onModifing));
				adapter.insert(temp, onModifing);
				encodePlaylist();
				
				isPlaylistChanged=true;
				cfgflipper.showPrevious();
			}
			
		});
		musiclistView=(ListView)findViewById(R.id.musiclist);
		musiclistView.setAdapter(musiclistAdapter);	
		
	}
	
	void queryMusics(){
		ContentResolver cr =this.getContentResolver();
		musics=cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[]{
				MediaStore.Audio.Media._ID, //int
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.DATA, //String 
				MediaStore.Audio.Media.DISPLAY_NAME, //String
				MediaStore.Audio.Media.MIME_TYPE //String
		},
		MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "+MediaStore.Audio.Media.DURATION+" > 10000",
		null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		Log.d("", "musics : "+musics.getCount());
	}
	void decodePlaylist(){
		Log.d("", "in decodePlaylist() playlistStr is : "+playlistStr);
		String[] strs;
		if(playlistStr==null){
			strs=null;
			Log.d("", "strs = null");
		}else{
			strs=playlistStr.split("<->");
			Log.d("", "strs.length = "+strs.length);
		}
		
		
		for(int i=0;i<10;i++){
			Log.d("", " i = "+i); 
			Music temp = new Music();
			temp._ID=0;
			temp.ALBUM="";
			temp.ARTIST="";
			temp.DATA="";
			temp.DURATION=0;
			temp.TITLE="Empty";
			temp.isExist=false;
			if(strs!=null){
				
				if(i<strs.length){
					Log.d("",strs[i]); 
					if(!strs[i].equals("")){
						musics.moveToFirst();
						while(!musics.isAfterLast()){
							Log.d("", musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA))); 
							if(strs[i].equals(defaultbg.DATA)){
								temp=defaultbg;
							}
							if(strs[i].equals(musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA)))){
								Log.d("", " i = "+i + " get");  
								
								temp._ID=musics.getInt(musics.getColumnIndex(MediaStore.Audio.Media._ID));
								temp.ALBUM=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ALBUM));
								temp.ARTIST=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ARTIST));
								temp.DATA=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA));
								temp.DURATION=musics.getLong(musics.getColumnIndex(MediaStore.Audio.Media.DURATION));
								temp.TITLE=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.TITLE));
								temp.isExist=true;
								break;
							};
							musics.moveToNext();
						}
					}
				}
			}
			playlist.add(temp);
		}
		Log.d("", "playlist.size() = "+playlist.size());
	}
	void encodePlaylist(){
		String str ="";
		Music temp = null;
		for(int i=0;i<10;i++){
			temp = playlist.get(i);
			str+=(temp.DATA+"<->");
		}
		playlistStr=str;
		Log.d("", " in encodePlaylist() playlistStr = "+playlistStr);
	}
	class PlaylistAdapter extends ArrayAdapter <Music> {
		OnClickListener l;
		public PlaylistAdapter(Context context,	List<Music> objects ,OnClickListener listener) {
			super(context,0, objects);
			l=listener;
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view==null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_item, null);
            }
            Music m=getItem(position);
            
            ((TextView)view.findViewById(R.id.no)).setText((position+1)+".");
            ((TextView)view.findViewById(R.id.album)).setText(m.ALBUM);
            ((TextView)view.findViewById(R.id.artist)).setText(m.ARTIST);
            ((TextView)view.findViewById(R.id.title)).setText(m.TITLE);
            ((TextView)view.findViewById(R.id.length)).setText(m.DURATION/60000+":"+(m.DURATION/1000)%60);
            
            view.setFocusable(true);
            view.setClickable(true);
            view.setTag(""+position);
            view.setOnClickListener(l);
            return view;
        }
		
	}
//	
//	class MusiclistAdapter extends CursorAdapter{
//		LayoutInflater inflater;
//		OnClickListener musicList_oCL;
//		public MusiclistAdapter(Context context, Cursor c) {
//			super(context, c);
//			inflater = LayoutInflater.from(context);
//			musicList_oCL = new OnClickListener(){
//
//				@Override
//				public void onClick(View arg0) {
//					int position = Integer.parseInt((String) arg0.getTag());
//					Log.d("", "set "+ onModifing +" in Playlist with "+position+" in Musiclist");
//					musics.moveToPosition(position);
//					Music temp=new Music();
//					temp._ID=musics.getInt(musics.getColumnIndex(MediaStore.Audio.Media._ID));
//					temp.ALBUM=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//					temp.ARTIST=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//					temp.DATA=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.DATA));
//					temp.TITLE=musics.getString(musics.getColumnIndex(MediaStore.Audio.Media.TITLE));
//					temp.DURATION=musics.getLong(musics.getColumnIndex(MediaStore.Audio.Media.DURATION));
//					
//					playlist.set(onModifing, temp);
//					Log.d("",playlist.get(onModifing).TITLE);
//					//playlistAdapter.;
//					PlaylistAdapter adapter=(PlaylistAdapter)playlistView.getAdapter();
//					adapter.remove(adapter.getItem(onModifing));
//					adapter.insert(temp, onModifing);
//					encodePlaylist();
//					
//					isPlaylistChanged=true;
//					cfgflipper.showPrevious();
//				}
//				
//			};
//		}
//
//		@Override
//		public void bindView(View view, Context arg1, Cursor cur) {
//			view.findViewById(R.id.no).setVisibility(View.GONE);
//            ((TextView)view.findViewById(R.id.album)).setText(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
//            ((TextView)view.findViewById(R.id.artist)).setText(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
//            ((TextView)view.findViewById(R.id.title)).setText(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
//            long duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
//            ((TextView)view.findViewById(R.id.length)).setText(duration/60000+":"+(duration/1000)%60);
//            //Log.d("","on bindView()");
//		}
//
//		@Override
//		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//			//Log.d("","on newView()");
//			View v=inflater.inflate(R.layout.playlist_item, null);
//			v.setFocusable(true);
//			v.setClickable(true);
//			v.setTag(""+cursor.getPosition());
//			v.setOnClickListener(musicList_oCL);
//			return v;
//		}
//		
//	}
	class Music{
		int _ID;
		long DURATION;
		boolean isExist;
		String TITLE,ARTIST,ALBUM,DATA;
		
	}
}
