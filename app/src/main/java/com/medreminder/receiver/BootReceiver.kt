package com.medreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medreminder.worker.ReminderWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderWorker.schedule(context)
        }
    }
}
