package prj.ccalvario.electricitycalculator;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import trikita.log.Log;

public class AddRateActivity extends AppCompatActivity {

    private TextView mTextViewTitle;
    private TextInputEditText mEditTextKwh;
    private TextInputEditText mEditTextCost;
    private AdView mAdView;

    private String mName = "";
    private int mIndex = -1;

    private boolean mRefreshList = false;
    private boolean mIsLastRate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rate);

        mAdView = findViewById(R.id.adview_addrate);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mEditTextKwh = findViewById(R.id.edittext_addRate_kwh);
        mEditTextCost = findViewById(R.id.edittext_addRate_cost);
        mTextViewTitle = findViewById(R.id.textview_addrate_title);

        Rate[] ratesList = SaveData.getInstance().getRates();

        Bundle b = getIntent().getExtras();
        if(b != null){
            mIndex = b.getInt("index", -1);
        }

        if(mIndex != -1){
            mIsLastRate = (ratesList.length - 1 == mIndex);

            Rate myRate = SaveData.getInstance().getRate(mIndex);
            mTextViewTitle.setText(myRate.getName());
            mName = myRate.getName();
            mEditTextKwh.setText(String.valueOf(myRate.getKwh()));
            mEditTextCost.setText(String.valueOf(myRate.getCost()));
        } else {
            mIsLastRate = true;
            mName = mTextViewTitle.getText() + " " + (ratesList.length + 1);
            mTextViewTitle.setText(mName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_item_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteButton = menu.findItem(R.id.action_delete);
        deleteButton.setVisible(mIsLastRate && mIndex != -1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(ValidateInput()){
                    SaveRate();
                    finish();
                }
                return true;
            case R.id.action_delete:
                DeleteRate();
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        if(mRefreshList){
            setResult(RESULT_OK, returnIntent);
        }
        super.finish();
    }

    private boolean ValidateInput() {
        boolean result = true;

        String strKwh = mEditTextKwh.getText().toString();
        if(!mIsLastRate) {
            if (strKwh.isEmpty()) {
                mEditTextKwh.setError(getResources().getString(R.string.addrate_error_field_empty));
                return false;
            }
        }


        return result;
    }

    private boolean SaveRate(){
        boolean result = true;
        String strKwh = mEditTextKwh.getText().toString();
        int kwh = 0;
        if(!strKwh.isEmpty()) {
            kwh = Integer.parseInt(strKwh);
        }
        else if(mIsLastRate){
            kwh = -1;
        }

        String strCost = mEditTextCost.getText().toString();
        double cost = 0;
        if(!strCost.isEmpty()) {
            cost = Double.parseDouble(strCost);
        }

        Rate myRate = new Rate();
        myRate.setName(mName);
        myRate.setKwh(kwh);
        myRate.setCost(cost);
        myRate.setIndex(mIndex);
        SaveData.getInstance().saveRate(myRate);

        SaveData.getInstance().LogRates();
        SaveData.getInstance().eventLogSetTieredRate();

        mRefreshList = true;

        return true;
    }

    private boolean DeleteRate(){
        boolean result = true;
        if(mIndex != -1) {
            SaveData.getInstance().removeRate(mIndex);
            mRefreshList = true;
        }

        return result;
    }
}
