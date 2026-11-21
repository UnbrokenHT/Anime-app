; Inno Setup Script para criacao do instalador
#define MyAppName "Anime App"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Custom App"
#define MyAppExeName "CustomApp.exe"

[Setup]
AppId={{D8A1C940-7A9B-4B6C-81E9-5F109A52C001}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={localappdata}\Programs\AnimeApp
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir=installer_output
OutputBaseFilename=AnimeApp_Setup
SetupIconFile=icon.ico
UninstallDisplayIcon={app}\{#MyAppExeName}
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "dist\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\config.json"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\custom.css"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\custom.js"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\icon.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "dist\icon.png"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
