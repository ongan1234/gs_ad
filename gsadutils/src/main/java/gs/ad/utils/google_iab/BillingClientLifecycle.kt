package gs.ad.utils.google_iab

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import gs.ad.utils.R
import gs.ad.utils.ads.AdmBuilder
import gs.ad.utils.ads.AdmMachine
import gs.ad.utils.ads.AdmManager
import gs.ad.utils.ads.AdmManager.Builder
import gs.ad.utils.google_iab.enums.ErrorType
import gs.ad.utils.google_iab.enums.ProductType
import gs.ad.utils.google_iab.models.BillingResponse
import gs.ad.utils.google_iab.models.ProductInfo
import gs.ad.utils.google_iab.models.PurchaseInfo

class BillingClientLifecycle(
    private val applicationContext: Context,
    private val licenseKey: String,
    private val consumableIds : List<String>,
    private val nonConsumableIds : List<String>,
    private val subscriptionIds : List<String>,
)  {

    private constructor(builder: Builder) : this(
        builder.context,
        builder.licenseKey,
        builder.consumableIds,
        builder.nonConsumableIds,
        builder.subscriptionIds){
       billingConnector = BillingConnector(builder.context, builder.licenseKey)
               .setConsumableIds(builder.consumableIds)
               .setNonConsumableIds(builder.nonConsumableIds)
               .setSubscriptionIds(builder.subscriptionIds)
               .autoAcknowledge()
               .autoConsume()
               .enableLogging()
               .connect()
    }

    private lateinit var billingConnector: BillingConnector

    private fun isReadyBillingConnector(): Boolean{
        return billingConnector.isReady
    }

    fun connectBillingConnector(){
        if(isReadyBillingConnector()) return
        billingConnector.connect()
    }

    fun setListener(eventListener: BillingEventListener){
        billingConnector.setBillingEventListener(eventListener)
    }

    fun destroyBillingConnector(){
        billingConnector.release()
    }

    fun fetchSubPurchasedProducts(){
        connectBillingConnector()
        billingConnector.fetchSubPurchasedProducts()
    }

    fun purchase(activity : Activity, productId : String){
        connectBillingConnector()
        billingConnector.purchase(activity, productId)
    }

    fun subscribe(activity : Activity, productId : String){
        connectBillingConnector()
        billingConnector.subscribe(activity, productId)
    }

    fun unsubscribe(activity : Activity, productId : String){
        connectBillingConnector()
        billingConnector.unsubscribe(activity, productId)
    }

    companion object {
        const val TAG = "BillingLifecycle"
        inline fun build(context: Context, block: Builder.() -> Unit) =
            Builder(context).apply(block).build()
    }

    class Builder(
        val context: Context
    ) {
        lateinit var licenseKey: String
        var consumableIds : List<String> = ArrayList()
        var nonConsumableIds : List<String> = ArrayList()
        var subscriptionIds : List<String> = ArrayList()
        fun build() = BillingClientLifecycle(
            this
        )
    }
}