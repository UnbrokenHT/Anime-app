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

    // Injeta o botao Voltar no topo esquerdo
    function injectBackButton() {
        if (document.getElementById('custom-app-back-btn')) return;
        if (!document.body) return;

        const btn = document.createElement('button');
        btn.id = 'custom-app-back-btn';
        btn.title = 'Voltar (Alt + Esquerda, Backspace ou Botao Lateral do Mouse)';
        btn.setAttribute('aria-label', 'Voltar');
        btn.innerHTML = '<svg viewBox="0 0 24 24"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>';

        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = '/';
            }
        });

        document.body.appendChild(btn);
    }

    function cleanAdElements() {
        ensureFullscreenAllowed();
        injectBackButton();

        // Remove iframes vazios (quadrados brancos)
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

        // Oculta containers de banners
        const adSelectors = [
            '[id*="ad-"]', '[id*="ad_"]', '[id*="ads_"]',
            '[class*="ad-"]', '[class*="ad_"]', '[class*="ads_"]',
            '[class*="banner"]', '[id*="banner"]',
            '.ad-container', '.adsbox'
        ];
        
        try {
            document.querySelectorAll(adSelectors.join(',')).forEach(el => {
                const tag = el.tagName.toLowerCase();
                if (tag !== 'header' && tag !== 'nav' && tag !== 'main' && tag !== 'section') {
                    el.style.setProperty('display', 'none', 'important');
                }
            });
        } catch (e) {}
    }

    // Atalhos de teclado para Voltar: Alt + Esquerda ou Backspace (fora de campos de texto)
    window.addEventListener('keydown', function(e) {
        if (e.key === 'F11') {
            e.preventDefault();
            if (document.fullscreenElement || document.webkitFullscreenElement) {
                if (document.exitFullscreen) document.exitFullscreen();
                else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
            } else {
                const el = document.documentElement;
                if (el.requestFullscreen) el.requestFullscreen();
                else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen();
            }
        } else if (
            (e.altKey && e.key === 'ArrowLeft') ||
            (e.key === 'Backspace' && !['INPUT', 'TEXTAREA'].includes((e.target || {}).tagName || '') && !e.target.isContentEditable)
        ) {
            if (window.history.length > 1) {
                window.history.back();
            }
        }
    });

    // Suporte ao botao de voltar lateral do mouse
    window.addEventListener('mouseup', function(e) {
        if (e.button === 3) {
            if (window.history.length > 1) {
                window.history.back();
            }
        }
    });

    // Gerencia visibilidade do botao em tela cheia
    function onFullscreenChange() {
        const isFs = !!(document.fullscreenElement || document.webkitFullscreenElement);
        if (document.body) {
            document.body.classList.toggle('in-fullscreen', isFs);
        }
    }
    document.addEventListener('fullscreenchange', onFullscreenChange);
    document.addEventListener('webkitfullscreenchange', onFullscreenChange);

    cleanAdElements();
    setInterval(cleanAdElements, 1200);

    const observer = new MutationObserver(cleanAdElements);
    observer.observe(document.body || document.documentElement, {
        childList: true,
        subtree: true
    });
})();
