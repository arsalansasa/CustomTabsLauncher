package com.droibit.android.customtabs.launcher.internal

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import com.droibit.android.customtabs.launcher.CustomTabsFallback
import android.text.TextUtils

import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION

import android.content.Intent

import android.content.pm.ResolveInfo

import android.content.pm.PackageManager
import androidx.browser.customtabs.CustomTabsService
import android.content.IntentFilter
import android.content.pm.PackageManager.MATCH_ALL
import android.util.Log
import java.lang.RuntimeException


internal class CustomTabsLauncherImpl {
    val STABLE_PACKAGE = "com.android.chrome"
    val BETA_PACKAGE = "com.chrome.beta"
    val DEV_PACKAGE = "com.chrome.dev"
    val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    private val TAG = "CustomTabsLauncherImpl"

    fun launch(
        context: Context,
        customTabsIntent: CustomTabsIntent,
        uri: Uri,
        expectCustomTabsPackages: List<String>,
        fallback: CustomTabsFallback?
    ) {
        val customTabsPackage = customGetPackageName(context)
        if (customTabsPackage == null && fallback != null) {
            fallback.openUrl(context, uri, customTabsIntent)
            return
        }
        customTabsIntent.intent.setPackage(customTabsPackage)
        customTabsIntent.launchUrl(context, uri)
    }

    fun customGetPackageName(context:Context) : String? {
        var sPackageNameToUse : String? = null
        val pm: PackageManager = context.getPackageManager()
        // Get default VIEW intent handler.
        // Get default VIEW intent handler.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
        var defaultViewHandlerPackageName: String? = null
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
        }

        // Get all apps that can handle VIEW intents.

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = pm.queryIntentActivities(activityIntent,MATCH_ALL )
        val packagesSupportingCustomTabs: MutableList<String> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        if (defaultViewHandlerPackageName == "com.sec.android.app.sbrowser"){
            defaultViewHandlerPackageName = STABLE_PACKAGE
        }
        if(packagesSupportingCustomTabs.contains("com.sec.android.app.sbrowser")){
            packagesSupportingCustomTabs.remove("com.sec.android.app.sbrowser")
        }
        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE
            return sPackageNameToUse
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE
            return sPackageNameToUse
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE
            return sPackageNameToUse
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE
            return sPackageNameToUse
        } else if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = STABLE_PACKAGE
            return sPackageNameToUse
        } else if (packagesSupportingCustomTabs.size == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs[0]
            return sPackageNameToUse
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName
        }
        return sPackageNameToUse

    }

    private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
        try {
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER)
            if (handlers.size == 0) {
                return false
            }
            for (resolveInfo in handlers) {
                val filter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Runtime exception while getting specialized handlers")
        }
        return false
    }
}