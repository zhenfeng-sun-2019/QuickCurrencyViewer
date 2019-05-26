package titanman.quickcurrencyviewer;

public interface MyCurrencyAPICallbacks {
    public void onSupportedCurrencyUpdated(boolean success, String error);

    public void onRateListUpdated(boolean success, String error);

    public void onRateConvertFinished(boolean success);
}
