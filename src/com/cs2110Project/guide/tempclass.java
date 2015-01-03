package com.cs2110Project.guide;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener,
		SensorEventListener {

	// Fields
	TextView text1 = null;
	TextView text2 = null;
	TextView text3 = null;
	ImageView img1 = null;
	Location loc1 = new Location("loc1");
	Location loc2 = new Location("loc2");
	LocationManager location = null;
	float radToDeg = (float) (180 / (Math.PI));
	float myBearing = 0;
	float myOrientation = 0;

	// Sensor Fields -- these fields were needed to determine the orientation of
	// the phone
	// I had to use this method because the Orientation Sensor has been
	// deprecated.
	// Source used -->
	// http://stackoverflow.com/questions/10291322/what-is-the-alternative-to-android-orientation-sensor
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;

	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private boolean mLastAccelerometerSet = false;
	private boolean mLastMagnetometerSet = false;

	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		text1 = (TextView) findViewById(R.id.text1);
		text2 = (TextView) findViewById(R.id.text2);
		text3 = (TextView) findViewById(R.id.text3);
		img1 = (ImageView) findViewById(R.id.arrow);
		loc1.setLatitude(38.02635327);
		loc1.setLongitude(-78.514589765);

		location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				MainActivity.this);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		// Display pivot point (for testing purposes)
		// text3.setText(Float.toString(img1.getPivotX()) + ", " +
		// Float.toString(img1.getPivotY()));
		// The arrow points to the right by default (=0) and spins at its center

	}

	@TargetApi(11)
	// This is needed to rotate an image
	public void onLocationChanged(Location location) {
		String locInfo = String.format(
				"Current loc = (%f, %f, args) @ (%f meters)",
				location.getLatitude(), location.getLongitude(),
				location.getAltitude());
		text1.setText(locInfo);

		// If within 25 meters, they are close by, if within 5 meters, they are
		// here!
		if (location.distanceTo(loc1) < 25) {
			text2.setText("ON FIRE!");
			if (location.distanceTo(loc1) < 5) {
				text2.setText("You're Here!");
			}
		} else {
			text2.setText("Cold Frosty Icecubes");
		}

		// Get bearing and set arrow rotation
		loc2.setLatitude(location.getLatitude());
		loc2.setLongitude(location.getLongitude());
		loc2.setBearing(loc2.bearingTo(loc1));

		// These were used to rewrite bearing and orientation values into
		// degrees
		if (loc2.getBearing() < 0) {
			myBearing = 360 + loc2.getBearing();
		} else {
			myBearing = loc2.getBearing();
		}
		if (mOrientation[0] * radToDeg < 0) {
			myOrientation = 360 + mOrientation[0] * radToDeg;
		} else {
			myOrientation = mOrientation[0] * radToDeg;
		}

		img1.setRotation(myBearing - myOrientation - 90);
		// Test for rotation degrees
		// text3.setText(Float.toString(img1.getRotation()));
		text3.setText("Target Location: " + Double.toString(loc1.getLatitude())
				+ ", " + Double.toString(loc1.getLongitude()) + "\n"
				+ ("Current Location: ") + Double.toString(loc2.getLatitude())
				+ ", " + Double.toString(loc2.getLongitude()) + "\n"
				+ ("Distance To: ") + location.distanceTo(loc1) + "\n"
				+ "Bearing: " + Float.toString(myBearing) + "\n" + "Compass: "
				+ Float.toString(myOrientation));

	}

	// These methods are necessary for implementation of the above classes
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	// Sensor Methods
	protected void onResume() {
		super.onResume();
		mLastAccelerometerSet = false;
		mLastMagnetometerSet = false;
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagnetometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

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

			// Test for orientation values
			/*
			 * text3.setText("azimuth_angle: " + mOrientation[0]*radToDeg + "\n"
			 * + "pitch_angle: " + mOrientation[1]*radToDeg + "\n" +
			 * "roll_angle: " + mOrientation[2]*radToDeg);
			 */
			/*
			 * Log.i("OrientationTestActivity",
			 * String.format("Orientation: %f, %f, %f", mOrientation[0],
			 * mOrientation[1], mOrientation[2]));
			 */
		}
	}
}