package org.echoline.drawterm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, String> map;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public void myFancyMethod(View v) {
        setContentView(R.layout.server_main);
        serverButtons();

        String s = map.get(((TextView)v).getText().toString());
        String []a = s.split("\007");

        ((EditText)MainActivity.this.findViewById(R.id.cpuServer)).setText((String)a[0]);
        ((EditText)MainActivity.this.findViewById(R.id.authServer)).setText((String)a[1]);
        ((EditText)MainActivity.this.findViewById(R.id.userName)).setText((String)a[2]);
        ((EditText)MainActivity.this.findViewById(R.id.passWord)).setText((String)a[3]);
    }

    public void populateServers(Context context) {
        ListView ll = findViewById(R.id.servers);
        ArrayAdapter<String> la = new ArrayAdapter<String>(MainActivity.this, R.layout.item_main);
        SharedPreferences settings = getSharedPreferences("DrawtermPrefs", 0);
        map = (Map<String, String>)settings.getAll();
        String key;
        Object []keys = map.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            key = (String)keys[i];
            la.add(key);
        }
        ll.setAdapter(la);
    }

    public void serverButtons() {
        Button button = (Button)findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cpu = ((EditText)MainActivity.this.findViewById(R.id.cpuServer)).getText().toString();
                String auth = ((EditText)MainActivity.this.findViewById(R.id.authServer)).getText().toString();
                String user = ((EditText)MainActivity.this.findViewById(R.id.userName)).getText().toString();
                String pass = ((EditText)MainActivity.this.findViewById(R.id.passWord)).getText().toString();

                SharedPreferences settings = getSharedPreferences("DrawtermPrefs", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(user + "@" + cpu + "/"  + auth, cpu + "\007" + auth + "\007" + user + "\007" + pass);
                editor.commit();
            }
        });

        button = (Button) findViewById(R.id.connect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String cpu = ((EditText)MainActivity.this.findViewById(R.id.cpuServer)).getText().toString();
                String auth = ((EditText)MainActivity.this.findViewById(R.id.authServer)).getText().toString();
                String user = ((EditText)MainActivity.this.findViewById(R.id.userName)).getText().toString();
                String pass = ((EditText)MainActivity.this.findViewById(R.id.passWord)).getText().toString();
                int w = MainActivity.this.getWindow().getDecorView().getWidth();
                int h = MainActivity.this.getWindow().getDecorView().getHeight() - 30;

                setContentView(R.layout.drawterm_main);
                MySurfaceView mView = new MySurfaceView(MainActivity.this, w, h);
                LinearLayout l = MainActivity.this.findViewById(R.id.dlayout);
                l.addView(mView, 1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                DrawTermThread t = new DrawTermThread(cpu, auth, user, pass, MainActivity.this);
                t.start();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        populateServers(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.server_main);
                serverButtons();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void dtmain(Object[] args);
    public native void setPass(String arg);
    public native void setWidth(int arg);
    public native void setHeight(int arg);
    public native byte[] getScreenData();
    public native void setMouse(int[] args);
}
