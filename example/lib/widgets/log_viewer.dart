import 'package:flutter/material.dart';

class LogViewer extends StatelessWidget {
  final String logs;
  final VoidCallback onClear;

  const LogViewer({super.key, required this.logs, required this.onClear});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(4, 16, 4, 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("Activity Logs"),
              TextButton.icon(
                onPressed: onClear,
                icon: const Icon(Icons.clear_all_rounded, size: 18),
                label: const Text("Clear"),
              ),
            ],
          ),
        ),
        Container(
          height: 180,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white.withValues(alpha: 0.5),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: Colors.black.withValues(alpha: 0.05)),
          ),
          child: SingleChildScrollView(
            reverse: true,
            child: SizedBox(
              width: double.infinity,
              child: Text(logs.isEmpty ? "System ready." : logs),
            ),
          ),
        ),
      ],
    );
  }
}
