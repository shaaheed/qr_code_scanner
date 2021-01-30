import 'package:flutter/services.dart';
import 'dart:async';

class PhotoDecoder {
  static const decodeMethodCall = "onDecode";

  final String path;
  final MethodChannel _channel;
  final Completer<String> _completer;

  PhotoDecoder(this.path)
      : _channel = MethodChannel("net.touchcapture.qr.flutterqr/photo_decoder"),
        _completer = Completer<String>() {
    _channel.setMethodCallHandler(
      (MethodCall call) async {
        switch (call.method) {
          case decodeMethodCall:
            if (call.arguments != null) {
              _completer.complete(call.arguments);
            }
        }
      },
    );
  }

  Future<String> decode() {
    _channel.invokeMethod("decode", path);
    return _completer.future;
  }
}
