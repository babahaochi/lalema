import Foundation
import SQLite3

final class DatabaseManager {
    static let shared = DatabaseManager()

    private var db: OpaquePointer?
    private let dbFileName = "lalema.sqlite"

    private init() {
        openDatabase()
        createTables()
    }

    deinit {
        if db != nil {
            sqlite3_close(db)
        }
    }

    private func getDocumentsDirectory() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }

    private func openDatabase() {
        let fileURL = getDocumentsDirectory().appendingPathComponent(dbFileName)

        if sqlite3_open(fileURL.path, &db) != SQLITE_OK {
            print("Error opening database")
        }
    }

    private func createTables() {
        let createTableSQL = """
        CREATE TABLE IF NOT EXISTS poop_records (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            time_hour INTEGER DEFAULT 0,
            time_minute INTEGER DEFAULT 0,
            amount TEXT DEFAULT 'NORMAL',
            consistency TEXT DEFAULT 'NORMAL',
            color TEXT DEFAULT 'BROWN',
            smell TEXT DEFAULT 'NORMAL',
            pain_level INTEGER DEFAULT 0,
            blood INTEGER DEFAULT 0,
            mucus INTEGER DEFAULT 0,
            notes TEXT DEFAULT ''
        );
        CREATE INDEX IF NOT EXISTS idx_poop_records_date ON poop_records(date);
        """

        var statement: OpaquePointer?
        if sqlite3_exec(db, createTableSQL, nil, nil, nil) != SQLITE_OK {
            let error = String(cString: sqlite3_errmsg(db))
            print("Error creating tables: \(error)")
        }
    }

    func insertRecord(_ record: PoopRecord) -> Int64? {
        let insertSQL = """
        INSERT INTO poop_records (date, created_at, time_hour, time_minute, amount, consistency, color, smell, pain_level, blood, mucus, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """

        var statement: OpaquePointer?
        var recordId: Int64?

        if sqlite3_prepare_v2(db, insertSQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (record.date as NSString).utf8String, -1, nil)
            sqlite3_bind_int64(statement, 2, record.createdAt)
            sqlite3_bind_int(statement, 3, Int32(record.timeHour))
            sqlite3_bind_int(statement, 4, Int32(record.timeMinute))
            sqlite3_bind_text(statement, 5, (record.amount.rawValue as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 6, (record.consistency.rawValue as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 7, (record.color.rawValue as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 8, (record.smell.rawValue as NSString).utf8String, -1, nil)
            sqlite3_bind_int(statement, 9, Int32(record.painLevel))
            sqlite3_bind_int(statement, 10, record.blood ? 1 : 0)
            sqlite3_bind_int(statement, 11, record.mucus ? 1 : 0)
            sqlite3_bind_text(statement, 12, (record.notes as NSString).utf8String, -1, nil)

            if sqlite3_step(statement) == SQLITE_DONE {
                recordId = sqlite3_last_insert_rowid(db)
            }
        }

        sqlite3_finalize(statement)
        return recordId
    }

    func getRecordsByDate(_ date: String) -> [PoopRecord] {
        let querySQL = "SELECT * FROM poop_records WHERE date = ? ORDER BY time_hour DESC, time_minute DESC;"

        var statement: OpaquePointer?
        var records: [PoopRecord] = []

        if sqlite3_prepare_v2(db, querySQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (date as NSString).utf8String, -1, nil)

            while sqlite3_step(statement) == SQLITE_ROW {
                if let record = parseRecord(from: statement) {
                    records.append(record)
                }
            }
        }

        sqlite3_finalize(statement)
        return records
    }

    func getRecordsByDateRange(startDate: String, endDate: String) -> [PoopRecord] {
        let querySQL = "SELECT * FROM poop_records WHERE date BETWEEN ? AND ? ORDER BY date DESC, time_hour DESC, time_minute DESC;"

        var statement: OpaquePointer?
        var records: [PoopRecord] = []

        if sqlite3_prepare_v2(db, querySQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (startDate as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 2, (endDate as NSString).utf8String, -1, nil)

            while sqlite3_step(statement) == SQLITE_ROW {
                if let record = parseRecord(from: statement) {
                    records.append(record)
                }
            }
        }

        sqlite3_finalize(statement)
        return records
    }

    func existsByDate(_ date: String) -> Bool {
        let querySQL = "SELECT EXISTS(SELECT 1 FROM poop_records WHERE date = ?);"

        var statement: OpaquePointer?
        var exists = false

        if sqlite3_prepare_v2(db, querySQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (date as NSString).utf8String, -1, nil)

            if sqlite3_step(statement) == SQLITE_ROW {
                exists = sqlite3_column_int(statement, 0) == 1
            }
        }

        sqlite3_finalize(statement)
        return exists
    }

    func getDistinctDateCountByMonth(year: Int, month: Int) -> Int {
        let pattern = String(format: "%04d-%02d-%%", year, month)
        let querySQL = "SELECT COUNT(DISTINCT date) FROM poop_records WHERE date LIKE ?;"

        var statement: OpaquePointer?
        var count = 0

        if sqlite3_prepare_v2(db, querySQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (pattern as NSString).utf8String, -1, nil)

            if sqlite3_step(statement) == SQLITE_ROW {
                count = Int(sqlite3_column_int(statement, 0))
            }
        }

        sqlite3_finalize(statement)
        return count
    }

    func getRecordCountByMonth(year: Int, month: Int) -> Int {
        let pattern = String(format: "%04d-%02d-%%", year, month)
        let querySQL = "SELECT COUNT(*) FROM poop_records WHERE date LIKE ?;"

        var statement: OpaquePointer?
        var count = 0

        if sqlite3_prepare_v2(db, querySQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (pattern as NSString).utf8String, -1, nil)

            if sqlite3_step(statement) == SQLITE_ROW {
                count = Int(sqlite3_column_int(statement, 0))
            }
        }

        sqlite3_finalize(statement)
        return count
    }

    func deleteByDate(_ date: String) {
        let deleteSQL = "DELETE FROM poop_records WHERE date = ?;"

        var statement: OpaquePointer?
        if sqlite3_prepare_v2(db, deleteSQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (date as NSString).utf8String, -1, nil)
            sqlite3_step(statement)
        }

        sqlite3_finalize(statement)
    }

    func deleteRecordById(_ id: Int64) {
        let deleteSQL = "DELETE FROM poop_records WHERE id = ?;"

        var statement: OpaquePointer?
        if sqlite3_prepare_v2(db, deleteSQL, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_int64(statement, 1, id)
            sqlite3_step(statement)
        }

        sqlite3_finalize(statement)
    }

    private func parseRecord(from statement: OpaquePointer?) -> PoopRecord? {
        guard let statement = statement else { return nil }

        let id = sqlite3_column_int64(statement, 0)

        guard let dateCString = sqlite3_column_text(statement, 1) else { return nil }
        let date = String(cString: dateCString)

        let createdAt = sqlite3_column_int64(statement, 2)
        let timeHour = Int(sqlite3_column_int(statement, 3))
        let timeMinute = Int(sqlite3_column_int(statement, 4))

        func getStringColumn(_ index: Int32) -> String {
            guard let cString = sqlite3_column_text(statement, index) else { return "" }
            return String(cString: cString)
        }

        let amount = PoopAmount(rawValue: getStringColumn(5)) ?? .normal
        let consistency = PoopConsistency(rawValue: getStringColumn(6)) ?? .normal
        let color = PoopColor(rawValue: getStringColumn(7)) ?? .brown
        let smell = PoopSmell(rawValue: getStringColumn(8)) ?? .normal
        let painLevel = Int(sqlite3_column_int(statement, 9))
        let blood = sqlite3_column_int(statement, 10) == 1
        let mucus = sqlite3_column_int(statement, 11) == 1
        let notes = getStringColumn(12)

        return PoopRecord(
            id: id,
            date: date,
            createdAt: createdAt,
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
    }
}
