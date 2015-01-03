package com.cs2110Project.guide;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class MenuActivity extends Activity {
	private Button startButton;
	private Spinner spinner;
	private ImageView image;
	private RelativeLayout rl;
	private RadioGroup rg;
	private RelativeLayout.LayoutParams params;
	private final int MANUAL_BUTTON_ID = 29;
	private final int MAP_BUTTON_ID = 31;

	private Context context;

	private boolean ARROW_MODE;
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		context = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.deleteFile(getResources().getString(R.string.tourLocationFile));
		ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
		// dynamic
		rl = (RelativeLayout) findViewById(R.id.menuWrapper);
		rg = new RadioGroup(this);
		RadioButton keyboardButton = new RadioButton(this);
		RadioButton mapButton = new RadioButton(this);
		mapButton.setText("Map entry");
		keyboardButton.setText("Keyboard entry");
		keyboardButton.setId(MANUAL_BUTTON_ID);
		mapButton.setId(MAP_BUTTON_ID);
		rg.addView(keyboardButton);
		rg.addView(mapButton);
		rg.setOrientation(RadioGroup.HORIZONTAL);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW, R.id.lineOneHolder);

		// static
		startButton = (Button) findViewById(R.id.start_button);
		spinner = (Spinner) findViewById(R.id.menuSpinner);
		image = (ImageView) findViewById(R.id.settingsImage);
		image.setAlpha(75);
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MenuActivity.this,
						SettingsActivity.class);
				MenuActivity.this.startActivity(myIntent);
			}

		});

		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int val = getValue();
				Intent myIntent = null;

				switch (val) {
				case 0:
					ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
					if (!ARROW_MODE) {
						myIntent = new Intent(MenuActivity.this,
								MainActivity.class);
						myIntent.putExtra("type", 0);
					} else {
						myIntent = new Intent(MenuActivity.this,
								ArrowMainActivity.class);
						myIntent.putExtra("type", 0);
					}
					try {
						FileOutputStream fos = openFileOutput(getResources()
								.getString(R.string.tourLocationFile),
								Context.MODE_APPEND);
						fos.write("/38.0317723283346!-78.51109564304352/"
								.getBytes());
						fos.write("/38.034089916446234!-78.51034730672836/"
								.getBytes());
						fos.write("/38.034436!-78.507188/".getBytes());
						fos.write("/38.036768678065805!-78.5051679611206/"
								.getBytes());
						fos.write("/38.03357443372703!-78.5044813156128/"
								.getBytes());
						fos.close();
					} catch (FileNotFoundException e) {
					} catch (NotFoundException e) {
					} catch (IOException e) {
					}
					break;
				case 1:
					ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
					if (!ARROW_MODE) {
						myIntent = new Intent(MenuActivity.this,
								MainActivity.class);
						myIntent.putExtra("type", 10);
					} else {
						myIntent = new Intent(MenuActivity.this,
								ArrowMainActivity.class);
						myIntent.putExtra("type", 10);
					}
					try {
						FileOutputStream fos = openFileOutput(getResources()
								.getString(R.string.tourLocationFile),
								Context.MODE_APPEND);
						fos.write("/38.0317723283346!-78.51109564304352/"
								.getBytes());
						fos.write("/38.03215472486102!-78.50898742675781/"
								.getBytes());
						fos.write("/38.031056!-78.509717/".getBytes());
						fos.write("/38.030718086688985!-78.51282835006714/"
								.getBytes());
						fos.write("/38.033422323379!-78.5078501701355/"
								.getBytes());
						fos.close();
					} catch (FileNotFoundException e) {
					} catch (NotFoundException e) {
					} catch (IOException e) {
					}
					break;
				case 2:
					ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
					if (!ARROW_MODE) {
						myIntent = new Intent(MenuActivity.this,
								MainActivity.class);
						myIntent.putExtra("type", 20);
					} else {
						myIntent = new Intent(MenuActivity.this,
								ArrowMainActivity.class);
						myIntent.putExtra("type", 20);
					}
					try {
						FileOutputStream fos = openFileOutput(getResources()
								.getString(R.string.tourLocationFile),
								Context.MODE_APPEND);
						fos.write("/38.0317723283346!-78.51109564304352/"
								.getBytes());
						fos.write("/38.03234064068306!-78.5104250907898/"
								.getBytes());
						fos.write("/38.03261106285455!-78.50810766220093/"
								.getBytes());
						fos.write("/38.03087!-78.519373/".getBytes());
						fos.write("/8.02989!-78.51536/".getBytes());
						fos.close();
					} catch (FileNotFoundException e) {
					} catch (NotFoundException e) {
					} catch (IOException e) {
					}
					break;
				case 3:
					ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
					int checkedButtonValue = rg.getCheckedRadioButtonId();
					if (checkedButtonValue == MANUAL_BUTTON_ID) {
						myIntent = new Intent(MenuActivity.this,
								DemoManualEntryActivity.class);
					}
					break;
				case Spinner.INVALID_POSITION:
					break;
				default:
					break;
				}
				if (myIntent != null)
					MenuActivity.this.startActivity(myIntent);
			}
		});

	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			if (pos == 3) {
				try {
					rl.addView(rg, params);
				} catch (Exception e) {
				}
			} else
				rl.removeView(rg);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	}

	public void onStop() {
		super.onStop();
		if (rl.getChildCount() != 0)
			rl.removeView(rg);

	}

	public void onResume() {
		super.onResume();
		if (spinner.getSelectedItemPosition() == 3)
			rl.addView(rg);
	}

	public int getValue() {
		int ret = 0;
		ret = spinner.getSelectedItemPosition();
		return ret;
	}
}
