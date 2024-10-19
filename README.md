## HOW TO INSTALL

#### Flutter

```yaml
dependencies:
  flutter:
    sdk: flutter
  direct_call: 1.0.0
```
#### Android: Added permission on manifest

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.CALL_PHONE" />
```
## HOW TO USE

```dart
class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    DirectCall.stream.listen((event) {
      switch (event.status) {
        case PhoneStateStatus.CALL_STATE_IDLE:
          debugPrint("手机状态：通话结束：${event.callRecord}");

        case PhoneStateStatus.CALL_STATE_RINGING:
          debugPrint("手机状态：来电话状态");

        case PhoneStateStatus.CALL_STATE_OFFHOOK:
          debugPrint("手机状态：正在接听电话/拨号");

        default:
          debugPrint("手机状态，非通话相关");
      }
    });

    super.initState();
  }

  @override
  void deactivate() {
    super.deactivate();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: () async {
              final result = await DirectCall.makePhoneCall('10010', 0);
              debugPrint("result:$result");
            },
            child: const Text('打电话'),
          ),
        ),
      ),
    );
  }
}
```

