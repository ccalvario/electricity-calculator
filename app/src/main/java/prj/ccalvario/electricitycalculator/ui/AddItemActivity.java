package prj.ccalvario.electricitycalculator.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TimePicker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import prj.ccalvario.electricitycalculator.R;
import prj.ccalvario.electricitycalculator.adapter.CustomExpandableListAdapter;
import prj.ccalvario.electricitycalculator.model.Item;
import prj.ccalvario.electricitycalculator.model.SampleItem;
import prj.ccalvario.electricitycalculator.utils.ExpandableListDataPump;
import prj.ccalvario.electricitycalculator.utils.SaveData;

public class AddItemActivity extends AppCompatActivity {

    private int mPosition = -1;
    private int mTimeMinutes = 0;
    private boolean mRefreshList = false;
    private String mName;

    private TextInputEditText mEditTextName;
    private TextInputEditText mEditTextDays;
    private TextInputEditText mEditTextTime;
    private TextInputEditText mEditTextWatts;
    private TextInputEditText mEditTextQuantity;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mAdView = findViewById(R.id.adview_additem);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mEditTextName = findViewById(R.id.edittext_additem_name);
        mEditTextDays = findViewById(R.id.edittext_additem_days);
        mEditTextTime = findViewById(R.id.edittext_additem_time);
        mEditTextWatts = findViewById(R.id.edittext_additem_watts);
        mEditTextQuantity = findViewById(R.id.edittext_additem_quantity);

        Bundle b = getIntent().getExtras();
        if(b != null){

            mName = b.getString("name");
            String watts = b.getString("watts");
            if(mName == null){
                mPosition = b.getInt("position");
            }
            else {
                mEditTextName.setText(mName);
                mEditTextWatts.setText(watts);
                mEditTextDays.setText(String.valueOf(30));
                mEditTextQuantity.setText(String.valueOf(1));
            }
            //Log.d("add item name "+ mName + " watts "+ watts);
        }

        if(mPosition != -1){
            Item myItem = SaveData.getInstance().getItem(mPosition);
            mEditTextName.setText(myItem.getName());
            mEditTextDays.setText(String.valueOf(myItem.getDays()));
            mTimeMinutes = myItem.getTime();
            String time = SaveData.convertTime(mTimeMinutes);
            mEditTextTime.setText(time);
            mEditTextWatts.setText(String.valueOf(myItem.getWatts()));
            mEditTextQuantity.setText(String.valueOf(myItem.getQuantity()));
        }else{
            mEditTextQuantity.setText("1");
        }

        mEditTextTime.setInputType(0);
        mEditTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    int hour = mTimeMinutes / 60;
                    int minute = mTimeMinutes % 60;

                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(AddItemActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            mTimeMinutes = selectedHour * 60 + selectedMinute;
                            String time = SaveData.convertTime(mTimeMinutes);
                            mEditTextTime.setText( time);
                            //mEditTextTime.setText( selectedHour + ":" + selectedMinute);

                            mEditTextName.requestFocus();
                        }
                    }, hour, minute, true);
                    mTimePicker.setTitle(getResources().getString(R.string.additem_time_picker));
                    mTimePicker.show();

                    mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mEditTextName.requestFocus();
                        }
                    });

                } else {
                    //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.showSoftInput(view, 0);
                }
            }
        });

        View v = (View)findViewById(R.id.pick_item);
        if(mName != null || mPosition != -1){
            v.setVisibility(View.GONE);
        }


        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickItemDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater  inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_item_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(ValidateInput()){
                    SaveItem();
                    finish();
                }
                return true;
            case R.id.action_delete:
                DeleteItem();
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteButton = menu.findItem(R.id.action_delete);
        deleteButton.setVisible(mPosition != -1);
        return true;
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        if(mRefreshList){
            setResult(RESULT_OK, returnIntent);
        }
        super.finish();
    }

    private boolean ValidateInput(){
        boolean result = true;

        if(mEditTextName.getText().toString().isEmpty()){
            mEditTextName.setError(getResources().getString(R.string.additem_error_field_empty));
            result = false;
        }

        if(mEditTextQuantity.getText().toString().isEmpty()){
            mEditTextQuantity.setError(getResources().getString(R.string.additem_error_field_empty));
            result = false;
        }

        String daysStr = mEditTextDays.getText().toString();


        if(daysStr.isEmpty()){
            mEditTextDays.setError(getResources().getString(R.string.additem_error_field_empty));
            result = false;
        } else {
            int days = Integer.parseInt(daysStr);
            if(days > 30 ){
                mEditTextDays.setError(getResources().getString(R.string.additem_error_days));
                result = false;
            }
        }

        if(mEditTextWatts.getText().toString().isEmpty()){
            mEditTextWatts.setError(getResources().getString(R.string.additem_error_field_empty));
            result = false;
        }

        if(mEditTextTime.getText().toString().isEmpty()){
            mEditTextTime.setError(getResources().getString(R.string.additem_error_field_empty));
            result = false;
        }
        else{
            mEditTextTime.setError(null);
        }

        return result;
    }

    private boolean SaveItem(){
        boolean result = true;

        String name = mEditTextName.getText().toString();

        String strDays = mEditTextDays.getText().toString();
        int days = 0;
        if(!strDays.isEmpty()) {
            days = Integer.parseInt(strDays);
        }

        String strWatts = mEditTextWatts.getText().toString();
        int watts = 0;
        if(!strWatts.isEmpty()) {
            watts = Integer.parseInt(strWatts);
        }

        String strQuantity = mEditTextQuantity.getText().toString();
        int quantity = 0;
        if(!strQuantity.isEmpty()) {
            quantity = Integer.parseInt(strQuantity);
        }

        Item myItem = new Item();
        myItem.setName(name);
        myItem.setDays(days);
        myItem.setTime((mTimeMinutes));
        myItem.setWatts((watts));
        myItem.setQuantity((quantity));
        myItem.setPosition(mPosition);
        SaveData.getInstance().saveItem(myItem);
        SaveData.getInstance().LogItems();
        if(mPosition != -1) {
            SaveData.getInstance().eventLogEditItem();
        } else {
            SaveData.getInstance().eventLogSaveItem();
        }
        mRefreshList = true;

        return true;
    }

    private boolean DeleteItem(){
        boolean result = true;
        if(mPosition != -1) {
            SaveData.getInstance().removeItem(mPosition);
            SaveData.getInstance().eventLogDeleteItem();
            mRefreshList = true;
        }

        return result;
    }

    public void showPickItemDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddItemActivity.this);
        builder.setTitle(getResources().getString(R.string.samples_dialog_title));

        ExpandableListView expandableListView;
        ExpandableListAdapter expandableListAdapter;
        final List<String> expandableListTitle;
        final HashMap<String, List<String>> expandableListDetail;

        expandableListView = new ExpandableListView(this);
        expandableListDetail = ExpandableListDataPump.getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        builder.setView(expandableListView);
        AlertDialog dialog = builder.create();
        dialog.show();

        /*expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });*/

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                SampleItem item = SaveData.getInstance().getSampleItem(groupPosition, childPosition);

                Intent intent = new Intent(AddItemActivity.this, AddItemActivity.class);
                Bundle b = new Bundle();
                b.putString("name", item.getName());
                b.putString("watts", String.valueOf(item.getWatts()));
                intent.putExtras(b);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                //startActivityForResult(intent, MainActivity.REQUEST_REFRESH_CODE);
                SaveData.getInstance().eventLogPickItem();
                startActivity(intent);
                //startActivityForResult(new Intent(AddItemActivity.this, AddItemActivity.class), MainActivity.REQUEST_REFRESH_CODE);
                finish();
                return false;
            }
        });


    }
}
