package io.jitrapon.glom.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.Size
import android.support.v4.app.Fragment
import android.util.Log
import io.jitrapon.glom.R
import io.jitrapon.glom.util.finish

/**
 * Helper class to wrap around navigation logic for Instant App in different modules
 *
 * @author Jitrapon Tiachunpun
 */
object Router {

    private const val TAG = "Router"

    /**
     * Launch a specfic activity by specifying the module under which the activity belongs to
     */
    fun navigate(from: Context?, isInstantApp: Boolean, toModule: String?,
                 shouldFinish: Boolean = false, @Size(2) transitionAnimations: Array<Int>? = null) {
        toModule ?: return
        from?.let {
            if (isInstantApp) {
                it.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://${it.getString(R.string.instant_app_host)}/${toModule.toLowerCase()}")).apply {
                    `package` = from.packageName
                    addCategory(Intent.CATEGORY_BROWSABLE)
                })
            }
            else {
                val module = toModule.toLowerCase()
                val className = "io.jitrapon.glom.$module.${module.capitalize()}Activity"
                try {
                    it.startActivity(Intent(it, Class.forName(className)))
                }
                catch (ex: Exception) {
                    Log.e(TAG, "Failed to launch module '$module'. Could not find class $className")
                }
            }
            if (shouldFinish) {
                if (from is Activity) {
                    from.finish()
                    transitionAnimations?.let {
                        from.overridePendingTransition(it[0], it[1])
                    }
                }
                else if (from is Fragment) from.finish()
            }
        }
    }
}