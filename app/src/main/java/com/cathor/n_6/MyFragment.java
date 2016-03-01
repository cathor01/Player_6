package com.cathor.n_6;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyFragment extends Fragment{
	public static int height = 58;
	private static LayoutInflater inflater;
	private static BaseAdapter adapter;
	public static ListView list;
	public static RelativeLayout re;
	private static ArrayList<Music> array;
	private static View view;
	private static String album;
	public static int change = 0;
	private static boolean hasprepared = false;

	
	/***
	 * 向Service发送具体的Message，现状态只可发送播放请求
	 * @param name 请求的名字
	 * @param value 具体值
	 * 
	 * */
	

	
	/**
	 * 将dp转化为px
	 * 
	 * */
	
	private static int getPx(float dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.getInstance().scale + 0.5f);
	}


	private static Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 101:
					array = MyService.getInstance().getList();
					album = MyService.getInstance().getNewAlbum();
					adapter = new BaseAdapter() {

						@SuppressLint("ViewHolder") @Override
						public View getView(final int position, View convertView, ViewGroup parent) {
							// TODO Auto-generated method stub

							RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.musicadapter, null);
							view.setClickable(false);
							view.setId(position + 10000);
							TextView title = (TextView)view.findViewById(R.id.title);
							TextView author = (TextView)view.findViewById(R.id.author);
							view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getPx(height - 4)));
							title.setText(array.get(position).getTitle());
							author.setText(array.get(position).getAuthor());
							view.setClickable(true);
							title.setClickable(false);
							author.setClickable(false);
							view.setOnClickListener(new View.OnClickListener () {//设置每个列表后面的播放功能实现

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if(MyService.getInstance().getNowPlay() == -1){
										MyService.getInstance().setNowPlay(album, v.getId() - 10000);
										Controller.handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
										change = 1;
									}
									else{
										try{
											if(MyService.getInstance().getPlayStatewioutThrow()){
												MyService.getInstance().stop();
												MainActivity.getInstance().updateNotification(2);
											}
										}
										catch(IllegalStateException e){
											Toast.makeText(inflater.getContext(), "请等待", Toast.LENGTH_LONG).show();
											return;
										}

										MyService.getInstance().setNowPlay(album, v.getId() - 10000);
										Controller.handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
										change = 1;

									}
								}
							});
							return view;
						}


						@Override
						public long getItemId(int position) {
							// TODO Auto-generated method stub
							return position;
						}

						@Override
						public Object getItem(int position) {
							// TODO Auto-generated method stub
							return array.get(position);
						}

						@Override
						public int getCount() {
							// TODO Auto-generated method stub
							return array.size();
						}
					};
					list.setAdapter(adapter);
					ViewGroup.LayoutParams params = list.getLayoutParams();
					params.height = getPx(height) * array.size();
					params.width = ViewGroup.LayoutParams.MATCH_PARENT;
					list.setLayoutParams(params);
					break;
			}
		}
	};
	
	public static void updateView(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					if(hasprepared){
						handler.sendEmptyMessage(101);
						break;
					}
					try{
						Thread.sleep(100);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}
	
	@SuppressLint("SdCardPath") @Override
	public View onCreateView(final LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		inflater = inflate;
		array = MyService.getInstance().getList();
		album = MyService.getInstance().getNewAlbum();
		// TODO Auto-generated method stub
		System.out.println("create MediaPlayer");
		view = createView();
		return view;
	}
	
	/***
	 * 暂停当前音乐(不管是否正在播放)
	 * 
	 * */	
	public static void pauseMusic(){
		if(MyService.getInstance().getNowPlay() != -1){
			try{
				if(MyService.getInstance().getPlayStatewioutThrow()){
					System.out.println("path------->1");
					MyService.getInstance().pause();
					MainActivity.getInstance().updateNotification(2);
					Controller.update();
				}
			}
			catch(IllegalStateException e){
				e.printStackTrace();
			}
		}
	}

	private static boolean state = false;
	
	public static void playMusic(boolean di){

		if(!di){state = di;return;}
		if(state) {
			if(MyService.getInstance().getNowPlay() != -1) {
				try {
					Controller.handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
				} catch (Exception e) {
				}
			}
		}
		state = di;
	}



	private static View createView(){
		if(array == null ||array.size() == 0){
			return inflater.inflate(R.layout.lyricinput, null);
		}
		re = new RelativeLayout(inflater.getContext());
		list = new ListView(inflater.getContext());
		re.addView(list);
		System.out.println("Create cursor");
		
		System.out.println("get " + MyService.getInstance().getLength() + " music");
		list.setClickable(false);
		list.setFastScrollEnabled(true);
		list.setFocusable(true);
		list.setVerticalScrollBarEnabled(false);
		adapter = new BaseAdapter() {
			
			@SuppressLint("ViewHolder") @Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				
				RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.musicadapter, null);
				view.setClickable(true);
				view.setId(position + 10000);
				view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getPx(height - 4)));
				TextView title = (TextView)view.findViewById(R.id.title);
				TextView author = (TextView)view.findViewById(R.id.author);
				title.setText(array.get(position).getTitle());
				author.setText(array.get(position).getAuthor());
				title.setClickable(false);
				author.setClickable(false);
				view.setOnClickListener(new View.OnClickListener () {//设置每个列表后面的播放功能实现
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(MyService.getInstance().getNowPlay() == v.getId() - 100){
							if(MyService.getInstance().getPlayStatewioutThrow()){
								MyService.getInstance().pause();
								MainActivity.getInstance().updateNotification(2);
							}
							else{
								Controller.handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
							}
						}
						else if(MyService.getInstance().getNowPlay() == -1){
							MyService.getInstance().setNowPlay(album, v.getId() - 100);
							Controller.handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
							change = 1;
						}
						else{
							try{
								if(MyService.getInstance().getPlayStatewioutThrow()){
									MyService.getInstance().stop();
									MainActivity.getInstance().updateNotification(2);
								}
							}
							catch(IllegalStateException e){
								Toast.makeText(inflater.getContext(), "请等待", Toast.LENGTH_LONG).show();
								return;
							}
							
							MyService.getInstance().setNowPlay(album, v.getId() - 100);
							Controller.handleMeg(MyService.PLAY, MyService.PLAY_CHANGE_RESOURCE);
							change = 1;
							
						}
					}
				});
				return view;
			}
			
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return array.get(position);
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return array.size();
			}
		};
		list.setAdapter(adapter);
		list.setDividerHeight(getPx(4));
		/*ViewGroup.LayoutParams params = list.getLayoutParams();
		params.height = getPx(height) * array.size();
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		list.setLayoutParams(params);*/
		int px = getPx(8);
		re.setPadding(px, 0, px, 0);
		hasprepared = true;
		return re;
	}
	
	/**
	 * Controller按钮点击响应
	 * 
	 * */
	
	/*public static int clickMenu(){
		try{
			if(MyService.getPlayStatewioutThrow()){
				System.out.println("path------->1");
				MyService.pause();
				MainActivity.updateNoti(2);
				return 2;
			}
			else{
				System.out.println("path------->2");
				if(MyService.getNowPlay() != -1){
					handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
					return 1;
				}else{
					return 0;
				}
			}
		}
		catch(IllegalStateException e){
			if(MyService.getNowPlay() != -1){
				System.out.println("path------->3");
				handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
				return 1;
			}
			System.out.println("path------->4");
			return 0;
		}
	}*/
}
