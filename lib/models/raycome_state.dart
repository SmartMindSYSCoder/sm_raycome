import 'blood_pressure_result.dart';
import 'raycome_event.dart';

enum RaycomeMeasurementStatus { idle, scanning, measuring, success, error }

class RaycomeState {
  final RaycomeConnectionState connectionState;
  final RaycomeMeasurementStatus measurementStatus;
  final int runningPressure;
  final BloodPressureResult? lastResult;
  final String? errorMessage;

  RaycomeState({
    required this.connectionState,
    required this.measurementStatus,
    this.runningPressure = 0,
    this.lastResult,
    this.errorMessage,
  });

  factory RaycomeState.initial() {
    return RaycomeState(
      connectionState: RaycomeConnectionState.unknown,
      measurementStatus: RaycomeMeasurementStatus.idle,
    );
  }

  RaycomeState copyWith({
    RaycomeConnectionState? connectionState,
    RaycomeMeasurementStatus? measurementStatus,
    int? runningPressure,
    BloodPressureResult? lastResult,
    String? errorMessage,
  }) {
    return RaycomeState(
      connectionState: connectionState ?? this.connectionState,
      measurementStatus: measurementStatus ?? this.measurementStatus,
      runningPressure: runningPressure ?? this.runningPressure,
      lastResult: lastResult ?? this.lastResult,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }

  String get displayStatus {
    switch (measurementStatus) {
      case RaycomeMeasurementStatus.scanning:
        return "Scanning...";
      case RaycomeMeasurementStatus.measuring:
        return "Measuring";
      case RaycomeMeasurementStatus.success:
        return "Measured";
      case RaycomeMeasurementStatus.error:
        return "Error";
      case RaycomeMeasurementStatus.idle:
        break;
    }
    if (connectionState == RaycomeConnectionState.connected) {
      return "Connected";
    }
    return "Disconnected";
  }

  /// Returns true if the plugin is currently performing an operation (Scanning, Connecting, Measuring).
  /// Excludes success state to allow immediate re-start.
  bool get isBusy =>
      (measurementStatus == RaycomeMeasurementStatus.scanning ||
          measurementStatus == RaycomeMeasurementStatus.measuring ||
          connectionState == RaycomeConnectionState.connected) &&
      measurementStatus != RaycomeMeasurementStatus.success;

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is RaycomeState &&
        other.connectionState == connectionState &&
        other.measurementStatus == measurementStatus &&
        other.runningPressure == runningPressure &&
        other.lastResult == lastResult &&
        other.errorMessage == errorMessage;
  }

  @override
  int get hashCode => Object.hash(
    connectionState,
    measurementStatus,
    runningPressure,
    lastResult,
    errorMessage,
  );

  @override
  String toString() {
    return 'RaycomeState(connectionState: $connectionState, measurementStatus: $measurementStatus, runningPressure: $runningPressure, lastResult: $lastResult, errorMessage: $errorMessage)';
  }
}
