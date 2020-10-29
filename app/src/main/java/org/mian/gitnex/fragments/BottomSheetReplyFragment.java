package org.mian.gitnex.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.ActionResult;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.Objects;

/**
 * @author opyale
 */

public class BottomSheetReplyFragment extends BottomSheetDialogFragment {

	private TinyDB tinyDB;
	private DraftsApi draftsApi;

	private int repositoryId;
	private int currentActiveAccountId;
	private int issueNumber;
	private long draftId;

	private TextView draftsHint;

	@Override
	public void onAttach(@NonNull Context context) {

		tinyDB = new TinyDB(context);
		draftsApi = new DraftsApi(context);

		repositoryId = (int) tinyDB.getLong("repositoryId", 0);
		currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
		issueNumber = Integer.parseInt(tinyDB.getString("issueNumber"));

		super.onAttach(context);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.bottom_sheet_reply_layout, container, false);
		Bundle arguments = requireArguments();

		draftsHint = view.findViewById(R.id.drafts_hint);

		EditText commentContent = view.findViewById(R.id.comment);
		TextView toolbarTitle = view.findViewById(R.id.toolbar_title);
		ImageButton close = view.findViewById(R.id.close);
		ImageButton drafts = view.findViewById(R.id.drafts);
		ImageButton send = view.findViewById(R.id.send);

		send.setEnabled(false);

		if(Objects.equals(arguments.getString("commentAction"), "edit")) {

			send.setVisibility(View.GONE);
		}

		if(arguments.getString("draftId") != null) {

			draftId = Long.parseLong(arguments.getString("draftId"));
		}

		if(!tinyDB.getString("issueTitle").isEmpty()) {

			toolbarTitle.setText(tinyDB.getString("issueTitle"));
		}
		else if(arguments.getString("draftTitle") != null) {

			toolbarTitle.setText(arguments.getString("draftTitle"));
		}

		if(arguments.getString("commentBody") != null) {

			send.setEnabled(true);
			send.setAlpha(1f);

			commentContent.setText(arguments.getString("commentBody"));

			if(arguments.getBoolean("cursorToEnd", false)) {

				commentContent.setSelection(commentContent.length());
			}
		}

		commentContent.requestFocus();
		commentContent.setOnTouchListener((v, event) -> {

			BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());

			switch(event.getAction()) {

				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_SCROLL:
					bottomSheetBehavior.setDraggable(false);
					break;

				default:
					bottomSheetBehavior.setDraggable(true);
			}

			return false;

		});

		commentContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				String text = commentContent.getText().toString();

				if(text.isEmpty()) {

					send.setEnabled(false);
					send.setAlpha(0.5f);
					saveDraft(null, true);
				}
				else {

					send.setEnabled(true);
					send.setAlpha(1f);
					saveDraft(text, false);
				}
			}

			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		close.setOnClickListener(v -> dismiss());

		drafts.setOnClickListener(v -> {

			Intent intent = new Intent(getContext(), MainActivity.class);
			intent.putExtra("launchFragment", "drafts");
			startActivity(intent);

			dismiss();
		});

		send.setOnClickListener(v -> IssueActions
			.reply(getContext(), commentContent.getText().toString(), issueNumber)
			.accept((status, result) -> {

				if(status == ActionResult.Status.SUCCESS) {

					Toasty.success(getContext(), getString(R.string.commentSuccess));

					tinyDB.putBoolean("commentPosted", true);
					tinyDB.putBoolean("resumeIssues", true);
					tinyDB.putBoolean("resumePullRequests", true);

					if(draftId != 0 && tinyDB.getBoolean("draftsCommentsDeletionEnabled")) {

						draftsApi.deleteSingleDraft((int) draftId);
					}

					dismiss();
				}
				else {

					Toasty.error(getContext(), getString(R.string.commentError));
					dismiss();
				}
			}));

		return view;
	}

	private void saveDraft(String text, boolean remove) {

		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
		valueAnimator.setDuration(500);
		valueAnimator.addUpdateListener(animation -> {

			float value = (Float) animation.getAnimatedValue();

			if(value == 0f)  {
				draftsHint.setVisibility((remove) ? View.GONE : View.VISIBLE);
			}

			draftsHint.setAlpha(value);
		});

		if(remove) {

			draftsApi.deleteSingleDraft((int) draftId);
			draftId = 0;

			valueAnimator.reverse();
		}
		else {

			if(draftId == 0) {
				draftId = draftsApi.insertDraft(repositoryId, currentActiveAccountId, issueNumber, text, StaticGlobalVariables.draftTypeComment, "TODO");
			} else {
				DraftsApi.updateDraft(text, (int) draftId, "TODO");
			}

			draftsHint.setVisibility(View.VISIBLE);
			valueAnimator.start();
		}
	}

	public static BottomSheetReplyFragment newInstance(Bundle bundle) {

		BottomSheetReplyFragment fragment = new BottomSheetReplyFragment();
		fragment.setArguments(bundle);

		return fragment;
	}
}
