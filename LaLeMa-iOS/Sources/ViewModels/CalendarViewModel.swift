import Foundation
import SwiftUI

final class CalendarViewModel: ObservableObject {
    @Published var currentYearMonth: Date = Date()
    @Published var recordedDates: Set<String> = []
    @Published var selectedDate: String?
    @Published var selectedRecords: [PoopRecord] = []
    @Published var showRecordForm: Bool = false
    @Published var showDetailDialog: Bool = false

    private let repository = PoopRepository.shared
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private var calendar: Calendar {
        Calendar.current
    }

    init() {
        loadMonth()
    }

    func loadMonth() {
        let year = calendar.component(.year, from: currentYearMonth)
        let month = calendar.component(.month, from: currentYearMonth)

        let startOfMonth = calendar.date(from: DateComponents(year: year, month: month, day: 1))!
        let endOfMonth = calendar.date(byAdding: DateComponents(month: 1, day: -1), to: startOfMonth)!

        let startString = dateFormatter.string(from: startOfMonth)
        let endString = dateFormatter.string(from: endOfMonth)

        let records = repository.getByDateRange(startDate: startString, endDate: endString)
        recordedDates = Set(records.map { $0.date })
    }

    func previousMonth() {
        if let newDate = calendar.date(byAdding: .month, value: -1, to: currentYearMonth) {
            currentYearMonth = newDate
            loadMonth()
        }
    }

    func nextMonth() {
        if let newDate = calendar.date(byAdding: .month, value: 1, to: currentYearMonth) {
            currentYearMonth = newDate
            loadMonth()
        }
    }

    func onDateClick(_ date: String) {
        let today = Date()
        let todayString = dateFormatter.string(from: today)
        let clickedDate = dateFormatter.date(from: date)!

        if recordedDates.contains(date) {
            selectedRecords = repository.getByDate(date)
            selectedDate = date
            showDetailDialog = true
        } else if let daysDiff = calendar.dateComponents([.day], from: clickedDate, to: today).day,
                  daysDiff >= 0 && daysDiff <= 6 {
            selectedDate = date
            showRecordForm = true
        }
    }

    func deleteRecord(_ record: PoopRecord) {
        if let id = record.id {
            repository.deleteRecord(id: id)
            loadMonth()
            showDetailDialog = false
        }
    }

    func makeUpRecord(
        timeHour: Int,
        timeMinute: Int,
        amount: PoopAmount,
        consistency: PoopConsistency,
        color: PoopColor,
        smell: PoopSmell,
        painLevel: Int,
        blood: Bool,
        mucus: Bool,
        notes: String
    ) {
        guard let date = selectedDate else { return }

        _ = repository.record(
            date: date,
            timeHour: timeHour,
            timeMinute: timeMinute,
            amount: amount,
            consistency: consistency,
            color: color,
            smell: smell,
            painLevel: painLevel,
            blood: blood,
            mucus: mucus,
            notes: notes
        )

        loadMonth()
        showRecordForm = false
        selectedDate = nil
    }

    func dismissDialog() {
        showDetailDialog = false
        showRecordForm = false
        selectedDate = nil
        selectedRecords = []
    }

    var yearMonthString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年M月"
        return formatter.string(from: currentYearMonth)
    }
}
