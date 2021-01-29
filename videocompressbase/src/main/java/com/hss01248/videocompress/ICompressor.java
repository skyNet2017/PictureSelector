package com.hss01248.videocompress;

import com.hss01248.videocompress.listener.ICompressListener;

public interface ICompressor {

    void compress(VideoInfo.RealCompressInfo info ,String inputPath, String outPath,@CompressType.Type String compressType, ICompressListener listener);
}
