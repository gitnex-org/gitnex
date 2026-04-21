package org.mian.gitnex.views.reactions;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ReactionAuthorsAdapter;
import org.mian.gitnex.databinding.BottomsheetReactionUsersBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class ReactionUsersBottomSheet extends BottomSheetDialogFragment {

	private static final String ARG_EMOJI = "emoji";
	private static final String ARG_USERS = "users";

	private BottomsheetReactionUsersBinding binding;

	public static ReactionUsersBottomSheet newInstance(String emoji, List<User> users) {
		ReactionUsersBottomSheet fragment = new ReactionUsersBottomSheet();
		Bundle args = new Bundle();
		args.putString(ARG_EMOJI, emoji);
		args.putSerializable(ARG_USERS, new ArrayList<>(users));
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetReactionUsersBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String emoji = getArguments() != null ? getArguments().getString(ARG_EMOJI, "👍") : "👍";
		List<User> users = getUsersFromArgs();

		binding.sheetTitle.setText(getString(R.string.reactions_by, emoji));

		ReactionAuthorsAdapter adapter = new ReactionAuthorsAdapter(requireContext(), users);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.recyclerView.setAdapter(adapter);

		if (users.isEmpty()) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}

		binding.btnClose.setOnClickListener(v -> dismiss());
	}

	@SuppressWarnings("unchecked")
	private List<User> getUsersFromArgs() {
		if (getArguments() == null) return new ArrayList<>();
		try {
			return (List<User>) getArguments().getSerializable(ARG_USERS);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
