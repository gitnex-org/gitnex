package org.mian.gitnex.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author M M Arif
 */

public class Images {

	public static Bitmap scaleImage(byte[] imageData, int sizeLimit) {

		Bitmap original = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
		if(original == null) {
			return null; // something went wrong
		}

		if(original.getHeight() > sizeLimit && original.getWidth() <= original.getHeight()) {

			double reductionPercentage = (double) sizeLimit / original.getHeight();

			Bitmap scaled = Bitmap.createScaledBitmap(original, (int) (reductionPercentage * original.getWidth()), sizeLimit, false);
			original.recycle();

			return scaled;

		}
		else if(original.getWidth() > sizeLimit && original.getHeight() < original.getWidth()) {

			double reductionPercentage = (double) sizeLimit / original.getWidth();

			Bitmap scaled = Bitmap.createScaledBitmap(original, sizeLimit, (int) (reductionPercentage * original.getHeight()), false);
			original.recycle();

			return scaled;

		}

		// Image size does not exceed bounds.
		return original;

	}

}
