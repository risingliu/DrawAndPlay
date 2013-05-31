package com.potato;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

public class SongInfoUtils {

	private ArrayList<Song> songList = new ArrayList<Song>();
	private Context mContext;

	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	private static Bitmap mCachedBit = null;

	public SongInfoUtils(Context context) {
		mContext = context;
	}

	public HashMap<String, Object> getSongInfo(String path) {
		HashMap<String, Object> songInfoMap = new HashMap<String, Object>();
		File file = new File(path);
		String fileName = file.getName();
		String filePath = "/mnt" + file.getPath();

		if (file.exists()) {
			if (mContext != null) {
				readDataFromSD();

				for (Song song : songList) {
					if (song.getmFilePath().equals(filePath)
							&& song.getmFileName().equals(fileName)) {
						songInfoMap.put("title", song.getmFileTitle());
						songInfoMap.put("artist", song.getmArtist());
						songInfoMap.put("album", song.getmAlbum());
						songInfoMap.put("_id", song.get_id());
						songInfoMap.put("album_id", song.getAlbum_id());
						break;
					}
				}
			}
		}
		return songInfoMap;
	}

	private void readDataFromSD() {
		Cursor c = mContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.YEAR,
						MediaStore.Audio.Media.MIME_TYPE,
						MediaStore.Audio.Media.SIZE,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.ALBUM_ID},
				MediaStore.Audio.Media.MIME_TYPE + "=? or "
						+ MediaStore.Audio.Media.MIME_TYPE + "=?",
				new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
		if (c.moveToFirst()) {
			Song song = null;
			do {
				song = new Song();
				song.set_id(c.getLong(0));
				song.setmFileName(c.getString(1));// file Name
				song.setmFileTitle(c.getString(2));// song name
				song.setmDuration(c.getInt(3));// play time
				song.setmArtist(c.getString(4));// artist
				song.setmAlbum(c.getString(5));// album
				song.setAlbum_id(c.getLong(10));
				if (c.getString(6) != null) {
					song.setmYear(c.getString(6));
				} else {
					song.setmYear("undefine");
				}
				if ("audio/mpeg".equals(c.getString(7).trim())) {// file type
					song.setmFileType("mp3");
				} else if ("audio/x-ms-wma".equals(c.getString(7).trim())) {
					song.setmFileType("wma");
				}
				if (c.getString(9) != null) {// file path
					song.setmFilePath(c.getString(9));
				}
				songList.add(song);
			} while (c.moveToNext());
			c.close();
		}
	}

	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefault) {
		Bitmap bm = null;
		if (album_id < 0) {
			// This is something that is not in the database, so get the album
			// art directly
			// from the file.
			if (song_id >= 0) {
				bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				bm = BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {
				// The album art thumbnail does not actually exist. Maybe the
				// user deleted it, or
				// maybe it never existed to begin with.
				bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return bm;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid,
			long albumid) {
		Bitmap bm = null;
		byte[] art = null;
		String path = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (FileNotFoundException ex) {

		}
		if (bm != null) {
			mCachedBit = bm;
		}
		return bm;
	}

	private static Bitmap getDefaultArtwork(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.dcover), null, opts);
	}

	 public Bitmap getAlbumart(Long album_id) 
	   {
	        Bitmap bm = null;
	        try 
	        {
	            final Uri sArtworkUri = Uri
	                .parse("content://media/external/audio/albumart");

	            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

	            ParcelFileDescriptor pfd = mContext.getContentResolver()
	                .openFileDescriptor(uri, "r");

	            if (pfd != null) 
	            {
	                FileDescriptor fd = pfd.getFileDescriptor();
	                bm = BitmapFactory.decodeFileDescriptor(fd);
	            }
	    } catch (Exception e) {
	    }
	    return bm;
	}
}
