package com.uday.urlshortener.service;

public interface QRCodeService {

    byte[] generateQRCodeImage(String text, int width, int height);

}
