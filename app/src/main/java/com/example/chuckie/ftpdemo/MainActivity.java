package com.example.chuckie.ftpdemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chuckie.ftpdemo.chuxFTPClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //ContexMenu
    final int DOWNLOAD_ID = Menu.FIRST;
    final int RENAME_ID = Menu.FIRST+1;
    final int DELETE_ID = Menu.FIRST+2;
    final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()+"/chuxFtpDownload";

    private List<String> remoteDirectory = new LinkedList<String>();

    private chuxFTPClient ftp = new chuxFTPClient();
    private ListView lv;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        isConnected = false;

        Intent intent = getIntent();
        String host = intent.getStringExtra("host");
        String port = intent.getStringExtra("port");
        String user = intent.getStringExtra("user");
        String password = intent.getStringExtra("password");
        System.out.println(host + port + user + password);

        final asyncFtp client = new asyncFtp(host,port,user,password);
        client.execute("");

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view;
                String choosed = (String) tv.getText();
                if (choosed.startsWith("/")) {
                    if(choosed.startsWith("/..") && !remoteDirectory.isEmpty()){
                        remoteDirectory.remove(remoteDirectory.size()-1);
                    }else{
                        remoteDirectory.add(choosed);
                    }
                    System.out.println(">>>"+remoteDirectory.toString());
                    asyncDisplay display = new asyncDisplay();
                    display.execute(sRemoteDirectory());

                } else {
                    System.out.println("file");
                }
                Toast.makeText(getApplicationContext(), "List Item " + tv.getText() + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });
//
//
//        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextView tv = (TextView) view;
//                Toast.makeText(getApplicationContext(),"List Item "+tv.getText()+" was Longclicked!",Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });


        registerForContextMenu(lv);
    }

    public String sRemoteDirectory(){
        String sremote = new String();
        for (String s : remoteDirectory){
            sremote = sremote + s;
        }
        return sremote;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DOWNLOAD_ID, 0, "Download");
        menu.add(0,RENAME_ID,0,"Rename");
        menu.add(0, DELETE_ID, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        System.out.println(item.getItemId());
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        TextView tv = (TextView) info.targetView;
        String sremote = sRemoteDirectory();
        switch (item.getItemId()){
            case 1:
                Toast.makeText(this,"Download "+tv.getText().toString(),Toast.LENGTH_SHORT).show();
                if (tv.getText().toString().startsWith("/")){
                    Toast.makeText(this,"Directory Can't be Downloaded",Toast.LENGTH_SHORT).show();
                }else{
                    File dir = new File(DOWNLOAD_PATH);
                    if(!dir.exists()){dir.mkdirs();}
                    asyncDownload download = new asyncDownload();
                    download.execute(sremote+"/"+tv.getText().toString(), DOWNLOAD_PATH);
                    System.out.println(sremote + "/" + tv.getText().toString());
                    System.out.println(DOWNLOAD_PATH);
                }
                break;

            case 2:
                Toast.makeText(this,"Rename "+tv.getText().toString(),Toast.LENGTH_SHORT).show();
                final String[] newname = {sremote};
                final String[] oldname = {sremote};
                if(tv.getText().toString().startsWith("/")){
                    oldname[0] = oldname[0]+tv.getText().toString();
                }else{
                    oldname[0] = oldname[0]+"/"+tv.getText().toString();
                }
                final EditText inputDia = new EditText(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Input New Name").setView(inputDia);
                builder.setNegativeButton("Cancle", null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        newname[0] = newname[0] + "/" + inputDia.getText().toString();
//                        System.out.println(newname[0]);
                        asyncRename rename = new asyncRename();
                        rename.execute(oldname[0], newname[0]);
                        asyncDisplay display_r = new asyncDisplay();
                        display_r.execute(sRemoteDirectory());
                    }
                });
                builder.show();
                break;

            case 3:
                Toast.makeText(this,"Delete "+tv.getText().toString(),Toast.LENGTH_SHORT).show();
                asyncDelete delete = new asyncDelete();
                delete.execute(sremote+"/"+tv.getText().toString());
                asyncDisplay display_d = new asyncDisplay();
                display_d.execute(sremote);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public class asyncDownload extends AsyncTask<String,Integer,Integer>{

        public asyncDownload() {

        }

        @Override
        protected Integer doInBackground(String[] objects) {
            if(isConnected){
                try {
                    ftp.downLoad(objects[0],objects[1]);
                    return 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer o ) {
            if(o!=0){
                System.out.println("Downloaded!");
                Toast.makeText(MainActivity.this,"File Downloaded to "+DOWNLOAD_PATH,Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class asyncRename extends  AsyncTask<String,Integer,String>{

        public asyncRename(){

        }


        @Override
        protected String doInBackground(String... strings) {
            if (isConnected){
                try {
                    String res = ftp.renameFile(strings[0],strings[1]);
                    return res;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
        }
    }

    public class asyncDelete extends AsyncTask<String,Integer,String>{

        public asyncDelete() {

        }

        @Override
        protected String doInBackground(String[] objects) {
            if(isConnected){
                try {
                    String res = ftp.delStuff(objects[0]);
                    return res;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String o ) {
            if(o!=null){
                System.out.println("Deleted!");
                Toast.makeText(MainActivity.this,o,Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class asyncFtp extends AsyncTask<String,Integer,List<String>>{
        private final String mHost;
        private final String mPort;
        private final String mUser;
        private final String mPassword;

        public asyncFtp(String host,String port,String user,String password) {
            mHost = host;
            mPort = port;
            mUser = user;
            mPassword = password;
        }

        @Override
        protected List<String> doInBackground(String[] objects) {
                isConnected = ftp.tryConnect(mHost, Integer.parseInt(mPort),mUser,mPassword);
                try {
                    System.out.println(objects[0]);
                    List<String> fileListFirst = ftp.disPlay(objects[0]);
                    return  fileListFirst;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> o ) {
            System.out.println(o);
            o.add(0,"/..");
            lv.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,o));
            System.out.println("Done");
        }
    }

    public class asyncDisplay extends AsyncTask<String,Integer,List<String>>{

        public asyncDisplay() {

        }

        @Override
        protected List<String> doInBackground(String[] objects) {
            if(isConnected){
                try {
                    System.out.println(objects[0]);
                    List<String> fileListDisplay = ftp.disPlay(objects[0]);
                    return  fileListDisplay;
                    } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> o ) {
            System.out.println(o);
            o.add(0,"/..");
            lv.setAdapter(new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,o));
            System.out.println("Done");
        }
    }

    public class asyncDisconnect extends AsyncTask<String,Integer,Integer>{

        public asyncDisconnect() {

        }

        @Override
        protected Integer doInBackground(String[] objects) {
            if(isConnected){
                try {
                    ftp.disConnect();
                    return 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer o ) {
            if(o!=0){
                System.out.println("Disconnected");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected){
            asyncDisconnect disconnect = new asyncDisconnect();
            disconnect.execute();
        }
    }
}