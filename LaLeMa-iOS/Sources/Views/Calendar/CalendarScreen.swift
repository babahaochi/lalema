import SwiftUI

struct CalendarScreen: View {
    @StateObject private var viewModel = CalendarViewModel()

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 7)
    private let weekdays = ["日", "一", "二", "三", "四", "五", "六"]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                日历卡片

                图例卡片
            }
            .padding()
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle("日历")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $viewModel.showRecordForm) {
            RecordFormView { hour, minute, amount, consistency, color, smell, painLevel, blood, mucus, notes in
                viewModel.makeUpRecord(
                    timeHour: hour,
                    timeMinute: minute,
                    amount: amount,
                    consistency: consistency,
                    color: color,
                    smell: smell,
                    painLevel: painLevel,
                    blood: blood,
                    mucus: mucus,
                    notes: notes
                )
            }
        }
        .sheet(isPresented: $viewModel.showDetailDialog) {
            记录详情Sheet(records: viewModel.selectedRecords, onDelete: { record in
                viewModel.deleteRecord(record)
            })
        }
    }

    private var 日历卡片: some View {
        VStack(spacing: 16) {
            HStack {
                Button(action: viewModel.previousMonth) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(AppColors.primaryLight)
                        .frame(width: 44, height: 44)
                }

                Spacer()

                Text(viewModel.yearMonthString)
                    .font(.system(size: 20, weight: .semibold))

                Spacer()

                Button(action: viewModel.nextMonth) {
                    Image(systemName: "chevron.right")
                        .foregroundColor(AppColors.primaryLight)
                        .frame(width: 44, height: 44)
                }
            }

            HStack(spacing: 2) {
                ForEach(weekdays, id: \.self) { day in
                    Text(day)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                }
            }

            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(daysInMonth, id: \.self) { dayInfo in
                    if dayInfo.isEmpty {
                        Color.clear
                            .aspectRatio(1, contentMode: .fit)
                    } else {
                        日期格子(dayInfo: dayInfo)
                    }
                }
            }
            .frame(height: 280)
        }
        .glassCard()
    }

    private var daysInMonth: [DayInfo] {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: viewModel.currentYearMonth)
        let month = calendar.component(.month, from: viewModel.currentYearMonth)

        guard let startOfMonth = calendar.date(from: DateComponents(year: year, month: month, day: 1)),
              let endOfMonth = calendar.date(byAdding: DateComponents(month: 1, day: -1), to: startOfMonth) else {
            return []
        }

        let firstWeekday = calendar.component(.weekday, from: startOfMonth)
        let offset = (firstWeekday - 1) % 7
        let daysCount = calendar.range(of: .day, in: .month, for: startOfMonth)?.count ?? 30

        var days: [DayInfo] = []

        for _ in 0..<offset {
            days.append(DayInfo(day: nil, dateString: nil, isToday: false, isRecorded: false, isFuture: false, isMakeupAvailable: false))
        }

        let today = Date()
        let todayString = formatDateString(today)

        for day in 1...daysCount {
            guard let date = calendar.date(from: DateComponents(year: year, month: month, day: day)) else {
                continue
            }

            let dateString = formatDateString(date)
            let isToday = dateString == todayString
            let isRecorded = viewModel.recordedDates.contains(dateString)
            let isFuture = date > today

            var isMakeupAvailable = false
            if !isFuture && !isRecorded {
                if let daysDiff = calendar.dateComponents([.day], from: date, to: today).day {
                    isMakeupAvailable = daysDiff >= 0 && daysDiff <= 6
                }
            }

            days.append(DayInfo(
                day: day,
                dateString: dateString,
                isToday: isToday,
                isRecorded: isRecorded,
                isFuture: isFuture,
                isMakeupAvailable: isMakeupAvailable
            ))
        }

        let remainder = days.count % 7
        if remainder != 0 {
            for _ in 0..<(7 - remainder) {
                days.append(DayInfo(day: nil, dateString: nil, isToday: false, isRecorded: false, isFuture: false, isMakeupAvailable: false))
            }
        }

        return days
    }

    private func formatDateString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    @ViewBuilder
    private func 日期格子(dayInfo: DayInfo) -> some View {
        if let day = dayInfo.day, let dateString = dayInfo.dateString {
            let isClickable = dayInfo.isToday || dayInfo.isRecorded || dayInfo.isMakeupAvailable

            ZStack {
                if dayInfo.isRecorded {
                    Circle()
                        .fill(AppColors.successLight)
                        .frame(width: 36, height: 36)
                    Text("\(day)")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                } else if dayInfo.isToday {
                    Circle()
                        .stroke(AppColors.primaryLight, lineWidth: 2)
                        .frame(width: 36, height: 36)
                    Text("\(day)")
                        .font(.system(size: 14, weight: .bold))
                } else if dayInfo.isFuture {
                    Text("\(day)")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary.opacity(0.5))
                } else if dayInfo.isMakeupAvailable {
                    Text("\(day)")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(AppColors.warningLight)
                } else {
                    Text("\(day)")
                        .font(.system(size: 14))
                        .foregroundColor(.primary.opacity(0.5))
                }
            }
            .aspectRatio(1, contentMode: .fit)
            .onTapGesture {
                if isClickable {
                    viewModel.onDateClick(dateString)
                }
            }
        } else {
            Color.clear
                .aspectRatio(1, contentMode: .fit)
        }
    }

    private var 图例卡片: some View {
        HStack(spacing: 24) {
            图例项(color: AppColors.successLight, text: "已记录", isOutline: false)
            图例项(color: AppColors.primaryLight, text: "今天", isOutline: true)
            图例项(color: AppColors.warningLight, text: "可补卡", isOutline: false)
        }
        .glassCard()
    }

    private func 图例项(color: Color, text: String, isOutline: Bool) -> some View {
        HStack(spacing: 4) {
            Circle()
                .fill(isOutline ? Color.clear : color)
                .frame(width: 16, height: 16)
                .overlay(
                    Circle()
                        .stroke(color, lineWidth: 2)
                        .opacity(isOutline ? 1 : 0)
                )
            Text(text)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
        }
    }
}

struct DayInfo: Hashable {
    let day: Int?
    let dateString: String?
    let isToday: Bool
    let isRecorded: Bool
    let isFuture: Bool
    let isMakeupAvailable: Bool
}

struct 记录详情Sheet: View {
    let records: [PoopRecord]
    let onDelete: (PoopRecord) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                ForEach(records) { record in
                    记录详情行(record: record)
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                onDelete(record)
                            } label: {
                                Label("删除", systemImage: "trash")
                            }
                        }
                }
            }
            .navigationTitle("记录详情")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("关闭") {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct 记录详情行: View {
    let record: PoopRecord

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "clock")
                    .foregroundColor(AppColors.primaryLight)
                Text(record.formattedTime)
                    .font(.system(size: 18, weight: .bold))
                Spacer()
            }

            HStack {
                Label(record.amount.displayName, systemImage: "circle.grid.2x2")
                Spacer()
                Label(record.consistency.displayName, systemImage: "drop")
            }
            .font(.system(size: 14))
            .foregroundColor(.secondary)

            HStack {
                Circle()
                    .fill(Color(hex: record.color.hexColor))
                    .frame(width: 20, height: 20)
                Text(record.color.displayName)
                Spacer()
                Text(record.smell.displayName)
            }
            .font(.system(size: 14))

            if record.blood || record.mucus {
                HStack(spacing: 8) {
                    if record.blood {
                        Text("有血")
                            .foregroundColor(.red)
                            .fontWeight(.medium)
                    }
                    if record.blood && record.mucus {
                        Text("|")
                            .foregroundColor(.secondary)
                    }
                    if record.mucus {
                        Text("有粘液")
                            .foregroundColor(.orange)
                            .fontWeight(.medium)
                    }
                }
                .font(.system(size: 14))
            }

            if !record.notes.isEmpty {
                Text("备注: \(record.notes)")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 8)
    }
}

#Preview {
    NavigationStack {
        CalendarScreen()
    }
}
