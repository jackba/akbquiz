package babybear.akbquiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class Database {
	public static final String databasePath = "/data/data/babybear.akbquiz/databases/AKBQuiz.db";
	static final String DBName_quiz = "AKBQuiz.db", DBName_cfg = "config.db",
			TabName_user = "user", TabName_quiz = "quiz",
			TabName_member = "member_info";

	static final int ColIndex_ID = 0, ColIndex_EDITOR = 1,
			ColIndex_QUESTION = 2, ColIndex_ANSWER = 3, ColIndex_WRONG1 = 4,
			ColIndex_WRONG2 = 5, ColIndex_WRONG3 = 6, ColIndex_DIFFICULTY = 7;

	static final int GroupOrder_NULL = -1, GroupOrder_AKB48 = 0,
			GroupOrder_SKE48 = 1, GroupOrder_NMB48 = 2, GroupOrder_HKT48 = 3,
			GroupOrder_NGZK46 = 6, GroupOrder_SDN48 = 7, GroupOrder_JKT48 = 8,
			GroupOrder_SNH48 = 9;

	static final String GroupName_AKB48 = "akb48", GroupName_SKE48 = "ske48",
			GroupName_NMB48 = "nmb48", GroupName_HKT48 = "hkt48",
			GroupName_NGZK46 = "ngzk46", GroupName_SDN48 = "sdn48",
			GroupName_JKT48 = "jkt48", GroupName_SNH48 = "snh48";

	static final String[] GroupNames = { GroupName_AKB48, GroupName_SKE48,
			GroupName_NMB48, GroupName_HKT48, "", "", GroupName_NGZK46,
			GroupName_SDN48, GroupName_JKT48, GroupName_SNH48 };

	static final String ColName_id = "_id", ColName_username = "username",
			ColName_user_identity = "user_identity", ColName_extend = "extend",
			ColName_playlist = "playlist", ColName_createTime = "createTime",
			ColName_counter_correct = "counter_correct",
			ColName_counter_wrong = "counter_wrong",
			ColName_time_played = "time_played", ColName_EDITOR = "editor",
			ColName_QUESTION = "question", ColName_ANSWER = "answer",
			ColName_WRONG1 = "wrong_1", ColName_WRONG2 = "wrong_2",
			ColName_WRONG3 = "wrong_3", ColName_DIFFICULTY = "difficulty";

	static final String KEY_switch_bg = "switch_bg", KEY_vol_bg = "vol_bg",
			KEY_switch_sound = "switch_sound", KEY_vol_sound = "vol_sound",
			KEY_switch_vibration = "switch_vibration",
			KEY_use_custom_background = "switch_vibration",
			KEY_bgm_loopmode = "bg_loopmode",
			KEY_calendar_id = "calendar_choosed",
			KEY_events_added = "events_added";

	static final String IDTag_weibo = "weibo_";

	static final int userdb_ver = 2;
	static final int quizdb_ver = 2;

	private final static int QuizType_Normal = 0;
	private final static int QuizType_Birthday = 1;
	private final static int QuizType_Team = 2;
	private final static int QuizType_Comefrom = 3;

	private final static int QUIZ_RAND_MAX = 100;
	private final static int QUIZ_DIVIDE_1 = 15;
	private final static int QUIZ_DIVIDE_2 = 30;
	private final static int QUIZ_DIVIDE_3 = 45;

	private String DBname = null;
	private Context context = null;
	private DatabaseHelper dbh = null;
	private SQLiteDatabase db = null;

	private String[] useableComefrom = null;
	private String[] useableBirthday = null;
	private String[] useableTeam = null;

	private SimpleDateFormat fmt = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	private SimpleDateFormat outputDateFormat = new SimpleDateFormat(
			"yyyy年 MM月 dd日", Locale.getDefault());

	/**
	 * 
	 * @param ctx
	 *            Context
	 * @param name
	 *            数据库名
	 */
	public Database(Context ctx, String name) {
		context = ctx;
		DBname = name;

	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (DBname.equals(Database.DBName_cfg)) {
				// CursorFactory factory = new CursorFactory();
				// db.create(factory);
				Date t = new Date();
				Toast.makeText(context, "建立用户数据库", Toast.LENGTH_SHORT).show();
				db.execSQL("CREATE TABLE user ("
						+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ ColName_username + " TEXT , " + ColName_user_identity
						+ " TEXT , " + ColName_extend + " INTEGER, "
						+ ColName_createTime + " DATETIME , "
						+ ColName_counter_correct + " INTEGER DEFAULT 0, "
						+ ColName_counter_wrong + " INTEGER DEFAULT 0, "
						+ ColName_time_played + " INTEGER DEFAULT 0)");

				ContentValues values = new ContentValues();
				values.put(ColName_username, "default");
				values.put(ColName_createTime, t.getTime());
				values.put(ColName_extend, 0);
				db.insert(TabName_user, null, values);
			} else if (DBname.equals(DBName_quiz)) {
				AssetManager am = context.getAssets();
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

						os.flush();
						os.close();
						is.close();

					} catch (FileNotFoundException e) {
						Toast.makeText(context, "数据库文件未找到,数据库升级失败",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					} catch (IOException e) {
						Toast.makeText(context, "无法建立数据文件,数据库升级失败",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (DBname.equals(DBName_cfg)) {
				switch (oldVersion) {
				case 1:
					String sql = "ALTER TABLE `user` ADD `"
							+ ColName_user_identity + "` TEXT ;";
					db.execSQL(sql);
					break;
				}
			} else if (DBname.equals(DBName_quiz)) {
				AssetManager am = context.getAssets();
				File fileout = new File(databasePath);
				fileout.delete();
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
						Toast.makeText(context, "数据库文件未找到,数据库升级失败",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					} catch (IOException e) {
						Toast.makeText(context, "无法建立数据文件,数据库升级失败",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				}
			}

		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			if (DBname.equals(DBName_quiz)) {
				Cursor cur = db.query("member_info",
						new String[] { "comefrom" },
						"`comefrom` IS NOT NULL AND `is_show`", null,
						"comefrom", null, null);
				useableComefrom = new String[cur.getCount()];
				if (cur.getCount() > 0) {
					int i = 0;
					while (cur.moveToNext()) {
						useableComefrom[i] = cur.getString(cur
								.getColumnIndex("comefrom"));
						i++;
					}
				}

				cur = db.query("member_info", new String[] { "birthday" },
						"`birthday` IS NOT NULL AND `is_show`", null,
						"birthday", null, null);
				useableBirthday = new String[cur.getCount()];
				if (cur.getCount() > 0) {
					int i = 0;
					while (cur.moveToNext()) {
						try {
							Date birth = fmt.parse(cur.getString(cur
									.getColumnIndex("birthday")));

							useableBirthday[i] = outputDateFormat.format(birth);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						i++;
					}
				}
				cur = db.query("member_info",
						new String[] { "`group`||' Team '||`team` AS `team`" },
						"`team` IS NOT NULL AND `is_show`", null, "team", null,
						null);
				useableTeam = new String[cur.getCount()];
				if (cur.getCount() > 0) {
					int i = 0;
					while (cur.moveToNext()) {
						useableTeam[i] = cur.getString(cur
								.getColumnIndex("team"));
						i++;
					}
				}
			}
		}
	}

	/**
	 * 向数据库中添加一个用户
	 * 
	 * @param username
	 *            用户名
	 */
	public long addUser(String username) {
		return addUser(username, "");
	}

	/**
	 * 
	 * @param username
	 * @param identity
	 * @return
	 */
	public long addUser(String username, String identity) {
		if (!DBname.equals(DBName_cfg))
			return -1;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Date t = new Date();
		// db.execSQL("INSERT INTO user (username , switch_bg , vol_bg , switch_sound , vol_sound , switch_vibration , extend , createTime ) VALUES ('"+username+"' , 1 , 10 , 1 , 10 , 1 , '' ,"+t.getTime()+")");
		ContentValues values = new ContentValues();
		values.put(ColName_username, username);
		values.put(ColName_user_identity, identity);
		values.put(ColName_createTime, t.getTime());

		long n = db.insert(TabName_user, null, values);
		Toast.makeText(context, "用户:'" + username + "'已建立", Toast.LENGTH_SHORT)
				.show();

		db.close();
		dbh.close();
		return n;
	}

	/**
	 * 从数据库中删除一个用户
	 * 
	 * @param _id
	 *            用户_id
	 */
	public void removeUser(int _id) {
		if (!DBname.equals(DBName_cfg))
			return;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		db.delete(TabName_user, "_id = " + _id, null);

		Toast.makeText(context, "用户_id:" + _id + " 已删除", Toast.LENGTH_SHORT)
				.show();
		db.close();
		dbh.close();
	};

	/**
	 * 从数据库中删除一个用户
	 * 
	 * @param username
	 *            用户名
	 */
	public void removeUser(String username) {
		if (!DBname.equals(DBName_cfg))
			return;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		db.delete(TabName_user, ColName_username + " = " + username, null);

		Toast.makeText(context, "用户:'" + username + "'已删除", Toast.LENGTH_SHORT)
				.show();
		db.close();
		dbh.close();
	}

	/**
	 * 更新用户信息
	 * 
	 * @param id
	 *            用户_id
	 * @param values
	 *            要更改的键值对
	 */
	public void updateInfo(int id, ContentValues values) {
		if (!DBname.equals(DBName_cfg))
			return;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();
		// values.getAsBoolean(key)
		db.update(TabName_user, values, "_id=" + id, null);

		db.close();
		dbh.close();
	}

	/**
	 * 设置当前用户
	 * 
	 * @param userId
	 *            用户_id
	 */
	public boolean setCurrentUser(int userId) {
		if (!DBname.equals(DBName_cfg))
			return false;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("extend", userId);
		int n = db.update(TabName_user, values, "_id=1", null);

		db.close();
		dbh.close();
		if (n < 1)
			return false;
		return true;
	}

	/**
	 * 自动获取当前用户的信息
	 * 
	 * @return 用户数据的键值对
	 */
	public int getCurrentUser() {
		if (!DBname.equals(DBName_cfg))
			return 0;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Cursor defaultUser = db.query(TabName_user, new String[] { "extend" },
				"_id=1", null, null, null, null);
		defaultUser.moveToFirst();
		int userid = defaultUser.getInt(0);
		// if (userid<1)return 0;
		// Cursor cur=db.query("user", null, "_id= '"+userid+"'", null, null,
		// null, null);
		//
		//
		// ContentValues cfgs;
		// if(cur.moveToFirst()){
		// cfgs = new ContentValues();
		// DatabaseUtils.cursorRowToContentValues(cur,cfgs);
		// cfgs.put(ColName_switch_sound, cur.getInt(2)==1?true:false);
		// cfgs.put(ColName_switch_bg, cur.getInt(4)==1?true:false);
		// cfgs.put(ColName_switch_vibration , cur.getInt(6)==1?true:false);
		// }else{
		// cfgs = null;
		// }
		// cur.close();

		defaultUser.close();
		db.close();
		dbh.close();
		return userid;
	}

	/**
	 * 获取所有用户列表
	 * 
	 * @return 用户列表 包括int _id ，Sting username 和一个 Boolean isChoosed
	 */
	public ArrayList<ContentValues> userListQuery() {
		if (!DBname.equals(DBName_cfg))
			return null;
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Cursor cur = db.query(TabName_user, null, "_id>1", null, null, null,
				null);

		ArrayList<ContentValues> userlist = new ArrayList<ContentValues>();
		if (cur.moveToFirst()) {
			do {
				ContentValues user = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, user);
				user.put("isChoosed", false);
				userlist.add(user);
			} while (cur.moveToNext());
		} else {
			userlist = null;
		}

		cur.close();
		db.close();
		dbh.close();
		return userlist;
	}

	/**
	 * 完全随机的获取n个题目 调试用 已弃用
	 * 
	 * @param n
	 *            题目数量
	 * @return 题目内容
	 */

	public ArrayList<ContentValues> QuizQuery(int n) {
		if (!DBname.equals(DBName_quiz))
			return null;
		dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = dbh.getWritableDatabase();

		Cursor cur = db.query(TabName_quiz, null, null, null, null, null,
				"random()", "0," + n);

		ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();
		if (cur.moveToFirst()) {
			do {
				ContentValues quiz = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, quiz);
				quizlist.add(quiz);
			} while (cur.moveToNext());
		} else {
			quizlist = null;
		}
		cur.close();
		db.close();
		dbh.close();
		return quizlist;
	}

	/**
	 * 通过难度和团体获取 20个题目 因难度不可用 还未启用
	 * 
	 * @param difficulty
	 *            难度
	 * @param groups
	 *            相关的团
	 * @return 题目内容
	 */
	public ArrayList<ContentValues> QuizQuery(int difficulty, String[] groups) {

		if (!DBname.equals(DBName_quiz))
			return null;
		dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = dbh.getWritableDatabase();

		String selection = "(";
		// String [] selectionArgs = new String [groups.length];
		for (int i = 0; i < groups.length; i++) {
			if (i > 0)
				selection += " OR ";
			selection += groups[i] + "=1";
		}
		selection += ") AND difficulty = " + difficulty;

		Log.d("Database", "selection is : " + selection);
		Cursor cur = db.query(TabName_quiz, null, selection, groups, null,
				null, "random()", "0,20");
		;

		ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();

		if (cur.moveToFirst()) {
			do {
				ContentValues quiz = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, quiz);
				quizlist.add(quiz);
			} while (cur.moveToNext());
		} else {
			quizlist = null;
		}

		Log.d("Database", "get " + quizlist.size() + " rows");

		cur.close();
		db.close();
		dbh.close();
		return quizlist;
	}

	/**
	 * 根据团体获取20个题目
	 * 
	 * @param groups
	 *            相关的团
	 * @return 题目内容
	 * @throws ParseException
	 */
	public ArrayList<ContentValues> QuizQuery(String[] groups) {
		if (!DBname.equals(DBName_quiz))
			return null;

		if (groups.length == 0)
			return QuizQuery(20);

		int[] quizCounts = this.getCountEachType(20);

		dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = dbh.getWritableDatabase();

		// 一般问题
		String selection = "";
		// String [] selectionArgs = new String [groups.length];
		for (int i = 0; i < groups.length; i++) {
			if (i > 0)
				selection += " OR ";
			selection += groups[i] + "=1";
		}
		Log.d("Database", "selection is : " + selection);

		Cursor cur = db.query(TabName_quiz, null, selection, null, null, null,
				"random()", "0," + quizCounts[QuizType_Normal]);

		ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();

		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, quiz);
					quizlist.add(quiz);
				} while (cur.moveToNext());
			}
		}

		// 生日
		selection = "`is_show` AND ( ";
		for (int i = 0; i < groups.length; i++) {
			if (i > 0)
				selection += " OR ";
			selection += "`group` like \"" + groups[i] + "\"";
		}
		selection += " )";
		cur = db.query(
				TabName_member,
				null,
				selection,
				null,
				null,
				null,
				"random()",
				"0,"
						+ (quizCounts[QuizType_Birthday]
								+ quizCounts[QuizType_Comefrom] + quizCounts[QuizType_Team]));
		int counter = 0;

		Random r = new Random();
		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					if (counter <= quizCounts[QuizType_Birthday]) {
						quiz.put(Database.ColName_QUESTION,
								cur.getString(cur.getColumnIndex("name"))
										+ "的生日是哪一天?");

						try {
							Date birth = fmt.parse(cur.getString(cur
									.getColumnIndex("birthday")));
							String birthday = outputDateFormat.format(birth);
							;
							quiz.put(Database.ColName_ANSWER, birthday);
							int i = 1;
							List<String> opt = new ArrayList<String>(
									Arrays.asList(useableBirthday));
							while (i < 4) {
								int t = r.nextInt(opt.size());
								if (opt.get(t).equals(birthday)) {
									opt.remove(t);
								} else {
									quiz.put("wrong_" + i, opt.get(t));
									opt.remove(t);
									i++;
								}
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}

					} else if (counter <= quizCounts[QuizType_Comefrom]
							+ quizCounts[QuizType_Birthday]) {
						quiz.put(Database.ColName_QUESTION,
								cur.getString(cur.getColumnIndex("name"))
										+ "是来自哪里的?");
						String comefrom = cur.getString(cur
								.getColumnIndex("comefrom"));
						quiz.put(Database.ColName_ANSWER, comefrom);
						int i = 1;
						ArrayList<String> opt = new ArrayList<String>(
								Arrays.asList(useableComefrom));
						while (i < 4) {
							int t = r.nextInt(opt.size());
							if (opt.get(t).equals(comefrom)) {
								opt.remove(t);
							} else {
								quiz.put("wrong_" + i, opt.get(t));
								opt.remove(t);
								i++;
							}
						}

					} else if (counter <= quizCounts[QuizType_Comefrom]
							+ quizCounts[QuizType_Birthday]
							+ quizCounts[QuizType_Team]) {
						quiz.put(Database.ColName_QUESTION,
								cur.getString(cur.getColumnIndex("name"))
										+ "是那个小队的?");
						String team = cur
								.getString(cur.getColumnIndex("group"))
								+ " Team "
								+ cur.getString(cur.getColumnIndex("team"));
						quiz.put(Database.ColName_ANSWER, team);
						int i = 1;
						ArrayList<String> opt = new ArrayList<String>(
								Arrays.asList(useableTeam));
						while (i < 4) {
							int t = r.nextInt(opt.size());
							if (opt.get(t).equals(team)) {
								opt.remove(t);
							} else {
								quiz.put("wrong_" + i, opt.get(t));
								opt.remove(t);
								i++;
							}
						}
					}

					quizlist.add(quiz);
					counter++;
				} while (cur.moveToNext());
			}
		}

		cur.close();
		db.close();
		dbh.close();
		return quizlist;
	};

	/**
	 * 查询成员信息
	 * 
	 * @return
	 */
	public ArrayList<ContentValues> infoQery() {
		if (!DBname.equals(DBName_quiz))
			return null;

		dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = dbh.getWritableDatabase();
		Cursor cur = db.query(TabName_member, new String[] { "`_id`", "`name`","`comefrom`",
				"`nickname`", "`group`", "`team`", "`birthday`" }, null, null, null,
				null, null);
		ArrayList<ContentValues> memberlist = new ArrayList<ContentValues>();

		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, quiz);
					memberlist.add(quiz);
				} while (cur.moveToNext());
			}
		}
		cur.close();
		db.close();
		dbh.close();
		return memberlist;
	}

	/**
	 * 获取各种类型题目的数量
	 * 
	 * @param totalNum
	 * @return
	 */
	private int[] getCountEachType(int totalNum) {
		int[] counts = { 0, 0, 0, 0, 0 };
		Random r = new Random();
		for (int i = 0; i < totalNum; i++) {
			int t = r.nextInt(QUIZ_RAND_MAX);
			if (t >= QUIZ_DIVIDE_3) {
				counts[QuizType_Normal]++;
			} else {
				if (t < QUIZ_DIVIDE_1) {
					counts[QuizType_Birthday]++;
				} else if (t < QUIZ_DIVIDE_2) {
					counts[QuizType_Comefrom]++;
				} else {
					counts[QuizType_Team]++;
				}
			}
		}
		return counts;

	}

}
