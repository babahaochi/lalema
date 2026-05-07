import Foundation

final class ReminderService {
    static let shared = ReminderService()

    private let userDefaults = UserDefaults.standard
    private let alarmEnabledKey = "lalema_alarm_enabled"
    private let alarmHourKey = "lalema_alarm_hour"
    private let alarmMinuteKey = "lalema_alarm_minute"

    private init() {}

    var isAlarmEnabled: Bool {
        get { userDefaults.bool(forKey: alarmEnabledKey) }
        set { userDefaults.set(newValue, forKey: alarmEnabledKey) }
    }

    var alarmHour: Int {
        get {
            if userDefaults.object(forKey: alarmHourKey) != nil {
                return userDefaults.integer(forKey: alarmHourKey)
            }
            return 9
        }
        set { userDefaults.set(newValue, forKey: alarmHourKey) }
    }

    var alarmMinute: Int {
        get {
            if userDefaults.object(forKey: alarmMinuteKey) != nil {
                return userDefaults.integer(forKey: alarmMinuteKey)
            }
            return 0
        }
        set { userDefaults.set(newValue, forKey: alarmMinuteKey) }
    }

    var formattedAlarmTime: String {
        String(format: "%02d:%02d", alarmHour, alarmMinute)
    }

    func setAlarm(hour: Int, minute: Int, enabled: Bool) {
        alarmHour = hour
        alarmMinute = minute
        isAlarmEnabled = enabled

        #if os(iOS)
        if enabled {
            scheduleLocalNotification(hour: hour, minute: minute)
        } else {
            cancelLocalNotification()
        }
        #endif
    }

    #if os(iOS)
    private func scheduleLocalNotification(hour: Int, minute: Int) {
        let center = UNUserNotificationCenter.current()

        center.removePendingNotificationRequests(withIdentifiers: ["lalema_daily_reminder"])

        let content = UNMutableNotificationContent()
        content.title = "该上厕所啦！"
        content.body = "记得记录今天的排便情况哦"
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(
            identifier: "lalema_daily_reminder",
            content: content,
            trigger: trigger
        )

        center.add(request) { error in
            if let error = error {
                print("Error scheduling notification: \(error)")
            }
        }
    }

    private func cancelLocalNotification() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(
            withIdentifiers: ["lalema_daily_reminder"]
        )
    }
    #endif
}

#if os(iOS)
import UserNotifications
#endif
