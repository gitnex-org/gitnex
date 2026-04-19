package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
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

					if (headerView != null) {
						headerView.setPadding(
								headerView.getPaddingLeft(),
								systemBars.top + extraMargin,
								headerView.getPaddingRight(),
								headerView.getPaddingBottom());
					}

					if (scrollableView != null) {
						if (scrollableView
								instanceof
								androidx.core.widget.NestedScrollView nsv) { // NestedScrollView
							runWhenMeasured(
									dockedToolbar,
									() ->
											nsv.post(
													() -> {
														if (nsv.getChildCount() > 0) {
															View child = nsv.getChildAt(0);

															int dockHeight =
																	(dockedToolbar != null)
																			? dockedToolbar
																					.getHeight()
																			: 0;

															int dynamicBottom =
																	systemBars.bottom
																			+ baseBottomPadding
																			+ dockHeight;

															nsv.setPadding(
																	nsv.getPaddingLeft(),
																	topP,
																	nsv.getPaddingRight(),
																	0);

															nsv.setClipToPadding(false);

															child.setPadding(
																	child.getPaddingLeft(),
																	child.getPaddingTop(),
																	child.getPaddingRight(),
																	dynamicBottom);
														}
													}));

						} else if (scrollableView
								instanceof
								androidx.recyclerview.widget.RecyclerView rv) { // RecyclerView
							runWhenMeasured(
									dockedToolbar,
									() ->
											rv.post(
													() -> {
														boolean canScroll =
																rv.canScrollVertically(1);

														int dynamicBottom =
																systemBars.bottom
																		+ baseBottomPadding
																		+ (canScroll
																				? 0
																				: getDockHeight(
																						dockedToolbar));

														rv.setPadding(
																rv.getPaddingLeft(),
																topP,
																rv.getPaddingRight(),
																dynamicBottom);

														rv.setClipToPadding(false);
													}));

						} else { // Fallback
							runWhenMeasured(
									dockedToolbar,
									() ->
											scrollableView.post(
													() -> {
														boolean canScroll =
																scrollableView.canScrollVertically(
																		1);

														int dynamicBottom =
																systemBars.bottom
																		+ baseBottomPadding
																		+ (canScroll
																				? 0
																				: getDockHeight(
																						dockedToolbar));

														scrollableView.setPadding(
																scrollableView.getPaddingLeft(),
																topP,
																scrollableView.getPaddingRight(),
																dynamicBottom);
													}));
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

	private static int getDockHeight(View dockedToolbar) {
		if (dockedToolbar == null) return 0;

		int height = dockedToolbar.getHeight();
		if (height == 0 && dockedToolbar.getLayoutParams() != null) {
			height = dockedToolbar.getLayoutParams().height;
		}
		return Math.max(height, 0);
	}

	private static void runWhenMeasured(View view, Runnable action) {
		if (view == null) {
			action.run();
			return;
		}

		if (view.getHeight() > 0) {
			action.run();
		} else {
			view.getViewTreeObserver()
					.addOnGlobalLayoutListener(
							new ViewTreeObserver.OnGlobalLayoutListener() {
								@Override
								public void onGlobalLayout() {
									if (view.getHeight() > 0) {
										view.getViewTreeObserver()
												.removeOnGlobalLayoutListener(this);
										action.run();
									}
								}
							});
		}
	}
}
