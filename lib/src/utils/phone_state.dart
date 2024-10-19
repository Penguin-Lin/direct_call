import 'package:direct_call/direct_call.dart';

class PhoneState {
  PhoneStateStatus status;
  CallRecord? callRecord;

  PhoneState({required this.status, this.callRecord});
}