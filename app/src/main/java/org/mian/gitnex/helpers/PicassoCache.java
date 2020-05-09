package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.squareup.picasso.Cache;
import org.mian.gitnex.util.TinyDB;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Author anonTree1417
 */

public class PicassoCache implements Cache {

	private Context ctx;
	private String TAG = "PicassoCache";

	private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
	private static final int COMPRESSION_QUALITY = 50; // 0 = high compression (low file size) | 100 = no compression
	private final int CACHE_SIZE;

	private static final String CACHE_MAP_FILE = "cacheMap";

	private File cachePath;
	private HashMap<String, String> cacheMap;

	public PicassoCache(File cachePath, Context ctx) throws IOException, ClassNotFoundException {

		TinyDB tinyDb = new TinyDB(ctx);

		CACHE_SIZE = FilesData.returnOnlyNumber(tinyDb.getString("cacheSizeImagesStr")) * 1024 * 1024;
		this.cachePath = cachePath;
		cacheMap = new HashMap<>();
		this.ctx = ctx;

		if(cacheMapExists(cachePath)) {

			cacheMap.putAll(loadCacheMap());

		}

	}

	@Override
	public Bitmap get(String key) {

		try {

			if(cacheMap.containsKey(key)) {

				FileInputStream fileInputStream = new FileInputStream(new File(cachePath, cacheMap.get(key)));

				Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
				fileInputStream.close();

				return bitmap;

			}

		}
		catch(IOException e) {

			Log.e(TAG, e.toString());

		}

		return null;

	}

	@Override
	public void set(String key, Bitmap bitmap) {

		try {

			String uuid = generateRandomFilename();
			File file = new File(cachePath, uuid);

			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			bitmap.compress(COMPRESS_FORMAT, COMPRESSION_QUALITY, fileOutputStream);

			fileOutputStream.flush();
			fileOutputStream.close();

			cacheMap.put(key, uuid);
			saveCacheMap(cacheMap);

		}
		catch(IOException e) {

			Log.e(TAG, e.toString());

		}

	}

	@Override
	public int size() {

		int currentSize = 0;

		for(String key : cacheMap.keySet()) {

			currentSize += new File(cachePath, cacheMap.get(key)).length();

		}

		return currentSize;

	}

	@Override
	public int maxSize() {

		return CACHE_SIZE;

	}

	@Override
	public void clear() {

		File[] files = cachePath.listFiles();

		if(files != null) {

			for(File file : files) {

				//noinspection ResultOfMethodCallIgnored
				file.delete();

			}

		}

	}

	@Override
	public void clearKeyUri(String keyPrefix) {

		for(String key : cacheMap.keySet()) {

			int len = Math.min(keyPrefix.length(), key.length());
			boolean match = true;

			for(int i=0; i<len; i++) {

				if(key.charAt(i) != keyPrefix.charAt(i)) {

					match = false;
					break;
				}

			}

			if(match) {

				//noinspection ResultOfMethodCallIgnored
				new File(cachePath, cacheMap.get(key)).delete();
				cacheMap.remove(key);

			}

		}

	}

	private String generateRandomFilename() {

		return UUID.randomUUID().toString();

	}

	private void saveCacheMap(Map<String, String> cacheMap) throws IOException {

		ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(cachePath, CACHE_MAP_FILE), false));

		objectOutputStream.writeObject(cacheMap);
		objectOutputStream.flush();
		objectOutputStream.close();

	}

	private Map<String, String> loadCacheMap() throws IOException, ClassNotFoundException {

		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(cachePath, CACHE_MAP_FILE)));

		Map<String, String> map = (HashMap<String, String>) objectInputStream.readObject();
		objectInputStream.close();

		return map;

	}

	private boolean cacheMapExists(File cachePath) {

		return new File(cachePath, CACHE_MAP_FILE).exists();

	}

}
