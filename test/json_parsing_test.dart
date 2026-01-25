import 'package:flutter_test/flutter_test.dart';
import 'package:sm_raycome/models/blood_pressure_result.dart';
import 'package:sm_raycome/models/raycome_event.dart';
import 'package:sm_raycome/src/raycome_constants.dart';

void main() {
  group('BloodPressureResult Parsing', () {
    test('Correctly parses valid map', () {
      final map = {
        'systolic': 120,
        'diastolic': 80,
        'heartRate': 70,

        'createTime': 1234567890,
      };
      final result = BloodPressureResult.fromMap(map);
      expect(result.systolic, 120);
      expect(result.diastolic, 80);
      expect(result.heartRate, 70);

      expect(result.createTime, 1234567890);
    });

    test('Handles null values', () {
      final map = {};
      final result = BloodPressureResult.fromMap(map);
      expect(result.systolic, 0);
      expect(result.diastolic, 0);
    });
  });

  group('RaycomeEvent Parsing', () {
    test('Parses connection state connected', () {
      final map = {
        'type': RaycomeConstants.eventConnectionState,
        'data': {'isConnected': true},
      };
      final event = RaycomeEvent.fromMap(map);
      expect(event.type, RaycomeEventType.connectionState);
      expect(event.data, RaycomeConnectionState.connected);
    });

    test('Parses connection state disconnected', () {
      final map = {
        'type': RaycomeConstants.eventConnectionState,
        'data': {'isConnected': false},
      };
      final event = RaycomeEvent.fromMap(map);
      expect(event.type, RaycomeEventType.connectionState);
      expect(event.data, RaycomeConnectionState.disconnected);
    });

    test('Parses measurement result', () {
      final map = {
        'type': RaycomeConstants.eventMeasurementResult,
        'data': {
          'systolic': 118,
          'diastolic': 78,
          'heartRate': 75,

          'createTime': 1000,
        },
      };
      final event = RaycomeEvent.fromMap(map);
      expect(event.type, RaycomeEventType.measurementResult);
      expect(event.result, isNotNull);
      expect(event.result!.systolic, 118);
      expect(event.data, isA<BloodPressureResult>());
    });

    test('Parses running pressure', () {
      final map = {
        'type': RaycomeConstants.eventRunning,
        'data': "Pressure: 150",
      };
      final event = RaycomeEvent.fromMap(map);
      expect(event.type, RaycomeEventType.running);
      expect(event.data, "Pressure: 150");
    });

    test('Parses explicit disconnect event', () {
      final map = {'type': RaycomeConstants.eventDisconnected, 'data': null};
      final event = RaycomeEvent.fromMap(map);
      expect(event.type, RaycomeEventType.disconnected);
      expect(event.data, RaycomeConnectionState.disconnected);
    });
  });
}
