package com.custom.animeapp;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private PinchFrameLayout customViewContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Suporte total a telas com notch e formato ultra-wide 21:9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        customViewContainer = findViewById(R.id.customViewContainer);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Sincroniza o zoom de pinça nativo com o elemento de vídeo no navegador (object-fit: cover)
        customViewContainer.setOnZoomChangeListener(isZoomed -> {
            String mode = isZoomed ? "cover" : "contain";
            runOnUiThread(() -> {
                webView.evaluateJavascript("if (window.setVideoFitMode) window.setVideoFitMode('" + mode + "');", null);
            });
        });

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new FullscreenWebChromeClient());

        // Se o sistema operacional tentar restaurar a barra de status em modo horizontal, esconde novamente
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && (isLandscape() || customView != null)) {
                getWindow().getDecorView().postDelayed(this::hideSystemBars, 200);
            }
        });

        String targetUrl = loadUrlFromConfig();
        webView.loadUrl(targetUrl);
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onVideoClicked() {
            runOnUiThread(() -> {
                // Forca orientacao horizontal e remove 100% da barra de status
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                hideSystemBars();
                getWindow().getDecorView().post(MainActivity.this::hideSystemBars);
                getWindow().getDecorView().postDelayed(MainActivity.this::hideSystemBars, 300);
            });
        }

        @JavascriptInterface
        public void onPinchGesture(boolean fill) {
            runOnUiThread(() -> {
                customViewContainer.applyZoom(fill);
            });
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Sempre que o celular estiver na horizontal, a barra de status DEVE SUMIR
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || customView != null) {
            hideSystemBars();
            getWindow().getDecorView().post(this::hideSystemBars);
            getWindow().getDecorView().postDelayed(this::hideSystemBars, 300);
        } else {
            showSystemBars();
        }
    }

    private void hideSystemBars() {
        // 1. Mata a barra de status no WindowManager
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 2. Oculta via WindowInsetsControllerCompat (Android 10 ao 15)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.statusBars());
            controller.hide(WindowInsetsCompat.Type.navigationBars());
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // 3. Flags completas de imersao para a raiz da visualizacao
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LOW_PROFILE
        );
    }

    private void showSystemBars() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && (isLandscape() || customView != null)) {
            hideSystemBars();
            getWindow().getDecorView().post(this::hideSystemBars);
        }
    }

    private String loadUrlFromConfig() {
        try {
            InputStream is = getAssets().open("app_config.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(jsonStr);
            return obj.optString("url", "https://example.com");
        } catch (Exception e) {
            return "https://example.com";
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectCustomCss();
            injectCustomJs();
        }
    }

    private void injectCustomCss() {
        try {
            InputStream is = getAssets().open("custom.css");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String css = new String(buffer, StandardCharsets.UTF_8);
            String encoded = Base64.encodeToString(css.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            String js = "javascript:(function() {" +
                    "var parent = document.head || document.documentElement;" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()";
            webView.evaluateJavascript(js, null);
        } catch (Exception ignored) {}
    }

    private void injectCustomJs() {
        try {
            InputStream is = getAssets().open("custom.js");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String js = new String(buffer, StandardCharsets.UTF_8);
            webView.evaluateJavascript(js, null);
        } catch (Exception ignored) {}
    }

    private class FullscreenWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                onHideCustomView();
                return;
            }

            customView = view;
            customViewCallback = callback;

            // Configura o elemento de video no PinchFrameLayout
            customViewContainer.setTargetChild(customView);

            customViewContainer.addView(customView);
            customViewContainer.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);

            // Esconde 100% da barra de status e barra de gestos
            hideSystemBars();
            getWindow().getDecorView().post(MainActivity.this::hideSystemBars);
            getWindow().getDecorView().postDelayed(MainActivity.this::hideSystemBars, 250);

            // Forca orientacao horizontal
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            if (customView == null) return;

            customViewContainer.resetScale();
            customViewContainer.removeView(customView);
            customViewContainer.setTargetChild(null);
            customViewContainer.setVisibility(View.GONE);
            customView = null;

            webView.setVisibility(View.VISIBLE);

            if (!isLandscape()) {
                showSystemBars();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
                customViewCallback = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            if (webView.getWebChromeClient() != null) {
                ((FullscreenWebChromeClient) webView.getWebChromeClient()).onHideCustomView();
            }
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
