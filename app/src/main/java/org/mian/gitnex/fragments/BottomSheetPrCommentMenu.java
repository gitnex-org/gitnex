package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomsheetPrCommentMenuBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BottomSheetPrCommentMenu extends BottomSheetDialogFragment {

	private BottomsheetPrCommentMenuBinding binding;
	private long commentId;
	private String commentBody;
	private String htmlUrl;
	private String commentAuthor;
	private String currentUser;
	private CommentMenuListener listener;

	public interface CommentMenuListener {
		void onEditComment(long commentId, String body);

		void onDeleteComment(long commentId);

		void onQuoteReply(long commentId, String body);

		void onCopyUrl(String url);

		void onShareComment(String body, String url);

		void onOpenInBrowser(String url);
	}

	public static BottomSheetPrCommentMenu newInstance(
			long commentId, String body, String htmlUrl, String commentAuthor, String currentUser) {
		BottomSheetPrCommentMenu sheet = new BottomSheetPrCommentMenu();
		Bundle args = new Bundle();
		args.putLong("comment_id", commentId);
		args.putString("comment_body", body);
		args.putString("comment_html_url", htmlUrl);
		args.putString("comment_author", commentAuthor);
		args.putString("current_user", currentUser);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetPrCommentMenuBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			commentId = args.getLong("comment_id");
			commentBody = args.getString("comment_body");
			htmlUrl = args.getString("comment_html_url");
			commentAuthor = args.getString("comment_author");
			currentUser = args.getString("current_user");
		}

		boolean isAuthor = currentUser != null && currentUser.equalsIgnoreCase(commentAuthor);
		if (!isAuthor) {
			binding.editCommentCard.setVisibility(View.GONE);
			binding.deleteCommentCard.setVisibility(View.GONE);
		}

		binding.editComment.setOnClickListener(
				v -> {
					if (listener != null) listener.onEditComment(commentId, commentBody);
					dismiss();
				});

		binding.deleteComment.setOnClickListener(
				v -> {
					if (listener != null) listener.onDeleteComment(commentId);
					dismiss();
				});

		binding.quoteReply.setOnClickListener(
				v -> {
					if (listener != null) listener.onQuoteReply(commentId, commentBody);
					dismiss();
				});

		binding.copyUrl.setOnClickListener(
				v -> {
					if (listener != null) listener.onCopyUrl(htmlUrl);
					dismiss();
				});

		binding.shareComment.setOnClickListener(
				v -> {
					if (listener != null) listener.onShareComment(commentBody, htmlUrl);
					dismiss();
				});

		binding.openBrowser.setOnClickListener(
				v -> {
					if (listener != null) listener.onOpenInBrowser(htmlUrl);
					dismiss();
				});
	}

	public void setCommentMenuListener(CommentMenuListener listener) {
		this.listener = listener;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applySheetStyle(dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
