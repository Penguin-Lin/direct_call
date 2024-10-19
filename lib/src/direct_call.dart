import 'dart:async';

import 'package:direct_call/src/utils/call_record.dart';
import 'package:direct_call/src/utils/constants.dart';
import 'package:direct_call/src/utils/phone_state.dart';
import 'package:direct_call/src/utils/phone_state_status.dart';
import 'package:flutter/services.dart';

class DirectCall {
  DirectCall();

  static const MethodChannel _methodChannel = MethodChannel(Constants.METHOD_CHANNEL);
  static const EventChannel _eventChannel = EventChannel(Constants.EVENT_CHANNEL);

  static Future<dynamic> makePhoneCall(String number, int? simSlot) async {
    try {
      final result = await _methodChannel
          .invokeMethod('makePhoneCall', {'number': number, 'simSlot': simSlot});

      return result;
    } on PlatformException catch (e) {
      print("Failed to get data: '$e'.");
      return 'Error: $e';
    }
  }

  static final Stream<PhoneState> stream =
      _eventChannel.receiveBroadcastStream().distinct().map((dynamic event) => PhoneState(
            status:
                PhoneStateStatus.values.firstWhere((element) => element.name == event['status']),
            callRecord:
                event['callRecord'] == null ? null : CallRecord.fromMap(event['callRecord']),
          ));
}
