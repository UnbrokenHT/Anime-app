(function() {
    // Garante que todos os iframes tenham permissao de tela cheia
    function ensureFullscreenAllowed() {
        document.querySelectorAll('iframe').forEach(iframe => {
            if (!iframe.hasAttribute('allowfullscreen')) {
                iframe.setAttribute('allowfullscreen', 'true');
            }
            if (!iframe.hasAttribute('webkitallowfullscreen')) {
                iframe.setAttribute('webkitallowfullscreen', 'true');
            }
            if (!iframe.hasAttribute('mozallowfullscreen')) {
                iframe.setAttribute('mozallowfullscreen', 'true');
            }
        });
    }

    let isHorizontalTriggered = false;

    // Notifica o app para esconder a barra do topo quando entrar em tela cheia no player
    function onFullscreenChange() {
        const isFs = !!(document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement);
        if (isFs && window.AndroidBridge && window.AndroidBridge.onVideoClicked) {
            window.AndroidBridge.onVideoClicked();
        }
    }
    document.addEventListener('fullscreenchange', onFullscreenChange);
    document.addEventListener('webkitfullscreenchange', onFullscreenChange);
    document.addEventListener('mozfullscreenchange', onFullscreenChange);

    // Escuta cliques diretos no botao de tela cheia de qualquer player
    document.addEventListener('click', function(e) {
        const target = e.target;
        if (target && target.closest && target.closest('[class*="fullscreen"], [id*="fullscreen"], .vjs-fullscreen-control, .jw-icon-fullscreen')) {
            if (window.AndroidBridge && window.AndroidBridge.onVideoClicked) {
                window.AndroidBridge.onVideoClicked();
            }
        }
    }, true);

    // Dispara a orientacao horizontal apenas uma vez ao iniciar a reproducao
    function triggerLandscapeOnPlay() {
        document.querySelectorAll('video').forEach(video => {
            if (!video.dataset.hooked) {
                video.dataset.hooked = 'true';

                video.addEventListener('play', function() {
                    if (!isHorizontalTriggered) {
                        isHorizontalTriggered = true;
                        if (window.AndroidBridge && window.AndroidBridge.onVideoClicked) {
                            window.AndroidBridge.onVideoClicked();
                        }
                    }
                });

                video.addEventListener('ended', function() {
                    isHorizontalTriggered = false;
                });
            }
        });
    }

    // Funcao global para aplicar o modo de enquadramento do video (Netflix Style)
    // 'cover' = Preenche a tela inteira (elimina barras pretas em 21:9)
    // 'contain' = Proporcao original (16:9)
    // As legendas permanecem ancoradas na posicao normal da tela do celular
    window.setVideoFitMode = function(mode) {
        document.querySelectorAll('video').forEach(v => {
            v.style.setProperty('object-fit', mode, 'important');
            v.style.setProperty('width', '100%', 'important');
            v.style.setProperty('height', '100%', 'important');
        });

        document.querySelectorAll('iframe').forEach(frame => {
            try {
                const doc = frame.contentDocument || frame.contentWindow.document;
                if (doc) {
                    doc.querySelectorAll('video').forEach(v => {
                        v.style.setProperty('object-fit', mode, 'important');
                        v.style.setProperty('width', '100%', 'important');
                        v.style.setProperty('height', '100%', 'important');
                    });
                }
            } catch (e) {}
        });
    };

    // Deteccao de gesto de pinça no navegador para alternar o modo pre-programado
    let touchStartDist = 0;
    let isPinching = false;

    function getDistance(e) {
        if (e.touches.length < 2) return 0;
        return Math.hypot(
            e.touches[0].clientX - e.touches[1].clientX,
            e.touches[0].clientY - e.touches[1].clientY
        );
    }

    document.addEventListener('touchstart', function(e) {
        if (e.touches.length === 2) {
            touchStartDist = getDistance(e);
            isPinching = true;
        }
    }, { passive: true });

    document.addEventListener('touchmove', function(e) {
        if (isPinching && e.touches.length === 2) {
            const currentDist = getDistance(e);
            const delta = currentDist - touchStartDist;

            // Abrir os dedos -> Preencher a tela (Zoom In estático 21:9)
            if (delta > 40) {
                window.setVideoFitMode('cover');
                if (window.AndroidBridge && window.AndroidBridge.onPinchGesture) {
                    window.AndroidBridge.onPinchGesture(true);
                }
                isPinching = false;
            }
            // Fechar os dedos -> Voltar ao original (Zoom Out 16:9)
            else if (delta < -40) {
                window.setVideoFitMode('contain');
                if (window.AndroidBridge && window.AndroidBridge.onPinchGesture) {
                    window.AndroidBridge.onPinchGesture(false);
                }
                isPinching = false;
            }
        }
    }, { passive: true });

    document.addEventListener('touchend', function(e) {
        if (e.touches.length < 2) {
            isPinching = false;
        }
    }, { passive: true });

    // Limpeza de anuncios sem interferir em nenhum componente do player
    function cleanAdElements() {
        ensureFullscreenAllowed();
        triggerLandscapeOnPlay();

        const iframes = document.querySelectorAll('iframe');
        iframes.forEach(iframe => {
            const src = (iframe.getAttribute('src') || '').toLowerCase();
            if (!src || src === 'about:blank' || src.includes('doubleclick') || src.includes('popunder')) {
                iframe.style.setProperty('display', 'none', 'important');
                const parent = iframe.parentElement;
                if (parent && parent.tagName === 'DIV' && parent.children.length === 1) {
                    parent.style.setProperty('display', 'none', 'important');
                }
            }
        });

        const adSelectors = [
            '[id*="ad-"]', '[id*="ad_"]', '[id*="ads_"]',
            '[class*="ad-"]', '[class*="ad_"]', '[class*="ads_"]',
            '[class*="banner"]', '[id*="banner"]',
            '.ad-container', '.adsbox'
        ];
        
        try {
            document.querySelectorAll(adSelectors.join(',')).forEach(el => {
                if (el.closest('video, [class*="player"], [id*="player"], .jwplayer, .video-js, iframe, .vjs-control-bar')) {
                    return;
                }
                const tag = el.tagName.toLowerCase();
                if (tag !== 'header' && tag !== 'nav' && tag !== 'main' && tag !== 'section') {
                    el.style.setProperty('display', 'none', 'important');
                }
            });
        } catch (e) {}
    }

    cleanAdElements();
    setInterval(cleanAdElements, 1200);

    const observer = new MutationObserver(cleanAdElements);
    observer.observe(document.body || document.documentElement, {
        childList: true,
        subtree: true
    });
})();
