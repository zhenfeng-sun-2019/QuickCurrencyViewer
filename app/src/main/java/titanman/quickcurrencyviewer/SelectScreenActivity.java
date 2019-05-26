package titanman.quickcurrencyviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectScreenActivity extends AppCompatActivity implements RateItemListUpdateCallbacks {

    private ListView mSupportedListSource;
    private ListView mSupportedListTarget;

    private TextView mTextSelectedSource;
    private TextView mTextSelectedTarget;

    private Button mButtonAdd;

    private SearchView mSearchSource;
    private SearchView mSearchTarget;

    private RateItemList mRateItemList = null;
    private ArrayList<String[]> mSupportedCurrency = null;

    SimpleAdapter mAdapterSource;
    SimpleAdapter mAdapterTarget;

    @Override
    protected void onResume() {
        super.onResume();
        if (mRateItemList != null) {
            mRateItemList.setUiCallback(this);
            mRateItemList.requestSupportedCurrency();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRateItemList != null) {
            mRateItemList.removeUiCallback(this);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initScreenView();

        mRateItemList = RateItemList.getInstance(getApplicationContext());
    }

    private void initScreenView() {
        mSupportedListSource = (ListView) findViewById(R.id.list_supported_source);
        mSupportedListTarget = (ListView) findViewById(R.id.list_supported_target);

        mTextSelectedSource = (TextView) findViewById(R.id.selected_source);
        mTextSelectedTarget = (TextView) findViewById(R.id.selected_target);

        mSearchSource = (SearchView) findViewById(R.id.search_source);
        mSearchTarget = (SearchView) findViewById(R.id.search_target);

        mButtonAdd = (Button) findViewById(R.id.button_add);

        if (mButtonAdd == null) {
            return;
        }

        mButtonAdd.setEnabled(false);
        // Source Currency List
        ListView.OnItemClickListener source_listener = new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Map<String, String> item = (Map<String, String>)mAdapterSource.getItem(position);
                 if(item != null) {
                     mTextSelectedSource.setText(item.get(MyConstants.DATA_KEY_CURRENCY_CODE));
                     enableAddButton();
                 }
            }
        };
        mSupportedListSource.setOnItemClickListener(source_listener);


        // Target Currency List
        ListView.OnItemClickListener target_listener = new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> item = (Map<String, String>)mAdapterTarget.getItem(position);
                if(item != null) {
                    mTextSelectedTarget.setText(item.get(MyConstants.DATA_KEY_CURRENCY_CODE));
                    enableAddButton();
                }
            }
        };
        mSupportedListTarget.setOnItemClickListener(target_listener);

        // "ADD" Button
        View.OnClickListener button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = (String) mTextSelectedSource.getText();
                String target = (String) mTextSelectedTarget.getText();
                String rate = "--.--";

                RateItem item = new RateItem(source, rate, target);
                mRateItemList.addRateItemToSelectedList(item);
                finish();
            }
        };

        mButtonAdd.setOnClickListener(button_listener);

        mSearchSource.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapterSource != null) {
                    mAdapterSource.getFilter().filter(newText);
                }
                return false;
            }
        });


        mSearchTarget.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapterTarget != null) {
                    mAdapterTarget.getFilter().filter(newText);
                }
                return false;
            }
        });

    }

    private void enableAddButton() {
        boolean source_OK = !mTextSelectedSource.getText().equals(getResources().getString(R.string.currency_not_selected));
        boolean target_OK = !mTextSelectedTarget.getText().equals(getResources().getString(R.string.currency_not_selected));
        if (source_OK && target_OK) {
            mButtonAdd.setEnabled(true);
        }
    }

    private void updateSupportedListView() {
        String[] mapKeys =
                new String[]{MyConstants.DATA_KEY_CURRENCY_CODE,
                        MyConstants.DATA_KEY_CURRENCY_FULLNAME};

        int[] listID = new int[]{R.id.currency_code,
                R.id.currency_fullname,};

        if (mSupportedCurrency == null) {
            return;
        }

        ArrayList<Map<String, ?>> displayArray = new ArrayList<Map<String, ?>>();

        for (String[] item : mSupportedCurrency) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(mapKeys[0], item[0]);
            map.put(mapKeys[1], item[1]);
            displayArray.add(map);
        }

        mAdapterSource = new SimpleAdapter(this, displayArray, R.layout.currency_item, mapKeys, listID);
        mSupportedListSource.setAdapter(mAdapterSource);
        mSupportedListSource.setTextFilterEnabled(true);

        mAdapterTarget = new SimpleAdapter(this, displayArray, R.layout.currency_item, mapKeys, listID);
        mSupportedListTarget.setAdapter(mAdapterTarget);
    }

    @Override
    public void onSupportedCurrencyUpdated(boolean success, String error) {
        if (success) {
            mSupportedCurrency = mRateItemList.getSupportedCurrency();
            updateSupportedListView();
        } else {

        }
    }

    @Override
    public void onRateListUpdated(boolean success, String error) {

    }

    @Override
    public void onRateConvertFinished(boolean success, String error) {

    }
}
