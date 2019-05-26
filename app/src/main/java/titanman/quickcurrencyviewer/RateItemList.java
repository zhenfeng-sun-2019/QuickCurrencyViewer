package titanman.quickcurrencyviewer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class RateItemList implements MyCurrencyAPICallbacks {

    private static final RateItemList mRateItemList = new RateItemList();

    private static ArrayList<RateItem> mSelectedRateItem = new ArrayList<>();

    private static ArrayList<String[]> mSupportedCurrency = new ArrayList<>();

    private static Context mContext;

    private MyCurrencyAPI mMyApi;

    private static MyDataBase mDbHelper;

    private static ArrayList<RateItemListUpdateCallbacks> mActivityCallback = new ArrayList<>();

    private RateItemList() {

    }

    public static RateItemList getInstance(Context context) {
        mContext = context;
        mDbHelper = new MyDataBase(mContext);
        initListData();
        return mRateItemList;
    }

    public void setUiCallback(RateItemListUpdateCallbacks callback) {
        mActivityCallback.add(callback);
    }

    public void removeUiCallback(RateItemListUpdateCallbacks callback) {
        mActivityCallback.remove(callback);
    }

    public ArrayList<RateItem> getDisplayData() {
        return mSelectedRateItem;
    }

    public ArrayList<String[]> getSupportedCurrency() {
        return mSupportedCurrency;
    }

    public void addRateItemToSelectedList(RateItem item) {
        mSelectedRateItem.add(item);
        mDbHelper.insertToDataBase(item);
        if (mActivityCallback != null && mActivityCallback.size() > 0) {
            for (RateItemListUpdateCallbacks callbacks : mActivityCallback) {
                callbacks.onRateListUpdated(true, "");
            }
        }
    }

    private static void initListData() {
        mSelectedRateItem = mDbHelper.readFromDatabase();
    }

    public void requestRateUpdate() {
        if (mMyApi != null) {
            mMyApi.cancel(true);
            mMyApi = null;
        }
        mMyApi = new MyCurrencyAPI();
        mMyApi.setMyCurrencyAPICallback(this);
        mMyApi.requestAllRateUpdate(mSelectedRateItem);
    }

    public void requestSupportedCurrency() {
        Log.e("XXX", "requestSupportedCurrency 1");
        if (mMyApi != null) {
            mMyApi.cancel(true);
            mMyApi = null;
            Log.e("XXX", "requestSupportedCurrency 2");
        }
        mMyApi = new MyCurrencyAPI();
        mMyApi.setMyCurrencyAPICallback(this);
        mMyApi.requestSupportedCurrency();
        Log.e("XXX", "requestSupportedCurrency 3");
    }

    public void requestClearSelectedList() {
        mDbHelper.clearDataBase();
        initListData();
        if (mActivityCallback != null && mActivityCallback.size() > 0) {
            for (RateItemListUpdateCallbacks callbacks : mActivityCallback) {
                callbacks.onRateListUpdated(true, "");
            }
        }
    }

    @Override
    public void onSupportedCurrencyUpdated(boolean success, String error) {
        mSupportedCurrency = mMyApi.getSupportedCurrency();
        if (mActivityCallback != null && mActivityCallback.size() > 0) {
            for (RateItemListUpdateCallbacks callbacks : mActivityCallback) {
                callbacks.onSupportedCurrencyUpdated(success, error);
            }
        }
    }

    @Override
    public void onRateListUpdated(boolean success, String error) {
        if(success) {
            mSelectedRateItem = mMyApi.getUpdatedRateList();
            mDbHelper.saveToDataBase(mSelectedRateItem);
        } else {
            mSelectedRateItem = mDbHelper.readFromDatabase();
        }
        if (mActivityCallback != null && mActivityCallback.size() > 0) {
            for (RateItemListUpdateCallbacks callbacks : mActivityCallback) {
                callbacks.onRateListUpdated(success, error);
            }
        }
    }

    @Override
    public void onRateConvertFinished(boolean success) {
        // To Do
    }
}
