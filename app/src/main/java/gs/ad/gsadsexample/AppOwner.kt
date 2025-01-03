package gs.ad.gsadsexample

import android.util.Log
import androidx.multidex.MultiDexApplication
import gs.ad.gsadsexample.ads.AdKeyPosition
import gs.ad.gsadsexample.sub.ConsumableProductId
import gs.ad.gsadsexample.sub.SubProductId
import gs.ad.utils.ads.AdmBuilder
import gs.ad.utils.ads.AdmConfig
import gs.ad.utils.google_iab.BillingClientLifecycle
import gs.ad.utils.google_iab.BillingConnector
import gs.ad.utils.google_iab.BillingEventListener
import gs.ad.utils.google_iab.enums.ErrorType
import gs.ad.utils.google_iab.enums.ProductType
import gs.ad.utils.google_iab.models.BillingResponse
import gs.ad.utils.google_iab.models.ProductInfo
import gs.ad.utils.google_iab.models.PurchaseInfo
import gs.ad.utils.utils.GlobalVariables

class AppOwner: MultiDexApplication() {
    lateinit var mAdmBuilder: AdmBuilder
    lateinit var mBillingClientLifecycle: BillingClientLifecycle

    override fun onCreate() {
        super.onCreate()
        mAdmBuilder = AdmBuilder.build(applicationContext) {
            keyShowOpen = AdKeyPosition.AppOpenAd_App_From_Background.name
            config = AdmConfig(
                listBannerAdUnitID = resources.getStringArray(R.array.banner_ad_unit_id).toList(),
                listInterstitialAdUnitID = resources.getStringArray(R.array.interstitial_ad_unit_id).toList(),
                listRewardAdUnitID = resources.getStringArray(R.array.reward_ad_unit_id).toList(),
                listNativeAdUnitID = resources.getStringArray(R.array.native_ad_unit_id).toList(),
                listOpenAdUnitID = resources.getStringArray(R.array.open_ad_unit_id).toList()
            )
        }

        enumValues<AdKeyPosition>().forEach {
            GlobalVariables.AdsKeyPositionAllow[it.name] = true
        }

        val subProductId: List<String> = enumValues<SubProductId>().map { it.id }

        mBillingClientLifecycle = BillingClientLifecycle.build(applicationContext){
            licenseKey = applicationContext.getString(R.string.license_key)
            subscriptionIds = subProductId
        }

        mBillingClientLifecycle.connectBillingConnector()

        mBillingClientLifecycle.fetchSubPurchasedProducts()
        mBillingClientLifecycle.setListener(eventListener = object :BillingEventListener{
            override fun onProductsFetched(productDetails: MutableList<ProductInfo>) {
                productDetails.forEach { s->
                    Log.d(TAG, s.product + "," + s.subscriptionOfferDetails.first().pricingPhases.first().formattedPrice)
                }
            }

            override fun onPurchasedProductsFetched(
                productType: ProductType,
                purchases: MutableList<PurchaseInfo>
            ) {
                purchases.forEach { s->
                    Log.d(TAG, "onPurchasedProductsFetched: " + s.product)
                }
            }

            override fun onProductsPurchased(purchases: MutableList<PurchaseInfo>) {
                purchases.forEach { s->
                    Log.d(TAG, "onProductsPurchased: " + s.product)
                }
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {
                Log.d(TAG, "onProductsPurchased: " + purchase.product)
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                Log.d(TAG, "onPurchaseConsumed: " + purchase.product)
            }

            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {}
                    ErrorType.CLIENT_DISCONNECTED -> {}
                    ErrorType.PRODUCT_NOT_EXIST -> {}
                    ErrorType.CONSUME_ERROR -> {}
                    ErrorType.CONSUME_WARNING -> {}
                    ErrorType.ACKNOWLEDGE_ERROR -> {}
                    ErrorType.ACKNOWLEDGE_WARNING -> {}
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {}
                    ErrorType.BILLING_ERROR -> {}
                    ErrorType.USER_CANCELED -> {}
                    ErrorType.SERVICE_UNAVAILABLE -> {}
                    ErrorType.BILLING_UNAVAILABLE -> {}
                    ErrorType.ITEM_UNAVAILABLE -> {}
                    ErrorType.DEVELOPER_ERROR -> {}
                    ErrorType.ERROR -> {}
                    ErrorType.ITEM_ALREADY_OWNED -> {}
                    ErrorType.ITEM_NOT_OWNED -> {}
                }
            }
        })
    }

    override fun onTerminate() {
        mBillingClientLifecycle.destroyBillingConnector()
        mAdmBuilder.resetCounterAds(AdKeyPosition.InterstitialAd_ScMain.name)
        super.onTerminate()
    }

    companion object{
        const val TAG = "AppOwner"
    }
}