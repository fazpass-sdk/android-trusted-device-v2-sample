package com.fazpass.tdv2_showcase_mobile.objects

import android.util.JsonReader

class NotifiableDeviceModel private constructor() {

    lateinit var id: String
    lateinit var name: String
    lateinit var osVersion: String
    lateinit var series: String
    lateinit var cpu: String

    constructor(jsonReader: JsonReader) : this() {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "id" -> this.id = jsonReader.nextString()
                "name" -> this.name = jsonReader.nextString()
                "os_version" -> this.osVersion = jsonReader.nextString()
                "series" -> this.series = jsonReader.nextString()
                "cpu" -> this.cpu = jsonReader.nextString()
                else -> jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
    }

    fun toMap(): Map<String, String> = mapOf(
        "id" to id,
        "name" to name,
        "os_version" to osVersion,
        "series" to series,
        "cpu" to cpu
    )
}