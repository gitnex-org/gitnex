package org.mian.gitnex.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Author M M Arif
 */

public class Images {

	public static Bitmap scaleImage(byte[] imageData, int maxSizeWidth, int maxSizeScaledWidth) {

		Bitmap scaledImage;
		Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		int orgWidth = image.getWidth();
		int orgHeight = image.getHeight();

		if(orgWidth > maxSizeWidth) {

			int aspectRatio = orgWidth / orgHeight;
			int scaledHeight = maxSizeScaledWidth * aspectRatio;
			scaledImage = Bitmap.createScaledBitmap(image, maxSizeScaledWidth, scaledHeight, false);
		}
		else {

			scaledImage = Bitmap.createScaledBitmap(image, orgWidth, orgHeight, false);
		}

		return scaledImage;
	}
}
