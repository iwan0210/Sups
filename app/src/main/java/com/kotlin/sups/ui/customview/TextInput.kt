package com.kotlin.sups.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kotlin.sups.R

class TextInput : TextInputLayout {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val textInput = TextInputEditText(context)
        textInput.inputType = InputType.TYPE_CLASS_TEXT
        textInput.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        textInput.isSingleLine = false
        addView(textInput, 0)

        textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error = if (s.toString().trim().isEmpty()) {
                    context.getString(R.string.cannot_be_empty)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        endIconMode = END_ICON_CLEAR_TEXT
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }
}