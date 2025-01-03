package gs.ad.utils.google_iab

import android.app.Activity
import android.content.Context

class BillingClientLifecycle(
    private val context: Context,
    private val licenseKey: String,
    private val consumableIds: List<String>,
    private val nonConsumableIds: List<String>,
    private val subscriptionIds: List<String>,
) {
    private constructor(builder: Builder) : this(
        builder.context,
        builder.licenseKey,
        builder.consumableIds,
        builder.nonConsumableIds,
        builder.subscriptionIds
    )

    private var billingConnector: BillingConnector = BillingConnector(context, licenseKey)
        .setConsumableIds(consumableIds)
        .setNonConsumableIds(nonConsumableIds)
        .setSubscriptionIds(subscriptionIds)
        .autoAcknowledge()
        .autoConsume()
        .enableLogging()
        .connect()

    private fun isReadyBillingConnector(): Boolean {
        return billingConnector.isReady
    }

    fun connectBillingConnector() {
        if (isReadyBillingConnector()) return
        billingConnector.connect()
    }

    fun setListener(eventListener: BillingEventListener) {
        billingConnector.setBillingEventListener(eventListener)
    }

    fun destroyBillingConnector() {
        billingConnector.release()
    }

    fun fetchSubPurchasedProducts() {
        connectBillingConnector()
        billingConnector.fetchSubPurchasedProducts()
    }

    fun purchase(activity: Activity, productId: String) {
        connectBillingConnector()
        billingConnector.purchase(activity, productId)
    }

    fun subscribe(activity: Activity, productId: String) {
        connectBillingConnector()
        billingConnector.subscribe(activity, productId)
    }

    fun unsubscribe(activity: Activity, productId: String) {
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
        var consumableIds: List<String> = ArrayList()
        var nonConsumableIds: List<String> = ArrayList()
        var subscriptionIds: List<String> = ArrayList()
        fun build() = BillingClientLifecycle(
            this
        )
    }
}