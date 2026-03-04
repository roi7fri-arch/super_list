import SwiftUI

struct SharedListView: View {
    @State private var selectedItemIds: Set<String> = []

    var body: some View {
        VStack {
            PersistentDeleteControl {
                selectedItemIds.removeAll()
            }
            List {
                Text("רשימת סופר משותפת")
            }
        }
        .environment(\.layoutDirection, .rightToLeft)
    }
}
