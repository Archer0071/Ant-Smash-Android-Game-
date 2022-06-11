package com.neurondigital.AntPop;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.*;

public class Surface extends SurfaceView implements Runnable, OnTouchListener, OnClickListener, OnLongClickListener, SensorEventListener {

	//game loop
	boolean isRunning = false, initialised = false;
	SurfaceHolder ourholder;
	Thread thread = null;
	int width = -1;
	int height = -1;
	Context context;
	Activity activity;
	long laststep;

	//sensor
	SensorManager sm;
	Sensor s;
	float sensorx, calibratex = 0;
	float sensory, calibratey = 0;
	boolean default_lanscape = false;

	//general images
	Bitmap play_img, settings_img, highscores_img, exit_img, home_img, selected_home_img, selected_play_img, selected_settings_img, selected_highscores_img, selected_exit_img;
	Bitmap pause_img, pause_play_img;
	Bitmap background_img;
	Bitmap sound_img, music_img, sound_mute_img, music_mute_img, instruction_img;
	Bitmap ball_img;

	//plane
	float ballx, bally, ball_speedx, ball_speedy;

	//obstacles
	ArrayList<Integer> obstaclex = new ArrayList<Integer>();
	ArrayList<Integer> obstacley = new ArrayList<Integer>();
	ArrayList<Integer> obstacletype = new ArrayList<Integer>();
	ArrayList<Integer> obstaclekilledtimer = new ArrayList<Integer>();

	//TODO:----------------------------------------------------------------------------------------remember to change text in strings.xml
	//TODO:----------------------------------------------------------------------------------------change ants and bugs (obstacles) types from here
	int[] obstacle_img_id = new int[] { R.drawable.ant1, R.drawable.ant2, R.drawable.bug1, R.drawable.bug2, R.drawable.bug3 };//obstacles image name
	int[] obstacle_killed_img_id = new int[] { R.drawable.ant1_killed, R.drawable.ant2_killed, R.drawable.bug1_killed, R.drawable.bug2_killed, R.drawable.bug3_killed };//obstacles killed image name
	final int ant1 = 0, ant2 = 1, bug1 = 2, bug2 = 3, bug3 = 4;//obstacle names
	int[] obstacle_points = new int[] { 2, 4, 5, 6, 7 };//obstacle points
	int[] obstacle_speed = new int[] { 2, 3, 4, 4, 5 };//obstacle speed
	float[] obstacle_size = new float[] { 0.05f, 0.05f, 0.07f, 0.09f, 0.1f };//obstacle size
	int[] obstacle_direction = new int[] { 2, 4, 3, 1, 2 };//obstacle entering direction.1:top, 2:right, 3:bottom, 4:left;

	gif[] obstacle_img = new gif[obstacle_img_id.length];//obstacle number
	Bitmap[] obstacle_killed_img = new Bitmap[obstacle_killed_img_id.length];//obstacle number

	//holes
	ArrayList<Float> holex = new ArrayList<Float>();
	ArrayList<Float> holey = new ArrayList<Float>();
	int laststarttime = -10;

	//game play
	int score = 0;
	int time = 0;
	final int menu = 0, play = 1, hiscores = 3, gameover = 4, settings = 5;
	int state = menu;
	boolean paused = false;
	boolean levelstarted = false, instructions = false;
	long now = SystemClock.uptimeMillis();
	long start;
	int countdown;
	int level = 0;
	int totaltime = 0, remaining_time = 0;

	//paints
	Paint scoretext = new Paint();
	Paint stroke_scoretext = new Paint();
	Paint logotext = new Paint();
	Paint titletext = new Paint();
	Paint stroke_titletext = new Paint();
	Paint menutext = new Paint();
	Paint gameplaytext = new Paint();
	Paint semitrasparentwhite = new Paint();
	Paint trasparentblack = new Paint();
	Paint background = new Paint();
	Paint menu_background = new Paint();
	Paint white = new Paint();
	Paint black = new Paint();
	Paint yellow_light = new Paint();
	Paint blue = new Paint();
	Paint green = new Paint();

	//score
	int[] hiscore = new int[10];
	String[] hiscorename = new String[10];

	//graphics
	boolean oncalibrate = false, onplay = false, onsettings = false, onexit = false, onhighscores = false, onmenu = false;

	//sound
	SoundPool sp;
	int sound_score, sound_beep;
	MediaPlayer music;
	boolean sound_muted = false, music_muted = false;

	public Surface(Context context, Activity activity) {
		super(context);
		//listeners
		setOnTouchListener(this);
		setOnClickListener(this);
		setOnLongClickListener(this);

		//sensors
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			s = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
		}

		//load images
		
		//TODO: -----------------------------------------------------------------------change image names if you added obstacles or changed image names
		//obstacle killed images
		for (int i = 0; i < obstacle_killed_img.length; i++) {
			obstacle_killed_img[i] = BitmapFactory.decodeResource(getResources(), obstacle_killed_img_id[i]);
		}

		//ball image
		ball_img = BitmapFactory.decodeResource(getResources(), R.drawable.ball);

		//menu images
		play_img = BitmapFactory.decodeResource(getResources(), R.drawable.play);
		settings_img = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		highscores_img = BitmapFactory.decodeResource(getResources(), R.drawable.highscores);
		exit_img = BitmapFactory.decodeResource(getResources(), R.drawable.exit);
		home_img = BitmapFactory.decodeResource(getResources(), R.drawable.home);
		pause_img = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
		pause_play_img = BitmapFactory.decodeResource(getResources(), R.drawable.pause_play);
		background_img = BitmapFactory.decodeResource(getResources(), R.drawable.wood_background);
		instruction_img = BitmapFactory.decodeResource(getResources(), R.drawable.instruction);

		//sound images
		sound_img = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
		sound_mute_img = BitmapFactory.decodeResource(getResources(), R.drawable.sound_mute);
		music_img = BitmapFactory.decodeResource(getResources(), R.drawable.music);
		music_mute_img = BitmapFactory.decodeResource(getResources(), R.drawable.music_mute);

		//sound
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		sound_score = sp.load(activity, R.raw.score, 1);
		sound_beep = sp.load(activity, R.raw.beep, 1);
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		music = MediaPlayer.create(activity, R.raw.music);

		//loop
		ourholder = getHolder();
		thread = new Thread(this);
		thread.start();

		//high score restart
		for (int i = 0; i < 10; i++) {
			hiscore[i] = 0;
			hiscorename[i] = "---";
		}

		this.context = context;
		this.activity = activity;

	}

	//level design
	public void handle_obstacles_and_holes() {
		int w = width;
		int h = height;
		//obstacle creator.
		//TODO:-----------------------------------------------------add obstacle generations. Change their timeline here...
		//eg: level_at_timeperiod(start time, end time, new int[] { put ant/bugs names here }, generation rate, new float[] { holes x }, new float[] { holes y });
		level_at_timeperiod(0, 500, new int[] { ant1 }, 50, new float[] { w * 0.4f }, new float[] { h * 0.4f });
		level_at_timeperiod(500, 1000, new int[] { ant1, ant1 }, 50, new float[] { w * 0.2f, w * 0.8f }, new float[] { h * 0.2f, h * 0.8f });
		level_at_timeperiod(1000, 1800, new int[] { ant1, ant2, ant1 }, 50, new float[] { w * 0.1f, w * 0.9f }, new float[] { h * 0.2f, h * 0.8f });
		level_at_timeperiod(1800, 2600, new int[] { ant2, ant2, ant2, bug1 }, 50, new float[] { w * 0.9f, w * 0.1f }, new float[] { h * 0.2f, h * 0.8f });
		level_at_timeperiod(2600, 3500, new int[] { ant2, ant1, bug1, bug2 }, 50, new float[] { w * 0.1f, w * 0.1f, w * 0.8f, w * 0.8f }, new float[] { h * 0.2f, h * 0.8f, h * 0.2f, h * 0.8f });
		level_at_timeperiod(3500, 4500, new int[] { ant2, ant2, ant1, bug2, bug2 }, 50, new float[] { w * 0.1f, w * 0.1f, w * 0.8f, w * 0.8f }, new float[] { h * 0.2f, h * 0.8f, h * 0.2f, h * 0.8f });
		level_at_timeperiod(4500, 5500, new int[] { ant2, bug1, ant1, bug2, bug2 }, 50, new float[] { w * 0.1f, w * 0.1f, w * 0.8f, w * 0.8f, w * 0.3f }, new float[] { h * 0.2f, h * 0.8f, h * 0.2f, h * 0.8f, h * 0.5f });
		level_at_timeperiod(5500, 6600, new int[] { bug1, bug2, bug2, bug3 }, 50, new float[] { w * 0.1f, w * 0.4f, w * 0.7f, w * 1f }, new float[] { h * 0.5f, h * 0.5f, h * 0.5f, h * 0.5f });
		level_at_timeperiod(6600, 8000, new int[] { bug1, bug2, bug2, bug3, ant1 }, 50, new float[] { w * 0.1f, w * 0.4f, w * 0.7f, w * 1f, w * 0.5f }, new float[] { h * 0.5f, h * 0.5f, h * 0.5f, h * 0.5f, h * 0.1f });
		level_at_timeperiod(8000, 99999, new int[] { bug1, bug2, bug3, bug3, ant1, ant2 }, 50, new float[] { w * 0.1f, w * 0.4f, w * 0.7f, w * 1f, w * 0.5f, w * 0.5f }, new float[] { h * 0.5f, h * 0.5f, h * 0.5f, h * 0.5f, h * 0.1f, h * 0.9f });
		level_at_timeperiod(99999, 999999, new int[] { bug1, bug2, bug2, bug3, bug3, bug3, ant1, ant2 }, 50, new float[] { w * 0.1f, w * 0.4f, w * 0.7f, w * 1f, w * 0.5f, w * 0.5f }, new float[] { h * 0.5f, h * 0.5f, h * 0.5f, h * 0.5f, h * 0.1f, h * 0.9f });

		//feel free to copy paste 'obstacles_at_timeperiod()' to generate obstacles on a timeframe of your own
	}

	//inputs///////////////////////////////////////////////////////////////////////////////////////////////////
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		//sleep for fps
		try {
			Thread.sleep(16);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		//read values

		if (default_lanscape) {
			sensorx = -event.values[0];
			sensory = event.values[1];
		} else {
			sensory = event.values[0];
			sensorx = event.values[1];
		}

	}

	public boolean onLongClick(View arg0) {
		return false;
	}

	public void onClick(View v) {
	}

	public boolean onTouch(View v, MotionEvent event) {
		//sleep for fps
		try {
			Thread.sleep(50);//20fps
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		float touchx = event.getX();
		float touchy = event.getY();

		//screen released
		if (event.getAction() == MotionEvent.ACTION_UP) {
			//release all keys
			oncalibrate = false;
			onplay = false;
			onsettings = false;
			onexit = false;
			onhighscores = false;
			onmenu = false;

			if (state == settings) {
				//calibrate
				if (intersect((width / 2) - (width / 4), (height / 2) - (width / 20), (width / 2), (width / 10), (int) touchx, (int) touchy)) {
					calibratex = sensorx;
					calibratey = sensory;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					state = menu;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
			} else if (state == hiscores) {
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					state = menu;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
			} else if (state == menu) {
				//play
				if (intersect((int) ((width / 2) - (play_img.getWidth() / 2)), (int) ((height / 2) - (play_img.getHeight() / 2)), play_img.getWidth(), play_img.getHeight(), (int) touchx, (int) touchy)) {
					for (int i = 0; i <= obstaclex.size() - 1; i++) {
						//delete all
						obstaclex.remove(i);
						obstacley.remove(i);
						obstacletype.remove(i);
						obstaclekilledtimer.remove(i);
					}

					levelstarted = true;
					instructions = true;
					score = 0;
					time = 0;
					level = 0;
					laststarttime = -10;

					//initialise ball position
					ballx = width / 2;
					bally = height / 2;
					ball_speedx = 0;
					ball_speedy = 0;

					//clear obstacles
					obstaclex.clear();
					obstacley.clear();
					obstacletype.clear();
					obstaclekilledtimer.clear();

					state = play;
					if (!music_muted) {
						music = MediaPlayer.create(activity, R.raw.music);
						music.start();
						music.setLooping(true);
					}
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);

				}
				//highscores
				if (intersect((int) ((width * 0.8f) - (highscores_img.getWidth() / 2)), (int) ((height / 2.1f) - (highscores_img.getHeight() / 2)), highscores_img.getWidth(), highscores_img.getHeight(), (int) touchx, (int) touchy)) {
					state = hiscores;
					loadscore();
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//settings
				if (intersect((int) ((width / 5) - (settings_img.getWidth() / 2)), (int) ((height / 2.2f) - (settings_img.getHeight() / 2)), settings_img.getWidth(), settings_img.getHeight(), (int) touchx, (int) touchy)) {
					state = settings;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//exit
				if (intersect((int) ((width * 0.9f) - (exit_img.getWidth() / 2)), (int) ((height * 0.9f) - (exit_img.getHeight() / 2)), exit_img.getWidth(), exit_img.getHeight(), (int) touchx, (int) touchy)) {
					System.exit(0);
					activity.finish();
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}

			} else if (state == play) {
				if (instructions) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
					instructions = false;
					start = SystemClock.uptimeMillis();
				} else {

					if (levelstarted) {
						if (intersect((width / 2) - (pause_img.getWidth() / 2), (int) (pause_img.getHeight() * 0.2f), pause_img.getWidth(), pause_img.getHeight(), (int) touchx, (int) touchy)) {
							if (paused) {
								paused = false;
								if (!music_muted) {
									music = MediaPlayer.create(activity, R.raw.music);
									music.start();
									music.setLooping(true);
								}
							} else {
								paused = true;
								music.stop();
							}
							if (sound_beep != 0 && !sound_muted)
								sp.play(sound_beep, 1, 1, 0, 0, 0);
						} else {
							if (paused) {
								if (sound_beep != 0 && !sound_muted)
									sp.play(sound_beep, 1, 1, 0, 0, 0);
								paused = false;
								if (!music_muted) {
									music = MediaPlayer.create(activity, R.raw.music);
									music.start();
									music.setLooping(true);
								}
							}
						}
					}
				}

			} else if (state == gameover) {
				show_enter_highscore();
				if (sound_beep != 0 && !sound_muted)
					sp.play(sound_beep, 1, 1, 0, 0, 0);
			}
		}

		//screen touched
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (state == settings) {
				//calibrate
				if (intersect((width / 2) - (width / 6), (height / 2) - (width / 6), (width / 3), (width / 3), (int) touchx, (int) touchy)) {
					oncalibrate = true;
				}
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					onmenu = true;
				}
			} else if (state == hiscores) {
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					onmenu = true;
				}
			} else if (state == menu) {
				//play
				if (intersect((int) ((width / 2) - (play_img.getWidth() / 2)), (int) ((height / 2) - (play_img.getHeight() / 2)), play_img.getWidth(), play_img.getHeight(), (int) touchx, (int) touchy)) {
					onplay = true;
				}
				//highscores
				if (intersect((int) ((width * 0.8f) - (highscores_img.getWidth() / 2)), (int) ((height / 2.1f) - (highscores_img.getHeight() / 2)), highscores_img.getWidth(), highscores_img.getHeight(), (int) touchx, (int) touchy)) {
					onhighscores = true;
				}
				//settings
				if (intersect((int) ((width / 5) - (settings_img.getWidth() / 2)), (int) ((height / 2.2f) - (settings_img.getHeight() / 2)), settings_img.getWidth(), settings_img.getHeight(), (int) touchx, (int) touchy)) {
					onsettings = true;
				}
				//exit
				if (intersect((int) ((width * 0.9f) - (exit_img.getWidth() / 2)), (int) ((height * 0.9f) - (exit_img.getHeight() / 2)), exit_img.getWidth(), exit_img.getHeight(), (int) touchx, (int) touchy)) {
					onexit = true;
				}

			} else if (state == play) {

				//Play screen down
			} else if (state == gameover) {
				//gameover screen down
			}

			if (state == play || state == menu) {
				if (intersect(((int) ((width * 0.95f) - (music_mute_img.getWidth() / 2))), (int) (music_mute_img.getHeight() * 0.12f), music_mute_img.getWidth(), music_mute_img.getHeight(), (int) touchx, (int) touchy)) {
					if (music_muted) {
						music_muted = false;
						if (state == play && !music.isPlaying()) {
							music = MediaPlayer.create(activity, R.raw.music);
							music.start();
							music.setLooping(true);
						}
					} else {
						music_muted = true;
						music.stop();

					}
				}
				if (intersect(((int) (width * 0.88f) - (sound_mute_img.getWidth() / 2)), (int) (sound_mute_img.getHeight() * 0.12f), sound_mute_img.getWidth(), sound_mute_img.getHeight(), (int) touchx, (int) touchy)) {
					sound_muted = (sound_muted) ? false : true;//toggle sound
				}
			}

		}

		return true;
	}

	//control code//////////////////////////////////////////////////////////////////////////////////////////////
	public void back() {
		if (state == play) {
			music.stop();
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == settings) {
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == hiscores) {
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == menu) {
			System.exit(0);
			activity.finish();
		} else if (state == gameover) {
			show_enter_highscore();
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		}
	}

	public void loadscore() {
		// load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		for (int i = 0; i < 10; i++) {
			hiscore[i] = hiscores.getInt("score" + i, 0);
			hiscorename[i] = hiscores.getString("name" + i, "---");
		}

	}

	public void savescore() {
		//load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		SharedPreferences.Editor hiscores_editor = hiscores.edit();
		for (int i = 0; i < 10; i++) {
			hiscores_editor.putInt("score" + i, hiscore[i]);
			hiscores_editor.putString("name" + i, hiscorename[i]);
		}
		hiscores_editor.commit();

	}

	public void show_enter_highscore() {

		loadscore();
		if (score > hiscore[9]) {
			try {
				Class<?> classtostart = Class.forName(context.getPackageName() + ".EnterHiscore");
				Intent intent = new Intent(context, classtostart).putExtra("score", score);
				context.startActivity(intent);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		state = menu;
		score = 0;
	}

	public void create_obstacle(int type) {
		//add new obstacle at spesific x and y
		if (obstacle_direction[type] == 1) {
			obstaclex.add(new Random().nextInt(width - (obstacle_img[type].width)));
			obstacley.add(-50);
		}
		if (obstacle_direction[type] == 2) {
			obstaclex.add(width + 50);
			obstacley.add(new Random().nextInt(height - (obstacle_img[type].height)));
		}
		if (obstacle_direction[type] == 3) {
			obstaclex.add(new Random().nextInt(width - (obstacle_img[type].width)));
			obstacley.add(height + 50);
		}
		if (obstacle_direction[type] == 4) {
			obstaclex.add(-50);
			obstacley.add(new Random().nextInt(height - (obstacle_img[type].height)));
		}
		obstacletype.add(type);
		obstaclekilledtimer.add(-1);
	}

	public boolean intersect(int x, int y, int width, int height, int pointx, int pointy) {

		if ((pointx > x) && (pointy > y) && (pointx < (x + width)) && (pointy < (y + height))) {
			return (true);
		}

		return false;
	}

	public boolean checkcollision(int box1x, int box1y, int width1, int height1, int box2x, int box2y, int width2, int height2) {
		Rect a = new Rect(box1x, box1y, box1x + width1, box1y + height1);
		Rect a2 = new Rect(box2x, box2y, box2x + width2, box2y + height2);
		if (a.intersect(a2)) {
			//no clollision
			return (true);
		} else {
			// collision
			return (false);
		}
	}

	public void level_at_timeperiod(int starttime, int endtime, int[] obstacletypes, int generation_frequency, float[] holesx, float[] holesy) {
		if (((time >= starttime) && (endtime == -1)) || ((time >= starttime) && (time < endtime))) {
			//obstacles
			if (time % (100 - generation_frequency) == 0) {
				int type = (int) (Math.random() * obstacletypes.length) + 1;
				create_obstacle(obstacletypes[type - 1]);
			}
			//new level
			if (laststarttime != starttime) {
				laststarttime = starttime;
				//holes
				holex.clear();
				holey.clear();
				for (int i = 0; i < holesx.length; i++) {
					holex.add(holesx[i]);
					holey.add(holesy[i]);
				}

				//reset time
				totaltime = endtime - starttime;

				//reset level
				start = SystemClock.uptimeMillis();
				levelstarted = false;
				ballx = width / 2;
				bally = height / 2;
				ball_speedx = 0;
				ball_speedy = 0;
				level++;
			}
			remaining_time = endtime - time;
		}
	}

	//initialise images. This code runs before first game loop.
	public void initialise() {

		//create obstacle images
		for (int i = 0; i < obstacle_img.length; i++) {
			obstacle_img[i] = new gif(getResources(), obstacle_img_id[i], 3, 1, 3, 10, 1, width * obstacle_size[i]);
			obstacle_img[i].rotate((obstacle_direction[i] * 90) + 90);

			//killed obstacles images
			obstacle_killed_img[i] = Bitmap.createScaledBitmap(obstacle_killed_img[i], (int) ((float) (width * obstacle_size[i])), (int) (((float) (width * obstacle_size[i]) / obstacle_killed_img[i].getWidth()) * obstacle_killed_img[i].getHeight()), true);
			//rotate killed obstacles images
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Bitmap m = Bitmap.createBitmap((int) 5, 5, Bitmap.Config.ARGB_8888);

			int newheight = (int) ((obstacle_killed_img[i].getHeight() / 2) * Math.abs(Math.sin(obstacle_direction[i] * (Math.PI / 180)))) + obstacle_killed_img[i].getHeight();
			int newwidth = (int) (obstacle_killed_img[i].getWidth() * Math.abs(Math.cos(obstacle_direction[i] * (Math.PI / 180)))) + obstacle_killed_img[i].getHeight();

			Bitmap temp_image = Bitmap.createScaledBitmap(m, newwidth, newheight, true);
			Canvas canvas = new Canvas(temp_image);
			//canvas.drawColor(Color.RED);
			canvas.rotate((float) (obstacle_direction[i] * 90) + 90, temp_image.getWidth() / 2, temp_image.getHeight() / 2);
			canvas.drawBitmap(obstacle_killed_img[i], (temp_image.getWidth() / 2) - (obstacle_killed_img[i].getWidth() / 2), (temp_image.getHeight() / 2) - (obstacle_killed_img[i].getHeight() / 2), paint);
			canvas.rotate(-(float) (obstacle_direction[i] * 90) + 90, temp_image.getWidth() / 2, temp_image.getHeight() / 2);

			obstacle_killed_img[i] = temp_image;
		}

		//scale images for best risponsiveness
		//TODO: ---------------------------------------------------------from here you can change the scalling of each image
		play_img = Bitmap.createScaledBitmap(play_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / play_img.getWidth()) * play_img.getHeight()), true);
		settings_img = Bitmap.createScaledBitmap(settings_img, (int) ((float) (width * 0.14f)), (int) (((float) (width * 0.14f) / settings_img.getWidth()) * settings_img.getHeight()), true);
		highscores_img = Bitmap.createScaledBitmap(highscores_img, (int) ((float) (width * 0.14f)), (int) (((float) (width * 0.14f) / highscores_img.getWidth()) * highscores_img.getHeight()), true);
		exit_img = Bitmap.createScaledBitmap(exit_img, (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / exit_img.getWidth()) * exit_img.getHeight()), true);
		home_img = Bitmap.createScaledBitmap(home_img, (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / home_img.getWidth()) * home_img.getHeight()), true);

		selected_play_img = Bitmap.createScaledBitmap(play_img, (int) ((float) (width * 0.25f)), (int) (((float) (width * 0.25f) / play_img.getWidth()) * play_img.getHeight()), true);
		selected_settings_img = Bitmap.createScaledBitmap(settings_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / settings_img.getWidth()) * settings_img.getHeight()), true);
		selected_highscores_img = Bitmap.createScaledBitmap(highscores_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / highscores_img.getWidth()) * highscores_img.getHeight()), true);
		selected_exit_img = Bitmap.createScaledBitmap(exit_img, (int) ((float) (width * 0.10f)), (int) (((float) (width * 0.10f) / exit_img.getWidth()) * exit_img.getHeight()), true);
		selected_home_img = Bitmap.createScaledBitmap(home_img, (int) ((float) (width * 0.10f)), (int) (((float) (width * 0.10f) / home_img.getWidth()) * home_img.getHeight()), true);

		pause_img = Bitmap.createScaledBitmap(pause_img, (int) ((float) (width * 0.04f)), (int) (((float) (width * 0.04f) / pause_img.getWidth()) * pause_img.getHeight()), true);
		pause_play_img = Bitmap.createScaledBitmap(pause_play_img, (int) ((float) (width * 0.04f)), (int) (((float) (width * 0.04f) / pause_play_img.getWidth()) * pause_play_img.getHeight()), true);
		background_img = Bitmap.createScaledBitmap(background_img, (int) ((float) (width)), (int) (((float) (width) / background_img.getWidth()) * background_img.getHeight()), true);
		instruction_img = Bitmap.createScaledBitmap(instruction_img, (int) ((float) (width * 0.15f)), (int) (((float) (width * 0.15f) / instruction_img.getWidth()) * instruction_img.getHeight()), true);
		sound_img = Bitmap.createScaledBitmap(sound_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / sound_img.getWidth()) * sound_img.getHeight()), true);
		sound_mute_img = Bitmap.createScaledBitmap(sound_mute_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / sound_mute_img.getWidth()) * sound_mute_img.getHeight()), true);
		music_img = Bitmap.createScaledBitmap(music_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / music_img.getWidth()) * music_img.getHeight()), true);
		music_mute_img = Bitmap.createScaledBitmap(music_mute_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / music_mute_img.getWidth()) * music_mute_img.getHeight()), true);
		ball_img = Bitmap.createScaledBitmap(ball_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / ball_img.getWidth()) * ball_img.getHeight()), true);

		//text and paint
		//TODO: -----------------------------------------------------------------------change fonts, text styles and colors
		//fonts
		Typeface tragic = Typeface.createFromAsset(context.getAssets(), "tragic.ttf");

		//Text paints
		scoretext.setARGB(255, 222, 222, 222);
		scoretext.setTypeface(tragic);
		scoretext.setTextSize(fontpercent_screenheight(8));
		scoretext.setAntiAlias(true);

		stroke_scoretext.setARGB(255, 40, 40, 40);
		stroke_scoretext.setTypeface(tragic);
		stroke_scoretext.setTextSize(fontpercent_screenheight(8));
		stroke_scoretext.setAntiAlias(true);
		stroke_scoretext.setStyle(Style.STROKE);
		stroke_scoretext.setStrokeWidth(4);

		//title paint
		titletext.setTypeface(tragic);
		titletext.setARGB(255, 241, 180, 23);
		titletext.setTextSize(fontpercent_screenheight(11));
		titletext.setAntiAlias(true);

		stroke_titletext.setTypeface(tragic);
		stroke_titletext.setTextSize(fontpercent_screenheight(11));
		stroke_titletext.setARGB(255, 0, 0, 0);
		stroke_titletext.setStyle(Style.STROKE);
		stroke_titletext.setStrokeWidth(4);
		stroke_titletext.setAntiAlias(true);

		//menu text
		//menutext.setTypeface(tragic);
		menutext.setARGB(255, 222, 222, 222);
		menutext.setTextSize(fontpercent_screenheight(5));
		menutext.setAntiAlias(true);

		//pause text
		gameplaytext.setTypeface(tragic);
		gameplaytext.setAntiAlias(true);
		gameplaytext.setTextSize(fontpercent_screenheight(10));
		gameplaytext.setARGB(255, 222, 222, 222);

		//coloring Paints
		semitrasparentwhite.setARGB(80, 222, 222, 222);
		trasparentblack.setARGB(150, 0, 0, 0);
		yellow_light.setARGB(150, 255, 218, 94);
		blue.setARGB(255, 38, 134, 181);
		black.setARGB(255, 20, 20, 20);
		white.setARGB(255, 222, 222, 222);
		green.setARGB(255, 130, 191, 55);

		initialised = true;

	}

	//program loop///////////////////////////////////////////////////////////////////////////////////////////////
	public void run() {
		while (isRunning) {
			if (ourholder.getSurface().isValid()) {
				now = SystemClock.uptimeMillis();
				if (now - laststep > 25) {
					//int timelag = (int) (now - laststep) - 25;
					//	System.out.println("lag " + timelag);

					Step();
					Canvas canvas = ourholder.lockCanvas();
					Draw(canvas);
					ourholder.unlockCanvasAndPost(canvas);

					laststep = SystemClock.uptimeMillis();
				}

			}

		}
	}

	//runs before draw() to move everything
	public void Step() {

		if (state == play) {
			if (instructions) {

			} else {
				if (!paused) {
					if (!levelstarted) {

						now = SystemClock.uptimeMillis();
						countdown = 3 - (int) ((now - start) / 1000);
						if (countdown == 0) {
							levelstarted = true;
						}

					} else {
						//initialise
						if (width != -1 && height != -1) {
							//move ball
							if (ball_speedx == 0)
								ball_speedx = 0.5f;
							if (ball_speedy == 0)
								ball_speedy = 0.5f;

							ball_speedx = ball_speedx + ((sensorx - calibratex) * 0.00009f * width);
							ball_speedy = ball_speedy + ((sensory - calibratey) * 0.00009f * width);

							float prevballx = ballx;
							float prevbally = bally;

							ballx = ballx + ball_speedx;
							bally = bally + ball_speedy;

							//check ball collisions with wall
							if (ballx - (ball_img.getWidth() / 2) < 0 || ballx + (ball_img.getWidth() / 2) > width) {
								ballx = prevballx;
								ball_speedx = -ball_speedx / 3;
							}
							if (bally - (ball_img.getHeight() / 2) < 0 || bally + (ball_img.getHeight() / 2) > height) {
								bally = prevbally;
								ball_speedy = -ball_speedy / 3;
							}

							//obstacles creation
							time++;
							handle_obstacles_and_holes();

							//handle all obstacles
							for (int i = 0; i <= obstaclex.size() - 1; i++) {
								//check if killed
								if (obstaclekilledtimer.get(i) != -1) {
									//not alive
									obstaclekilledtimer.set(i, obstaclekilledtimer.get(i) - 1);
									if (obstaclekilledtimer.get(i) <= 0) {
										//has been dead for too long. Remove.
										obstaclex.remove(i);
										obstacley.remove(i);
										obstacletype.remove(i);
										obstaclekilledtimer.remove(i);
										break;
									}
								} else {
									//move obstacles if alive
									obstaclex.set(i, (int) (obstaclex.get(i) + (Math.cos(-((((obstacle_direction[obstacletype.get(i)] * 90) + 90) * Math.PI / 180) - (Math.PI / 2))) * (Math.random() * obstacle_speed[obstacletype.get(i)] * width * 0.003f))));
									obstacley.set(i, (int) (obstacley.get(i) - (Math.sin(-((((obstacle_direction[obstacletype.get(i)] * 90) + 90) * Math.PI / 180) - (Math.PI / 2))) * (Math.random() * obstacle_speed[obstacletype.get(i)] * width * 0.003f))));

									//delete if out of screen
									if (obstaclex.get(i) < -100 || obstaclex.get(i) > (width + 100)) {
										obstaclex.remove(i);
										obstacley.remove(i);
										obstacletype.remove(i);
										obstaclekilledtimer.remove(i);
										break;
									} else if (obstacley.get(i) < -100 || obstacley.get(i) > (height + 100)) {
										obstaclex.remove(i);
										obstacley.remove(i);
										obstacletype.remove(i);
										obstaclekilledtimer.remove(i);
										break;
									} else {
										//collision detection with obstacles
										if (checkcollision(obstaclex.get(i) - (obstacle_img[obstacletype.get(i)].width / 2), obstacley.get(i) - (obstacle_img[obstacletype.get(i)].height / 2), obstacle_img[obstacletype.get(i)].width, obstacle_img[obstacletype.get(i)].height, (int) ballx - (ball_img.getWidth() / 2), (int) bally - (ball_img.getHeight() / 2), ball_img.getWidth(), ball_img.getHeight())) {
											score += obstacle_points[obstacletype.get(i)];
											if (sound_score != 0 && !sound_muted)
												sp.play(sound_score, 1, 1, 0, 0, 0);

											obstaclekilledtimer.set(i, 200);
										}
									}

								}
							}

							for (int i = 0; i <= holex.size() - 1; i++) {
								//collision detection with holes
								if (checkcollision((int) (holex.get(i) - (height * 0.06f)), (int) (holey.get(i) - (height * 0.06f)), (int) ((height * 0.06f) * 2), (int) ((height * 0.06f) * 2), (int) ballx - (ball_img.getWidth() / 2), (int) bally - (ball_img.getHeight() / 2), ball_img.getWidth(), ball_img.getHeight())) {
									state = gameover;
									music.stop();
								}
							}

						}

					}
				}
			}
		}
	}

	public void Draw(Canvas canvas) {
		if (!initialised) {
			width = canvas.getWidth();
			height = canvas.getHeight();
			initialise();
		} else {


			if (state == menu) {
				//draw background
				for (int y = 0; y < ((height) / background_img.getHeight()) + 1; y++) {
					canvas.drawBitmap(background_img, 0, y * background_img.getHeight(), null);
				}

				//draw title
				canvas.drawText(context.getString(R.string.app_name), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.app_name)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.app_name), (width / 2) - (titletext.measureText(context.getString(R.string.app_name)) / 2), (height / 7), titletext);

				//sound control
				if (music_muted)
					canvas.drawBitmap(music_mute_img, (width * 0.95f) - (music_mute_img.getWidth() / 2), music_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(music_img, (width * 0.95f) - (music_img.getWidth() / 2), music_img.getHeight() * 0.12f, null);

				if (sound_muted)
					canvas.drawBitmap(sound_mute_img, (width * 0.88f) - (sound_mute_img.getWidth() / 2), sound_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(sound_img, (width * 0.88f) - (sound_img.getWidth() / 2), sound_img.getHeight() * 0.12f, null);

				//draw menu buttons
				if (onplay)
					canvas.drawBitmap(selected_play_img, (width / 2) - (selected_play_img.getWidth() / 2), (height / 2) - (selected_play_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(play_img, (width / 2) - (play_img.getWidth() / 2), (height / 2) - (play_img.getHeight() / 2), null);

				if (onhighscores)
					canvas.drawBitmap(selected_highscores_img, (width * 0.8f) - (selected_highscores_img.getWidth() / 2), (height / 2.1f) - (selected_highscores_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(highscores_img, (width * 0.8f) - (highscores_img.getWidth() / 2), (height / 2.1f) - (highscores_img.getHeight() / 2), null);

				if (onsettings)
					canvas.drawBitmap(selected_settings_img, (width / 5) - (selected_settings_img.getWidth() / 2), (height / 2.2f) - (selected_settings_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(settings_img, (width / 5) - (settings_img.getWidth() / 2), (height / 2.2f) - (settings_img.getHeight() / 2), null);

				if (onexit)
					canvas.drawBitmap(selected_exit_img, (width * 0.9f) - (selected_exit_img.getWidth() / 2), (height * 0.9f) - (selected_exit_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(exit_img, (width * 0.9f) - (exit_img.getWidth() / 2), (height * 0.9f) - (exit_img.getHeight() / 2), null);

			}

			if (state == settings) {
				//draw background
				for (int y = 0; y < ((height) / background_img.getHeight()) + 1; y++) {
					canvas.drawBitmap(background_img, 0, y * background_img.getHeight(), null);
				}

				//draw title
				canvas.drawText(context.getString(R.string.settings_title), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.settings_title)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.settings_title), (width / 2) - (titletext.measureText(context.getString(R.string.settings_title)) / 2), (height / 7), titletext);

				//calibrate tool
				canvas.drawRect((width / 2) - (width / 4), (height / 2) - (width / 6), (width / 2) + (width / 4), (height / 2) + (width / 6), black);
				if (oncalibrate)
					canvas.drawCircle((float) ((width / 2) - ((sensorx - calibratex) * (float) (width / 80))), (float) ((height / 2) - ((sensory - calibratey) * (float) (width / 80))), (width / 30), yellow_light);
				else
					canvas.drawCircle((float) ((width / 2) - ((sensorx - calibratex) * (float) (width / 80))), (float) ((height / 2) - ((sensory - calibratey) * (float) (width / 80))), (width / 30), white);
				canvas.drawText(context.getString(R.string.calibrate_instructions), (width / 2) - (menutext.measureText(context.getString(R.string.calibrate_instructions)) / 2), (height * 0.65f), menutext);

				//back to menu button
				if (onmenu)
					canvas.drawBitmap(selected_home_img, (width * 0.9f) - (selected_home_img.getWidth() / 2), (height * 0.9f) - (selected_home_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(home_img, (width * 0.9f) - (home_img.getWidth() / 2), (height * 0.9f) - (home_img.getHeight() / 2), null);
			}

			if (state == gameover) {
				//draw background
				for (int y = 0; y < ((height) / background_img.getHeight()) + 1; y++) {
					canvas.drawBitmap(background_img, 0, y * background_img.getHeight(), null);
				}

				//draw title
				canvas.drawText(context.getString(R.string.game_over), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.game_over)) / 2), (height / 2) - titletext.getTextSize(), stroke_titletext);
				canvas.drawText(context.getString(R.string.game_over), (width / 2) - (titletext.measureText(context.getString(R.string.game_over)) / 2), (height / 2) - titletext.getTextSize(), titletext);

				//click to continue text
				canvas.drawText(context.getString(R.string.game_over_continue), (width / 2) - (menutext.measureText(context.getString(R.string.game_over_continue)) / 2), (height / 2) + titletext.getTextSize(), menutext);

			}

			if (state == hiscores) {

				//draw background
				for (int y = 0; y < ((height) / background_img.getHeight()) + 1; y++) {
					canvas.drawBitmap(background_img, 0, y * background_img.getHeight(), null);
				}

				//draw title
				canvas.drawText(context.getString(R.string.highscore_title), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.highscore_title)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.highscore_title), (width / 2) - (titletext.measureText(context.getString(R.string.highscore_title)) / 2), (height / 7), titletext);

				//back to menu button
				if (onmenu)
					canvas.drawBitmap(selected_home_img, (width * 0.9f) - (selected_home_img.getWidth() / 2), (height * 0.9f) - (selected_home_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(home_img, (width * 0.9f) - (home_img.getWidth() / 2), (height * 0.9f) - (home_img.getHeight() / 2), null);

				//hiscores
				for (int i = 0; i < 10; i++) {
					canvas.drawText(hiscorename[i], (width / 2) - (width / 4), (height / 2) - (height / 3.5f) + (i * menutext.getTextSize() * 1.5f), menutext);
					canvas.drawText("" + hiscore[i], (width / 2) + (width / 6), (height / 2) - (height / 3.5f) + (i * menutext.getTextSize() * 1.5f), menutext);
				}

			}

			if (state == play) {

				//draw background
				for (int y = 0; y < ((height) / background_img.getHeight()) + 1; y++) {
					canvas.drawBitmap(background_img, 0, y * background_img.getHeight(), null);
				}
				//draw holes
				for (int i = 0; i <= holex.size() - 1; i++) {
					canvas.drawCircle(holex.get(i), holey.get(i), (height * 0.08f), black);
				}

				//draw obstacles
				for (int i = 0; i <= obstaclex.size() - 1; i++) {
					if (obstaclekilledtimer.get(i) == -1) {
						//alive
						obstacle_img[obstacletype.get(i)].draw(canvas, obstaclex.get(i) - (obstacle_img[obstacletype.get(i)].width / 2), obstacley.get(i) - (obstacle_img[obstacletype.get(i)].height / 2), 0);
					} else {
						canvas.drawBitmap(obstacle_killed_img[obstacletype.get(i)], obstaclex.get(i) - (obstacle_killed_img[obstacletype.get(i)].getWidth() / 2), obstacley.get(i) - (obstacle_killed_img[obstacletype.get(i)].getHeight() / 2), null);
					}
				}

				//draw ball				
				canvas.drawBitmap(ball_img, ballx - (ball_img.getWidth() / 2), bally - (ball_img.getHeight() / 2), null);

				//draw time remaining
				canvas.drawRect(width * 0.03f, height * 0.04f, width * 0.38f, height * 0.07f, black);
				canvas.drawRect(width * 0.03f, height * 0.02f, (float) ((width * 0.03f) + (width * 0.35f) - (((width * 0.35f) * ((float) remaining_time / (((float) totaltime == 0) ? 1 : (float) totaltime))))), height * 0.09f, green);

				//draw score
				canvas.drawText("Score: " + score, width * 0.03f, height * 0.19f, stroke_scoretext);
				canvas.drawText("Score: " + score, width * 0.03f, height * 0.19f, scoretext);

				//pause
				if (paused)
					canvas.drawBitmap(pause_play_img, (width / 2) - (pause_play_img.getWidth() / 2), pause_play_img.getHeight() * 0.2f, null);
				else
					canvas.drawBitmap(pause_img, (width / 2) - (pause_img.getWidth() / 2), pause_img.getHeight() * 0.2f, null);

				//sound control
				if (music_muted)
					canvas.drawBitmap(music_mute_img, (width * 0.95f) - (music_mute_img.getWidth() / 2), music_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(music_img, (width * 0.95f) - (music_img.getWidth() / 2), music_img.getHeight() * 0.12f, null);

				if (sound_muted)
					canvas.drawBitmap(sound_mute_img, (width * 0.88f) - (sound_mute_img.getWidth() / 2), sound_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(sound_img, (width * 0.88f) - (sound_img.getWidth() / 2), sound_img.getHeight() * 0.12f, null);

				if (instructions) {
					canvas.drawRect((width / 2) - (width / 4), (height / 2) - (height / 4), (width / 2) + (width / 4), (height / 2) + (height / 4), trasparentblack);

					StaticLayout instructionlayout = new StaticLayout(context.getString(R.string.instructions), new TextPaint(menutext), (int) ((width / 4.4f) + (width / 4)), Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
					canvas.translate((width / 2) - (width / 4.4f), (height / 2) + (height / 30)); //position the text
					instructionlayout.draw(canvas);
					canvas.translate(-((width / 2) - (width / 4.4f)), -((height / 2) + (height / 30))); //position the text

					canvas.drawText(context.getString(R.string.instructions_continue), (width / 2) - (menutext.measureText(context.getString(R.string.instructions_continue)) / 2), (height / 2) + (height / 3), menutext);
					//instruction image
					canvas.drawBitmap(instruction_img, (width / 2) - (instruction_img.getWidth() / 2), (height * 0.4f) - (instruction_img.getHeight() / 2), null);

				} else {
					if (!levelstarted) {
						canvas.drawText(context.getString(R.string.level) + " " + level, (width / 2) - (gameplaytext.measureText(context.getString(R.string.level) + " " + level) / 2), (height / 2) - ((gameplaytext.getTextSize() / 2)), gameplaytext);
						canvas.drawText("" + countdown, (width / 2) - (gameplaytext.measureText("" + countdown) / 2), (height / 2) + (gameplaytext.getTextSize()), gameplaytext);

					}
				}

				if (paused) {
					canvas.drawRect(0, 0, width, height, trasparentblack);
					canvas.drawText(context.getString(R.string.Pause), (width / 2) - (gameplaytext.measureText(context.getString(R.string.Pause)) / 2), (height / 2) - (gameplaytext.getTextSize() / 2), gameplaytext);
				}

			}

		}
	}

	public int fontpercent_screenheight(double d) {
		//get resolution
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		int px = (int) ((float) dm.heightPixels * ((float) d / 100));
		float dp = px / dm.density;
		return (int) dp;
	}

	//enviorment handlars//////////////////////////////////////////////////////////////////////////////////////////
	public void pause() {
		isRunning = false;
		if (state == play)
			music.stop();
		if (state == play && levelstarted && !instructions)
			paused = true;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;
		sm.unregisterListener(this);
	}

	public void resume() {
		isRunning = true;
		thread = new Thread(this);
		thread.start();
		sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}

}
