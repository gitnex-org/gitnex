package org.mian.gitnex.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class CreditsActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        TextView creditKasun = findViewById(R.id.creditKasun);
        ImageView closeActivity = findViewById(R.id.close);

        creditKasun.setMovementMethod(LinkMovementMethod.getInstance());

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }
}
