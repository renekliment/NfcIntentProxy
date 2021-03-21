package cz.renekliment.nfcintentproxy

import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

class ProxyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = this.intent
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            this.handleNdefIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            this.handleNdefIntent(intent)
        }
    }

    private fun handleNdefIntent(in_intent: Intent) {
        Log.d(this::class.simpleName, "Handling NDEF_DISCOVERED")

        val isPhilipsBrushHeadTag2021 =
            in_intent.dataString == "https://www.philips.com/nfcbrushheadtap"
        val isPhilipsBrushHeadTagOld = in_intent.dataString == "https://www.philips.com"
        val isPhilipsBrushHeadTag = isPhilipsBrushHeadTag2021 || isPhilipsBrushHeadTagOld

        if (!isPhilipsBrushHeadTag) {
            Log.e(this::class.simpleName, "Tag does not contain a currently supported URL.")
            // TODO: Error message, this should not happen (the app should register only for valid URLs - currently Philips stuff).
            return
        }

        // TODO throttle/debounce
        // 1. Because there are non-idempotent actions like "toggle", "+/- some value", ...
        // 2. To just save data / cpu / whatever

        val rawTagId = in_intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)?.toHexString()
        if (rawTagId == null) {
            Log.e(this::class.simpleName, "Tag ID is empty. This should never happen. :shrug:")
            // TODO: Error message
            return
        }

        Log.d(this::class.simpleName, "Found tag with ID: $rawTagId")

        val tagId =
            if (isPhilipsBrushHeadTag2021) "PhilipsBrushHead2021-$rawTagId" else "PhilipsBrushHeadOld-$rawTagId"

        Log.d(this::class.simpleName, "Passing along with ID: $tagId")

        Intent().also { intent ->
            intent.setAction("cz.renekliment.nfcintentproxy.TAG_READ")
            intent.putExtra("tag_id", tagId)
            // TODO: Only send the intent to HASS Companion? Send twice (for each flavor)?
            // Also, that is important because on my phone the HASS companion app sometimes just crashes/dies
            // so an intent would probably start the app if it was not running.
            sendBroadcast(intent)
        }

        this.finish()
    }
}