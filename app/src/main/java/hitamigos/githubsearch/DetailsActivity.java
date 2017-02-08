/*
 * Copyright (c) 2017.
 * 个人版权所有
 * kuangmeng.net
 */

package hitamigos.githubsearch;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hitamigos.githubsearch.R.id.date;

public class DetailsActivity extends AppCompatActivity {
    private Intent intent;
    private ListView listView=null;
    List<View> views = new ArrayList<>();
    public static String message;
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
        setContentView(R.layout.details);
        intent = getIntent();
        final String mess = intent.getStringExtra(ResultActivity.EXTRA_MESSAGE);
        if(mess != null){
            message = mess;
        }
        System.out.println(message);
        GetData(message);
        listView=new ListView(this);
            //生成SimpleAdapter适配器对象
            SimpleAdapter mySimpleAdapter = new SimpleAdapter(this, list,//数据源
                    R.layout.item,//ListView内部数据展示形式的布局文件
                    new String[]{"sha", "message", "name","email","date"},//HashMap中的两个key值
                    new int[]{R.id.sha, R.id.message, R.id.name,R.id.email, date});/*布局文件
                                                            布局文件的各组件分别映射到HashMap的各元素上，完成适配*/
            listView.setAdapter(mySimpleAdapter);
            //添加点击事件
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                }
            });
        views.add(listView);
        ViewPager viewPager= (ViewPager) findViewById(R.id.pages);
        PageAdapter pageAdapter=new PageAdapter(views);
        viewPager.setAdapter(pageAdapter);
    }
    public void GetData(String message){
        try {
            //创建一个HttpClient对象
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request=new HttpGet(message);
            request.addHeader("Accept","text/json");
            //获取响应的结果
            HttpResponse response =httpclient.execute(request);
            //获取HttpEntity
            HttpEntity entity=response.getEntity();
            //获取响应的结果信息
            String json = EntityUtils.toString(entity,"UTF-8");
            //JSON的解析过程
            if(json!=null){
                com.alibaba.fastjson.JSONObject JSON=new com.alibaba.fastjson.JSONObject();
                List<HashMap> liststr =JSON.parseArray(json,HashMap.class);
                TextView items = (TextView)findViewById(R.id.items);
                items.setText("本项目共有"+liststr.size()+"次commit信息！");
                items.setGravity(Gravity.CENTER);
                System.out.println(liststr.size());
                for(int i =0;i<liststr.size();i++){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("sha","SHA:"+liststr.get(i).get("sha"));
                    System.out.println(liststr.get(i).get("sha"));
                    Gson gson = new Gson();
                    Map<String, Object> maps = new HashMap<String, Object>();
                    maps = gson.fromJson(liststr.get(i).get("commit").toString(), maps.getClass());
                    map.put("message","Message:"+maps.get("message").toString());
                    System.out.println(maps.get("committer"));
                    String me = maps.get("committer").toString().trim().substring(1,maps.get("committer").toString().trim().length());
                    String[] arr = me.split("\\,");
                    String date = arr[0].substring(5);
                    String email = arr[1].substring(7);
                    String name = arr[2].substring(6,arr[2].length()-1);
                    map.put("name","Name:"+name);
                    map.put("email","Email:"+email);
                    map.put("date","Date:"+date);
                    list.add(map);
                }
            }else{

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
