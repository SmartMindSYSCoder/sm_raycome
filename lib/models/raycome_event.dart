import '../src/raycome_constants.dart';
import 'blood_pressure_result.dart';

enum RaycomeConnectionState { connected, disconnected, unknown }

enum RaycomeEventType {
  connectionState,
  onFoundFinish,
  power,
  running,
  error,
  measurementResult,
  disconnected,
  unknown,
}

class RaycomeEvent {
  final RaycomeEventType type;
  final dynamic data;
  final BloodPressureResult? result;

  RaycomeEvent({required this.type, required this.data, this.result});

  factory RaycomeEvent.fromMap(Map<dynamic, dynamic> map) {
    String typeString = map['type'] as String;
    dynamic data = map['data'];
    BloodPressureResult? result;
    RaycomeEventType type;

    // Map string type to enum
    switch (typeString) {
      case RaycomeConstants.eventConnectionState:
        type = RaycomeEventType.connectionState;
        break;
      case RaycomeConstants.eventOnFoundFinish:
        type = RaycomeEventType.onFoundFinish;
        break;
      case RaycomeConstants.eventPower:
        type = RaycomeEventType.power;
        break;
      case RaycomeConstants.eventRunning:
        type = RaycomeEventType.running;
        break;
      case RaycomeConstants.eventError:
        type = RaycomeEventType.error;
        break;
      case RaycomeConstants.eventMeasurementResult:
        type = RaycomeEventType.measurementResult;
        break;
      case RaycomeConstants.eventDisconnected:
        type = RaycomeEventType.disconnected;
        break;
      default:
        type = RaycomeEventType.unknown;
    }

    if (type == RaycomeEventType.measurementResult && data is Map) {
      result = BloodPressureResult.fromMap(data);
      // We also update data to be the result object for backward compatibility or generic usage if needed
      data = result;
    } else if (type == RaycomeEventType.disconnected) {
      data = RaycomeConnectionState.disconnected;
    } else if (type == RaycomeEventType.connectionState && data is Map) {
      bool isConnected = data['isConnected'] ?? false;
      data = isConnected
          ? RaycomeConnectionState.connected
          : RaycomeConnectionState.disconnected;
    }

    return RaycomeEvent(type: type, data: data, result: result);
  }
}
