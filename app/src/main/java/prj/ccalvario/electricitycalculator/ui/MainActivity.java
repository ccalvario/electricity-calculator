package prj.ccalvario.electricitycalculator.ui;


import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import prj.ccalvario.electricitycalculator.BuildConfig;
import prj.ccalvario.electricitycalculator.R;
import prj.ccalvario.electricitycalculator.adapter.CustomAdapter;
import prj.ccalvario.electricitycalculator.adapter.CustomItemClickListener;
import prj.ccalvario.electricitycalculator.adapter.CustomItemDecoration;
import prj.ccalvario.electricitycalculator.utils.SaveData;
import trikita.log.Log;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_REFRESH_CODE = 1;

    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private TextView mConsumption;
    private TextView mCost;
    private ImageButton mInfo;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private final String APP_ID_ADMOB = "ca-app-pub-5965660713221392~7374699353";//"ca-app-pub-3940256099942544~3347511713" test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        if (!BuildConfig.DEBUG) {
            Log.usePrinter(Log.ANDROID, false); // from now on Log.d etc do nothing and is likely to be optimized with JIT
        }

        FloatingActionButton addItem = findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddItemActivity.class), REQUEST_REFRESH_CODE);
            }
        });

        MobileAds.initialize(this, APP_ID_ADMOB);
        mAdView = findViewById(R.id.adview_main);
        AdRequest adRequest = new AdRequest.Builder().build();
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("72E598EC1A226E6453D6484D88EFF41B").build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.chart_interstitial_ad_unit));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        SaveData.getInstance().Init(this);
        SaveData.getInstance().LogItems();

        mRecyclerView = findViewById(R.id.main_list_items);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(SaveData.getInstance().getItems(), new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                Bundle b = new Bundle();
                b.putInt("position", position);
                intent.putExtras(b);
                startActivityForResult(intent, REQUEST_REFRESH_CODE);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        CustomItemDecoration myItemDecoration = new CustomItemDecoration();
        mRecyclerView.addItemDecoration(myItemDecoration);
        mConsumption = findViewById(R.id.textview_main_consumption);
        mConsumption.setText(SaveData.getInstance().getConsumptionStr());

        mCost = findViewById(R.id.textview_main_cost);
        mCost.setText(SaveData.getInstance().getCostStr());

        mInfo = findViewById(R.id.imageButton_info);
        mInfo.setVisibility(View.GONE);
        double cost = SaveData.getInstance().getCost();
        if(cost == SaveData.ERROR_NO_RATE_SET || cost == SaveData.ERROR_RATE_INVALID) {
            mInfo.setVisibility(View.VISIBLE);
        }
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(getResources().getString(R.string.info_set_rates_dialog_title));
                alertDialog.setMessage(getResources().getString(R.string.info_set_rates));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });


        CardView myCardView = findViewById(R.id.cardview_main);
        myCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ChartActivity.class));
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQUEST_REFRESH_CODE);
                return true;
            //case R.id.action_about:
            //    startActivity(new Intent(MainActivity.this, AboutActivity.class));
            //    return true;
            case R.id.action_rateapp:
                OpenRateApp();
                return true;
            case R.id.action_privacy:
                startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult resultCode " + resultCode + " requestCode " + requestCode);
        if (requestCode == REQUEST_REFRESH_CODE && resultCode == RESULT_OK) {
            mAdapter.setListContent(SaveData.getInstance().getItems());
        }
        mConsumption.setText(SaveData.getInstance().getConsumptionStr());
        mCost.setText(SaveData.getInstance().getCostStr());
        double cost = SaveData.getInstance().getCost();
        if(cost == SaveData.ERROR_NO_RATE_SET || cost == SaveData.ERROR_RATE_INVALID) {
            mInfo.setVisibility(View.VISIBLE);
        } else {
            mInfo.setVisibility(View.GONE);
        }
    }

    private void OpenRateApp(){
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }
}
