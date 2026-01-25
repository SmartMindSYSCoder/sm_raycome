import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:sm_raycome/models/blood_pressure_result.dart';

class ResultCard extends StatelessWidget {
  final BloodPressureResult result;
  final VoidCallback? onClear;

  const ResultCard({super.key, required this.result, this.onClear});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).colorScheme.primary,
      elevation: 8,
      shadowColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.4),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              if (onClear != null)
                IconButton(
                  onPressed: onClear,
                  icon: const Icon(Icons.close, color: Colors.white54),
                  tooltip: 'Clear Result',
                ),
            ],
          ),
          Padding(
            padding: const EdgeInsets.only(
              right: 20,
              left: 20,
              bottom: 30,
              top: 8,
            ),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _VitalsItem(
                      label: "SYS",
                      value: result.systolic.toString(),
                      unit: "mmHg",
                    ),
                    _VitalsItem(
                      label: "DIA",
                      value: result.diastolic.toString(),
                      unit: "mmHg",
                    ),
                    _VitalsItem(
                      label: "HEART RATE",
                      value: result.heartRate.toString(),
                      unit: "BPM",
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 8,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white12,
                    borderRadius: BorderRadius.circular(30),
                  ),
                  child: Text(
                    "Captured at ${DateFormat('hh:mm a').format(DateTime.now())}",
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _VitalsItem extends StatelessWidget {
  final String label;
  final String value;
  final String unit;

  const _VitalsItem({
    required this.label,
    required this.value,
    required this.unit,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          label,
          style: const TextStyle(
            color: Colors.white70,
            fontWeight: FontWeight.bold,
            letterSpacing: 1.2,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 40,
            fontWeight: FontWeight.w900,
          ),
        ),
        Text(unit, style: const TextStyle(color: Colors.white54, fontSize: 12)),
      ],
    );
  }
}
