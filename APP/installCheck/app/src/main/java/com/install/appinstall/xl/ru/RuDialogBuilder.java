package com.install.appinstall.xl.ru;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class RuDialogBuilder extends AlertDialog.Builder {
    public RuDialogBuilder(Context context) { super(context); }
    public RuDialogBuilder(Context context, int themeResId) { super(context, themeResId); }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        return super.setTitle(RuStrings.translate(title));
    }

    @Override
    public AlertDialog.Builder setMessage(CharSequence message) {
        return super.setMessage(RuStrings.translate(message));
    }

    @Override
    public AlertDialog.Builder setPositiveButton(
            CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setPositiveButton(RuStrings.translate(text), listener);
    }

    @Override
    public AlertDialog.Builder setNegativeButton(
            CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setNegativeButton(RuStrings.translate(text), listener);
    }

    @Override
    public AlertDialog.Builder setNeutralButton(
            CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setNeutralButton(RuStrings.translate(text), listener);
    }

    @Override
    public AlertDialog.Builder setItems(
            CharSequence[] items, DialogInterface.OnClickListener listener) {
        return super.setItems(RuStrings.translateArray(items), listener);
    }

    @Override
    public AlertDialog.Builder setSingleChoiceItems(
            CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
        return super.setSingleChoiceItems(
                RuStrings.translateArray(items), checkedItem, listener);
    }

    @Override
    public AlertDialog.Builder setMultiChoiceItems(
            CharSequence[] items,
            boolean[] checkedItems,
            DialogInterface.OnMultiChoiceClickListener listener) {
        return super.setMultiChoiceItems(
                RuStrings.translateArray(items), checkedItems, listener);
    }
}
