import 'package:flutter/services.dart';
import 'dart:async';

import 'package:qr_code_scanner/qr_code_scanner.dart';

class PhotoDecoder {
  static const decodeMethodCall = "onDecode";

  final String path;
  final Completer<Barcode> _completer = Completer<Barcode>();
  static const MethodChannel _channel =
      MethodChannel('net.touchcapture.qr.flutterqr/photo_decoder');

  PhotoDecoder(this.path) {
    _channel.setMethodCallHandler(
      (MethodCall call) async {
        switch (call.method) {
          case decodeMethodCall:
            if (call.arguments != null) {
              final args = call.arguments as Map;
              final code = args['code'] as String;
              final rawType = args['type'] as String;
              // Raw bytes are only supported by Android.
              final rawBytes = args['rawBytes'] as List<int>;
              final format = BarcodeTypesExtension.fromString(rawType);
              if (format != null) {
                final barcode = Barcode(code, format, rawBytes);
                _completer.complete(barcode);
              } else {
                throw Exception('Unexpected barcode type $rawType');
              }
            }
        }
      },
    );
  }

  Future<Barcode> decode() {
    _channel.invokeMethod("decode", path);
    return _completer.future;
  }
}
