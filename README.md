# Jokoa — App Android (Reto 2)

App nativa en Java de la tienda de videojuegos y móviles Jokoa. Consume la API REST de Symfony
(`https://jokoa.duckdns.org/ws`, repositorio `juegalmi-backend`), la misma base de datos que usarán la app Ionic y la web Angular.

- Splash animado, login y registro con JWT (sesión guardada en SharedPreferences; si el token caduca vuelve al login)
- Juegos y Móviles: lista con Volley + Glide + RecyclerView, esqueleto de carga y ficha con compra y alquiler
- Pedidos: compras y alquileres, con devolución
- Tiendas: mapa osmdroid con las tiendas de la API
- Perfil editable y soporte técnico (tickets por departamento)

Requisitos: Android Studio, JDK 17, minSdk 24 / targetSdk 35. Crea `local.properties` con `sdk.dir` (no se sube a Git).
