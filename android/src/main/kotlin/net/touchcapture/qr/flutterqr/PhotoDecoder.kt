package net.touchcapture.qr.flutterqr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.DecodeHintType
import com.google.zxing.BarcodeFormat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.BinaryMessenger
import java.util.*
import kotlin.collections.ArrayList

class PhotoDecoder(private val messenger: BinaryMessenger) : MethodChannel.MethodCallHandler {
    private val channel: MethodChannel

    init {
        channel = MethodChannel(messenger, "net.touchcapture.qr.flutterqr/photo_decoder")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "decode" -> decode(call.arguments as String)
            else -> result.notImplemented()
        }
    }

    fun decode(path: String) {
        DecoderTask().execute(path)
    }

    inner class DecoderTask : AsyncTask<String, Void, com.google.zxing.Result?>() {
        private fun decode(bitmap: Bitmap): com.google.zxing.Result? {
            val HINTS: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
            val allFormats = ArrayList<BarcodeFormat>()
            allFormats.add(BarcodeFormat.AZTEC)
            allFormats.add(BarcodeFormat.CODABAR)
            allFormats.add(BarcodeFormat.CODE_39)
            allFormats.add(BarcodeFormat.CODE_93)
            allFormats.add(BarcodeFormat.CODE_128)
            allFormats.add(BarcodeFormat.DATA_MATRIX)
            allFormats.add(BarcodeFormat.EAN_8)
            allFormats.add(BarcodeFormat.EAN_13)
            allFormats.add(BarcodeFormat.ITF)
            allFormats.add(BarcodeFormat.MAXICODE)
            allFormats.add(BarcodeFormat.PDF_417)
            allFormats.add(BarcodeFormat.QR_CODE)
            allFormats.add(BarcodeFormat.RSS_14)
            allFormats.add(BarcodeFormat.RSS_EXPANDED)
            allFormats.add(BarcodeFormat.UPC_A)
            allFormats.add(BarcodeFormat.UPC_E)
            allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION)
            HINTS[DecodeHintType.TRY_HARDER] = BarcodeFormat.QR_CODE
            HINTS[DecodeHintType.POSSIBLE_FORMATS] = allFormats
            HINTS[DecodeHintType.CHARACTER_SET] = "utf-8"

            var result: com.google.zxing.Result?
            var source: RGBLuminanceSource? = null
            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                source = RGBLuminanceSource(width, height, pixels)
                return MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), HINTS)
            } catch (e: Exception) {
                e.printStackTrace()
                if (source != null) {
                    try {
                        return MultiFormatReader().decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS)
                    } catch (e2: Throwable) {
                        e2.printStackTrace()
                    }

                }
                return null
            }
        }

        private fun loadBitmap(path: String): Bitmap? {
            return try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true//  only load options
                BitmapFactory.decodeFile(path, options)
                var sampleSize = options.outHeight / 800
                if (sampleSize <= 0) {
                    sampleSize = 1
                }
                options.inSampleSize = sampleSize
                options.inJustDecodeBounds = false
                BitmapFactory.decodeFile(path, options)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun doInBackground(vararg params: String): com.google.zxing.Result? {
            if (params.isNotEmpty()) {
                var path = params.first()
                var bitmap = loadBitmap(path)
                if (bitmap != null) {
                    return decode(bitmap)
                }
            }
            return null;
        }

        override fun onPostExecute(result: com.google.zxing.Result?) {
            Shared.activity?.runOnUiThread {
                if (result != null) {
                    val code = mapOf(
                            "code" to result.text,
                            "type" to result.barcodeFormat.name,
                            "rawBytes" to result.rawBytes)
                    channel.invokeMethod("onDecode", code)
                }
                else {
                    channel.invokeMethod("onDecode", "")
                }
            }
        }
    }

}