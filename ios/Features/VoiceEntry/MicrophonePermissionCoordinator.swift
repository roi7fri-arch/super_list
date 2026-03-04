import Foundation

enum MicrophonePermissionState {
    case granted
    case denied
    case unknown
}

struct MicrophonePermissionCoordinator {
    func canStartCapture(state: MicrophonePermissionState) -> Bool {
        state == .granted
    }

    func guidanceMessage(state: MicrophonePermissionState) -> String {
        switch state {
        case .granted:
            return ""
        case .denied:
            return "נא לאפשר גישה למיקרופון בהגדרות"
        case .unknown:
            return "נדרשת הרשאת מיקרופון"
        }
    }
}
