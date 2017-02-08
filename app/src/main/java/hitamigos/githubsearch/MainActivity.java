package hitamigos.githubsearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class .getSimpleName();
    public final static String EXTRA_MESSAGE = "uno.meng.download.MESSAGE";
    public void sendMessage(){
        EditText editText = (EditText)findViewById(R.id.message);
        String message = editText.getText().toString();
        if(message.trim() != null && !message.equals(" ")){
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra(EXTRA_MESSAGE,message);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        EditText editText = (EditText)findViewById(R.id.message);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER) {
                    sendMessage();
                }
                return false;
            }
        });
        Button btn = (Button)findViewById(R.id.buttonsearch);
        btn .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //你要执行的代码
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.rss:
                Toast.makeText(MainActivity.this, ""+"！", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
