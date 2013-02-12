/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.adapters;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Based on http://stackoverflow.com/a/10082836/441265
 */
public class TabsAdapter extends FragmentPagerAdapter implements
        ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private final ActionBar actionBar;
    private final ViewPager viewPager;
    private final ArrayList<Fragment> tabs = new ArrayList<Fragment>();

    public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        actionBar = activity.getSupportActionBar();
        viewPager = pager;
        viewPager.setAdapter(this);
        viewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Fragment fragment) {
        tab.setTabListener(this);
        tabs.add(fragment);
        actionBar.addTab(tab);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        Object tag = tab.getTag();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i) == tag) {
                viewPager.setCurrentItem(i);
            }
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}

