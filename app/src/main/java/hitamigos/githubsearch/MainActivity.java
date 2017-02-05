package hitamigos.githubsearch;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

import hitamigos.githubsearch.data.DataHelper;
import hitamigos.githubsearch.data.Suggestion;
import online.osslab.FloatingSearchView;
import online.osslab.suggestions.SearchSuggestionsAdapter;
import online.osslab.suggestions.model.SearchSuggestion;
import online.osslab.util.view.BodyTextView;
import online.osslab.util.view.IconImageView;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    private FloatingSearchView searchView;

    private ViewGroup parentView;
    private TextView colorName;
    private TextView colorValue;
    public String returnstr = "kuangmeng";
    public List<HashMap> list = null;
    public String getSpeak() {
        return Speak;
    }

    public void setSpeak(String speak) {
        Speak = speak;
    }

    private String Speak;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSpeech();
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
        parentView = (ViewGroup)findViewById(R.id.parent_view);
        searchView = (FloatingSearchView)findViewById(R.id.floating_search_view);
        colorName = (TextView)findViewById(R.id.color_name_text);
        colorValue = (TextView)findViewById(R.id.color_value_text);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        refreshBackgroundColor("Blue", "#1976D2");
        GetData(returnstr);
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                } else {
                    searchView.showProgress();
                    DataHelper.find(MainActivity.this, newQuery, new DataHelper.OnFindResultsListener() {
                        @Override
                        public void onResults(List<Suggestion> results) {
                            searchView.swapSuggestions(results);
                            searchView.hideProgress();
                        }
                    });
                }
            }
        });
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
//                Suggestion colorSuggestion = (Suggestion) searchSuggestion;
//                refreshBackgroundColor(colorSuggestion.getColor().getName(), colorSuggestion.getColor().getHex());
            }
            @Override
            public void onSearchAction() {
                System.out.println("hello");
            }
        });
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus(){
               // searchView.swapSuggestions(DataHelper.getHistory(MainActivity.this, 5));
            }
            @Override
            public void onFocusCleared() {
            }
        });
        //handle menu clicks the same way as you would
        //in a regular activity
        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_voice) {
                    startSpeechDialog();
                } else if(item.getItemId() == R.id.action_history) {
                    Toast.makeText(getApplicationContext(), item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                //drawerLayout.openDrawer(GravityCompat.START);
            }
            @Override
            public void onMenuClosed() {
               // drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        searchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
            }
        });
        searchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(IconImageView leftIcon, BodyTextView bodyText, SearchSuggestion item, int itemPosition) {
                Suggestion suggestion = (Suggestion) item;
                if (suggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(leftIcon.getResources().getDrawable(R.drawable.ic_history_black_24dp));
                    leftIcon.setAlpha(.36f);
                } //else
                   // leftIcon.setImageDrawable(new ColorDrawable(Color.parseColor(suggestion.getColor().getHex())));
            }
        });
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                searchView.closeMenu(false);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        ListView listView=new ListView(this);
            //生成SimpleAdapter适配器对象
        SimpleAdapter mySimpleAdapter = new SimpleAdapter(this, list,
                    R.layout.list,
                    new String[]{"name", "description", "clone_url","stargazers_count","fork_count"},
                    new int[]{R.id.name, R.id.info, R.id.link,R.id.star,R.id.fork});
        listView.setAdapter(mySimpleAdapter);
            //添加点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        Toast.makeText(getApplicationContext(),
                                "正在获取commit信息！",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        startActivity(intent);
                    }
            });
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
                 list =JSON.parseArray(json,HashMap.class);
                for(int i =0;i<list.size();i++){
                    System.out.print(list.get(i).get("name"));
                    System.out.print(list.get(i).get("description"));
                    System.out.print(list.get(i).get("forks_count"));
                    System.out.print(list.get(i).get("stargazers_count"));
                    System.out.print(list.get(i).get("language"));
                    System.out.print(list.get(i).get("created_at"));
                    System.out.println(list.get(i).get("clone_url"));
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

    private void refreshBackgroundColor(String colorName, String colorValue){
        int color = Color.parseColor(colorValue);
        Palette.Swatch swatch = new Palette.Swatch(color, 0);
        this.colorName.setTextColor(swatch.getTitleTextColor());
        this.colorName.setText("项目地址：\nhttps://github.com/kuangmeng/GitHubSearch\n\n我的博客\nhttp://www.meng.uno");
        this.colorValue.setTextColor(swatch.getBodyTextColor());
        //this.colorValue.setText(colorValue);
        parentView.setBackgroundColor(color);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getDarkerColor(color, .8f));
    }
    private static int getDarkerColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, Math.max((int)(r * factor), 0), Math.max((int)(g * factor), 0),
                Math.max((int)(b * factor), 0));
    }
    /*
       语音搜索
        */
    private void initSpeech() {
        // 请勿在 “ =”与 appid 之间添加任务空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=584be381");
    }
    private void startSpeechDialog() {
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener()) ;
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant. LANGUAGE, "en" );
        mDialog.setParameter(SpeechConstant. ACCENT, "mandarin" );
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener( new MyRecognizerDialogListener()) ;
        //4. 显示dialog，接收语音输入
        mDialog.show();
    }
    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //为解析的
            //  showTip(result) ;
            System. out.println(" 没有解析的 :" + result);
            String text = hitamigos.githubsearch.speak.JsonParser.parseIatResult(result) ;//解析过后的
            System. out.println(" 解析后的 :" + text);
            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString()) ;
                sn = resultJson.optString("sn" );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text) ;//没有得到一句，添加到
            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults .get(key));
            }
            setSpeak(resultBuffer.toString());
            System.out.println(getSpeak());
         //   sendMessage(getSpeak());
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败 ");
            }
        }
    }
    /**
     * 语音识别
     */
    private void startSpeech(){
        //1. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer( this, null); //语音识别器
        //2. 设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
        mIat.setParameter(SpeechConstant. DOMAIN, "iat" );// 短信和日常用语： iat (默认)
        mIat.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mIat.setParameter(SpeechConstant. ACCENT, "mandarin" );// 设置普通话
        //3. 开始听写
        mIat.startListening( mRecoListener);
    }


    // 听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {
        // 听写结果回调接口 (返回Json 格式结果，用户可参见附录 13.1)；
//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
//关于解析Json的代码可参见 Demo中JsonParser 类；
//isLast等于true 时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e (TAG, results.getResultString());
            System.out.println(results.getResultString()) ;
            showTip(results.getResultString()) ;
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true)) ;
            // 获取错误码描述
            Log. e(TAG, "error.getPlainDescription(true)==" + error.getPlainDescription(true ));
        }
        // 开始录音
        public void onBeginOfSpeech() {
            showTip(" 开始录音 ");
        }

        //volume 音量值0~30， data音频数据
        public void onVolumeChanged(int volume, byte[] data) {
            showTip(" 声音改变了 ");
        }

        // 结束录音
        public void onEndOfSpeech() {
            showTip(" 结束录音 ");
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
        }
    };
    private void showTip (String data) {
        Toast.makeText( this, data, Toast.LENGTH_SHORT).show() ;
    }
}
