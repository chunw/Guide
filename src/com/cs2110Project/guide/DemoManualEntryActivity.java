package com.cs2110Project.guide;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class DemoManualEntryActivity extends Activity {
	private EditText latInput, lonInput;
	private ListView listView;

	private String WRITE_FILENAME;
	private String BREAK_CHAR;
	private String MID_CHAR;
	private FileOutputStream fos;
	private FileInputStream fis;
	private Button saveButton, startButton, clearButton;
	private ArrayList<String> locs;
	private ArrayAdapter<String> adapter;
	private Context context;

	private SharedPreferences prefs;

	private boolean ARROW_MODE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		ARROW_MODE = prefs.getBoolean("PREF_ARROW_MODE", false);
		WRITE_FILENAME = getResources().getString(R.string.demoLocationFile);
		BREAK_CHAR = getResources().getString(R.string.IOBreakChar);
		MID_CHAR = getResources().getString(R.string.IOMidChar);

		setContentView(R.layout.demo_manual_entry_layout);
		context = DemoManualEntryActivity.this;
		latInput = (EditText) findViewById(R.id.demoLatEditText);
		lonInput = (EditText) findViewById(R.id.demoLonEditText);
		listView = (ListView) findViewById(R.id.demoListView);

		View header = getLayoutInflater().inflate(R.layout.demo_manual_header,
				null);
		saveButton = (Button) header.findViewById(R.id.demoSaveButton);
		startButton = (Button) header.findViewById(R.id.demoStartButton);
		clearButton = (Button) header.findViewById(R.id.demoClearButton);

		listView.addHeaderView(header);

		locs = readLocs();
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, locs);

		listView.setAdapter(adapter);

		latInput.setText("38.0317");
		lonInput.setText("-78.510779");

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					double lat = Double.parseDouble(latInput.getText()
							.toString());
					double lon = Double.parseDouble(lonInput.getText()
							.toString());
					String text = lat + " " + lon + "\n";
					adapter.insert(text, 0);
					adapter.notifyDataSetChanged();
					try {
						fos = openFileOutput(WRITE_FILENAME,
								Context.MODE_APPEND);
						text = BREAK_CHAR + lat + MID_CHAR + lon + BREAK_CHAR;
						fos.write(text.getBytes());
						fos.close();
					} catch (IOException e) {
					}
				} catch (Exception e) {
				}
			}
		});

		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String title = "Empty the list";
				String message = "Are you sure?";
				String button1String = "No";
				String button2String = "Yes";

				AlertDialog.Builder ad = new AlertDialog.Builder(context);
				ad.setTitle(title);
				ad.setMessage(message);

				ad.setPositiveButton(button1String,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing

							}
						});
				ad.setNegativeButton(button2String,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								adapter.clear();
								listView.postInvalidate();
								context.deleteFile(WRITE_FILENAME);
							}
						});
				ad.setCancelable(true);
				ad.show();
			}

		});

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent;
				if (!ARROW_MODE) {
					myIntent = new Intent(DemoManualEntryActivity.this,
							MainActivity.class);
					myIntent.putExtra("type", 31);
				} else {
					myIntent = new Intent(DemoManualEntryActivity.this,
							ArrowMainActivity.class);
					myIntent.putExtra("type", 31);
				}
				DemoManualEntryActivity.this.startActivity(myIntent);
			}

		});
	}

	public ArrayList<String> readLocs() {
		ArrayList<String> table = new ArrayList<String>();
		String inputString = null;
		try {
			fis = openFileInput(WRITE_FILENAME);
			BufferedReader inputReader = new BufferedReader(
					new InputStreamReader(fis));
			inputString = inputReader.readLine();
			fis.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		String[] splitOne = {};
		if (inputString != null)
			splitOne = inputString.split(BREAK_CHAR);
		for (String s : splitOne) {
			String[] temp = s.split(MID_CHAR);
			if (temp.length > 1) {
				table.add(temp[0] + " " + temp[1]);
			}
		}
		return table;
	}
}
