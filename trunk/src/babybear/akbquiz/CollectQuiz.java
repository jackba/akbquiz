package babybear.akbquiz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CollectQuiz extends Activity {
	private QuizContainder quiz, lastquiz;

	Handler handler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.collect_quiz);

	}

	public void onStart() {
		super.onStart();
		handler = new Handler();
		init();
	}

	private void init() {

		lastquiz = quiz = new QuizContainder();

		// OnEditorActionListener eal = new OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2)
		// {
		// if(arg1!=EditorInfo.IME_ACTION_NEXT)return false;
		// switch (arg0.getId()) {
		// case R.id.question:
		// break;
		// case R.id.editor:
		// break;
		// case R.id.answer:
		// break;
		// case R.id.wrong1:
		// break;
		// case R.id.wrong2:
		// break;
		// case R.id.wrong3:
		// break;
		// }
		//
		// return false;
		// }
		//
		// };
/*
		OnFocusChangeListener fcl = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1)
					return;
				switch (arg0.getId()) {
				case R.id.question:
					quiz.question = ((EditText) arg0).getText().toString();
					break;
				case R.id.editor:
					quiz.editor = ((EditText) arg0).getText().toString();
					break;
				case R.id.answer:
					quiz.options[0] = ((EditText) arg0).getText().toString();
					break;
				case R.id.wrong1:
					quiz.options[1] = ((EditText) arg0).getText().toString();
					break;
				case R.id.wrong2:
					quiz.options[2] = ((EditText) arg0).getText().toString();
					break;
				case R.id.wrong3:
					quiz.options[3] = ((EditText) arg0).getText().toString();
					break;
				}
			}

		};

		EditText editor = (EditText) findViewById(R.id.editor);
		// editor.setOnEditorActionListener(eal);
		editor.setOnFocusChangeListener(fcl);
		

		EditText question = (EditText) findViewById(R.id.question);
		// question.setOnEditorActionListener(eal);
		question.setOnFocusChangeListener(fcl);

		EditText answer = (EditText) findViewById(R.id.answer);
		// answer.setOnEditorActionListener(eal);
		answer.setOnFocusChangeListener(fcl);

		EditText wrong1 = (EditText) findViewById(R.id.wrong1);
		// wrong1.setOnEditorActionListener(eal);
		wrong1.setOnFocusChangeListener(fcl);

		EditText wrong2 = (EditText) findViewById(R.id.wrong2);
		// wrong2.setOnEditorActionListener(eal);
		wrong2.setOnFocusChangeListener(fcl);

		EditText wrong3 = (EditText) findViewById(R.id.wrong3);
		// wrong3.setOnEditorActionListener(eal);
		wrong3.setOnFocusChangeListener(fcl);*/

		OnClickListener l = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.akb_toggle:
					quiz.akb = ((ToggleButton) v).isChecked();
					break;
				case R.id.ske_toggle:
					quiz.ske = ((ToggleButton) v).isChecked();
					break;
				case R.id.nmb_toggle:
					quiz.nmb = ((ToggleButton) v).isChecked();
					break;
				case R.id.jkt_toggle:
					quiz.jkt = ((ToggleButton) v).isChecked();
					break;
				case R.id.hkt_toggle:
					quiz.hkt = ((ToggleButton) v).isChecked();
					break;
				case R.id.ngzk_toggle:
					quiz.ngzk = ((ToggleButton) v).isChecked();
					break;
				// case R.id.sdn_toggle:
				// quiz.sdn=((ToggleButton)v).isChecked();
				// break;
				case R.id.snh_toggle:
					quiz.snh = ((ToggleButton) v).isChecked();
					break;

				case R.id.cancel:
					CollectQuiz.this.finish();

				case R.id.submit:
					
					quiz.question = ((EditText)findViewById(R.id.question)).getText().toString();
					quiz.editor = ((EditText) findViewById(R.id.editor)).getText().toString();
					quiz.options[0] = ((EditText) findViewById(R.id.answer)).getText().toString();
					quiz.options[1] = ((EditText) findViewById(R.id.wrong1)).getText().toString();
					quiz.options[2] = ((EditText) findViewById(R.id.wrong2)).getText().toString();
					quiz.options[3] = ((EditText) findViewById(R.id.wrong3)).getText().toString();
					
					
					
					if (quiz == lastquiz
							|| QuizContainder.isNewQuiz(quiz, lastquiz)) 
						if (QuizContainder.isUseable(CollectQuiz.this, quiz)){
						new Thread() {
							public void run() {
								submit();
							}
						}.start();
					}
				}
			}
		};

		ToggleButton akb = (ToggleButton) findViewById(R.id.akb_toggle);
		akb.setOnClickListener(l);

		ToggleButton ske = (ToggleButton) findViewById(R.id.ske_toggle);
		ske.setOnClickListener(l);

		ToggleButton nmb = (ToggleButton) findViewById(R.id.nmb_toggle);
		nmb.setOnClickListener(l);

		ToggleButton hkt = (ToggleButton) findViewById(R.id.hkt_toggle);
		hkt.setOnClickListener(l);

		ToggleButton ngzk = (ToggleButton) findViewById(R.id.ngzk_toggle);
		ngzk.setOnClickListener(l);

		// ToggleButton sdn = (ToggleButton) findViewById(R.id.sdn_toggle);
		// sdn.setOnClickListener(l);
		// setView(sdn,SDN48IsChoosed);
		//
		ToggleButton jkt = (ToggleButton) findViewById(R.id.jkt_toggle);
		jkt.setOnClickListener(l);

		ToggleButton snh = (ToggleButton) findViewById(R.id.snh_toggle);
		snh.setOnClickListener(l);

		((Button) findViewById(R.id.submit)).setOnClickListener(l);
		((Button) findViewById(R.id.cancel)).setOnClickListener(l);

		RatingBar diff = (RatingBar) findViewById(R.id.difficulty);
		diff.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar arg0, float rating,
					boolean fromUser) {
				if (!fromUser)
					return;
				quiz.difficulty = (int) (rating);

			}

		});

	}

	private boolean submit() {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(
					"http://fanhuashe.sinaapp.com/quiz/quizcollect/addquiz.php");
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			String postContent = "editor=" + URLEncoder.encode(quiz.editor)
					+ "&group=" + quiz.getGroup() + "&difficulty="
					+ quiz.difficulty + "&question="
					+ URLEncoder.encode(quiz.question) + "&answer="
					+ URLEncoder.encode(quiz.options[0]) + "&wrong="
					+ quiz.getWrongs();// TODO
			
			Log.d("cq", "postContent="+postContent);
			dos.write(postContent.getBytes());
			dos.flush();
			dos.close();
			
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"utf-8"));

			final String line = br.readLine();
			if (line.equalsIgnoreCase("success")) {
				lastquiz = quiz;
				quiz = new QuizContainder();

				handler.post(new Runnable() {
				
					@Override
					public void run() {
					// TODO Auto-generated method stub
						Toast.makeText(CollectQuiz.this, "提交成功", Toast.LENGTH_SHORT).show();
					}
					
				});
			} else {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
					// TODO Auto-generated method stub
						Log.d("submit", "提交失败:"+line);
						Toast.makeText(CollectQuiz.this, "提交失败:"+line, Toast.LENGTH_LONG).show();
					}
					
				});

			}

			br.close();
			in.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	static class QuizContainder {
		public int difficulty = 0;
		public String editor;
		public String question;
		public String[] options = new String[4];
		public boolean akb, ske, nmb, hkt, sdn, snh, jkt, ngzk;

		static int LIMIT_TOP_EDITOR = 32;
		static int LIMIT_LOW_EDITOR = 2;
		static int LIMIT_TOP_QUESTION = 200;
		static int LIMIT_LOW_QUESTION = 7;
		static int LIMIT_TOP_ANSWER = 50;
		static int LIMIT_LOW_ANSWER = 2;

		public String getGroup() {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(akb);
			sb.append(",");
			sb.append(ske);
			sb.append(",");
			sb.append(nmb);
			sb.append(",");
			sb.append(hkt);
			sb.append(",");
			sb.append(ngzk);
			sb.append(",");
			sb.append(sdn);
			sb.append(",");
			sb.append(jkt);
			sb.append(",");
			sb.append(snh);
			sb.append("]");
			return sb.toString();
		}

		public String getWrongs() {
			StringBuffer sb = new StringBuffer();
			sb.append("[\"");
			sb.append(options[1]);
			sb.append("\",\"");
			sb.append(options[2]);
			sb.append("\",\"");
			sb.append(options[3]);
			sb.append("\"]");
			return sb.toString();
		}

		static boolean isNewQuiz(QuizContainder q1, QuizContainder q2) {
			if (q1.question.equalsIgnoreCase(q2.question))
				return false;
			return true;
		}

		static boolean isUseable(Context context, QuizContainder q1) {
			if (q1.difficulty <= 0 || q1.difficulty > 10) {
				Toast.makeText(context, "请设置难度", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (q1.editor.length() > LIMIT_TOP_EDITOR
					|| q1.editor.length() < LIMIT_LOW_EDITOR) {
				Toast.makeText(
						context,
						"作者名需要大于等于" + LIMIT_LOW_EDITOR + "且小于等于"
								+ LIMIT_TOP_EDITOR + "字", Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			if (q1.question.length() > LIMIT_TOP_QUESTION
					|| q1.question.length() < LIMIT_LOW_QUESTION) {
				Toast.makeText(
						context,
						"问题内容需要大于等于" + LIMIT_LOW_QUESTION + "且小于等于"
								+ LIMIT_TOP_QUESTION + "字", Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			for (int i = 0; i < 4; i++) {
				if (q1.options[i].length() > LIMIT_TOP_ANSWER
						|| q1.options[i].length() < LIMIT_LOW_ANSWER) {
					Toast.makeText(
							context,
							"选项内容需要大于等于" + LIMIT_LOW_ANSWER + "且小于等于"
									+ LIMIT_TOP_ANSWER + "字",
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			return true;
		}
	}
}