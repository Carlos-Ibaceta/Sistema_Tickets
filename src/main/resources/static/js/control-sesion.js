/* ==========================================
   CONTROL DE INACTIVIDAD (AUTO-LOGOUT)
   ========================================== */

// Configuración: Tiempo en milisegundos
// 30 minutos = 1800000 ms
// Para pruebas ponle 305000 (5 min y 5 seg)
const TIEMPO_LIMITE = 20 * 60 * 1000;

let temporizador;

function reiniciarConteo() {
    // Si el usuario mueve el mouse o teclea, reiniciamos la cuenta regresiva
    clearTimeout(temporizador);
    temporizador = setTimeout(cerrarSesionPorInactividad, TIEMPO_LIMITE);

    // Opcional: Para depurar en consola y ver que funciona
    // console.log("Actividad detectada. Temporizador reiniciado.");
}

function cerrarSesionPorInactividad() {
    // Redirigir al login forzando el logout
    window.location.href = '/login?logout=true&motivo=inactividad';
}

// Eventos que detectan actividad del usuario
window.onload = reiniciarConteo;
document.onmousemove = reiniciarConteo;
document.onkeypress = reiniciarConteo;
document.onclick = reiniciarConteo;
document.onscroll = reiniciarConteo;