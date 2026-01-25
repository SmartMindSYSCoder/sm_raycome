import 'package:flutter/material.dart';
import 'dart:async';
import 'package:intl/intl.dart';
import 'package:sm_raycome/sm_raycome.dart';

import 'widgets/permission_card.dart';
import 'widgets/status_card.dart';
import 'widgets/result_card.dart';
import 'widgets/log_viewer.dart';
import 'widgets/scan_button.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Raycome Demo',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6750A4),
          brightness: Brightness.light,
        ),
      ),
      home: const RaycomeHome(),
    );
  }
}

class RaycomeHome extends StatefulWidget {
  const RaycomeHome({super.key});

  @override
  State<RaycomeHome> createState() => _RaycomeHomeState();
}

class _RaycomeHomeState extends State<RaycomeHome> {
  final _smRaycome = SmRaycome();
  String _logs = "";
  StreamSubscription? _subscription;
  bool _isInit = false;

  @override
  void initState() {
    super.initState();
    _initPlugin();
  }

  Future<void> _initPlugin() async {
    await _smRaycome.init();
    // Listen to state changes for logging
    _subscription = _smRaycome.events.listen((event) {
      // Use the displayStatus for readable state
      _addLog(
        "State: ${event.displayStatus} | Status: ${event.measurementStatus.name} | Pressure: ${event.runningPressure}",
      );
    });
    setState(() {
      _isInit = true;
    });
  }

  void _addLog(String log) {
    if (!mounted) return;
    final time = DateFormat('HH:mm:ss').format(DateTime.now());
    // Safe UI update
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        setState(() {
          _logs = "[$time] $log\n$_logs";
        });
      }
    });
  }

  void _clearLogs() {
    setState(() => _logs = "");
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<RaycomeState>(
      stream: _smRaycome.events,
      initialData: _smRaycome.currentEvent,
      builder: (context, snapshot) {
        // Handle loading/initial state if needed
        if (!_isInit && !snapshot.hasData) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        final state = snapshot.data ?? RaycomeState.initial();

        return Scaffold(
          appBar: _buildAppBar(),
          body: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Theme.of(
                    context,
                  ).colorScheme.primaryContainer.withValues(alpha: 0.2),
                  Theme.of(context).colorScheme.surface,
                ],
              ),
            ),
            child: SafeArea(
              child: CustomScrollView(
                slivers: [
                  SliverPadding(
                    padding: const EdgeInsets.all(16),
                    sliver: SliverList(
                      delegate: SliverChildListDelegate([
                        PermissionCard(plugin: _smRaycome, onLog: _addLog),
                        const SizedBox(height: 16),
                        StatusCard(state: state),
                        const SizedBox(height: 16),
                        if (state.lastResult != null) ...[
                          ResultCard(
                            result: state.lastResult!,
                            onClear: () => _smRaycome.clearResult(),
                          ),
                          const SizedBox(height: 16),
                        ],
                        _buildControl(state),
                        const SizedBox(height: 16),
                        LogViewer(logs: _logs, onClear: _clearLogs),
                      ]),
                    ),
                  ),
                ],
              ),
            ),
          ),
          extendBodyBehindAppBar: true,
        );
      },
    );
  }

  Widget _buildControl(RaycomeState state) {
    // Determine if we are in an active state (scanning, connecting, or measuring)
    // If success, we consider it 'not busy' so user can start again
    final isBusy = state.isBusy;

    // If active, show Stop button. If idle/finished/error, show Start button.
    if (isBusy) {
      return ScanButton(
        label: "Stop",
        icon: Icons.stop_circle_outlined,
        backgroundColor: Colors.red.shade700,
        onPressed: () {
          _smRaycome.stop();
          _addLog("Stopping...");
        },
      );
    } else {
      return ScanButton(
        label: "Start",
        icon: Icons.play_arrow_rounded,
        onPressed: () {
          _clearLogs();
          _smRaycome.start();
          _addLog("Starting...");
        },
      );
    }
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      backgroundColor: Colors.transparent,
      elevation: 0,
      title: const Text(
        'Raycome Plugin',
        style: TextStyle(fontWeight: FontWeight.bold),
      ),
      actions: [
        IconButton(
          icon: const Icon(Icons.settings_outlined),
          onPressed: () => _smRaycome.openSettings(),
          tooltip: 'Settings',
        ),
      ],
    );
  }
}
