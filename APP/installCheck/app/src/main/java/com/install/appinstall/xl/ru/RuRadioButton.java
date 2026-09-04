package com.install.appinstall.xl.ru;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class RuRadioButton extends RadioButton {
    public RuRadioButton(Context context) { super(context); }
    public RuRadioButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public RuRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(RuStrings.translate(text), type);
    }
}
