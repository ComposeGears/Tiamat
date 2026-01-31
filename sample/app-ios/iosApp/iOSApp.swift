import SwiftUI
import TiamatApp

@main
struct iOSApp: App {

    init() {
        KoinInit.shared.start()
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}