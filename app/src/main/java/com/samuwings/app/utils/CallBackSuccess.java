package com.samuwings.app.utils;

import com.stripe.android.model.Token;

public interface CallBackSuccess {
    void onstart();
    void success(Token token);
    void failer(Exception error);
}
