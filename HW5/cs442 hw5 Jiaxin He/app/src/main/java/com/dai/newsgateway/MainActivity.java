package com.dai.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_ARTICLES = "ARTICLES";
    private static final String ACTION_SERVICE = "SERVICE";
    private static final String TAG = "MainActivity";

    private MainActivity ma = this;
    private NewsReceiver newsReceiver;

    private MyPageAdapter pageAdapter;
    private List<Fragment> fragments = new ArrayList<>();
    private ViewPager pager;
    private Menu mymenu;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<Article>  articles = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();

    private String currentCategory = "";
    private String currentSource = "";

    private HashMap<String,Source> sourceHashMap = new HashMap<>();
    private ArrayList<String> sourceNameList = new ArrayList<>();
    private HashMap<String,ArrayList<String>> categoryHashMap = new HashMap<>();

    public class setColorAdapter extends ArrayAdapter{
        public setColorAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = TextView.inflate(getBaseContext(), R.layout.drawer_item, null);
            }
            TextView textView = convertView.findViewById(R.id.text_view);
            Random random = new Random();
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            textView.setTextColor(Color.rgb(r,g,b));
            return super.getView(position, convertView, parent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, NewsService.class);
        startService(intent);


        newsReceiver = new NewsReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_ARTICLES);
        registerReceiver(newsReceiver, intentFilter);


        //setup drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerList = findViewById(R.id.drawer_list);
        drawerList.setAdapter(new setColorAdapter(this,
                R.layout.drawer_item, sourceNameList));



       // drawerList.setBackgroundColor(000000);
        drawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pager.setBackground(null);

                        String current = sourceNameList.get(position);
                        getSupportActionBar().setTitle(current);


                        Intent intentClick = new Intent();
                        intentClick.setAction(ACTION_SERVICE);

                        Source source = sourceHashMap.get(sourceNameList.get(position));

                        Log.d(TAG, "onItemClick: adding selected source id " + source.getId());
                        intentClick.putExtra("sourceID", source.getId());

                        // broadcast the intent
                        sendBroadcast(intentClick);
                        Log.d(TAG, "onItemClick: broadcast sent");

                        // close the drawer
                        drawerLayout.closeDrawer(drawerList);
                        Log.d(TAG, "onItemClick: Source Clicked: " + sourceNameList.get(position));
                    }
                }
        );

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );


        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //set PageViewer and Adapter
        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        if(savedInstanceState != null) {
            sourceHashMap.clear();
            sourceHashMap = (HashMap<String, Source>) savedInstanceState.getSerializable("sourceHashMap");
            sourceNameList.clear();
            sourceNameList = (ArrayList<String>) savedInstanceState.getSerializable("sourceNameList");
            categoryHashMap.clear();
            categoryHashMap = (HashMap<String,ArrayList<String>>) savedInstanceState.getSerializable("categoryHashMap");
            categories = (ArrayList<String>)savedInstanceState.getSerializable("categories");

            Collections.sort(categories);


            if(sourceHashMap.size() != 0 ){
                drawerLayout.setBackgroundResource(0);
                drawerList.setAdapter(new setColorAdapter(this,
                        R.layout.drawer_item, sourceNameList));
                articles = (ArrayList<Article>)savedInstanceState.getSerializable("articles");
                if(articles != null)
                    addFragment(articles);
            }
            else{
                SourceLoaderRunnable arl = new SourceLoaderRunnable(ma,"",handler);
                new Thread(arl).start();
            }

            pageAdapter.notifyDataSetChanged();
            for (int i = 0; i< pageAdapter.getCount(); i++) pageAdapter.notifyChangeInPosition(i);
        }else{

            SourceLoaderRunnable arl = new SourceLoaderRunnable(ma,"",handler);
             new Thread(arl).start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == 1) {
                Object[] objs = (Object[]) msg.obj;
                ArrayList<Source> s = (ArrayList<Source>)objs[0];
                ArrayList<String> c = (ArrayList<String>)objs[1];
                getSources(s, c);

            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("sourceNameList", sourceNameList);
        outState.putSerializable("sourceHashMap", sourceHashMap);
        if(articles !=  null)
            outState.putSerializable("articles", articles);
        outState.putSerializable("categories",categories);
//        outState.putSerializable("menu",(Serializable)mymenu);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
////
    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        Intent intent = new Intent(ma, NewsService.class);
        stopService(intent);
        super.onDestroy();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();


        inflater.inflate(R.menu.menu, menu);
        mymenu = menu;
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mymenu = menu;
        return true;
    }

        public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: drawerToggle " + item);
            return true;
        }

        Log.d(TAG, "onOptionsItemSelected: else, category selected from options menu");
        String category = (String) item.getTitle();
        Log.d(TAG, "onOptionsItemSelected: item.getTitle() is " + category);
        currentCategory = category; // for save instance state

        ArrayList<String> temp = new ArrayList<>(categoryHashMap.get(category));

        sourceNameList.clear();
        sourceNameList.addAll(temp);

        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();

        return super.onOptionsItemSelected(item);

    }
    public void getSources(ArrayList<Source> sources, ArrayList<String> categoriesList) {
        sourceHashMap.clear();
        sourceNameList.clear();
        categoryHashMap.clear();

        categories.addAll(categoriesList);
        Source source;
        String category;
        String name;
        for (int i = 0; i < sources.size(); i++) {
            source = sources.get(i);
            category = source.getCategory();
            name = source.getName();

            sourceNameList.add(name);
            sourceHashMap.put(name, source);

            if (!categoryHashMap.containsKey(category) ) {
                categoryHashMap.put(category, new ArrayList<String>());
            }

            categoryHashMap.get(category).add(name);

        }

        Collections.sort(categories);

        for (String s : categories) {
            mymenu.add(s);
        }


        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
    }

    public class NewsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: intent's action is " + intent.getAction());
            switch (intent.getAction()){
                case ACTION_ARTICLES:
                    try{
                        Log.d(TAG, "onReceive: aaaaaaaaaaaaaaaaaa");
                        articles = (ArrayList<Article>) intent.getSerializableExtra("articles");
                        addFragment(articles);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }


    public void addFragment(ArrayList<Article> articleArrayList){
        for(int i = 0; i < pageAdapter.getCount(); i++ ){
            pageAdapter.notifyChangeInPosition(i);
        }
        if(fragments!=null)
            fragments.clear();
        for (int i = 0; i < articleArrayList.size(); i++) {

            fragments.add(MyFragment.newInstance(ma,articleArrayList.get(i),articleArrayList.size(),i));
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return baseId + position;
        }

        public void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }

}
