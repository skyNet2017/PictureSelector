package com.hss01248.takephoto.api;

public interface TakePhotoListener {

    void onSuccess(String path);

   default void onFail(String path, String msg){}

   default void onCancel(){}
}
