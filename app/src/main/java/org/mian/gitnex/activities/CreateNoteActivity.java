package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.mian.gitnex.databinding.ActivityCreateNoteBinding;
import io.noties.markwon.Markwon;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.core.spans.BlockQuoteSpan;
import io.noties.markwon.core.spans.CodeSpan;
import io.noties.markwon.core.spans.HeadingSpan;
import io.noties.markwon.core.spans.LinkSpan;

/**
 * @author M M Arif
 */

public class CreateNoteActivity extends BaseActivity {

	private ActivityCreateNoteBinding activityCreateNoteBinding;
	private View.OnClickListener onClickListener;
	private final View.OnClickListener createNoteListener = v -> processNewNote();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateNoteBinding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
		setContentView(activityCreateNoteBinding.getRoot());

		//boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		activityCreateNoteBinding.noteTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(activityCreateNoteBinding.noteTitle, InputMethodManager.SHOW_IMPLICIT);

		/*activityCreateNoteBinding.setOnTouchListener((touchView, motionEvent) -> {

			touchView.getParent().requestDisallowInterceptTouchEvent(true);

			if((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

				touchView.getParent().requestDisallowInterceptTouchEvent(false);
			}
			return false;
		});*/

		initCloseListener();
		activityCreateNoteBinding.close.setOnClickListener(onClickListener);

		//createOrganizationButton = activityCreateOrganizationBinding.createNewOrganizationButton;

		/*if(!connToInternet) {

			activityCreateNoteBinding.createNote.setEnabled(false);
		}
		else {

			activityCreateNoteBinding.createNote.setOnClickListener(createNoteListener);
		}*/
	}

	private void processNewNote() {

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		//activityCreateNoteBinding.createNote.setEnabled(false);
	}

	private void enableProcessButton() {

		//activityCreateNoteBinding.createNote.setEnabled(true);
	}
}
