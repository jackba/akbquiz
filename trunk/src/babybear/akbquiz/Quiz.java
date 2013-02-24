package babybear.akbquiz;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class Quiz extends Activity {
	private static final String TAG = "Quiz";
	private static final long[] v_right = { 100, 100, 100, 100 }, v_wrong = {
			100, 300, 100, 500 };
	private static final String[] ColName = { Database.ColName_ANSWER,
			Database.ColName_WRONG1, Database.ColName_WRONG2,
			Database.ColName_WRONG3 };

	private int right_count = 0, wrong_count = 0, time_count = 0,
			quiz_index = 0;

	private int difficulty = 0;

	private boolean isVibratorOn = true;
	private Database db = null;
	private Button[] Buttons = new Button[4];
	private Animation rightAnim = null, wrongAnim = null;

	private TextView quiz_Question = null;
	private TextView quiz_Title = null;
	private PopupWindow Right = null, Wrong = null;

	private Vibrator vibrator;
	private Timer timer = new Timer(true);

	private ArrayList<ContentValues> quizList = null;
	private int correct_answer = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quiz);

		right_count = 0;
		wrong_count = 0;
		time_count = 0;

		db = new Database(this, Database.DBName_quiz);
		vibrator = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);

		Intent intent = getIntent();
		Log.d(TAG,
				"AKB "
						+ intent.getBooleanExtra(Database.GroupName_AKB48,
								false));

		SharedPreferences sp_cfg = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		isVibratorOn = sp_cfg.getBoolean(Database.ColName_switch_vibration,
				true);
		// isSoundOn=intent.getIntExtra(Database.ColName_switch_sound,
		// 1)==1?true:false;

		Buttons[0] = (Button) findViewById(R.id.button_A);
		Buttons[1] = (Button) findViewById(R.id.button_B);
		Buttons[2] = (Button) findViewById(R.id.button_C);
		Buttons[3] = (Button) findViewById(R.id.button_D);

		Buttons[0].setOnClickListener(l);
		Buttons[1].setOnClickListener(l);
		Buttons[2].setOnClickListener(l);
		Buttons[3].setOnClickListener(l);

		quiz_Question = (TextView) findViewById(R.id.quiz);

		quiz_Title = (TextView) findViewById(R.id.quiz_Title);

		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View rightView = mLayoutInflater.inflate(R.layout.right, null);

		Right = new PopupWindow(rightView);
		Right.setWidth(LayoutParams.FILL_PARENT);
		Right.setHeight(LayoutParams.FILL_PARENT);

		View wrongView = mLayoutInflater.inflate(R.layout.wrong, null);
		Wrong = new PopupWindow(wrongView);
		Wrong.setWidth(LayoutParams.FILL_PARENT);
		Wrong.setHeight(LayoutParams.FILL_PARENT);

		rightAnim = AnimationUtils.loadAnimation(this, R.anim.show_fade_out);
		// rightAnim.setStartOffset(300);
		rightAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {

				Right.dismiss();
				if (quiz_index >= quizList.size()) {
					summary();
				} else
					setQuiz();

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {

			}

		});

		View rightAnimObj = rightView.findViewById(R.id.anim_obj);
		rightAnimObj.setAnimation(rightAnim);

		wrongAnim = AnimationUtils.loadAnimation(this, R.anim.show_fade_out);
		// wrongAnim.setStartOffset(300);
		wrongAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				Wrong.dismiss();
				if (quiz_index >= quizList.size()) {
					summary();
				} else
					setQuiz();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {

			}
		});
		View wrongAnimObj = wrongView.findViewById(R.id.anim_obj);
		wrongAnimObj.setAnimation(wrongAnim);

		getQuiz();
		timer.scheduleAtFixedRate(t_timer, 1000, 1000);
	}

	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		// getQuiz();
		setQuiz();
	}

	public void onStop() {
		super.onStop();
		if (Right.isShowing())
			Right.dismiss();
		if (Wrong.isShowing())
			Wrong.dismiss();

	}

	public void onDestory() {
		super.onDestroy();

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	void getQuiz() {
		// int num = Level * 5 + 5;
		Bundle data = this.getIntent().getExtras();
		ArrayList<String> groups = new ArrayList<String>();
		if (data.getBoolean(Database.GroupName_AKB48)) {
			groups.add(Database.GroupName_AKB48);
		}
		if (data.getBoolean(Database.GroupName_SKE48)) {
			groups.add(Database.GroupName_SKE48);
		}
		if (data.getBoolean(Database.GroupName_NMB48)) {
			groups.add(Database.GroupName_NMB48);
		}
		if (data.getBoolean(Database.GroupName_HKT48)) {
			groups.add(Database.GroupName_HKT48);
		}
		if (data.getBoolean(Database.GroupName_NGZK46)) {
			groups.add(Database.GroupName_NGZK46);
		}
		if (data.getBoolean(Database.GroupName_SDN48)) {
			groups.add(Database.GroupName_SDN48);
		}
		if (data.getBoolean(Database.GroupName_JKT48)) {
			groups.add(Database.GroupName_JKT48);
		}
		if (data.getBoolean(Database.GroupName_SNH48)) {
			groups.add(Database.GroupName_SNH48);
		}
		// quizList = db.QuizQuery(data.getInt(Database.ColName_DIFFICULTY, 1),
		// groups.toArray(new String[0]));
		String[] group = groups.toArray(new String[0]);

		quizList = db.QuizQuery(group);
		
		
		// Log.d("Quiz", "get "+quizList.size()+" rows");

		quiz_index = 0;
	}

	void setQuiz() {

		if (quizList == null || quizList.size() == 0) {
			Toast.makeText(this, "十分抱歉，题库中没有您要的题目", Toast.LENGTH_SHORT).show();
			return;
		}

		Log.d(TAG, "setQuiz()");
		Random r = new Random();

		// correct_answer=r.nextInt(4);

		int temp;
		boolean flag = true;

		Log.d(TAG, "correct_answer = " + correct_answer);

		ContentValues a_quiz = quizList.get(quiz_index);
		quiz_Question.setText("ID. " + a_quiz.getAsInteger(Database.ColName_id)
				+ "\n  " + a_quiz.getAsString(Database.ColName_QUESTION));
		// quiz_Title.setText("" + (quiz_index + 1) + "  出题者:"
		// + a_quiz.getAsString(Database.ColName_EDITOR));

		int[] answer_index = new int[4];
		for (int i = 0; i < 4; i++) {
			flag = false;
			do {
				temp = r.nextInt(4);
				for (int j = 0; j < i; j++) {
					if (temp == answer_index[j]) {
						flag = true;
						break;
					}
					flag = false;
				}
			} while (flag);
			if (temp == 0)
				correct_answer = i;
			answer_index[i] = temp;
			Buttons[i].setText(a_quiz.getAsString(ColName[temp]));
			// Log.d(TAG,"answer_index["+i+"] = "+answer_index[i]);
		}
	}

	void check(int answer) {
		Log.d(TAG, "answer = " + answer);
		quiz_index++;
		if (answer == correct_answer) {
			Right.showAtLocation(findViewById(R.id.full), Gravity.CENTER, 0, 0);
			rightAnim.startNow();
			if (isVibratorOn)
				vibrator.vibrate(v_right, -1);
			MainMenu.se.play(MainMenu.se.sound_right);
			right_count++;
			Log.d(TAG, "Right");
		} else {
			Wrong.showAtLocation(findViewById(R.id.full), Gravity.CENTER, 0, 0);
			wrongAnim.startNow();
			if (isVibratorOn)
				vibrator.vibrate(v_wrong, -1);
			MainMenu.se.play(MainMenu.se.sound_wrong);
			wrong_count++;
			Log.d(TAG, "Wrong");

		}
	}

	void summary() {
		timer.cancel();
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.summary,
				null);

		TextView right = (TextView) layout.findViewById(R.id.summary_right);
		right.setText("" + right_count);
		TextView wrong = (TextView) layout.findViewById(R.id.summary_wrong);
		wrong.setText("" + wrong_count);

		// float sum_rate=;
		TextView rate = (TextView) layout.findViewById(R.id.summary_rate);
		rate.setText((float) right_count / (right_count + wrong_count) * 100
				+ "%");
		TextView sum = (TextView) layout.findViewById(R.id.summary_sum);
		sum.setText("" + (right_count + wrong_count));
		TextView time = (TextView) layout.findViewById(R.id.summary_time);
		time.setText("" + time_count);

		AlertDialog.Builder summary_bulider = new AlertDialog.Builder(this);
		summary_bulider
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent();
								intent.putExtra("right", right_count);
								intent.putExtra("wrong", wrong_count);
								intent.putExtra("time", time_count);
								setResult(RESULT_OK, intent);
								dialog.dismiss();
								finish();
							}
						}).setTitle("统计信息").setView(layout).create().show();

	}

	OnClickListener l = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// Log.d(TAG, ""+arg0);\\\
			rightAnim.reset();
			wrongAnim.reset();
			switch (arg0.getId()) {
			case R.id.button_A:
				check(0);
				break;
			case R.id.button_B:
				check(1);
				break;
			case R.id.button_C:
				check(2);
				break;
			case R.id.button_D:
				check(3);
			}
		}

	};

	private TimerTask t_timer = new TimerTask() {
		@Override
		public void run() {
			time_count++;
			// Message msg= new Message();
			// msg.what=1;
			// h_timer.sendMessage(msg);
		}
	};
}
