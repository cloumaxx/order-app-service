# 📦 Order App Service

API backend **Spring Boot (Java 21)** para la aplicación **Order App**.  
Expone endpoints REST para la gestión de pedidos y se integra con PostgreSQL como base de datos.  
Se empaqueta con Gradle, se dockeriza y se publica en **Docker Hub**.  
El tag de la imagen corresponde al **commit SHA** para garantizar despliegues reproducibles en Kubernetes vía Helm + Argo CD.

---

## 📂 Estructura del proyecto

```
order-app-service/
  order-app/
    src/main/java/com/uni.sabana.order_app/
      application.service/      # Lógica de negocio (OrderService)
      domain.models/            # Entidades del dominio (Order)
      infrastructure/           # Repositorios / configuración
      web.controller/           # Controladores REST (OrderController)
      OrderAppApplication.java  # Clase principal Spring Boot
    src/main/resources/         # application.yml, resources
    src/test/                   # pruebas unitarias
    build.gradle
    settings.gradle
    Dockerfile
    Jenkinsfile
```

---

## ⚙️ Requisitos

- **Java 21**
- **Gradle 8+**
- **Docker**
- Base de datos **PostgreSQL**

---

## ▶️ Ejecución local

### Con Gradle
```bash
cd order-app
./gradlew clean bootRun
```

### Empaquetado como JAR
```bash
./gradlew clean bootJar
java -jar build/libs/*-SNAPSHOT.jar
```

### Variables de entorno necesarias
```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=orders
DB_USER=order_user
DB_PASSWORD=order_pass
```

---

## 🐳 Docker

### Build local
```bash
docker build -t docker.io/eduardoalvear/order-app:local .
```

### Run local
```bash
docker run --rm -p 8080:8080   -e DB_HOST=host.docker.internal   -e DB_NAME=orders   -e DB_USER=order_user   -e DB_PASSWORD=order_pass   docker.io/eduardoalvear/order-app:local
```

### Verificar salud
```bash
curl http://localhost:8080/actuator/health
```

---

## 🔄 CI/CD con Jenkins y Argo CD

1. Jenkins clona este repo (`order-app-service`).
2. Calcula el tag corto del commit (`git rev-parse --short HEAD`).
3. Construye la imagen Docker y la publica en Docker Hub:
   ```
   docker.io/eduardoalvear/order-app:<SHA>
   ```
4. Jenkins clona el repo `order-app-infra`, actualiza `charts/order-platform/values.yaml` con el nuevo tag y hace commit/push.
5. Argo CD detecta el cambio y sincroniza automáticamente el clúster en Minikube (namespace `my-tech`).

---

## 📡 Endpoints REST

Base path: `/api/orders`

### 1. Listar todos los pedidos
```http
GET /api/orders
```
**Response (200 OK):**
```json
[
  {
    "id": 1,
    "description": "Primer pedido",
    "amount": 100.0
  },
  {
    "id": 2,
    "description": "Segundo pedido",
    "amount": 50.0
  }
]
```

---

### 2. Obtener un pedido por ID
```http
GET /api/orders/{id}
```
**Ejemplo:**
```http
GET /api/orders/1
```
**Response (200 OK):**
```json
{
  "id": 1,
  "description": "Primer pedido",
  "amount": 100.0
}
```
Si no existe:
```json
null
```

---

### 3. Crear un nuevo pedido
```http
POST /api/orders
Content-Type: application/json
```
**Body (ejemplo):**
```json
{
  "description": "Pedido nuevo",
  "amount": 200.0
}
```
**Response (201 Created):**
```json
{
  "id": 3,
  "description": "Pedido nuevo",
  "amount": 200.0
}
```

---

## 🔍 Troubleshooting

- **No arranca** → revisar logs (`docker logs <container>`).
- **Problemas DB** → verificar reachability:
  ```bash
  nc -zv <DB_HOST> 5432
  ```
- **Fallo en build**:
  ```bash
  ./gradlew clean bootJar -x test
  ```

---
