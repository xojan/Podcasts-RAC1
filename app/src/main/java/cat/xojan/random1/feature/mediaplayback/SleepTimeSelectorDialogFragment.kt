package cat.xojan.random1.feature.mediaplayback

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cat.xojan.random1.R


class SleepTimeSelectorDialogFragment: DialogFragment() {

    companion object {
        val TAG = SleepTimeSelectorDialogFragment::class.java.simpleName
    }

    interface Listener {
        fun onTimeSelected(milliseconds: Long, label: String?)
    }

    private lateinit var listener: Listener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as MediaPlaybackFullScreenActivity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = context?.resources?.getStringArray(R.array.sleep_timer_options)
        val builder = AlertDialog.Builder(activity as Context)
                .setItems(R.array.sleep_timer_options) { _, which ->
                    when (which) {
                        0 -> listener.onTimeSelected(0, options?.get(0))
                        1 -> listener.onTimeSelected(300000, options?.get(1))
                        2 -> listener.onTimeSelected(600000, options?.get(2))
                        3 -> listener.onTimeSelected(900000, options?.get(3))
                        4 -> listener.onTimeSelected(1200000, options?.get(4))
                        5 -> listener.onTimeSelected(1800000, options?.get(5))
                        6 -> listener.onTimeSelected(2700000, options?.get(6))
                        7 -> listener.onTimeSelected(3600000, options?.get(7))
                    }
                }
        return builder.create()
    }
}