package babybear.akbquiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainMenu extends Activity {

	private static final String databasePath = "/data/data/babybear.akbquiz/databases/AKBQuiz.db";

	public static final int REQUEST_CONFIG = 0, REQUEST_START_NORMAL = 1,
			REQUEST_START_CHALLENGE = 2;

	public static final String key_playmode = "playmode";

	static Database db = null;
	static SoundEffectManager se;
	static ArrayList<ContentValues> userList = null;
	static int currentUserInList = 0;

	static String username = "";

	static int whichIsChoose = 0;
	static EditText T_username = null;
	static AlertDialog userCreator = null;

	static ListView userListView = null;
	static ArrayAdapter<ContentValues> userListAdapter = null;
	static ViewFlipper menu_flipper = null;

	int config_vol_bg, config_vol_sound;
	boolean config_sw_bg, config_sw_sound, config_sw_vir;

	SharedPreferences sp_cfg;

	void setCurrent(int currentUserId) {
		db.setCurrentUser(currentUserId);
		for (int i = 0; i < userList.size(); i++) {
			if (userList.get(i).getAsInteger(Database.ColName_id) == currentUserId)
				currentUserInList = i;
		}

		username = userList.get(currentUserInList).getAsString(
				Database.ColName_username);
		// ((TextView) findViewById(R.id.username)).setText(username);

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		db = new Database(this, Database.DBName_cfg);
		se = new SoundEffectManager(this);
		sp_cfg = getSharedPreferences("config", Context.MODE_PRIVATE);

		// config_vol_bg=sp_cfg.getInt(Database.ColName_vol_bg, 10);
		// config_vol_sound=sp_cfg.getInt(Database.ColName_vol_sound, 10);
		// config_sw_bg=sp_cfg.getBoolean(Database.ColName_switch_bg, true);
		// config_sw_sound=;
		// config_sw_vir=sp_cfg.getBoolean(Database.ColName_switch_vibration,
		// true);

		menu_flipper = (ViewFlipper) findViewById(R.id.menu_flipper);
		userListView = (ListView) findViewById(R.id.listview_user);

		se.setSwitch(sp_cfg.getBoolean(Database.ColName_switch_sound, true));

		refreshUserlist();
		if (userList != null)
			setCurrent(db.getCurrentUser());

		OnClickListener l = new OnClickListener() {

			@Override
			public void onClick(View v) {

				se.play(se.sound_click);
				switch (v.getId()) {
				case R.id.start:
					Intent intent = new Intent(MainMenu.this, Chooser.class);
					intent.putExtra(key_playmode, REQUEST_START_NORMAL);
					startActivityForResult(intent,
							MainMenu.REQUEST_START_NORMAL);
					break;
				case R.id.record:
					refreshRecord();
					showRecord();
					break;
				case R.id.users:
					showUserList();
					break;
				case R.id.config:
					Intent intent_cfg = new Intent(MainMenu.this,
							ConfigActivity.class);
					startActivityForResult(intent_cfg, REQUEST_CONFIG);
					break;
				case R.id.create_user:
					createUser(true);
					break;
				}
			}
		};

		((Button) findViewById(R.id.start)).setOnClickListener(l);
		((Button) findViewById(R.id.record)).setOnClickListener(l);
		((Button) findViewById(R.id.users)).setOnClickListener(l);
		((Button) findViewById(R.id.config)).setOnClickListener(l);
		((Button) findViewById(R.id.create_user)).setOnClickListener(l);

		Intent intentBgMusic = new Intent(this, BgMusic.class);

		if (userList == null) {
			// intentBgMusic.putExtra(Database.ColName_extend,"");
			intentBgMusic.putExtra(Database.ColName_vol_bg, 10);
			intentBgMusic.putExtra(Database.ColName_switch_bg, true);
		} else {

			intentBgMusic.putExtra(Database.ColName_playlist,
					sp_cfg.getString(Database.ColName_playlist, ""));
			intentBgMusic.putExtra(Database.ColName_vol_bg,
					sp_cfg.getInt(Database.ColName_vol_bg, 10));
			intentBgMusic.putExtra(Database.ColName_switch_bg,
					sp_cfg.getBoolean(Database.ColName_switch_bg, true));
		}
		startService(intentBgMusic);

	}

	public void onStart() {
		super.onStart();
		if (userList == null)
			firstRun();

	}

	protected void onRestart() {
		super.onRestart();

	}

	protected void onResume() {
		super.onResume();
		// Message msg = new Message();
		// msg.arg1=BgMusic.BGHandler.PLAY;
		// BgMusic.bgHandler.sendMessage(msg);
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		super.onStop();

		if (userCreator != null)
			if (userCreator.isShowing())
				userCreator.dismiss();

	}

	public void onDestory() {
		super.onDestroy();
		se.release();
		Intent intentBgMusic = new Intent(this, BgMusic.class);
		stopService(intentBgMusic);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ContentValues currUser = userList.get(currentUserInList);
		ContentValues userdata = new ContentValues();
		userdata.put(Database.ColName_id,
				currUser.getAsInteger(Database.ColName_id));

		switch (requestCode) {
		case REQUEST_START_NORMAL:
			if (resultCode == Activity.RESULT_OK) {
				int right = data.getIntExtra("right", 0)
						+ currUser
								.getAsInteger(Database.ColName_counter_correct);
				int wrong = data.getIntExtra("wrong", 0)
						+ currUser.getAsInteger(Database.ColName_counter_wrong);
				int time = data.getIntExtra("time", 0)
						+ currUser.getAsInteger(Database.ColName_time_played);

				userdata.put(Database.ColName_counter_correct, right);
				userdata.put(Database.ColName_counter_wrong, wrong);
				userdata.put(Database.ColName_time_played, time);
				db.updateInfo(userdata.getAsInteger(Database.ColName_id),
						userdata);
				userList.get(currentUserInList).putAll(userdata);
			}
			break;
		case REQUEST_CONFIG:
			// TODO

			break;
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (menu_flipper.getDisplayedChild() == 0) {
				Intent intentBgMusic = new Intent(this, BgMusic.class);
				stopService(intentBgMusic);
				finish();
			} else {
				menu_flipper.showPrevious();
				return false;
			}
			break;
		}

		return super.onKeyDown(keyCode, event);

	}

	/**
	 * 弹出一个AlartDialog创建一个用户
	 * 
	 * @param isCancelable
	 *            是否可以取消
	 */
	void createUser(boolean isCancelable) {
		OnClickListener l_userCreater = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (arg0.getId() == R.id.ok) {
					String userin_lower, user_lower;

					String t_username = T_username.getText().toString();
					if (t_username.equals("")) {
						Toast.makeText(MainMenu.this, "请输入一个用户名",
								Toast.LENGTH_SHORT).show();
						return;
					}
					Log.d("USER CREATER", "username typed in : " + t_username);

					// Log.d("USER CREATER","userList.getCount() : "+userList.size()
					// );
					if (userList != null) {
						for (int i = 0; i < userList.size(); i++) {
							userin_lower = t_username.toLowerCase(Locale
									.getDefault());
							user_lower = userList.get(i)
									.getAsString(Database.ColName_username)
									.toLowerCase(Locale.getDefault());
							Log.d("USER CREATER", "userin_lower = "
									+ userin_lower + " &&   user_lower = "
									+ user_lower);
							if (userin_lower.equals(user_lower)) {
								Toast.makeText(MainMenu.this, "已存在相同的用户名",
										Toast.LENGTH_SHORT).show();
								return;
							}
						}

					}

					int newUserId = (int) db.addUser(t_username);
					refreshUserlist();
					setCurrent(newUserId);

					se.setSwitch(true);
				}
				userCreator.cancel();

			}

		};

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.username, null);

		T_username = (EditText) layout.findViewById(R.id.username);
		Button B_ok = (Button) layout.findViewById(R.id.ok), B_cancel = (Button) layout
				.findViewById(R.id.cancel);
		B_ok.setOnClickListener(l_userCreater);
		B_cancel.setOnClickListener(l_userCreater);

		AlertDialog.Builder userCreatorBuilder = new AlertDialog.Builder(this);
		userCreator = userCreatorBuilder.setTitle("创建一个新用户")
				.setIcon(R.drawable.logo_48).setView(layout).create();
		userCreator.setCancelable(isCancelable);
		if (!isCancelable)
			B_cancel.setVisibility(View.GONE);
		userCreator.show();
	}

	void showUserList() {
		menu_flipper.showNext();
	}

	/**
	 * 显示当前用户的游戏记录
	 */
	void showRecord() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.record,
				null);

		TextView TV_username = (TextView) layout
				.findViewById(R.id.record_username);
		TV_username.setText(username);

		ContentValues userdata = userList.get(currentUserInList);
		int right = userdata.getAsInteger(Database.ColName_counter_correct);
		int wrong = userdata.getAsInteger(Database.ColName_counter_wrong);
		int sumtime = userdata.getAsInteger(Database.ColName_time_played);

		TextView TV_sum = (TextView) layout.findViewById(R.id.record_sum);
		TV_sum.setText("" + (right + wrong));

		TextView TV_right = (TextView) layout.findViewById(R.id.record_right);
		TV_right.setText("" + right);

		TextView TV_wrong = (TextView) layout.findViewById(R.id.record_wrong);
		TV_wrong.setText("" + wrong);

		TextView TV_accuracy = (TextView) layout
				.findViewById(R.id.record_accuracy);
		TV_accuracy.setText("" + ((float) right / (right + wrong)));

		TextView TV_sumtime = (TextView) layout
				.findViewById(R.id.record_sumtime);
		TV_sumtime.setText("" + sumtime);

		TextView TV_meantime = (TextView) layout
				.findViewById(R.id.record_meantime);
		TV_meantime.setText("" + ((float) sumtime / (right + wrong)));

		AlertDialog.Builder userCreatorBuilder = new AlertDialog.Builder(this);
		userCreatorBuilder.setView(layout)
				.setNegativeButton(android.R.string.ok, null).create().show();
	}

	/**
	 * 刷新userlist并刷新显示
	 */
	protected void refreshUserlist() {
		userList = db.userListQuery();
		if (userList == null)
			return;
		for (int i = 0; i < userList.size(); i++) {
			Log.d("refreshUserList",
					userList.get(i).getAsString(Database.ColName_username));
		}
		for (int i = 0; i < userList.size(); i++) {
			if (userList.get(i).getAsString(Database.ColName_username)
					.equals(username))
				userList.get(i).put("isChoosed", true);
			else
				userList.get(i).put("isChoosed", false);
		}

		userListAdapter = new UserlistAdapter(this, userList);
		userListView.setAdapter(userListAdapter);

		userListAdapter.notifyDataSetChanged();

		Log.d("U chooser", "userList.size() = " + userList.size());

	}

	/**
	 * 用户列表显示时用到的Adapter类
	 */
	class UserlistAdapter extends ArrayAdapter<ContentValues> {

		public UserlistAdapter(Context context, List<ContentValues> objects) {
			super(context, 0, objects);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(getContext()).inflate(
						R.layout.user_chooser_item, null);
			}

			ContentValues values = getItem(position);

			// if((TextView) v.findViewById(R.id.username) == null)Log.d("123",
			// "v.findViewById(R.id.username) == null");
			((TextView) v.findViewById(R.id.username)).setText(values
					.getAsString(Database.ColName_username));
			if (values.getAsBoolean("isChoosed"))
				v.setBackgroundColor(0xffa0a0a0);
			else
				v.setBackgroundColor(0xff000000);
			View del = v.findViewById(R.id.delete);
			del.setTag("" + position);
			del.setClickable(true);
			del.setOnClickListener(deleteListener);

			v.setTag("" + position);
			v.setClickable(true);
			v.setFocusable(true);
			v.setOnClickListener(listViewListener);

			Log.d("UserlistAdapter", "Is getting view in position : "
					+ position);
			Log.d("UserlistAdapter",
					"username : "
							+ values.getAsString(Database.ColName_username));
			Log.d("UserlistAdapter",
					"isChoosed : " + values.getAsBoolean("isChoosed"));
			return v;
		}
	};

	protected void refreshRecord() {
		// TODO Auto-generated method stub

	}

	/**
	 * 选择用户的操作
	 */
	OnClickListener listViewListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			int pos = Integer.parseInt((String) arg0.getTag());
			// ContentValues tuser = ;
			int _id = userList.get(pos).getAsInteger(Database.ColName_id);

			setCurrent(_id);

			// userdata=db.getCurrentUser();
			// username=userdata.getAsString(Database.ColName_username);
			// TV_username.setText(username);
			refreshUserlist();

		}

	};

	/**
	 * 点选删除的操作
	 */
	OnClickListener deleteListener = new OnClickListener() {
		int pos;
		int _id;
		boolean isCurrent = false;

		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					if (isCurrent) {
						if (pos + 1 >= userList.size()) {
							if (pos - 1 < 0) {
								Toast.makeText(MainMenu.this, "这是唯一的用户，不可删除",
										Toast.LENGTH_SHORT).show();
								return;
							} else {
								pos--;
							}
						} else {
							pos++;
						}
						setCurrent(userList.get(pos).getAsInteger(
								Database.ColName_id));
					}

					db.removeUser(_id);
					refreshUserlist();
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					break;
				}
			}

		};

		@Override
		public void onClick(View arg0) {
			pos = Integer.parseInt((String) arg0.getTag());
			ContentValues tuser = userList.get(pos);
			_id = tuser.getAsInteger(Database.ColName_id);
			String username = tuser.getAsString(Database.ColName_username);
			isCurrent = tuser.getAsBoolean("isChoosed");
			AlertDialog.Builder confirm = new AlertDialog.Builder(MainMenu.this);
			confirm.setTitle("确认删除");
			confirm.setMessage("确定要删除用户“" + username + "”吗？");
			confirm.setPositiveButton(android.R.string.ok, l);
			confirm.setNegativeButton(android.R.string.cancel, l);
			confirm.create().show();
		}

	};

	/**
	 * 首次运行
	 */
	void firstRun() {

		Editor cfg_Editor = sp_cfg.edit();
		cfg_Editor.putBoolean(Database.ColName_switch_bg, true);
		cfg_Editor.putBoolean(Database.ColName_switch_sound, true);
		cfg_Editor.putBoolean(Database.ColName_switch_vibration, true);
		cfg_Editor.putInt(Database.ColName_vol_bg, 10);
		cfg_Editor.putInt(Database.ColName_vol_sound, 10);
		cfg_Editor.putString(Database.ColName_playlist, "android.resource://"
				+ getPackageName() + "/" + R.raw.bg);
		cfg_Editor.commit();

		AssetManager am = getAssets();
		File fileout = new File(databasePath);
		if (!fileout.exists()) {
			try {
				InputStream is = am.open("q.db");

				fileout.createNewFile();
				FileOutputStream os = new FileOutputStream(fileout);

				int temp = 0;
				temp = is.read();
				while (temp != -1) {
					os.write(temp);
					temp = is.read();
				}
				;

				os.flush();
				os.close();
				is.close();

			} catch (FileNotFoundException e) {
				Toast.makeText(this, "数据文件未找到", Toast.LENGTH_SHORT).show();
				finish();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(this, "无法建立数据文件", Toast.LENGTH_SHORT).show();
				finish();
				e.printStackTrace();
			}
		}

		createUser(false);
	}

}