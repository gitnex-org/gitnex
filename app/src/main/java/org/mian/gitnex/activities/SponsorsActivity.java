package org.mian.gitnex.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class SponsorsActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsors);

        ImageView closeActivity = findViewById(R.id.close);

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
