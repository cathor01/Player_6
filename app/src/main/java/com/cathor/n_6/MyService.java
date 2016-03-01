package com.cathor.n_6;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MyService extends Service{

	private static MyService _service;

	private BassBoost _bass;

	private boolean bass_state;

	private Virtualizer virtualizer;

	private boolean virtual_state;

	public static synchronized MyService getInstance(){
		if(_service == null){
			_service = new MyService();
		}
		return _service;
	}

	public MyService(){
		super();
	}
	
	public final static String SET_NOW = "setnow"; 
	public final static String STOP = "stop";
	public final static String PAUSE = "pause";
	public final static String PLAY = "play"; //播放的标识
	public final static String FLAG = "flag";
	public final static String MOVE_TO_NEXT = "moveToNext";
	private MediaPlayer player = new MediaPlayer();
	private static int flag = 0; //播放模式
	public final static String PLAY_CHANGE_RESOURCE = "chan"; //改变了Resource的播放tFlag
	public final static String PLAY_NO_CHANGE = "noc"; //未改变Resource的播放tFlag
	private static String[] albumArray = {};
	private static Map<String, ArrayList<Music>> map = null;
	private static ArrayList<Music> array = null; //播放列表
	private static String album;
	private static String nalbum;
	private static boolean hasLyric = false;
	private static ArrayList<Music> narray = null;
	private static int nowPlay = -1; //正在播放曲目(初始为0)
	private static int length = 0; //array长度
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	/**
	 * 获取正在播放曲目Id
	 * */	
	public int getNowPlay(){
		return nowPlay;
	}

	public void setTime(int time){
		player.seekTo(time);
	}

	public int getNowPlayTime(){
		return player.getCurrentPosition();
	}

	public int getArrayLength(String name){
		if(map.containsKey(name)){
			return map.get(name).size();
		}
		return -1;
	}

    public ArrayList<Music> getArrayAt(String album){
        return map.get(album);
    }

	public int getMaxTime(){
		return player.getDuration();
	}

	public void getLyric(){
		hasLyric = true;
	}

	public BassBoost getBassBoost(){
		return _bass;
	}

	public boolean getBassBoostState(){
		return _bass.getEnabled();
	}

	public Virtualizer getVirtualizer(){
		return virtualizer;
	}

	public boolean getVirtualizerState(){
		return virtualizer.getEnabled();
	}

	private void setNewBackGround(){
		if(MainActivity.getInstance()!= null){
			Music music = array.get(nowPlay);
			MyImageLoader loader = MyImageLoader.Companion.getInstance();
			MyImageLoader.ValueType valuetype = loader.getImageValue(music);
			if(valuetype == null){
				MainActivity.getInstance().initBackGround();
			}
			else {
				MainActivity.getInstance().setBackGround(valuetype, music);
			}
		}
	}

	public String getNewAlbum(){
		return nalbum;
	}


	/**
	 * 设置新播放列表但不停止
	 *
	 * **/
	public void setNewList(String talbum){
		nalbum = talbum;
		narray = map.get(talbum);
	}

	public ArrayList<Music> getList(){
		return narray;
	}
	
	/***
	 * 设置正在播放曲目id
	 * */
	public void setNowPlay(String talbum,int newNow){
		if(talbum == album) {
			nowPlay = newNow;
		}
		else{
			setArray(talbum);
			nowPlay = newNow;
		}
		Logger.INSTANCE.d("nowPlaying------>" + nowPlay);
	}

	/**
	 * 设置array，并更新length
	 * 
	 * */
	public void setArray(String talbum){
		array = map.get(talbum);
		length = array.size();
		nowPlay = -1;
	}
	
	public ArrayList<Music> getArray(){
		return array;
	}
	
	public String[] getAlbumList(){
		return albumArray;
	}
	
	public static void setMarray(Map<String, ArrayList<Music>> tMarray){
		map = tMarray;
		Set<String> set = tMarray.keySet();
		Logger.INSTANCE.d(set.size());
		albumArray = set.toArray(albumArray);
		if(albumArray.length == 0){
			array = new ArrayList<>();
			nalbum = "";
			narray = new ArrayList<>();
		}
		else{
			Arrays.sort(albumArray);
			array = map.get(albumArray[0]);
			nalbum = albumArray[0];
			narray = map.get(nalbum);
		}
		length = array.size();
		nowPlay = -1;
	}

	public void updateMarray(Map<String, ArrayList<Music>> tMarray){
		Logger.INSTANCE.d("tMarry is" + tMarray.size());
		map = tMarray;
		Set<String> set = tMarray.keySet();
		Logger.INSTANCE.d(set.size());
		albumArray = new String[1];
		albumArray = set.toArray(albumArray);
		Arrays.sort(albumArray);
		nalbum = albumArray[0];
		narray = map.get(nalbum);
	}

	public String getCurrentAlbum(){
		return album;
	}
	
	/**
	 * 获取在position位置的Music类型
	 * */
	public Music getItemAt(int position){
		return array.get(position);
	}
	/***
	 *  获取数组长度
	 * */
	public int getLength() {
		return length;
	}

	@Override
	public void onCreate(){
		// TODO Auto-generated method stub
		super.onCreate();
		
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				playover();
			}
		});
		player.setOnPreparedListener(new PreparedListener());
		Logger.INSTANCE.d("Create the Srevice");
		_service = this;
		_bass = new BassBoost(0, player.getAudioSessionId());
		SharedPreferences preferences = getSharedPreferences(MyApplication.Companion.getPREFERENCE_NAME(), Activity.MODE_PRIVATE);
		_bass.setEnabled(preferences.getBoolean(MyApplication.Companion.getPREFERENCE_BASS_STATUS(), false));
		_bass.setStrength((short) preferences.getInt(MyApplication.Companion.getPREFERENCE_BASS_VALUE(), 0));
		virtualizer = new Virtualizer(0, player.getAudioSessionId());
		virtualizer.setEnabled(preferences.getBoolean(MyApplication.Companion.getPREFERENCE_VIRTUAL_STATUS(), false));
		virtualizer.setStrength((short) preferences.getInt(MyApplication.Companion.getPREFERENCE_VIRTUAL_VALUE(), 0));
        Log.e("myservice",_bass.getStrengthSupported() + " &&& " + _bass.getEnabled() + " &$ " + _bass.getRoundedStrength());
        virtualizer.getStrengthSupported();
	}
	/**
	 * 获取当前播放状态
	 * */
	public boolean getPlayStatewioutThrow(){
		Logger.INSTANCE.d("is playing?");
		return player.isPlaying();
	}
		
	public void stop(){
		player.stop();
	}
	
	public void totalStop(){
		setFlag(0);
		player.stop();
	}
	
	public void pause(){
		player.pause();
		Controller.update();
	}



	private class PreparedListener implements MediaPlayer.OnPreparedListener {
		boolean flag = true;

		@Override
		public void onPrepared(MediaPlayer mp) {
			if(flag) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// 当歌曲还在播放时
						// 就一直调用changeCurrent方法
						// 虽然一直调用， 但界面不会一直刷新
						// 只有当唱到下一句时才刷新
						while (true) {
							if (player.isPlaying()) {

								if (hasLyric) {
									// 调用changeCurrent方法， 参数是当前播放的位置
									// LrcView会自动判断需不需要下一行
									TabAdapter.getLrcFragment().changeCurrent(player.getCurrentPosition());

									// 当然这里还是要睡一会的啦
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} else {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							} else {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}).start();

				flag = false;
			}

		}
	}

	public String getNowPlayTitle(){
		return getItemAt(nowPlay).getTitle();
	}

	public String getNowPlayAuthor(){
		return getItemAt(nowPlay).getAuthor();
	}

	/***
	 * 播放
	 * @param tflag 播放的模式
	 * */
	private int play(String tflag){
		if(tflag.equals(PLAY_CHANGE_RESOURCE)){
			player.reset();
			Logger.INSTANCE.d(getItemAt(nowPlay).getPath());
			try {
				player.setDataSource(getItemAt(nowPlay).getPath());
				player.prepare();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

		}
		Logger.INSTANCE.d("The World!!!");
		try {

			player.start();
			if(tflag.equals(PLAY_CHANGE_RESOURCE)){
				setTime(1);
			}
		}
		catch(IllegalStateException e){
			e.printStackTrace();
			Log.e("Player", "WTF!0!!!!");
		}
		Controller.initedPlayer();
		Log.e("Mess", player.getDuration() + "");
		String path = getItemAt(getNowPlay()).getPath();
		int i = path.lastIndexOf(".");
		String lpath = path.substring(0, i+1) + "lrc";
		Logger.INSTANCE.d(lpath);
		try {
			if(TabAdapter.getLrcFragment().setLrcPath(lpath)!= 1) {
				String filename = getItemAt(getNowPlay()).getAuthor() + "-" + getItemAt(getNowPlay()).getTitle() + ".lrc";
				filename = filename.replace("/", "_");
				filename = filename.replace("<", "");
				filename = filename.replace(">", "");
				String mpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/../SimplePlayer/Lyrics/" + filename;
				Logger.INSTANCE.d(mpath);
				if(1 != TabAdapter.getLrcFragment().setLrcPath(mpath)){
					mpath = mpath.replace(".lrc", ".txt");
					Logger.INSTANCE.d(mpath);
					if(1 != TabAdapter.getLrcFragment().setLrcPath(mpath)){
						TabAdapter.getLrcFragment().noLrc();
						Logger.INSTANCE.d("啥都没有");
					}
					hasLyric = false;
				}
				else {

					hasLyric = true;
				}
			}
			else{
				hasLyric = true;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		setNewBackGround();
		Logger.INSTANCE.d(getItemAt(nowPlay).getAlbum_id());
		Controller.update();
		return 1;
	}
	/**
	 * 播放完成后调用
	 * */
	public void playover(){
		Controller.stopSeek();
		switch(flag){
		case 0:
			moveToNextWithoutRewind();
			break;
		case 1:
			moveToNext();
			break;
		case 2:
			play(MyService.PLAY_NO_CHANGE);
			break;
		case 3:
			setNowPlay(album,new Random().nextInt(length));
			play(MyService.PLAY_CHANGE_RESOURCE);
			MyFragment.change = 1;
			MainActivity.getInstance().updateNotification(1);
			break;
		}
	}
	/**
	 * 设置播放模式
	 * */
	public void setFlag(int tflag){
		flag = tflag;
	}
	/**
	 * 获取播放模式
	 * */
	public int getFlag(){
		return flag;
	}
	/***
	 * 下一曲(会自动回到第一首)
	 * */
	public void moveToNext(){
		MyFragment.change = 1;
		Logger.INSTANCE.d("nowPlay -p ------->" + getNowPlay());
		setNowPlay(album, (getNowPlay() + 1) % length);
		Logger.INSTANCE.d("nowPlay -l ------->" + getNowPlay());
		play(MyService.PLAY_CHANGE_RESOURCE);

		MainActivity.getInstance().updateNotification(1);
	}

	/***
	 * 下一曲(不会自动回到第一首)
	 * */
	public void moveToNextWithoutRewind(){
		MyFragment.change = 1;
		Logger.INSTANCE.d("nowPlay -p ------->" + getNowPlay());
		if(getNowPlay() < length - 1) {
			setNowPlay(album, getNowPlay() + 1);
		}
		else{
			MainActivity.getInstance().updateNotification(2);
			return;
		}
		Logger.INSTANCE.d("nowPlay -l ------->" + getNowPlay());
		play(MyService.PLAY_CHANGE_RESOURCE);

		MainActivity.getInstance().updateNotification(1);
	}

	/**
	 * 上一曲
	 * */
	public void moveToLast(){
		MyFragment.change = 2;
		Logger.INSTANCE.d("nowPlay -p ------->" + getNowPlay());
		setNowPlay(album, (getNowPlay() - 1 + length) % length);
		Logger.INSTANCE.d("nowPlay -l ------->" + getNowPlay());
		play(MyService.PLAY_CHANGE_RESOURCE);

		MainActivity.getInstance().updateNotification(1);
	}
	/**
	 * 解析intent数据并执行操作
	 * */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				for(String key :bundle.keySet()){
					String value = bundle.getString(key);
					Logger.INSTANCE.d("key------->"+key);
					Logger.INSTANCE.d("value----->"+value);
					switch(key){
					case SET_NOW:///未使用
						setNowPlay(album, Integer.parseInt(value));
						Logger.INSTANCE.d("execute setNow");
						break;
					case STOP://未使用
						stop();
						Logger.INSTANCE.d("execute stop");
						break;
					case PAUSE://未使用
						pause();
						Logger.INSTANCE.d("execute pause");
						break;
					case PLAY:
						play(value);
						Logger.INSTANCE.d("execute play");
						break;
					case MOVE_TO_NEXT: //未使用
						moveToNext();
						Logger.INSTANCE.d("execute moveToNext");
						break;
					case FLAG: //未使用
						setFlag(Integer.parseInt(value));
						Logger.INSTANCE.d("execute setFlag");
						break;
					}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Logger.INSTANCE.d("Destroy the Service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
