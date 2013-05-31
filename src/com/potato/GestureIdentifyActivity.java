package com.potato;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GestureIdentifyActivity extends Activity {
	public static final String UPDATE_ACTION = "com.potato.action.UPDATE_ACTION";
	public static final String CTRL_ACTION = "com.potato.action.CTRL_ACTION";
	
	public static final String MUSIC_FOLDER = "/sdcard/MUSIC/";
	
	private final int LISTDIALOG = 1;
	
	SQLiteDatabase db;
	ActivityReceiver activityReceiver;
	ArrayList<String> songs;
	int status;
	int index = 0;//当前播放位置
	
	
	private static final float LENGTH_THRESHOLD = 120.0f;

	// 存放手势的文件
	private File mStoreFile = new File(
			Environment.getExternalStorageDirectory(), "gestures");
	// 手势库
	private static GestureLibrary mGestureLib;
	Gesture mGesture;
	
	private static TextView songTitle;
	private static TextView songArtist;
	private static TextView songAlbum;
	private static ImageView album_cover;
	private static ListView playlist;

	 static GestureLibrary getStore() {
	 return mGestureLib;
	 }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_identify);
		
		//SQLite数据库
		db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir().toString() + "/my.db3", null);

		// 手势画板
		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gesture_overlay_view_test);
		
	    songTitle = (TextView) findViewById(R.id.title);
		songArtist = (TextView) findViewById(R.id.artist);
		songAlbum = (TextView) findViewById(R.id.album);
		album_cover = (ImageView) findViewById(R.id.album_cover);
		
		//默认播放列表——所有本地音乐
		songs = getSongList(MUSIC_FOLDER);
		
		// 手势识别的监听器——单笔
		gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
			@Override
			public void onGesturePerformed(GestureOverlayView overlay,
					Gesture gesture) {
				updateGestureLibrary();
				mGesture = gesture;
				if (mGesture.getLength() < LENGTH_THRESHOLD) {
					overlay.clear(false);
				} else if (mGesture != null) {
					// 从手势库中查询匹配的内容，匹配的结果可能包括多个相似的结果，匹配度高的结果放在最前面
					ArrayList<Prediction> predictions = mGestureLib
							.recognize(mGesture);
					if (predictions.size() > 0) {
						Prediction prediction = (Prediction) predictions.get(0);
						// 匹配的手势
						if (prediction.score > 1.0) {
							// play music
							Toast.makeText(GestureIdentifyActivity.this,
									prediction.name, Toast.LENGTH_SHORT).show();

							doWithResult(prediction.name);

							// Intent intent = new Intent();
							// intent.setClass(GestureIdentifyActivity.this,
							// MusicPlayerActivity.class);
							// intent.putExtra("queryWord", prediction.name);
							// startActivity(intent);
						} else {
							Toast.makeText(GestureIdentifyActivity.this,
									R.string.gestureNoExist, Toast.LENGTH_SHORT)
									.show();
						}
					} else {
						Toast.makeText(GestureIdentifyActivity.this,
								R.string.gestureNoExist, Toast.LENGTH_SHORT)
								.show();
					}
				}
			}

		});

		// //多笔监听器
		// gestures.addOnGestureListener(new
		// GestureOverlayView.OnGestureListener() {
		//
		// @Override
		// public void onGestureStarted(GestureOverlayView overlay, MotionEvent
		// event) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onGestureEnded(GestureOverlayView overlay, MotionEvent
		// event) {
		// mGesture = overlay.getGesture();
		// if(mGesture.getLength() < LENGTH_THRESHOLD){
		// overlay.clear(false);
		// }
		// else if (mGesture != null) {
		// // 从手势库中查询匹配的内容，匹配的结果可能包括多个相似的结果，匹配度高的结果放在最前面
		// ArrayList<Prediction> predictions = mGestureLib.recognize(mGesture);
		// if (predictions.size() > 0) {
		// Prediction prediction = (Prediction) predictions.get(0);
		// // 匹配的手势
		// if (prediction.score > 1.0) {
		// // play music
		// Toast.makeText(GestureIdentifyActivity.this,
		// prediction.name, Toast.LENGTH_SHORT).show();
		//
		// Intent intent = new Intent();
		// intent.setClass(GestureIdentifyActivity.this,
		// MusicPlayerActivity.class);
		// intent.putExtra("queryWord", prediction.name);
		// startActivity(intent);
		// } else {
		// Toast.makeText(GestureIdentifyActivity.this,
		// R.string.gestureNoExist, Toast.LENGTH_SHORT).show();
		// }
		// } else {
		// Toast.makeText(GestureIdentifyActivity.this,
		// R.string.gestureNoExist, Toast.LENGTH_SHORT).show();
		// }
		// }
		// }
		//
		// @Override
		// public void onGestureCancelled(GestureOverlayView overlay,
		// MotionEvent event) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onGesture(GestureOverlayView overlay, MotionEvent event)
		// {
		// // TODO Auto-generated method stub
		//
		// }
		// });

		// 从raw中加载已经有的手势库
		// mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);

		if (mGestureLib == null) {
			mGestureLib = GestureLibraries.fromFile(mStoreFile);
		}
		if (!mGestureLib.load()) {
			Log.e("Identify", "load gesture library error!");
			finish();
		}

		//创建IntentFilter,接受Service发送过来的广播消息
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		
		activityReceiver = new ActivityReceiver();
		registerReceiver(activityReceiver, filter);
		
		Intent intent = new Intent(this, MusicService.class);
		intent.putStringArrayListExtra("songs", songs);
		startService(intent);
	}

	private void doWithResult(String name) {
		//先判断此手势是否是playlist手势
		if(getPlayListNames().contains(name)){
			ArrayList<String> heartList = new ArrayList<String>();
			Cursor c = db.rawQuery("select song from heart_list where list_name=?", new String[]{name});
			if(c.moveToFirst()){
				do{
					String song = c.getString(0);
					heartList.add(song);
				}while(c.moveToNext());
			}
			songs = heartList;
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		// 转到手势列表activity
		if (name.equals("drop")) {
			Intent intent = new Intent();
			intent.setClass(GestureIdentifyActivity.this,
					GestureListActivity.class);
			startActivity(intent);
		}
		//播放或暂停
		if(name.equals("play")){
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putExtra("operation", 1);
			sendBroadcast(intent);
		}
		//下一曲
		if(name.equals("nex") || name.equals("next")){
			index++;
			if (index >= songs.size())
				index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		//上一曲
		if(name.equals("pre")){
			index--;
			if (index <= -1)
				index = songs.size() - 1;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		//收藏到播放列表
		if(name.equals("heart")){
			//弹出dialog，选择列表
        	final String[] arr = getPlayListNames2().toArray(new String[]{});
        	
        	final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        	
        	final Dialog dia = new Dialog(this);
        	dia.setContentView(R.layout.dialog_layer);
        	dia.setTitle("请选择播放列表"); 
        	dia.setCancelable(true);
        	
        	playlist =(ListView)dia.findViewById(R.id.playlist);
        	playlist.setAdapter(arrayAdapter);
        	
            //列表项单击事件
        	playlist.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                        long arg3) {
                    insertInPlaylist(arr[arg2]);
                    dia.dismiss();
                }
            });
        	
        	dia.show();
            
//        	//为对话框设置多个列表
//			b.setAdapter(
//				arrayAdapter
//				//为按钮设置监听器
//				, new OnClickListener() 
//				{
//					//该方法的which参数代表用户单击了那个列表项
//					@Override
//					public void onClick(DialogInterface dialog
//						, int which) 
//					{
//						
//					}
//				});
//			//创建、并显示对话框
//			b.create().show();	
//        	
//            DialogInterface.OnClickListener listener =   
//                new DialogInterface.OnClickListener() {  
//                      
//                    @Override  
//                    public void onClick(DialogInterface dialogInterface,   
//                            int which) {  
//                    	insertInPlaylist(arrayAdapter.getItem(which).toString());
//                    	
//                    }  
//                };  
//            b.setAdapter(arrayAdapter, listener);
//            b.create().show();  
		}
//		//播放收藏
//		if(name.equals("memeshe")){
//			ArrayList<String> heartList = new ArrayList<String>();
//			Cursor c = db.rawQuery("select song from heart_list", null);
//			if(c.moveToFirst()){
//				do{
//					String song = c.getString(0);
//					heartList.add(song);
//				}while(c.moveToNext());
//			}
//			songs = heartList;
//			index = 0;
//			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
//			intent.putStringArrayListExtra("songs", songs);
//			intent.putExtra("index", index);
//			sendBroadcast(intent);
//		}
		//进入创建播放列表activity
		if(name.equals("list")){
			Intent intent = new Intent();
			intent.setClass(GestureIdentifyActivity.this,
					CreatePlayListActivity.class);
			startActivity(intent);
//			updateGestureLibrary();
		}
		//音乐推荐
		if(name.equals("happy")){
			songs = getSongList(MUSIC_FOLDER + "happy/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("unhappy")){
			songs = getSongList(MUSIC_FOLDER + "unhappy/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("man")){
			songs = getSongList(MUSIC_FOLDER + "man/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("woman")){
			songs = getSongList(MUSIC_FOLDER + "woman/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("moon")){
			songs = getSongList(MUSIC_FOLDER + "moon/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("peak")){
			songs = getSongList(MUSIC_FOLDER + "peak/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("十八般武艺")){
			songs = getSongList(MUSIC_FOLDER + "18/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("beyonce")){
			songs = getSongList(MUSIC_FOLDER + "beyonce/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("fast")){
			songs = getSongList(MUSIC_FOLDER + "fast/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
		if(name.equals("wave")){
			songs = getSongList(MUSIC_FOLDER + "wave/");
			index = 0;
			Intent intent = new Intent("com.potato.action.CTRL_ACTION");
			intent.putStringArrayListExtra("songs", songs);
			intent.putExtra("index", index);
			sendBroadcast(intent);
		}
	}

	public class ActivityReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			status = intent.getIntExtra("status", -1);
//			int _index = intent.getIntExtra("index", -1);
//			if(_index != index){
//				index = _index;
			updatePlayInfo(index);
//			}
		}
	}
	
	private void updatePlayInfo(int index){
		SongInfoUtils songInfoUtil = new SongInfoUtils(this);
		HashMap<String, Object> songInfo = songInfoUtil.getSongInfo(songs.get(index));
		songTitle.setText(songInfo.get("title").toString());
		songArtist.setText(songInfo.get("artist").toString());
		songAlbum.setText(songInfo.get("album").toString());
		//album cover
		Bitmap bm = songInfoUtil.getAlbumart(Long.parseLong(songInfo.get("album_id").toString()));
        if(bm != null){  
            Log.d("GestureIdentify","bm is not null==========================");  
            album_cover.setImageBitmap(bm);  
        }else{  
            Log.d("GestureIdentify","bm is null============================");  
        } 
		
	}
	
	private ArrayList<String> getSongList(String folderPath){
		ArrayList<String> dList = new ArrayList<String>();
		File[] folder = new File(folderPath).listFiles(); 
		for(File file : folder){
			dList.add(file.getPath());
		}
		return dList;
	}
	
	private ArrayList<String> getPlayListNames(){
		ArrayList<String> nameList = new ArrayList<String>();
		Cursor c = db.rawQuery("select distinct list_name from heart_list", null);
		if(c.moveToFirst()){
			do{
				String song = c.getString(0);
				nameList.add(song);
			}while(c.moveToNext());
		}
		return nameList;
	}
	
	private ArrayList<String> getPlayListNames2(){
		ArrayList<String> nameList = new ArrayList<String>();
		Cursor c = db.rawQuery("select distinct list_name from playlist", null);
		if(c.moveToFirst()){
			do{
				String song = c.getString(0);
				nameList.add(song);
			}while(c.moveToNext());
		}
		return nameList;
	}
	
	private void insertInPlaylist(String list){
		//获取当前播放歌曲的filepath
		String filePath = songs.get(index);
		//查看数据库中是否已经有了			
		try{
			Cursor c = db.rawQuery("select * from heart_list where song = ? and list_name = ?", new String[]{filePath, list});
			if(!c.moveToFirst()){
				db.execSQL("insert into heart_list values(null, ?, ?)", new String[]{filePath, list});
			}
		}
		catch(SQLiteException e){
			db.execSQL("create table heart_list(_id integer primary key autoincrement, song varchar(50), list_name varchar(10))");
			db.execSQL("insert into heart_list values(null, ?, ?)", new String[]{filePath, list});
		}
	}
	
	private void updateGestureLibrary(){
		mStoreFile = new File(
				Environment.getExternalStorageDirectory(), "gestures");
		if (mGestureLib == null) {
			mGestureLib = GestureLibraries.fromFile(mStoreFile);
		}
		if (!mGestureLib.load()) {
			Log.e("Identify", "load gesture library error!");
			finish();
		}
	}
}