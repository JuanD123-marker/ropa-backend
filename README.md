# Backend — App de venta de ropa

Esqueleto en Spring Boot con el modelo de datos y la autenticación separada
de administrador/comprador que quedaron definidos en el concepto. No incluye
todavía la subida real de fotos (fondo profesional con `rembg`) ni el
almacenamiento en Google Drive/Cloudflare R2 — eso es el siguiente paso.

## Requisitos

- Java 21
- Maven (o el wrapper `./mvnw` si lo agregas con `mvn -N wrapper:wrapper`)
- Una base de datos PostgreSQL (Aiven, Supabase, o local)

## Configuración

Antes de correrlo, edita `src/main/resources/application.yml` con los datos
reales de tu base de datos, o expórtalos como variables de entorno:

```
export APP_JWT_SECRET="una-cadena-larga-y-aleatoria"
```

`ddl-auto: update` deja que Hibernate cree las tablas automáticamente en
desarrollo a partir de las entidades en `model/`. Para producción, lo
correcto es migrar a Flyway o Liquibase y poner `ddl-auto: validate`.

## Correr el proyecto

```
mvn spring-boot:run
```

Debería quedar arriba en `http://localhost:8080`. Prueba con:

```
curl http://localhost:8080/api/health
```

## Cómo crear el primer administrador

No hay un endpoint público para crear la tienda ni el primer administrador
(a propósito, para no exponer "crear tienda" abiertamente). Por ahora, esa
primera fila se inserta directo en la base de datos:

```sql
insert into tienda (nombre) values ('Mi tienda');

-- La contraseña debe ir como hash BCrypt, no en texto plano.
-- Puedes generar uno rápido desde una consola Java/Spring, o con
-- cualquier generador de hashes BCrypt online para probar en desarrollo.
insert into administrador (tienda_id, nombre, correo, password_hash)
values (1, 'Juan', 'admin@ejemplo.com', '<hash-bcrypt-aqui>');
```

## Estructura

```
model/            -> Entidades JPA (Tienda, Administrador, Comprador, Prenda, ...)
model/enums/       -> Talla, EstadoPedido, TipoEntrega, CanalVenta, EstadoPrenda, TipoUsuario
repository/       -> Interfaces Spring Data JPA
security/         -> JWT (emisión/validación) y el filtro que separa admin/comprador
service/          -> Lógica de negocio (auth, prendas, pedidos, finanzas)
controller/       -> Endpoints REST
dto/              -> Lo que entra y sale por la API (nunca se exponen las entidades como request)
```

## Endpoints principales

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/admin/auth/login` | público |
| POST | `/api/comprador/auth/registro` | público (opcional) |
| POST | `/api/comprador/auth/login` | público |
| GET  | `/api/catalogo/prendas?tiendaId=1` | público |
| POST | `/api/pedidos?tiendaId=1` | público (invitado o comprador) |
| POST | `/api/admin/prendas` | administrador |
| GET  | `/api/admin/prendas` | administrador |
| POST | `/api/admin/ventas-fisicas` | administrador (registrar venta del local) |
| GET  | `/api/admin/pedidos` | administrador |
| PATCH | `/api/admin/pedidos/{id}/estado` | administrador |
| GET  | `/api/admin/finanzas/resumen?desde=...&hasta=...` | administrador |
| GET  | `/api/comprador/pedidos` | comprador registrado |

## Nota sobre verificación

Este proyecto no se pudo compilar dentro de esta sesión porque el entorno no
tiene salida a Maven Central (política de red del entorno, no relacionada con
el código). El código se escribió y se revisó a mano con cuidado, pero
antes de construir sobre él, corre `mvn compile` en tu máquina para
confirmar que compila limpio — y avísame si sale algún error para
corregirlo.

## Próximos pasos sugeridos

1. Confirmar que compila y correr contra una base de datos real (Aiven/Supabase).
2. Agregar el endpoint de subida de fotos: recibir la imagen, mandarla a
   `rembg`, guardar el resultado en Google Drive y devolver la URL para
   usarla en `PrendaRequest.fotosUrl`.
3. Endpoint para editar/pausar una prenda existente y ajustar stock manualmente.
4. Conectar el frontend en React contra estos endpoints.
