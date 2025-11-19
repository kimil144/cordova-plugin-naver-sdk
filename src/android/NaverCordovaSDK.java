package com.raccoondev85.plugin.naver;

import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NaverCordovaSDK extends CordovaPlugin {

    private static final String LOG_TAG = "NaverCordovaSDK";
    private static final boolean DEBUG_LOG = true;
//    private OAuthLogin NaverIdLoginSDK.INSTANCE;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        try{
            NaverResources.initResources(cordova.getActivity().getApplication());
            NaverIdLoginSDK.INSTANCE.initialize(cordova.getActivity().getApplication(), NaverResources.OAUTH_CLIENT_ID, NaverResources.OAUTH_CLIENT_SECRET, NaverResources.OAUTH_CLIENT_NAME);
//            NaverResources.initResources(cordova.getActivity().getApplication());
//            NaverIdLoginSDK.INSTANCE = OAuthLogin.getInstance();
//            NaverIdLoginSDK.INSTANCE.showDevelopersLog(DEBUG_LOG);
//            NaverIdLoginSDK.INSTANCE.init(cordova.getActivity(), NaverResources.OAUTH_CLIENT_ID,
//                    NaverResources.OAUTH_CLIENT_SECRET, NaverResources.OAUTH_CLIENT_NAME);
        }catch (Exception e) {

        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            this.login(callbackContext);
            return true;
        } else if (action.equals("logout")) {
            this.logout(callbackContext);
            return true;
        } else if (action.equals("unlinkApp")) {
            this.unlinkApp(callbackContext);
            return true;
        } else if (action.equals("refreshAccessToken")) {
            this.refreshAccessToken(callbackContext);
            return true;
        } else if (action.equals("getAccessToken")) {
            this.getAccessToken(callbackContext);
            return true;
        }

        return false;
    }


    private void login(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NaverIdLoginSDK.INSTANCE.authenticate(cordova.getActivity(), new OAuthLoginCallback() {

                    @Override
                    public void onSuccess() {
                        String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                        String refreshToken = NaverIdLoginSDK.INSTANCE.getRefreshToken();
                        long expiresAt = NaverIdLoginSDK.INSTANCE.getExpiresAt();
                        String tokenType = NaverIdLoginSDK.INSTANCE.getTokenType();

                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("accessToken", accessToken);
                            userinfo.put("refreshToken", refreshToken);
                            userinfo.put("expiresAt", expiresAt);
                            userinfo.put("tokenType", tokenType);

                            callbackContext.success(userinfo);

//                            JSONObject prop = new JSONObject(content.toString());
//                            JSONObject[] objs = new JSONObject[] { userinfo, (JSONObject) prop.get("response") };
//                            for (JSONObject obj : objs) {
//                                Iterator it = obj.keys();
//                                while (it.hasNext()) {
//                                    String key = (String)it.next();
//                                    resultObject.put(key, obj.get(key));
//                                }
//                            }
//                            mCallbackContext.success(resultObject);
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(int httpStatus, String message) {
                        String errorCode = NaverIdLoginSDK.INSTANCE.getLastErrorCode().getCode();
                        String errorDescription = NaverIdLoginSDK.INSTANCE.getLastErrorDescription();

                        JSONObject resultObject = new JSONObject();
                        try {
                            resultObject.put("code", errorCode);
                            resultObject.put("description", errorDescription);

                            callbackContext.error(resultObject);
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        onFailure(errorCode, message);
                    }
                });
            }
        });

    }


    private void logout(CallbackContext callbackContext) {
        NaverIdLoginSDK.INSTANCE.logout();
        callbackContext.success();
    }


    private void unlinkApp(CallbackContext callbackContext) {

        NidOAuthLogin login = new NidOAuthLogin();
        login.callDeleteTokenApi(cordova.getActivity(), new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                callbackContext.success();
           }

            @Override
            public void onFailure(int httpStatus, String message) {
                String errorCode = NaverIdLoginSDK.INSTANCE.getLastErrorCode().getCode();
                String errorDescription = NaverIdLoginSDK.INSTANCE.getLastErrorDescription();

                JSONObject resultObject = new JSONObject();
                try {
                    resultObject.put("code", errorCode);
                    resultObject.put("description", errorDescription);

                    callbackContext.error(resultObject);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
           }

            @Override
            public void onError(int errorCode, String message) {
                onFailure(errorCode, message);
            }
        });
    }


    private void refreshAccessToken(CallbackContext callbackContext) {
        NidOAuthLogin login = new NidOAuthLogin();
        login.callRefreshAccessTokenApi(cordova.getActivity(), new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                callbackContext.success(NaverIdLoginSDK.INSTANCE.getAccessToken());
            }

            @Override
            public void onFailure(int httpStatus, String message) {
                String errorCode = NaverIdLoginSDK.INSTANCE.getLastErrorCode().getCode();
                String errorDescription = NaverIdLoginSDK.INSTANCE.getLastErrorDescription();

                JSONObject resultObject = new JSONObject();
                try {
                    resultObject.put("code", errorCode);
                    resultObject.put("description", errorDescription);

                    callbackContext.error(resultObject);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                onFailure(errorCode, message);
            }
        });
    }


    private void getAccessToken(CallbackContext callbackContext) {
        String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
        callbackContext.success(accessToken);
    }


//    private class RequestApiTask extends AsyncTask<Object, Void, String> {
//        private CallbackContext mCallbackContext;
//        private Context mContext;
//
//        @Override
//        protected String doInBackground(Object... args) {
//            mContext = (Context) args[0];
//            mCallbackContext = (CallbackContext)args[1];
//
//            String url = "https://openapi.naver.com/v1/nid/me";
//            String at = NaverIdLoginSDK.INSTANCE.getAccessToken();
//            return NaverIdLoginSDK.INSTANCE.requestApi(mContext, at, url);
//        }
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//
//        protected void onPostExecute(String content) {
//            if(content == null){
//                mCallbackContext.error("API call failed");
//                return;
//            }
//            JSONObject resultObject = new JSONObject();
//
//            try{
//                String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken(mContext);
//                String refreshToken = NaverIdLoginSDK.INSTANCE.getRefreshToken(mContext);
//                long expiresAt = NaverIdLoginSDK.INSTANCE.getExpiresAt(mContext);
//                String tokenType = NaverIdLoginSDK.INSTANCE.getTokenType(mContext);
//
//                JSONObject userinfo = new JSONObject();
//
//                userinfo.put("accessToken", accessToken);
//                userinfo.put("refreshToken", refreshToken);
//                userinfo.put("expiresAt", expiresAt);
//                userinfo.put("tokenType", tokenType);
//
//                JSONObject prop = new JSONObject(content.toString());
//                JSONObject[] objs = new JSONObject[] { userinfo, (JSONObject) prop.get("response") };
//                for (JSONObject obj : objs) {
//                    Iterator it = obj.keys();
//                    while (it.hasNext()) {
//                        String key = (String)it.next();
//                        resultObject.put(key, obj.get(key));
//                    }
//                }
//                mCallbackContext.success(resultObject);
//            }catch (Exception e){
//                mCallbackContext.error(e.getMessage());
//            }
//
//        }
//    }
}
