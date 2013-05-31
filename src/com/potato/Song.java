package com.potato;

public class Song {
	private String mFileName = "";
	
	private String mFileTitle = "";
	
	private long _id = 0;
	
	private long album_id = 0;
	
	private int mDuration = 0;
	
	private String mArtist = "";
	
	private String mAlbum = "";
	
	private String mYear = "";
	
	private String mFileType = "";
	
	private String mFilePath = "";

	public String getmFileName() {
		return mFileName;
	}

	public void setmFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	public String getmFileTitle() {
		return mFileTitle;
	}

	public void setmFileTitle(String mFileTitle) {
		this.mFileTitle = mFileTitle;
	}

	public int getmDuration() {
		return mDuration;
	}

	public void setmDuration(int mDuration) {
		this.mDuration = mDuration;
	}

	public String getmArtist() {
		return mArtist;
	}

	public void setmArtist(String mArtist) {
		this.mArtist = mArtist;
	}

	public String getmAlbum() {
		return mAlbum;
	}

	public void setmAlbum(String mAlbum) {
		this.mAlbum = mAlbum;
	}

	public String getmYear() {
		return mYear;
	}

	public void setmYear(String mYear) {
		this.mYear = mYear;
	}

	public String getmFileType() {
		return mFileType;
	}

	public void setmFileType(String mFileType) {
		this.mFileType = mFileType;
	}

	public String getmFilePath() {
		return mFilePath;
	}

	public void setmFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public long getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(long album_id) {
		this.album_id = album_id;
	}
}