package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.View;
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
	private static final int DIMEN_DOCK_CLEARANCE = R.dimen.dimen12dp; // RV padding for bottom
	private static final int DIMEN_PULL_DISTANCE = R.dimen.dimen48dp; // SwipeRefresh

	// For activities - call in onCreate
	public static void applyEdgeToEdge(
			ComponentActivity activity,
			View dockedToolbar,
			View scrollableView,
			SwipeRefreshLayout swipeRefresh,
			View headerView) {

		EdgeToEdge.enable(activity);
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
		final int baseBottomPadding =
				(int) context.getResources().getDimension(DIMEN_DOCK_CLEARANCE);
		final int pullDistance = (int) context.getResources().getDimension(DIMEN_PULL_DISTANCE);

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
					int bottomP =
							(dockedToolbar != null)
									? (systemBars.bottom + baseBottomPadding)
									: (systemBars.bottom + extraMargin);

					if (headerView != null) {
						headerView.setPadding(
								headerView.getPaddingLeft(),
								systemBars.top + extraMargin,
								headerView.getPaddingRight(),
								headerView.getPaddingBottom());
					}

					if (scrollableView != null) {
						if (scrollableView instanceof androidx.core.widget.NestedScrollView nsv) {
							if (nsv.getChildCount() > 0) {
								nsv.setPadding(
										nsv.getPaddingLeft(), topP, nsv.getPaddingRight(), 0);
								nsv.setClipToPadding(false);

								View innerChild = nsv.getChildAt(0);
								innerChild.setPadding(
										innerChild.getPaddingLeft(),
										innerChild.getPaddingTop(),
										innerChild.getPaddingRight(),
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
