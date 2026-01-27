/* ==========================================
   CONTROL DE INACTIVIDAD (AUTO-LOGOUT)
   ========================================== */

// Configuración: Tiempo en milisegundos
// 20 minutos = 1200000 ms
// El JS saltará a los 20 min, ganándole al servidor (que espera 25 min)
const TIEMPO_LIMITE = 20 * 60 * 1000;

let temporizador;

function reiniciarConteo() {
    // Si el usuario mueve el mouse o teclea, reiniciamos la cuenta regresiva
    clearTimeout(temporizador);
    temporizador = setTimeout(cerrarSesionPorInactividad, TIEMPO_LIMITE);
}

function cerrarSesionPorInactividad() {
    console.log("Inactividad detectada. Cerrando sesión...");

    // --- CORRECCIÓN FINAL ---
    // Usamos ruta relativa. Esto funciona automáticamente en:
    // - localhost:8080
    // - localhost (puerto 80)
    // - 192.168.x.x
    // - http://gestion-tickets
    window.location.href = '/login?logout=true&motivo=inactividad';
}

// Eventos que detectan actividad del usuario
window.onload = reiniciarConteo;
document.onmousemove = reiniciarConteo;
document.onkeypress = reiniciarConteo;
document.onclick = reiniciarConteo;
document.onscroll = reiniciarConteo;