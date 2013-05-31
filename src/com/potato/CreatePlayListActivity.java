package com.potato;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CreatePlayListActivity extends Activity {
	private static final float LENGTH_THRESHOLD = 120.0f;

	private Gesture mGesture;
	private View mDoneButton;
	SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//SQLite���ݿ�
		db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/my.db3", null);

		setContentView(R.layout.create_playlist);

		mDoneButton = findViewById(R.id.done);

		GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
		overlay.addOnGestureListener(new GesturesProcessor());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mGesture != null) {
			outState.putParcelable("gesture", mGesture);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mGesture = savedInstanceState.getParcelable("gesture");
		if (mGesture != null) {
			final GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
			overlay.post(new Runnable() {
				public void run() {
					overlay.setGesture(mGesture);
				}
			});

			mDoneButton.setEnabled(true);
		}
	}

	/**
	 * �������Ƶİ�ť
	 * @param v
	 */
	public void addGesture(View v) {
		if (mGesture != null) {
			final TextView input = (TextView) findViewById(R.id.gesture_name);
			final CharSequence name = input.getText();
			if (name.length() == 0) {
				input.setError("����gesture�����֣�");
				return;
			}
			
			// ���������ӵ����ƿ�
			final GestureLibrary store = GestureIdentifyActivity.getStore();			// ע1
			store.addGesture(name.toString(), mGesture);
			store.save();

			setResult(RESULT_OK);
			
			//���б�����ӵ����ݿ�
			try{
				Cursor c = db.rawQuery("select * from playlist where list_name = ?", new String[]{name.toString()});
				if(!c.moveToFirst()){
					db.execSQL("insert into playlist values(null, ?)", new String[]{name.toString()});
				}
			}
			catch(SQLiteException e){
				db.execSQL("create table playlist(_id integer primary key autoincrement, list_name varchar(10))");
				db.execSQL("insert into playlist values(null, ?)", new String[]{name.toString()});
				}

			final String path = new File(
					Environment.getExternalStorageDirectory(), "gestures")
					.getAbsolutePath();
			
			Toast.makeText(this, "����ɹ�" + path, Toast.LENGTH_LONG).show();
		} else {
			setResult(RESULT_CANCELED);
		}

		finish();

	}

	/**
	 * ȡ������
	 * @param v
	 */
	public void cancelGesture(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * ���Ƽ�����
	 * @author Administrator
	 *
	 */
	private class GesturesProcessor implements
			GestureOverlayView.OnGestureListener {
		public void onGestureStarted(GestureOverlayView overlay,
				MotionEvent event) {
			mDoneButton.setEnabled(false);
			mGesture = null;
		}

		public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			// ��ȡ��GestureOverlayView����
			mGesture = overlay.getGesture();										// ע2
			if (mGesture.getLength() < LENGTH_THRESHOLD) {
				overlay.clear(false);
			}
			else{
				mDoneButton.setEnabled(true);
			}
		}

		public void onGestureCancelled(GestureOverlayView overlay,
				MotionEvent event) {
		}
	}
}
