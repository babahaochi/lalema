import Foundation
import SwiftUI

final class HomeViewModel: ObservableObject {
    @Published var streak: Int = 0
    @Published var monthCount: Int = 0
    @Published var monthRate: Float = 0
    @Published var todayRecords: [PoopRecord] = []
    @Published var showRecordForm: Bool = false

    private let repository = PoopRepository.shared
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    func loadTodayStatus() {
        let today = Date()
        let todayString = dateFormatter.string(from: today)

        todayRecords = repository.getByDate(todayString)
        streak = repository.getStreak()

        let calendar = Calendar.current
        let year = calendar.component(.year, from: today)
        let month = calendar.component(.month, from: today)
        let dayOfMonth = calendar.component(.day, from: today)

        monthCount = repository.getRecordCountByMonth(year: year, month: month)
        monthRate = dayOfMonth > 0 ? Float(monthCount) / Float(dayOfMonth) : 0
    }

    func record(
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
        let todayString = dateFormatter.string(from: Date())

        _ = repository.record(
            date: todayString,
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

        loadTodayStatus()
    }

    func recordForDate(
        date: String,
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

        loadTodayStatus()
    }
}
