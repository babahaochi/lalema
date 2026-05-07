import SwiftUI

struct RecordFormView: View {
    @Environment(\.dismiss) private var dismiss

    let onSubmit: (Int, Int, PoopAmount, PoopConsistency, PoopColor, PoopSmell, Int, Bool, Bool, String) -> Void

    @State private var timeHour: Int = Calendar.current.component(.hour, from: Date())
    @State private var timeMinute: Int = Calendar.current.component(.minute, from: Date())
    @State private var selectedAmount: PoopAmount = .normal
    @State private var selectedConsistency: PoopConsistency = .normal
    @State private var selectedColor: PoopColor = .brown
    @State private var selectedSmell: PoopSmell = .normal
    @State private var selectedPainLevel: Int = 0
    @State private var hasBlood: Bool = false
    @State private var hasMucus: Bool = false
    @State private var notes: String = ""
    @State private var showTimePicker: Bool = false

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Button(action: { showTimePicker = true }) {
                        HStack {
                            Image(systemName: "clock")
                                .foregroundColor(AppColors.brown500)
                            Text("时间: \(String(format: "%02d:%02d", timeHour, timeMinute))")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(.secondary)
                        }
                    }
                    .foregroundColor(.primary)
                }

                Section("量多量少") {
                    HStack(spacing: 8) {
                        ForEach(PoopAmount.allCases, id: \.self) { amount in
                            ChoiceChip(
                                label: amount.displayName,
                                selected: selectedAmount == amount
                            ) {
                                selectedAmount = amount
                            }
                        }
                    }
                }

                Section("干稀程度") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(PoopConsistency.allCases, id: \.self) { consistency in
                                ChoiceChip(
                                    label: consistency.displayName,
                                    selected: selectedConsistency == consistency
                                ) {
                                    selectedConsistency = consistency
                                }
                            }
                        }
                    }
                }

                Section("颜色") {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 12) {
                        ForEach(PoopColor.allCases, id: \.self) { color in
                            ColorChip(
                                colorHex: color.hexColor,
                                displayName: color.displayName,
                                selected: selectedColor == color
                            ) {
                                selectedColor = color
                            }
                        }
                    }
                }

                Section("气味") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(PoopSmell.allCases, id: \.self) { smell in
                                ChoiceChip(
                                    label: smell.displayName,
                                    selected: selectedSmell == smell
                                ) {
                                    selectedSmell = smell
                                }
                            }
                        }
                    }
                }

                Section("疼痛程度") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(Array(PainLevel.allCases.enumerated()), id: \.element) { index, level in
                                ChoiceChip(
                                    label: level.displayName,
                                    selected: selectedPainLevel == index
                                ) {
                                    selectedPainLevel = index
                                }
                            }
                        }
                    }
                }

                Section {
                    Toggle("有血", isOn: $hasBlood)
                        .tint(AppColors.brown500)
                    Toggle("有粘液", isOn: $hasMucus)
                        .tint(AppColors.brown500)
                }

                Section("备注") {
                    TextField("备注（可选）", text: $notes)
                }
            }
            .navigationTitle("记录排便")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("确认") {
                        onSubmit(
                            timeHour,
                            timeMinute,
                            selectedAmount,
                            selectedConsistency,
                            selectedColor,
                            selectedSmell,
                            selectedPainLevel,
                            hasBlood,
                            hasMucus,
                            notes
                        )
                        dismiss()
                    }
                    .fontWeight(.semibold)
                    .foregroundColor(AppColors.green500)
                }
            }
            .sheet(isPresented: $showTimePicker) {
                TimePickerSheet(hour: $timeHour, minute: $timeMinute)
            }
        }
    }
}

struct TimePickerSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var hour: Int
    @Binding var minute: Int

    @State private var selectedHour: Int
    @State private var selectedMinute: Int

    init(hour: Binding<Int>, minute: Binding<Int>) {
        self._hour = hour
        self._minute = minute
        self._selectedHour = State(initialValue: hour.wrappedValue)
        self._selectedMinute = State(initialValue: minute.wrappedValue)
    }

    var body: some View {
        NavigationStack {
            VStack {
                DatePicker(
                    "选择时间",
                    selection: Binding(
                        get: {
                            var components = DateComponents()
                            components.hour = selectedHour
                            components.minute = selectedMinute
                            return Calendar.current.date(from: components) ?? Date()
                        },
                        set: { newDate in
                            let components = Calendar.current.dateComponents([.hour, .minute], from: newDate)
                            selectedHour = components.hour ?? 0
                            selectedMinute = components.minute ?? 0
                        }
                    ),
                    displayedComponents: .hourAndMinute
                )
                .datePickerStyle(.wheel)
                .labelsHidden()
                .padding()

                Spacer()
            }
            .navigationTitle("选择时间")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("确定") {
                        hour = selectedHour
                        minute = selectedMinute
                        dismiss()
                    }
                    .fontWeight(.semibold)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
