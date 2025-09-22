# 📦 Order App Service

API backend **Spring Boot (Java 21)** para la aplicación **Order App**.  
Expone endpoints REST para la gestión de pedidos y se integra con **PostgreSQL**.  
Se construye con **Gradle**, se dockeriza y se publica en **Docker Hub**.  
El tag de la imagen usa el **commit SHA** para garantizar despliegues reproducibles con **Helm + Argo CD**.

---

## 📚 Tabla de contenido
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos](#-requisitos)
- [Configuración de entorno](#-configuración-de-entorno)
- [Ejecución local](#️-ejecución-local)
- [Docker](#-docker)
- [CI/CD con Jenkins y Argo CD](#-cicd-con-jenkins-y-argo-cd)
  - [Instalación rápida de Jenkins en Kubernetes](#instalación-rápida-de-jenkins-en-kubernetes)
  - [Configurar credenciales GitHub (SSH o HTTPS+PAT)](#configurar-credenciales-github-ssh-o-httpspat)
  - [Pipeline (flujo)](#pipeline-flujo)
- [Endpoints REST](#-endpoints-rest)
- [Healthcheck](#-healthcheck)
- [Troubleshooting](#-troubleshooting)
- [Licencia](#-licencia)

---

## 📂 Estructura del proyecto

```
order-app-service/
  order-app/
    src/main/java/com/unisabana/orderapp/
      application/service/      # Lógica de negocio (OrderService)
      domain/models/            # Entidades del dominio (Order)
      infrastructure/           # Repositorios / configuración
      web/controller/           # Controladores REST (OrderController)
      OrderAppApplication.java  # Clase principal Spring Boot
    src/main/resources/         # application.yml, recursos
    src/test/                   # Pruebas unitarias
    build.gradle
    settings.gradle
    Dockerfile
    Jenkinsfile
```

---

## ⚙️ Requisitos

- **Java 21**
- **Gradle 8+**
- **Docker 24+**
- **PostgreSQL 14+**

---

## 🔧 Configuración de entorno

Variables mínimas:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=orders
DB_USER=order_user
DB_PASSWORD=order_pass
SERVER_PORT=8080
```

Ejemplo `src/main/resources/application.yml`:

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:orders}
    username: ${DB_USER:order_user}
    password: ${DB_PASSWORD:order_pass}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate.format_sql: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

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

---

## 🐳 Docker

### Build local
```bash
docker build -t docker.io/eduardoalvear/order-app:local .
```

### Run local
```bash
docker run --rm -p 8080:8080   -e DB_HOST=host.docker.internal   -e DB_PORT=5432   -e DB_NAME=orders   -e DB_USER=order_user   -e DB_PASSWORD=order_pass   docker.io/eduardoalvear/order-app:local
```

### Verificar salud
```bash
curl http://localhost:8080/actuator/health
```

---

## 🔄 CI/CD con Jenkins y Argo CD

### Instalación rápida de Jenkins en Kubernetes

```bash
helm repo add jenkins https://charts.jenkins.io
helm repo update
kubectl create namespace jenkins || true

helm upgrade --install jenkins jenkins/jenkins   -n jenkins   --set controller.admin.username=admin   --set controller.admin.password=admin123   --set persistence.storageClass=standard   --set persistence.size=8Gi
```

Verifica el pod:
```bash
kubectl -n jenkins get pods
# Debe aparecer jenkins-0 en STATUS Running
```

Port-forward para UI:
```bash
kubectl -n jenkins port-forward svc/jenkins 8081:8080
# UI: http://localhost:8081
```

### Configurar credenciales GitHub (SSH o HTTPS+PAT)

#### Opción A) SSH (recomendado)
Generar clave dentro del pod:
```bash
kubectl -n jenkins exec -it jenkins-0 -- sh -lc   'mkdir -p /var/jenkins_home/.ssh &&    ssh-keygen -t ed25519 -C "jenkins@order-app" -f /var/jenkins_home/.ssh/id_rsa_ssh_orderapp -N "" &&    ssh-keyscan github.com >> /var/jenkins_home/.ssh/known_hosts &&    cat /var/jenkins_home/.ssh/id_rsa_ssh_orderapp.pub'
```

1. **Copia** la clave pública y agrégala en GitHub: *Settings → SSH and GPG keys → New SSH key*.  
2. Prueba conexión:
```bash
kubectl -n jenkins exec -it jenkins-0 -- sh -lc   'ssh -i /var/jenkins_home/.ssh/id_rsa_ssh_orderapp -T git@github.com'
# Deberías ver: "You've successfully authenticated, but GitHub does not provide shell access."
```
3. En Jenkins → **Manage Credentials** → añade una **SSH Username with private key** (ej. ID: `github-ssh-orderapp`, user: `git`, private key: `/var/jenkins_home/.ssh/id_rsa_ssh_orderapp`).

#### Opción B) HTTPS + PAT
1. Crea un **Personal Access Token (Classic)** con permisos mínimos:  
   - **repo** (solo para repos públicos: `public_repo`)  
   - (Opcional) `read:packages` si usas GitHub Packages  
2. En Jenkins → **Manage Credentials** → **Username/Password**:  
   - Username: tu usuario de GitHub  
   - Password: el **PAT**  
   - ID sugerido: `github-https-pat`

### Pipeline (flujo)

1. Jenkins **clona** este repo (`order-app-service`).
2. Obtiene el **SHA corto**: `git rev-parse --short HEAD`.
3. Construye y publica la imagen:
   ```
   docker.io/eduardoalvear/order-app:<SHA>
   ```
4. Jenkins **clona** `order-app-infra`, actualiza el tag en `charts/order-platform/values.yaml` (ej. `image.tag: <SHA>`), hace commit y push.
5. **Argo CD** detecta el cambio y **sincroniza** el clúster (namespace `my-tech`).

---

## 📡 Endpoints REST

**Base path:** `/api/orders`

### 1) Listar todos los pedidos
```http
GET /api/orders
```
**200 OK**
```json
[
  { "id": 1, "description": "Primer pedido", "amount": 100.0 },
  { "id": 2, "description": "Segundo pedido", "amount": 50.0 }
]
```

### 2) Obtener un pedido por ID
```http
GET /api/orders/{id}
```
**200 OK**
```json
{ "id": 1, "description": "Primer pedido", "amount": 100.0 }
```
**404 Not Found** (recomendado)  

### 3) Crear un nuevo pedido
```http
POST /api/orders
Content-Type: application/json
```
**Body**
```json
{ "description": "Pedido nuevo", "amount": 200.0 }
```
**201 Created**
```json
{ "id": 3, "description": "Pedido nuevo", "amount": 200.0 }
```

---

## 🩺 Healthcheck

```http
GET /actuator/health
```

---

## 🔍 Troubleshooting

- **La app no arranca**
  ```bash
  docker logs <container>
  ```
- **Problemas con la base de datos**
  ```bash
  nc -zv <DB_HOST> 5432
  ```
- **Fallo en build**
  ```bash
  ./gradlew clean bootJar -x test
  ```
- **Jenkins en Init/CrashLoop**
  - Revisa PVC y `persistence.storageClass`.
  - Verifica `controller.admin.password` y recursos del nodo.
- **Argo CD no actualiza**
  - Verifica que el commit con el nuevo tag se haya hecho en `order-app-infra`.
  - Revisa `Application` y el `values.yaml` apuntado por la App.
  - Forzar Sync desde la UI de Argo.

---
