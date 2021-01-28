package com.hss01248.videocompress;

import androidx.annotation.StringDef;

public interface CompressType {
    String TYPE_UPLOAD_720P = "upload720p";
    String TYPE_UPLOAD_1080P = "upload1080p";
    String TYPE_LOCAL_STORE = "localstore";
    String TYPE_BILIBILI = "bilibili";

    @StringDef({TYPE_UPLOAD_720P, TYPE_UPLOAD_1080P,TYPE_LOCAL_STORE,TYPE_BILIBILI})
    public @interface Type {

    }
}
