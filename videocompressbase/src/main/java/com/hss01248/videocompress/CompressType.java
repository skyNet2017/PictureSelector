package com.hss01248.videocompress;

import androidx.annotation.StringDef;

public interface CompressType {

    String TYPE_SDR_480P = "sdr-480p";
    String TYPE_SDR_360P = "sdr-360p";
    String TYPE_SDR_720P = "sdr-720p";
    String TYPE_SDR_1080P = "sdr-1080p";

    String TYPE_HDR_720P = "hdr-720p";
    String TYPE_HDR_1080P = "hdr-1080p";

    String TYPE_HDR_2K = "hdr-2k";
    String TYPE_HDR_4K = "hdr-4k";

    String TYPE_FOR_STORE = "for-store";



    @StringDef({TYPE_SDR_720P, TYPE_SDR_1080P,TYPE_SDR_480P,
            TYPE_SDR_360P,TYPE_HDR_720P,TYPE_HDR_1080P,TYPE_HDR_2K,TYPE_HDR_4K,TYPE_FOR_STORE})
    public @interface Type {

    }
}
