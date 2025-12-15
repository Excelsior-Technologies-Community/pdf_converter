# PDFConverter
A Flutter application that allows users to select multiple images from the device and convert them into a single PDF file using Android native code (Kotlin) via MethodChannel. The generated PDF is automatically saved and opened on the device.

## ✨ Features
* 📸 Select single or multiple images from device storage
* 🔄 Convert images into one PDF document
* ⚡ Native Android PDF generation using PdfDocument
* 🧵 Background thread processing (smooth UI)
* 📂 Automatically saves PDF in app external storage
* 📖 Automatically opens the generated PDF
* 🎯 Clean Flutter UI with loading state & success message
  
  ---
## ✨ Preview
![screen-20251215-1808142](https://github.com/user-attachments/assets/9c554c9b-7403-4365-a237-ac9237311db4)

---

## ✨ Installation
Add this to your package's pubspec.yaml file:
```
dependencies:
  pdf_converter:
    path: ../pdf_converter  # For local development
```
from git:
```
dependencies:
  pdf_converter:
    git:
      url: https://github.com/yourusername/pdf_converter.git  # Your github path
```
Then run:
```
flutter pub get
```
---
## 📁 Project Structure
```
pdf_converter/
├── android/
│   └── app/
│       ├── src/main/java/com/example/pdf_converter/
│       │   └── MainActivity.kt
│       ├── src/main/res/xml/
│       │   └── file_paths.xml
│       ├── src/main/AndroidManifest.xml
│       └── build.gradle
├── lib/
│   └── main.dart
├── pubspec.yaml
└── README.md
```
---
## 📦 Flutter UI Flow
1. User taps Select Images button
2. Shows loading indicator while PDF is generated
3. Displays success message when PDF is created
4. Shows generated PDF file name
---
## 🔐 Required Permissions
Add the following permission in AndroidManifest.xml:
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
---
## 📄 FileProvider Configuration
AndroidManifest.xml:
```
<provider
   android:name="androidx.core.content.FileProvider"
   android:authorities="${applicationId}.fileprovider"
   android:exported="false"
   android:grantUriPermissions="true">
   <meta-data
       android:name="android.support.FILE_PROVIDER_PATHS"
       android:resource="@xml/file_paths" />
</provider>
```
res/xml/file_paths.xml:
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="external_files"
        path="." />
</paths>
```
---
## 📘 Usage
- Open the app on your Android device
- Tap on the Select Images button
- Choose one or multiple images from gallery or file manager
- Confirm the selection
- Wait while the app converts images into a PDF
- PDF will be automatically opened once created
- You will also see a success message with the PDF file name
---
## 🚀 Example
```
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PdfConverterExample extends StatefulWidget {
  const PdfConverterExample({super.key});

  @override
  State<PdfConverterExample> createState() => _PdfConverterExampleState();
}

class _PdfConverterExampleState extends State<PdfConverterExample> {
  static const MethodChannel _channel = MethodChannel('native_pdf');

  String? pdfPath;
  bool loading = false;

  Future<void> convertImagesToPdf() async {
    setState(() => loading = true);

    try {
      final result =
          await _channel.invokeMethod<String>('pickImagesAndConvertToPdf');

      if (result != null) {
        setState(() => pdfPath = result);
      }
    } catch (e) {
      debugPrint('Error: $e');
    }

    setState(() => loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('PDF Converter Example'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton.icon(
              onPressed: loading ? null : convertImagesToPdf,
              icon: const Icon(Icons.picture_as_pdf),
              label: const Text('Select Images & Convert'),
            ),
            const SizedBox(height: 20),
            if (loading) const CircularProgressIndicator(),
            if (pdfPath != null) ...[
              const SizedBox(height: 20),
              Text(
                'PDF Created:',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 8),
              Text(
                pdfPath!,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 12),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
```
---
## 📜 License
MIT License
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
