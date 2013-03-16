package babybear.akbquiz;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class Database {
	static final String DBName_quiz = "AKBQuiz.db", DBName_cfg = "config.db",
			TabName_user = "user", TabName_quiz = "quiz";

	static final int ColIndex_ID = 0, ColIndex_EDITOR = 1,
			ColIndex_QUESTION = 2, ColIndex_ANSWER = 3, ColIndex_WRONG1 = 4,
			ColIndex_WRONG2 = 5, ColIndex_WRONG3 = 6, ColIndex_DIFFICULTY = 7;
	// ColIndex_username = 1;

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
			ColName_user_identity = "user_identity",
			ColName_switch_bg = "switch_bg", ColName_vol_bg = "vol_bg",
			ColName_switch_sound = "switch_sound",
			ColName_vol_sound = "vol_sound",
			ColName_switch_vibration = "switch_vibration",
			ColName_extend = "extend", ColName_playlist = "playlist",
			ColName_createTime = "createTime",
			ColName_counter_correct = "counter_correct",
			ColName_counter_wrong = "counter_wrong",
			ColName_time_played = "time_played", ColName_EDITOR = "editor",
			ColName_QUESTION = "question", ColName_ANSWER = "answer",
			ColName_WRONG1 = "wrong_1", ColName_WRONG2 = "wrong_2",
			ColName_WRONG3 = "wrong_3", ColName_DIFFICULTY = "difficulty";
	
	static final String IDTag_weibo="weibo_";

	static final int userdb_ver = 2;
	static final int quizdb_ver = 1;
	// static SQLiteDatabase Database;

	private String DBname = null;
	private Context context = null;
	private DatabaseHelper dbh = null;
	private SQLiteDatabase db = null;

	// Hash a = null;
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
			}

		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);

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
	 * 完全随机的获取n个题目
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
	 * 通过难度和团体获取 20个题目
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

	public ArrayList<ContentValues> QuizQuery(String[] groups) {
		if (!DBname.equals(DBName_quiz))
			return null;

		if (groups.length == 0)
			return QuizQuery(20);

		dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = dbh.getWritableDatabase();

		String selection = "";
		// String [] selectionArgs = new String [groups.length];
		for (int i = 0; i < groups.length; i++) {
			if (i > 0)
				selection += " OR ";
			selection += groups[i] + "=1";
		}

		Log.d("Database", "selection is : " + selection);
		Cursor cur = db.query(TabName_quiz, null, selection, null, null, null,
				"random()", "0,20");

		ArrayList<ContentValues> quizlist;

		if (cur.getCount() > 0) {
			quizlist = new ArrayList<ContentValues>();
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, quiz);
					quizlist.add(quiz);
				} while (cur.moveToNext());
			} else {
				quizlist = null;
			}
		} else {
			quizlist = null;
		}

		cur.close();
		db.close();
		dbh.close();
		return quizlist;
	};
	
	public int addQuiz(){
		//TODO 添加问题
		return 0;
		
	}
	
}
