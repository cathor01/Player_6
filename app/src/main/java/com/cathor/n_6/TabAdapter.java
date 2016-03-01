package com.cathor.n_6;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabAdapter extends FragmentPagerAdapter {

	private static LrcFragment df;

	public static LrcFragment getLrcFragment(){
		return df;
	}

	String[] title = {"Album", "Music","Detail"};
	public TabAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		if (arg0 == 2){
			df = new LrcFragment();
			Bundle bundle = new Bundle();
			bundle.putInt(LrcFragment.DIVIDER_HEIGHT, 2);
			bundle.putFloat(LrcFragment.TEXT_SIZE, 18);
			df.setArguments(bundle);
			return df;
		}
		else if(arg0 == 1){
			MyFragment mf = new MyFragment();
			return mf;
		}
		else{
			AlbumFragment af = new AlbumFragment();
			return af;
		}
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
