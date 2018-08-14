package com.ss.localchat.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ss.localchat.fragment.ChatListFragment;
import com.ss.localchat.fragment.DiscoveredUsersFragment;

import java.util.List;

public class ViewPagerFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;

    public ViewPagerFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragmentList = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return ChatListFragment.FRAGMENT_TITLE;
            case 1:
                return DiscoveredUsersFragment.FRAGMENT_TITLE;
            default:
                return null;
        }
    }
}
