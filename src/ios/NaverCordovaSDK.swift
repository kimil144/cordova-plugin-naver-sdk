import Foundation
import NidThirdPartyLogin

@objc(NaverCordovaSDK)
class NaverCordovaSDK: CDVPlugin {

    override func pluginInitialize() {
        super.pluginInitialize()

        guard
            let appName = Bundle.main.object(forInfoDictionaryKey: "NaverClientName") as? String,
            let clientId = Bundle.main.object(forInfoDictionaryKey: "NaverClientID") as? String,
            let clientSecret = Bundle.main.object(forInfoDictionaryKey: "NaverClientSecret") as? String,
            let urlScheme = Bundle.main.object(forInfoDictionaryKey: "NaverAppScheme") as? String
        else {
            NSLog("NaverCordovaSDK: missing Naver client Info.plist keys")
            return
        }

        NidOAuth.shared.initialize(appName: appName, clientId: clientId, clientSecret: clientSecret, urlScheme: urlScheme)
    }

    @objc(login:)
    func login(_ command: CDVInvokedUrlCommand) {
        NidOAuth.shared.requestLogin { [weak self] result in
            guard let self = self else { return }

            switch result {
            case .success(let output):
                let payload: [String: Any] = [
                    "accessToken": output.accessToken.tokenString,
                    "refreshToken": output.refreshToken.tokenString,
                    "tokenType": "bearer"
                ]
                let pluginResult = CDVPluginResult(status: .ok, messageAs: payload)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            case .failure(let error):
                let pluginResult = CDVPluginResult(status: .error, messageAs: error.errorDescription ?? "Naver login failed")
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    }

    @objc(logout:)
    func logout(_ command: CDVInvokedUrlCommand) {
        NidOAuth.shared.logout()
        let pluginResult = CDVPluginResult(status: .ok, messageAs: "success")
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(unlinkApp:)
    func unlinkApp(_ command: CDVInvokedUrlCommand) {
        NidOAuth.shared.disconnect { [weak self] result in
            guard let self = self else { return }

            let pluginResult: CDVPluginResult
            switch result {
            case .success:
                pluginResult = CDVPluginResult(status: .ok, messageAs: "success")
            case .failure(let error):
                pluginResult = CDVPluginResult(status: .error, messageAs: error.errorDescription ?? "Naver unlink failed")
            }
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    // CDVPlugin registers this automatically for CDVPluginHandleOpenURLNotification
    // (posted by CDVAppDelegate's application:openURL:options:), so the Naver
    // app-switch login callback gets routed back into the SDK without any swizzling.
    override func handleOpenURL(_ notification: Notification) {
        guard let url = notification.object as? URL else { return }
        _ = NidOAuth.shared.handleURL(url)
    }
}
