import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        Platform.shared.start()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
