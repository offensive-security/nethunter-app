package com.offsec.nethunter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.InverseBindingAdapter;

public class MITMFViewModel extends BaseObservable {


    private boolean intervalChecked = false;
    private boolean injectJS;
    private boolean injectHTML;
    private boolean injectHTMLPayload;
    private boolean injectJSEnabled = true;
    private boolean injectionEnabled = false;
    private boolean shellShockEnabled = false;
    private boolean spoofEnabled = false;
    private boolean responderChecked = false;

    public void setIntervalChecked(boolean intervalChecked) {
        this.intervalChecked = intervalChecked;
    }

    public void clickInject(View view) {
        injectionEnabled = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.injectionEnabled);
        notifyEnabledChanged();
    }

    public void clickSpoof(View view) {
        spoofEnabled = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.spoofEnabled);
    }

    @Bindable
    public boolean isInjectionEnabled() {
        return injectionEnabled;
    }

    @Bindable
    public boolean isIntervalChecked() {
        return intervalChecked;
    }


    public void setInjectJS(boolean injectJS) {
        this.injectJS = injectJS;
        notifyPropertyChanged(BR.injectJS);
    }


    @Bindable
    public boolean isInjectJS() {
        return injectJS;
    }

    public void clickInjectJS(View view) {
        injectJS = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.injectJS);
    }

    public void setInjectHTML(boolean injectHTML) {
        this.injectHTML = injectHTML;
        notifyPropertyChanged(BR.injectHTML);
    }

    @Bindable
    public boolean isInjectHTML() {
        return injectHTML;
    }


    public void clickInjectHTML(View view) {
        injectHTML = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.injectHTML);
    }

    public void setInjectHTMLPayload(boolean injectHTMLPayload) {
        this.injectHTMLPayload = injectHTMLPayload;
        notifyPropertyChanged(BR.injectHTMLPayload);
    }

    @Bindable
    public boolean isInjectHTMLPayload() {
        return injectHTMLPayload;
    }

    public void clickInjectHTMLPayload(View view) {
        injectHTMLPayload = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.injectHTMLPayload);
    }

    private boolean injectJSEmpty = true;
    public TextWatcher injectJSWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectJSEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };

    private void notifyEnabledChanged() {
        notifyPropertyChanged(BR.injectJSEnabled);
        notifyPropertyChanged(BR.injectHtmlUrlEnabled);
        notifyPropertyChanged(BR.injectHtmlEnabled);
    }

    private boolean injectHtmlURLEmpty = true;

    public TextWatcher injectHtmlUrlWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectHtmlURLEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };

    private boolean injectHtmlEmpty = true;

    public TextWatcher injectHtmlWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            injectHtmlEmpty = s.toString().equals("");
            notifyEnabledChanged();
        }
    };

    @Bindable
    public boolean isInjectJSEnabled() {
        return injectHtmlURLEmpty && injectHtmlEmpty && injectionEnabled;
    }

    @Bindable
    public boolean isInjectHtmlUrlEnabled() {
        return injectJSEmpty && injectHtmlEmpty && injectionEnabled;
    }

    @Bindable
    public boolean isInjectHtmlEnabled() {
        return injectJSEmpty && injectHtmlURLEmpty && injectionEnabled;
    }


    @Bindable
    public boolean isShellShockEnabled() {
        return shellShockEnabled && spoofEnabled;
    }

    public void setShellShockEnabled(boolean enabled) {
        shellShockEnabled = enabled;
        notifyPropertyChanged(BR.shellShockEnabled);
    }

    @Bindable
    public boolean isSpoofEnabled() {
        return spoofEnabled;
    }

    @Bindable
    public boolean isResponderChecked() {
        return responderChecked;
    }

    public void setResponderChecked(boolean responderChecked) {
        this.responderChecked = responderChecked;
        notifyPropertyChanged(BR.responderChecked);
    }

    private static class TextWatcherAdapter implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


    public void responderClicked(View view) {
        responderChecked = ((CheckBox) view).isChecked();
        notifyPropertyChanged(BR.responderChecked);

    }


    public void onClick(View view) {

    }

    @InverseBindingAdapter(attribute = "android:checked", event = "android:checked")
    public static boolean getViewChecked(CheckBox view) {
        return view.isChecked();
    }

}
