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
	int status = 0x11;// δ����״̬
	List<String> songs;
	int index = 0;// ��ǰ���Ÿ������б����λ��

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//��ȡsd��������������ΪĬ���б�
		songs = getDefaultList();
		
		//����MyReceiver
		MyReceiver myReceiver = new MyReceiver();
		//����IntentFilter,����Activity���͹����Ĺ㲥��Ϣ
		IntentFilter filter = new IntentFilter();
		filter.addAction(GestureIdentifyActivity.CTRL_ACTION);
		registerReceiver(myReceiver, filter);
		
		if (mp != null) {
			mp.reset();
			mp.release();
			mp = null;
		}

		mp = new MediaPlayer();

		// ������ɺ��Զ�������һ�׸�
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				index++;
				if (index >= songs.size()) {
					index = 0;
				}
				// ��Activity����Broadcast֪ͨ�ı䵱ǰ������Ϣ
				Intent sendIntent = new Intent(
						MusicPlayerActivity.UPDATE_ACTION);
				sendIntent.putExtra("index", index);
				sendBroadcast(sendIntent);
				// ����ָ������
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

	// ����Activity��Ĳ������������ţ���ͣ����һ�ף���һ��
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<String> tempSongs = intent.getStringArrayListExtra("songs");
			int tempIndex = intent.getIntExtra("index", -1);
			//Ϊ�� ��ʾ�����б�δ��
			if(tempSongs != null){
				songs = tempSongs;
				index = tempIndex;
				playMusic(songs.get(index));
				status = 0x12;
			}
			else{
				if (tempIndex != -1) {
					// �뵱ǰ���Ÿ�����һ��
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
					// ֹͣ״̬
					if (status == 0x11) {
						playMusic(songs.get(index));
						status = 0x12;
					}
					// ����״̬
					else if (status == 0x12) {
						mp.pause();
						status = 0x13;
					}
					// ��ͣ״̬
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
			// ��Activity���͹㲥���ı䵱ǰ����״̬
			Intent sendIntent = new Intent(GestureIdentifyActivity.UPDATE_ACTION);
			sendIntent.putExtra("update", status);
//			sendIntent.putExtra("index", index);
			sendBroadcast(sendIntent);
		}
	}

	// ��������
	private void playMusic(String songPath) {
		try {
			/* ����MediaPlayer */
			mp.reset();
			/* ����Ҫ���ŵ��ļ���·�� */
			mp.setDataSource(songPath);
			/* ׼������ */
			mp.prepare();
			/* ��ʼ���� */
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
