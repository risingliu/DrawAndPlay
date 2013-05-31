package com.potato;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

public class MusicPlayerActivity extends ListActivity 
{
	public static final String UPDATE_ACTION = "com.patato.action.UPDATE_ACTION";
	
	ActivityReceiver activityReceiver;
	String[] songs;
	int status;
	int index;//��ǰ����λ��
	
	/* ����������ť */
	private ImageButton	mFrontImageButton	= null;
	private ImageButton	mStopImageButton	= null;
	private ImageButton	mStartImageButton	= null;
	private ImageButton	mPauseImageButton	= null;
	private ImageButton	mNextImageButton	= null;

	/* MediaPlayer���� */
	public MediaPlayer	mMediaPlayer = null;
	
	/* �����б� */
	private List<String> mMusicList = new ArrayList<String>();
	
	/* ��ǰ���Ÿ��������� */
	private int currentListItme = 0;
	
	/* ���ֵ�·�� */
	private static final String MUSIC_PATH = new String("/sdcard/Baidu_music/download/");
	
	/* �ؼ��� */
	private String queryWord = "default";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_player);
		
		Bundle extras = getIntent().getExtras(); 
		queryWord = extras.getString("queryWord"); 
//		/* ������ʾ�����б� */
//		musicList();
		/* ����MediaPlayer���� */
		mMediaPlayer = new MediaPlayer();

		mFrontImageButton = (ImageButton) findViewById(R.id.LastImageButton); 
		mStopImageButton = (ImageButton) findViewById(R.id.StopImageButton);
		mStartImageButton = (ImageButton) findViewById(R.id.StartImageButton); 
		mPauseImageButton = (ImageButton) findViewById(R.id.PauseImageButton);
		mNextImageButton = (ImageButton) findViewById(R.id.NextImageButton); 
		
		//�ж�ǰһ��activity�������Ĺؼ���
		if(queryWord.equals("history")){
			playMusic(MUSIC_PATH + "Glasvegas-Glasvegas-Daddy'S Gone-320.mp3");
		}
		else {
			playMusic(MUSIC_PATH + "Imagine Dragons-Continued Silence-Radioactive-320.mp3");
		}
		
		//ֹͣ��ť
		mStopImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				/* �Ƿ����ڲ��� */
				if (mMediaPlayer.isPlaying())
				{
					//����MediaPlayer����ʼ״̬
					mMediaPlayer.reset();
				}
			}
		}); 
		
		//��ʼ��ť
		mStartImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				playMusic(MUSIC_PATH + mMusicList.get(currentListItme));
			}
		});  
		
		//��ͣ
		mPauseImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			public void onClick(View view)
			{
				if (mMediaPlayer.isPlaying())
				{
					/* ��ͣ */
					mMediaPlayer.pause();
				}
				else 
				{
					/* ��ʼ���� */
					mMediaPlayer.start();
				}
			}
		});
		
		//��һ��
		mNextImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			@Override
			public void onClick(View arg0)
			{
				nextMusic();
			}
		});
		//��һ��
		mFrontImageButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			@Override
			public void onClick(View arg0)
			{
				FrontMusic();
			}
		});
		
		//����IntentFilter,����Service���͹����Ĺ㲥��Ϣ
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		registerReceiver(activityReceiver, filter);
		Intent intent = new Intent(this, MusicService.class);
		startService(intent);
	}
	
	public class ActivityReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			status = intent.getIntExtra("status", -1);
			int _index = intent.getIntExtra("index", -1);
			if(_index != index){
				updatePlayInfo(index);
			}
		}
	}
	
	private void updatePlayInfo(int index){
		
	}
	
	/*<----------------------------------------------------------------->*/
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ( keyCode ==  KeyEvent.KEYCODE_BACK)
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	/* �����ǵ���б�ʱ�����ű���������� */
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		currentListItme = position;
		playMusic(MUSIC_PATH + mMusicList.get(position));
	}


//	/* �����б� */
//	public void musicList()
//	{
//		//ȡ��ָ��λ�õ��ļ�������ʾ�������б�
//		File home = new File(MUSIC_PATH);
//		if (home.listFiles(new MusicFilter()).length > 0)
//		{
//			for (File file : home.listFiles(new MusicFilter()))
//			{
//				mMusicList.add(file.getName());
//			}
//			ArrayAdapter<String> musicList = new ArrayAdapter<String>(MusicPlayerActivity.this,R.layout.musicitme, mMusicList);
//			setListAdapter(musicList);
//		}
//		else{
//			Log.e("Music", "Music file not exists");
//		}
//	}
	
	private void playMusic(String path)
	{
		try
		{
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			/* ����Ҫ���ŵ��ļ���·�� */
			mMediaPlayer.setDataSource(path);
			/* ׼������ */
			mMediaPlayer.prepare();
			/* ��ʼ���� */
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() 
			{
				public void onCompletion(MediaPlayer arg0)
				{
					//�������һ��֮�������һ��
					nextMusic();
				}
			});
		}catch (IOException e){}
	}

	/* ��һ�� */
	private void nextMusic()
	{
		if (++currentListItme >= mMusicList.size())
		{
			currentListItme = 0;
		}
		else
		{
			playMusic(MUSIC_PATH + mMusicList.get(currentListItme));
		}
	}
	
	/* ��һ�� */
	private void FrontMusic()
	{
		if (--currentListItme >= 0)
		{
			currentListItme = mMusicList.size();
		}
		else
		{
			playMusic(MUSIC_PATH + mMusicList.get(currentListItme));
		}
	}

}

/* �����ļ����� */
class MusicFilter implements FilenameFilter
{
	public boolean accept(File dir, String name)
	{
		//���ﻹ��������������ʽ�������ļ�
		return (name.endsWith(".mp3"));
	}
}

