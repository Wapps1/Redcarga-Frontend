# 📱 RED CARGA - Documentación Completa de la Aplicación

## 🎯 Concepto General

**Red Carga** es una plataforma móvil que conecta dos tipos de usuarios:

1. **👤 CLIENTES (Remitentes)**: Personas o empresas que necesitan transportar carga de forma interprovincial.
2. **🚚 PROVEEDORES (Transportistas)**: Empresas de transporte que ofrecen servicios de logística.

### Modelo de Negocio

- ❌ **NO intermedia pagos** por servicios de transporte
- ❌ **NO cobra comisiones** por viajes
- ✅ **Monetización exclusiva**: Planes de suscripción que pagan los PROVEEDORES
- 💰 Los pagos por servicios de transporte se realizan **FUERA de la app** (directo entre cliente y proveedor)

---

## 🔐 Sistema de Autenticación Actual

### Tecnologías Implementadas:

- **SessionManager** (Singleton con Hilt)
  - Gestión de sesión con SharedPreferences
  - StateFlow para observar estado de autenticación
  - Métodos: `saveSession()`, `logout()`, `isUserLoggedIn()`, `getAuthToken()`

- **Navegación Type-Safe**
  - Sealed classes para rutas
  - Navigation Compose 2.8.5
  - Grafos separados: Auth (público) y Main (protegido)
  - Animaciones suaves entre pantallas

### Pantallas Implementadas:

✅ **Welcome** (completamente funcional)
- Fondo con 3 elipses blur (colores exactos del Figma)
- Botones: "Crear Cuenta" y "Iniciar Sesión"
- Multi-idioma (español/inglés)

🔜 Pendientes de implementar:
- SignIn
- SignUp
- ForgotPassword
- Verify2FA

---

## 👤 FLUJO DEL CLIENTE (Remitente de Carga)

### 1️⃣ Registro/Login

#### Datos Obligatorios:
```
✓ DNI (Documento de Identificación)
✓ Nombre completo
✓ Validación: DNI y nombre deben coincidir (verificación)
✓ Número telefónico (con verificación 2FA/SMS)
✓ Correo electrónico
✓ Edad
✓ PIN (seguridad adicional)
✓ RUC (opcional o requerido según tipo de usuario)
```

#### Características de Sesión:
- **Auto-login**: El usuario solo inicia sesión una vez
- La app guarda la sesión de forma segura
- Al abrir la app nuevamente, entra automáticamente
- Implementado con `SessionManager` + SharedPreferences

---

### 2️⃣ Pantalla Principal del Cliente (Home)

El cliente tiene acceso a **6 opciones principales**:

#### 📝 1. Realizar Solicitud
Crear una nueva solicitud de transporte de carga.

#### 💵 2. Ver Cotizaciones
Ver las **primeras ofertas** (cotizaciones informales) que los proveedores hacen a sus solicitudes.

#### 🤝 3. Ver Tratos
Ver cotizaciones que el cliente ha **aceptado parcialmente** y están en negociación activa con el proveedor (incluye chat).

#### 👤 4. Perfil de Usuario
Gestionar datos personales, configuraciones de cuenta.

#### 📋 5. Plantillas
Ver y gestionar plantillas guardadas de ítems frecuentes para reutilizar en futuras solicitudes.

#### 📍 6. Trazabilidad de Carga
Ver en tiempo real la ubicación de la carga mediante geolocalización GPS (solo para tratos formales en tránsito).

---

### 3️⃣ Flujo de Creación de Solicitud

#### Por cada ítem único, el cliente ingresa:

**📋 Información Obligatoria:**
```
✓ Nombre del ítem
✓ Frecuencia (¿única vez? ¿recurrente?)
✓ Categoría (seleccionable desde lista predefinida)
✓ Fotos (mínimo 1, máximo N)
✓ Medidas (alto × ancho × profundidad)
✓ Peso
```

**🤖 Análisis con IA:**
1. Cliente toma fotos del ítem
2. IA analiza las imágenes automáticamente
3. IA estima las medidas aproximadas del objeto
4. Campo de "Medidas" se **autocompleta**
5. Cliente puede **editar** si no está de acuerdo

**💾 Auto-guardado de Plantillas:**
- Cada configuración de ítem se guarda automáticamente como plantilla
- Puede reutilizarse en futuras solicitudes
- También se pueden gestionar manualmente desde "Mis Plantillas"

#### Definir Ruta de Transporte:

```
📍 Origen (obligatorio)
📍 Puntos intermedios (opcional, pueden ser 0 o más)
📍 Destino final (obligatorio)
```

#### Enviar Solicitud:
- Una vez completada toda la información
- Click en "Enviar Solicitud"
- La solicitud se publica en **TIEMPO REAL** para proveedores

---

### 4️⃣ Flujo de Cotización → Trato → Viaje

```
┌─────────────────────────────────────────────────────────┐
│ CLIENTE CREA SOLICITUD                                   │
└─────────────────────────────────────────────────────────┘
                    ↓ (Tiempo Real)
┌─────────────────────────────────────────────────────────┐
│ SOLICITUD LLEGA A PROVEEDORES                           │
│ (Matching según rutas, capacidades, etc.)               │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ PROVEEDORES RESPONDEN CON COTIZACIONES                  │
│ (En tiempo real: precio, fecha estimada, condiciones)   │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ CLIENTE VE COTIZACIONES (Pantalla "Ver Cotizaciones")   │
│ Compara precios, reputación, fechas de proveedores      │
└─────────────────────────────────────────────────────────┘
                    ↓ (Cliente selecciona una cotización)
┌─────────────────────────────────────────────────────────┐
│ SE INICIA TRATO                                         │
│ Estado: "Trato en Negociación"                          │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ SE ACTIVA CHAT (Cliente ↔ Proveedor)                   │
│ Ambos pueden negociar: precio, fechas, condiciones      │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ NEGOCIACIÓN Y AJUSTES                                   │
│ Pueden modificar términos, cambiar ítems, etc.          │
└─────────────────────────────────────────────────────────┘
                    ↓ (Ambos confirman en la app)
┌─────────────────────────────────────────────────────────┐
│ TRATO FORMAL (Acuerdo cerrado en la app)               │
│ Estado: "Trato Formalizado"                             │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ PAGO FUERA DE LA APP                                    │
│ Cliente paga al proveedor directamente                  │
│ (Transferencia, efectivo, etc.)                          │
│ ⚠️ La app NO intermedia este pago                       │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ TRAMITACIÓN DE DOCUMENTOS (Desde la app)               │
│ ✓ Cliente: Genera Guía de Remisión (GRE)               │
│ ✓ Proveedor: Genera Guía de Transportista              │
│ Ambos documentos son OBLIGATORIOS por ley               │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ CARGA EN CAMINO (Viaje activo)                         │
│ Estado: "En Tránsito"                                   │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ PROVEEDOR ACTIVA GEOLOCALIZACIÓN GPS                    │
│ Tracking en tiempo real obligatorio                     │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ CLIENTE VE TRAZABILIDAD                                 │
│ ✓ Mapa con ubicación en tiempo real                    │
│ ✓ Chat activo con proveedor                            │
│ ✓ Actualizaciones de estado                            │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ CARGA ENTREGADA                                         │
│ Estado: "Entregado"                                     │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│ VENTANA DE CALIFICACIÓN (7 días)                       │
│ ✓ Cliente califica al proveedor                        │
│ ✓ Proveedor califica al cliente                        │
│ Sistema de doble ciego                                  │
└─────────────────────────────────────────────────────────┘
```

---

### 5️⃣ Cambios Post-Trato (Modificaciones)

Una vez que el trato está **formalizado**, pueden surgir cambios. Hay **3 casos posibles**:

#### 📈 CASO A: Sube el Precio

**Cuándo:** Cliente agrega ítems, aumenta peso/volumen, cambia ruta a más larga, etc.

**Flujo:**
```
1. Cliente solicita cambio en la app
2. Proveedor actualiza cotización en el TRATO
3. Nueva cotización queda registrada como modificación
4. Cliente debe ACEPTAR los nuevos términos en la app
5. Ajuste monetario se resuelve FUERA de la app
6. La app solo REGISTRA el cambio (trazabilidad)
```

#### 📉 CASO B: Baja el Precio

**Cuándo:** Cliente quita ítems, reduce distancia, menor peso, etc.

**Flujo:**
```
1. Cliente solicita cambio en la app
2. Proveedor actualiza términos con reducción de precio
3. Cliente acepta los nuevos términos
4. Cualquier devolución monetaria se hace FUERA de la app
5. La app solo REGISTRA la reducción (trazabilidad)
```

#### ❌ CASO C: Proveedor NO Acepta el Cambio

**Flujo:**
```
1. Cliente solicita cambio
2. Proveedor rechaza en la app
3. Cliente tiene 2 opciones:
   
   a) SEGUIR CON LO ORIGINAL
      - No hay cambios de precio
      - Continúa el viaje como estaba pactado
   
   b) CANCELAR TRATO
      - Según política de cancelación
      - Puede haber penalidad (fuera de la app)
      - La app registra la cancelación
```

**⚠️ Importante:** En todos los casos, **la app NO procesa dinero**, solo registra los cambios para trazabilidad.

---

## 🚚 FLUJO DEL PROVEEDOR (Transportista)

### 1️⃣ Registro/Login

Similar al cliente, con datos de empresa:
- RUC (obligatorio)
- Razón social
- Datos del representante legal
- Documentos de la empresa

### 2️⃣ Pantalla Principal del Proveedor (Home)

El proveedor tiene acceso a **6 opciones principales**:

#### 🚛 1. Administrar Flotas
Gestionar sus vehículos de transporte:
- Lista de vehículos/unidades
- Por cada vehículo:
  - Placa
  - Tipo de vehículo
  - Capacidad (peso/volumen)
  - Dimensiones
  - Documentos (SOAT, revisión técnica, etc.)
  - Estado (disponible, en uso, en mantenimiento)
  - Atributos visibles para clientes

#### 🗺️ 2. Administrar Rutas
Configurar y gestionar:
- Rutas disponibles que cubre
- Itinerarios habituales
- Calendarios de salidas
- Ventanas de tiempo
- Capacidades por ruta

#### 📨 3. Ver Solicitudes
Ver solicitudes activas de clientes:
- En tiempo real
- Filtradas por rutas que cubre
- Detalles de cada solicitud
- Opción de enviar cotización

#### 🤝 4. Ver Tratos
Gestionar tratos:
- En negociación (con chat activo)
- Formalizados (acuerdo cerrado)
- En tránsito (viaje activo)
- Completados

#### 📍 5. Tracking/Geolocalización
**OBLIGATORIO** activar cuando hay carga en camino:
- GPS en tiempo real
- Cliente puede ver ubicación
- Actualizaciones automáticas

#### 💳 6. Administrar Pagos
Gestión de suscripción a la plataforma:
- Plan actual (Básico, Pro, Premium, etc.)
- Ciclo de cobro
- Facturación
- Historial de pagos
- Comprobantes
- **Opcional**: Registro de cobros a clientes (fuera de app, solo para control interno)

### 3️⃣ Responsabilidades del Proveedor

```
✓ Responder solicitudes con cotizaciones (precio, fecha)
✓ Negociar en chat con clientes
✓ Emitir Guía de Transportista (documento obligatorio)
✓ ACTIVAR tracking GPS cuando carga está en camino
✓ Mantener chat activo con cliente
✓ Actualizar estados del viaje
✓ Reportar incidencias si las hay
✓ Confirmar entrega
```

---

## ⭐ SISTEMA DE CALIFICACIONES Y REPUTACIÓN

### 📋 Condiciones para Calificar

**Solo se puede calificar si:**
```
✓ Trato Formal (acuerdo cerrado en la app)
✓ Documentos tramitados correctamente (GRE + Guía Transportista)
✓ Estado final: "Entregado" o disputa resuelta
✓ Ventana de 7 días desde la entrega
✓ Sistema de doble ciego (nadie ve hasta que ambos califiquen)
✓ Si hay disputa abierta: calificaciones se pausan hasta resolver
```

### 👤 Cliente → Proveedor (5 Dimensiones)

**Escala:** 1 a 5 estrellas por dimensión

1. **⏱️ Puntualidad**
   - Recojo en tiempo acordado
   - Entrega en ventana estimada

2. **📦 Cuidado de la Carga**
   - Sin daños
   - Manipulación correcta
   - Embalaje apropiado

3. **💬 Comunicación**
   - Claridad de mensajes
   - Disponibilidad para responder
   - Proactividad en actualizaciones

4. **💰 Exactitud de Cotización**
   - Precio final vs cotizado
   - Sin cobros sorpresa
   - Condiciones respetadas

5. **📄 Documentación**
   - Guías emitidas correctamente
   - Sin errores en documentos
   - Entregadas a tiempo

### 🚚 Proveedor → Cliente (5 Dimensiones)

**Escala:** 1 a 5 estrellas por dimensión

1. **📦 Preparación de la Carga**
   - Embalaje adecuado
   - Acceso fácil para carga
   - Listo en tiempo acordado

2. **📝 Claridad de Información**
   - Medidas reales vs declaradas
   - Peso real vs declarado
   - Ruta real vs informada
   - Sin sorpresas

3. **💬 Comunicación**
   - Respuestas rápidas
   - Claridad en instrucciones
   - Disponibilidad

4. **💵 Cumplimiento de Pagos**
   - Pagó según lo acordado
   - Sin retrasos
   - Sin disputas de pago

5. **⏱️ Tiempo de Espera**
   - No generó demoras innecesarias
   - Procesos de carga ágiles

### 🏷️ Tags Rápidos (Selección Múltiple)

**Ejemplos de tags:**
- "Muy puntual"
- "Excelente comunicación"
- "Empaque deficiente"
- "Ruta alterada sin aviso"
- "Demora en recojo"
- "Carga en perfecto estado"
- "Precios claros"
- "Documentos impecables"

**Utilidad:**
- Sirven para filtros
- Analítica rápida
- Cuando no se quiere escribir comentario largo

### 📊 Visualización de Reputación

**Cliente ve (al elegir proveedor):**
```
✓ Promedio global (1-5 estrellas)
✓ Número total de reseñas
✓ Histograma de distribución (cuántos 5★, 4★, etc.)
✓ Últimos comentarios
✓ Tags más frecuentes
✓ Badges especiales (ej: "100 viajes sin incidencias")
```

**Proveedor ve (al evaluar solicitudes):**
```
✓ Promedio del cliente
✓ Tags frecuentes (ej: "Listo en el horario", "Cambios frecuentes")
✓ Historial de disputas
✓ Número de viajes completados
```

**Perfil de Reputación (ambos tienen):**
```
✓ Desglose por cada dimensión
✓ Evolución en últimos 90 días
✓ Distribución de estrellas
✓ Tasa de respuesta en chats
✓ Tasa de puntualidad
✓ Porcentaje de disputas resueltas
```

### 🎯 Sistema de Doble Ciego

```
1. Viaje se marca como "Entregado"
2. Se abre ventana de calificación (7 días)
3. Cliente califica → se guarda OCULTO
4. Proveedor califica → se guarda OCULTO
5. Cuando AMBOS califican:
   - Ambas calificaciones se revelan
   - Se actualizan promedios
   - Se publican comentarios
6. Si pasan 7 días y solo uno calificó:
   - Se publica la única calificación
   - El otro pierde oportunidad
```

---

## 🏗️ BOUNDED CONTEXTS - Arquitectura Backend

La aplicación está diseñada con **Domain-Driven Design (DDD)** usando **13 Bounded Contexts**:

### 1️⃣ **IAM** (Identity & Access Management)

**Propósito:** Autenticación y autorización técnica

**Posee:**
- Usuarios técnicos (cuentas de acceso)
- Credenciales (email/password/PIN)
- MFA/2FA
- Sesiones (tokens JWT)
- Roles y permisos (RBAC)

**Comandos:**
- `RegistrarCuenta`
- `IniciarSesion`
- `RefrescarToken`
- `Habilitar2FA`
- `AsignarRol`

**Eventos:**
- `SesionIniciada`
- `RolAsignado`
- `2FAActivado`
- `CuentaBloqueada`

**⚠️ Nota:** NO guarda datos civiles (DNI, nombre legal). Solo gestiona acceso técnico.

---

### 2️⃣ **IDENTITY**

**Propósito:** Identidad civil/tributaria y verificación KYC

**Posee:**
- Personas físicas/jurídicas
- DNI/RUC
- Nombre legal completo
- Validaciones (match DNI-nombre con RENIEC/SUNAT)
- Estado de verificación KYC

**Comandos:**
- `VerificarDNI`
- `VerificarRUC`
- `ActualizarDatosLegales`
- `ValidarIdentidad`

**Eventos:**
- `IdentidadVerificada`
- `RUCValidado`
- `IdentidadActualizada`
- `VerificacionFallida`

**⚠️ Nota:** Es el "upstream owner" de `CustomerId` y `ProviderId`. Otros BCs consumen sus proyecciones.

---

### 3️⃣ **CUSTOMERS**

**Propósito:** Perfil operativo del cliente remitente

**Posee:**
- Preferencias del cliente
- Plantillas de ítems guardadas
- Historial de solicitudes
- Contactos frecuentes
- Configuraciones de notificaciones

**Comandos:**
- `CrearPerfilCliente`
- `ActualizarPreferencias`
- `GuardarPlantillaItem`
- `EliminarPlantilla`

**Eventos:**
- `PerfilClienteCreado`
- `PlantillaCreada`
- `PlantillaActualizada`
- `PreferenciasActualizadas`

**⚠️ Nota:** NO almacena reputación consolidada (eso es REPUTATION), solo referencias.

---

### 4️⃣ **PROVIDERS**

**Propósito:** Perfil del proveedor/transportista

**Posee:**
- Datos de empresa visibles al cliente
- Estados de verificación (documentos)
- Plan/suscripción activa (referencia a PAYMENTS)
- Políticas de operación (cancelación, etc.)
- Configuraciones de visibilidad

**Comandos:**
- `CrearPerfilProveedor`
- `ActualizarPerfilProveedor`
- `VincularSuscripcion`
- `ActualizarPoliticas`

**Eventos:**
- `PerfilProveedorCreado`
- `PerfilProveedorActualizado`
- `ProveedorHabilitado`
- `ProveedorDeshabilitado`

**⚠️ Nota:** NO gestiona vehículos directamente (eso es FLEET).

---

### 5️⃣ **FLEET**

**Propósito:** Gestión de flota y documentación de vehículos

**Posee:**
- Vehículos/unidades
- Atributos técnicos (capacidad, dimensiones)
- Documentos por vehículo (SOAT, revisión técnica)
- Conductores asociados
- Estados (disponible, en uso, mantenimiento)

**Comandos:**
- `RegistrarVehiculo`
- `ActualizarDocumentosVehiculo`
- `AsignarConductor`
- `CambiarEstadoVehiculo`

**Eventos:**
- `VehiculoRegistrado`
- `DocumentoVehiculoVencido`
- `ConductorAsignado`
- `VehiculoDeshabilitado`

**⚠️ Nota:** Es fuente de verdad para disponibilidad técnica. TRIPS solo referencia.

---

### 6️⃣ **PLANNING**

**Propósito:** Planificación operativa y matching oferta-demanda

**Posee:**
- Rutas/itinerarios plantillas
- Calendarios de salida
- Capacidades planificadas por ruta
- Reglas de elegibilidad (matching)
- Ventanas de tiempo

**Comandos:**
- `PublicarDisponibilidad`
- `PlanificarSalida`
- `SugerirProveedoresParaSolicitud`
- `ReservarCapacidad`

**Eventos:**
- `DisponibilidadPublicada`
- `SalidaPlanificada`
- `CapacidadReservada`
- `CapacidadLiberada`

**⚠️ Nota:** NO negocia precios. Solo propone y reserva capacidad. Negociación vive en DEALS.

---

### 7️⃣ **REQUESTS**

**Propósito:** Demanda del cliente (solicitudes de transporte)

**Posee:**
- Solicitud completa
- Ítems con detalles:
  - Nombre, categoría, frecuencia
  - Fotos (URLs)
  - Medidas (IA estimada + manual)
  - Peso
- Ruta (origen, intermedios, destino)
- Estado de la solicitud

**Comandos:**
- `CrearSolicitud`
- `EditarSolicitud`
- `AgregarItem`
- `ModificarRuta`
- `CerrarSolicitud`

**Eventos:**
- `SolicitudCreada`
- `SolicitudEditada`
- `ItemAgregado`
- `SolicitudCerrada`

**⚠️ Nota:** NO contiene cotizaciones. Solo el requerimiento. Dispara matching en PLANNING y negociación en DEALS.

---

### 8️⃣ **DEALS**

**Propósito:** Negociación y acuerdo entre Cliente y Proveedor

**Estados de un Deal:**
```
Cotización → Trato en Negociación → Trato Formal → En Tránsito → Entregado
```

**Posee:**
- Ofertas/cotizaciones de proveedores
- Estado actual del trato
- Chat de negociación (mensajes)
- Historial de modificaciones:
  - Alzas de precio
  - Bajas de precio
  - Rechazos de cambios
- Términos acordados finales

**Comandos:**
- `ProponerOferta` (proveedor)
- `IniciarTrato` (cliente acepta cotización)
- `FormalizarTrato` (ambos confirman)
- `SolicitarCambio` (cliente)
- `AceptarCambio` (proveedor)
- `RechazarCambio` (proveedor)
- `CancelarTrato`
- `EnviarMensajeChat`

**Eventos:**
- `OfertaEmitida`
- `TratoIniciado`
- `TratoFormalizado`
- `TratoModificado` (Alza | Baja | Rechazo)
- `TratoCancelado`
- `MensajeEnviado`

**⚠️ Nota:** Al formalizar, emite eventos para WAYBILLS (documentos) y TRIPS (tracking). Dinero fuera de la app: solo registra cambios de términos.

---

### 9️⃣ **PAYMENTS**

**Propósito:** Suscripciones de proveedores (NO pagos de viajes)

**Posee:**
- Planes disponibles (Básico, Pro, Premium)
- Suscripciones activas de proveedores
- Ciclos de cobro
- Comprobantes de pago
- Historial de facturación
- Features por plan

**Comandos:**
- `ActivarSuscripcion`
- `CambiarPlan`
- `RenovarSuscripcion`
- `CancelarSuscripcion`
- `EmitirComprobante`

**Eventos:**
- `SuscripcionActivada`
- `SuscripcionRenovada`
- `SuscripcionCancelada`
- `ComprobanteEmitido`
- `PagoFallido`

**⚠️ Nota:** Es "upstream owner" del estado de suscripción que habilita features premium en otros BCs (visibilidad, trazabilidad avanzada, etc.).

---

### 🔟 **WAYBILLS**

**Propósito:** Documentos obligatorios del viaje

**Posee:**
- Guía de Remisión (GRE) - emitida por cliente
- Guía de Transportista - emitida por proveedor
- Flujos de emisión/validación
- Enlaces a TRIPS y DEALS
- Estado de documentos (Emitido, Anulado, Observado)

**Comandos:**
- `GenerarGRE` (cliente)
- `GenerarGuiaTransportista` (proveedor)
- `AnularDocumento`
- `ActualizarDocumento`

**Eventos:**
- `DocumentosEmitidos`
- `DocumentoAnulado`
- `DocumentoObservado`
- `DocumentoValidado`

**Integraciones:**
- SUNAT / servicios tributarios
- Validación de números de documento

**⚠️ Nota:** Se habilita tras `TratoFormalizado`. Ambos documentos son OBLIGATORIOS para iniciar el viaje.

---

### 1️⃣1️⃣ **TRIPS**

**Propósito:** Ejecución del viaje y trazabilidad en tiempo real

**Estados de un Trip:**
```
Programado → En Ruta → Incidencia (opcional) → Entregado
```

**Posee:**
- Itinerario del trato formal
- Asignación de vehículo y conductor (referencia a FLEET)
- Estados del viaje
- Tracking de geolocalización GPS (coords + timestamp)
- Incidencias reportadas
- Fotos de entrega

**Comandos:**
- `CrearTripDesdeTrato`
- `AsignarVehiculo`
- `ActivarTracking` (OBLIGATORIO)
- `ReportarPosicion` (automático cada N segundos)
- `ReportarIncidencia`
- `MarcarEntregado`
- `AdjuntarFotoEntrega`

**Eventos:**
- `TripCreado`
- `TrackingActivado`
- `PosicionReportada`
- `IncidenciaReportada`
- `ViajeEntregado`
- `ViajeCompletado`

**⚠️ Nota:** 
- Consume FLEET (vehículo disponible)
- Consume WAYBILLS (documentos OK)
- Al cerrar con "Entregado", dispara a REPUTATION la ventana de rating

---

### 1️⃣2️⃣ **REPUTATION**

**Propósito:** Calificaciones y reputación (cliente y proveedor)

**Posee:**
- Ratings (1-5 estrellas)
- Dimensiones evaluadas:
  - Puntualidad, Cuidado, Comunicación, etc.
- Tags/etiquetas
- Comentarios
- Reglas de doble ciego
- Ventana de 7 días
- Agregados/promedios/histogramas
- Badges especiales

**Comandos:**
- `AbrirVentanaCalificacion` (tras "Entregado")
- `RegistrarCalificacion`
- `CerrarVentana` (7 días o ambos calificaron)
- `RecalcularMetricas`
- `AsignarBadge`

**Eventos:**
- `VentanaCalificacionAbierta`
- `CalificacionRegistrada`
- `VentanaCerrada`
- `MetricasActualizadas`
- `BadgeAsignado`

**⚠️ Nota:** Provee proyecciones consultables por REQUESTS/DEALS (filtrar proveedores por reputación). NO mezcla con perfiles base.

---

### 1️⃣3️⃣ **NOTIFICATION**

**Propósito:** Entrega de notificaciones multicanal

**Posee:**
- Canales (Push, Email, SMS)
- Plantillas de mensajes
- Reglas de envío
- Rate limiting
- Programación de envíos
- Historial de notificaciones

**Comandos:**
- `EnviarNotificacion`
- `ProgramarNotificacion`
- `CancelarEnvio`
- `ActualizarPlantilla`

**Eventos:**
- `NotificacionEnviada`
- `EntregaConfirmada`
- `EntregaFallida`
- `NotificacionProgramada`

**⚠️ Nota:** Es **downstream de todos**. Reacciona a eventos de: DEALS, TRIPS, WAYBILLS, PAYMENTS, REPUTATION, etc.

---

## 📱 BOUNDED CONTEXTS EN FRONTEND (Simplificados)

Para el desarrollo móvil, se fusionan y simplifican algunos BCs. Total: **11 BCs en frontend**

### Frontend BCs:

1. **auth** → Fusiona IAM + partes de IDENTITY para login/registro/KYC básico
2. **customers** → Perfil operativo del cliente + plantillas de ítems
3. **providers** → Perfil de proveedor + estado de suscripción (referencia)
4. **fleet** → Vehículos y documentos por unidad
5. **planning** → Rutas/itinerarios y salidas activas del proveedor
6. **requests** → Crear solicitud, listar, consultar
7. **deals** → Cotizaciones → trato, chat, modificaciones
8. **waybills** → Emisión de GRE y Guía de Transportista
9. **trips** → Tracking GPS y estados del viaje
10. **reputation** → (opcional para MVP, implementar después)
11. **notification** → (opcional, centro de notificaciones mock)

---

## 📂 ESTRUCTURA DE CARPETAS POR BC

Cada Bounded Context sigue esta estructura estricta:

```
app/src/main/java/com/wapps1/redcarga/features/<bc_name>/
│
├── data/                                    # CAPA DE DATOS
│   │
│   ├── di/
│   │   └── DataModule.kt                   # @Module @InstallIn(SingletonComponent)
│   │                                       # Provee: Retrofit, Room, Repositories
│   │
│   ├── local/                              # PERSISTENCIA LOCAL (Room)
│   │   ├── db/
│   │   │   └── <BcName>Database.kt        # RoomDatabase del BC
│   │   │
│   │   ├── dao/
│   │   │   └── <Entity>Dao.kt             # Uno por entidad agregada
│   │   │
│   │   └── entities/
│   │       └── <Entity>Entity.kt          # Entidades Room (tablas)
│   │
│   ├── remote/                             # DATOS REMOTOS (API)
│   │   ├── models/
│   │   │   └── <Endpoint>Dto.kt           # DTOs exclusivos de red
│   │   │
│   │   └── services/
│   │       └── <BcName>Service.kt         # Interface Retrofit/Ktor
│   │
│   ├── mappers/
│   │   └── <BcName>Mappers.kt             # DTO ↔ Entity ↔ Domain
│   │
│   └── repositories/
│       └── <BcName>RepositoryImpl.kt      # Implementa contrato de dominio
│
├── domain/                                 # CAPA DE DOMINIO
│   │
│   ├── models/
│   │   └── <DomainModel>.kt               # Modelos puros de dominio
│   │                                      # (NO DTOs, NO Entities)
│   │
│   └── repositories/
│       └── <BcName>Repository.kt          # Contratos/Interfaces
│                                          # Consume la capa de presentación
│
└── presentation/                           # CAPA DE PRESENTACIÓN
    │
    ├── di/
    │   └── PresentationModule.kt          # @InstallIn(ViewModelComponent)
    │                                      # Para Dispatchers, objetos de UI
    │
    ├── components/                        # COMPONENTES REUTILIZABLES
    │   └── <Component>.kt                 # Ej: ItemCard, CustomButton
    │
    ├── viewmodels/
    │   └── <Screen>ViewModel.kt           # Uno por flujo/pantalla
    │
    └── views/                             # PANTALLAS
        ├── <Screen>.kt                    # Composable de pantalla
        └── <Screen>Detail.kt              # (Opcional) más pantallas
```

---

## 🎨 ESTADO ACTUAL DE IMPLEMENTACIÓN

### ✅ **Completamente Implementado:**

#### Core (Infraestructura):
- ✅ **SessionManager** (Singleton con Hilt)
  - SharedPreferences para persistencia
  - StateFlow para observar autenticación
  - Métodos: saveSession, logout, isUserLoggedIn, getAuthToken, getUserId

- ✅ **Sistema de Navegación Type-Safe**
  - Sealed classes para rutas (Route.kt)
  - Navigation Graphs separados (NavGraph.kt)
    - Auth Graph (público): Welcome, SignIn, SignUp, ForgotPassword
    - Main Graph (protegido): Home, Profile, Settings
  - Navigation.kt con animaciones
  - NavExtensions.kt con helpers
  - Integración con Hilt

- ✅ **Multi-idioma (i18n)**
  - LocaleHelper.kt
  - values/strings.xml (español - predeterminado)
  - values-en/strings.xml (inglés)
  - 47+ strings traducidos

#### Auth BC:
- ✅ **Welcome Screen** (100% funcional)
  - Fondo con 3 elipses blur (valores exactos del Figma)
  - Colores: RcColor2, RcColor3, RcColor5
  - Blur de 250.dp con BlurMaskFilter
  - Botones: "Crear Cuenta" y "Iniciar Sesión"
  - Multi-idioma
  - Preview funcional

#### Theme:
- ✅ Colores definidos (RcColor1-8, White, Black)
- ✅ Typography con Montserrat
- ✅ RedcargaTheme

#### Gradle:
- ✅ Dependencies configuradas:
  - Navigation Compose 2.8.5
  - Hilt 2.56.2
  - Room 2.7.2
  - Coroutines 1.9.0

### 🔜 **Pendiente de Implementar (Solo UI):**

#### Auth BC:
- 🔜 SignInScreen
- 🔜 SignUpScreen
- 🔜 ForgotPasswordScreen
- 🔜 Verify2FAScreen

#### Customers BC (Cliente):
- 🔜 HomeClienteScreen
- 🔜 CrearSolicitudScreen
- 🔜 AgregarItemScreen
- 🔜 DefinirRutaScreen
- 🔜 MisSolicitudesScreen
- 🔜 VerCotizacionesScreen
- 🔜 VerTratosScreen
- 🔜 ChatTratoScreen
- 🔜 PlantillasScreen
- 🔜 TrazabilidadScreen
- 🔜 PerfilClienteScreen

#### Providers BC (Proveedor):
- 🔜 HomeProveedorScreen
- 🔜 AdministrarFlotasScreen
- 🔜 DetalleVehiculoScreen
- 🔜 AdministrarRutasScreen
- 🔜 VerSolicitudesDisponiblesScreen
- 🔜 EnviarCotizacionScreen
- 🔜 MisTratosProveedorScreen
- 🔜 DetalleTratoProveedorScreen
- 🔜 ActivarTrackingScreen
- 🔜 AdministrarPagosScreen

#### Waybills BC:
- 🔜 EmitirGuiaRemisionScreen
- 🔜 EmitirGuiaTransportistaScreen

#### Reputation BC:
- 🔜 CalificarProveedorModal
- 🔜 CalificarClienteModal
- 🔜 VerReputacionScreen

---

## 🎨 GUÍA DE DISEÑO (Mantener en todas las vistas)

### Paleta de Colores:
```kotlin
RcColor1 = #FFF9F5  (fondo claro)
RcColor2 = #FEC6A3  (melocotón)
RcColor3 = #F3C4BE  (rosa claro)
RcColor4 = #EC8366  (coral oscuro - primarios)
RcColor5 = #F26A6C  (rosa coral)
RcColor6 = #3D3D3D  (texto oscuro)
RcColor7 = #F8EBE2  (fondo suave)
RcColor8 = #9D9D9D  (gris)
White   = #FFFFFF
Black   = #000000
```

### Fondo con Blur (Reutilizar en todas):
```kotlin
@Composable
private fun WelcomeBackground(
    modifier: Modifier = Modifier,
    blurDp: Dp = 250.dp
) {
    // Tres elipses con blur usando MaskFilter
    // Colores: RcColor5, RcColor2, RcColor3
    // Posiciones exactas del Figma (Frame 387×852)
}
```

### Botones:
```kotlin
// Botón primario
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = RcColor6
    ),
    shape = RoundedCornerShape(28.dp),
    modifier = Modifier.width(250.dp).height(52.dp),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
)

// Botón secundario
OutlinedButton(
    border = BorderStroke(2.dp, Color.White),
    shape = RoundedCornerShape(28.dp),
    modifier = Modifier.width(250.dp).height(52.dp),
    colors = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.White.copy(alpha = 0.15f),
        contentColor = Color.White.copy(alpha = 0.90f)
    )
)
```

### Tipografía:
```kotlin
MaterialTheme.typography.bodyMedium      // Texto normal
MaterialTheme.typography.headlineMedium  // Títulos grandes
MaterialTheme.typography.titleSmall      // Títulos pequeños
FontWeight.Bold                          // Negrita fuerte
FontWeight.SemiBold                      // Negrita media
```

### Espaciado:
```kotlin
.padding(horizontal = 24.dp, vertical = 32.dp)
Arrangement.spacedBy(24.dp)
```

### Estructura de Composable:
```kotlin
@Composable
fun MyScreen(
    onNavigateX: () -> Unit,
    onNavigateY: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con blur
        WelcomeBackground(Modifier.matchParentSize())
        
        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // UI aquí
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyScreenPreview() {
    RedcargaTheme(darkTheme = false) {
        MyScreen(
            onNavigateX = {},
            onNavigateY = {},
            onBackClick = {}
        )
    }
}
```

### Strings Multi-idioma:
```kotlin
// SIEMPRE usar stringResource
Text(text = stringResource(R.string.my_string_key))

// Para strings con parámetros
stringResource(R.string.welcome_user, userName)
```

---

## 📚 PRÓXIMOS PASOS

### Fase 1: Completar Auth Flow (Prioridad ALTA)
1. Implementar SignInScreen
2. Implementar SignUpScreen  
3. Implementar Verify2FAScreen
4. Implementar ForgotPasswordScreen
5. Conectar con SessionManager

### Fase 2: Home Clientes (Prioridad ALTA)
1. Implementar HomeClienteScreen (6 opciones)
2. Implementar navegación a cada sección

### Fase 3: Flujo de Solicitud (Prioridad ALTA)
1. CrearSolicitudScreen (multi-step)
2. AgregarItemScreen (con IA mock para fotos)
3. DefinirRutaScreen (mapa mock)
4. MisSolicitudesScreen

### Fase 4: Cotizaciones y Tratos
1. VerCotizacionesScreen
2. VerTratosScreen
3. ChatTratoScreen
4. Flujo de modificaciones

### Fase 5: Tracking y Documentos
1. TrazabilidadScreen (mapa GPS)
2. EmitirGuiaRemisionScreen
3. EmitirGuiaTransportistaScreen

### Fase 6: Proveedor Flow
1. HomeProveedorScreen
2. AdministrarFlotasScreen
3. VerSolicitudesDisponiblesScreen
4. MisTratosProveedorScreen

### Fase 7: Reputación
1. CalificarProveedorModal
2. CalificarClienteModal
3. VerReputacionScreen

---

## 🔐 CONSIDERACIONES DE SEGURIDAD

- ✅ Sesiones persistentes con SharedPreferences (encriptación recomendada)
- ✅ Tokens JWT refresh automático
- ✅ 2FA obligatorio para operaciones sensibles
- ✅ Validación de identidad con RENIEC/SUNAT
- ⚠️ TODO: Encriptar tokens localmente
- ⚠️ TODO: Certificado SSL Pinning
- ⚠️ TODO: Ofuscación de código con ProGuard

---

## 📊 MÉTRICAS Y ANALÍTICA (Futuro)

- Número de solicitudes por día
- Tasa de conversión (solicitud → trato formal)
- Tiempo promedio de negociación
- Calificación promedio por proveedor
- Tasa de cancelación
- Disputas abiertas/cerradas
- Tracking de geolocalización (heatmaps)

---

## 🚀 STACK TECNOLÓGICO

### Android:
- Kotlin 2.0.21
- Jetpack Compose
- Navigation Compose 2.8.5
- Material3
- Hilt 2.56.2 (DI)
- Room 2.7.2 (Database)
- Coroutines 1.9.0
- Coil (imágenes)
- Retrofit (API)

### Backend (Fuera de scope móvil):
- Microservicios por BC
- Event-Driven Architecture
- API REST + WebSockets (chat, tracking)
- CQRS + Event Sourcing
- PostgreSQL / MongoDB
- Redis (cache)
- RabbitMQ / Kafka (eventos)

---

## 📝 NOTAS FINALES

- **Pagos:** NUNCA se procesan en la app. Solo suscripciones de proveedores.
- **Documentos:** GRE y Guía de Transportista son OBLIGATORIOS por ley peruana.
- **Tracking:** GPS en tiempo real es OBLIGATORIO cuando carga está en camino.
- **Calificaciones:** Sistema de doble ciego para evitar sesgos.
- **Monetización:** 100% por suscripciones de proveedores (sin comisiones por viaje).

---

**Última actualización:** 30 de Septiembre, 2025  
**Versión del documento:** 1.0.0  
**Estado:** Documentación completa de la arquitectura y flujos de negocio
