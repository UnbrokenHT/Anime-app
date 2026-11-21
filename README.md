# ⛩️ Anime App - Desktop & Mobile Dedicated Client

Um cliente dedicado multiplataforma (**Windows Desktop** e **Android**) construído para oferecer uma experiência nativa de streaming estilo Crunchyroll/Netflix, eliminando barras de navegação tradicionais, anúncios intrusivos e proporcionando imersão total em telas ultra-wide.

---

## 🌟 Principais Recursos

### 🖥️ Versão PC (Windows Desktop)
- **Janela 100% Nativa (Sem Chrome UI):** Execução sem barras de endereço, abas ou caixas de pesquisa, utilizando o motor de alta performance **Microsoft Edge WebView2**.
- **Botão Voltar Integrado:** Botão flutuante translúcido no canto superior esquerdo com efeito *glassmorphism*, integrado ao tema escuro da aplicação.
  - Suporte a atalhos: `Alt + Seta Esquerda`, `Backspace` e botão lateral do mouse.
  - Ocultação automática quando um vídeo entra em tela cheia.
- **Tela Cheia Imersiva:** Captura o evento nativo do player e oculta bordas e a barra de tarefas do Windows (com suporte à tecla `F11`).
- **Limpeza Visual de Anúncios:** Injeção automática de CSS e JS para colapsar *iframes* vazios e remover caixas brancas residuais.
- **Instalador Oficial do Windows:** Pacote gerado com **Inno Setup** com atalho na Área de Trabalho e suporte a desinstalação.

---

### 📱 Versão Mobile (Android)
- **Auto-Horizontal Estilo Netflix:** Ao dar play ou abrir qualquer vídeo, o aplicativo rotaciona imediatamente para modo paisagem (horizontal).
- **Ocultação Absoluta da Barra de Notificações:** Barra de status (hora, bateria, Wi-Fi) e barra de navegação/gestos são completamente ocultadas durante a exibição.
- **Suporte a Telas 21:9 & Notch:** Configurado com `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` para preencher telas ultra-wide de ponta a ponta sem cortes pretos na câmera.
- **Gesto de Pinça (Pinch-to-Zoom):**
  - **Afastar os dedos:** Alterna para o modo *Preencher Tela* (`object-fit: cover`), eliminando barras pretas laterais em smartphones 19.5:9, 20:9 e 21:9.
  - **Juntar os dedos:** Retorna ao modo *Original* (`object-fit: contain` em 16:9).
  - **Legendas Estáveis:** As legendas permanecem perfeitamente ancoradas no rodapé da carcaça do celular sem sofrer distorção.
- **Navegação com Gesto/Botão Voltar:** O botão físico/virtual de voltar do Android navega pelo histórico de páginas sem fechar o aplicativo.

---

## 🛠️ Tecnologias Utilizadas

| Plataforma | Tecnologias |
| :--- | :--- |
| **Windows Desktop** | Python 3, `pywebview` (Microsoft Edge WebView2), PyInstaller, Inno Setup |
| **Android** | Java, Android SDK 34, `WebView` + `WebChromeClient`, AndroidX, Gradle |
| **Frontend/Injeção** | CSS3 (Modern Glassmorphism & Flexbox), Vanilla JavaScript (DOM MutationObserver) |

---

## 📁 Estrutura do Repositório

```text
├── desktop/                  # Versão Windows Desktop
│   ├── app.py                # Código principal com motor WebView2
│   ├── config.json           # Configuração de URL, título e dimensões
│   ├── custom.css            # Estilização do botão voltar e limpeza visual
│   ├── custom.js             # Lógica de injeção de navegação e atalhos
│   ├── icon.ico              # Ícone do aplicativo para Windows
│   ├── requirements.txt      # Dependências Python
│   ├── run.bat               # Script para rodar diretamente via Python
│   ├── build_exe.bat         # Compilação do binário standalone (.exe)
│   ├── build_installer.bat   # Compilação do instalador (.exe)
│   └── installer.iss         # Script do instalador Inno Setup
│
└── android/                  # Versão Android (Projeto Android Studio)
    ├── app/
    │   ├── src/main/java/    # MainActivity.java & PinchFrameLayout.java
    │   ├── src/main/assets/  # Configuração, custom.css e custom.js
    │   └── src/main/res/     # Layouts, temas imersivos e ícones mipmap
    └── build.gradle          # Configuração Gradle do projeto
```

---

## 🚀 Como Compilar

### 💻 Windows
1. Certifique-se de ter o **Python 3.10+** instalado.
2. Instale as dependências:
   ```bash
   pip install -r requirements.txt
   ```
3. Para gerar o executável standalone:
   ```bash
   build_exe.bat
   ```
4. Para compilar o instalador `.exe`:
   ```bash
   build_installer.bat
   ```

### 📱 Android
1. Abra a pasta `android/` no **Android Studio**.
2. Aguarde a sincronização do Gradle.
3. No menu superior, vá em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. O arquivo `.apk` final estará localizado em:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## ⚖️ Aviso Legal / Disclaimer

Este software foi desenvolvido exclusivamente como um cliente visual para fins de estudo de integração de interfaces web em ambientes nativos (WebView2 e Android WebChromeClient). Não hospeda, armazena, transmite nem distribui nenhum arquivo de mídia protegido por direitos autorais.
