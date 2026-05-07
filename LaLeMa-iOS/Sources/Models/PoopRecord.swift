import Foundation

struct PoopRecord: Identifiable, Codable, Equatable {
    var id: Int64?
    let date: String
    let createdAt: Int64
    var timeHour: Int
    var timeMinute: Int
    var amount: PoopAmount
    var consistency: PoopConsistency
    var color: PoopColor
    var smell: PoopSmell
    var painLevel: Int
    var blood: Bool
    var mucus: Bool
    var notes: String

    init(
        id: Int64? = nil,
        date: String,
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        timeHour: Int = Calendar.current.component(.hour, from: Date()),
        timeMinute: Int = Calendar.current.component(.minute, from: Date()),
        amount: PoopAmount = .normal,
        consistency: PoopConsistency = .normal,
        color: PoopColor = .brown,
        smell: PoopSmell = .normal,
        painLevel: Int = 0,
        blood: Bool = false,
        mucus: Bool = false,
        notes: String = ""
    ) {
        self.id = id
        self.date = date
        self.createdAt = createdAt
        self.timeHour = timeHour
        self.timeMinute = timeMinute
        self.amount = amount
        self.consistency = consistency
        self.color = color
        self.smell = smell
        self.painLevel = painLevel
        self.blood = blood
        self.mucus = mucus
        self.notes = notes
    }

    var formattedTime: String {
        String(format: "%02d:%02d", timeHour, timeMinute)
    }
}

enum PoopAmount: String, Codable, CaseIterable {
    case small = "SMALL"
    case normal = "NORMAL"
    case large = "LARGE"

    var displayName: String {
        switch self {
        case .small: return "少量"
        case .normal: return "正常"
        case .large: return "大量"
        }
    }
}

enum PoopConsistency: String, Codable, CaseIterable {
    case veryHard = "VERY_HARD"
    case hard = "HARD"
    case normal = "NORMAL"
    case soft = "SOFT"
    case verySoft = "VERY_SOFT"
    case liquid = "LIQUID"

    var displayName: String {
        switch self {
        case .veryHard: return "非常干"
        case .hard: return "较干"
        case .normal: return "正常"
        case .soft: return "偏软"
        case .verySoft: return "很软"
        case .liquid: return "稀便"
        }
    }
}

enum PoopColor: String, Codable, CaseIterable {
    case brown = "BROWN"
    case darkBrown = "DARK_BROWN"
    case lightBrown = "LIGHT_BROWN"
    case green = "GREEN"
    case black = "BLACK"
    case red = "RED"
    case yellow = "YELLOW"
    case gray = "GRAY"

    var displayName: String {
        switch self {
        case .brown: return "棕色"
        case .darkBrown: return "深棕色"
        case .lightBrown: return "浅棕色"
        case .green: return "绿色"
        case .black: return "黑色"
        case .red: return "红色"
        case .yellow: return "黄色"
        case .gray: return "灰白色"
        }
    }

    var hexColor: String {
        switch self {
        case .brown: return "#8B4513"
        case .darkBrown: return "#5D4037"
        case .lightBrown: return "#A0522D"
        case .green: return "#228B22"
        case .black: return "#1a1a1a"
        case .red: return "#DC143C"
        case .yellow: return "#FFD700"
        case .gray: return "#808080"
        }
    }
}

enum PoopSmell: String, Codable, CaseIterable {
    case normal = "NORMAL"
    case slight = "SLIGHT"
    case strong = "STRONG"
    case veryStrong = "VERY_STRONG"

    var displayName: String {
        switch self {
        case .normal: return "正常"
        case .slight: return "稍有气味"
        case .strong: return "气味较重"
        case .veryStrong: return "非常臭"
        }
    }
}

enum PainLevel: Int, Codable, CaseIterable {
    case none = 0
    case mild = 1
    case moderate = 2
    case severe = 3

    var displayName: String {
        switch self {
        case .none: return "无疼痛"
        case .mild: return "轻微"
        case .moderate: return "中等"
        case .severe: return "严重"
        }
    }
}
