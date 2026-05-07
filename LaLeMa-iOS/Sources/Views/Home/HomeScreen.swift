import SwiftUI

struct HomeScreen: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var showRecordForm = false
    @State private var buttonScale: CGFloat = 1.0

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 32) {
                    Text("今天感觉如何？")
                        .font(.system(size: 28, weight: .semibold))
                        .padding(.top, 20)

                    记录按钮

                    统计数据

                    日历按钮
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("拉了吗")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: SettingsScreen()) {
                        Image(systemName: "gearshape")
                            .foregroundColor(.white)
                    }
                }
            }
            .onAppear {
                viewModel.loadTodayStatus()
            }
            .sheet(isPresented: $showRecordForm) {
                RecordFormView { hour, minute, amount, consistency, color, smell, painLevel, blood, mucus, notes in
                    viewModel.record(
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
        }
    }

    private var 记录按钮: some View {
        Button(action: {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                buttonScale = 0.92
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                    buttonScale = 1.0
                }
                showRecordForm = true
            }
        }) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [AppColors.primaryLight, AppColors.secondaryLight],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 180, height: 180)
                    .shadow(color: AppColors.primaryLight.opacity(0.3), radius: 20)

                VStack(spacing: 8) {
                    Image(systemName: "plus")
                        .font(.system(size: 48, weight: .medium))
                        .foregroundColor(.white)
                    Text("记录")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                }
            }
        }
        .buttonStyle(PlainButtonStyle())
        .scaleEffect(buttonScale)
    }

    private var 统计数据: some View {
        HStack(spacing: 12) {
            StatCard(
                value: "\(viewModel.streak)",
                label: "连续打卡",
                icon: Image(systemName: "flame.fill"),
                iconColor: AppColors.tertiaryLight
            )

            StatCard(
                value: "\(viewModel.monthCount)",
                label: "本月次数",
                icon: Image(systemName: "face.smiling"),
                iconColor: AppColors.primaryLight
            )

            StatCard(
                value: "\(Int(viewModel.monthRate * 100))%",
                label: "打卡率",
                icon: Image(systemName: "chart.pie.fill"),
                iconColor: AppColors.successLight
            )
        }
    }

    private var 日历按钮: some View {
        NavigationLink(destination: CalendarScreen()) {
            HStack {
                Image(systemName: "calendar")
                    .font(.system(size: 24))
                    .foregroundColor(AppColors.primaryLight)
                Text("查看日历")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(AppColors.primaryLight)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .glassCard()
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    HomeScreen()
}
