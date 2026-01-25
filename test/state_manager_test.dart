import 'package:flutter_test/flutter_test.dart';
import 'package:sm_raycome/models/blood_pressure_result.dart';
import 'package:sm_raycome/models/raycome_event.dart';
import 'package:sm_raycome/models/raycome_state.dart';
import 'package:sm_raycome/src/raycome_state_manager.dart';

void main() {
  group('RaycomeStateManager', () {
    late RaycomeStateManager manager;

    setUp(() {
      manager = RaycomeStateManager();
    });

    test('Initial state is correct', () {
      expect(
        manager.currentState.connectionState,
        RaycomeConnectionState.unknown,
      );
      expect(
        manager.currentState.measurementStatus,
        RaycomeMeasurementStatus.idle,
      );
    });

    test('Handles connection state change', () {
      final event = RaycomeEvent(
        type: RaycomeEventType.connectionState,
        data: RaycomeConnectionState.connected,
      );
      final newState = manager.processEvent(event);
      expect(newState, isNotNull);
      expect(
        manager.currentState.connectionState,
        RaycomeConnectionState.connected,
      );
    });

    test('Handles running pressure', () {
      final event = RaycomeEvent(type: RaycomeEventType.running, data: "120");
      final newState = manager.processEvent(event);
      expect(newState!.measurementStatus, RaycomeMeasurementStatus.measuring);
      expect(newState.runningPressure, 120);
    });

    test('Handles measurement success', () {
      final result = BloodPressureResult(
        systolic: 120,
        diastolic: 80,
        heartRate: 70,

        createTime: 0,
      );
      final event = RaycomeEvent(
        type: RaycomeEventType.measurementResult,
        data: result,
        result: result,
      );
      final newState = manager.processEvent(event);
      expect(newState!.measurementStatus, RaycomeMeasurementStatus.success);
      expect(newState.lastResult, result);
    });

    test('Handles error', () {
      final event = RaycomeEvent(
        type: RaycomeEventType.error,
        data: "Low battery",
      );
      final newState = manager.processEvent(event);
      expect(newState!.measurementStatus, RaycomeMeasurementStatus.error);
      expect(newState.errorMessage, "Low battery");
    });

    test('Handles disconnect resets state', () {
      // First set some state
      manager.processEvent(
        RaycomeEvent(type: RaycomeEventType.running, data: "100"),
      );

      final event = RaycomeEvent(
        type: RaycomeEventType.disconnected,
        data: RaycomeConnectionState.disconnected,
      );
      final newState = manager.processEvent(event);

      expect(newState!.connectionState, RaycomeConnectionState.disconnected);
      expect(newState.measurementStatus, RaycomeMeasurementStatus.idle);
      expect(newState.runningPressure, 0);
    });

    test('setScanning manually updates state', () {
      final newState = manager.setScanning();
      expect(newState!.measurementStatus, RaycomeMeasurementStatus.scanning);
      expect(
        manager.currentState.measurementStatus,
        RaycomeMeasurementStatus.scanning,
      );
    });

    test('Power event triggers measuring', () {
      final event = RaycomeEvent(type: RaycomeEventType.power, data: null);
      final newState = manager.processEvent(event);
      expect(newState!.measurementStatus, RaycomeMeasurementStatus.measuring);
    });
  });

  group('RaycomeState Display Status', () {
    test('Returns correct strings', () {
      var state = RaycomeState.initial();
      expect(state.displayStatus, "Disconnected");

      state = state.copyWith(connectionState: RaycomeConnectionState.connected);
      expect(state.displayStatus, "Connected");

      state = state.copyWith(
        measurementStatus: RaycomeMeasurementStatus.scanning,
      );
      expect(state.displayStatus, "Scanning...");

      state = state.copyWith(
        measurementStatus: RaycomeMeasurementStatus.measuring,
      );
      expect(state.displayStatus, "Measuring");

      state = state.copyWith(
        measurementStatus: RaycomeMeasurementStatus.success,
      );
      expect(state.displayStatus, "Measured");

      state = state.copyWith(measurementStatus: RaycomeMeasurementStatus.error);
      expect(state.displayStatus, "Error");
    });
  });
}
