package com.offsec.nethunter;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.offsec.nethunter.utils.NhPaths;

public class MITMfFragment extends Fragment implements ActionBar.TabListener {

    TabsPagerAdapter TabsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences sharedpreferences;

    NhPaths nh;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public MITMfFragment() {
    }

    public static MITMfFragment newInstance(int sectionNumber) {
        MITMfFragment fragment = new MITMfFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.mitmf, container, false);
        TabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pagerMITMF);
        mViewPager.setAdapter(TabsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
            }
        });
        setHasOptionsMenu(true);
        sharedpreferences = getActivity().getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        return rootView;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    //public static class TabsPagerAdapter extends FragmentPagerAdapter {
    public static class TabsPagerAdapter extends FragmentStatePagerAdapter {


        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MITMfGeneral();
                case 1:
                    return new MITMfResponder();
                default:
                    return new MITMfInject();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Injection";
                case 1:
                    return "Responder";
                default:
                    return "MITMf General";
            }
        }
    }

    public static class MITMfGeneral extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_general, container, false);
            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class MITMfInject extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_inject, container, false);
            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }

    public static class MITMfResponder extends MITMfFragment implements View.OnClickListener {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mitmf_responder, container, false);
            return rootView;
        }

        @Override
        public void onClick(View v) {

        }
    }
}