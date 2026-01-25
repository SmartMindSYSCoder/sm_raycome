import 'package:flutter/material.dart';
import 'package:sm_raycome/sm_raycome.dart';

class StatusCard extends StatelessWidget {
  final RaycomeState state;

  const StatusCard({super.key, required this.state});

  @override
  Widget build(BuildContext context) {
    final isConnected =
        state.connectionState == RaycomeConnectionState.connected;

    return Card(
      elevation: 4,
      shadowColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.1),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      child: Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Connection State",
                      style: Theme.of(
                        context,
                      ).textTheme.labelLarge?.copyWith(color: Colors.grey),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      state.displayStatus,
                      style: Theme.of(context).textTheme.headlineSmall
                          ?.copyWith(
                            color: isConnected ? Colors.green : Colors.blueGrey,
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                  ],
                ),
                _PulseIndicator(active: isConnected),
              ],
            ),
            if (state.measurementStatus ==
                RaycomeMeasurementStatus.measuring) ...[
              const SizedBox(height: 24),
              // Show progress bar if we have a value, otherwise show indeterminate
              if (state.runningPressure > 0) ...[
                ClipRRect(
                  borderRadius: BorderRadius.circular(10),
                  child: LinearProgressIndicator(
                    value: state.runningPressure / 200,
                    minHeight: 12,
                    backgroundColor: Theme.of(
                      context,
                    ).colorScheme.primary.withValues(alpha: 0.1),
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      "Current Pressure",
                      style: TextStyle(color: Colors.blueGrey),
                    ),
                    Text(
                      "${state.runningPressure} mmHg",
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    ),
                  ],
                ),
              ] else ...[
                // Show indeterminate loader if pressure is 0 but status is measuring
                const _MeasuringIndicator(),
              ],
            ],
          ],
        ),
      ),
    );
  }
}

class _MeasuringIndicator extends StatelessWidget {
  const _MeasuringIndicator();

  @override
  Widget build(BuildContext context) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 250),
      child: Container(
        key: const ValueKey("measuring"),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Row(
          children: [
            SizedBox(
              height: 22,
              width: 22,
              child: CircularProgressIndicator(
                strokeWidth: 3,
                valueColor: AlwaysStoppedAnimation<Color>(
                  Theme.of(context).colorScheme.primary,
                ),
              ),
            ),
            const SizedBox(width: 12),
            const Expanded(
              child: Text(
                "Measuring in progress… keep still",
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PulseIndicator extends StatelessWidget {
  final bool active;

  const _PulseIndicator({required this.active});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: active
            ? Colors.green.withValues(alpha: 0.1)
            : Colors.blueGrey.withValues(alpha: 0.05),
        shape: BoxShape.circle,
      ),
      child: Icon(
        active ? Icons.bluetooth_connected : Icons.bluetooth_disabled,
        color: active ? Colors.green : Colors.blueGrey,
        size: 28,
      ),
    );
  }
}
