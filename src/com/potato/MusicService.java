package com.potato;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;

public class MusicService extends Service {
	public static final String MUSIC_FOLDER = "/sdcard/MUSIC/";
	
	MyReceiver myReceiver;
	private MediaPlayer mp = null;
	private static final int MUSIC_PLAY = 1;
//	private static final int MUSIC_NEXT = 2;
//	private static final int MUSIC_PRE = 3;
	int status = 0x11;// 未播放状态
	List<String> songs;
	int index = 0;// 当前播放歌曲在列表里的位置

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//获取sd卡的所有音乐作为默认列表
		songs = getDefaultList();
		
		//创建MyReceiver
		MyReceiver myReceiver = new MyReceiver();
		//创建IntentFilter,接受Activity发送过来的广播消息
		IntentFilter filter = new IntentFilter();
		filter.addAction(GestureIdentifyActivity.CTRL_ACTION);
		registerReceiver(myReceiver, filter);
		
		if (mp != null) {
			mp.reset();
			mp.release();
			mp = null;
		}

		mp = new MediaPlayer();

		// 播放完成后自动播放下一首歌
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				index++;
				if (index >= songs.size()) {
					index = 0;
				}
				// 向Activity发送Broadcast通知改变当前播放信息
				Intent sendIntent = new Intent(
						MusicPlayerActivity.UPDATE_ACTION);
				sendIntent.putExtra("index", index);
				sendBroadcast(sendIntent);
				// 播放指定音乐
				playMusic(songs.get(index));
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mp != null) {
			mp.stop();
			mp = null;
		}
	}

	// 定义Activity里的操作，包括播放，暂停，上一首，下一首
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<String> tempSongs = intent.getStringArrayListExtra("songs");
			int tempIndex = intent.getIntExtra("index", -1);
			//为空 表示播放列表未变
			if(tempSongs != null){
				songs = tempSongs;
				index = tempIndex;
				playMusic(songs.get(index));
				status = 0x12;
			}
			else{
				if (tempIndex != -1) {
					// 与当前播放歌曲不一致
					if (tempIndex != index) {
						index = tempIndex;
						playMusic(songs.get(index));
						status = 0x12;
					}
				}
			}

			int operation = intent.getIntExtra("operation", -1);
			if (operation != -1) {
				switch (operation) {
				case MUSIC_PLAY:
					// 停止状态
					if (status == 0x11) {
						playMusic(songs.get(index));
						status = 0x12;
					}
					// 播放状态
					else if (status == 0x12) {
						mp.pause();
						status = 0x13;
					}
					// 暂停状态
					else {
						mp.start();
						status = 0x12;
					}
					break;
//				case MUSIC_NEXT:
//					playMusic(songs.get(index));
//					break;
//				case MUSIC_PRE:
//					playMusic(songs.get(index));
//					break;
				}
			}
			// 向Activity发送广播，改变当前播放状态
			Intent sendIntent = new Intent(GestureIdentifyActivity.UPDATE_ACTION);
			sendIntent.putExtra("update", status);
//			sendIntent.putExtra("index", index);
			sendBroadcast(sendIntent);
		}
	}

	// 播放音乐
	private void playMusic(String songPath) {
		try {
			/* 重置MediaPlayer */
			mp.reset();
			/* 设置要播放的文件的路径 */
			mp.setDataSource(songPath);
			/* 准备播放 */
			mp.prepare();
			/* 开始播放 */
			mp.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getDefaultList(){
		ArrayList<String> dList = new ArrayList<String>();
		File[] folder = new File(MUSIC_FOLDER).listFiles(); 
		for(File file : folder){
			dList.add(file.getPath());
		}
		return dList;
	}

}
