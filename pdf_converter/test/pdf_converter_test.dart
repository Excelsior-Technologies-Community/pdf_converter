import 'package:flutter_test/flutter_test.dart';
import 'package:pdf_converter/pdf_converter.dart';

void main() {
  test('PdfConverter method exists', () async {
    expect(PdfConverter.pickImagesAndConvertToPdf, isNotNull);
  });
}
