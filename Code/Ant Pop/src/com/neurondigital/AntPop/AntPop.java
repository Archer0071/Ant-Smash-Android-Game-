package com.neurondigital.AntPop;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AntPop extends Activity {

	Surface view;
	WakeLock WL;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//TODO: --------------------------------------------------------------------------change advert unit id
		AdView ad = new AdView(this, AdSize.BANNER, "XXXXXXXXXXXXXXXXX");//enter advert id here
		RelativeLayout layout = new RelativeLayout(this);

		//layout
		view = new Surface(this, this);

		LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		ad.setLayoutParams(params1);

		layout.addView(view);
		layout.addView(ad);
		setContentView(layout);

		//TODO: ad-------------------------------------------------------------------------add testing devices if you wish
		AdRequest request = new AdRequest();
		request.addTestDevice("275D94C2B5B93B3C4014933E75F92565");///nexus7//////testing
		request.addTestDevice("91608B19766D984A3F929C31EC6AB947"); /////////////////testing//////////////////remove///////////
		request.addTestDevice("6316D285813B01C56412DAF4D3D80B40"); ///test htc sensesion xl
		request.addTestDevice("8C416F4CAF490509A1DA82E62168AE08");//asus transformer
		ad.loadAd(request);

		//wake-lock
		PowerManager PM = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WL = PM.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Graphics");
		WL.acquire();

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if (rotation == 0)
			view.default_lanscape = true;
		if (rotation == 180)
			view.default_lanscape = true;
		if (rotation == 90)
			view.default_lanscape = false;
		if (rotation == 270)
			view.default_lanscape = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			view.back();
			return false;
		}

		return false;
	}

	protected void onPause() {
		super.onPause();
		view.pause();
		WL.release();

	}

	protected void onResume() {
		super.onResume();
		view.resume();
		WL.acquire();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WL.release();

	}

}