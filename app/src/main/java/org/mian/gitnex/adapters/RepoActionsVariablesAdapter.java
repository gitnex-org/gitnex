package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.ActionVariable;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListActionVariablesBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class RepoActionsVariablesAdapter
		extends RecyclerView.Adapter<RepoActionsVariablesAdapter.ViewHolder> {

	private List<ActionVariable> variables;
	private final Context context;
	private final OnVariableActionListener listener;

	public interface OnVariableActionListener {
		void onDeleteClick(ActionVariable variable, int position);
	}

	public RepoActionsVariablesAdapter(
			Context context, List<ActionVariable> variables, OnVariableActionListener listener) {
		this.context = context;
		this.variables = variables;
		this.listener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListActionVariablesBinding binding =
				ListActionVariablesBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		ActionVariable variable = variables.get(position);
		holder.bind(variable, position);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return variables != null ? variables.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ActionVariable> newList) {
		this.variables = newList;
		notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		final ListActionVariablesBinding binding;

		ViewHolder(ListActionVariablesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(ActionVariable variable, int position) {
			binding.variableName.setText(
					variable.getName() != null
							? variable.getName()
							: context.getString(R.string.na));

			String value = variable.getData() != null ? variable.getData() : "";
			binding.variableData.setText(value.isEmpty() ? context.getString(R.string.na) : value);

			boolean hasDescription =
					variable.getDescription() != null && !variable.getDescription().isEmpty();
			if (hasDescription) {
				binding.variableDescription.setVisibility(View.VISIBLE);
				binding.variableDescription.setText(variable.getDescription());
			} else {
				binding.variableDescription.setVisibility(View.GONE);
			}

			binding.btnCopy.setOnClickListener(
					v -> {
						if (!value.isEmpty()) {
							AppUtil.copyToClipboard(
									context,
									value,
									context.getString(R.string.copied_to_clipboard));
						}
					});

			binding.btnDelete.setOnClickListener(
					v -> {
						if (listener != null) {
							listener.onDeleteClick(variable, position);
						}
					});
		}
	}
}
