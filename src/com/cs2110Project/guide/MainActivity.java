package com.cs2110Project.guide;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements LocationListener,
		SensorEventListener {

	// Fields
	private int TIME_BETWEEN_UPDATES; // seconds
	private int DIST_BETWEEN_UPDATES = 5; // meters

	float radToDeg = (float) (180 / (Math.PI));
	float myBearing = 0;
	float myOrientation = 0;

	private TextView destText, timerText, statusText;
	private Button saveButton, pauseButton, recenterButton;
	private LocationManager locationManager;
	private String bestProvider;

	private FileOutputStream fos;
	private FileInputStream fis;
	private String READ_FILENAME, LOG_FILENAME, BREAK_CHAR, MID_CHAR,
			TIME_LOG_FILENAME;

	private MapView mapview;
	private boolean mLastAccelerometerSet, mLastMagnetometerSet;
	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];

	private double latitude, longitude;
	private final Criteria crit = new Criteria();
	private MapController mc;
	private SensorManager mSensorManager;
	private Sensor mMagnetometer, mAccelerometer;

	private GeoPoint userLoc;
	private UserLocOverlay userOverlay;
	private MarkerItemizedOverlay locationOverlays;
	private ArrayList<String> locations;
	private Context context;

	private Timer logTimer;
	private Timer timeTimer;
	private Handler UIHandler;
	private Location lastLocation;

	private int timeCounter;
	private int seconds, minutes;

	private float textSize;
	private int startVal;

	private boolean ARROW_MODE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		Intent intent = getIntent();
		startVal = intent.getIntExtra("type", 0);
		if (startVal == 31) {
			READ_FILENAME = getResources().getString(R.string.demoLocationFile);
		} else {
			READ_FILENAME = getResources().getString(R.string.tourLocationFile);
		}

		LOG_FILENAME = getResources().getString(R.string.locationLog);
		BREAK_CHAR = getResources().getString(R.string.IOBreakChar);
		MID_CHAR = getResources().getString(R.string.IOMidChar);
		TIME_LOG_FILENAME = getResources().getString(R.string.timeLog);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		TIME_BETWEEN_UPDATES = Integer.parseInt((prefs.getString(
				"PREF_UPDATE_FREQ", "30")));

		setContentView(R.layout.tour_layout);
		latitude = -1;
		longitude = -1;

		locations = new ArrayList<String>();

		destText = (TextView) findViewById(R.id.destTextView);
		timerText = (TextView) findViewById(R.id.timerTextView);
		statusText = (TextView) findViewById(R.id.statusTextView);
		mapview = (MapView) findViewById(R.id.tourMapView);
		// saveButton = (Button) findViewById(R.id.saveButton);
		pauseButton = (Button) findViewById(R.id.pauseButton);
		recenterButton = (Button) findViewById(R.id.recenterButton);

		textSize = pauseButton.getTextSize();
		final float textsize = textSize;
		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (pauseButton.getText().equals("Pause")) {
					timeTimer.cancel();
					pauseButton.setTextSize(textsize - 12);
					pauseButton.setText("Resume");
					pauseButton.invalidate();
				} else {
					timeTimer = new Timer();
					timeTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							timeCounter++;
							seconds++;
							if (seconds % 60 == 0) {
								seconds = 0;
								minutes++;
							}
							UIHandler.post(new Runnable() {
								@Override
								public void run() {
									if (seconds >= 10) {
										timerText.setText(minutes + ":"
												+ seconds);
									} else {
										timerText.setText(minutes + ":0"
												+ seconds);
									}
									timerText.invalidate();
								}
							});
						}
					}, 0, 1000);
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							pauseButton.setTextSize(textsize - 8);
							pauseButton.setText("Pause");
							pauseButton.invalidate();
						}
					});
				}
			}

		});

		recenterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateLocation();
				mc.animateTo(userLoc);
			}
		});

		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// bestProvider = locationManager.getBestProvider(crit, true);
		// locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		updateLocation();

		mc = mapview.getController();

		mapview.setBuiltInZoomControls(true);

		userLoc = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));

		mc.animateTo(userLoc);
		mc.setZoom(21);

		Drawable marker = getResources().getDrawable(R.drawable.flag_marker);
		marker = scale(marker, 2.5f);

		locationOverlays = new MarkerItemizedOverlay(marker);
		updateArrayLocations();
		updateLocationOverlays();

		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(context,
				mapview);
		myLocationOverlay.enableMyLocation();
		List<Overlay> overlays = mapview.getOverlays();
		overlays.add(myLocationOverlay);
		mapview.postInvalidate();

		UIHandler = new Handler();
		logTimer = new Timer();
		timeTimer = new Timer();
		timeCounter = 0;
		seconds = 0;
		minutes = 0;
		timeTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				timeCounter++;
				seconds++;
				if (seconds % 60 == 0) {
					seconds = 0;
					minutes++;
				}
				UIHandler.post(new Runnable() {
					@Override
					public void run() {
						if (seconds >= 10) {
							timerText.setText(minutes + ":" + seconds);
						} else {
							timerText.setText(minutes + ":0" + seconds);
						}
						timerText.invalidate();
					}
				});
			}
		}, 0, 1000);

		logTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (lastLocation != null) {
					String text = BREAK_CHAR + lastLocation.getLatitude()
							+ MID_CHAR + lastLocation.getLongitude() + MID_CHAR
							+ timerText.getText() + BREAK_CHAR;
					System.out.println(text);
					try {
						fos = openFileOutput(LOG_FILENAME, Context.MODE_APPEND);
						fos.write(text.getBytes());
						fos.close();
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
			}

		}, 0, 5000);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public Drawable scale(Drawable drawable, float factor) {
		Bitmap bitmapOrg = ((BitmapDrawable) drawable).getBitmap();
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		// calculate the scale
		float scale = factor;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
				height, matrix, true);
		// make a Drawable from Bitmap to allow to set the BitMap
		return new BitmapDrawable(resizedBitmap);

	}

	public void updateLocation() {
		Location loc = locationManager.getLastKnownLocation(locationManager
				.getBestProvider(crit, true));
		if (loc != null) {
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			lastLocation = loc;
		} else {
			// default to rice hall
			latitude = 38.031447;
			longitude = -78.510822;
		}
	}

	@Override
	public void onStop() {
		try {
			fos.close();
			fis.close();
		} catch (Exception e) {
		}
		logTimer.cancel();
		timeTimer.cancel();
		super.onStop();
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		logTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

			}

		}, 5000, 5000);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagnetometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}

	public void checkWin() {
		if (locations.size() == 0) {
			logTimer.cancel();
			timeTimer.cancel();
			UIHandler.post(new Runnable() {

				@Override
				public void run() {
					String title = "Congratulations!";
					String message = "The tour is finished!";

					AlertDialog.Builder ad = new AlertDialog.Builder(context);
					ad.setTitle(title);
					ad.setMessage(message);
					ad.setCancelable(true);
					ad.show();
				}
			});

			try {
				fos = openFileOutput(TIME_LOG_FILENAME, Context.MODE_APPEND);
				CharSequence text = timerText.getText();
				String t = BREAK_CHAR + startVal + MID_CHAR + text.toString()
						+ BREAK_CHAR;
				System.out.println(text);
				fos.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}

	public void updateArrayLocations() {
		System.out.println("updating arrays...");
		locations.clear();
		String inputString = null;
		try {
			fis = openFileInput(READ_FILENAME);
			BufferedReader inputReader = new BufferedReader(
					new InputStreamReader(fis));
			inputString = inputReader.readLine();
			// System.out.println("input string" + inputString);
			fis.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
		String[] splitOne = {};
		if (inputString != null) {
			splitOne = inputString.split(BREAK_CHAR);
			for (String s : splitOne) {
				String[] temp = s.split(MID_CHAR);
				if (temp.length > 1) {
					locations.add(temp[0] + " " + temp[1]);
				}
			}
		}
	}

	public void updateLocationOverlays() {
		locationOverlays.clear();
		if (locations.size() > 0) {
			String lat = locations.get(0).split(" ")[0];
			String lon = locations.get(0).split(" ")[1];
			Double x = Double.parseDouble(lat) * 1E6;
			Double y = Double.parseDouble(lon) * 1E6;
			System.out.println("geopoint at " + x + " " + y);
			GeoPoint geoPoint = new GeoPoint(x.intValue(), y.intValue());
			locationOverlays.addNewItem(geoPoint, "", "");

			List<Overlay> overlays = mapview.getOverlays();
			overlays.add(locationOverlays);
			mapview.postInvalidate();
		}
	}

	@Override
	public void onLocationChanged(Location loc) {
		if (lastLocation != null) {
			userLoc = new GeoPoint((int) (loc.getLatitude() * 1E6),
					(int) (loc.getLongitude() * 1E6));
			if (locationOverlays.size() != 0) {
				final OverlayItem item = locationOverlays.getItem(0);
				Location tempLoc = new Location("createdInOnLocationChanged");
				tempLoc.setLatitude(item.getPoint().getLatitudeE6() / 1E6);
				tempLoc.setLongitude(item.getPoint().getLongitudeE6() / 1E6);
				final double distance = lastLocation.distanceTo(tempLoc);
				if (distance > 200) {
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							statusText.setText("Cold");
							statusText.invalidate();
							destText.setText((item.getPoint().getLatitudeE6() / 1E6)
									+ " " + item.getPoint().getLongitudeE6());
							destText.invalidate();
						}
					});
				} else if (distance > 100) {
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							statusText.setText("Getting warmer");
							statusText.invalidate();
							destText.setText((item.getPoint().getLatitudeE6() / 1E6)
									+ " " + item.getPoint().getLongitudeE6());
							destText.invalidate();
						}
					});
				} else if (distance > 50) {
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							statusText.setText("Warm");
							statusText.invalidate();
							destText.setText((item.getPoint().getLatitudeE6() / 1E6)
									+ " " + item.getPoint().getLongitudeE6());
							destText.invalidate();
						}
					});
				} else if (distance > 10) {
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							statusText.setText("Hot");
							statusText.invalidate();
							destText.setText((item.getPoint().getLatitudeE6() / 1E6)
									+ " " + item.getPoint().getLongitudeE6());
							destText.invalidate();
						}
					});
				} else {
					UIHandler.post(new Runnable() {
						@Override
						public void run() {
							statusText.setText("You're there!");
							statusText.invalidate();

							locations.remove(0);
							updateLocationOverlays();
							checkWin();
						}
					});
				}
				// // do arrow stuff
				//
				// System.out.println("doing arrow stuff...");
				// lastLocation.setBearing(lastLocation.bearingTo(tempLoc));
				// if (lastLocation.getBearing() < 0) {
				// myBearing = 360 + lastLocation.getBearing();
				// } else {
				// myBearing = lastLocation.getBearing();
				// }
				// myBearing -= 90;
				// GeoPoint geo = new GeoPoint(
				// (int) (lastLocation.getLatitude() * 1E6),
				// (int) (lastLocation.getLongitude() * 1E6));
				// arrow = BitmapFactory.decodeResource(this.getResources(),
				// R.drawable.arrow);
				// Matrix rot = new Matrix();
				// rot.preRotate(myBearing);
				// Bitmap rArrow = Bitmap.createBitmap(arrow, 0, 0,
				// arrow.getWidth(), arrow.getHeight(), rot, true);
				// Drawable d = new BitmapDrawable(getResources(), rArrow);
				// d = scale(d, 0.5f);
				// MarkerItemizedOverlay arrowOverlay = new
				// MarkerItemizedOverlay(
				// d);
				// arrowOverlay.addNewItem(geo, "", "");
				// List<Overlay> list = mapview.getOverlays();
				// if (previousOverlay != null) {
				// list.remove(previousOverlay);
				// }
				// list.add(arrowOverlay);
				// previousOverlay = arrowOverlay;
				// UIHandler.post(new Runnable() {
				//
				// @Override
				// public void run() {
				// mapview.postInvalidate();
				// }
				// });

				// lastLocation.setBearing(lastLocation.bearingTo(tempLoc));
				// if (lastLocation.getBearing() < 0) {
				// myBearing = 360 + lastLocation.getBearing();
				// } else {
				// myBearing = lastLocation.getBearing();
				// }
				// if (mOrientation[0] * radToDeg < 0) {
				// myOrientation = 360 + mOrientation[0] * radToDeg;
				// } else {
				// myOrientation = mOrientation[0] * radToDeg;
				// }
				//
				// img1.setRotation(myBearing - myOrientation - 90);

			}
			lastLocation = loc;
		} else {
			lastLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

	}

	private MarkerItemizedOverlay previousOverlay;

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor == mAccelerometer) {
			System.arraycopy(event.values, 0, mLastAccelerometer, 0,
					event.values.length);
			mLastAccelerometerSet = true;
		} else if (event.sensor == mMagnetometer) {
			System.arraycopy(event.values, 0, mLastMagnetometer, 0,
					event.values.length);
			mLastMagnetometerSet = true;
		}
		if (mLastAccelerometerSet && mLastMagnetometerSet) {
			SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
					mLastMagnetometer);
			SensorManager.getOrientation(mR, mOrientation);

		}
	}
}