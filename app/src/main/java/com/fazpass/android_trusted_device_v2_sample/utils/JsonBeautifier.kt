package com.fazpass.android_trusted_device_v2_sample.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.fazpass.android_trusted_device_v2_sample.objects.SpanProps

class JsonBeautifier(json: String): CharSequence {

    private val value: CharSequence
    val spans: List<SpanProps>

    init {
        val list = arrayListOf<InsertProps>()

        var indent = 0
        var asString = false
        for (i in json.indices) {
            val char = json[i]
            val nextChar = if (i != json.length-1) json[i+1] else null

            when (char) {
                '"' -> {
                    asString = !asString
                }
                ',' -> {
                    if (!asString) {
                        list.add(InsertProps("\n" + getIndents(indent), i+1))
                    }
                }
                '{','[' -> {
                    if (!asString) {
                        list.add(InsertProps("\n" + getIndents(++indent), i+1))
                    }
                }
                '}',']' -> {
                    if (!asString) {
                        list.add(InsertProps("\n" + getIndents(--indent), i))
                        if (nextChar != ',') {
                            list.add(InsertProps(getIndents(indent), i+1))
                        }
                    }
                }
            }
        }

        val chars = json.toMutableList()
        for (props in list.reversed()) {
            for (c in props.text.toCharArray().reversed()) {
                chars.add(props.index, c)
            }
        }
        value = SpannableString(chars.joinToString(""))

        val startKeyIndexes = value.indexesOf("\t\t\"")
        val endKeyIndexes = value.indexesOf("\":")
        val startValueIndexes = value.indexesOf("\":[\"\\w0-9-]")
        val endValueIndexes = value.indexesOf("[\"\\w\\s:][\"a-zA-Z0-9,]\n")
        spans = arrayListOf()
        spans.apply {
            for (i in startKeyIndexes.indices) {
                val startKeyIndex = startKeyIndexes[i]
                val endKeyIndex = endKeyIndexes[i]+1
                add(SpanProps(ForegroundColorSpan(Color.BLUE), startKeyIndex, endKeyIndex))
            }
            for (i in startValueIndexes.indices) {
                val startValueIndex = startValueIndexes[i]+2
                val endValueIndex = if (value.elementAt(endValueIndexes[i]+1) != ',') endValueIndexes[i]+2 else endValueIndexes[i]+1
                add(SpanProps(ForegroundColorSpan(Color.parseColor("#FF5000")), startValueIndex, endValueIndex))
            }
        }
    }

    private fun CharSequence.indexesOf(pat: String, log: Boolean = false): List<Int> =
        pat.toRegex()
            .findAll(this)
            .map {
                if (log) Log.i("REGEX range: ${it.range}", it.value)
                it.range.first
            }
            .toList()

    private fun getIndents(indent: Int): String {
        var t = ""
        for (i in 0 until indent) {
            t += "\t\t"
        }
        return t
    }

    override val length: Int = value.length

    override fun get(index: Int): Char {
        return value[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return value.subSequence(startIndex, endIndex)
    }

    data class InsertProps(
        val text: String,
        val index: Int
    )
}