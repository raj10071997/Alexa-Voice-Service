package com.game.dhanraj.myownalexa.NavigationDrawer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.game.dhanraj.myownalexa.AboutActivity;
import com.game.dhanraj.myownalexa.Alarm.MyAlarm;
import com.game.dhanraj.myownalexa.MainActivity;
import com.game.dhanraj.myownalexa.R;
import com.game.dhanraj.myownalexa.sharedpref.Util;

/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationFragment extends Fragment {


    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View containerView;
    private LinearLayout about,login,settings,timersAndAlarm;

    public static final String KEY_USER_LEARNED_DRAWER="user_learned_drawer";

    //this variable indicates that the user is aware of the drawer existence or not
    private boolean mUserLearnedDrawer;
    //indicates whether the drawer is pulled out the first time or the screen is rotated
    private boolean mFromSavedInstanceState;

    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(),KEY_USER_LEARNED_DRAWER,"false"));
        if(savedInstanceState!=null)
        {
            mFromSavedInstanceState=true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_navigation_fragment, container, false);

        about = (LinearLayout) layout.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), AboutActivity.class);
                startActivity(i);
            }
        });

        timersAndAlarm = (LinearLayout) layout.findViewById(R.id.alarmsAndTimer);
        timersAndAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), MyAlarm.class);
                startActivity(i);
            }
        });

        login = (LinearLayout)layout.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),MainActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        settings = (LinearLayout)layout.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: make settings activity
            }
        });

        return layout;
    }

    public void setUp(int fragmentId,DrawerLayout drawerLayout,Toolbar toolbar) {
         containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(mUserLearnedDrawer==false)
                {
                    mUserLearnedDrawer=true;
                    saveToPreferences(getActivity(),KEY_USER_LEARNED_DRAWER, String.valueOf(mUserLearnedDrawer));
                }
//                getActivity().getActionBar().setTitle("dhanraj sahu");
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        if(!mFromSavedInstanceState && !mUserLearnedDrawer)
        {
            mDrawerLayout.openDrawer(containerView);
        }

        //it is deprecated find new method or way
        mDrawerLayout.setDrawerListener(mDrawerToggle);

       // mDrawerToggle.syncState();

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public static void saveToPreferences(Context context,String preferenceName, String preferenceValue)
    {
        SharedPreferences.Editor preferences = Util.getPrefernces(context).edit();
        preferences.putString(preferenceName,preferenceValue);
        preferences.apply();
    }

    public static String readFromPreferences(Context context,String preferenceName, String defaultValue)
    {
        SharedPreferences preferences = Util.getPrefernces(context);
        return preferences.getString(preferenceName,defaultValue);

    }

}
