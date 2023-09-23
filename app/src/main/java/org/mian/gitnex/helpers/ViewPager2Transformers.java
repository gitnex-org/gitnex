package org.mian.gitnex.helpers;

import android.view.View;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author M M Arif
 */
public abstract class ViewPager2Transformers {

	public static void returnSelectedTransformer(ViewPager2 viewPager, int selection) {

		if (selection == 0) {
			viewPager.setPageTransformer(new DepthPageTransformer());
		} else if (selection == 1) {
			viewPager.setPageTransformer(new ZoomOutPageTransformer());
		} else if (selection == 2) {
			viewPager.setPageTransformer(null);
		}
	}

	public static class DepthPageTransformer implements ViewPager2.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0f);

			} else if (position <= 0) { // [-1,0]
				// Use the default slide transition when moving to the left page
				view.setAlpha(1f);
				view.setTranslationX(0f);
				view.setScaleX(1f);
				view.setScaleY(1f);

			} else if (position <= 1) { // (0,1]
				// Fade the page out.
				view.setAlpha(1 - position);

				// Counteract the default slide transition
				// view.setTranslationX(pageWidth * -position);

				// Scale the page down (between MIN_SCALE and 1)
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0f);
			}
		}
	}

	public static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0f);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as well.
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1).
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(
						MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0f);
			}
		}
	}
}
