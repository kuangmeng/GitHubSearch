package hitamigos.githubsearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "hitamigos.githubsearch.MESSAGE";
    private Intent intent;
    private ListView listView=null;
    List<View> views = new ArrayList<>();
    public static String message;
    public String url;
    public String logins;
    public String loginnames;
    public String homes;
    public String blogs;
    public String bios;
    public String foll;
    public String folling;
    public String repos;
    public String type;
    public String instenturl;
    List<Map<String, Object>> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        ///在Android2.2以后必须添加以下代码
        //本应用采用的Android4.0
        //设置线程的策略
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        //设置虚拟机的策略
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                //.detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        intent = getIntent();
        final String mess = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if(mess != null){
            message = mess;
        }
        GetData(message);
        listView=new ListView(this);
            //生成SimpleAdapter适配器对象
            SimpleAdapter mySimpleAdapter = new SimpleAdapter(this, list,//数据源
                    R.layout.list,//ListView内部数据展示形式的布局文件
                    new String[]{"name", "info", "fork","star","time","language"},//HashMap中的两个key值
                    new int[]{R.id.name, R.id.info, R.id.fork,R.id.star,R.id.time,R.id.language});/*布局文件
                                                            布局文件的各组件分别映射到HashMap的各元素上，完成适配*/
            listView.setAdapter(mySimpleAdapter);
            //添加点击事件
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    instenturl =instenturl+list.get(arg2).get("name")+"/commits";
                    Intent in = new Intent(ResultActivity.this,DetailsActivity.class);
                    in.putExtra(EXTRA_MESSAGE,instenturl);
                    startActivity(in);
                }
            });
        views.add(listView);
        ViewPager viewPager= (ViewPager) findViewById(R.id.page);
        PageAdapter pageAdapter=new PageAdapter(views);
        viewPager.setAdapter(pageAdapter);
    }
    public static Bitmap getBitmap(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
    public void GetData(String message){
        try {
            //创建一个HttpClient对象
            HttpClient httpclient = new DefaultHttpClient();
            String processURL="https://api.github.com/users/"+message+"/repos";
            String userURL = "https://api.github.com/users/"+message;
            HttpGet request=new HttpGet(processURL);
            request.addHeader("Accept","text/json");
            //获取响应的结果
            HttpResponse response =httpclient.execute(request);
            //获取HttpEntity
            HttpEntity entity=response.getEntity();
            //获取响应的结果信息
            String json = EntityUtils.toString(entity,"UTF-8");
            //JSON的解析过程
            if(json!=null && !json.toString().equals("[]")){
                com.alibaba.fastjson.JSONObject JSON=new com.alibaba.fastjson.JSONObject();
                List<HashMap> liststr =JSON.parseArray(json,HashMap.class);
                HttpGet requests=new HttpGet(userURL);
                request.addHeader("Accept","text/json");
                HttpResponse responses =httpclient.execute(requests);
                HttpEntity entitys=responses.getEntity();
                String jsons = EntityUtils.toString(entitys,"UTF-8");
                Gson gson = new Gson();
                Map<String, Object> maps = new HashMap<String, Object>();
                maps = gson.fromJson(jsons, maps.getClass());
                url = maps.get("avatar_url").toString();
                type = maps.get("type").toString();
                logins = maps.get("login").toString();
                instenturl = "https://api.github.com/repos/"+logins+"/";
                loginnames = maps.get("name").toString()==null ? "" : maps.get("name").toString();
                homes = maps.get("html_url").toString();
                foll = maps.get("followers").toString();
                folling = maps.get("following").toString();
                repos = maps.get("public_repos").toString();
                ImageView imageView = (ImageView) findViewById(R.id.image);
                imageView.setImageBitmap(getBitmap(url));
                TextView login = (TextView) findViewById(R.id.login);
                login.setText(logins);
                TextView loginname = (TextView) findViewById(R.id.loginname);
                loginname.setText("Name:" + loginnames);
                TextView home = (TextView) findViewById(R.id.home);
                home.setText("Home:" + homes);
                TextView follower = (TextView) findViewById(R.id.follower);
                follower.setText("Follower:" + foll);
                TextView following = (TextView) findViewById(R.id.following);
                following.setText("Following:" + folling);
                TextView repo = (TextView) findViewById(R.id.repo);
                repo.setText("Repos:" + repos);
                TextView ty = (TextView)findViewById(R.id.type);
                ty.setText(type);
                if(type.equals("User")){
                    blogs = maps.get("blog").toString()== null ? "" : maps.get("blog").toString();
                    bios = maps.get("bio").toString()==null?"":maps.get("bio").toString();
                    TextView blog = (TextView) findViewById(R.id.blog);
                    blog.setText(Html.fromHtml("Blog:" + blogs));
                    blog.setMovementMethod(LinkMovementMethod.getInstance());
                    TextView bio = (TextView) findViewById(R.id.bio);
                    bio.setText("Bio:" + bios);
                }else{
                    blogs = maps.get("email").toString()==null?"":maps.get("email").toString();
                    TextView blog = (TextView) findViewById(R.id.blog);
                    blog.setText(Html.fromHtml("Email:" + blogs));
                    blog.setMovementMethod(LinkMovementMethod.getInstance());
                }
                for(int i =0;i<liststr.size();i++){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name",liststr.get(i).get("name"));
                    map.put("info","Description:"+liststr.get(i).get("description"));
                    map.put("fork",liststr.get(i).get("forks_count"));
                    map.put("star",liststr.get(i).get("stargazers_count"));
                    map.put("language",liststr.get(i).get("language"));
                    map.put("time",liststr.get(i).get("created_at"));
                    list.add(map);
                }
            }else{
                try {
                    //创建一个HttpClient对象
                    HttpClient httpc = new DefaultHttpClient();
                    String searchURL = "https://api.github.com/search/repositories?q=" + message;
                    HttpGet re = new HttpGet(searchURL);
                    re.addHeader("Accept", "text/json");
                    //获取响应的结果
                    HttpResponse res = httpclient.execute(re);
                    //获取HttpEntity
                    HttpEntity en = res.getEntity();
                    //获取响应的结果信息
                    String jsons = EntityUtils.toString(en, "UTF-8");
                    //JSON的解析过程
                    if (jsons != null) {
                        Gson gson = new Gson();
                        Map<String, Object> maps = new HashMap<String, Object>();
                        maps = gson.fromJson(jsons, maps.getClass());
                        String count = maps.get("total_count").toString();
                        System.out.println(count);
                        instenturl = "https://api.github.com/repos/";
                        String[] items = maps.get("items").toString().split("\\,");
                        ImageView imageView = (ImageView) findViewById(R.id.image);
                        imageView.setImageResource(R.drawable.github);
                        TextView login = (TextView) findViewById(R.id.login);
                        login.setText(message);
                        TextView loginname = (TextView) findViewById(R.id.loginname);
                        if (count != null) {
                            loginname.setText("Count:" + count);
                        }
                        for (int i = 0; i < items.length; i++){
                            Map<String, Object> map = new HashMap<String, Object>();
                            while(!items[i].contains("full_name")) i++;
                            map.put("name", items[i].substring(11));
                            System.out.println(items[i].substring(11));
                            while(!items[i].contains("description")) i++;
                            map.put("info", "Description:"+items[i].substring(13));
                            System.out.println(items[i].substring(13));
                            while(!items[i].contains("created_at")) i++;
                            map.put("time", items[i].substring(12));
                            System.out.println(items[i].substring(12));
                            while(!items[i].contains("stargazers_count")) i++;
                            map.put("star", items[i].substring(18));
                            System.out.println(items[i].substring(18));
                            while(!items[i].contains("language")) i++;
                            map.put("language", items[i].substring(10));
                            System.out.println(items[i].substring(10));
                            while(!items[i].contains("forks_count")) i++;
                            map.put("fork", items[i].substring(13));
                            System.out.println(items[i].substring(13));
                            list.add(map);
                        }
                    }
                } catch (ClientProtocolException e){
                    e.printStackTrace();
                } catch (IOException i) {
                    i.printStackTrace();
                } catch(com.alibaba.fastjson.JSONException j){
                    j.printStackTrace();
                }
            }
        } catch (ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        } catch(com.alibaba.fastjson.JSONException j){
            j.printStackTrace();
        }
    }

}
