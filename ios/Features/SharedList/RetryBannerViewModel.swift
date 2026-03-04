import Foundation

@MainActor
final class RetryBannerViewModel: ObservableObject {
    @Published var isVisible: Bool = false
    @Published var message: String = ""

    func showPersistentFailure() {
        isVisible = true
        message = "שגיאת סנכרון מתמשכת, ננסה שוב"
    }

    func clear() {
        isVisible = false
        message = ""
    }
}
