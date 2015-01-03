package com.cs2110Project.guide;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MarkerItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> items;

	public MarkerItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		items = new ArrayList<OverlayItem>();
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		OverlayItem item;
		try {
			item = items.get(i);
			return item;
		} catch (Exception e) {
			item = new OverlayItem(new GeoPoint(0,0), "", "");
			return item;
		}
	}

	public void clear() {
		items.clear();
	}

	@Override
	public int size() {
		return items.size();
	}

	public void removeItem(int index) {
		items.remove(index);
		populate();
	}

	public void removeItem(OverlayItem item) {
		items.remove(item);
		populate();
	}

	public void addNewItem(GeoPoint geoPoint, String markerText, String snippet) {
		items.add(new OverlayItem(geoPoint, markerText, snippet));
		populate();
	}

}
