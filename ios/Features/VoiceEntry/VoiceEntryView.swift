import SwiftUI

struct VoiceEntryView: View {
    @State private var isHolding = false

    var body: some View {
        VStack {
            Spacer()
            Button(action: {}) {
                Circle()
                    .fill(Color.red)
                    .frame(width: 120, height: 120)
                    .overlay(Text("לחץ ודבר").foregroundColor(.white))
            }
            .accessibilityLabel("כפתור קולי ראשי")
            .simultaneousGesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in isHolding = true }
                    .onEnded { _ in isHolding = false }
            )
            Spacer()
        }
        .environment(\.layoutDirection, .rightToLeft)
    }
}
