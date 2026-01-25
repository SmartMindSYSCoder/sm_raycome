import 'package:flutter/material.dart';
import 'package:sm_raycome/sm_raycome.dart';

class PermissionCard extends StatelessWidget {
  final SmRaycome plugin;
  final Function(String) onLog;

  const PermissionCard({super.key, required this.plugin, required this.onLog});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      color: Theme.of(
        context,
      ).colorScheme.secondaryContainer.withValues(alpha: 0.4),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        leading: CircleAvatar(
          backgroundColor: Theme.of(context).colorScheme.secondary,
          child: const Icon(
            Icons.security_rounded,
            color: Colors.white,
            size: 20,
          ),
        ),
        title: const Text(
          "Plugin Permissions",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: const Text("Bluetooth & Location"),
        trailing: ElevatedButton(
          onPressed: () async {
            bool granted = await plugin.getPermissions();
            onLog(granted ? "Permissions allowed" : "Permissions denied");
            if (!context.mounted) return;
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                behavior: SnackBarBehavior.floating,
                content: Text(granted ? "Access Granted" : "Access Denied"),
                backgroundColor: granted ? Colors.green : Colors.redAccent,
              ),
            );
          },
          style: ElevatedButton.styleFrom(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          child: const Text("Authorize"),
        ),
      ),
    );
  }
}
