package titanman.quickcurrencyviewer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the Main Activity Class When user launches the application.
 * It has the selected currency list to be displayed
 */
public class MainScreenActivity extends AppCompatActivity implements RateItemListUpdateCallbacks {

    private final Long UPDATE_TIMER = 1000 * 60 * 30L;

    private ListView mSelectedList = null;

    private RateItemList mRateList = null;

    private ArrayList<RateItem> mRateListData = null;

    private TextView mInfoText = null;

    private Handler mUpdateHandle = null;

    private String[] mapKeys = new String[]
            {MyConstants.DATA_KEY_SOURCE_CURRENCY,
                    MyConstants.DATA_KEY_RATE,
                    MyConstants.DATA_KEY_TARGET_CURRENCY};

    private int[] mListID = new int[]{
            R.id.source_currency,
            R.id.rate_number,
            R.id.target_currency};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoText = (TextView) findViewById(R.id.text_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSelectedList = (ListView) findViewById(R.id.list_selected);

        mRateList = RateItemList.getInstance(getApplicationContext());
        mRateList.setUiCallback(this);

        mRateListData = mRateList.getDisplayData();
        mUpdateHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                requestRateUpdate();
            }
        };

        requestRateUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRateListView();
        requestRateUpdate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            launchSelectActivity();
            return true;
        }

        if (id == R.id.action_clear) {
            clearRateList();
        }

        if (id == R.id.action_update) {
            requestRateUpdate();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRateListUpdated(boolean success, String error) {
        if (success) {
            mRateListData = mRateList.getDisplayData();
            updateRateListView();
        } else {
            mInfoText.setText(error);
        }
    }

    @Override
    public void onSupportedCurrencyUpdated(boolean success, String error) {
        // Do nothing currently
    }

    @Override
    public void onRateConvertFinished(boolean success, String error) {
        // Do nothing currently
    }


    private void launchSelectActivity() {        Intent select = new Intent();
        select.setClass(getApplicationContext(), SelectScreenActivity.class);
        startActivity(select);
    }

    private void clearRateList() {
        if (mRateList != null) {
            mRateList.requestClearSelectedList();
        }
    }

    private void requestRateUpdate() {
        if (mRateList != null) {
            mRateList.requestRateUpdate();
            mInfoText.setText(R.string.info_updating);
        }
    }

    private void updateRateListView() {
        if (mSelectedList == null) {
            return;
        }
        ArrayList<Map<String, ?>> displayArray = new ArrayList<Map<String, ?>>();
        for (RateItem item : mRateListData) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(mapKeys[0], "1 " + item.getSourceCurrency() + " =");
            map.put(mapKeys[1], item.getRateNumber());
            map.put(mapKeys[2], " " + item.getTargetCurrency());
            displayArray.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, displayArray, R.layout.list_item, mapKeys, mListID);
        mSelectedList.setAdapter(adapter);

        updateInformation();
        mUpdateHandle.removeMessages(1);
        mUpdateHandle.sendEmptyMessageDelayed(1, UPDATE_TIMER);

    }

    private void updateInformation() {
        String infoString;

        if (mRateList.getDisplayData().size() == 0) {
            infoString = getResources().getString(R.string.info_no_items);
            mInfoText.setText(infoString);
            return;
        }

        // Last Updated information
        infoString = getResources().getString(R.string.info_last_updated);
        LocalDateTime localDateTime = LocalDateTime.now();
        infoString += localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        infoString += " "
                + localDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0, 8);
        infoString += "\n";

        // Next update information
        infoString += getResources().getString(R.string.info_next_update);
        localDateTime = localDateTime.plusSeconds(UPDATE_TIMER / 1000);

        infoString += localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);

        infoString += " "
                + localDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0, 8);

        // Next update information Hint
        infoString += "\n";
        infoString += "\n";
        infoString += getResources().getString(R.string.info_update_hint);
        mInfoText.setText(infoString);
    }

}
