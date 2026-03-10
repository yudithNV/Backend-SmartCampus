# SmartCampus - Backend

API REST para el sistema SmartCampus, construida con Spring Boot.

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Autenticación y autorización
- **Spring Validation** - Validación de datos
- **Maven** - Gestión de dependencias

## 📋 Prerequisitos

- Java 21 o superior
- Maven 3.6+
- Base de datos (configurar en `application.properties`)

## Instalación

```bash
git clone https://github.com/yudithNV/Backend-SmartCampus.git
```
```sh
cd Backend-SmartCampus
```
```sh
./mvnw clean install
```

### Modo de desarrollo 💻

```sh
./mvnw spring-boot:run
```

### Modo para producción ☁️

```sh
./mvnw clean package
java -jar target/smartcampus-0.0.1-SNAPSHOT.jar
```
### El servidor se ejecuta en

```sh
http://localhost:8080
```