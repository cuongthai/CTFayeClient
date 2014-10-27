package com.chatwing.whitelabel.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.facebook.widget.LoginButton;

/**
 * Created by nguyenthanhhuy on 4/15/14.
 * <p/>
 * A login button that allows login text to be set dynamically in at run time.
 */
public class DynamicTextLoginButton extends LoginButton {
    private CharSequence mLoginText;

    public DynamicTextLoginButton(Context context) {
        super(context);
    }

    public DynamicTextLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicTextLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the only login text that this button is to display.
     *
     * @param loginText
     */
    public void setLoginText(CharSequence loginText) {
        mLoginText = loginText;
        setText(loginText);
    }

    /**
     * Sets the text that this button is to display
     * (see {@link com.facebook.widget.LoginButton#setText(CharSequence, android.widget.TextView.BufferType)}).
     * <br/>
     * However, only the text that was set by {@link #setLoginText(CharSequence)}
     * is accepted. All other texts are ignored, including default login and
     * logout texts set by {@link com.facebook.widget.LoginButton super class}.
     * <br/>
     * This method is supposed to be called internally. To change the login text,
     * use {@link #setLoginText(CharSequence)}.
     */
    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        if (text.equals(mLoginText)) {
            super.setText(text, type);
        }
    }
}
