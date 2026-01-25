import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'models/raycome_state.dart';
import 'models/raycome_event.dart';
import 'src/raycome_permissions.dart';
import 'src/raycome_constants.dart';
import 'src/raycome_state_manager.dart';

export 'src/raycome_permissions.dart';
export 'src/raycome_constants.dart';
export 'models/raycome_state.dart';
export 'models/raycome_event.dart';

class SmRaycome {
  static final SmRaycome _instance = SmRaycome._internal();

  factory SmRaycome() {
    return _instance;
  }

  SmRaycome._internal();

  static const MethodChannel _methodChannel = MethodChannel(
    RaycomeConstants.methodChannelName,
  );
  static const EventChannel _eventChannel = EventChannel(
    RaycomeConstants.eventChannelName,
  );

  final ValueNotifier<RaycomeConnectionState> connectionStateNotifier =
      ValueNotifier(RaycomeConnectionState.unknown);

  bool get isConnected =>
      connectionStateNotifier.value == RaycomeConnectionState.connected;

  final RaycomeStateManager _stateManager = RaycomeStateManager();
  final StreamController<RaycomeState> _eventController =
      StreamController.broadcast();

  Stream<RaycomeState> get events => _eventController.stream;
  RaycomeState get currentEvent => _stateManager.currentState;

  StreamSubscription? _platformEventSubscription;
  final StreamController<RaycomeEvent> _rawEventController =
      StreamController.broadcast();

  Timer? _disconnectTimer;

  Future<bool?> init() async {
    final result = await _methodChannel.invokeMethod<bool>(
      RaycomeConstants.methodInit,
    );
    // Subscribe to platform events internally to ensure state is always updated
    _platformEventSubscription?.cancel();

    // Directly listen to the event channel
    _platformEventSubscription = _eventChannel.receiveBroadcastStream().listen((
      rawEvent,
    ) {
      final event = RaycomeEvent.fromMap(rawEvent as Map);
      _updateConnectionState(event);

      // Delegate state management to helper class
      final newState = _stateManager.processEvent(event);
      if (newState != null) {
        _eventController.add(newState);

        // Check for success to start auto-disconnect timer
        if (newState.measurementStatus == RaycomeMeasurementStatus.success) {
          _startDisconnectTimer();
        } else if (newState.measurementStatus ==
            RaycomeMeasurementStatus.measuring) {
          // Cancel timer if we start measuring again effectively
          _disconnectTimer?.cancel();
        }
      }

      _rawEventController.add(event);
    });
    return result;
  }

  void _startDisconnectTimer() {
    debugPrint("Raycome: Starting auto-disconnect timer (10s)");
    _disconnectTimer?.cancel();
    _disconnectTimer = Timer(const Duration(seconds: 10), () async {
      debugPrint("Raycome: Timer fired. Attempting disconnect...");
      await stop();
    });
  }

  /// Request all necessary permissions for Raycome SDK.
  Future<bool> getPermissions() {
    return RaycomePermissions.checkAndRequestPermissions();
  }

  /// Open application settings.
  Future<void> openSettings() {
    return RaycomePermissions.openSettings();
  }

  Future<bool?> start() async {
    // Cancel any pending disconnect timer to allow new measurement
    _disconnectTimer?.cancel();

    // Manually update state to scanning
    final newState = _stateManager.setScanning();
    if (newState != null) {
      _eventController.add(newState);
    }

    return await _methodChannel.invokeMethod<bool>(
      RaycomeConstants.methodStart,
    );
  }

  Future<bool?> stop() async {
    // Manually force disconnected state to ensure immediate UI feedback
    debugPrint("Raycome: Stopping and forcing disconnected state update.");
    final event = RaycomeEvent(
      type: RaycomeEventType.disconnected,
      data: RaycomeConnectionState.disconnected,
    );
    _updateConnectionState(event);
    final newState = _stateManager.processEvent(event);
    if (newState != null) {
      _eventController.add(newState);
    }
    _rawEventController.add(event);

    return await _methodChannel.invokeMethod<bool>(RaycomeConstants.methodStop);
  }

  Stream<RaycomeEvent> get rawEvents => _rawEventController.stream;

  void _updateConnectionState(RaycomeEvent event) {
    if (event.type == RaycomeEventType.connectionState &&
        event.data is RaycomeConnectionState) {
      connectionStateNotifier.value = event.data as RaycomeConnectionState;
    } else if (event.type == RaycomeEventType.disconnected) {
      connectionStateNotifier.value = RaycomeConnectionState.disconnected;
    }
  }

  /// Manually clears the measurement result.
  void clearResult() {
    final newState = _stateManager.clearResult();
    if (newState != null) {
      _eventController.add(newState);
    }
  }
}
