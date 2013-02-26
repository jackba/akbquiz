package babybear.akbquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RatingBar;
import android.widget.ToggleButton;

public class Chooser extends Activity {

	private static final String PreferenceName_Quiz = "quiz";
	private static final String KeyName_difficulty = "difficulty";

	private static boolean AKB48IsChoosed, SKE48IsChoosed, NMB48IsChoosed,
			HKT48IsChoosed, NGZK46IsChoosed, SDN48IsChoosed, JKT48IsChoosed,
			SNH48IsChoosed;
	private static int difficulty = 1;
	private static int MODE;

	private boolean isChanged = false;
	public void onCreate(Bundle InstanceState) {
		super.onCreate(InstanceState);
		setContentView(R.layout.chooser);

		initdata();
		initWidget();

	}

	private void initdata() {
		SharedPreferences sp_quiz = getSharedPreferences(PreferenceName_Quiz,
				Context.MODE_PRIVATE);

		difficulty = sp_quiz.getInt(KeyName_difficulty, 1);
		AKB48IsChoosed = sp_quiz.getBoolean(Database.GroupName_AKB48, false);
		SKE48IsChoosed = sp_quiz.getBoolean(Database.GroupName_SKE48, false);
		NMB48IsChoosed = sp_quiz.getBoolean(Database.GroupName_NMB48, false);
		HKT48IsChoosed = sp_quiz.getBoolean(Database.GroupName_HKT48, false);
		NGZK46IsChoosed = sp_quiz.getBoolean(Database.GroupName_NGZK46, false);
		SDN48IsChoosed = sp_quiz.getBoolean(Database.GroupName_SDN48, false);
		JKT48IsChoosed = sp_quiz.getBoolean(Database.GroupName_JKT48, false);
		SNH48IsChoosed = sp_quiz.getBoolean(Database.GroupName_SNH48, false);

		MODE = this.getIntent().getIntExtra(MainMenu.key_playmode,
				MainMenu.REQUEST_START_NORMAL);

	}

	private void initWidget() {

		ToggleButton akb = (ToggleButton) findViewById(R.id.akb_toggle);
		akb.setOnClickListener(l);
		akb.setChecked(AKB48IsChoosed);

		ToggleButton ske = (ToggleButton) findViewById(R.id.ske_toggle);
		ske.setOnClickListener(l);
		ske.setChecked(SKE48IsChoosed);

		ToggleButton nmb = (ToggleButton) findViewById(R.id.nmb_toggle);
		nmb.setOnClickListener(l);
		nmb.setChecked(NMB48IsChoosed);

		ToggleButton hkt = (ToggleButton) findViewById(R.id.hkt_toggle);
		hkt.setOnClickListener(l);
		hkt.setChecked(HKT48IsChoosed);

		ToggleButton ngzk = (ToggleButton) findViewById(R.id.ngzk_toggle);
		ngzk.setOnClickListener(l);
		ngzk.setChecked(NGZK46IsChoosed);

		// ToggleButton sdn = (ToggleButton) findViewById(R.id.sdn_toggle);
		// sdn.setOnClickListener(l);
		// setView(sdn,SDN48IsChoosed);
		//
		ToggleButton jkt = (ToggleButton) findViewById(R.id.jkt_toggle);
		jkt.setOnClickListener(l);
		jkt.setChecked(JKT48IsChoosed);

		ToggleButton snh = (ToggleButton) findViewById(R.id.snh_toggle);
		snh.setOnClickListener(l);
		snh.setChecked(SNH48IsChoosed);

		RatingBar diff = (RatingBar) findViewById(R.id.difficulty);
		diff.setRating(difficulty / 2.0f);
		diff.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar arg0, float rating,
					boolean fromUser) {
				if (!fromUser)
					return;
				difficulty = (int) (rating * 2);
				isChanged = true;

			}

		});

		findViewById(R.id.game_start).setOnClickListener(l);

	}

	


	private void save() {
		Editor e_quiz = getSharedPreferences(PreferenceName_Quiz,
				Context.MODE_PRIVATE).edit();
		e_quiz.putBoolean(Database.GroupName_AKB48, AKB48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_SKE48, SKE48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_NMB48, NMB48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_HKT48, HKT48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_NGZK46, NGZK46IsChoosed);
		e_quiz.putBoolean(Database.GroupName_SDN48, SDN48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_JKT48, JKT48IsChoosed);
		e_quiz.putBoolean(Database.GroupName_SNH48, SNH48IsChoosed);

		e_quiz.putInt(KeyName_difficulty, difficulty);

		e_quiz.commit();
	}

	OnClickListener l = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.akb_toggle:
				AKB48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.ske_toggle:
				SKE48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.nmb_toggle:
				NMB48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.hkt_toggle:
				HKT48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			// case R.id.snd_toggle:
			// SDN48IsChoosed = !SDN48IsChoosed;
			// setView(R.id.sdn_toggle, SDN48IsChoosed);
			// isChanged = true;
			// break;

			case R.id.ngzk_toggle:
				NGZK46IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.jkt_toggle:
				JKT48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.snh_toggle:
				SNH48IsChoosed = ((ToggleButton)v).isChecked();
				isChanged = true;
				break;

			case R.id.game_start:
				if (isChanged)
					save();
				
				Intent intent = new Intent(Chooser.this, Quiz.class);
				intent.putExtra(Database.GroupName_AKB48, AKB48IsChoosed);
				intent.putExtra(Database.GroupName_SKE48, SKE48IsChoosed);
				intent.putExtra(Database.GroupName_NMB48, NMB48IsChoosed);
				intent.putExtra(Database.GroupName_HKT48, HKT48IsChoosed);
				intent.putExtra(Database.GroupName_NGZK46, NGZK46IsChoosed);
				intent.putExtra(Database.GroupName_SDN48, SDN48IsChoosed);
				intent.putExtra(Database.GroupName_JKT48, JKT48IsChoosed);
				intent.putExtra(Database.GroupName_SNH48, SNH48IsChoosed);

				intent.putExtra(KeyName_difficulty, difficulty);

				intent.putExtra(MainMenu.key_playmode, MODE);

				startActivityForResult(intent, MODE);

				break;
			}
		}

	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Intent result = new Intent(data);
			setResult(RESULT_OK, result);
			finish();
		}
	}

	public boolean onKeydown(int keyCode, KeyEvent e) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isChanged)
				save();
			finish();
			break;
		}

		return super.onKeyDown(keyCode, e);
	}
	
	protected void onRestart() {
		super.onRestart();
	}

}
