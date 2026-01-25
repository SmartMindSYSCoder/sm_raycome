class BloodPressureResult {
  final int systolic;
  final int diastolic;
  final int heartRate;

  final int createTime;

  BloodPressureResult({
    required this.systolic,
    required this.diastolic,
    required this.heartRate,

    required this.createTime,
  });

  factory BloodPressureResult.fromMap(Map<dynamic, dynamic> map) {
    return BloodPressureResult(
      systolic: map['systolic'] ?? 0,
      diastolic: map['diastolic'] ?? 0,
      heartRate: map['heartRate'] ?? 0,

      createTime: map['createTime'] ?? 0,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is BloodPressureResult &&
        other.systolic == systolic &&
        other.diastolic == diastolic &&
        other.heartRate == heartRate &&
        other.createTime == createTime;
  }

  @override
  int get hashCode => Object.hash(systolic, diastolic, heartRate, createTime);

  @override
  String toString() {
    return 'BloodPressureResult(systolic: $systolic, diastolic: $diastolic, heartRate: $heartRate)';
  }
}
