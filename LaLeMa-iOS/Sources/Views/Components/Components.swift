import SwiftUI

struct ChoiceChip: View {
    let label: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 12, weight: .medium))
                .padding(.horizontal, 8)
                .padding(.vertical, 6)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(selected ? AppColors.brown500 : Color(.systemBackground))
                )
                .foregroundColor(selected ? .white : AppColors.brown700)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(AppColors.brown500.opacity(selected ? 0 : 0.3), lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

struct ColorChip: View {
    let colorHex: String
    let displayName: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 2) {
                Circle()
                    .fill(Color(hex: colorHex))
                    .frame(width: 36, height: 36)
                    .overlay(
                        Circle()
                            .stroke(AppColors.brown500, lineWidth: selected ? 2 : 0)
                    )
                Text(displayName)
                    .font(.system(size: 10))
                    .foregroundColor(selected ? AppColors.brown500 : AppColors.brown700)
            }
        }
        .buttonStyle(.plain)
    }
}

struct StatCard: View {
    let value: String
    let label: String
    let icon: Image
    let iconColor: Color

    var body: some View {
        VStack(spacing: 8) {
            icon
                .font(.system(size: 28))
                .foregroundColor(iconColor)

            Text(value)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.primary)

            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .glassCard()
    }
}

struct GlassButton: View {
    let title: String
    let icon: Image?
    let action: () -> Void

    init(title: String, icon: Image? = nil, action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon = icon {
                    icon
                        .font(.system(size: 20))
                }
                Text(title)
                    .font(.system(size: 16, weight: .medium))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(
                LinearGradient(
                    colors: [AppColors.primaryLight, AppColors.secondaryLight],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .foregroundColor(.white)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}
