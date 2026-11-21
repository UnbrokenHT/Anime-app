package com.custom.animeapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PinchFrameLayout extends FrameLayout {

    private ScaleGestureDetector scaleGestureDetector;
    private View targetChild;
    private boolean isZoomedToFill = false;
    private Context context;
    private OnZoomChangeListener zoomChangeListener;

    public interface OnZoomChangeListener {
        void onZoomChanged(boolean isZoomed);
    }

    public void setOnZoomChangeListener(OnZoomChangeListener listener) {
        this.zoomChangeListener = listener;
    }

    public PinchFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public PinchFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PinchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();

                // Gesto de pinça abrindo os dedos -> Zoom In pré-programado para preencher a tela (Netflix)
                if (factor > 1.08f && !isZoomedToFill) {
                    applyZoom(true);
                    return true;
                }
                // Gesto de pinça fechando os dedos -> Zoom Out para tamanho original (16:9)
                else if (factor < 0.92f && isZoomedToFill) {
                    applyZoom(false);
                    return true;
                }
                return false;
            }
        });
    }

    // Calcula a proporcao exata da tela do celular especifico (ex: 19.5:9, 20:9, 21:9) em relacao ao video 16:9
    private float calculateFillScale() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float width = Math.max(dm.widthPixels, dm.heightPixels);
        float height = Math.min(dm.widthPixels, dm.heightPixels);
        float screenRatio = width / height;
        float videoRatio = 16.0f / 9.0f; // 1.777f

        if (screenRatio > videoRatio) {
            return screenRatio / videoRatio;
        }
        return 1.25f;
    }

    public void applyZoom(boolean fill) {
        if (this.isZoomedToFill == fill && targetChild != null) {
            return;
        }

        this.isZoomedToFill = fill;
        float targetScale = fill ? calculateFillScale() : 1.0f;

        // Se encontrar a superficie de video nativa (SurfaceView ou TextureView),
        // amplia APENAS o video, mantendo as legendas e controles ancorados na tela do celular
        View videoSurface = findVideoSurface(targetChild);
        View viewToAnimate = (videoSurface != null) ? videoSurface : targetChild;

        if (viewToAnimate != null) {
            viewToAnimate.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .setDuration(220)
                    .start();
        }

        if (zoomChangeListener != null) {
            zoomChangeListener.onZoomChanged(fill);
        }
    }

    private View findVideoSurface(View root) {
        if (root == null) return null;
        if (root instanceof SurfaceView || root instanceof TextureView) {
            return root;
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findVideoSurface(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    public void setTargetChild(View child) {
        this.targetChild = child;
        this.isZoomedToFill = false;
        if (child != null) {
            child.setScaleX(1.0f);
            child.setScaleY(1.0f);
        }
    }

    public void resetScale() {
        this.isZoomedToFill = false;
        if (targetChild != null) {
            targetChild.setScaleX(1.0f);
            targetChild.setScaleY(1.0f);
            View surface = findVideoSurface(targetChild);
            if (surface != null) {
                surface.setScaleX(1.0f);
                surface.setScaleY(1.0f);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        scaleGestureDetector.onTouchEvent(ev);

        // Se houver 2 ou mais dedos na tela (gesto de pinça), consome o evento para evitar cliques nos botoes
        if (ev.getPointerCount() >= 2) {
            return true;
        }

        // Toque com 1 dedo vai 100% diretamente para os botoes do player
        return super.dispatchTouchEvent(ev);
    }
}
