package gs.ad.utils.ads.format

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import gs.ad.utils.R
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.TYPE_ADS
import gs.ad.utils.utils.NetworkUtil
import gs.ad.utils.utils.PreferencesManager


internal class AdmNativeAd(
    private val context: Context,
    private val admMachine: AdmMachine,
    listNativeAdUnitID: List<String>
) : AdListener() {

    data class AdmModel(
        var nameActivity: String,
        var keyPosition: String,
        var adContainerView: ConstraintLayout?,
        var nativeAd: NativeAd?,
        var nativeAdView: NativeAdView?,
    )

    private var mListAdmModel: MutableList<AdmModel> = ArrayList()

    private val keyPosition: String get() {
        val model = currentModel ?: return "null"
        return model.keyPosition
    }

    private val currentModel: AdmModel?
        get() {
            val act = admMachine.getCurrentActivity() ?: return null
            val nameActivity = act::class.java.simpleName
            val model =
                mListAdmModel.stream().filter { md -> md.nameActivity == nameActivity }.findFirst()
                    .orElse(null)
            return model ?: currentModel()
        }

    private fun currentModel(): AdmModel? {
        val model =
            mListAdmModel.stream().filter { md -> md.nativeAdView?.isShown == true }.findFirst()
                .orElse(null)
        return model
    }

    fun currentModelByKeyPosition(keyPosition: String): AdmModel? {
        val model =
            mListAdmModel.stream().filter { md -> md.keyPosition == keyPosition }.findFirst()
                .orElse(null)
        return model
    }

    private val listNativeAdUnitId: List<String> = listNativeAdUnitID
    private val isMutedVideo: Boolean = true
    private var keyPositionPreloaded: String? = null

    fun preloadAd(id: Int, keyPosition: String, isFullScreen: Boolean) {
        if (listNativeAdUnitId.isEmpty() ||
            id >= listNativeAdUnitId.count() ||
            !NetworkUtil.isNetworkAvailable(context) ||
            admMachine.getCurrentActivity().isFinishing ||
            admMachine.getCurrentActivity().isDestroyed ||
            currentModelByKeyPosition(keyPosition) != null ||
            currentModelByKeyPosition(keyPosition)?.nativeAd != null) {
            admMachine.onAdFailToLoaded(TYPE_ADS.NativeAd, keyPosition)
            return
        }

        if (PreferencesManager.getInstance().isSUB()) return

        val act = admMachine.getCurrentActivity()
        mListAdmModel.add(
            AdmModel(
                act::class.java.simpleName,
                keyPosition,
                null,
                null,
                null,
            )
        )

        val builder = AdLoader.Builder(context, listNativeAdUnitId[id])
        builder.forNativeAd { nativeAd ->
            keyPositionPreloaded = keyPosition

            currentModelByKeyPosition(keyPosition)?.nativeAd = nativeAd
        }

        val videoOptions = VideoOptions.Builder().setStartMuted(isMutedVideo).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
        if (isFullScreen) adOptions.setMediaAspectRatio(MediaAspectRatio.PORTRAIT)
        val adOptionsBuild = adOptions.build()
        builder.withNativeAdOptions(adOptionsBuild)
        val adLoader = builder.withAdListener(this).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun applyNativeAdView(
        keyPosition: String,
        adContainerView: ConstraintLayout,
        nativeAdView: NativeAdView
    ) {
        val model = currentModelByKeyPosition(keyPosition) ?: return
        if (model.nativeAd == null) {
            destroyView(keyPosition)
            return
        }
        val nativeAd = model.nativeAd ?: return

        model.adContainerView = adContainerView
        model.nativeAdView = nativeAdView

        populateNativeAdView(nativeAd, nativeAdView)
        adContainerView.removeAllViews()
        adContainerView.addView(nativeAdView)
        nativeAdView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    fun loadAd(
        id: Int,
        keyPosition: String,
        adContainerView: ConstraintLayout,
        layoutNativeAdView: Int,
        isFullScreen: Boolean
    ) {
//        destroyView(keyPosition)

        if (listNativeAdUnitId.isEmpty() ||
            id >= listNativeAdUnitId.count() ||
            !NetworkUtil.isNetworkAvailable(context) ||
            currentModelByKeyPosition(keyPosition) != null ||
            admMachine.getCurrentActivity().isDestroyed ||
            admMachine.getCurrentActivity().isFinishing
            ){
            admMachine.onAdFailToLoaded(TYPE_ADS.NativeAd, keyPosition)
            return
        }
        if (PreferencesManager.getInstance().isSUB()) return

        val act = admMachine.getCurrentActivity()
        adContainerView.visibility = GONE

        val nativeAdView =
            LayoutInflater.from(context).inflate(layoutNativeAdView, null) as NativeAdView

        adContainerView.removeAllViews()
        adContainerView.addView(nativeAdView)

        nativeAdView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
        }

        mListAdmModel.add(
            AdmModel(
                act::class.java.simpleName,
                keyPosition,
                adContainerView,
                null,
                nativeAdView,
            )
        )

        val builder = AdLoader.Builder(context, listNativeAdUnitId[id])
        builder.forNativeAd { nativeAd ->
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            populateNativeAdView(nativeAd, nativeAdView)
            currentModelByKeyPosition(keyPosition)?.nativeAd = nativeAd
        }

        val videoOptions = VideoOptions.Builder().setStartMuted(isMutedVideo).build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
        if (isFullScreen) adOptions.setMediaAspectRatio(MediaAspectRatio.PORTRAIT)
        val adOptionsBuild = adOptions.build()
        builder.withNativeAdOptions(adOptionsBuild)
        val adLoader = builder.withAdListener(this).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onAdClicked() {
        super.onAdClicked()
        Log.d(TAG, "native ads onAdLoaded")
        admMachine.onAdClicked(TYPE_ADS.NativeAd, keyPosition)
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
        Log.d(TAG, "native ads onAdLoaded")
        if (keyPositionPreloaded.isNullOrEmpty()) {
            currentModel?.adContainerView?.visibility = VISIBLE
            admMachine.onAdLoaded(TYPE_ADS.NativeAd, keyPosition)
        } else {
            keyPositionPreloaded?.let { key ->
                currentModelByKeyPosition(key)?.let {
                    admMachine.onAdLoaded(
                        TYPE_ADS.NativeAd,
                        it.keyPosition
                    )
                    keyPositionPreloaded = null
                }
            }
        }
    }

    override fun onAdFailedToLoad(p0: LoadAdError) {
        super.onAdFailedToLoad(p0)
        Log.d(TAG, "native ads onAdFailedToLoad " + p0.message)
        if (keyPositionPreloaded.isNullOrEmpty()) {
            admMachine.onAdFailToLoaded(TYPE_ADS.NativeAd, keyPosition)
        } else {
            keyPositionPreloaded?.let { key ->
                currentModelByKeyPosition(key)?.let {
                    admMachine.onAdFailToLoaded(
                        TYPE_ADS.NativeAd,
                        it.keyPosition
                    )
                    keyPositionPreloaded = null
                }
            }
        }
        destroyView()
    }

    fun showAdView(keyPosition: String = "") {
        currentModelByKeyPosition(keyPosition)?.adContainerView?.visibility = View.VISIBLE
        currentModelByKeyPosition(keyPosition)?.nativeAdView?.visibility = View.VISIBLE
    }

    fun hideAdView(keyPosition: String = "") {
        currentModelByKeyPosition(keyPosition)?.adContainerView?.visibility = View.GONE
        currentModelByKeyPosition(keyPosition)?.nativeAdView?.visibility = View.GONE
    }

    fun destroyAllView() {
        val act = admMachine.getCurrentActivity() ?: return

        mListAdmModel.forEach {
            Log.d(
                TAG,
                "removeModel: " + it.keyPosition + ", " + it.nameActivity + ", " + act::class.java.simpleName
            )
            if (it.nameActivity == act::class.java.simpleName) removeModel(it)
        }
    }

    fun destroyView(keyPosition: String = "") {
        if (mListAdmModel.isEmpty()) return
        val model =
            if (keyPosition.isEmpty()) currentModel else currentModelByKeyPosition(keyPosition)

        removeModel(model)
    }

    private fun removeModel(model: AdmModel?) {
        Log.d(TAG, "removeModel: " + model?.keyPosition + ", " + model?.nameActivity)

        model?.nativeAd?.destroy()
        model?.nativeAd = null
        model?.nativeAdView?.destroy()
        model?.nativeAdView = null
        model?.adContainerView?.removeAllViews()
        model?.adContainerView?.visibility = View.GONE

        mListAdmModel.remove(model)
    }

    private fun loadingNativeAdView(nativeAdView: NativeAdView) {
        // Set the media view.
        nativeAdView.mediaView = nativeAdView.findViewById(R.id.ad_media)

        // Set other ad assets.
        nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
        nativeAdView.priceView = nativeAdView.findViewById(R.id.ad_price)
        nativeAdView.starRatingView = nativeAdView.findViewById(R.id.ad_stars)
        nativeAdView.storeView = nativeAdView.findViewById(R.id.ad_store)
        nativeAdView.advertiserView = nativeAdView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        nativeAdView.headlineView?.let {
            (it as TextView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        nativeAdView.bodyView?.let {
            (it as TextView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }

        nativeAdView.callToActionView?.let {
            (it as Button).background = ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }

        nativeAdView.iconView?.let {
            (it as ImageView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
        }

        nativeAdView.priceView?.let {
            (it as TextView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }

        nativeAdView.storeView?.let {
            (it as TextView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }

        nativeAdView.starRatingView?.let {
            (it as RatingBar).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
        }

        nativeAdView.advertiserView?.let {
            (it as TextView).background =
                ContextCompat.getDrawable(context, R.drawable.round_corner)
            it.text = ""
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        // Set the media view.
        nativeAdView.mediaView = nativeAdView.findViewById(R.id.ad_media)

        // Set other ad assets.
        nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
        nativeAdView.priceView = nativeAdView.findViewById(R.id.ad_price)
        nativeAdView.starRatingView = nativeAdView.findViewById(R.id.ad_stars)
        nativeAdView.storeView = nativeAdView.findViewById(R.id.ad_store)
        nativeAdView.advertiserView = nativeAdView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        nativeAdView.headlineView?.let {
            (it as TextView).text = nativeAd.headline
        }
        nativeAd.mediaContent?.let { mc ->
            nativeAdView.mediaView?.let {
                it.mediaContent = mc
            }
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        nativeAdView.bodyView?.let {
            if (nativeAd.body == null) {
                (it as TextView).visibility = View.INVISIBLE
            } else {
                (it as TextView).visibility = View.VISIBLE
                (it as TextView).text = nativeAd.body
            }
        }

        nativeAdView.callToActionView?.let {
            if (nativeAd.callToAction == null) {
                (it as Button).visibility = View.INVISIBLE
            } else {
                (it as Button).visibility = View.VISIBLE
                (it as Button).text = nativeAd.callToAction
            }
        }

        nativeAdView.iconView?.let {
            if (nativeAd.icon == null) {
                (it as ImageView).visibility = View.GONE
            } else {
                (it as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                (it as ImageView).visibility = View.VISIBLE
            }
        }

        nativeAdView.priceView?.let {
            if (nativeAd.price == null) {
                (it as TextView).visibility = View.INVISIBLE
            } else {
                (it as TextView).visibility = View.VISIBLE
                (it as TextView).text = nativeAd.price
            }
        }

        nativeAdView.storeView?.let {
            if (nativeAd.store == null) {
                (it as TextView).visibility = View.INVISIBLE
            } else {
                (it as TextView).visibility = View.VISIBLE
                (it as TextView).text = nativeAd.store
            }
        }

        nativeAdView.starRatingView?.let {
            if (nativeAd.starRating == null) {
                (it as RatingBar).visibility = View.INVISIBLE
            } else {
                (it as RatingBar).rating = nativeAd.starRating!!.toFloat()
                (it as RatingBar).visibility = View.VISIBLE
            }
        }

        nativeAdView.advertiserView?.let {
            if (nativeAd.advertiser == null) {
                (it as TextView).visibility = View.INVISIBLE
            } else {
                (it as TextView).text = nativeAd.advertiser
                (it as TextView).visibility = View.VISIBLE
            }
        }


        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val mediaContent = nativeAd.mediaContent
        val vc = mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc != null && mediaContent.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.
                        super.onVideoEnd()
                    }
                }
        } else {

        }
    }

    companion object {
        const val TAG = "AdmNativeAd"
    }
}