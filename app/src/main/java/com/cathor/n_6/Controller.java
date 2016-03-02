package com.cathor.n_6;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import app.minimize.com.seek_bar_compat.SeekBarCompat;


public class Controller extends Fragment {
	private static MusicInfo from;
	private static MusicInfo to;
	private static FloatingActionButton play;
	private static SeekBarCompat seek;
	private static boolean playerHasInited = false;
	private static TextView nowT;
	private static TextView maxT;
	private static Snackbar snackbar;
	private static RelativeLayout relative;
	private static LayoutInflater inflater;
	private static RelativeLayout.LayoutParams initParams;
	private static ValueAnimator vaGo = ValueAnimator.ofFloat(1f, 0f);
	private static ValueAnimator vaCome = ValueAnimator.ofFloat(0f, 1f);
	private static Intent intent;
	private static Handler handler = new Handler();
	private static Runnable runnable = new Runnable() {
		@Override
		public void run() {
			seek.setProgress(MyService.getInstance().getNowPlayTime());
			handler.postDelayed(this, 1000);
		}
	};
	public Controller(FloatingActionButton actionButton){
		play = actionButton;
	}

	public Controller(){}

	public static void initedPlayer(){
		playerHasInited = true;
	}

	private static void changeVisible(){
		int visible = seek.getVisibility();
		if(visible != View.VISIBLE){
			visible = View.INVISIBLE;
		}else{
			visible = View.VISIBLE;
		}
		seek.setVisibility(visible);
		nowT.setVisibility(visible);
		maxT.setVisibility(visible);
	}

	private static String praseTime(int t){
		int sec = t / 1000;
		int M = sec / 60;
		int S = sec % 60;
		String re = "";
		if(M < 10){re += "0" + M + ":";}
		else{
			re += M + ":";
		}
		if(S < 10){return re + "0" + S;}
		return re + S;
	}

	public static void handleMeg(String name, String value){
		intent = new Intent(inflater.getContext(), MyService.class);
		intent.putExtra(name, value);
		inflater.getContext().startService(intent);
	}

	public static void stopSeek(){
		handler.removeCallbacks(runnable);
		seek.setProgress(0);
	}

	public static void initSeek(){
		if(playerHasInited) {
			ValueAnimator va = ValueAnimator.ofInt(seek.getProgress(), 0);
			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					int value = (int) animation.getAnimatedValue();
					seek.setProgress(value);
				}

			});
			Logger.INSTANCE.d("maxvalue:" + MyService.getInstance().getMaxTime());
			va.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					seek.setMax(MyService.getInstance().getMaxTime());
					maxT.setText(praseTime(MyService.getInstance().getMaxTime()));
					Log.e("Seek", "卧槽" + MyService.getInstance().getMaxTime());
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
			va.setDuration(100);
			va.start();
		}
	}

	/**
	 * 将Controller的两个标题给打包
	 * */
	
	private static class MusicInfo{
		RelativeLayout relative;
		TextView title;
		TextView author;
		public MusicInfo(RelativeLayout relative, TextView title, TextView author) {
			// TODO Auto-generated constructor stub
			this.relative = relative;
			this.title = title;
			this.author = author;
		}
		/**
		 * 初始化为不可见状态
		 * */
		private void init(){
			this.relative.setLayoutParams(initParams);
			this.relative.setVisibility(View.INVISIBLE);
			this.relative.setAlpha(0);
		}
		/**
		 * 切换动画（上一曲）
		 * @param titleV 上一曲的title
		 * @param authorV 上一曲的author
		 * */
		
		public static void go(String titleV, String authorV){
			to.title.setText(titleV);
			to.author.setText(authorV);
			vaGo.start();
		}
		
		/**
		 * 切换动画（下一曲）
		 * @param titleV 下一曲的title
		 * @param authorV 下一曲的author
		 * */
		public static void come(String titleV, String authorV){
			to.title.setText(titleV);
			to.author.setText(authorV);
			vaCome.start();
		}
	}
	/***
	 * 所有控件初始化
	 * */
	public static void init(){
		from.title.setText("列表中选择播放");
		from.author.setText("");
		initSeek();
		nowT.setText(praseTime(0));
		maxT.setText(praseTime(0));
		play.setImageResource(R.drawable.play);
	}
	/**
	 * dp转px
	 * */
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.getInstance().scale + 0.5f);
	}
	
	/**
	 *更新所有控件
	 * */
	
	public static void update(){
		MyService _service = MyService.getInstance();
		if(_service.getNowPlay() != -1){
			if(MyFragment.change != 0){
				handler.postDelayed(runnable, 1000);
				String titleV;
				titleV = _service.getItemAt(_service.getNowPlay()).getTitle();
				String authorV = _service.getItemAt(_service.getNowPlay()).getAuthor();
				if(MyFragment.change == 2){
					MusicInfo.go(titleV, authorV);
				}
				else if(MyFragment.change == 1){
					MusicInfo.come(titleV, authorV);
				}
				Log.e("seek", "WTF");
				initSeek();
				MyFragment.change = 0;
			}
			try{
				if(_service.getPlayStatewioutThrow()){
					Logger.INSTANCE.d("Play");
					handler.postDelayed(runnable, 1000);
					play.setImageResource(R.drawable.pause);
				}
				else{
					Logger.INSTANCE.d("Pause");
					handler.removeCallbacks(runnable);
					play.setImageResource(R.drawable.play);
				}
			} catch(IllegalStateException e){
				e.printStackTrace();
				stopSeek();
				Logger.INSTANCE.d("Error Play");
				play.setImageResource(R.drawable.play);
			}

		}
	}
	/***
	 *  点击播放模式后snack
	 * */
	private void toastInfo(){
		switch(MyService.getInstance().getFlag()){
		case 0:
			snackbar = Snackbar.make(play, "顺序播放", Snackbar.LENGTH_SHORT);
			snackbar.show();
			snackbar.setAction("关闭", new OnClickListener() {
				@Override
				public void onClick(View v) {
					snackbar.dismiss();
				}
			});
			break;
		case 1:
			snackbar = Snackbar.make(play, "全部循环", Snackbar.LENGTH_SHORT);
			snackbar.show();
			snackbar.setAction("关闭", new OnClickListener() {
				@Override
				public void onClick(View v) {
					snackbar.dismiss();
				}
			});
			break;
		case 2:
			snackbar = Snackbar.make(play, "单曲循环", Snackbar.LENGTH_SHORT);
			snackbar.show();
			snackbar.setAction("关闭", new OnClickListener() {
				@Override
				public void onClick(View v) {
					snackbar.dismiss();
				}
			});
			break;
		case 3:
			snackbar = Snackbar.make(play, "随机播放", Snackbar.LENGTH_SHORT);
			snackbar.show();
			snackbar.setAction("关闭", new OnClickListener() {
				@Override
				public void onClick(View v) {
					snackbar.dismiss();
				}
			});
		}
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		vaGo.setDuration(500);
		vaGo.removeAllListeners();
		
		vaGo.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				from.relative.setAlpha(value);
				from.relative.setScaleX(value);
				from.relative.setScaleY(value);
				to.relative.setAlpha(1 - value);
				to.relative.setScaleX(1 + value);
				to.relative.setScaleY(1 + value);
			}
		});
		
		vaGo.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				to.relative.setScaleX(2);
				to.relative.setScaleY(2);
				to.relative.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				from.relative.setVisibility(View.INVISIBLE);
				MusicInfo temp = from;
				Logger.INSTANCE.d("From--------->" + from.title.getText());
				Logger.INSTANCE.d("To  --------->" + to.title.getText());
				to.relative.setVisibility(View.VISIBLE);
				from = to;
				to = temp;
				Logger.INSTANCE.d("From--------->" + from.title.getText());
				Logger.INSTANCE.d("To  --------->" + to.title.getText());
				to.init();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		vaCome.setDuration(500);
		vaCome.removeAllListeners();
		vaCome.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float value = (Float)animation.getAnimatedValue();
				from.relative.setAlpha(1 - value);
				from.relative.setScaleX(1 + value);
				from.relative.setScaleY(1 + value);
				to.relative.setAlpha(value);
				to.relative.setScaleX(value);
				to.relative.setScaleY(value);
			}
		});
		
		vaCome.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				to.relative.setScaleX(0);
				to.relative.setScaleY(0);
				to.relative.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				from.relative.setVisibility(View.INVISIBLE);
				MusicInfo temp = from;
				Logger.INSTANCE.d("From--------->" + from.title.getText());
				Logger.INSTANCE.d("To  --------->" + to.title.getText());
				to.relative.setVisibility(View.VISIBLE);
				from = to;
				to = temp;
				Logger.INSTANCE.d("From--------->" + from.title.getText());
				Logger.INSTANCE.d("To  --------->" + to.title.getText());
				to.init();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		inflater = inflate;
		relative = (RelativeLayout)inflater.inflate(R.layout.buttombar, null);
		RelativeLayout musicInfo = (RelativeLayout)relative.findViewById(R.id.name);
		OnTouchListener listener = new OnTouchListener() {
			float pY;
			float pX;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(MyService.getInstance().getNowPlay() != -1){
					if(event.getAction() == MotionEvent.ACTION_DOWN){
						pY = event.getY();
						pX = event.getX();
					}
					if(event.getAction() == MotionEvent.ACTION_UP){
						if(event.getY() >= pY + getPx(20) && (event.getX() - pX <= event.getY() - pY && pX - event.getX() <= event.getY() - pY)){
							MyService.getInstance().moveToLast();
						}
						if(event.getY() <= pY - getPx(20) && (event.getX() - pX <= pY - event.getY() && pX - event.getX() <= pY - event.getY())){
							MyService.getInstance().moveToNext();
						}
						update();
					}
				}
				return false;
			}
		};
		musicInfo.setOnTouchListener(listener);
		TextView title = (TextView)musicInfo.findViewById(R.id.title);
		TextView author = (TextView)musicInfo.findViewById(R.id.author);
		RelativeLayout temp = (RelativeLayout)relative.findViewById(R.id.cscroll);
		seek = (SeekBarCompat)temp.findViewById(R.id.seek);
		nowT = (TextView)temp.findViewById(R.id.nowt);
		nowT.setClickable(false);

		maxT = (TextView)temp.findViewById(R.id.maxt);
		maxT.setClickable(false);
		seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				nowT.setText(praseTime(seekBar.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(MyService.getInstance().getPlayStatewioutThrow()) {
					MyService.getInstance().setTime(seekBar.getProgress());
					Logger.INSTANCE.d("WTF@!");
				}
			}
		});
		from = new MusicInfo(musicInfo, title, author);
		RelativeLayout musicInfo1 = (RelativeLayout)relative.findViewById(R.id.name1);
		musicInfo1.setOnTouchListener(listener);
		initParams = (RelativeLayout.LayoutParams)musicInfo1.getLayoutParams();
		TextView title1 = (TextView)musicInfo1.findViewById(R.id.title1);
		TextView author1 = (TextView)musicInfo1.findViewById(R.id.author1);
		to = new MusicInfo(musicInfo1, title1, author1);
		update();
		play.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				MyService.getInstance().setFlag((MyService.getInstance().getFlag() + 1) % 4);
				update();
				toastInfo();
				return true;
			}
		});
		play.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int t = clickMenu();
				if(t == 0){
					Toast.makeText(inflater.getContext(), "尚未选择歌曲", Toast.LENGTH_SHORT).show();
				}
			}
		});
		return relative;
	}
	public static int clickMenu(){
		MyService _service = MyService.getInstance();
		try{
			if(_service.getPlayStatewioutThrow()){
				Logger.INSTANCE.d("path------->1");
				_service.pause();
				MainActivity.getInstance().updateNotification(1);
				return 2;
			}
			else{
				Logger.INSTANCE.d("path------->2");
				if(_service.getNowPlay() != -1){
					handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
					return 1;
				}else{
					return 0;
				}
			}
		}
		catch(IllegalStateException e){
			if(_service.getNowPlay() != -1){
				Logger.INSTANCE.d("path------->3");
				handleMeg(MyService.PLAY, MyService.PLAY_NO_CHANGE);
				return 1;
			}
			Logger.INSTANCE.d("path------->4");
			return 0;
		}
	}

	/**
	 * 改变控制栏背景颜色
	 * */

	public static void changeColor(int color){
		relative.setBackgroundColor(color);
	}

}