package titanman.quickcurrencyviewer;

public interface RateItemListUpdateCallbacks
{
    public void onSupportedCurrencyUpdated(boolean success, String error);
    public void onRateListUpdated(boolean success, String error);
    public void onRateConvertFinished(boolean success, String error);
}
