package cat.xojan.random1.domain.model

import com.crashlytics.android.Crashlytics


class CrashReporter(val crashlytics: Crashlytics?) {

    fun logException(e: Throwable) {
        crashlytics?.let {
            Crashlytics.logException(e)
        }
    }

    fun logException(message: String) {
        crashlytics?.let {
            Crashlytics.log(message)
        }
    }
}