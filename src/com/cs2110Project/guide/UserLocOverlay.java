package com.cs2110Project.guide;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class UserLocOverlay extends Overlay {
	private Location location;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		if (location != null) {
			this.location = location;
		}
	}

	private final int mRadius = 20;

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection proj = mapView.getProjection();

		if (shadow == false && location != null) {
			Double latitude = location.getLatitude() * 1E6;
			Double longitude = location.getLongitude() * 1E6;
			GeoPoint geoPoint = new GeoPoint(latitude.intValue(),
					longitude.intValue());

			Point point = new Point();
			proj.toPixels(geoPoint, point);

			RectF oval = new RectF(point.x - mRadius, point.y - mRadius,
					point.x + mRadius, point.y + mRadius);

			Paint backPaint = new Paint();
			backPaint.setARGB(175, 50, 50, 50);
			backPaint.setAntiAlias(true);


			canvas.drawOval(oval, backPaint);
		}
		super.draw(canvas, mapView, shadow);

	}

	@Override
	public boolean onTap(GeoPoint point, MapView mapView) {
		return false;
	}
}
