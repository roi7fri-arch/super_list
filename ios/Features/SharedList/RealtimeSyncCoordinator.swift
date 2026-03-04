import Foundation

@MainActor
final class RealtimeSyncCoordinator: ObservableObject {
    @Published var lastPayload: String = ""

    func onEvent(_ payload: String) {
        lastPayload = payload
    }
}
