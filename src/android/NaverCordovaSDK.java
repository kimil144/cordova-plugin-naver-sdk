package com.raccoondev85.plugin.naver;

import android.content.Context;

import com.navercorp.nid.oauth.NidOAuth;
import com.navercorp.nid.oauth.NidOAuthCallback;
import com.navercorp.nid.oauth.NidOAuthInitializingCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;



public class NaverCordovaSDK extends CordovaPlugin {

    private static final String LOG_TAG = "NaverCordovaSDK";
    private static final boolean DEBUG_LOG = true;
//    private OAuthLogin NaverIdLoginSDK.INSTANCE;

     @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        final Context app = cordova.getActivity().getApplicationContext();

        // SDK 초기화 (5.11.x)
        if (sInitialized.compareAndSet(false, true)) {
            // 리소스 로딩 (기존 그대로)
            NaverResources.initResources(app);

            NidOAuth.initialize(
                app,
                NaverResources.OAUTH_CLIENT_ID,
                NaverResources.OAUTH_CLIENT_SECRET,
                NaverResources.OAUTH_CLIENT_NAME,
                new NidOAuthInitializingCallback() {
                    @Override public void onSuccess() { /* 필요시 로그 */ }
                    @Override public void onFailure(Exception e) { /* 필요시 로그 */ }
                }
            );
            NidOAuth.setShowDevelopersLog(DEBUG_LOG);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "login":              login(callbackContext);            return true;
            case "logout":             logout(callbackContext);           return true;
            case "unlinkApp":          unlinkApp(callbackContext);        return true;
            case "refreshAccessToken": refreshAccessToken(callbackContext); return true;
            case "getAccessToken":     getAccessToken(callbackContext);   return true;
        }
        return false;
    }

    private void login(final CallbackContext cb) {
        cordova.getActivity().runOnUiThread(() ->
            NidOAuth.requestLogin(cordova.getActivity(), new NidOAuthCallback() {
                @Override public void onSuccess() {
                    try {
                        JSONObject userinfo = new JSONObject();
                        userinfo.put("accessToken",  NidOAuth.getAccessToken());
                        userinfo.put("refreshToken", NidOAuth.getRefreshToken());
                        userinfo.put("expiresAt",    NidOAuth.getExpiresAt());
                        userinfo.put("tokenType",    NidOAuth.getTokenType());
                        cb.success(userinfo);
                    } catch (JSONException e) {
                        cb.error(e.getMessage());
                    }
                }
                @Override public void onFailure(String errorCode, String errorDesc) {
                    try {
                        JSONObject err = new JSONObject();
                        err.put("code", errorCode);
                        err.put("description", errorDesc);
                        cb.error(err);
                    } catch (JSONException e) {
                        cb.error(e.getMessage());
                    }
                }
            })
        );
    }

    private void logout(final CallbackContext cb) {
        NidOAuth.logout(new NidOAuthCallback() {
            @Override public void onSuccess() { cb.success(); }
            @Override public void onFailure(String code, String desc) {
                try {
                    JSONObject err = new JSONObject();
                    err.put("code", code);
                    err.put("description", desc);
                    cb.error(err);
                } catch (JSONException e) { cb.error(e.getMessage()); }
            }
        });
    }

    private void unlinkApp(final CallbackContext cb) {
        // 연동 해제 (서버 토큰 삭제 + 클라이언트 토큰 삭제)
        NidOAuth.disconnect(new NidOAuthCallback() {
            @Override public void onSuccess() { cb.success(); }
            @Override public void onFailure(String code, String desc) {
                try {
                    JSONObject err = new JSONObject();
                    err.put("code", code);
                    err.put("description", desc);
                    cb.error(err);
                } catch (JSONException e) { cb.error(e.getMessage()); }
            }
        });
    }

    private void refreshAccessToken(final CallbackContext cb) {
        // 5.11.x에서는 requestLogin이 토큰 갱신 플로우를 포함
        NidOAuth.requestLogin(cordova.getActivity(), new NidOAuthCallback() {
            @Override public void onSuccess() {
                cb.success(NidOAuth.getAccessToken());
            }
            @Override public void onFailure(String code, String desc) {
                try {
                    JSONObject err = new JSONObject();
                    err.put("code", code);
                    err.put("description", desc);
                    cb.error(err);
                } catch (JSONException e) { cb.error(e.getMessage()); }
            }
        });
    }

    private void getAccessToken(CallbackContext cb) {
        cb.success(NidOAuth.getAccessToken());
    }
}
