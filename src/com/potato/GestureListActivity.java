package com.potato;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GestureListActivity extends Activity {

	private static final int STATUS_SUCCESS = 0;
	private static final int STATUS_CANCELLED = 1;
	private static final int STATUS_NO_STORAGE = 2;
	private static final int STATUS_NOT_LOADED = 3;

	private static final int REQUEST_NEW_GESTURE = 1;

	// 存放手势的文件
	private final File mStoreFile = new File(
			Environment.getExternalStorageDirectory(), "gestures");

	private GesturesAdapter mAdapter;
	private static GestureLibrary mGestureLib;

	private TextView mTvEmpty;
	Button mBtnAddGesture;
	Button mBtnIdentify;

	Resources mRes;
	ListView mListViewGesture;

	// 宽度、高度
	private int mThumbnailSize;
	// 密度
	private int mThumbnailInset;
	// 颜色
	private int mPathColor;

	static GestureLibrary getStore() {
		return mGestureLib;
	}

	/**
	 * 手势排序
	 */
	private final Comparator<NamedGesture> mSorter = new Comparator<NamedGesture>() {			
		public int compare(NamedGesture object1, NamedGesture object2) {
			return object1.name.compareTo(object2.name);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mRes = getResources();

		mBtnAddGesture = (Button) findViewById(R.id.addButton);

		mPathColor = mRes.getColor(R.color.gesture_color);
		mThumbnailInset = (int) mRes
				.getDimension(R.dimen.gesture_thumbnail_inset);
		mThumbnailSize = (int) mRes
				.getDimension(R.dimen.gesture_thumbnail_size);

		mListViewGesture = (ListView) findViewById(android.R.id.list);
		mAdapter = new GesturesAdapter(this);
		mListViewGesture.setAdapter(mAdapter);

		if (mGestureLib == null) {
			mGestureLib = GestureLibraries.fromFile(mStoreFile);
		}
		mTvEmpty = (TextView) findViewById(android.R.id.empty);
		loadGestures();															

		if (mListViewGesture.getAdapter().isEmpty()) {
			mListViewGesture.setEmptyView(mTvEmpty);
			mBtnAddGesture.setEnabled(false);
		} else {
			mBtnAddGesture.setEnabled(true);
		}
	}

	//添加手势后刷新
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_NEW_GESTURE:
				loadGestures();
				break;
			}
		}
	}

	/**
	 * 识别手势
	 */
	public void identifyGestures(View v) {
		Intent intent = new Intent(this, GestureIdentifyActivity.class);
		startActivity(intent);
	}

	/**
	 * 创建手势
	 * @param v
	 */
	public void addGesture(View v) {
		Intent intent = new Intent(this, CreateGestureActivity.class);
		startActivityForResult(intent, REQUEST_NEW_GESTURE);
	}

	/**
	 * 加载手势
	 * @return
	 */
	private int loadGestures() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return STATUS_NO_STORAGE;
		}

		final GestureLibrary gestureLib = mGestureLib;

		mAdapter.clear();
		if (gestureLib.load()) {										// 注2
			for (String name : gestureLib.getGestureEntries()) {

				for (Gesture gesture : gestureLib.getGestures(name)) {
					final Bitmap bitmap = gesture.toBitmap(mThumbnailSize,			// 注3
							mThumbnailSize, mThumbnailInset, mPathColor);
					final NamedGesture namedGesture = new NamedGesture();
					namedGesture.gesture = gesture;
					namedGesture.name = name;

					mAdapter.addBitmap(namedGesture.gesture.getID(), bitmap);
					mAdapter.add(namedGesture);
				}
			}
			mAdapter.sort(mSorter);							// 排序
			mAdapter.notifyDataSetChanged();//更新UI
			return STATUS_SUCCESS;
		}

		return STATUS_NOT_LOADED;
	}

	static class NamedGesture {
		String name;
		Gesture gesture;
	}

	private class GesturesAdapter extends ArrayAdapter<NamedGesture> {
		private final LayoutInflater mInflater;
		private final Map<Long, Drawable> mThumbnails = Collections
				.synchronizedMap(new HashMap<Long, Drawable>());

		public GesturesAdapter(Context context) {
			super(context, 0);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		void addBitmap(Long id, Bitmap bitmap) {
			mThumbnails.put(id, new BitmapDrawable(bitmap));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_gesture,
						parent, false);
			}

			final NamedGesture gesture = getItem(position);
			final TextView label = (TextView) convertView;

			label.setTag(gesture);
			label.setText(gesture.name);
			label.setCompoundDrawablesWithIntrinsicBounds(
					mThumbnails.get(gesture.gesture.getID()), null, null, null);

			return convertView;
		}
	};
}