package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoVariable;
import org.mian.gitnex.databinding.ListForgejoActionsVariablesBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class ForgejoActionsVariablesAdapter
		extends RecyclerView.Adapter<ForgejoActionsVariablesAdapter.VariableHolder> {

	private final Context context;
	private List<ForgejoVariable> variables;
	private final OnVariableAction listener;

	public interface OnVariableAction {
		void onDelete(ForgejoVariable variable, int position);
	}

	public ForgejoActionsVariablesAdapter(
			Context context, List<ForgejoVariable> variables, OnVariableAction listener) {
		this.context = context;
		this.variables = variables;
		this.listener = listener;
	}

	@NonNull @Override
	public VariableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new VariableHolder(
				ListForgejoActionsVariablesBinding.inflate(
						LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull VariableHolder holder, int position) {
		holder.bind(variables.get(position), position);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return variables.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ForgejoVariable> newList) {
		this.variables = newList;
		notifyDataSetChanged();
	}

	public class VariableHolder extends RecyclerView.ViewHolder {
		private final ListForgejoActionsVariablesBinding binding;

		VariableHolder(ListForgejoActionsVariablesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(ForgejoVariable variable, int position) {
			binding.variableName.setText(variable.getName());
			binding.variableValue.setText(variable.getData());

			binding.btnCopy.setOnClickListener(
					v -> {
						AppUtil.copyToClipboard(
								context,
								variable.getData(),
								context.getString(R.string.copied_to_clipboard));
					});

			binding.btnDelete.setOnClickListener(
					v -> {
						if (listener != null) listener.onDelete(variable, position);
					});
		}
	}
}
