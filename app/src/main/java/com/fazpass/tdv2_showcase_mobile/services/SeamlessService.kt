package com.fazpass.tdv2_showcase_mobile.services

import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import com.fazpass.tdv2_showcase_mobile.objects.NotifiableDeviceModel
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.Scanner
import javax.net.ssl.HttpsURLConnection

class SeamlessService {

    companion object {
        private const val BASE_URL_STAGING = "https://api.fazpas.com/v2/trusted-device"
        private val BASE_URL: String
            get() = BASE_URL_STAGING
        private const val PIC_ID = "hello@mail.com"
        private const val MERCHANT_APP_ID = "afb2c34a-4c4f-4188-9921-5c17d81a3b3d"
        private const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZGVudGlmaWVyIjozNn0.mfny8amysdJQYlCrUlYeA-u4EG1Dw9_nwotOl-0XuQ8"
    }

    var fazpassId: String = ""
        private set
    var challenge: String = ""
        private set
    val notifiableDevices: ArrayList<NotifiableDeviceModel> = arrayListOf()

    fun check(
        meta: String,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/check"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID
        )
        fetch(url, body, callback)
    }

    fun enroll(
        meta: String,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/enroll"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun validate(
        meta: String,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/validate"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "fazpass_id" to fazpassId,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun remove(
        meta: String,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/remove"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "fazpass_id" to fazpassId,
            "challenge" to challenge
        )
        fetch(url, body, callback)
    }

    fun sendNotification(
        meta: String,
        selectedDevice: String,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/send/notification"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "pic_id" to PIC_ID,
            "selected_device" to selectedDevice
        )
        fetch(url, body, callback)
    }

    fun validateNotification(
        meta: String,
        notificationId: String,
        answer: Boolean,
        callback: (String) -> Unit
    ) {
        val url = "$BASE_URL/validate/notification"
        val body = mapOf(
            "merchant_app_id" to MERCHANT_APP_ID,
            "meta" to meta,
            "notification_id" to notificationId,
            "result" to answer
        )
        fetch(url, body, callback)
    }

    private fun fetch(url: String, body: Map<String, Any>, callback: (String) -> Unit) {
        Thread {
            var s: Scanner? = null
            var response = ""

            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $BEARER_TOKEN")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.setRequestProperty("Accept", "*/*")
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br")
            conn.setRequestProperty("Connection", "keep-alive")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            try {
                conn.outputStream.write(JSONObject(body).toString().toByteArray(charset = Charsets.UTF_8))
                s = if (conn.responseCode == 200) {
                    Scanner(conn.inputStream, "UTF-8")
                        .useDelimiter("\\A")
                } else {
                    Scanner(conn.errorStream, "UTF-8")
                        .useDelimiter("\\A")
                }
                response = s.next()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                conn.disconnect()
                s?.close()
            }

            readDataFromResponse(response)
            Handler(Looper.getMainLooper()).post {
                Log.i("API Request", "url=$url\nbody=$body\nresponse=$response")
                callback(response)
            }
        }.start()
    }

    private fun readDataFromResponse(response: String) {
        val responseReader = JsonReader(response.reader())
        responseReader.beginObject()
        while (responseReader.hasNext()) {
            when (responseReader.nextName()) {
                "data" -> responseReader.beginObject()
                "identification" -> {
                    responseReader.beginObject()
                    responseReader.nextName()
                    responseReader.beginObject()
                    while (responseReader.hasNext()) {
                        when (responseReader.nextName()) {
                            "fazpass_id" -> this.fazpassId = responseReader.nextString()
                            "challenge" -> this.challenge = responseReader.nextString()
                            else -> responseReader.skipValue()
                        }
                    }
                    responseReader.endObject()
                    responseReader.endObject()
                }
                "linked_devices" -> {
                    responseReader.beginObject()
                    responseReader.nextName()
                    if (responseReader.peek() == JsonToken.NULL) {
                        responseReader.skipValue()
                    } else {
                        notifiableDevices.clear()
                        responseReader.beginArray()
                        while (responseReader.hasNext()) {
                            notifiableDevices.add(NotifiableDeviceModel(responseReader))
                        }
                        responseReader.endArray()
                    }
                    responseReader.endObject()
                }
                else -> responseReader.skipValue()
            }
        }
        responseReader.close()
    }

}