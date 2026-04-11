package org.mian.gitnex.helpers;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author mmarif
 */
public abstract class ViewPager2Transformers {

	public static void returnSelectedTransformer(ViewPager2 viewPager) {
		viewPager.setPageTransformer(new DepthPageTransformer());
	}

	public static class DepthPageTransformer implements ViewPager2.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(@NonNull View view, float position) {

			if (position < -1) {
				view.setAlpha(0f);

			} else if (position <= 0) {
				view.setAlpha(1f);
				view.setTranslationX(0f);
				view.setScaleX(1f);
				view.setScaleY(1f);

			} else if (position <= 1) {
				view.setAlpha(1 - position);
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

			} else {
				view.setAlpha(0f);
			}
		}
	}
}
