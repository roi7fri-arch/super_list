import Foundation

@MainActor
final class FallbackPollingCoordinator: ObservableObject {
    @Published var isEnabled: Bool = false

    func enable() {
        isEnabled = true
    }

    func disable() {
        isEnabled = false
    }
}
