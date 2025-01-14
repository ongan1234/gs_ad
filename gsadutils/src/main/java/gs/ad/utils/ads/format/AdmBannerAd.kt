package gs.ad.utils.ads.format

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager


internal class AdmBannerAd(
    private val context : Context,
    private val admMachine: AdmMachine,
    listBannerAdUnitID: List<String>
) : AdListener() {
    data class AdmModel(
        var nameActivity : String,
        var keyPosition: String,
//        var textView: TextView?,
        var adContainerView: ConstraintLayout?,
        var adView: AdView?
    )

    private var mListAdmModel: MutableList<AdmModel> = ArrayList()
    private val currentModel: AdmModel? get() {
        val act = admMachine.getCurrentActivity() ?: return null
        val nameActivity = act::class.java.simpleName
        val model = mListAdmModel.stream().filter { md -> md.nameActivity == nameActivity }.findFirst().orElse(null)
        return model ?: currentModel()
    }

    private fun currentModel(): AdmModel?{
        val model = mListAdmModel.stream().filter { md -> md.adView?.isShown == true }.findFirst().orElse(null)
        return model
    }

    private val keyPosition: String get(){
        val model = currentModel ?: return "null"
        return model.keyPosition
    }

    private fun currentModelByKeyPosition(keyPosition: String): AdmModel?{
        val model = mListAdmModel.stream().filter { md -> md.keyPosition == keyPosition }.findFirst().orElse(null)
        return model
    }

    private val listBannerAdUnitId: List<String> = listBannerAdUnitID
    private var countTier : Int = 0

    // [START get_ad_size]
    // Get the ad size with screen width.
    private val adSize: AdSize
        get() {
            val act = admMachine.getCurrentActivity() ?: return AdSize.BANNER
            val displayMetrics = act.resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics = act.windowManager.currentWindowMetrics
                    windowMetrics.bounds.width()
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(act, adWidth)
        }

    fun loadBanner(id : Int = -1, keyPosition: String, adContainerView: ConstraintLayout) {
//        destroyView(keyPosition)

        if (listBannerAdUnitId.isEmpty() ||
            id >= listBannerAdUnitId.count() ||
            !NetworkUtil.isNetworkAvailable(context) ||
            currentModelByKeyPosition(keyPosition) != null ||
            admMachine.getCurrentActivity().isFinishing ||
            admMachine.getCurrentActivity().isDestroyed) {
            admMachine.onAdFailToLoaded(TYPE_ADS.BannerAd, keyPosition)
            return
        }

        if (PreferencesManager.getInstance().isSUB()) return

        val act = admMachine.getCurrentActivity()
        // Create a new ad view.
        val mAdView = AdView(act)
        mAdView.setAdSize(adSize)
        val unitAdId = if(id == -1) countTier else id
        mAdView.adUnitId = listBannerAdUnitId[unitAdId]
        if (countTier >= listBannerAdUnitId.size - 1) {
            countTier = 0
        } else {
            countTier++
        }
        mAdView.adListener = this

//        val textView = TextView(act)
//        textView.id = View.generateViewId()
//        textView.textSize = (adSize.height / 4.0).toFloat()
//        textView.text = act.resources.getString(R.string.loading_ads)
//        textView.gravity = Gravity.CENTER

        // Replace ad container with new ad view.
        adContainerView.removeAllViews()
        adContainerView.addView(mAdView)
//        adContainerView.parent?.let {
//            (it as ViewGroup).addView(textView)
//        }

//        textView.updateLayoutParams<ConstraintLayout.LayoutParams> {
//            height = 50
//            width = 0
//            startToStart = adContainerView.id
//            endToEnd = adContainerView.id
//            topToTop = adContainerView.id
//            bottomToBottom = adContainerView.id
//        }
        // Start loading the ad in the background.
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.visibility = View.VISIBLE
        adContainerView.visibility = View.VISIBLE
//        textView.visibility = View.VISIBLE

        mListAdmModel.add(
            AdmModel(
            act::class.java.simpleName,
            keyPosition,
//            textView,
            adContainerView,
            mAdView
            )
        )
    }

    override fun onAdOpened() {
        super.onAdOpened()
        Log.d(TAG, "bannerView onAdOpened")
    }

    override fun onAdImpression() {
        super.onAdImpression()
        Log.d(TAG, "bannerView onAdImpression " + admMachine.getCurrentActivity().let { it::class.java.simpleName })
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
        Log.d(TAG, "bannerView onAdLoaded " + admMachine.getCurrentActivity().let { it::class.java.simpleName })
        val model = currentModel ?: return
//        model.textView?.visibility = View.GONE
        model.adContainerView?.visibility = View.VISIBLE
        model.adView?.visibility = View.VISIBLE

        admMachine.onAdLoaded(TYPE_ADS.BannerAd, keyPosition)
    }

    override fun onAdClicked() {
        // Called when a click is recorded for an ad.
        Log.d(TAG, "Ad was clicked.")

        admMachine.onAdClicked(TYPE_ADS.BannerAd, keyPosition)
    }

    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
        super.onAdFailedToLoad(loadAdError)
        Log.d(TAG, "bannerView " + loadAdError.message)
        admMachine.onAdFailToLoaded(TYPE_ADS.BannerAd, keyPosition)
        destroyView()
    }

    fun showAdView(){
        resumeAdView()
        currentModel?.adContainerView?.visibility = View.VISIBLE
        currentModel?.adView?.visibility = View.VISIBLE
    }

    fun hideAdView(){
        pauseAdView()
        currentModel?.adContainerView?.visibility = View.GONE
        currentModel?.adView?.visibility = View.GONE
    }

    fun pauseAdView(){
        currentModel?.adView?.pause()
    }

    fun resumeAdView(){
        currentModel?.adView?.resume()
    }

    fun destroyView(keyPosition: String = "") {
        if (mListAdmModel.isEmpty()) return
        val model = if(keyPosition.isEmpty()) currentModel else currentModelByKeyPosition(keyPosition)
        Log.d(TAG, "destroyView : " + keyPosition + ", " + model?.nameActivity)
        model?.adView?.destroy()
        model?.adView = null
//        model?.textView = null
        model?.adContainerView?.removeAllViews()
        model?.adContainerView?.visibility = View.GONE
        mListAdmModel.remove(model)
    }
    
    companion object{
        const val TAG = "AdmBannerAd"
    }
}
