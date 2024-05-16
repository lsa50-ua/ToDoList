# Documentación Práctica 4
## Cambios introducidos en la aplicación

### Nuevo diseño de la vista About

``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:replace="fragments :: head (titulo='Acerca de')"></head>

<body>

<!-- Si el usuario está logueado, incluye el Navbar normal -->
<div th:if="${usuario != null}">
    <!-- Recuperamos el objeto "usuarios" que nos pasa el Controller y usamos sus atributos, pasandolos al NavBar -->
    <div th:replace="fragments :: navbar(userName=${usuario} ? ${usuario.getNombre()} : '',
                     userId=${usuario} ? ${usuario.getId()} : '')"></div>
</div>

<!-- Si el usuario no está logueado, incluye el Navbar para no logueados -->
<div th:if="${usuario == null}">
    <div th:replace="fragments :: navbar_noLogueado"></div>
</div>

<div class="container-fluid">
    <div class="container-fluid">
        <h1 style="padding-left: 45%">ToDoList</h1>
        <ul style="padding-left: 35%">
            <li>Desarrollada por Noel Martínez Pomares, Jose Antonio Gomis Arenas , Luis Simón Albarrán </li>
            <li>Versión 1.3.0</li>
            <li>Fecha de última release: 26/11/2023</li>
        </ul>
        <h2 style="padding-left: 45%"> Emails De contacto</h2>
        <p style="padding-left: 35%">Contactanos en caso de ayuda o encontrar un error en la aplicación</p>
        <ul style="padding-left: 35%">
            <li><a href="mailto:jaga21@alu.ua.es">jaga21@alu.ua.es</a></li>
            <li><a href="mailto:npm50@alu.ua.es">npm50@alu.ua.es</a></li>
            <li><a href="mailto:lsa50@alu.ua.es">lsa50@alu.ua.es</a></li>
        </ul>
    </div>

</div>

<div th:replace="fragments::javascript"/>

</body>
</html>
```

En la que se centran los elementos y se muestran los nombres y los mails de los integrantes del grupo.
![image](https://github.com/mads-ua-23-24/todolist-equipo-03/assets/78731028/04391df2-dd40-4fc8-8a7a-8ecf17e52038)


### Nuevo diseño de la vista de ver los usuarios de un grupo

``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:replace="fragments :: head (titulo='Usuarios en Equipo')"></head>

<body>

<div th:replace="fragments :: navbar(userName=${usuario} ? ${usuario.getNombre()} : '',
                     userId=${usuario} ? ${usuario.getId()} : '')"></div>

<div class="container mt-5">

    <div class="row">
        <div class="col text-center">
            <h2 class="mb-4" th:text="'Usuarios del equipo: ' + ${equipo.nombre}"></h2>
        </div>
    </div>

    <div class="row mt-3">
        <div class="col">
            <div class="table-responsive">
                <table class="table table-bordered table-striped">
                    <thead class="thead-dark">
                    <tr>
                        <th class="text-center">Id</th>
                        <th class="text-center">Correo Electrónico</th>
                        <th class="text-center">Nombre</th>
                        <th class="text-center">Fecha de Nacimiento</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr th:each="usuario, iterStat : ${usuarios}" th:class="${iterStat.odd}? 'odd' : 'even'">
                        <td class="text-center" th:text="${usuario.id}"></td>
                        <td th:text="${usuario.email}"></td>
                        <td th:text="${usuario.nombre}"></td>
                        <td class="text-center" th:text="${usuario.fechaNacimiento}"></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col text-center">
            <a class="btn btn-primary" th:href="@{/equipos}">Volver a Equipos</a>
        </div>
    </div>

</div>

<div th:replace="fragments::javascript"/>

</body>
</html>
```
Los cambios introducidos en su mayor parte son visuales, centrando las columnas y marcando mas las líneas de la tabla, y que puedan ajustarse a la pantalla de diferentes dispositivos.
![image (1)](https://github.com/mads-ua-23-24/todolist-equipo-03/assets/78731028/71c4dc52-d2c3-414e-ad7c-0b29e30a2345)


### Nuevo atributo 'descripcion' en los Equipos
Se ha añadido un nuevo atributo a los equipos, para ponerles una descripción. Primero se ha añadido tanto el atributo como el setter y getter al Equipo y al EquipoData:
```java
private String descripcion;

public String getDescripcion() {
    return descripcion;
}
public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
}
```

En el EquipoService se ha creado un nuevo método para añadir una descripción o una nueva descripción a un Equipo:
```java
@Transactional
public EquipoData añadirDescripcion(Long equipoId, String nuevaDescripcion) {
    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

    if (equipo == null) {
        throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
    }

    equipo.setDescripcion(nuevaDescripcion);

    equipo = equipoRepository.save(equipo);

    return modelMapper.map(equipo, EquipoData.class);
}
```

En el EquipoController:
```java
@PostMapping("/equipos/{equipoId}/descripcion")
public String añadirDescripcion(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevaDescripcion") String nuevaDescripcion, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    if (!usuario.isAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }

    if (nuevaDescripcion == null || nuevaDescripcion.trim().isEmpty()) {
        model.addAttribute("errorCambiarDescripcion", "La descripción no puede estar vacía o compuesta por espacios en blanco.");
    } else {
        try {
            equipoService.añadirDescripcion(equipoId, nuevaDescripcion);
            model.addAttribute("correctaDescripcion", "La descripción se ha añadido con éxito.");
        } catch (EquipoServiceException e) {
            model.addAttribute("errorCambiarDescripcion", e.getMessage());
        }
    }

    EquipoData equipo = equipoService.recuperarEquipo(equipoId);
    model.addAttribute("equipo", equipo);

    return "administracionEquipo";
}
```

Que este método se encargará de realizar la petición que se realiza desde el html de administración de un equipo en concreto, añdiendose la descripción a la lista de equipos.  
equipo.html:
```html
<table class="table table-striped">
    <thead>
    <tr>
        <th>Nombre del Equipo</th>
        <th>Descripción</th>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <tr th:each="equipo: ${equipos}">
        <td th:text="${equipo.nombre}"></td>
        <td th:text="${equipo.descripcion}"></td>
        <td>
            <a class="btn btn-primary btn-xs" th:href="@{'/equipos/' + ${equipo.id} + '/usuarios'}">Usuarios</a>
        </td>
        <td>
            <form th:action="@{'/equipos/' + ${equipo.id} + '/unirse'}" method="post">
                <button type="submit" class="btn btn-success">Unirse</button>
            </form>
        </td>
        <td>
            <form th:action="@{'/equipos/' + ${equipo.id} + '/abandonar'}" method="post">
                <button type="submit" class="btn btn-danger">Abandonar</button>
            </form>
        </td>
        <td th:if="${esAdmin}">
            <a class="btn btn-info btn-xs" th:href="@{'/equipos/' + ${equipo.id} + '/administrar'}">Administrar</a>
        </td>
    </tr>
    </tbody>
</table>
```

administracionEquipo.html:
```html
<div class="row mt-3">
    <div class="col">
        <div th:if="${errorCambiarDescripcion}">
            <div class="alert alert-danger" th:text="${errorCambiarDescripcion}"></div>
        </div>
        <div th:if="${correctaDescripcion}">
            <div class="alert alert-success" th:text="${correctaDescripcion}"></div>
        </div>
        <br>
        <form th:action="@{'/equipos/' + ${equipoId} + '/descripcion'}" method="post">
            <div class="form-group">
                <input type="text" class="form-control" id="nuevaDescripcion" name="nuevaDescripcion" placeholder="Nueva Descripción" required>
            </div>
            <button type="submit" class="btn btn-success">Añadir Descripción</button>
        </form>
    </div>
</div>
```

![image](https://github.com/mads-ua-23-24/todolist-equipo-03/assets/78731028/f3c7b172-3a30-454a-8691-61ff94497ef6)  
![image](https://github.com/mads-ua-23-24/todolist-equipo-03/assets/78731028/241a169e-4869-48cf-b271-b74d6473cfc3)


## Esquema de datos 1.2.0
Para esta versión voy a mostrar como se crear inicialmente los diferentes modelos de datos:
equipos:
```sql
CREATE TABLE public.equipos (
    id bigint NOT NULL,
    nombre character varying(255)
);
```

tareas:
```sql
CREATE TABLE public.tareas (
    id bigint NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);
```

usuarios:
```sql
CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean,
    bloqueado boolean,
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);
```

## Script de migración de la base de datos
Aquí lo único a comentar es el contenido del script para poder actualizar la versión 1.2.0 a la 1.3.0. Y este fichero solo contiene lo necesario para agregar la descripcion a los equipos:
```sql
ALTER TABLE public.equipos
ADD COLUMN descripcion character varying(255)
```

## URL de la imagen Docker de la aplicación

[Enlace a la imagen Docker](https://hub.docker.com/layers/jaga21ua/mads-todolist-equipo03/1.3.0/images/sha256:01677068d633dc5ba66e52ceed21ae26e7840e5ffc1abf7d87abf922dc389a3e?uuid=FE3501C5-B156-4E89-A0B1-F364CD400ACE)
