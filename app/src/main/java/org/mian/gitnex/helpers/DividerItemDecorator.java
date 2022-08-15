package org.mian.gitnex.helpers;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author M M Arif
 */

public class DividerItemDecorator extends RecyclerView.ItemDecoration {

	private final Drawable rvDivider;

	public DividerItemDecorator(Drawable divider) {
		rvDivider = divider;
	}

	@Override
	public void onDrawOver(@NonNull Canvas canvas, RecyclerView parent, @NonNull RecyclerView.State state) {

		int dividerLeft = parent.getPaddingLeft();
		int dividerRight = parent.getWidth() - parent.getPaddingRight();

		int childCount = parent.getChildCount();
		for(int i = 0; i <= childCount - 2; i++) {
			View child = parent.getChildAt(i);

			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

			int dividerTop = child.getBottom() + params.bottomMargin;
			int dividerBottom = dividerTop + rvDivider.getIntrinsicHeight();

			rvDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
			rvDivider.draw(canvas);
		}
	}

}
