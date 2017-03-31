package com.ethan.myclub.discover;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethan.myclub.R;
import com.ethan.myclub.main.BaseActivity;
import com.ethan.myclub.main.BaseFragment;
import com.ethan.myclub.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoverFragment extends BaseFragment {


    public DiscoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = onCreateView(inflater, R.layout.fragment_discover, container);
        return view;
    }

    @Override
    public void willBeDisplayed() {
        super.willBeDisplayed();
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity != null) {
            baseActivity.getToolbarWrapper().setTitle("发现").show();
            Utils.StatusBarLightMode(baseActivity, true);
        }
    }
}
