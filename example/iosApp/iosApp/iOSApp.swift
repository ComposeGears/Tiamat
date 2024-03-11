import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinLib.shared.start()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
