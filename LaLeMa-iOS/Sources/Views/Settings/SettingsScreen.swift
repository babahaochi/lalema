import SwiftUI
import UserNotifications

struct SettingsScreen: View {
    @State private var reminderEnabled: Bool = ReminderService.shared.isAlarmEnabled
    @State private var reminderHour: Int = ReminderService.shared.alarmHour
    @State private var reminderMinute: Int = ReminderService.shared.alarmMinute
    @State private var showTimePicker: Bool = false
    @State private var notificationPermissionGranted: Bool = false

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                闹钟提醒卡片

                日历提醒卡片

                关于卡片
            }
            .padding()
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle("设置")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(hour: $reminderHour, minute: $reminderMinute)
        }
        .onAppear {
            checkNotificationPermission()
        }
    }

    private var 闹钟提醒卡片: some View {
        VStack(spacing: 16) {
            HStack {
                Image(systemName: "alarm")
                    .font(.system(size: 24))
                    .foregroundColor(AppColors.primaryLight)

                VStack(alignment: .leading, spacing: 4) {
                    Text("闹钟提醒")
                        .font(.system(size: 16, weight: .medium))
                    Text("每天固定时间提醒您排便")
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }

                Spacer()

                Toggle("", isOn: $reminderEnabled)
                    .tint(AppColors.primaryLight)
                    .onChange(of: reminderEnabled) { _, newValue in
                        if newValue {
                            requestNotificationPermission()
                        } else {
                            ReminderService.shared.setAlarm(hour: reminderHour, minute: reminderMinute, enabled: false)
                        }
                    }
            }

            if reminderEnabled {
                Divider()

                Button(action: { showTimePicker = true }) {
                    HStack {
                        Image(systemName: "clock")
                            .foregroundColor(AppColors.primaryLight)
                        Text("提醒时间")
                            .foregroundColor(.primary)
                        Spacer()
                        Text(FormattedTime(hour: reminderHour, minute: reminderMinute))
                            .foregroundColor(AppColors.primaryLight)
                            .fontWeight(.semibold)
                        Image(systemName: "chevron.right")
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
        .padding()
        .glassCard()
    }

    private var 日历提醒卡片: some View {
        HStack {
            Image(systemName: "calendar")
                .font(.system(size: 24))
                .foregroundColor(AppColors.primaryLight)

            VStack(alignment: .leading, spacing: 4) {
                Text("日历提醒")
                    .font(.system(size: 16, weight: .medium))
                Text("在日历中创建每日排便提醒事件")
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
            }

            Spacer()
        }
        .padding()
        .glassCard()
    }

    private var 关于卡片: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("关于")
                .font(.system(size: 16, weight: .semibold))

            Text("拉了吗 v1.5.0")
                .font(.system(size: 14))
                .foregroundColor(.secondary)

            Text("帮助您养成良好的排便习惯")
                .font(.system(size: 12))
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .glassCard()
    }

    private func FormattedTime(hour: Int, minute: Int) -> String {
        String(format: "%02d:%02d", hour, minute)
    }

    private func checkNotificationPermission() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                notificationPermissionGranted = settings.authorizationStatus == .authorized
            }
        }
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                notificationPermissionGranted = granted
                if granted {
                    ReminderService.shared.setAlarm(hour: reminderHour, minute: reminderMinute, enabled: true)
                } else {
                    reminderEnabled = false
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        SettingsScreen()
    }
}
