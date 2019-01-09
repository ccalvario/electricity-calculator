package prj.ccalvario.electricitycalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;

import trikita.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class SaveData {

    private static SaveData INSTANCE = null;
    private Context mContext = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private JSONArray mItemList;
    private JSONArray mRatesList;
    private JSONArray mSamplesList;

    private final String mSamplesFile = "samples.json";
    private final String mPrefNameItems = "itemsList";
    private final String mKeyItems = "items";
    private final String mPrefNameRates = "ratesList";
    private final String mKeyRates = "rates";

    private final String mLogEventAddItem = "add_item";
    private final String mLogEventEditItem = "edit_item";
    private final String mLogEventDeleteItem = "delete_item";
    private final String mLogEventPickItem = "pick_item";
    private final String mLogEventSetRate = "myevent_set_rate";
    private final String mLogEventSetTieredRate = "myevent_set_tiered_rate";
    private final String mLogEventOpenChart = "myevent_open_chart";

    private SaveData() {};

    public static SaveData getInstance() {
        if(INSTANCE == null){
            INSTANCE = new SaveData();
        }
        return INSTANCE;
    }

    public void Init(Context context){
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mItemList = loadJSONArray(mPrefNameItems, mKeyItems);
        mRatesList = loadJSONArray(mPrefNameRates, mKeyRates);
        loadSamplesFile();
        getCost();

        //SampleItem item = getSampleItem(1, 1);
        //Log.d("SampleItem " + item.getName() + " w " + item.getWatts());
    }

    private void saveJSONArray(String prefName, String key, JSONArray array) {
        SharedPreferences settings = mContext.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, array.toString());
        editor.commit();
    }

    private JSONArray loadJSONArray(String prefName, String key) {
        try {
            SharedPreferences settings = mContext.getSharedPreferences(prefName, 0);
            return new JSONArray(settings.getString(key, "[]"));
        }
        catch (JSONException e) {
            Log.d("JSONException loadJSONArray "+ e.getMessage());
            return new JSONArray();
        }
    }

    public void saveItem(Item pItem){
        if(pItem.getPosition() != -1){
            editItem(pItem);
        }
        else{
            addItem(pItem);
        }
    }

    public void removeItem(int pos){
        mItemList.remove(pos);
        saveJSONArray(mPrefNameItems, mKeyItems, mItemList);
        mItemList = loadJSONArray(mPrefNameItems, mKeyItems);
    }

    public void removeItem(Item pItem){
        mItemList.remove(pItem.getPosition());
    }

    private void addItem(Item pItem){
        JSONObject item = new JSONObject();
        try {
            item.put(Item.NAME_KEY, pItem.getName());
            item.put(Item.DAYS_KEY, pItem.getDays());
            item.put(Item.TIME_KEY, pItem.getTime());
            item.put(Item.WATTS_KEY, pItem.getWatts());
            item.put(Item.QUANTITY_KEY, pItem.getQuantity());

            mItemList.put(item);
            saveJSONArray(mPrefNameItems, mKeyItems, mItemList);
        }
        catch (JSONException e) {}
    }

    private void editItem(Item pItem){
        Item[] itemList = getItems();
        if(pItem.getPosition() != -1){
            itemList[pItem.getPosition()] = pItem;
        }
        mItemList = new JSONArray();
        for(int i = 0; i < itemList.length; i++){
            JSONObject item = new JSONObject();
            try {
                item.put(Item.NAME_KEY, itemList[i].getName());
                item.put(Item.DAYS_KEY, itemList[i].getDays());
                item.put(Item.TIME_KEY, itemList[i].getTime());
                item.put(Item.WATTS_KEY, itemList[i].getWatts());
                item.put(Item.QUANTITY_KEY, itemList[i].getQuantity());

                mItemList.put(item);
            }
            catch (JSONException e) {}
        }
        saveJSONArray(mPrefNameItems, mKeyItems, mItemList);
    }

    public Item[] getItems(){
        int length = mItemList.length();
        Item[] itemList = new Item[length];
        try {
            for (int i = 0; i < length; i++) {
                JSONObject item = mItemList.getJSONObject(i);
                if(item != null){
                    itemList[i] = new Item();
                    itemList[i].setName(item.getString(Item.NAME_KEY));
                    itemList[i].setDays(item.getInt(Item.DAYS_KEY));
                    itemList[i].setTime(item.getInt(Item.TIME_KEY));
                    itemList[i].setWatts(item.getInt(Item.WATTS_KEY));
                    itemList[i].setQuantity(item.getInt(Item.QUANTITY_KEY));
                    itemList[i].setPosition(i);
                }
            }
        }
        catch (JSONException e) {}

        return itemList;
    }

    public Item getItem(int pos){

        Item myItem = new Item();
        try {
            JSONObject jsonItem = mItemList.getJSONObject(pos);
            if (jsonItem != null) {

                myItem.setName(jsonItem.getString(Item.NAME_KEY));
                myItem.setDays(jsonItem.getInt(Item.DAYS_KEY));
                myItem.setTime(jsonItem.getInt(Item.TIME_KEY));
                myItem.setWatts(jsonItem.getInt(Item.WATTS_KEY));
                myItem.setQuantity(jsonItem.getInt(Item.QUANTITY_KEY));
                myItem.setPosition(pos);
            }
        }
        catch (JSONException e) {}

        return myItem;
    }

    public double getConsumption(){
        Item[] itemsList = getItems();
        double consumption = 0;
        for(int i = 0; i < itemsList.length; i++){
            consumption += itemsList[i].getConsumption();
        }
        return consumption;
    }

    public String getConsumptionStr(){
        double consumption = SaveData.getInstance().getConsumption();
        String result;
        double watts = 0;

        if(consumption > 10000){
            watts = consumption / 1000;
            result = String.format("%1$,.2f", watts) + " MWh";
        }
        else{
            result = String.format("%1$,.2f", consumption) + " kWh";
        }
        return result;
    }

    public Item[] getItemsChart(){
        Item[] itemsList = getItems();
        double totalConsumption = getConsumption();
        for(int i = 0; i < itemsList.length; i++){
            double consumption = itemsList[i].getConsumption();
            double percentage = (consumption / totalConsumption) * 100;
            itemsList[i].setPercentage((float)percentage);
        }
        Arrays.sort(itemsList, new SortByPercentage());

        int maxListLength = 10;
        if(itemsList.length > maxListLength){
            Item[] newItemsList = new Item[maxListLength];
            float percentage = 0;
            for(int i = 0; i < maxListLength - 1; i++){
                newItemsList[i] = itemsList[i];
                percentage += itemsList[i].getPercentage();
            }
            newItemsList[maxListLength - 1] = new Item();
            newItemsList[maxListLength - 1].setName(mContext.getResources().getString(R.string.chart_others));
            newItemsList[maxListLength - 1].setPercentage(100 - percentage);
            return newItemsList;
        }
        return itemsList;
    }

    public void LogItems(){
        Log.d("Logging Items");
        int length = mItemList.length();
        try {
            for (int i = 0; i < length; i++) {
                JSONObject item = mItemList.getJSONObject(i);
                if(item != null){
                    Log.d("Item name: "+ item.getString(Item.NAME_KEY)
                            + " days: "+ item.getString(Item.DAYS_KEY)
                            + " time: "+ item.getString(Item.TIME_KEY)
                            + " watts: "+ item.getString(Item.WATTS_KEY)
                            + " number: "+ item.getString(Item.QUANTITY_KEY));
                }
            }
        }
        catch (JSONException e) {}
    }

    public static String convertTime(int minutes){
        int hour = minutes / 60;
        int minute = minutes % 60;
        String result = hour + ":" + String.format("%02d", minute);
        return result;
    }

    private String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            Log.d("loadJSONFromAsset " + e.getMessage());
            return null;
        }
        return json;
    }

    private void loadSamplesFile(){
        mSamplesList = new JSONArray();
        try {
            String fileStr = loadJSONFromAsset(mSamplesFile);
            mSamplesList = new JSONArray(fileStr);
        }
        catch (JSONException e) {
            Log.d("loadSamplesFile " + e.getMessage());
        }
    }

    public SampleItem getSampleItem(int categoryId, int id){
        SampleItem sItem = new SampleItem();
        try{
            JSONObject category = mSamplesList.getJSONObject(categoryId);
            if(category != null){
                JSONArray items = category.getJSONArray("items");
                JSONObject item = items.getJSONObject(id);
                if(item != null){
                    sItem.setName(getStringResourceByName(item.getString("name")));
                    sItem.setCategoryId(categoryId);
                    sItem.setItemId(id);
                    sItem.setWatts(item.getInt("watts"));
                }
            }

        }
        catch (JSONException e) {
            Log.d("getSampleItem " + e.getMessage());
        }
        return sItem;
    }

    public String getCategoryName(int categoryId){
        String name = "";
        try{
            JSONObject category = mSamplesList.getJSONObject(categoryId);
            if(category != null){
                name  = getStringResourceByName(category.getString("categoryName"));
            }
        }
        catch (JSONException e) {
            Log.d("getCategoryName " + e.getMessage());
        }
        return name;
    }

    public int getLengthCatSampleList(){
        return mSamplesList.length();
    }

    public int getLenghtSampleItems(int categoryId){
        int length = 0;
        try{
            JSONObject category = mSamplesList.getJSONObject(categoryId);
            if(category != null){
                JSONArray items = category.getJSONArray("items");
                length = items.length();
            }
        }
        catch (JSONException e) {
            Log.d("getLenghtSampleItems " + e.getMessage());
        }
        return length;
    }

    public Rate[] getRates(){
        int length = mRatesList.length();
        Rate[] rateList = new Rate[length];
        try {
            for (int i = 0; i < length; i++) {
                JSONObject rate = mRatesList.getJSONObject(i);
                if(rate != null){
                    rateList[i] = new Rate();
                    rateList[i].setName(rate.getString(Rate.NAME_KEY));
                    rateList[i].setKwh(rate.getInt(Rate.KWH_KEY));
                    rateList[i].setCost(rate.getDouble(Rate.COST_KEY));
                    rateList[i].setIndex(rate.getInt(Rate.INDEX_KEY));
                    rateList[i].setLastRate(length - 1 == i );

                }
            }
        }
        catch (JSONException e) {}

        return rateList;
    }

    public Rate getRate(int index){

        Rate myRate = new Rate();
        try {
            JSONObject jsonItem = mRatesList.getJSONObject(index);
            String name = jsonItem.getString(Rate.NAME_KEY);
            if(name.isEmpty() || name == null){
                return null;
            }
            if (jsonItem != null) {
                myRate.setName(jsonItem.getString(Rate.NAME_KEY));
                myRate.setKwh(jsonItem.getInt(Rate.KWH_KEY));
                myRate.setCost(jsonItem.getDouble(Rate.COST_KEY));
                myRate.setIndex(jsonItem.getInt(Rate.INDEX_KEY));
            }
        }
        catch (JSONException e) {
            Log.d("JSONException getRate " + e.getMessage());
            myRate = null;
        }

        return myRate;
    }

    private void addRate(Rate pRate){
        JSONObject rate = new JSONObject();
        try {
            rate.put(Rate.NAME_KEY, pRate.getName());
            rate.put(Rate.KWH_KEY, pRate.getKwh());
            rate.put(Rate.COST_KEY, pRate.getCost());
            rate.put(Rate.INDEX_KEY, mRatesList.length());

            mRatesList.put(rate);
            saveJSONArray(mPrefNameRates, mKeyRates, mRatesList);
        }
        catch (JSONException e) { Log.d("JSONException addRate "+ e.getMessage());}
    }

    private void editRate(Rate pRate){
        Rate[] rateList = getRates();
        if(pRate.getIndex() != -1){
            rateList[pRate.getIndex()] = pRate;
        }
        mRatesList = new JSONArray();
        try {
            for (int i = 0; i < rateList.length; i++) {
                JSONObject rate = new JSONObject();

                rate.put(Rate.NAME_KEY, rateList[i].getName());
                rate.put(Rate.KWH_KEY, rateList[i].getKwh());
                rate.put(Rate.COST_KEY, rateList[i].getCost());
                rate.put(Rate.INDEX_KEY, rateList[i].getIndex());

                mRatesList.put(rate);

            }
            saveJSONArray(mPrefNameRates, mKeyRates, mRatesList);
        }
        catch (JSONException e) { Log.d("JSONException editRate "+ e.getMessage());}

    }

    public void removeRate(int index){
        mRatesList.remove(index);
        saveJSONArray(mPrefNameRates, mKeyRates, mRatesList);
        mRatesList = loadJSONArray(mPrefNameRates, mKeyRates);
    }

    public void saveRate(Rate pRate){
        if(pRate.getIndex() != -1){
            editRate(pRate);
        }
        else{
            addRate(pRate);
        }
    }

    public void LogRates(){
        Log.d("Logging Rates");
        int length = mRatesList.length();
        try {
            for (int i = 0; i < length; i++) {
                JSONObject rate = mRatesList.getJSONObject(i);
                if(rate != null){
                    Log.d("Item name: "+ rate.getString(Rate.NAME_KEY)
                            + " kwh: "+ rate.getString(Rate.KWH_KEY)
                            + " cost: "+ rate.getString(Rate.COST_KEY)
                            + " index: "+ rate.getString(Rate.INDEX_KEY));
                }
            }
        }
        catch (JSONException e) { Log.d("JSONException LogRates "+ e.getMessage());}
    }

    public void removeRate(Rate pRate){
        mRatesList.remove(pRate.getIndex());
    }

    public String getCostStr(){
        double cost = getCost();
        if(cost == -1){
            return "$ Error";
        } else {
            return "$ " + String.format("%1$,.2f", cost);
        }
    }

    public double getCost(){
        double cost = 0;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String rateType = sharedPref.getString(mContext.getString(R.string.key_rate_type), "");
        if(rateType.isEmpty()){
            return cost;
        }
        boolean isFixedRate = rateType.equals("0");
        double consumptionKWh = getConsumption();

        if(isFixedRate){
            String fixedRate = sharedPref.getString(mContext.getString(R.string.key_fixed_rate), "0");
            if(fixedRate == null || fixedRate.isEmpty()) {
                fixedRate = "0";
            }
            float rate = Float.parseFloat(fixedRate);
            cost = rate * consumptionKWh;
        } else {
            Rate[] rates = getRates();
            for(int i = 0; i < rates.length; i++){
                Rate myRate = rates[i];
                double kWh = 0;

                if(myRate.getKwh() == -1 && (rates.length -1 != myRate.getIndex())){
                    return -1;
                } else if(myRate.getKwh() == -1){
                    kWh = consumptionKWh;
                } else{
                    kWh = Math.min(consumptionKWh, myRate.getKwh());
                }
                cost += kWh * myRate.getCost();
                consumptionKWh -= kWh;
                if(consumptionKWh <= 0){
                    break;
                }
            }
            if(consumptionKWh > 0){
                Log.d("Error consumptionKWh " + consumptionKWh);
                return -1;
            }
        }

        return cost;
    }

    private String getStringResourceByName(String name) {
        String packageName = mContext.getPackageName();
        int resId = mContext.getResources().getIdentifier(name, "string", packageName);
        if(resId == 0){
            return name;
        }
        return mContext.getString(resId);
    }

    public void eventLogSaveItem(){
        mFirebaseAnalytics.logEvent(mLogEventAddItem, new Bundle());
    }

    public void eventLogEditItem(){
        mFirebaseAnalytics.logEvent(mLogEventEditItem, new Bundle());
    }

    public void eventLogDeleteItem(){ mFirebaseAnalytics.logEvent(mLogEventDeleteItem, new Bundle()); }

    public void eventLogPickItem(){
        mFirebaseAnalytics.logEvent(mLogEventPickItem, new Bundle());
    }

    public void eventLogSetRate(){
        mFirebaseAnalytics.logEvent(mLogEventSetRate, new Bundle());
    }

    public void eventLogOpenChart(){
        mFirebaseAnalytics.logEvent(mLogEventOpenChart, new Bundle());
    }

    public void eventLogSetTieredRate(){
        mFirebaseAnalytics.logEvent(mLogEventSetTieredRate, new Bundle());
    }

    class SortByPercentage implements Comparator<Item>
    {
        public int compare(Item a, Item b)
        {
            if (a.getPercentage() > b.getPercentage()) {
                return -1;
            }
            else if (a.getPercentage() < b.getPercentage()) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
