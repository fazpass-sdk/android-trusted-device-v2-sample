package com.fazpass.tdv2_showcase_mobile.views

import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.fazpass.tdv2_showcase_mobile.R
import com.fazpass.tdv2_showcase_mobile.objects.SpanProps

class EntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private var _name: String
    private var _value: CharSequence

    var name: String
        get() = _name
        set(value) {
            _name = value
            updateView()
        }

    var value: CharSequence
        get() = _value
        set(value) {
            _value = value
            updateView()
        }

    fun setSpans(props: List<SpanProps>) {
        for (prop in props) {
            valueView.text.setSpan(prop.what, prop.start, prop.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private val nameView: TextView
    private val valueView: EditText

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EntryView, defStyle, 0)
        _name = a.getString(R.styleable.EntryView_name) ?: ""
        _value = a.getString(R.styleable.EntryView_value) ?: ""
        a.recycle()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.entry_view, this)

        nameView = findViewById(R.id.ev_name)
        valueView = findViewById(R.id.ev_value)
        valueView.keyListener = null

        orientation = VERTICAL

        updateView()
    }

    private fun updateView() {
        nameView.text = _name
        valueView.setText(_value)
    }
}