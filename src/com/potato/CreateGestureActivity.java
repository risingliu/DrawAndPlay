package com.potato;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.MotionEvent;
import android.gesture.GestureOverlayView;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class CreateGestureActivity extends Activity {
	private static final float LENGTH_THRESHOLD = 120.0f;

	private Gesture mGesture;
	private View mDoneButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_gesture);

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
	 * 增加手势的按钮
	 * @param v
	 */
	public void addGesture(View v) {
		if (mGesture != null) {
			final TextView input = (TextView) findViewById(R.id.gesture_name);
			final CharSequence name = input.getText();
			if (name.length() == 0) {
				input.setError("输入gesture的名字！");
				return;
			}
			
			// 把手势增加到手势库
			final GestureLibrary store = GestureListActivity.getStore();			// 注1
			store.addGesture(name.toString(), mGesture);
			store.save();

			setResult(RESULT_OK);

			final String path = new File(
					Environment.getExternalStorageDirectory(), "gestures")
					.getAbsolutePath();
			Toast.makeText(this, "保存成功" + path, Toast.LENGTH_LONG).show();
		} else {
			setResult(RESULT_CANCELED);
		}

		finish();

	}

	/**
	 * 取消手势
	 * @param v
	 */
	public void cancelGesture(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * 手势监听着
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
			// 获取在GestureOverlayView手势
			mGesture = overlay.getGesture();										// 注2
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
