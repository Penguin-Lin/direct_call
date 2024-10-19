
class CallRecord {
  String number;
  int type;
  int date;
  int duration;

  CallRecord({
    required this.number, 
    required this.type,
    required this.date,
    required this.duration
  });

  static fromMap(event) {
    return CallRecord(
      number: event['number'],
      type: event['type'],
      date: event['date'],
      duration: event['duration']
    );
  }
}