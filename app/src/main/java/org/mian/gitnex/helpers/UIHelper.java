package org.mian.gitnex.helpers;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsetsController;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;

/**
 * @author mmarif
 */
public class UIHelper {

	private static final int DIMEN_EXTRA_MARGIN =
			R.dimen.dimen12dp; // Top/Bottom spacing (fragments)
	private static final int DIMEN_PULL_DISTANCE = R.dimen.dimen48dp; // SwipeRefresh

	// For activities - call in onCreate
	public static void applyEdgeToEdge(
			ComponentActivity activity,
			View dockedToolbar,
			View scrollableView,
			SwipeRefreshLayout swipeRefresh,
			View headerView) {

		EdgeToEdge.enable(activity);

		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(R.attr.isDark, typedValue, true);
		boolean isDark = typedValue.data != 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowInsetsController controller = activity.getWindow().getInsetsController();
			if (controller != null) {
				controller.setSystemBarsAppearance(
						isDark ? 0 : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
						WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
			}
		}

		applyInsets(
				activity.findViewById(android.R.id.content),
				dockedToolbar,
				scrollableView,
				swipeRefresh,
				headerView);
	}

	// For activities and fragments
	public static void applyInsets(
			View rootView,
			View dockedToolbar,
			View scrollableView,
			SwipeRefreshLayout swipeRefresh,
			View headerView) {

		final Context context = rootView.getContext();
		final int extraMargin = (int) context.getResources().getDimension(DIMEN_EXTRA_MARGIN);
		final int pullDistance = (int) context.getResources().getDimension(DIMEN_PULL_DISTANCE);

		final int staticBottomClearance =
				(int) (72 * context.getResources().getDisplayMetrics().density);

		ViewCompat.setOnApplyWindowInsetsListener(
				rootView,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(
									WindowInsetsCompat.Type.systemBars()
											| WindowInsetsCompat.Type.ime());

					int topP =
							(headerView == null)
									? (systemBars.top + extraMargin)
									: (scrollableView != null ? scrollableView.getPaddingTop() : 0);

					int bottomP = systemBars.bottom + staticBottomClearance;

					if (headerView != null) {
						headerView.setPadding(
								headerView.getPaddingLeft(),
								systemBars.top + extraMargin,
								headerView.getPaddingRight(),
								headerView.getPaddingBottom());
					}

					if (scrollableView != null) {
						if (scrollableView instanceof android.view.ViewGroup group) {
							group.setClipToPadding(false);
						}

						if (scrollableView instanceof androidx.core.widget.NestedScrollView nsv) {
							nsv.setPadding(nsv.getPaddingLeft(), topP, nsv.getPaddingRight(), 0);
							if (nsv.getChildCount() > 0) {
								View child = nsv.getChildAt(0);
								child.setPadding(
										child.getPaddingLeft(),
										child.getPaddingTop(),
										child.getPaddingRight(),
										bottomP);
							}
						} else {
							scrollableView.setPadding(
									scrollableView.getPaddingLeft(),
									topP,
									scrollableView.getPaddingRight(),
									bottomP);
						}
					}

					if (swipeRefresh != null) {
						int start = systemBars.top;
						int end = start + pullDistance;
						swipeRefresh.setProgressViewOffset(false, start, end);
					}

					if (dockedToolbar != null) {
						if (dockedToolbar.getLayoutParams()
								instanceof CoordinatorLayout.LayoutParams params) {
							params.bottomMargin = systemBars.bottom + extraMargin;
							dockedToolbar.setLayoutParams(params);
						}
					}

					return windowInsets;
				});
	}
}
