package com.cathor.n_6;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.animation.ValueAnimator.ofFloat;
import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.createChooser;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Color.rgb;
import static android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT;
import static android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP;
import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;
import static android.media.AudioManager.ACTION_HEADSET_PLUG;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static android.support.v7.graphics.Palette.Builder;
import static android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED;
import static android.util.Log.v;
import static android.view.LayoutInflater.from;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.cathor.n_6.Controller.init;
import static com.cathor.n_6.FastBlur.fastblur;
import static com.cathor.n_6.LrcSelect.setArray;
import static com.cathor.n_6.MainActivity.FileUtils.getPath;
import static com.cathor.n_6.MyFragment.pauseMusic;
import static com.cathor.n_6.MyFragment.playMusic;
import static com.cathor.n_6.TabAdapter.getLrcFragment;
import static com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.out;


public class MainActivity extends AppCompatActivity {
	String filePath;
	public String data;
	public InputStream stream;
	private static MainActivity activity;
	public LayoutInflater inflater;
	private Bitmap bitmap = null;
	private Bitmap nB = null;
	private ViewPager mview;
	private int width;
	private int height;
	private ImageView background;
	private int counter = 1;
	public NotificationManager nm;
	private final static int FILE_SELECT = 101;
	private final static int LRC_INPUT = 102;
	private final static int LRC_SELECT = 103;
	private final static int OPINION_ENTER = 104;
	public float scale;
	private Intent service;
	private ValueAnimator va;
	private RemoteViews remote;
	private FragmentManager fm;
	private MyFragment fragment;
	private Controller control;
	private Toolbar toolbar;
	private Notification notifi;
	private final static int initColor = 0xff7744ff;
	private final static int initDarkerColor = 0xff4f2DAA;
	private int pColor;
	private int pDarkerColor;
	private static final int rX = 16 * 16 * 16 * 16;
	private static final int gX = 16 * 16;
	private ComponentName mComponentName;
	private AudioManager mAudioManager;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private SystemBarTintManager tintManager;
	private NavigationView list;

	public static Map<String, ArrayList<Music>> album_list;
	public static Map<String, ArrayList<Music>> author_list;
	public static Map<String, ArrayList<Music>> folder_list;
	public static Map<String, ArrayList<Music>> play_list;
	private FloatingActionButton actionBar;
	public static int nowType = 0;

	private static final int ALBUM_LIST = 0;
	private static final int AUTHOR_LSIT = 1;
	private static final int FOLDER_LIST = 2;
	private static final int PLAY_LIST = 3;


	public static Map<String, ArrayList<Music>> getNowPlay() {
		switch (nowType) {
			case ALBUM_LIST:
				return album_list;
			case AUTHOR_LSIT:
				return author_list;
			case FOLDER_LIST:
				return folder_list;
			case PLAY_LIST:
				return play_list;
			default:
				return null;
		}
	}

	public static MainActivity getInstance() {
		return activity;
	}

	/**
	 * 推送Notification
	 * （尚未完成）
	 *
	 * @param flag 播放或否
	 * @author 瑞凯
	 */
	public void updateNotification(int flag) {

	}

	/***
	 * 注册各种监听
	 */

	private MediaButtonReceiver mediaButtonReceiver = new MediaButtonReceiver();

	public final static String B_PHONE_STATE = ACTION_PHONE_STATE_CHANGED;

	private PhoneReceiver receiver = new PhoneReceiver();

	private void registerHeadsetPlugReceiver() {
		IntentFilter intentFilter = new IntentFilter(ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(headsetPlugReceiver, intentFilter);
		IntentFilter intentFilter1 = new IntentFilter(ACTION_HEADSET_PLUG);
		registerReceiver(headsetPlugReceiver, intentFilter1);
		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(B_PHONE_STATE);
		intentFilter2.setPriority(MAX_VALUE);
		registerReceiver(receiver, intentFilter2);

		mAudioManager.registerMediaButtonEventReceiver(mComponentName);
	}

	/**
	 * 获取系统耳机插入、拔出广播
	 */

	private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
				pauseMusic();
				playMusic(false);
			} else if (ACTION_HEADSET_PLUG.equals(action)) {
				out.println("plug_isn");
				playMusic(true);
			}
		}
	};


	/**
	 * Handler处理
	 * <p/>
	 * 101： 抛弃
	 * 102： 抛弃
	 * 104： 处理获取的图像（缩小到合适大小， 获取平均颜色， 高斯模糊）
	 * 201： 抛弃
	 * 202： 设置背景图像
	 * 301： 获取音乐列表
	 * 302：
	 * 304:  更新播放的类型
	 * 1101： 改变应用颜色
	 */

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			/*case 101:
				FragmentTransaction ft = fm.beginTransaction();
				android.app.Fragment f = fm.findFragmentByTag("frag");
				if(f != null){
					ft.remove(f);
				}
				ft.commit();
				try{
					MyService.stop();
				}
				catch(IllegalStateException e){
					e.printStackTrace();
				}
				MyService.setNowPlay(-1);
				Controller.init();
				updateNoti(11);
				MyFragment.dropData();
				handler.sendEmptyMessage(102);
				break;
			case 102:
				fragment = new MyFragment();
				FragmentTransaction ft1 = fm.beginTransaction();
				Fragment f1 = fm.findFragmentByTag("frag");
				if(f1 != null){
					ft1.remove(f1);
				}
				ft1.replace(R.id.frag, fragment, "frag");
				ft1.commit();
				
				break;
				*/
				case 104:
                    final Music music_f = (Music)msg.getData().getSerializable("music");
					Thread thread1 = new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							BitmapFactory.Options options = new BitmapFactory.Options();

							options.inJustDecodeBounds = false;

							options.inSampleSize = 2;   // width，hight设为原来的十分一

                            Bitmap origin = BitmapFactory.decodeStream(getInstance().stream, null, options);
                            int left = 0;
                            int top = 0;
                            int height = origin.getHeight();
                            int width = origin.getWidth();
                            boolean flag = false;
                            if (origin.getWidth() > 1024) {
                                left = origin.getWidth() / 2 - 512;
                                width = 1024;
                                flag = true;
                            }
                            if (origin.getHeight() > 1024) {
                                top = origin.getHeight() / 2 - 512;
                                height = 1024;
                                flag = true;
                            }
                            if(flag){
                                origin = Bitmap.createBitmap(origin, left, top, width, height, null, false);
                            }
                            int[] colors = getInstance().changeColor(origin);
                            if(music_f != null){
                                music_f.setColor(colors[0]);
                                music_f.setColor_darker(colors[1]);
                            }
                            //changeColor(color);
                            if(getInstance().bitmap != null && !getInstance().bitmap.isRecycled()){
                                getInstance().bitmap.recycle();
                            }
                            getInstance().bitmap = blur(origin);
                            //nB = zoomBitmap(bitmap);
                            origin.recycle();
                            origin = null;
                            try {
								getInstance().stream.close();
								getInstance().stream = null;
							}catch (IOException e){
								e.printStackTrace();
							}
                            Message msg = new Message();
                            msg.what = 202;
                            Bundle data = new Bundle();
                            if(music_f != null){
                                data.putSerializable("music", music_f);
                            }
                            msg.setData(data);
							handler.sendMessage(msg);
						}
					});
					thread1.start();
					break;
				case 105:
					Music music = (Music)msg.getData().getSerializable("music");
					changeColor((int)music.getColor(), (int)music.getColor_darker());
					Bitmap old_img =  ((BitmapDrawable) getInstance().background.getDrawable()).getBitmap();
					BitmapFactory.Options options_ = new BitmapFactory.Options();
					options_.inJustDecodeBounds = false;
					options_.inSampleSize = 1;   // width，hight设为原来的十分一
					getInstance().background.setImageBitmap(BitmapFactory.decodeStream(getInstance().stream, null, options_));

					if(old_img !=  null){
						old_img.recycle();
						old_img = null;
					}
					System.gc();
					break;
				case 201:
					getInstance().toolbar.getMenu().findItem(R.id.refresh).setVisible(true); //当音乐列表界面出现后方可刷新
					break;
				case 202:
					//remote.setImageViewBitmap(R.id.album, nB);
                    Music music1 = (Music)msg.getData().getSerializable("music");
					getInstance().updateNotification(2);
					Bitmap oldBitmap =  ((BitmapDrawable) getInstance().background.getDrawable()).getBitmap();
                    FreedomLoadTask task = new FreedomLoadTask(music1);
                    task.execute(getInstance().bitmap);
					getInstance().background.setImageBitmap(getInstance().bitmap);
					if(oldBitmap !=  null){
						oldBitmap.recycle();
						oldBitmap = null;
					}
					System.gc();
					break;
				case 203:
					getInstance().toolbar.getMenu().findItem(R.id.lrc).setVisible(true);
				case 302:
					AlbumFragment.getInstance().updateView();
					MyFragment.updateView();
					init();
					break;
				case 304:
                    AlbumFragment.getInstance().updateView();
					MyFragment.updateView();
					break;
				case 1101:
					Bundle data = msg.getData();
					getInstance().changeColor(data.getInt("color"), data.getInt("darker"));
					break;
			}
		}

	};

	/**
	 * 设置toolbar的副标题
	 */
	public void setSubTitle(String title) {
		getInstance().toolbar.setSubtitle(title);
	}

	/**
	 * 跳转到音乐列表
	 */
	public void jumpToMusic() {
		getInstance().mview.setCurrentItem(1, true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getActionBar().hide();

		scale = MainActivity.this.getResources().getDisplayMetrics().density;
		service = new Intent(this, MyService.class); //音乐服务
		startService(service);

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		// AudioManager注册一个MediaButton对象
		mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());


		fm = getSupportFragmentManager();
		if (fragment == null) {
			fragment = new MyFragment(); //手动单例。。。。。。
		}
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Point size = new Point();
		wm.getDefaultDisplay().getSize(size);
		width = size.x;
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //通知栏，，，，做的还有问题
		va = ofFloat(0f, 1f); //某个蛋疼功能
		va.setDuration(1000);
		super.onCreate(savedInstanceState);
		inflater = this.getLayoutInflater();
		activity = this;
		registerHeadsetPlugReceiver();
		View outer = from(this).inflate(R.layout.activity_main, null);
		setContentView(outer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		final PercentRelativeLayout main = (PercentRelativeLayout) findViewById(R.id.main_percent);
		background = (ImageView) main.findViewById(R.id.backImg);
		height = background.getHeight();
		mview = (ViewPager) main.findViewById(R.id.id_pager);
		mview.setAdapter(new TabAdapter(fm));
		mview.setOffscreenPageLimit(3);
		mview.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setSubTitle("主列表");
                        break;
                    case 1:
                        setSubTitle("音乐列表");
                        break;
                    case 2:
                        setSubTitle("同步歌词");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
		/*notifi = new Notification(R.mipmap.ic_launcher, "开始音乐", currentTimeMillis());  弃用的Notification
		notifi.flags = FLAG_NO_CLEAR;
		//RelativeLayout notiV = (RelativeLayout) inflater.inflate(notification, null);
		remote = new RemoteViews(getPackageName(), R.layout.notification);
		notifi.contentView = remote;
		remote.setTextViewText(R.id.ntitle, "No Media");
		remote.setImageViewResource(R.id.npause, R.drawable.play);
		remote.setOnClickPendingIntent(R.id.rmain, getActivity(activity, 0, getIntent(), FLAG_CANCEL_CURRENT));
		nm.notify(111, notifi);*/
		actionBar = (FloatingActionButton) main.findViewById(R.id.start);
		if (control == null) { //手动单例。。。。。。
			control = new Controller(actionBar);
		}
		FragmentTransaction ftt = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag("cont");
		if (f != null) {
			ftt.remove(f);
		}
		ftt.replace(R.id.controller, control, "cont");
		ftt.commit();
		AnimatorUpdateListener listener = new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				float alpha = (Float) animation.getAnimatedValue();
				main.setAlpha(alpha);
			}
		};
		va.addUpdateListener(listener);
		toolbar = (Toolbar) drawerLayout.findViewById(R.id.main_percent).findViewById(R.id.tool); //Toolbar android.support.v7.Toolbar 使用
		//toolbar.getMenu().getItem(3).setVisible(false);
		toolbar.setTitle("SimplePlayer");
		toolbar.setBackgroundColor(initColor);
		toolbar.setTitleTextColor(0xffffffff);
		toolbar.setSubtitleTextColor(0xffffffff);
		//toolbar.setLogo(R.mipmap.ic_launcher);
		//setSubTitle("专辑列表");
		pColor = initColor;
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (SDK_INT == KITKAT || SDK_INT == KITKAT_WATCH) {
			tintManager = new SystemBarTintManager(this);  //使用了第三方的库
			tintManager.setStatusBarTintEnabled(true);  // 设置可沉浸
			tintManager.setStatusBarTintResource(R.color.systemBar_color);  // 背景和Toolbar颜色相同
			tintManager.setNavigationBarTintEnabled(true);
			tintManager.setNavigationBarTintResource(R.color.systemBar_color);
			SystemBarConfig config = tintManager.getConfig();
			outer.setPadding(0, config.getPixelInsetTop(true), 0, config.getPixelInsetBottom());  //　不然一块儿白
		}

		mDrawerToggle = new ActionBarDrawerToggle(this,
				drawerLayout,
				toolbar,
                R.string.drawer_open,  // 自定义的，不是很确定用处
				R.string.drawer_close  // 同上
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {

				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {

				invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
			}
		};
		list = (NavigationView) drawerLayout.findViewById(R.id.list_left);
		if (list != null) {
			list.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					//切换相应 Fragment 等操作
					menuItem.setChecked(true);
					if (menuItem.getTitle().equals("专辑")) {
						nowType = ALBUM_LIST;
					} else if (menuItem.getTitle().equals("歌手")) {
						nowType = AUTHOR_LSIT;
					} else if (menuItem.getTitle().equals("文件夹")) {
						nowType = FOLDER_LIST;
					} else if (menuItem.getTitle().equals("播放列表")) {
						nowType = PLAY_LIST;
					}
					drawerLayout.closeDrawers();
					handler.sendEmptyMessage(304);
					MyService.getInstance().updateMarray(getNowPlay());
					return false;
				}
			});
		}
		toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				int id = item.getItemId();
				switch (id) {
					case R.id.click:
						va.start();
						return false;
					case R.id.select:
						Intent tIntent = new Intent(ACTION_GET_CONTENT);
						tIntent.setType("image/*");
						tIntent.addCategory(CATEGORY_OPENABLE);
						try {
							startActivityForResult(createChooser(tIntent, "选择一个应用"), FILE_SELECT);
						} catch (ActivityNotFoundException ex) {
							makeText(MainActivity.this, "请安装一个图片浏览器", LENGTH_SHORT).show();
						}
						return false;
					case R.id.lrc:
						showLrcDialog();
						break;
					case R.id.opinion:
						showOpinion();
						break;
				}

				return false;
			}
		});

		mDrawerToggle.syncState();
		drawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageLoader.getInstance().clearDiskCache();
		ImageLoader.getInstance().clearMemoryCache();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false).create();
			dialog.show();
			Window window = dialog.getWindow();
			window.setContentView(R.layout.exit_alert);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		//handler.sendEmptyMessage(302);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
			case FILE_SELECT:
				if (resultCode == RESULT_OK) {
					Logger.INSTANCE.d("requestCode = [" + requestCode + "], data = [" + data + "]");
					Uri uri = data.getData();
					filePath = getPath(this, uri);
					try{
                        MyImageLoader.ValueType type = new MyImageLoader.ValueType(true, filePath);
						setBackGround(type, null);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				break;
			case LRC_INPUT:
				if (data != null) {
					Bundle bundle = data.getExtras();
					List<LrcItem> lists = (List<LrcItem>) bundle.getSerializable("lrc");
					setArray(lists);
					Intent intent = new Intent(MainActivity.this, LrcSelect.class);
					startActivityForResult(intent, LRC_SELECT);
				}
				break;
			case LRC_SELECT:
				break;
			case OPINION_ENTER:
				break;
		}

	}

	public void toastErrorInfo(String name) {
		makeText(activity, name + "获取音乐信息失败", LENGTH_SHORT).show();
	}

	/**
	 * 展示歌词输入框
	 */
	public void showLrcDialog() {
		if (MyService.getInstance().getNowPlay() == -1) {
			makeText(activity, "尚未选择歌曲╮(╯▽╰)╭", LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(activity, LrcInput.class);
		activity.startActivityForResult(intent, LRC_INPUT);
	}

	private void showOpinion() {
		Intent intent = new Intent(activity, MediaController.class);
		Bundle bundle = new Bundle();
		bundle.putLong("Color", pColor);
		intent.putExtras(bundle);
		startActivityForResult(intent, OPINION_ENTER);
	}

	/**
	 * 设置背景图片
	 *
	 * @param valuetype 图片输入流
	 */
	public void setBackGround(MyImageLoader.ValueType valuetype, Music music) {
        Message msg = new Message();
        Bundle data = new Bundle();
		if(music != null) {
			data.putSerializable("music", music);
			if(!music.getImg_path().equals("")){
				msg.what = 105;
			}
			else{
				msg.what = 104;
			}
		}
		else{
			msg.what = 104;
		}
        msg.setData(data);
		try {
			if (valuetype.getType()) {
				this.stream = new FileInputStream((String)valuetype.getValue());
			} else {
				this.stream = new FileInputStream((FileDescriptor) valuetype.getValue());
			}
			handler.sendMessage(msg);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	private void updateClick(int i) {
		v("click", "click" + i);
		if (i != getInstance().nowType) {
			getInstance().nowType = i;
			MyService.getInstance().updateMarray(getInstance().getNowPlay());
			handler.sendEmptyMessage(304);
		}
	}

	/**
	 * 改变应用整体颜色
	 * 自带获取图像主体颜色功能
	 */

	public int[] changeColor(Bitmap bitmap) {
		Builder builder = Palette.from(bitmap);

		Palette palette = builder.generate();

		int color = palette.getMutedColor(initColor);

		int r = red(color);

		int g = green(color);

		int b = blue(color);

		int darker = rgb(r / 5 * 4, g / 5 * 4, b / 5 * 4);

		Bundle bundle = new Bundle();
		bundle.putInt("color", color);
		bundle.putInt("darker", darker);
		Message msg = new Message();
		msg.setData(bundle);
		msg.what = 1101;
		handler.sendMessage(msg);
        return new int[]{color, darker};
	}

	public void changeColor(int mcolor, int mdarker) {


		final int from = pColor;
		int to = mcolor; // new color to animate to

		int r = 255 - red(to) < 60 ? 255 - red(to) : 60;
		int g = 255 - green(to) < 60 ? 255 - green(to) : 60;
		int b = 255 - blue(to) < 60 ? 255 - blue(to) : 60;

		final int rd = red(to) - red(from);
		final int gd = green(to) - green(from);
		final int bd = blue(to) - blue(from);

		final int fromD = pDarkerColor;

		final int rdd = red(mdarker) - red(fromD);
		final int gdd = green(mdarker) - green(fromD);
		final int bdd = blue(mdarker) - blue(fromD);

		getLrcFragment().setColor(rgb(r, g, b));
		ValueAnimator colorAnimation = ofFloat(0, 1);
		colorAnimation.setDuration(500);
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				float percent = (Float) animator.getAnimatedValue();

				int red = red(from) + (int) (rd * percent);
				int green = green(from) + (int) (gd * percent);
				int blue = blue(from) + (int) (bd * percent);
				int color = rgb(red, green, blue);
				int darker = rgb(red(fromD) + (int) (rdd * percent), green(fromD) + (int) (gdd * percent), blue(fromD) + (int) (bdd * percent));
				toolbar.setBackgroundColor(color);

				Controller.changeColor(color);
				int[] colors = new int[]{darker};
				int[][] states = new int[1][];
				states[0] = new int[]{};
				actionBar.setBackgroundTintList(new ColorStateList(states, colors));
				if (SDK_INT == 19 || SDK_INT == 20) {
					tintManager.setStatusBarTintColor(color);
					tintManager.setNavigationBarTintColor(color);
				}
				if (SDK_INT >= LOLLIPOP) {
					GradientDrawable drawable = new GradientDrawable();
					activity.getWindow().setNavigationBarColor(color);

					Window window = activity.getWindow();

					// clear FLAG_TRANSLUCENT_STATUS flag:
					window.clearFlags(FLAG_TRANSLUCENT_STATUS);

					// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
					window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

					// finally change the color

					window.setStatusBarColor(darker);

					drawable.setOrientation(BOTTOM_TOP);
					drawable.setColors(new int[]{color, darker});
					drawable.setGradientType(LINEAR_GRADIENT);
					list.setBackground(drawable);
				} else {
					list.setBackgroundColor(color);
				}
			}
		});
		colorAnimation.start();
		pColor = mcolor;
		pDarkerColor = mdarker;
	}

	/**
	 * 初始化背景图，在未找到album的情况下调用
	 */

	public void initBackGround() {
		background.setImageResource(R.drawable.top);
		changeColor(initColor, initDarkerColor);
		pColor = initColor;
		//remote.setImageViewResource(R.id.album, R.drawable.bg);
		updateNotification(2);
		nB = null;
		bitmap = null;
	}

	/**
	 * 高斯模糊处理
	 */

	@TargetApi(JELLY_BEAN_MR1)
	private static Bitmap blur(Bitmap bkg) {
		int radius = 10;
		Bitmap bitmap;
		if (bkg.getHeight() > 50 || bkg.getWidth() > 50) {
			Bitmap temp = small(bkg);
			bitmap = fastblur(temp, 4, radius);
			temp.recycle();
			temp = null;
		} else {
			bitmap = fastblur(bkg, 4, radius);
		}
		return bitmap;
	}

	/**
	 * 使bitmap放大到原来的16倍
	 */

	private static Bitmap big(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(4f, 4f); //长和宽放大缩小的比例
		Bitmap resizeBmp = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	/**
	 * 使bitmap缩小到原大小的1/16
	 */

	private static Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		float width = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
		matrix.postScale(50 / width, 50 / width); //长和宽放大缩小的比例
		return createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		//fm.beginTransaction().replace(R.id.controller, new Controller(counter++ + ""));
		super.onRestart();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 获取dp对应的px
	 */
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int) (dp * scale + 0.5f);
	}
    
   /* private static Bitmap zoomBitmap(Bitmap bitmap){
    	Matrix matrix = new Matrix();
    	float width = bitmap.getWidth();
    	float height = bitmap.getHeight();
    	float scaleX = getPx(70) / width;
    	float scaleY = getPx(70) / height;
    	Bitmap newB = Bitmap.createBitmap(bitmap, 0, 0, (int)width, (int)height, matrix, true);
    	return newB;
    }*/

	public void saveImage2SDCard(File file, Bitmap bitmap) {
		FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            //将bitmap写入流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            Log.v("MainActivity", "文件无法创建");
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	private static int getRandom(int count) {
		return (int) Math.round(Math.random() * (count));
	}

	private static String getRandomString(int length){
		StringBuilder sb = new StringBuilder();
		String random_words = "abcdefghijklmnopqrstuvwxyz1234567890";
		int len = random_words.length();
		for (int i = 0; i < length; i++) {
			sb.append(random_words.charAt(getRandom(len - 1)));
		}
		return sb.toString();
	}

	/**
     * @author victor_freedom (x_freedom_reddevil@126.com)
	 * @createddate 2015-1-31 下午11:39:18
	 * @Description: 缓存图片，返回一个bitmap对象
	 */
	class FreedomLoadTask extends AsyncTask<Bitmap, String, String> {
        private Music music;
        public FreedomLoadTask(Music music){
            this.music = music;
        }
		@Override
		protected String doInBackground(Bitmap... params) {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/../SimplePlayer/Album/");
            if (!root.exists()) {
                root.mkdirs();
            }
            String name = getRandomString(12) + ".jpg";
            File file = new File(root, name);
            while(file.exists()){
                file = new File(root, getRandomString(12) + ".jpg");
            }
			saveImage2SDCard(file, params[0]);
			return file.getPath();
		}

		@Override
		protected void onPostExecute(String s) {
            music.setImg_path(s);
            ((MyApplication)MainActivity.getInstance().getApplication()).getDatamanager().UpdateAlbumPath(music);
            MyImageLoader.Companion.getInstance().updateValue(music);
		}
	}


	/**
	 * 内部类，包含title，author，path
	 * */


	/**
	 * 解析Uri的类
	 * 通过调用其静态方法实现
	 */
	public static class FileUtils {
		/***
		 * 解析Uri
		 *
		 * @param context 调用的Context
		 * @param uri     需要解析的Uri (仅允许Image)
		 * @return 解析后的Path
		 * @author 瑞凯
		 */

		public static String getPath(Context context, Uri uri) {
			File fi = new File(uri.getPath());

			if (uri == null) return null;
			final String scheme = uri.getScheme();
			out.println();
			String data = null;
			if (scheme == null) {
				out.println("sch1");
				data = uri.getPath();
			} else if (SCHEME_FILE.equals(scheme)) {
				data = uri.getPath();
			} else if (SCHEME_CONTENT.equals(scheme)) {
				ContentResolver cr = context.getContentResolver();

				//CursorLoader loader = new CursorLoader(context,uri, new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
				Cursor cursor = cr.query(uri, new String[]{DATA, _ID}, null, null, null);
				if (null != cursor) {
					out.println("sch2");
					if (cursor.moveToFirst()) {
						out.println(cursor.getColumnCount());
						int index = cursor.getColumnIndex(DATA);
						String tem = cursor.getString(index);
						out.println("id: " + cursor.getString(cursor.getColumnIndex(_ID)));
						if (index > -1) {
							data = tem;
							out.println("sch 3: " + data);
						}
					}
					cursor.close();
				}
			}
			return data;
		}
	}
}