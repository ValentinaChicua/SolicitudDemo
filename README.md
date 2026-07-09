# SolicitudDemo

Servicio Spring Boot para recibir solicitudes de demo y sincronizarlas con HubSpot.

## Requisitos

- Java 17
- Gradle Wrapper incluido en el proyecto
- Un token válido de HubSpot con permisos para CRM

## Instalacion

1. Clona o abre el proyecto en tu maquina.
2. Verifica que Java 17 este disponible.
3. Revisa `src/main/resources/application.properties` y configura el token de HubSpot.

Ejemplo:

```properties
spring.application.name=SolicitudDemo
hubspot.api-token=tu_token_de_hubspot
```

Si prefieres no dejar el token fijo, puedes reemplazarlo por una variable de entorno desde tu entorno local.

## Ejecucion

### En Windows PowerShell

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.15.6-hotspot'
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
./gradlew bootRun
```

### Ejecutar pruebas

```powershell
./gradlew test
```

La aplicacion se levanta en `http://localhost:8080`.

## API

### Sincronizar contacto

`POST /api/hubspot/contacts/sync`

Este endpoint recibe un JSON con los datos basicos del contacto y hace lo siguiente:

1. Busca el contacto por email en HubSpot.
2. Si existe, actualiza su informacion.
3. Si no existe, lo crea.
4. Imprime el resultado en consola.

### Body de ejemplo

```json
{
  "firstname": "Juan",
  "lastname": "Perez",
  "email": "juan@test.com",
  "company": "ABC SAS"
}
```

### Ejemplo con `curl`

```bash
curl -X POST http://localhost:8080/api/hubspot/contacts/sync \
  -H "Content-Type: application/json" \
  -d '{"firstname":"Juan","lastname":"Perez","email":"juan@test.com","company":"ABC SAS"}'
```

### Ejemplo en PowerShell

```powershell
$body = @{
  firstname = 'Juan'
  lastname  = 'Perez'
  email     = 'juan@test.com'
  company   = 'ABC SAS'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/hubspot/contacts/sync' -ContentType 'application/json' -Body $body
```

### Respuesta esperada

El servicio devuelve un objeto con esta informacion:

- `operation`: `created` o `updated`
- `contactId`: identificador del contacto en HubSpot
- `email`: correo usado en la busqueda
- `companyId`: identificador de la empresa asociada, si aplica

## Flujo funcional

1. El controlador recibe el JSON.
2. El servicio arma las propiedades del contacto.
3. El cliente de HubSpot busca por email.
4. Si no existe, crea el contacto.
5. Si existe, lo actualiza.
6. El resultado se registra en consola.

## Consola

Ademas del logger de Spring, el servicio imprime el resultado en consola con un mensaje similar a este:

```text
Resultado de la operacion: SyncResult[operation=created, contactId=..., email=..., companyId=...]
```

