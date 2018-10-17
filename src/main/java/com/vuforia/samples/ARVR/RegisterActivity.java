package com.vuforia.samples.ARVR;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;

public class RegisterActivity extends FragmentActivity{
    public static int SELECT_PICTURE = 1;
    private ViewPager viewPager;
    private RegisterPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewPager = findViewById(R.id.register_viewpager);
        viewPager.setOffscreenPageLimit(2);
        FragmentManager fm = getSupportFragmentManager();

        adapter = new RegisterPagerAdapter(fm);
        viewPager.setAdapter(adapter);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager.setCurrentItem(1, true);
    }

    private class RegisterPagerAdapter extends FragmentPagerAdapter {
        private SparseArray<Fragment> fragments;

        public RegisterPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    fragments.put(position, new RegisterFaceFragment());
                    break;
                case 1:
                    fragments.put(position, new RegisterVoiceFragment());
                    break;
            }

            return fragments.get(position);
        }

        public Fragment getItemAt(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE && data != null) {
                ((RegisterFaceFragment)adapter.getItemAt(0)).onActivityResult(data);
            }
        }
    }}
