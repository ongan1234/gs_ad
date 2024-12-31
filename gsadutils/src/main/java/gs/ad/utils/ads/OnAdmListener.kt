package gs.ad.utils.ads

interface OnAdmListener {

    fun onAdHaveReward(typeAds: TYPE_ADS, keyPosition: String){}
    fun onAdNotHaveReward(typeAds: TYPE_ADS, keyPosition: String){}

    fun onAdShowed(typeAds: TYPE_ADS, keyPosition: String){}
    fun onAdClicked(typeAds: TYPE_ADS, keyPosition: String){}
    fun onAdLoaded(typeAds: TYPE_ADS, keyPosition: String){}
    fun onAdClosed(typeAds: TYPE_ADS, keyPosition: String){}
    fun onAdFailToLoaded(typeAds: TYPE_ADS, keyPosition: String){}
}