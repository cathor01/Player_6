package com.cathor.n_6;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * 使用了网易云音乐的API
 *
 * Created by Cathor on 2015/9/18.
 */
public class LrcSearcher {
    private final static String NET_SEARCH_URL = "http://music.163.com/api/search/pc";
    private final static String NET_REFERENCE = "http://music.163.com/";
    private final static String NET_COOKIE = "appver=1.5.0.75771";
    private final static int NET_LIMIT = 10;
    private final static int READ_TIME_OUT = 5000;
    private final static String NET_LYRIC_URL = "http://music.163.com/api/song/lyric?os=pc&id=";
    private final static String NET_LYRIC_OTHER = "&lv=1&kv=1&tv=-1";

    private final static String XIAMI_SEARCH_URL = "http://www.xiami.com/web/search-songs?spm=0.0.0.0.8ej3Ac";
    private final static String XIAMI_LTRIC_URL = "http://www.xiami.com/song/playlist/id/";
    private final static String XIAMI_LYRIC_OTHER = "/object_name/default/object_id/0/cat/json";
    private final static String ENCODE = "UTF-8";

    public static List<LrcItem> search(String title, String author) throws Exception{
        /*String hextitle = encode(title);
        String hexauthor = encode(author);
        */
        List<LrcItem> list1 = new ArrayList<>();
        try {
            list1 = searchInNet(title, author);
        }
        catch(Exception e){
            MainActivity.getInstance().toastErrorInfo("网易云音乐 ");
        }
        List<LrcItem> list2 = new ArrayList<>();
        try {
            list2 = searchInXiami(title, author);
        }
        catch(Exception e){
            MainActivity.getInstance().toastErrorInfo("虾米音乐 ");
        }
        List<LrcItem> lists = new ArrayList<>();
        int len1 = list1.size();
        int len2 = list2.size();
        int iterator = 0;
        while (iterator < Math.max(len1, len2)){
            if(iterator < len1){
                lists.add(list1.get(iterator));
            }
            if(iterator < len2){
                lists.add(list2.get(iterator));
            }
            iterator++;
        }
        return lists;
    }

    private static List<LrcItem> searchInNet(String title, String author) throws Exception{
        URL url1 = new URL(NET_SEARCH_URL);
        HttpURLConnection con1 = (HttpURLConnection)url1.openConnection();
        con1.setDoInput(true);
        con1.setDoOutput(true);
        con1.setRequestMethod("POST");
        con1.setRequestProperty("Cookie", NET_COOKIE);
        con1.setRequestProperty("Referer", NET_REFERENCE);
        con1.setReadTimeout(READ_TIME_OUT);
        PrintWriter pw = new PrintWriter(con1.getOutputStream());
        pw.write("&type=1");
        pw.write("&s=" + title + " " + author);
        pw.write("&limit=" + NET_LIMIT);
        pw.flush();
        pw.close();
        InputStream stream = con1.getInputStream();
        Scanner scanner1 = new Scanner(stream);
        StringBuilder sb1 = new StringBuilder();
        List<LrcItem> lists = new ArrayList<>();
        while(scanner1.hasNextLine()){
            sb1.append(scanner1.nextLine() + "\n");
        }
        scanner1.close();
        //String out = new String(sb1.toString().getBytes(), ENCODE);
        JSONObject result = new JSONObject(new String(sb1.toString().getBytes(), ENCODE));
        JSONObject info = result.getJSONObject("result");
        JSONArray infoarray = info.getJSONArray("songs");
        for(int i = 0; i < infoarray.length(); i++){
            JSONObject tempJson = infoarray.getJSONObject(i);
            String ttitle = tempJson.getString("name");
            String tauthor = tempJson.getJSONArray("artists").getJSONObject(0).getString("name");
            String tlink = NET_LYRIC_URL + tempJson.getString("id") + NET_LYRIC_OTHER;
            lists.add(new LrcItem(ttitle, tauthor, tlink, Resource.Net));
        }
        return lists;
    }

    private static List<LrcItem> searchInXiami(String author, String artist) throws Exception{
        URL url1 = new URL(XIAMI_SEARCH_URL);
        HttpURLConnection con1 = (HttpURLConnection)url1.openConnection();
        con1.setDoInput(true);
        con1.setDoOutput(true);
        con1.setRequestMethod("GET");
        con1.setReadTimeout(READ_TIME_OUT);
        PrintWriter pw = new PrintWriter(con1.getOutputStream());
        pw.write("&key=" + author + " " + artist);
        pw.flush();
        pw.close();
        InputStream in11 = con1.getInputStream();
        Scanner scanner1 = new Scanner(in11);
        StringBuilder sb1 = new StringBuilder();
        //List<LrcItem> lists = new ArrayList<>();
        while(scanner1.hasNextLine()){
            sb1.append(scanner1.nextLine() + "\n");
        }
        scanner1.close();
        List<LrcItem> lists = new ArrayList<>();
        JSONArray array = new JSONArray(new String(sb1.toString().getBytes(), ENCODE));
        for(int i = 0; i < (array.length() > 10 ? 10 : array.length()); i++){
            JSONObject object = array.getJSONObject(i);
            String tauthor = object.getString("author");
            String ttitle = object.getString("title");
            String tlink = XIAMI_LTRIC_URL + object.getInt("id") + XIAMI_LYRIC_OTHER;
            lists.add(new LrcItem(ttitle, tauthor, tlink, Resource.XIAMI));
        }
        return lists;
    }

    public static String download(String path, String title, String author, Resource tresource) throws Exception{
        switch (tresource){
            case Net:
                return downloadFromNet(path, title, author);
            case XIAMI:
                return downloadFromXiami(path, title, author);
            default:
                return null;
        }
    }

    private static String downloadFromNet(String path, String title, String author) throws Exception{
        URL url1 = new URL(path);
        HttpURLConnection con1 = (HttpURLConnection)url1.openConnection();
        con1.setDoInput(true);
        con1.setDoOutput(true);
        con1.setRequestMethod("GET");
        con1.setRequestProperty("Cookie", "appver=1.5.0.75771");
        con1.setRequestProperty("Referer", "http://music.163.com/");
        con1.setReadTimeout(5000);
        InputStream stream = con1.getInputStream();
        Scanner scanner1 = new Scanner(stream);
        StringBuilder sb1 = new StringBuilder();
        while(scanner1.hasNextLine()){
            sb1.append(scanner1.nextLine() + "\n");
        }
        scanner1.close();
        String out = new String(sb1.toString().getBytes(), ENCODE);
        System.out.println("out = " + out);
        JSONObject result = new JSONObject(new String(sb1.toString().getBytes(), ENCODE));
        try {
            JSONObject info = result.getJSONObject("lrc");
            String lrc = info.getString("lyric");
            String extension = ".txt";
            if(lrc.matches("[\\s\\S]*\\[\\d+:\\d+\\.\\d+\\][\\s\\S]*")){
                extension = ".lrc";
            }
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/../SimplePlayer/Lyrics/");
            if (!root.exists()) {
                root.mkdirs();
            }
            String filename = author + "-" + title + extension;
            filename = filename.replace("/", "_");
            filename = filename.replace("<", "");
            filename = filename.replace(">", "");
            File f = new File(root, filename);
            if (f.exists()) {
                f.delete();
            }
            System.out.println(f.toString());
            f.createNewFile();
            FileOutputStream fout = new FileOutputStream(f);
            BufferedOutputStream outs = new BufferedOutputStream(fout);
            byte[] first3bytes = new byte[3];
            first3bytes[0] = (byte) 0xEF;
            first3bytes[1] = (byte) 0xBB;
            first3bytes[2] = (byte) 0xBF;
            outs.write(first3bytes);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs, ENCODE));
            writer.append(lrc);
            JSONObject trans = result.getJSONObject("tlyric");
            if(trans.getInt("version") > 0){
                String trans_lrc = trans.getString("lyric");
                writer.append(trans_lrc);
            }
            writer.flush();
            writer.close();
            fout.close();
            return f.getAbsolutePath();
        }
        catch(JSONException e){
            return "wtf";
        }
    }

    private static String downloadFromXiami(String path, String title, String author) throws Exception{
        URL url1 = new URL(path);
        HttpURLConnection con1 = (HttpURLConnection)url1.openConnection();
        con1.setDoInput(true);
        con1.setDoOutput(true);
        con1.setRequestMethod("GET");
        con1.setReadTimeout(5000);
        InputStream stream = con1.getInputStream();
        Scanner scanner1 = new Scanner(stream);
        StringBuilder sb1 = new StringBuilder();
        while(scanner1.hasNextLine()){
            sb1.append(scanner1.nextLine() + "\n");
        }
        scanner1.close();
        String out = new String(sb1.toString().getBytes(), ENCODE);
        JSONObject result = new JSONObject(new String(sb1.toString().getBytes(), ENCODE));
        try {
            JSONObject data = result.getJSONObject("data");
            JSONObject info = data.getJSONArray("trackList").getJSONObject(0);
            String lrcurl = info.getString("lyric");
            if(lrcurl.equals("") || lrcurl.length() == 0){
                return "wtf";
            }
            lrcurl = lrcurl.replace("\\/", "/");
            URL url2 = new URL(lrcurl);
            HttpURLConnection con2 = (HttpURLConnection)url2.openConnection();
            con2.setDoInput(true);
            con2.setDoOutput(false);
            con2.setRequestMethod("GET");
            InputStream in2 = con2.getInputStream();
            Scanner scanner2 = new Scanner(in2);
            StringBuilder sb2 = new StringBuilder();
            //List<LrcItem> lists = new ArrayList<>();
            while(scanner2.hasNextLine()){
                sb2.append(scanner2.nextLine() + "\n");
            }
            scanner2.close();
            String lrc = new String(sb2.toString().getBytes(), ENCODE);
            String extension = ".txt";
            if(lrc.matches("[\\s\\S]*\\[\\d+:\\d+\\.\\d+\\][\\s\\S]*")){
                extension = ".lrc";
            }
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/../SimplePlayer/Lyrics/");
            if (!root.exists()) {
                root.mkdirs();
            }
            String filename = author + "-" + title + extension;
            filename = filename.replace("/", "_");
            filename = filename.replace("<", "");
            filename = filename.replace(">", "");
            File f = new File(root, filename);
            if (f.exists()) {
                f.delete();
            }
            System.out.println(f.toString());
            f.createNewFile();
            FileOutputStream fout = new FileOutputStream(f);
            BufferedOutputStream outs = new BufferedOutputStream(fout);
            byte[] first3bytes = new byte[3];
            first3bytes[0] = (byte) 0xEF;
            first3bytes[1] = (byte) 0xBB;
            first3bytes[2] = (byte) 0xBF;
            outs.write(first3bytes);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs, ENCODE));
            writer.append(lrc);
            writer.flush();
            writer.close();
            fout.close();
            return f.getAbsolutePath();
        }
        catch(JSONException e){
            return "wtf";
        }
    }


   /* private static String hexString = "0123456789ABCDEF";

    public static String encode(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }*/

}
