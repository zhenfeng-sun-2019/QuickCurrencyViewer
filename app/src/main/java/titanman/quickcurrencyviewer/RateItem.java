package titanman.quickcurrencyviewer;

public class RateItem {

    private String mSourceCurrency;
    private String mTargetCurrency;
    private String mRateNumber;

    public RateItem(String source, String rate, String target) {
        this.mSourceCurrency = source;
        this.mRateNumber = rate;
        this.mTargetCurrency = target;
    }

    public String getSourceCurrency() {
        return mSourceCurrency;
    }

    public String getTargetCurrency() {
        return mTargetCurrency;
    }

    public String getRateNumber() {
        return mRateNumber;
    }

    public void setRateNumber(String rateNumber) {
        this.mRateNumber = rateNumber;
    }

}
