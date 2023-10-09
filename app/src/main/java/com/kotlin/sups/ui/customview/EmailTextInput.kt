package com.kotlin.sups.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.kotlin.sups.R
import com.kotlin.sups.helper.isNotEmail

class EmailTextInput : TextInputLayout {

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
        textInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        textInput.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(textInput, 0)

        textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error = if (s.toString().trim().isNotEmail()) {
                    context.getString(R.string.email_invalid)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hint = context.getString(R.string.email)
        endIconMode = END_ICON_CLEAR_TEXT
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        startIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_email)
    }
}