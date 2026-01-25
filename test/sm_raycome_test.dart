import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sm_raycome/sm_raycome.dart';

void main() {
  const MethodChannel channel = MethodChannel('sm_raycome');
  final SmRaycome smRaycome = SmRaycome();
  final List<String> log = <String>[];

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    log.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          log.add(methodCall.method);
          switch (methodCall.method) {
            case 'init':
              return true;
            case 'start':
            case 'stop':
              return true;
            default:
              return null;
          }
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('start invokes method', () async {
    await smRaycome.start();
    expect(log, contains('start'));
  });

  test('stop invokes method', () async {
    await smRaycome.stop();
    expect(log, contains('stop'));
  });

  test('init', () async {
    expect(await smRaycome.init(), true);
  });
}
