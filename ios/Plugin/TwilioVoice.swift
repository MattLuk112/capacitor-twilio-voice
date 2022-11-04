import Foundation

@objc public class TwilioVoice: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
