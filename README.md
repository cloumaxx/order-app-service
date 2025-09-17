# Order App Service

Microservicio de órdenes desarrollado en **Spring Boot 3 + Java 21**.  
Expone endpoints REST para gestionar órdenes y se integra en un pipeline CI/CD con Jenkins y ArgoCD.

---

## 🚀 Endpoints principales

Base path: `/api/orders`

- `GET /api/orders` → lista todas las órdenes
- `GET /api/orders/{id}` → obtiene una orden por id
- `POST /api/orders` → crea una nueva orden
- `GET /actuator/health` → health check (readiness / liveness)

---

## ⚙️ Requisitos locales

- Java 21
- Gradle 8+
- Docker Desktop (para construir imágenes)

---

## 🐳 Construcción y prueba local

```bash
# Construir jar
./gradlew clean build

# Construir imagen Docker
docker build -t eduardoalvear/order-app:dev .

# Ejecutar local
docker run -p 8080:8080 eduardoalvear/order-app:dev
