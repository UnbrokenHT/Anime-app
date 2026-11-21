import webview
import json
import os
import sys

# Habilita suporte a tela cheia nativa (HTML5 / Video / F11) no WebView2
try:
    from webview.platforms import edgechromium
    _orig_on_webview_ready = edgechromium.EdgeChrome.on_webview_ready

    def _patched_on_webview_ready(self, sender, args):
        _orig_on_webview_ready(self, sender, args)
        if args.IsSuccess:
            def on_fullscreen_changed(s, a):
                try:
                    is_fullscreen = bool(s.ContainsFullScreenElement)
                    if is_fullscreen != self.form.is_fullscreen:
                        self.form.toggle_fullscreen()
                except Exception as e:
                    print(f"Erro ao alternar tela cheia: {e}")
            sender.CoreWebView2.ContainsFullScreenElementChanged += on_fullscreen_changed

    edgechromium.EdgeChrome.on_webview_ready = _patched_on_webview_ready
except Exception as e:
    pass

CONFIG_FILE = "config.json"

DEFAULT_CONFIG = {
    "title": "Web App",
    "url": "https://example.com",
    "width": 1280,
    "height": 720
}

def get_base_path():
    """Retorna o caminho base correto mesmo se empacotado como executável."""
    if getattr(sys, 'frozen', False):
        return os.path.dirname(sys.executable)
    return os.path.dirname(os.path.abspath(__file__))

def load_config():
    config_path = os.path.join(get_base_path(), CONFIG_FILE)
    if not os.path.exists(config_path):
        # Tenta buscar na pasta pai caso o executavel esteja em dist/
        parent_config = os.path.join(os.path.dirname(get_base_path()), CONFIG_FILE)
        if os.path.exists(parent_config):
            config_path = parent_config
        else:
            with open(config_path, "w", encoding="utf-8") as f:
                json.dump(DEFAULT_CONFIG, f, indent=4)
            return DEFAULT_CONFIG
    try:
        with open(config_path, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"Erro ao ler {CONFIG_FILE}, usando configurações padrão: {e}")
        return DEFAULT_CONFIG

def get_icon_path():
    """Busca o arquivo de icone na pasta do app ou na pasta pai."""
    for folder in [get_base_path(), os.path.dirname(get_base_path())]:
        for name in ["icon.ico", "icon.png"]:
            path = os.path.join(folder, name)
            if os.path.exists(path):
                return path
    return None

def get_file_content(filename):
    """Busca o conteudo de um arquivo na pasta do app ou na pasta pai."""
    for folder in [get_base_path(), os.path.dirname(get_base_path())]:
        path = os.path.join(folder, filename)
        if os.path.exists(path):
            try:
                with open(path, "r", encoding="utf-8") as f:
                    return f.read()
            except Exception as e:
                print(f"Erro ao ler {filename}: {e}")
    return None

def main():
    config = load_config()

    target_url = config.get("url", "https://example.com")
    window_title = config.get("title", "Web App")
    window_width = config.get("width", 1280)
    window_height = config.get("height", 720)
    enable_debug = config.get("debug", False)

    # Cria a janela sem bordas/barras de navegação do navegador
    window = webview.create_window(
        title=window_title,
        url=target_url,
        width=window_width,
        height=window_height,
        resizable=True,
        fullscreen=False,
        confirm_close=False
    )

    def on_page_loaded():
        # Injeta CSS personalizado (ex: remover caixas brancas de anuncios)
        css = get_file_content("custom.css")
        if css:
            window.load_css(css)

        # Injeta JS personalizado
        js = get_file_content("custom.js")
        if js:
            window.run_js(js)

    window.events.loaded += on_page_loaded

    icon_path = get_icon_path()

    # Inicia a WebView (no Windows utiliza WebView2 nativa do Edge)
    webview.start(private_mode=False, icon=icon_path, debug=enable_debug)

if __name__ == "__main__":
    main()
