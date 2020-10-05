package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class ClickListener implements View.OnClickListener {

    private String infoText;
    private Context mCtx;

    public ClickListener(String infoText, Context mCtx) {

        this.infoText = infoText;
        this.mCtx = mCtx;
    }

    @Override
    public void onClick(View v) {

        LayoutInflater inflater1 = LayoutInflater.from(mCtx);
        View layout = inflater1.inflate(R.layout.custom_toast_info, v.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.toastText);
        text.setText(infoText);

        Toast toast = new Toast(mCtx.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }

}
