package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Callback;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.util.TinyDB;
import java.util.Locale;
import java.util.Objects;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

/**
 * Author M M Arif
 */

public class ProfileFragment extends Fragment {

    private Context ctx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    	ctx = getContext();

        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());

	    BlurView blurView = v.findViewById(R.id.blurView);
        TextView userFullName = v.findViewById(R.id.userFullName);
	    ImageView userAvatarBackground = v.findViewById(R.id.userAvatarBackground);
        ImageView userAvatar = v.findViewById(R.id.userAvatar);
        TextView userLogin = v.findViewById(R.id.userLogin);
        TextView userLanguage = v.findViewById(R.id.userLanguage);
        ImageView userLanguageIcon = v.findViewById(R.id.userLanguageIcon);

	    ViewGroup aboutFrame = v.findViewById(R.id.aboutFrame);

	    String[] userLanguageCodes = tinyDb.getString("userLang").split("-");
	    Locale locale = new Locale(userLanguageCodes[0], userLanguageCodes[1]);

	    userFullName.setText(tinyDb.getString("userFullname"));
	    userLogin.setText(getString(R.string.usernameWithAt, tinyDb.getString("userLogin")));
	    userLanguage.setText(locale.getDisplayCountry());

	    PicassoService.getInstance(ctx).get()
		    .load(tinyDb.getString("userAvatar"))
		    .transform(new RoundedTransformation(8, 0))
		    .placeholder(R.drawable.loader_animated)
		    .resize(120, 120)
		    .centerCrop().into(userAvatar);

	    PicassoService.getInstance(ctx).get()
		    .load(tinyDb.getString("userAvatar"))
		    .into(userAvatarBackground, new Callback() {

			    @Override
			    public void onSuccess() {

				    int textColor = new ColorInverter().getImageViewContrastColor(userAvatarBackground);

				    userFullName.setTextColor(textColor);
				    userLogin.setTextColor(textColor);
				    userLanguage.setTextColor(textColor);

				    ImageViewCompat.setImageTintList(userLanguageIcon, ColorStateList.valueOf(textColor));

				    blurView.setupWith(aboutFrame)
					    .setBlurAlgorithm(new RenderScriptBlur(ctx))
					    .setBlurRadius(3)
					    .setHasFixedTransformationMatrix(true);

			    }

			    @Override
			    public void onError(Exception e) {}

		    });


        ProfileFragment.SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        ViewPager mViewPager = v.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Typeface myTypeface;

        switch(tinyDb.getInt("customFontId", -1)) {

            case 0:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/roboto.ttf");
                break;

            case 2:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/sourcecodeproregular.ttf");
                break;

            default:
                myTypeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/manroperegular.ttf");
                break;

        }

        TabLayout tabLayout = v.findViewById(R.id.tabs);

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();

        for (int j = 0; j < tabsCount; j++) {

            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();

            for (int i = 0; i < tabChildCount; i++) {

                View tabViewChild = vgTab.getChildAt(i);

                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(myTypeface);
                }
            }
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        return v;

    }

    public static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            switch (position) {

                case 0: // followers
                    return ProfileFollowersFragment.newInstance("repoOwner", "repoName");

                case 1: // following
                    return ProfileFollowingFragment.newInstance("repoOwner", "repoName");

                case 2: // emails
                    return ProfileEmailsFragment.newInstance("repoOwner", "repoName");

            }

            return fragment;

        }

        @Override
        public int getCount() {
            return 3;
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        menu.clear();
        Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.profile_dotted_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                ((MainActivity)ctx).finish();
                return true;

            case R.id.profileMenu:
                BottomSheetProfileFragment bottomSheet = new BottomSheetProfileFragment();
                bottomSheet.show(getChildFragmentManager(), "profileBottomSheet");
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
