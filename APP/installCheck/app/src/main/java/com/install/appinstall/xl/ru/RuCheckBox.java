package com.install.appinstall.xl.ru;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class RuCheckBox extends CheckBox {
    public RuCheckBox(Context context) { super(context); }
    public RuCheckBox(Context context, AttributeSet attrs) { super(context, attrs); }
    public RuCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(RuStrings.translate(text), type);
    }
}
