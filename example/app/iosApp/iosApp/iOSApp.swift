import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        Platform.shared.start()
        A3rdParty.shared.start()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
