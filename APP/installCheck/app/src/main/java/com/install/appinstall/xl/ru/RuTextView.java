package com.install.appinstall.xl.ru;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RuTextView extends TextView {
    public RuTextView(Context context) { super(context); }
    public RuTextView(Context context, AttributeSet attrs) { super(context, attrs); }
    public RuTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(RuStrings.translate(text), type);
    }
}
