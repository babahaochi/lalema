import Foundation

final class PoopRepository {
    static let shared = PoopRepository()

    private let database = DatabaseManager.shared
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private init() {}

    func record(
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
    ) -> Bool {
        let record = PoopRecord(
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

        if let _ = database.insertRecord(record) {
            return true
        }
        return false
    }

    func isRecorded(date: String) -> Bool {
        return database.existsByDate(date)
    }

    func getByDate(_ date: String) -> [PoopRecord] {
        return database.getRecordsByDate(date)
    }

    func getByDateRange(startDate: String, endDate: String) -> [PoopRecord] {
        return database.getRecordsByDateRange(startDate: startDate, endDate: endDate)
    }

    func getCountByMonth(year: Int, month: Int) -> Int {
        return database.getDistinctDateCountByMonth(year: year, month: month)
    }

    func getRecordCountByMonth(year: Int, month: Int) -> Int {
        return database.getRecordCountByMonth(year: year, month: month)
    }

    func getStreak() -> Int {
        var streak = 0
        var currentDate = Date()
        var daysChecked = 0

        while daysChecked < 365 {
            let dateString = dateFormatter.string(from: currentDate)
            if database.existsByDate(dateString) {
                streak += 1
                currentDate = Calendar.current.date(byAdding: .day, value: -1, to: currentDate) ?? currentDate
                daysChecked += 1
            } else {
                break
            }
        }

        return streak
    }

    func deleteByDate(_ date: String) {
        database.deleteByDate(date)
    }

    func deleteRecord(id: Int64) {
        database.deleteRecordById(id)
    }
}
