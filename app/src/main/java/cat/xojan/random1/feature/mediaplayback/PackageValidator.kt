package cat.xojan.random1.feature.mediaplayback

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log

class PackageValidator {

    private val TAG = PackageValidator::class.java.simpleName

    /**
     * @return false if the caller is not authorized to get data from this MediaBrowserService
     */
    fun isCallerAllowed(context: Context, callingPackage: String, callingUid: Int): Boolean {
        // Always allow calls from the framework, self app or development environment.
        if (Process.SYSTEM_UID == callingUid || Process.myUid() == callingUid) {
            return true
        }

        if (isPlatformSigned(context, callingPackage)) {
            return true
        }

        val packageInfo = getPackageInfo(context, callingPackage) ?: return false
        if (packageInfo.signatures.size != 1) {
            Log.w(TAG, "Caller does not have exactly one signature certificate!")
            return false
        }

        return false
    }

    /**
     * @return true if the installed package signature matches the platform signature.
     */
    private fun isPlatformSigned(context: Context, pkgName: String): Boolean {
        val platformPackageInfo = getPackageInfo(context, "android")

        // Should never happen.
        if (platformPackageInfo?.signatures == null
                || platformPackageInfo.signatures.isEmpty()) {
            return false
        }

        val clientPackageInfo = getPackageInfo(context, pkgName)

        return (clientPackageInfo?.signatures != null
                && clientPackageInfo.signatures.isNotEmpty() &&
                platformPackageInfo.signatures[0] == clientPackageInfo.signatures[0])
    }

    /**
     * @return [PackageInfo] for the package name or null if it's not found.
     */
    private fun getPackageInfo(context: Context, pkgName: String): PackageInfo? {
        try {
            val pm = context.packageManager
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package manager can't find package: " + pkgName)
        }

        return null
    }
}
