import '../models/raycome_event.dart';
import '../models/raycome_state.dart';

/// Manages state transitions for the Raycome plugin.
class RaycomeStateManager {
  RaycomeState _currentState;

  RaycomeStateManager({RaycomeState? initialState})
    : _currentState = initialState ?? RaycomeState.initial();

  RaycomeState get currentState => _currentState;

  /// Processes an event and returns the new state if it changed, null otherwise.
  RaycomeState? processEvent(RaycomeEvent event) {
    RaycomeState newState = _currentState;

    if (event.type == RaycomeEventType.connectionState &&
        event.data is RaycomeConnectionState) {
      newState = newState.copyWith(
        connectionState: event.data as RaycomeConnectionState,
      );
    } else if (event.type == RaycomeEventType.disconnected) {
      newState = _currentState.copyWith(
        connectionState: RaycomeConnectionState.disconnected,
        measurementStatus: RaycomeMeasurementStatus.idle,
        runningPressure: 0,
        // Keep lastResult visible
      );
    } else if (event.type == RaycomeEventType.running) {
      int pressure = 0;
      final rawData = event.data?.toString() ?? "";
      // Extract digits if the string contains non-numeric characters
      final match = RegExp(r'\d+').firstMatch(rawData);
      if (match != null) {
        pressure = int.tryParse(match.group(0)!) ?? 0;
      }

      newState = newState.copyWith(
        measurementStatus: RaycomeMeasurementStatus.measuring,
        runningPressure: pressure,
      );
    } else if (event.type == RaycomeEventType.measurementResult &&
        event.result != null) {
      newState = newState.copyWith(
        measurementStatus: RaycomeMeasurementStatus.success,
        lastResult: event.result,
        runningPressure: 0,
      );
    } else if (event.type == RaycomeEventType.error) {
      newState = newState.copyWith(
        measurementStatus: RaycomeMeasurementStatus.error,
        errorMessage: event.data.toString(),
        runningPressure: 0,
      );
    } else if (event.type == RaycomeEventType.power) {
      // Power event indicates measurement is about to start - set measuring immediately
      newState = newState.copyWith(
        measurementStatus: RaycomeMeasurementStatus.measuring,
        runningPressure: 0,
      );
    }

    if (newState != _currentState) {
      _currentState = newState;
      return newState;
    }
    return null;
  }

  /// Manually sets the state to scanning and resets previous results.
  RaycomeState? setScanning() {
    final newState = RaycomeState(
      connectionState: _currentState.connectionState,
      measurementStatus: RaycomeMeasurementStatus.scanning,
      lastResult: null,
      runningPressure: 0,
      errorMessage: null,
    );
    if (newState != _currentState) {
      _currentState = newState;
      return newState;
    }
    return null;
  }

  /// Clears the last measurement result.
  RaycomeState? clearResult() {
    final newState = RaycomeState(
      connectionState: _currentState.connectionState,
      measurementStatus: _currentState.measurementStatus,
      runningPressure: _currentState.runningPressure,
      lastResult: null,
      errorMessage: _currentState.errorMessage,
    );

    if (newState != _currentState) {
      _currentState = newState;
      return newState;
    }
    return null;
  }
}
