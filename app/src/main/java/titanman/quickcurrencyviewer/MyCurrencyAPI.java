package titanman.quickcurrencyviewer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class MyCurrencyAPI extends AsyncTask<String, String[], String[]> {

    private static final String TAG = "MyCurrencyAPI";

    // essential URL structure is built using constants
    private static final String ACCESSE_KEY_SUFFIX = "?access_key=";
    private static final String ACCESS_KEY = "393d7e91eea017841fc9bf9fe784e94f";
    private static final String BASE_URL = "http://apilayer.net/api/";


    private static final String ENDPOINT_LIST = "list";
    private static final String ENDPOINT_LIVE = "live";
    private static final String ENDPOINT_CONVERT = "convert";

    private static final String API_ACTION_GET_CURRENCY_LIST = "get_currency_list";
    private static final String API_ACTION_GET_RATE_LIST = "get_rate_list";
    private static final String API_ACTION_GET_RATE_CONVERT = "get_rate_convert";

    private static final String API_RESULT_OK = "API_OK";
    private static final String API_RESULT_ERROR_NO_RESPONSE = "Net work error";
    private static final String API_RESULT_ERROR_NOT_SUCCESS = "Failed to get result";

    private static final int TIMEOUT_MILLIS = 0;

    private MyCurrencyAPICallbacks mCallback = null;
    private ArrayList<String[]> mSupportedCurrency = new ArrayList<>();
    private ArrayList<RateItem> mRateList = new ArrayList<>();

    private static StringBuffer mResult = null;

    public void setMyCurrencyAPICallback(MyCurrencyAPICallbacks callback) {
        this.mCallback = callback;
    }

    public void requestSupportedCurrency() {
        this.execute(API_ACTION_GET_CURRENCY_LIST);
    }

    public void requestAllRateUpdate(ArrayList<RateItem> rateItemList) {
        mRateList = rateItemList;
        this.execute(API_ACTION_GET_RATE_LIST);
    }

    public ArrayList<String[]> getSupportedCurrency() {
        return mSupportedCurrency;
    }

    public ArrayList<RateItem> getUpdatedRateList() {
        return mRateList;
    }

    private StringBuffer sendRequest(String endpoint, String... currencies) {
        URL url = null;
        HttpURLConnection httpConn = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;
        final StringBuffer response = new StringBuffer();
        try {
            switch (endpoint) {
                default:
                case ENDPOINT_LIST:
                case ENDPOINT_LIVE:
                    url = new URL(BASE_URL + endpoint + ACCESSE_KEY_SUFFIX + ACCESS_KEY);
                    break;
                case ENDPOINT_CONVERT:
                    if (currencies.length >= 2) {
                        url = new URL(BASE_URL + endpoint + ACCESSE_KEY_SUFFIX + ACCESS_KEY
                                + "&from=" + currencies[0] + "&to=" + currencies[1] + "&amount=1");
                    }
                    break;
            }
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(TIMEOUT_MILLIS);
            httpConn.setReadTimeout(TIMEOUT_MILLIS);
            httpConn.setRequestMethod("GET");
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(false);
            httpConn.setDoInput(true);
            httpConn.connect();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = httpConn.getInputStream();
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {

            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException happened");
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException happened");
        } catch (IOException e) {
            Log.e(TAG, "IOException happened");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {

                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
        return response;
    }

    @Override
    protected String[] doInBackground(String... params) {
        String[] result = new String[]{"", ""};
        switch (params[0]) {
            case API_ACTION_GET_CURRENCY_LIST:
                result[0] = API_ACTION_GET_CURRENCY_LIST;
                StringBuffer currencyList = sendRequest(ENDPOINT_LIST);
                result[1] = parseSupportedCurrency(currencyList);
                return result;
            case API_ACTION_GET_RATE_LIST:
                result[0] = API_ACTION_GET_RATE_LIST;
                result[1] = API_RESULT_OK;
                if (mRateList!= null && mRateList.size() > 0) {
                    Iterator<RateItem> iterator = mRateList.iterator();
                    while (iterator.hasNext()) {
                        RateItem singleRate = iterator.next();
                        StringBuffer response = sendRequest(ENDPOINT_CONVERT, singleRate.getSourceCurrency(), singleRate.getTargetCurrency());
                        final String[] rateInfo = new String[5];
                        result[1] = parseRateResult(response, rateInfo);
                        if (result[1].equals(API_RESULT_OK)) {
                            singleRate.setRateNumber(rateInfo[3]);
                        } else {
                            break;
                        }
                    }
                }
                return result;
            case API_ACTION_GET_RATE_CONVERT:
                result[0] = API_ACTION_GET_RATE_CONVERT;
                return result;
        }
        return null;
    }


    @Override
    protected void onPostExecute(String... action) {
        switch (action[0]) {
            case API_ACTION_GET_CURRENCY_LIST:
                if (mCallback != null) {
                    mCallback.onSupportedCurrencyUpdated(action[1].equals(API_RESULT_OK), action[1]);
                }
                break;
            case API_ACTION_GET_RATE_LIST:
                if (mCallback != null)
                    mCallback.onRateListUpdated(action[1].equals(API_RESULT_OK), action[1]);
                break;
            case API_ACTION_GET_RATE_CONVERT:
                // To Do
                break;
        }
    }

    private String parseSupportedCurrency(StringBuffer response) {

        ArrayList<String[]> list = new ArrayList<>();

        final String tab_success = "success";

        final String tab_currency = "\"currencies\":";
        final String tab_brk_start = "{";
        final String tab_brk_end = "}";
        final String tab_coma = ",";

        String tmp = "";
        int start = 0;
        int end = 0;

        if (response.toString().equals("")) {
            return API_RESULT_ERROR_NO_RESPONSE;
        }

        if(response.indexOf(tab_success) <0) {
            return API_RESULT_ERROR_NOT_SUCCESS;
        }

        // Step1
        start = response.indexOf(tab_currency) + tab_currency.length();
        tmp = response.substring(start);
        response.delete(0, response.length());
        response.insert(0, tmp);

        //Step2
        start = response.indexOf(tab_brk_start) + tab_brk_start.length();
        end = response.indexOf(tab_brk_end);
        tmp = response.substring(start, end);
        response.delete(0, response.length());
        response.insert(0, tmp);

        String currencyInfo = "";
        String[] currencyData = null;

        //Step3
        do {
            start = 0;
            end = response.indexOf(tab_coma);
            if (end < 0) {
                currencyInfo = response.substring(start);
            } else {
                currencyInfo = response.substring(start, end);
            }

            currencyData = currencyInfo.split(":");

            for (int i = 0; i < currencyData.length; i++) {
                currencyData[i] = currencyData[i].replace('"', ' ');
                currencyData[i] = currencyData[i].trim();
            }

            list.add(currencyData);
            tmp = response.substring(end + 1);

            response.delete(0, response.length());
            response.insert(0, tmp);

        } while (end >= 0);

        mSupportedCurrency = list;

        return API_RESULT_OK;

    }

    private String parseRateResult(StringBuffer response, String[] rateInfo) {

        for (String i : rateInfo) {
            i = "";
        }
        final String tab_success = "success";
        final String tab_source = "\"from\":";
        final String tab_target = "\"to\":";
        final String tab_amount = "\"amount\":";
        final String tab_quote = "\"quote\":";
        final String tab_result = "\"result\":";
        final String tab_coma = ",";

        final String[] search_keys = new String[]{
                tab_source,
                tab_target,
                tab_amount,
                tab_quote,
                tab_result
        };

        String tmp = "";
        int start = 0;
        int end = 0;

        if (response.toString().equals("")) {
            return API_RESULT_ERROR_NO_RESPONSE;
        }

        if(response.indexOf(tab_success) <0) {
            return API_RESULT_ERROR_NOT_SUCCESS;
        }

        for (int i = 0; i < search_keys.length; i++) {
            start = response.indexOf(search_keys[i]) + search_keys[i].length();

            if (start >= 0) {
                tmp = response.substring(start);
            }

            response.delete(0, response.length());
            response.insert(0, tmp);

            end = response.indexOf(tab_coma);
            if (end >= 0) {
                tmp = response.substring(0, end);
            }

            tmp = tmp.replace('"', ' ');
            tmp = tmp.replace('}', ' ');
            tmp = tmp.trim();
            rateInfo[i] = tmp;
        }
        return API_RESULT_OK;
    }
}
