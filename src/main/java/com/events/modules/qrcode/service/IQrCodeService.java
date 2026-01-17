package com.events.modules.qrcode.service;

public interface IQrCodeService {
    byte[] generateQrCode(String text, int width, int height);
}
