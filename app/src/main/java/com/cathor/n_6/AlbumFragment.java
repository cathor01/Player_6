package com.cathor.n_6;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yalantis.phoenix.PullToRefreshView;


public class AlbumFragment extends Fragment {
    private static AlbumFragment instance;
	private LayoutInflater inflater;
	private int height = 108;
	private String[] albumList;
	private ListView list;
	public static boolean hascreated = false;
    private PullToRefreshView refreshView;
	private View view;
	private BaseAdapter adapter;

    private MyImageLoader.OnImageLoadListener listener = new MyImageLoader.OnImageLoadListener() {
        @Override
        public void OnImageLoaded(int position) {

        }

        @Override
        public void OnImageLoadFailed(int position) {

        }
    };
    // private HashMap<Integer, Drawable> map = new HashMap<>();
	/*private MyImageLoader.OnImageLoadListener listener = new MyImageLoader.OnImageLoadListener() {
        @Override
        public void onImageLoad(int t, @NotNull Drawable drawable) {
            setImageViewDrawable(t, drawable);
        }

        @Override
        public void onError(int t) {
            Log.d(this.getClass().toString(), t + "load failed");
        }
    };*/

    public static AlbumFragment getInstance(){
        if(instance == null){
            instance = new AlbumFragment();
        }
        return instance;
    }
    public AlbumFragment(){
        instance = this;
    }

   /* public void setImageViewDrawable(int id, Drawable drawable){
        map.put(id, drawable);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putInt("id", id);
        msg.setData(data);
        msg.what = 103;
        handler.sendMessage(msg);
    }*/

	@SuppressLint("ViewHolder") @Override
	public View onCreateView(LayoutInflater tinflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		inflater = tinflater;
		albumList = MyService.getInstance().getAlbumList();
        MyImageLoader.Companion.getInstance().setOnImageLoadListener(listener);
		view = createView();
		return view;
	}


    private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case 101:
					albumList = MyService.getInstance().getAlbumList();
					Logger.INSTANCE.d("album is " + albumList.length);
					list.setAdapter(createAdapter());
					ViewGroup.LayoutParams params = list.getLayoutParams();
					params.height = getPx(height) * albumList.length;
					params.width = ViewGroup.LayoutParams.MATCH_PARENT;
					list.setLayoutParams(params);
                    break;
                case 102:
                    MainActivity.getInstance().handler.sendEmptyMessage(304);
                    refreshView.setRefreshing(false);
                    break;
                /*case 103:
                    try {
                        int id = msg.getData().getInt("id");
                        ImageView img = ref.get(id);
                        img.setImageDrawable(map.get(id));
                        map.remove(id);
                    }
                    catch (Exception e){}
                    break;*/
			}
		}
	};
	
	public void updateView(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					if(AlbumFragment.hascreated) {
						handler.sendEmptyMessage(101);
						break;
					}
					try {
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

    //private ArrayList<ImageView> ref;

	private BaseAdapter createAdapter(){
        /*if(ref != null) {
            ref.clear();
        }
        ref = new ArrayList<>(albumList.length);
		*/
		BaseAdapter adapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method
				RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.album, null);
				TextView album = (TextView)view.findViewById(R.id.album_title);
				TextView length = (TextView)view.findViewById(R.id.album_length);
				view.setId(position + 2000);
                view.setTag(position + 2000);
				album.setClickable(false);
				view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getPx(height - 4)));
				view.setClickable(true);
				if(MainActivity.nowType != 2) {
					album.setText(albumList[position]);
				}
				else{
					album.setText(albumList[position].substring(albumList[position].lastIndexOf("/") + 1));
				}
				length.setText("共" + MyService.getInstance().getArrayLength(albumList[position]) + "首");
				view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        MyService.getInstance().setNewList(albumList[v.getId() - 2000]);
                        Logger.INSTANCE.d(albumList[v.getId() - 2000]);
                        MyFragment.updateView();
                        MainActivity.getInstance().jumpToMusic();
                    }
                });
                ImageView img = (ImageView)view.findViewById(R.id.album_img);
                MyImageLoader.Companion.getInstance().loadImage(MyService.getInstance().getArrayAt(albumList[position]), position,img);
                Logger.INSTANCE.d(img.toString());
                //ref.add(img);
                return view;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position + 3000;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return albumList[position];
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return albumList.length;
			}
		};
//        ImageLoader loader = ImageLoader.Companion.getInstance();
//        loader.setLoadLimit(0, albumList.length - 1);
//        for (int i = 0; i < albumList.length; i++){
//            loader.loadImage(i, MyService.getInstance().getArrayAt(albumList[i]), listener);
//        }
        Log.d("length", albumList.length + "张");
        //loader.getExecuteorService().shutdown();
        return adapter;
	}

	private View createView(){
		if(albumList == null || albumList.length == 0){
			return inflater.inflate(R.layout.lyricinput, null);
		}
		RelativeLayout layout = new RelativeLayout(inflater.getContext());
		int px = getPx(8);
		refreshView = new PullToRefreshView(inflater.getContext());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layout.addView(refreshView, layoutParams);
		refreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataManager manager = ((MyApplication) MainActivity.getInstance().getApplication()).GetDataManager();
                        manager.DropData();
                        MainActivity.album_list = manager.GetAlbumData();
                        MainActivity.author_list = manager.GetAuthorData();
                        MainActivity.folder_list = manager.GetFolderData();
                        MainActivity.play_list = manager.GetListData();
                        MyService.setMarray(MainActivity.getNowPlay());
                        handler.sendEmptyMessage(102);
                    }
                });
                thread.start();
            }
        });
		list = new ListView(inflater.getContext());
        list.setFastScrollEnabled(true);
        list.setFocusable(true);
        list.setVerticalScrollBarEnabled(false);
        refreshView.addView(list);
		adapter = createAdapter();
		list.setAdapter(adapter);
        list.setDivider(new ColorDrawable(Color.TRANSPARENT));
		list.setDividerHeight(getPx(4));
		layout.setPadding(px, 0, px, 0);
		hascreated = true;
		return layout;
	}
	
	private int getPx(int dp) {
		// TODO Auto-generated method stub
		return (int)(dp * MainActivity.getInstance().scale + 0.5f);
	}
}
