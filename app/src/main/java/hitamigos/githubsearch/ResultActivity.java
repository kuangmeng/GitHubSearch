package hitamigos.githubsearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
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
public class ResultActivity extends AppCompatActivity {
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
        setContentView(R.layout.result);
        intent = getIntent();
        String mess = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if(mess != null){
            message = mess;
        }
        GetData(message);
        LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.result, null);
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
                    //获得选中项的HashMap对象
                    // Map<String,Object>  map=(Map<String,Object>)listView.getItemAtPosition(arg2);
                }
            });
        views.add(listView);
        ViewPager viewPager= (ViewPager) findViewById(R.id.page);
        PageAdapter pageAdapter=new PageAdapter(views);
        viewPager.setAdapter(pageAdapter);
    }
    public void GetData(String message){
        try {
            //创建一个HttpClient对象
            HttpClient httpclient = new DefaultHttpClient();
            //远程登录URL
            String processURL="https://api.github.com/users/"+message+"/repos";
            //创建HttpGet对象
            HttpGet request=new HttpGet(processURL);
            //请求信息类型MIME每种响应类型的输出（普通文本、html 和 XML，json）。允许的响应类型应当匹配资源类中生成的 MIME 类型
            //资源类生成的 MIME 类型应当匹配一种可接受的 MIME 类型。如果生成的 MIME 类型和可接受的 MIME 类型不 匹配，那么将
            //生成 com.sun.jersey.api.client.UniformInterfaceException。例如，将可接受的 MIME 类型设置为 text/xml，而将
            //生成的 MIME 类型设置为 application/xml。将生成 UniformInterfaceException。
            request.addHeader("Accept","text/json");
            //获取响应的结果
            HttpResponse response =httpclient.execute(request);
            //获取HttpEntity
            HttpEntity entity=response.getEntity();
            //获取响应的结果信息
            String json = EntityUtils.toString(entity,"UTF-8");
            //JSON的解析过程
            // List<Map<String, Object>> list = new ArrayList<>();
            if(json!=null){
                com.alibaba.fastjson.JSONObject JSON=new com.alibaba.fastjson.JSONObject();
                List<HashMap> liststr =JSON.parseArray(json,HashMap.class);
                for(int i =0;i<liststr.size();i++){
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name",liststr.get(i).get("name"));
                    map.put("info",liststr.get(i).get("description"));
                    map.put("fork",liststr.get(i).get("forks_count"));
                    map.put("star",liststr.get(i).get("stargazers_count"));
                    map.put("language",liststr.get(i).get("language"));
                    map.put("time",liststr.get(i).get("created_at"));
                    System.out.println(liststr.get(i).get("clone_url"));
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

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    public void Jump(){
        //Intent in = new Intent(this, AppListActivity.class);
        //startActivity(in);
        // System.out.println("前往下载页");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // TODO Auto-generated method stub
        switch(item.getItemId()){
            case R.id.rss:
                Toast.makeText(ResultActivity.this, ""+"正在前往下载页！", Toast.LENGTH_SHORT).show();
                Jump();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
