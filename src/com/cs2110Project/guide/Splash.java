package com.cs2110Project.guide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {

	private final int SPLASH_DISPLAY_LENGTH = 3000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.splashscreen);

		String file1Name = getString(R.string.demoLocationFile);
		this.deleteFile(file1Name);
		String file2Name = getString(R.string.locationLog);
		this.deleteFile(file2Name);
		String file3Name = getString(R.string.timeLog);
		this.deleteFile(file3Name);
		String file4Name = getString(R.string.tour1LocationFile);
		this.deleteFile(file3Name);
		String file5Name = getString(R.string.tour2LocationFile);
		this.deleteFile(file3Name);
		String file6Name = getString(R.string.tourLocationFile);
		this.deleteFile(file3Name);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				/* Create an Intent that will start the Menu-Activity. */
				Intent mainIntent = new Intent(Splash.this, MenuActivity.class);
				Splash.this.startActivity(mainIntent);
				Splash.this.finish();
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
}