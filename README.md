# LETMC Sound — Android (Java nativo) — v2.0 COMPLETO

## Configuración rápida

### 1. Reemplazar Anon Key
`app/src/main/java/com/letmc/sound/data/api/SupabaseManager.java`
```java
public static final String SUPABASE_ANON = "TU_ANON_KEY_AQUI"; // ← reemplaza esto
```
Clave en: **Supabase → Project Settings → API → anon / public key**

### 2. Importar en Android Studio
- File → Open → carpeta LETMCSound → OK
- Esperar Gradle sync (~2-5 min)

### 3. Ejecutar
- Dispositivo físico o emulador API 24+
- Run → Run 'app'

## Funcionalidades implementadas ✅

| Módulo            | Estado |
|-------------------|--------|
| Login / Register  | ✅ |
| Sesión cifrada    | ✅ |
| Lista de Beats    | ✅ |
| Reproductor Audio | ✅ (ExoPlayer) |
| Compra de Beat    | ✅ |
| Contrato PDF      | ✅ (Edge Function generar-contrato) |
| Lista de Letras   | ✅ |
| Detalle de Letra  | ✅ |
| Film Makers       | ✅ |
| Diseño Gráfico    | ✅ |
| Chat Realtime     | ✅ (WebSocket Supabase) |
| Enviar Ofertas    | ✅ |
| Editar Perfil     | ✅ |
| Subir Avatar      | ✅ (Supabase Storage) |
| Publicar Beat     | ✅ (Storage + BD) |
| Notificaciones    | ✅ |
| Cerrar sesión     | ✅ |

## Stack técnico
- **UI**: Java + ViewBinding + Navigation Component
- **HTTP**: Retrofit2 + OkHttp
- **Realtime**: OkHttp WebSocket → Supabase Realtime
- **Audio**: ExoPlayer (androidx.media3)
- **Imágenes**: Glide
- **Storage**: Supabase Storage REST API
- **Contratos**: Supabase Edge Function (generar-contrato)
- **Seguridad**: EncryptedSharedPreferences
