library pdf_converter;

export 'image_to_pdf_page.dart';

import 'package:flutter/services.dart';

class PdfConverter {
  static const MethodChannel _channel = MethodChannel('native_pdf');

  static Future<String?> pickImagesAndConvertToPdf() {
    return _channel.invokeMethod('pickImagesAndConvertToPdf');
  }
}
