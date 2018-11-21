package prj.ccalvario.electricitycalculator;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import trikita.log.Log;

public class RatesActivity extends AppCompatActivity {

    public static final int REQUEST_REFRESH_CODE = 1;

    private RecyclerView mRecyclerView;
    private RateAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);

        mAdView = findViewById(R.id.adview_rates);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        SaveData.getInstance().LogRates();
        FloatingActionButton addItem = findViewById(R.id.fab_rates_add);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(RatesActivity.this, AddRateActivity.class), REQUEST_REFRESH_CODE);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_rates);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RateAdapter(SaveData.getInstance().getRates(), new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(RatesActivity.this, AddRateActivity.class);
                Bundle b = new Bundle();
                b.putInt("index", position);
                intent.putExtras(b);
                startActivityForResult(intent, REQUEST_REFRESH_CODE);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ccz onActivityResult resultCode " + resultCode + " requestCode " + requestCode);
        if (requestCode == REQUEST_REFRESH_CODE && resultCode == RESULT_OK) {
            Log.d("ccz onActivityResult notifyDataSetChanged");
            mAdapter.setListContent(SaveData.getInstance().getRates());
        }
    }
}
