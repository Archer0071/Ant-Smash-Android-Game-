package com.neurondigital.AntPop;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EnterHiscore extends Activity {

	Surface view;
	WakeLock WL;
	
	//score
	int[] hiscore = new int[10];
	String[] hiscorename = new String[10];
	int score;
	EditText namebox;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//set screen full screen and no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.enterhiscore);
		
		//get score
		score = getIntent().getIntExtra("score", 0);
		
		//declare views
		namebox = (EditText) findViewById(R.id.namebox);
		Button save = (Button) findViewById(R.id.save);
		
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				process_highscore(score, namebox.getText().toString());
				
			}
		});
		
	}
	
	public void loadscore() {
		// load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		for (int i = 0; i < 10; i++) {
			hiscore[i] = hiscores.getInt("score" + i, 0);
			hiscorename[i] = hiscores.getString("name" + i, "---");
		}

	}

	public void savescore() {
		//load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor hiscores_editor = hiscores.edit();
		for (int i = 0; i < 10; i++) {
			hiscores_editor.putInt("score" + i, hiscore[i]);
			hiscores_editor.putString("name" + i, hiscorename[i]);
		}
		hiscores_editor.commit();
		loadscore();
	}
	
	public void process_highscore(int score, String name){
		System.out.println("ll"+score + name);
		loadscore();
		boolean ready = false;
		if (score > hiscore[0]) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];
			hiscore[5] = hiscore[4];
			hiscorename[5] = hiscorename[4];
			hiscore[4] = hiscore[3];
			hiscorename[4] = hiscorename[3];
			hiscore[3] = hiscore[2];
			hiscorename[3] = hiscorename[2];
			hiscore[2] = hiscore[1];
			hiscorename[2] = hiscorename[1];
			hiscore[1] = hiscore[0];
			hiscorename[1] = hiscorename[0];

			hiscore[0] = score;
			hiscorename[0] = name;
			ready = true;
		}

		if (score > hiscore[1] && score <= hiscore[0] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];
			hiscore[5] = hiscore[4];
			hiscorename[5] = hiscorename[4];
			hiscore[4] = hiscore[3];
			hiscorename[4] = hiscorename[3];
			hiscore[3] = hiscore[2];
			hiscorename[3] = hiscorename[2];
			hiscore[2] = hiscore[1];
			hiscorename[2] = hiscorename[1];

			hiscore[1] = score;
			hiscorename[1] = name;
			ready = true;
		}
		if (score > hiscore[2] && score <= hiscore[1] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];
			hiscore[5] = hiscore[4];
			hiscorename[5] = hiscorename[4];
			hiscore[4] = hiscore[3];
			hiscorename[4] = hiscorename[3];
			hiscore[3] = hiscore[2];
			hiscorename[3] = hiscorename[2];

			hiscore[2] = score;
			hiscorename[2] = name;
			ready = true;
		}
		if (score > hiscore[3] && score <= hiscore[2] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];
			hiscore[5] = hiscore[4];
			hiscorename[5] = hiscorename[4];
			hiscore[4] = hiscore[3];
			hiscorename[4] = hiscorename[3];

			hiscore[3] = score;
			hiscorename[3] = name;
			ready = true;
		}
		if (score > hiscore[4] && score <= hiscore[3] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];
			hiscore[5] = hiscore[4];
			hiscorename[5] = hiscorename[4];

			hiscore[4] = score;
			hiscorename[4] = name;
			ready = true;
		}
		if (score > hiscore[5] && score <= hiscore[4] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];
			hiscore[6] = hiscore[5];
			hiscorename[6] = hiscorename[5];

			hiscore[5] = score;
			hiscorename[5] = name;
			ready = true;
		}
		if (score > hiscore[6] && score <= hiscore[5] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];
			hiscore[7] = hiscore[6];
			hiscorename[7] = hiscorename[6];

			hiscore[6] = score;
			hiscorename[6] = name;
			ready = true;
		}
		if (score > hiscore[7] && score <= hiscore[6] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];
			hiscore[8] = hiscore[7];
			hiscorename[8] = hiscorename[7];

			hiscore[7] = score;
			hiscorename[7] = name;
			ready = true;
		}
		if (score > hiscore[8] && score <= hiscore[7] && !ready) {

			hiscore[9] = hiscore[8];
			hiscorename[9] = hiscorename[8];

			hiscore[8] = score;
			hiscorename[8] = name;
			ready = true;
		}
		if (score > hiscore[9] && score <= hiscore[8] && !ready) {

			hiscore[9] = score;
			hiscorename[9] = name;
			ready = true;
		}

		savescore();
		//go back to hiscores
		finish();
	}





}