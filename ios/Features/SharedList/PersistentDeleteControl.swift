import SwiftUI

struct PersistentDeleteControl: View {
    let onTap: () -> Void

    var body: some View {
        HStack {
            Spacer()
            Button(action: onTap) {
                Text("מחק")
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .accessibilityLabel("כפתור מחיקה קבוע")
        }
        .environment(\.layoutDirection, .rightToLeft)
    }
}
