# XuperIPTV — Reproductor M3U para Google TV

App IPTV con interfaz dark estilo XuperTV, optimizada para Google TV Streamer y control remoto.

---

## Características

- 📺 Reproducción de streams IPTV vía listas M3U/M3U8 (HTTP/HTTPS)
- 🎮 Navegación 100% con control remoto (D-pad, botón atrás)
- 📂 Múltiples listas M3U guardadas localmente
- ⭐ Canales favoritos persistentes
- 🔍 Búsqueda en tiempo real por nombre o grupo
- 🗂️ Agrupación por categorías (colapsables)
- ▶️ Reproductor HLS/HTTP con ExoPlayer (Media3)
- 🌑 Tema dark oscuro idéntico al estilo XuperTV

---

## Pasos para compilar

### Requisitos
- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 34

### 1. Abrir el proyecto
```
File → Open → seleccionar la carpeta XuperIPTV
```

### 2. Sincronizar Gradle
Android Studio lo hará automáticamente al abrir.  
Si no: **File → Sync Project with Gradle Files**

### 3. Compilar APK de debug
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
El APK queda en:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Instalar en el Google TV Streamer

### Opción A — ADB por Wi-Fi (recomendado)
```bash
# 1. En el Google TV: Ajustes → Sistema → Acerca del dispositivo → 
#    Presionar "Build" 7 veces para activar opciones de desarrollador
# 2. Ajustes → Opciones de desarrollador → Depuración por ADB → Activar
# 3. Anotar la IP del dispositivo (Ajustes → Red)

# Desde tu PC:
adb connect 192.168.X.X:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Opción B — Pendrive / Sideload con Downloader app
1. Subir el APK a un servidor web o Google Drive
2. Instalar **Downloader** desde Google Play en el TV
3. Pegar la URL del APK y descargar/instalar

---

## Agregar listas M3U

1. Abrir la app → botón **+** en la barra lateral (o presionar MENU en el control)
2. Escribir un nombre y la URL de tu lista `.m3u` o `.m3u8`
3. La app descarga y parsea la lista automáticamente

**Ejemplo de URL local** (si tenés el archivo en el storage del dispositivo):
```
/storage/emulated/0/Download/mi-lista.m3u
```

---

## Estructura del proyecto

```
XuperIPTV/
├── app/src/main/
│   ├── java/com/xuperiptv/
│   │   ├── data/
│   │   │   └── Models.kt          ← Channel, Playlist, ChannelGroup
│   │   ├── utils/
│   │   │   ├── M3UParser.kt       ← Parser M3U completo
│   │   │   └── PreferencesManager.kt ← Persistencia
│   │   └── ui/
│   │       ├── home/
│   │       │   ├── MainActivity.kt
│   │       │   ├── MainViewModel.kt
│   │       │   ├── ChannelAdapter.kt
│   │       │   └── GroupAdapter.kt
│   │       ├── player/
│   │       │   └── PlayerActivity.kt  ← ExoPlayer + HLS
│   │       └── settings/
│   │           ├── SettingsActivity.kt
│   │           └── PlaylistSettingsAdapter.kt
│   └── res/
│       ├── layout/     ← Layouts XML
│       ├── drawable/   ← Íconos vectoriales
│       ├── values/     ← Colores, strings, temas
│       └── color/      ← Selectores de color
└── build.gradle
```

---

## Tecnologías

| Librería | Uso |
|---|---|
| ExoPlayer (Media3) | Reproducción HLS/HTTP |
| OkHttp | Descarga de listas M3U remotas |
| Glide | Logos de canales |
| Kotlin Coroutines | Carga asíncrona |
| AndroidX Leanback | Optimización TV |

---

## Notas

- Para listas con autenticación, agregar usuario/contraseña directamente en la URL:  
  `http://usuario:contraseña@servidor.com:puerto/lista.m3u`
- El reproductor soporta HLS (`.m3u8`), HTTP directo, y la mayoría de streams IPTV
- Los canales favoritos y las listas se guardan en `SharedPreferences` del dispositivo
