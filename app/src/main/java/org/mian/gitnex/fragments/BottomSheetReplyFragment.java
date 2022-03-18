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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.ActionResult;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.databinding.BottomSheetReplyLayoutBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import java.util.Objects;

/**
 * @author opyale
 */

public class BottomSheetReplyFragment extends BottomSheetDialogFragment {

	private enum Mode { EDIT, SEND }
	private Mode mode = Mode.SEND;

	private TinyDB tinyDB;
	private DraftsApi draftsApi;

	private int currentActiveAccountId;
	private IssueContext issue;
	private long draftId;

	private Runnable onInteractedListener;
	private TextView draftsHint;

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		tinyDB = TinyDB.getInstance(context);
		draftsApi = BaseApi.getInstance(context, DraftsApi.class);

		currentActiveAccountId = ((BaseActivity) requireActivity()).getAccount().getAccount().getAccountId();
		issue = IssueContext.fromBundle(requireArguments());
	}

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetReplyLayoutBinding bottomSheetReplyLayoutBinding = BottomSheetReplyLayoutBinding.inflate(inflater, container, false);
		Bundle arguments = requireArguments();

		draftsHint = bottomSheetReplyLayoutBinding.draftsHint;

		EditText comment = bottomSheetReplyLayoutBinding.comment;
		TextView toolbarTitle = bottomSheetReplyLayoutBinding.toolbarTitle;
		ImageButton close = bottomSheetReplyLayoutBinding.close;
		ImageButton drafts = bottomSheetReplyLayoutBinding.drafts;
		ImageButton send = bottomSheetReplyLayoutBinding.send;

		send.setEnabled(false);

		if(Objects.equals(arguments.getString("commentAction"), "edit") &&
			arguments.getString("draftId") == null) {

			send.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_save));
			mode = Mode.EDIT;
		}

		if(arguments.getString("draftId") != null) {

			draftId = Long.parseLong(arguments.getString("draftId"));
		}

		if(issue.getIssue() != null && !issue.getIssue().getTitle().isEmpty()) {

			toolbarTitle.setText(EmojiParser.parseToUnicode(issue.getIssue().getTitle()));
		}
		else if(arguments.getString("draftTitle") != null) {

			toolbarTitle.setText(arguments.getString("draftTitle"));
		}

		if(arguments.getString("commentBody") != null) {

			send.setEnabled(true);
			send.setAlpha(1f);

			comment.setText(arguments.getString("commentBody"));

			if(arguments.getBoolean("cursorToEnd", false)) {

				comment.setSelection(comment.length());
			}
		}

		comment.requestFocus();
		comment.setOnTouchListener((v, event) -> {

			BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetReplyLayoutBinding.getRoot().getParent());

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

		comment.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				String text = comment.getText().toString();

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

		send.setOnClickListener(v -> {

			if(mode == Mode.SEND) {

				IssueActions
					.reply(getContext(), comment.getText().toString(), issue)
					.accept((status, result) -> {

						if(status == ActionResult.Status.SUCCESS) {

							FragmentActivity activity = requireActivity();
							if(activity instanceof IssueDetailActivity) {
								((IssueDetailActivity) activity).commentPosted = true;
							}

							Toasty.success(getContext(), getString(R.string.commentSuccess));

							if(draftId != 0 && tinyDB.getBoolean("draftsCommentsDeletionEnabled", true)) {
								draftsApi.deleteSingleDraft((int) draftId);
							}

							if(onInteractedListener != null) {
								onInteractedListener.run();
							}
						}
						else {

							Toasty.error(getContext(), getString(R.string.genericError));
						}

						dismiss();

					});
			} else {

				IssueActions
					.edit(getContext(), comment.getText().toString(), arguments.getInt("commentId"), issue)
					.accept((status, result) -> {

						FragmentActivity activity = requireActivity();
						if(activity instanceof IssueDetailActivity) {
							((IssueDetailActivity) activity).commentEdited = true;
						}

						if(status == ActionResult.Status.SUCCESS) {

							if(draftId != 0 && tinyDB.getBoolean("draftsCommentsDeletionEnabled", true)) {
								draftsApi.deleteSingleDraft((int) draftId);
							}

							if(onInteractedListener != null) {
								onInteractedListener.run();
							}
						}
						else {

							Toasty.error(getContext(), getString(R.string.genericError));
						}

						dismiss();

					});
			}
		});

		return bottomSheetReplyLayoutBinding.getRoot();
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

			String draftType;
			if(issue.getIssueType().equalsIgnoreCase("Issue")) {

				draftType = Constants.draftTypeIssue;
			}
			else if(issue.getIssueType().equalsIgnoreCase("Pull")) {

				draftType = Constants.draftTypePull;
			}
			else {

				draftType = "";
			}

			if(draftId == 0) {

				draftId = draftsApi.insertDraft(issue.getRepository().getRepositoryId(), currentActiveAccountId, issue.getIssueIndex(), text, draftType, "TODO", issue.getIssueType());
			}
			else {

				draftsApi.updateDraft(text, (int) draftId, "TODO");
			}

			draftsHint.setVisibility(View.VISIBLE);
			valueAnimator.start();
		}
	}

	public static BottomSheetReplyFragment newInstance(Bundle bundle, IssueContext issue) {

		BottomSheetReplyFragment fragment = new BottomSheetReplyFragment();
		bundle.putSerializable(IssueContext.INTENT_EXTRA, issue);
		fragment.setArguments(bundle);

		return fragment;
	}

	public void setOnInteractedListener(Runnable onInteractedListener) {

		this.onInteractedListener = onInteractedListener;
	}

}
