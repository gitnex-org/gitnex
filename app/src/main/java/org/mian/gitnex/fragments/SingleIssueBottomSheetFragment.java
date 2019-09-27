package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.activities.AddRemoveAssigneesActivity;
import org.mian.gitnex.activities.AddRemoveLabelsActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.util.TinyDB;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.ClipboardManager;
import android.content.ClipData;

/**
 * Author M M Arif
 */

public class SingleIssueBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.single_issue_bottom_sheet_layout, container, false);

        final TinyDB tinyDB = new TinyDB(getContext());

        TextView replyToIssue = v.findViewById(R.id.replyToIssue);
        TextView editIssue = v.findViewById(R.id.editIssue);
        TextView editLabels = v.findViewById(R.id.editLabels);
        TextView closeIssue = v.findViewById(R.id.closeIssue);
        TextView reOpenIssue = v.findViewById(R.id.reOpenIssue);
        TextView addRemoveAssignees = v.findViewById(R.id.addRemoveAssignees);
        TextView copyIssue = v.findViewById(R.id.copyIssue);

        replyToIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), ReplyToIssueActivity.class));
                dismiss();

            }
        });

        editIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), EditIssueActivity.class));
                dismiss();

            }
        });

        editLabels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), AddRemoveLabelsActivity.class));
                dismiss();

            }
        });

        addRemoveAssignees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), AddRemoveAssigneesActivity.class));
                dismiss();

            }
        });

        copyIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get url of repo
                String repoFullName = tinyDB.getString("repoFullName");
                String instanceUrlWithProtocol = "https://" + tinyDB.getString("instanceUrlRaw");
                if(!tinyDB.getString("instanceUrlWithProtocol").isEmpty()) {
                    instanceUrlWithProtocol = tinyDB.getString("instanceUrlWithProtocol");
                }

                //get issue number
                String issue = tinyDB.getString("issueNumber");

                //combine
                issue = instanceUrlWithProtocol + "/" + repoFullName + "/issues/" + issue;

                // copy to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("issue", issue);
                clipboard.setPrimaryClip(clip);

                dismiss();

            }
        });

        if(tinyDB.getString("issueState").equals("open")) { // close issue

            reOpenIssue.setVisibility(View.GONE);

            closeIssue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    IssueActions.closeReopenIssue(getContext(), Integer.valueOf(tinyDB.getString("issueNumber")), "closed");
                    dismiss();

                }
            });

        }
        else if(tinyDB.getString("issueState").equals("closed")) {

            closeIssue.setVisibility(View.GONE);

            reOpenIssue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    IssueActions.closeReopenIssue(getContext(), Integer.valueOf(tinyDB.getString("issueNumber")), "open");
                    dismiss();

                }
            });

        }

        return v;
    }

}