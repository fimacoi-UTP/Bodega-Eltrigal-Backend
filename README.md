# Bodega El Trigal — Backend

API REST para el sistema de gestión de **Bodega El Trigal**, una bodega de barrio en Piura, Perú.
Expone la lógica de catálogo, inventario, ventas de mostrador, pedidos web y seguridad para
ser consumida por el frontend (React) de forma desacoplada.

## Stack

- **Java 21** (Temurin / Adoptium)
- **Spring Boot 3.4.1** — Web, Data JPA, Validation, Security
- **Hibernate / JPA** sobre **MySQL**
- **JWT** (jjwt 0.12.6) para autenticación stateless
- **Lombok** para reducir boilerplate
- **springdoc-openapi (Swagger UI)** para documentar y probar la API
- **Maven** como gestor de dependencias

## Arquitectura

- Base de datos relacional normalizada hasta **3FN** (13 entidades + 2 tablas puente).
- API REST desacoplada (el frontend consume vía `fetch`).
- Capas: `entity` → `repository` → `service` → `controller`, con DTOs (`Request`/`Response`)
  para no exponer las entidades.
- Operaciones de stock protegidas con `@Transactional` (reserva/descuento atómico + Kardex).
- Patrones de diseño: Repository, Strategy (pagos), State (estados de pedido), Decorator (promociones).

## Requisitos previos

- JDK 21
- MySQL 8+ en ejecución
- Maven (o el wrapper `./mvnw` incluido)

## Configuración

### 1. Crear la base de datos

Ejecuta el script SQL del modelo (3FN) en MySQL para crear la base `eltrigal_db` con sus tablas.
Luego siembra las categorías, marcas y roles base.

### 2. Variables de entorno (NO se suben al repo)

La contraseña de MySQL y el secreto de JWT se leen de variables de entorno, nunca del código:

| Variable       | Descripción                                   |
|----------------|-----------------------------------------------|
| `DB_PASSWORD`  | Contraseña del usuario `root` de MySQL        |
| `JWT_SECRET`   | Clave secreta para firmar los JWT (mín. 32 caracteres) |

En Windows (PowerShell), antes de arrancar:

```powershell
$env:DB_PASSWORD="tu_password_mysql"
$env:JWT_SECRET="tu_clave_secreta_larga_de_al_menos_32_caracteres"
```

> El `application.properties` usa `${DB_PASSWORD}` y `${JWT_SECRET}`; el esquema se valida
> con `spring.jpa.hibernate.ddl-auto=validate` (la base la manda el script SQL, no Hibernate).

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`.

## Documentación interactiva (Swagger)

Con el backend corriendo, abre en el navegador:

http://localhost:8080/swagger-ui.html

## Autenticación

La API es **stateless con JWT**. Casi todos los endpoints requieren un token.

1. Haz login en `POST /api/auth/login` y copia el `token` de la respuesta.
2. En Swagger, usa el botón **Authorize** y pega el token (sin "Bearer").
   En otros clientes, envía la cabecera: `Authorization: Bearer <token>`.

### Usuarios de prueba (se siembran al arrancar)

| Rol      | Email                 | Contraseña  |
|----------|-----------------------|-------------|
| Admin    | admin@eltrigal.pe     | admin123    |
| Cajero   | cajero@eltrigal.pe    | cajero123   |
| Cliente  | cliente@eltrigal.pe   | cliente123  |

## Endpoints principales

### Autenticación
| Método | Ruta              | Descripción            |
|--------|-------------------|------------------------|
| POST   | /api/auth/login   | Inicia sesión, da JWT  |

### Productos
| Método | Ruta                   | Descripción              |
|--------|------------------------|--------------------------|
| GET    | /api/productos         | Lista productos activos  |
| GET    | /api/productos/{id}    | Detalle de un producto   |
| POST   | /api/productos         | Crea un producto         |
| PUT    | /api/productos/{id}    | Actualiza un producto    |
| DELETE | /api/productos/{id}    | Baja lógica (soft delete)|

### Inventario (lotes y Kardex)
| Método | Ruta                               | Descripción                         |
|--------|------------------------------------|-------------------------------------|
| POST   | /api/lotes                         | Ingreso de lote (sube stock)        |
| GET    | /api/lotes/producto/{productoId}   | Lotes de un producto                |
| GET    | /api/lotes/por-vencer?dias=30      | Lotes próximos a vencer             |
| POST   | /api/movimientos                   | Ajuste / merma de stock             |
| GET    | /api/movimientos/producto/{id}     | Kardex (historial) de un producto   |

### Ventas (POS)
| Método | Ruta                       | Descripción                     |
|--------|----------------------------|---------------------------------|
| POST   | /api/ventas                | Registra venta (descuenta stock)|
| GET    | /api/ventas/{id}           | Detalle de una venta            |
| GET    | /api/ventas/hoy            | Ventas del día                  |
| PUT    | /api/ventas/{id}/anular    | Anula venta (repone stock)      |

### Pedidos web (patrón State)
| Método | Ruta                               | Descripción                          |
|--------|------------------------------------|--------------------------------------|
| POST   | /api/pedidos                       | Crea pedido (reserva stock)          |
| GET    | /api/pedidos/{id}                  | Detalle de un pedido                 |
| GET    | /api/pedidos?estado=PENDIENTE      | Lista pedidos (filtro por estado)    |
| PUT    | /api/pedidos/{id}/estado?nuevoEstado=CONFIRMADO | Cambia estado (transición validada) |

## Seguridad

- Contraseñas almacenadas con **BCrypt**.
- `DB_PASSWORD` y `JWT_SECRET` **nunca** se versionan: van como variables de entorno.
- Rutas protegidas por JWT; `/api/auth/**` y Swagger son públicas.

## Proyecto académico

Desarrollado para el curso **Herramientas de Desarrollo UTP - 2026** — Bodega El Trigal.