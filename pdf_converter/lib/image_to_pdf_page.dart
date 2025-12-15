import 'package:flutter/material.dart';
import 'pdf_converter.dart';

class ImageToPdfPage extends StatefulWidget {
  const ImageToPdfPage({super.key});

  @override
  State<ImageToPdfPage> createState() => _ImageToPdfPageState();
}

class _ImageToPdfPageState extends State<ImageToPdfPage> {
  bool loading = false;
  String? lastPdfPath;

  Future<void> pickImagesAndConvert() async {
    setState(() => loading = true);

    final path = await PdfConverter.pickImagesAndConvertToPdf();

    if (mounted && path != null) {
      setState(() => lastPdfPath = path);
    }

    setState(() => loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('PDF Converter')),
      body: Center(
        child: ElevatedButton(
          onPressed: pickImagesAndConvert,
          child: const Text('Select Images'),
        ),
      ),
    );
  }
}
