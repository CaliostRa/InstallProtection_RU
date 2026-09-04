package com.install.appinstall.xl.ru;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class RuButton extends Button {
    public RuButton(Context context) { super(context); }
    public RuButton(Context context, AttributeSet attrs) { super(context, attrs); }
    public RuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(RuStrings.translate(text), type);
    }
}
