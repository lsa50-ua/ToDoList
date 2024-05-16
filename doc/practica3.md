# Práctica 2
## NOEL MARTÍNEZ POMARES 48771960T

### Gestionar pertenencia al equipo

#### Servicio y modelo

##### Primer Test - Nuevas excepciones al crear un equipo

En este primer commit he añadido dos excepciones en el método pra crear un equipo. Estas excepciones saltarán si el nombre del equipo es null o si es una cadena vacia; y también
habrá otra excepción si ya existe un equipo con dicho nombre:  
````
@Transactional
public EquipoData crearEquipo(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
        throw new EquipoServiceException("El nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
    }

    List<Equipo> equipos = equipoRepository.findAll();

    // Buscar si ya existe un equipo con el mismo nombre
    for (Equipo equipoBD : equipos) {
        if (equipoBD.getNombre().equals(nombre)) {
            throw new EquipoServiceException("Ya existe un equipo con el mismo nombre.");
        }
    }

    Equipo equipoNuevo = new Equipo(nombre);
    equipoNuevo = equipoRepository.save(equipoNuevo);
    return modelMapper.map(equipoNuevo, EquipoData.class);
}
````

Junto a su test correspondiente:  
````
@Test
public void excepcionesCrearEquipo() {
    equipoService.crearEquipo("Proyecto");

    assertThatThrownBy(() -> equipoService.crearEquipo(""))
            .isInstanceOf(EquipoServiceException.class);
    assertThatThrownBy(() -> equipoService.crearEquipo("Proyecto"))
            .isInstanceOf(EquipoServiceException.class);
}
````

##### Segundo Test - Usuario puede abandonar un equipo correctamente

En el segundo commit he creado un método en el servicio de los equipos para que un usuario concreto pueda abandonar el equipo:  
````
@Transactional
public void abandonarEquipo(Long equipoId, Long usuarioId) {
    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
    Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

    if (equipo == null || usuario == null) {
        throw new EquipoServiceException("No se puede abandonar el equipo. Equipo o usuario no encontrados.");
    }

    equipo.removeUsuario(usuario);
}
````

Para que esto funcione correctamente hay que eliminar al usuario del equipo, pero también hay que eliminar al equipo del usuario.  
Equipo.java:  
````
public void removeUsuario(Usuario usuario) {
    if (!usuarios.contains(usuario)) return;
    usuarios.remove(usuario);
    usuario.removeEquipo(this); // Actualiza la relación inversa
}
````

Usuario.java:  
````
public void removeEquipo(Equipo equipo) {
    if (!equipos.contains(equipo)) return;
    equipos.remove(equipo);
}
````

Para terminar he creado el siguiente test para comprobar que este método funciona correctamente:  
````
@Test
public void abandonarEquipo() {
    // GIVEN
    UsuarioData usuario = new UsuarioData();
    usuario.setEmail("user@ua");
    usuario.setPassword("123");
    usuario = usuarioService.registrar(usuario);
    EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
    equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

    // WHEN
    equipoService.abandonarEquipo(equipo.getId(), usuario.getId());

    // THEN
    assertThatThrownBy(() -> equipoService.usuariosEquipo(equipo.getId()))
            .isInstanceOf(EquipoServiceException.class);
}
````

#### Controller y vista

##### Primer Test - Crear un nuevo equipo

En este primer commit he añadido a la vista /equipos un botón para poder un equipo introduciendo su nombre, dará error si el nombre del equipo ya existe:  
````
<div class="row mt-3">
    <div class="col">
        <h2>Crear Equipo</h2>
        <form th:action="@{/equipos/crear}" method="post">
            <div class="form-group">
                <input type="text" class="form-control" id="nombreEquipo" name="nombreEquipo" placeholder="Nombre del equipo" required>
            </div>
            <button type="submit" class="btn btn-success">Crear Equipo</button>
        </form>
    </div>
</div>

<div th:if="${errorCrearEquipo}">
    <div class="alert alert-danger" th:text="${errorCrearEquipo}"></div>
</div>
````

Este botón está controlado en el siguiente método del EquipoController, donde se comprueba si el equipo ya existe o si el nombre es incorrecto:  
````
@PostMapping("/equipos/crear")
public String crearEquipo(@RequestParam("nombreEquipo") String nombreEquipo, Model model, HttpSession session) {
    Long id = managerUserSession.usuarioLogeado();

    if (id == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(id);
    model.addAttribute("usuario", usuario);

    // Validación de nombre de equipo no vacío
    if (nombreEquipo == null || nombreEquipo.trim().isEmpty()) {
        model.addAttribute("errorCrearEquipo", "El nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        return "equipos";
    }

    try {
        // Intenta crear el equipo
        equipoService.crearEquipo(nombreEquipo);
    } catch (EquipoServiceException e) {
        model.addAttribute("errorCrearEquipo", e.getMessage());
    }

    List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
    model.addAttribute("equipos", equipos);

    return "equipos";
}
````

Junto con su @Test correspondiente:  
````
@Test
public void crearEquipo() throws Exception {
    UsuarioData usuario = new UsuarioData();
    usuario.setId(1L);
    usuario.setNombre("noel");
    usuario.setEmail("noel@gmail.com");
    usuario.setPassword("noel");

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Equipo 1");

    List<EquipoData> equipos = new ArrayList<>();
    equipos.add(equipo);

    when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
    when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

    when(equipoService.crearEquipo(equipo.getNombre())).thenReturn(equipo);
    when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);

    mockMvc.perform(post("/equipos/crear")
                    .param("nombreEquipo", equipo.getNombre()))
            .andExpect(status().isOk())
            .andExpect(view().name("equipos"))
            .andExpect(content().string(containsString("Equipo 1")));
}
````

##### Segundo Test - Usuario puede unirse a un equipo

En este segundo commit he comenzado añadiendo un botón en la lista de equipos para poder unirse a dicho equipo. Si ya perteneces al equipo saldrá un mensaje de aviso y no te unirás.  
````
<div th:if="${errorUnirse}">
    <div class="alert alert-danger" th:text="${errorUnirse}"></div>
</div>

<div th:if="${correcto}">
    <div class="alert alert-success" th:text="${correcto}"></div>
</div>

<div class="row mt-3">
    <div class="col">
        <table class="table table-striped">
                <td>
                    <a class="btn btn-primary btn-xs" th:href="@{'/equipos/' + ${equipo.id} + '/usuarios'}">Usuarios</a>
                </td>
                <td>
                    <form th:action="@{'/equipos/' + ${equipo.id} + '/unirse'}" method="post">
                        <button type="submit" class="btn btn-success">Unirse</button>
                    </form>
                </td>
````

Además a esta vista html también he añadido un código de javascript para hacer desaparecer las alertas a los 5 segundos de estar presentes:  
````
<script th:inline="javascript">
    /* Función para ocultar alertas después de 5 segundos */
    function hideAlerts() {
        setTimeout(function() {
            var alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(alert) {
                alert.style.display = 'none';
            });
        }, 5000); // 5000 milisegundos = 5 segundos
    }

    // Llama a la función al cargar la página
    document.addEventListener('DOMContentLoaded', function() {
        hideAlerts();
    });
</script>
````

El botón se controla por el siguiente método en el EquipoController:  
````
@PostMapping("/equipos/{equipoId}/unirse")
public String unirseAEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    // Obtén el usuario y el equipo correspondientes
    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
        model.addAttribute("errorUnirse", "Ya perteneces a este equipo.");
    } else {
        equipoService.añadirUsuarioAEquipo(equipoId, idUsuario);
        model.addAttribute("correcto", "Te has unido al equipo.");
    }

    List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
    model.addAttribute("equipos", equipos);
    return "equipos";
}
````

Junto a su @Test:  
````
@Test
public void unirseAEquipo() throws Exception {
    UsuarioData usuario = new UsuarioData();
    usuario.setId(1L);
    usuario.setNombre("noel");
    usuario.setEmail("noel@gmail.com");
    usuario.setPassword("noel");

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Nuevo Equipo");

    List<EquipoData> equipos = new ArrayList<>();
    equipos.add(equipo);

    when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
    when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

    when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(new ArrayList<>());
    when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);


    mockMvc.perform(post("/equipos/" + equipo.getId() + "/unirse"))
            .andExpect(status().isOk())
            .andExpect(view().name("equipos"))
            .andExpect(content().string(containsString("Te has unido al equipo.")));
}
````

##### Tercer Test - Un usuario puede abandonar un equipo al que pertenece

En este último commit de la historia de usuario he añadido un botón de igual manera que el botón de unirse pero ahora para abandoran el equipo. También
salen mensajes de error si no perteneces al equipo.  
````
<td>
    <form th:action="@{'/equipos/' + ${equipo.id} + '/abandonar'}" method="post">
        <button type="submit" class="btn btn-danger">Abandonar</button>
    </form>
</td>
````

Controlado por su método en el EquipoController:  
````
@PostMapping("/equipos/{equipoId}/abandonar")
public String abandonarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
        equipoService.abandonarEquipo(equipoId, idUsuario);
        model.addAttribute("correctoAbandono", "Has abandonado el equipo.");
    } else {
        model.addAttribute("errorAbandonar", "No perteneces a este equipo.");
    }

    List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
    model.addAttribute("equipos", equipos);
    return "equipos";
}
````

Junto con el último test de la historia:  
````
@Test
public void abandonarEquipo() throws Exception {
    UsuarioData usuario = new UsuarioData();
    usuario.setId(1L);
    usuario.setNombre("noel");
    usuario.setEmail("noel@gmail.com");
    usuario.setPassword("noel");

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Nuevo Equipo");

    List<EquipoData> equipos = new ArrayList<>();
    equipos.add(equipo);

    List<UsuarioData> usuarios = new ArrayList<>();
    usuarios.add(usuario);

    when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
    when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

    when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(usuarios);
    when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);


    mockMvc.perform(post("/equipos/" + equipo.getId() + "/abandonar"))
            .andExpect(status().isOk())
            .andExpect(view().name("equipos"))
            .andExpect(content().string(containsString("Has abandonado el equipo.")));
}
````



### Gestión de equipos

#### Servicio y modelo

##### Primer Test - Método para cambiar el nombre y su test correspondiente

En este commit he comenzado con el método para que el admin pueda cambiar el nombre a un determinado equipo. Para ello empiezo añadiendo un setter en el Equipo.java:  
````
public void setNombre(String nombre) {
    this.nombre = nombre;
}
````

Con este método he podido crear correctamente el método en el EquipoService para cambiar el nombre:  
````
@Transactional
public EquipoData cambiarNombreEquipo(Long equipoId, String nuevoNombre) {
    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

    if (equipo == null) {
        throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
    }

    List<Equipo> equipos = equipoRepository.findAllByOrderByNombreAsc().stream()
            .filter(e -> e.getNombre().equals(nuevoNombre))
            .collect(Collectors.toList());

    // La lista equipo es una lista donde están los equipos que tiene el mismo nombre que el nuevoNombre
    if (!equipos.isEmpty()) {
        throw new EquipoServiceException("Ya existe un equipo con el nombre " + nuevoNombre + ".");
    }

    equipo.setNombre(nuevoNombre);

    equipo = equipoRepository.save(equipo);

    return modelMapper.map(equipo, EquipoData.class);
}
````

Junto a su test correspondiente:  
````
@Test
public void testCambiarNombreEquipo() {
    // GIVEN
    EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
    equipoService.cambiarNombreEquipo(equipo1.getId(), "Nuevo Proyecto");

    // WHEN
    List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

    // THEN
    assertThat(equipos).hasSize(1);
    assertThat(equipos.get(0).getNombre()).isEqualTo("Nuevo Proyecto");
}
````

##### Segundo Test - Probar las excepciones del método Cambiar Nombre

En este commit simplemente he comprobado que se lanzan correctamente las excepciones del método de cambiar nombre:  
````
@Test
public void excepcionesCambiarNombreEquipo() {
    // GIVEN
    EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
    equipoService.crearEquipo("Nuevo Proyecto");

    // THEN
    // Verificar la excepción cuando el equipo no existe en la base de datos
    assertThatThrownBy(() -> equipoService.cambiarNombreEquipo(100L, "Nombre Cambiado"))
            .isInstanceOf(EquipoServiceException.class);

    // Verificar la excepción cuando se intenta cambiar a un nombre que ya existe en otro equipo
    assertThatThrownBy(() -> equipoService.cambiarNombreEquipo(equipo.getId(), "Nuevo Proyecto"))
            .isInstanceOf(EquipoServiceException.class);
}
````

##### Tercer Test - Método para eliminar un equipo y su test asociado

Aquí simplemente he creado el método correspondiente para que el admin pueda eliminar a un determinado equipo. A parte de esto, hay que eliminar también al equipo de todos sus
usuarios relacionados, y entonces borrar el equipo:    
````
 @Transactional
public void eliminarEquipo(Long equipoId) {
    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

    if (equipo == null) {
        throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
    }

    // Eliminamos el equipo de los usuarios asociados
    for (Usuario usuario : equipo.getUsuarios()) {
        usuario.removeEquipo(equipo);
    }

    equipoRepository.delete(equipo);
}
````

Con su @Test correspondiente:  
````
@Test
public void eliminarEquipo() {
    // GIVEN
    EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
    equipoService.eliminarEquipo(equipo.getId());

    // WHEN
    List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

    // THEN
    assertThat(equipos).hasSize(0);
}
````

#### Controller y vista

##### Primer Test - Creado botón para administrar, con su método en el controller y una nueva vista; el test comprueba que salga la vista

Antes de nada he añadido a todos los métodos del EquipoController una comprobación para comprobar si el usuario logueado es admin o no:    
````
if (usuario != null) {
    boolean esAdmin = usuario.isAdmin();
    model.addAttribute("esAdmin", esAdmin);
}
````

En la vista de /equipos he añadido un botón de Administrar un equipo que solo se hace visible cuando el usuario logueado es admin:    
````
<td th:if="${esAdmin}">
    <a class="btn btn-info btn-xs" th:href="@{'/equipos/' + ${equipo.id} + '/administrar'}">Administrar</a>
</td>
````

Luego este botón se controla en el siguiente método del EquipoController:  
````
@GetMapping("/equipos/{equipoId}/administrar")
public String administrarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    // Verificar si el usuario es administrador antes de permitir la administración del equipo
    if (!usuario.isAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }

    EquipoData equipo = equipoService.recuperarEquipo(equipoId);
    model.addAttribute("equipo", equipo);

    return "administracionEquipo";
}
````

Que redirecciona a una nueva vista /administracionEquipo:  
````
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:replace="fragments :: head (titulo='Administración de Equipo')"></head>

<body>

<div th:replace="fragments :: navbar(userName=${usuario} ? ${usuario.getNombre()} : '',
                     userId=${usuario} ? ${usuario.getId()} : '')"></div>

<div class="container-fluid">

    <div class="row mt-3">
        <div class="col">
            <h2>Administración de Equipo</h2>
        </div>
    </div>
</div>

<div th:replace="fragments::javascript"/>

</body>
</html>
````

Para comprobar el funcionamiento he creado el siguiente @Test:  
````
@Test
public void administrarEquipo() throws Exception {
    UsuarioData usuario = new UsuarioData();
    usuario.setId(1L);
    usuario.setNombre("admin");
    usuario.setEmail("admin@gmail.com");
    usuario.setPassword("admin");
    usuario.setAdmin(true);

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Equipo Admin");

    when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
    when(usuarioService.findById(usuario.getId())).thenReturn(usuario);
    when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);

    mockMvc.perform(get("/equipos/" + equipo.getId() + "/administrar"))
            .andExpect(status().isOk())
            .andExpect(view().name("administracionEquipo"))
            .andExpect(content().string(containsString("Administración de Equipo")));
}
````

##### Segundo Test - administrador puede cambiar correctamente el nombre deun equipo

En la vista de /administracionEquipo he añadido un botón con un input de texto para que el admin pueda cambiar de nombre al equipo. Si el nombre ya existe en la base de datos se mostrará un mensaje de error:   
````
<div class="row mt-3">
    <div class="col">
        <h2>Administración de Equipo</h2>
        <h2 th:text="'Administración del equipo: ' + ${equipo.nombre}"></h2>
    </div>
</div>

<div class="row mt-3">
    <div class="col">
        <div th:if="${errorCambiarNombre}">
            <div class="alert alert-danger" th:text="${errorCambiarNombre}"></div>
        </div>
        <div th:if="${correctoCambioNombre}">
            <div class="alert alert-success" th:text="${correctoCambioNombre}"></div>
        </div>
        <br>
        <form th:action="@{'/equipos/' + ${equipoId} + '/cambiarNombre'}" method="post">
            <div class="form-group">
                <input type="text" class="form-control" id="nuevoNombre" name="nuevoNombre" placeholder="Nuevo Nombre del Equipo" required>
            </div>
            <button type="submit" class="btn btn-success">Cambiar Nombre</button>
        </form>
    </div>
</div>
````

El botón está controlado por el siguiente método, donde se comprueba otra vez que el usuario sea admin, luego comprueba que el nombre sea correcto:    
````
@PostMapping("/equipos/{equipoId}/cambiarNombre")
public String cambiarNombreEquipo(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevoNombre") String nuevoNombre, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    if (!usuario.isAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }

    if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
        model.addAttribute("errorCambiarNombre", "El nuevo nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
    } else {
        try {
            equipoService.cambiarNombreEquipo(equipoId, nuevoNombre);
            model.addAttribute("correctoCambioNombre", "El nombre del equipo se ha cambiado con éxito.");
        } catch (EquipoServiceException e) {
            model.addAttribute("errorCambiarNombre", e.getMessage());
        }
    }

    EquipoData equipo = equipoService.recuperarEquipo(equipoId);
    model.addAttribute("equipo", equipo);

    return "administracionEquipo";
}
````

Todo esto comprobado por el siguiente @Test:  
````
@Test
public void cambiarNombreEquipo() throws Exception {
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setNombre("admin");
    admin.setEmail("admin@gmail.com");
    admin.setPassword("admin");
    admin.setAdmin(true);

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Equipo Original");

    when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
    when(usuarioService.findById(admin.getId())).thenReturn(admin);

    when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
    when(equipoService.cambiarNombreEquipo(equipo.getId(), "Nuevo Nombre")).thenReturn(equipo);

    mockMvc.perform(post("/equipos/1/cambiarNombre")
                    .param("nuevoNombre", "Nuevo Nombre"))
            .andExpect(status().isOk())
            .andExpect(view().name("administracionEquipo"))
            .andExpect(content().string(containsString("Nuevo Nombre")));
}
````

##### Tercer Test - Administrador puede eliminar correctamente equipos

En este último commit de la historia de usuario he añadido un botón de igual manera que el botón de cambiar el nombre, pero esta vez para eliminar el equipo:  
````
<div class="row mt-3">
    <div class="col">
        <form th:action="@{'/equipos/' + ${equipoId} + '/eliminar'}" method="post">
            <button type="submit" class="btn btn-danger">Eliminar Equipo</button>
        </form>
    </div>
</div>
````

Esta vez el método tiene 2 posibles finalizaciones, que haya un error y el admin se quede en la página de administración o que el equipo se elimine correctamente y el admin sea
redirigido a la vista del listado de equipos donde podrá ver que el equipo ya no se encuentra en la lista, puediendolo crear otra vez:  
````
@PostMapping("/equipos/{equipoId}/eliminar")
public String eliminarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
    Long idUsuario = managerUserSession.usuarioLogeado();

    if (idUsuario == null) {
        throw new UsuarioNoLogeadoException();
    }

    UsuarioData usuario = usuarioService.findById(idUsuario);
    model.addAttribute("usuario", usuario);

    if (!usuario.isAdmin()) {
        throw new UsuarioNoAutorizadoException();
    }

    try {
        equipoService.eliminarEquipo(equipoId);

        boolean esAdmin = usuario.isAdmin();
        model.addAttribute("esAdmin", esAdmin);

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "redirect:/equipos";

    } catch (EquipoServiceException e) {
        model.addAttribute("errorEliminacion", e.getMessage());
        return "administracionEquipo";
    }
}
````

Junto con el último test de la historia:  
````
@Test
public void eliminarEquipoAdmin() throws Exception {
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setNombre("admin");
    admin.setEmail("admin@gmail.com");
    admin.setPassword("admin");
    admin.setAdmin(true);

    EquipoData equipo = new EquipoData();
    equipo.setId(1L);
    equipo.setNombre("Equipo");

    when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
    when(usuarioService.findById(admin.getId())).thenReturn(admin);

    when(equipoService.findAllOrdenadoPorNombre()).thenReturn(new ArrayList<>());

    mockMvc.perform(post("/equipos/" + equipo.getId() + "/eliminar"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/equipos")) //
            .andExpect(content().string(not(containsString("Equipo"))));
}
````



### BASE DE DATOS

![image](https://github.com/mads-ua-23-24/mads-todolist-nmp50-ua/assets/78731028/4627758d-7729-4638-a9d7-0af4809a34f4)  
![image](https://github.com/mads-ua-23-24/mads-todolist-nmp50-ua/assets/78731028/11335076-eec7-4f64-99a2-f0ab0e99435f)  
![image](https://github.com/mads-ua-23-24/mads-todolist-nmp50-ua/assets/78731028/669134dd-abe2-4888-8889-6bc9a2008f1b)  



### LINKS

Trello público: https://trello.com/b/VQoo3uP6/todolist-mads  
Docker Hub: https://hub.docker.com/r/noelmartinnez/spring-boot-demoapp  
GitHub: https://github.com/mads-ua-23-24/mads-todolist-nmp50-ua  
