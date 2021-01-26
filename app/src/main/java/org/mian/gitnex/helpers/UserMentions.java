package org.mian.gitnex.helpers;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.res.ResourcesCompat;
import org.mian.gitnex.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author M M Arif
 */

public class UserMentions {

    public static Spannable UserMentionsFunc(Context mCtx, CharSequence bodyWithMD) {

        Spannable bodyWithMentions = new SpannableString(bodyWithMD);
        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(bodyWithMD);

        while (matcher.find())
        {

            int indexStart = String.valueOf(bodyWithMD).indexOf(matcher.group());
            int indexEnd = indexStart + matcher.group().length();
            bodyWithMentions.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(mCtx.getResources(), R.color.colorDarkGreen, null)), indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        return bodyWithMentions;

    }

}
