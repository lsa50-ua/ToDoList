# Práctica 2
## NOEL MARTÍNEZ POMARES 48771960T

### MODEL Y DTO

En el fichero Usuario.java he añadido dos nuevos atributos booleanos, uno de ellos para indicar si el usuario es un administrador (sólo puede haber uno) y el otro para saber si ese usuario está bloqueado o no.
```
@Column(name = "admin")
private boolean admin = false;

@Column(name = "bloqueado")
private boolean bloqueado = false;
```

También he añadido sus respectivos setters y getters:
```
public boolean isAdmin() {
    return admin;
}

public void setAdmin(boolean estado) {
    admin = estado;
}

public boolean isBloqueado() {
    return bloqueado;
}

public void setBloqueado(boolean estado) {
    bloqueado = estado;
}
```

En los DTO UsuarioData.java y RegistroData.java también he añadido estos atributos admin y bloqueado, incluyendo sus getters y setters. Los atributos esta vez no los he inicializado. Estos atributos en los DTO son útiles para el registro de los usuarios, su login y demás funciones.



### EXCEPTION

He creado 2 nuevas excepciones.  
* UsuarioNoAutorizadoException para sólo permitir el acceso del admin a la URL /registrados y /registrados/{id}:  
```
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Usuario no es admin.")
public class UsuarioNoAutorizadoException extends RuntimeException {
}
```

* UsuarioBloqueadoException para cuando el usuario esté bloqueado:  
```
@ResponseStatus(value = HttpStatus.LOCKED, reason="Usuario bloqueado por el administrador.")
public class UsuarioBloqueadoException extends RuntimeException {
}
```



### SERVICE

En el UsuarioService he añadido un nuevo tipo de LoginStatus "USUARIO_BLOQUEADO" a su enumerado.  
He actualizado los métodos login() y registrar().  

* login() -> he añadido una comprobación más para saber si el usuario está bloqueado o no:  
```
} else if (usuario.get().isBloqueado()) {
    return LoginStatus.USUARIO_BLOQUEADO; // AÑADIDO PARA INDICAR QUE EL USUARIO ESTÁ BLOQUEADO
} 
```

* registrar() -> he añadido un linea de código para que al nuevo usuario que se va a crear en la base de datos se le asigne el valor false en el atributo "bloqueado":  
```
else {
    Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
    usuarioNuevo.setBloqueado(usuario.isBloqueado()); // AÑADIDO PARA PODER CREAR AL USUARIO CON EL ATRIBUTO "BLOQUEADO"
    usuarioNuevo = usuarioRepository.save(usuarioNuevo);
    return modelMapper.map(usuarioNuevo, UsuarioData.class);
}
```


He añadido dos nuevos métodos.  
* actualizar() -> este método cambia el estado del atributo "bloqueado" a un usuario concreto y lo actualiza en la bd:  
```
@Transactional
public UsuarioData actualizarUsuario(UsuarioData usuarioData) {
    // Verifica si el usuario existe en la base de datos
    Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuarioData.getId());

    if (!usuarioExistente.isPresent()) {
        throw new UsuarioServiceException("El usuario con ID " + usuarioData.getId() + " no existe en la base de datos");
    }

    // Actualiza los campos relevantes del usuario existente con los datos proporcionados en usuarioData
    Usuario usuarioActualizado = usuarioExistente.get();
    usuarioActualizado.setBloqueado(usuarioData.isBloqueado());

    // Guarda los cambios en la base de datos
    usuarioActualizado = usuarioRepository.save(usuarioActualizado);

    // Devuelve el usuario actualizado en forma de UsuarioData
    return modelMapper.map(usuarioActualizado, UsuarioData.class);
}
```

* listadoCompleto() -> este método devuelve una lista completa de todos los usuarios que hay en la base de datos.  
```
@Transactional(readOnly = true)
public List<Usuario> listadoCompleto(){
    return (List<Usuario>) usuarioRepository.findAll();
}
```



### CONTROLLER

* HomeController: simplemente he creado el método que nos ayuda a mostrar el /about, comprobando si ha iniciado sesión o no para luego mostrar una navbar u otra:  
```
@GetMapping("/about")
public String about(Model model, HttpSession session) {
    // Obtenemos el id del usuario en sesión para comprobar si está logueado o no
    Long id = managerUserSession.usuarioLogeado();

    if(id != null){
        // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
        UsuarioData user = usuarioService.findById(id);
        // "usuario" lo usaremos en la vista html
        model.addAttribute("usuario", user);
    }

    // si no está logueado, se mostrará el navbar de no estar logueado
    return "about";
}
```

* LoginController: he cambiado el método POST del login, para que se compruebe si el usuario está bloqueado o no y también para comprobar si el usuario es un admin, en caso afirmativo se redirige el usuario a la URL /registrados y no a las tareas del usuarios:  
```
// Verificar si el usuario está bloqueado
if (usuario != null && usuario.isBloqueado()) {
    throw new UsuarioBloqueadoException();
}

managerUserSession.logearUsuario(usuario.getId());

// Si es admin se redirecciona al listado de usuarios
if (usuario.isAdmin()) {
    return "redirect:/registrados";
}
else{
    return "redirect:/usuarios/" + usuario.getId() + "/tareas";
}
```
```
else if (loginStatus == UsuarioService.LoginStatus.USUARIO_BLOQUEADO) {
    model.addAttribute("error", "Usuario Bloqueado.");
    return "formLogin";
}
```

En el método GET del /registro simplemente ha hecho una comprobación para saber si existe algún admin en la base de datos, pues en la vista html debemos saber si mostrar el botón para conversirte admin o no:  
```
boolean admin = false;

List<Usuario> listaUsuarios = usuarioService.listadoCompleto();

// Buscamos en el listado completo de usuarios si hay alguno de ellos que sea admin
for(int i = 0; i < listaUsuarios.size() ; i++) {
    if(listaUsuarios.get(i).isAdmin()){
        admin = true;
        break;
    }
}

model.addAttribute("admin", admin);
```

En el método POS del /registro simplemente he añadido 2 lineas, una para asignarle el nuevo valor del atributo "bloqueado" y otra para el "admin":  
```
usuarioData.setAdmin(registroData.isAdmin());
usuarioData.setBloqueado(false);
```


También he creado un nuevo Controller para la lista de registrados y sus detalles.  
* RegistradosController: primero he creado un método privado del controller para comprobar si el usuario que está introduciendose en la URL es admin o no, lanzando excepción en caso negativo:  
```
private void comprobarAdmin(Long idUsuario) {
    if (idUsuario != null) {
        UsuarioData user = usuarioService.findById(idUsuario);
        if (user != null && !user.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }
    } else {
        throw new UsuarioNoLogeadoException();
    }
}
```

He creado el método GET para devolver la lista de registrados, donde compruebo que el usuario esté logueado y sea admin, y luego obtengo la lista completa de usuarios y se la paso a la vista html:  
```
@GetMapping("/registrados")
public String listadoRegistrados(Model model, HttpSession session) {
    Long id = managerUserSession.usuarioLogeado();

    comprobarAdmin(id);

    // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
    UsuarioData user = usuarioService.findById(id);
    // "usuario" lo usaremos en la vista html
    model.addAttribute("usuario", user);

    List<Usuario> registrados = usuarioService.listadoCompleto();

    model.addAttribute("registrados", registrados);
    return "listaRegistrados";
}
```

El método GET para los detalles es muy parecido al anterior, solo que ahora una vez que hemos obtenido el listado de usuarios, buscamos por id al usuario concreto que queremos:  
```
@GetMapping("/registrados/{id}")
public String descripcionRegistrado(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
    Long id = managerUserSession.usuarioLogeado();

    comprobarAdmin(id);

    // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
    UsuarioData user = usuarioService.findById(id);
    // "logueado" lo usaremos en la vista html
    model.addAttribute("logueado", user);

    List<Usuario> registrados = usuarioService.listadoCompleto();
    Usuario usuario = usuarioService.buscarUsuarioPorId(registrados, idUsuario);

    model.addAttribute("usuario", usuario);
    return "descripcionUsuario";
}
```

Por último he creado dos métodos POST para poder bloquear y desbloquear a los usuarios al iniciar sesión, estos métodos cambiand el valor del atributo "bloqueado" al usuario y lo actualizan en la bd:  
```
@PostMapping("/registrados/bloquear/{id}")
public String bloquearUsuario(@PathVariable(value="id") Long idUsuario) {
    UsuarioData usuario = usuarioService.findById(idUsuario);

    if (usuario != null) {
        usuario.setBloqueado(true);
        usuarioService.actualizarUsuario(usuario);
    }

    return "redirect:/registrados";
}
```
```
@PostMapping("/registrados/desbloquear/{id}")
public String desbloquearUsuario(@PathVariable(value="id") Long idUsuario) {
    UsuarioData usuario = usuarioService.findById(idUsuario);

    if (usuario != null) {
        usuario.setBloqueado(false);
        usuarioService.actualizarUsuario(usuario);
    }

    return "redirect:/registrados";
}
```



### TEMPLATES

* about.html: de esta vista destaco el fragmento de código donde se hace la distinción de si el usuario está logueado o no, mostrando así una navbar u otra:  
```
<div th:if="${usuario != null}">
<!-- Recuperamos el objeto "usuarios" que nos pasa el Controller y usamos sus atributos, pasandolos al NavBar -->
<div th:replace="fragments :: navbar(userName=${usuario} ? ${usuario.getNombre()} : '',
                 userId=${usuario} ? ${usuario.getId()} : '')"></div>
</div>
<div th:if="${usuario == null}">
    <div th:replace="fragments :: navbar_noLogueado"></div>
</div>
```

* descripcionUsuario.html: vista donde se muestran los datos de un usuario concreto:  
```
<thead>
<tr>
    <th>Id</th>
    <th>Correo Electrónico</th>
    <th>Nombre</th>
    <th>Fecha de nacimiento</th>
</tr>
</thead>
<tbody>
<tr>
    <td th:text="${usuario.id}"></td>
    <td th:text="${usuario.email}"></td>
    <td th:text="${usuario.nombre}"></td>
    <td th:text="${usuario.fechaNacimiento}"></td>
</tr>
</tbody>
```

* formRegistro.html: aquí he añadido la checkbox para poder convertirse en admin:  
```
<div th:if="${admin == false}">
    <div class="form-group">
        <label for="admin">Admin: </label>
        <input id="admin" type="checkbox" name="admin"/>
    </div>
</div>
```

* fragments.html: he creado las diferentes navbars a mostrar, pues se comparten para varias vistas.  

* listaRegistrados.html: he creado esta nueva vista donde se muestra la lista de usuarios registrados y donde puedo destacar los 3 botónes que contiene, el primero de ellos es para poder ir a la descripción del usuarios, y los otros dos son para bloquear y desbloquear al usuario; si el botón que se muestra es el de bloquear, el de desbloquear no se mostrará hasta que el usuario esté bloqueado y viceversa:  
```
<td>
    <a class="btn btn-primary btn-xs" th:href="@{/registrados/{id}(id=${registrado.id})}"/>Desc</a>
</td>
<td th:if="${registrado.admin == false and registrado.isBloqueado() == true}">
    <form th:action="@{/registrados/desbloquear/{id}(id=${registrado.id})}" method="post">
        <button type="submit" class="btn btn-block btn-xs btn-success">
            Desbloquear
        </button>
    </form>
</td>
<td th:if="${registrado.admin == false and registrado.isBloqueado() == false}">
    <form th:action="@{/registrados/bloquear/{id}(id=${registrado.id})}" method="post">
        <button type="submit" class="btn btn-block btn-xs btn-danger">
            Bloquear
        </button>
    </form>
</td>
```



### TESTs

#### Controller

* AcercaDeWebTest: primero he creado un método privado para añadir un usuario a la base de datos:  
```
Map<String, Long> addUsuarioBD() {
// Añadimos un usuario a la base de datos
UsuarioData usuario = new UsuarioData();
usuario.setEmail("noel@gmail.com");
usuario.setPassword("noel");
usuario.setNombre("noel");
usuario = usuarioService.registrar(usuario);

// Devolvemos el id del usuario
Map<String, Long> id = new HashMap<>();
id.put("usuarioId", usuario.getId());
return id;
}
```

Luego he creado dos tests para asegurarme de que el GET de la página about se muestra correctamente:  
```
@Test
public void getAboutDevuelveNombreAplicacion() throws Exception {
this.mockMvc.perform(get("/about"))
        .andExpect(content().string(containsString("ToDoList")));
}

@Test
public void getAboutDevuelveVersionAplicacion() throws Exception {
this.mockMvc.perform(get("/about"))
        .andExpect(content().string(containsString("Versión")));
}
```

Por último he creado dos tests para comprobar que se muestra una navbar diferente dependiendo de si el usuario está logueado o no:  
```
@Test
public void testNavBarNoLogueado() throws Exception {

when(managerUserSession.usuarioLogeado()).thenReturn(null);

this.mockMvc.perform(get("/about"))
        .andExpect(content().string(containsString("Registro")));
}

@Test
public void testNavBarLogueado() throws Exception {
Long usuarioId = addUsuarioBD().get("usuarioId");

// Moqueamos el método usuarioLogeado para que devuelva el usuario 1L,
// el mismo que se está usando en la petición. De esta forma evitamos
// que salte la excepción de que el usuario que está haciendo la
// petición no está logeado.
when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

// Realiza una solicitud GET a /about
this.mockMvc.perform(get("/about"))
        .andExpect(content().string(containsString("noel")));
}
```


* RegistradosWebTest: he dividido este fichero de Tests en los tests para comprobar el funcionamiento de la lista de registrados y por otra parte, para comprobar el funcionamiento de la página de descripción de los usuarios.
Para el listado de usuarios he creado 3 Tests, el primero de ellos prueba que un usuario no logueado no puede ver el listado de usuarios, el segundo de ellos prueba que un usuario que no sea admin no puede el listado de usuarios, y el último de ellos prueba que un usuario que sea admin si que puede ver el listado de usuarios correctamente:  
```
@Test
public void usuarioNoLogeadoNoPuedeVerListado() throws Exception {
    when(managerUserSession.usuarioLogeado()).thenReturn(null);

    when(usuarioService.listadoCompleto()).thenThrow(new UsuarioNoLogeadoException());

    mockMvc.perform(get("/registrados"))
            .andExpect(status().isUnauthorized());
}

@Test
public void usuarioNoAdminNoPuedeVerListado() throws Exception {
    UsuarioData usuarioNoAdmin = new UsuarioData();
    usuarioNoAdmin.setId(1L);
    usuarioNoAdmin.setNombre("usuario");
    usuarioNoAdmin.setEmail("usuario@gmail.com");
    usuarioNoAdmin.setPassword("usuario");
    usuarioNoAdmin.setAdmin(false);

    when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNoAdmin.getId());

    // Configuro el comportamiento del servicio para lanzar una excepción de tipo UsuarioNoAutorizadoException
    when(usuarioService.listadoCompleto()).thenThrow(new UsuarioNoAutorizadoException());

    mockMvc.perform(get("/registrados"))
            .andExpect(status().isUnauthorized());
}

@Test
public void adminPuedeVerListado() throws Exception {
    UsuarioData admin = new UsuarioData();
    admin.setId(2L);
    admin.setNombre("admin");
    admin.setEmail("admin@gmail.com");
    admin.setPassword("admin");
    admin.setAdmin(true);

    when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
    when(usuarioService.findById(admin.getId())).thenReturn(admin);

    // Simulo una lista de usuarios registrados
    List<Usuario> listaUsuarios = new ArrayList<>();
    Usuario usuarioRegistrado = new Usuario();
    usuarioRegistrado.setEmail("usuario@gmail.com");
    listaUsuarios.add(usuarioRegistrado);

    when(usuarioService.listadoCompleto()).thenReturn(listaUsuarios);

    mockMvc.perform(get("/registrados"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("usuario@gmail.com")));
}
```

Por otra parte he creado 3 tests para probar la descripción, el primero de ellos prueba que un usuario no logueado no puede ver la descripción, el segundo de ellos prueba que un usuario que no sea un admin no puede ver la descripción, y el último prueba que un usuario que si que es admin si que puede ver la descripción de los usuarios:  
```
@Test
public void usuarioNoLogueadoNoPuedeVerDescripcion() throws Exception {
    when(managerUserSession.usuarioLogeado()).thenReturn(null);

    this.mockMvc.perform(get("/registrados/1"))
            .andExpect(status().isUnauthorized());
}

@Test
public void noAdminNoPuedeVerDescripcion() throws Exception {
    when(managerUserSession.usuarioLogeado()).thenReturn(1L);

    UsuarioData usuarioRegistrado = new UsuarioData();
    usuarioRegistrado.setId(1L);
    usuarioRegistrado.setNombre("usuario");
    usuarioRegistrado.setEmail("usuario@gmail.com");
    usuarioRegistrado.setPassword("usuario");
    usuarioRegistrado.setAdmin(false);

    when(usuarioService.findById(1L)).thenReturn(usuarioRegistrado);

    this.mockMvc.perform(get("/registrados/1"))
            .andExpect(status().isUnauthorized());
}

@Test
public void adminPuedeVerDescripcion() throws Exception {
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setNombre("admin");
    admin.setEmail("admin@gmail.com");
    admin.setPassword("admin");
    admin.setAdmin(true);

    when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
    when(usuarioService.findById(admin.getId())).thenReturn(admin);

    Usuario usuarioRegistrado = new Usuario();
    usuarioRegistrado.setId(2L);
    usuarioRegistrado.setNombre("usuario");
    usuarioRegistrado.setEmail("usuario@gmail.com");
    usuarioRegistrado.setPassword("usuario");
    usuarioRegistrado.setAdmin(false);

    List<Usuario> listaUsuarios = new ArrayList<>();
    listaUsuarios.add(usuarioRegistrado);

    when(usuarioService.listadoCompleto()).thenReturn(listaUsuarios);
    when(usuarioService.buscarUsuarioPorId(listaUsuarios,2L)).thenReturn(usuarioRegistrado);

    this.mockMvc.perform(get("/registrados/2"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("usuario@gmail.com")));
}
```


* UsuarioWebTest: a este fichero de tests he añadido varios tests nuevos.
He creado dos tests para probar, en un caso que un admin puede bloquear a un usuario y este usuario no puede entonces iniciar sesión, y en otro caso todo lo contrario, que un admin puede desbloquear a un usuario:  
```
@Test
public void adminPuedeBloquearUsuarioYUsuarioNoPuedeIniciarSesion() throws Exception {
    // Crear un usuario administrador y otro usuario normal
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setAdmin(true);

    UsuarioData usuarioNormal = new UsuarioData();
    usuarioNormal.setId(2L);
    usuarioNormal.setAdmin(false);

    // Moquear la llamada a usuarioService.findById para devolver los usuarios creados
    when(usuarioService.findById(1L)).thenReturn(admin);
    when(usuarioService.findById(2L)).thenReturn(usuarioNormal);

    // Un administrador bloquea al usuario normal
    this.mockMvc.perform(post("/registrados/bloquear/2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/registrados"));

    // Verificar que el usuario normal está bloqueado
    UsuarioData usuarioBloqueado = usuarioService.findById(2L);
    assertTrue(usuarioBloqueado.isBloqueado());

    // Intentar iniciar sesión con el usuario bloqueado
    when(usuarioService.login(usuarioNormal.getEmail(), "contraseña")).thenReturn(UsuarioService.LoginStatus.USUARIO_BLOQUEADO);

    this.mockMvc.perform(post("/login")
                    .param("eMail", usuarioNormal.getEmail())
                    .param("password", "contraseña"))
            .andExpect(content().string(containsString("Usuario Bloqueado.")));
}

@Test
public void adminPuedeDesbloquearUsuarioYUsuarioPuedeIniciarSesion() throws Exception {
    // Crear un usuario administrador y otro usuario normal bloqueado
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setEmail("admin@gmail.com");
    admin.setAdmin(true);

    UsuarioData usuarioBloqueado = new UsuarioData();
    usuarioBloqueado.setId(2L);
    usuarioBloqueado.setAdmin(false);
    usuarioBloqueado.setEmail("noel@gmail.com");
    usuarioBloqueado.setBloqueado(true);

    // Moquear la llamada a usuarioService.findById para devolver los usuarios creados
    when(usuarioService.findById(1L)).thenReturn(admin);
    when(usuarioService.findById(2L)).thenReturn(usuarioBloqueado);

    // El administrador desbloquea al usuario bloqueado
    this.mockMvc.perform(post("/registrados/desbloquear/2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/registrados"));

    // Verificar que el usuario bloqueado está desbloqueado
    UsuarioData usuarioDesbloqueado = usuarioService.findById(2L);
    assertFalse(usuarioDesbloqueado.isBloqueado());

    when(usuarioService.findByEmail("admin@gmail.com")).thenReturn(admin);
    when(usuarioService.findByEmail("noel@gmail.com")).thenReturn(usuarioBloqueado);

    // Intentar iniciar sesión con el usuario desbloqueado
    when(usuarioService.login(usuarioBloqueado.getEmail(), "contraseña")).thenReturn(UsuarioService.LoginStatus.LOGIN_OK);

    this.mockMvc.perform(post("/login")
                    .param("eMail", usuarioBloqueado.getEmail())
                    .param("password", "contraseña"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/usuarios/2/tareas"));
}
```

También he probado que en el GET de /registro aparece el checkbox para poder ser admin cuando no existe ningún admin en la BD, y también he probado que este checkbox no aparece cuando si que existe un admin en la base de datos:  
```
@Test
public void comprobarApareceCheckboxAdmin() throws Exception {
    this.mockMvc.perform(get("/registro"))
            .andExpect(content().string(containsString("Admin: ")));
}

@Test
public void adminYaExistenteYNoApareceCheckboxEnRegistro() throws Exception {
    UsuarioData admin = new UsuarioData();
    admin.setId(1L);
    admin.setEmail("admin@gmail.com");
    admin.setPassword("admin");
    admin.setNombre("admin");
    admin.setAdmin(true);

    when(usuarioService.findByEmail("admin@gmail.com")).thenReturn(admin);
    when(usuarioService.registrar(admin)).thenReturn(admin);

    this.mockMvc.perform(post("/registro"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setEmail("admin@gmail.com");
    usuario.setPassword("admin");
    usuario.setNombre("admin");
    usuario.setAdmin(true);

    List<Usuario> listaUsuarios = new ArrayList<>();
    listaUsuarios.add(usuario);

    when(usuarioService.listadoCompleto()).thenReturn(listaUsuarios);

    this.mockMvc.perform(get("/registro"))
            .andExpect(content().string(not(containsString("Admin: "))));
}
```


#### Repository

* UsuarioTest: en este fichero de test he hecho tanto modificaciones, como he añadido nuevos tests.
He creado los siguientes tests para probar que se crear correctamente usuarios con los atributos nuevos  "admin" y "bloqueado":  
```
@Test
public void crearUsuarioAdmin() throws Exception {

    // GIVEN
    // Creado un nuevo usuario,
    Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

    // WHEN
    // actualizamos sus propiedades usando los setters,

    usuario.setNombre("Juan Gutiérrez");
    usuario.setPassword("12345678");

    // Compruebo la nueva propiedad
    usuario.setAdmin(true);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    usuario.setFechaNacimiento(sdf.parse("1997-02-20"));

    // THEN
    // los valores actualizados quedan guardados en el usuario y se
    // pueden recuperar con los getters.

    assertThat(usuario.getEmail()).isEqualTo("juan.gutierrez@gmail.com");
    assertThat(usuario.getNombre()).isEqualTo("Juan Gutiérrez");
    assertThat(usuario.getPassword()).isEqualTo("12345678");
    assertThat(usuario.getFechaNacimiento()).isEqualTo(sdf.parse("1997-02-20"));
    assertThat(usuario.isAdmin()).isEqualTo(true);
}

@Test
public void crearUsuarioBloqueado() throws Exception {

    // GIVEN
    // Creado un nuevo usuario,
    Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

    // WHEN
    // actualizamos sus propiedades usando los setters,

    usuario.setNombre("Juan Gutiérrez");
    usuario.setPassword("12345678");

    // Compruebo la nueva propiedad
    usuario.setBloqueado(true);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    usuario.setFechaNacimiento(sdf.parse("1997-02-20"));

    // THEN
    // los valores actualizados quedan guardados en el usuario y se
    // pueden recuperar con los getters.

    assertThat(usuario.getEmail()).isEqualTo("juan.gutierrez@gmail.com");
    assertThat(usuario.getNombre()).isEqualTo("Juan Gutiérrez");
    assertThat(usuario.getPassword()).isEqualTo("12345678");
    assertThat(usuario.getFechaNacimiento()).isEqualTo(sdf.parse("1997-02-20"));
    assertThat(usuario.isBloqueado()).isEqualTo(true);
}
```

También he modificado el siguiente test para que se pruebe también que el usuario se crea correctamente con los nuevos atributos seteados a true:  
```
@Test
@Transactional
public void crearUsuarioBaseDatos() throws ParseException {
    // GIVEN
    // Un usuario nuevo creado sin identificador

    Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
    usuario.setNombre("Juan Gutiérrez");
    usuario.setPassword("12345678");
    usuario.setBloqueado(true);
    usuario.setAdmin(true);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    usuario.setFechaNacimiento(sdf.parse("1997-02-20"));

    // WHEN
    // se guarda en la base de datos

    usuarioRepository.save(usuario);

    // THEN
    // se actualiza el identificador del usuario,

    assertThat(usuario.getId()).isNotNull();

    // y con ese identificador se recupera de la base de datos el usuario con
    // los valores correctos de las propiedades.

    Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);
    assertThat(usuarioBD.getEmail()).isEqualTo("juan.gutierrez@gmail.com");
    assertThat(usuarioBD.getNombre()).isEqualTo("Juan Gutiérrez");
    assertThat(usuarioBD.getPassword()).isEqualTo("12345678");
    assertThat(usuarioBD.getFechaNacimiento()).isEqualTo(sdf.parse("1997-02-20"));

    // Comprobamos que los nuevos atributos de los usuarios se guardan correctamente en la BD.
    assertThat(usuario.isBloqueado()).isEqualTo(true);
    assertThat(usuario.isAdmin()).isEqualTo(true);
}
```


#### Service

* UsuarioServiceTest: en este fichero de tests también he hecho tanto modificaciones, como he añadido nuevos tests.
Para empezar he creado un nuevo método privado que crea un usuario admin y lo registra en la BD:  
```
Long addUsuarioBloqueadoBD() {
    UsuarioData usuario = new UsuarioData();
    usuario.setEmail("noel@gmail.com");
    usuario.setNombre("noel");
    usuario.setPassword("noel");
    usuario.setBloqueado(true);
    UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
    return nuevoUsuario.getId();
}
```

He modificado el siguiente test para que se compruebe tambíen el nuevo LoginStatus que he creado en el UsuarioService.java:  
```
@Test
public void servicioLoginUsuario() {
    // GIVEN
    // Un usuario en la BD

    addUsuarioBD();
    addUsuarioBloqueadoBD();

    // WHEN
    // intentamos logear un usuario y contraseña correctos
    UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

    // intentamos logear un usuario correcto, con una contraseña incorrecta
    UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

    // intentamos logear un usuario que no existe,
    UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

    // intentamos logear un usuario que está bloqueado,
    UsuarioService.LoginStatus loginStatus4 = usuarioService.login("noel@gmail.com", "noel");

    // THEN

    // el valor devuelto por el primer login es LOGIN_OK,
    assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

    // el valor devuelto por el segundo login es ERROR_PASSWORD,
    assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

    // el valor devuelto por el tercer login es USER_NOT_FOUND,
    assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);

    // y el valor devuelto por el cuarto login es USUARIO_BLOQUEADO,
    assertThat(loginStatus4).isEqualTo(UsuarioService.LoginStatus.USUARIO_BLOQUEADO);
}
```

He añadido el siguiente test donde se comprueba que el método listadoCompleto() devuelve todos los usuarios que han sido registrados en la BD:  
```
@Test
public void servicioComprobarListadoCompleto() {
    // GIVEN
    // Dos usuario en la BD

    addUsuarioBD();
    addUsuarioBloqueadoBD();

    // WHEN
    // recuperamos el listado completo de registrados,

    List<Usuario> usuarios = usuarioService.listadoCompleto();

    // THEN
    // los usuarios son devueltos correctamente por el método.

    assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    assertThat(usuarios.get(1).getEmail()).isEqualTo("noel@gmail.com");
}
```

Por último, también he comprobado que le nuevo método actualizar(), funciona correctamente para actualizar el atributo "bloqueado" de los usuarios:  
```
@Test
public void servicioComprobarActualizarUsuarioBloqueado() {
    // GIVEN
    // Un usuario en la BD bloqueado

    Long idUsuario = addUsuarioBloqueadoBD();

    UsuarioData usuario = usuarioService.findById(idUsuario);

    // WHEN
    // cambiamos de estado su atributo "bloqueado",

    usuario.setBloqueado(false);
    usuarioService.actualizarUsuario(usuario);

    List<Usuario> usuarios = usuarioService.listadoCompleto();

    // THEN
    // los usuarios son devueltos correctamente por el método.

    assertThat(usuarios.get(0).getEmail()).isEqualTo("noel@gmail.com");
    assertThat(usuarios.get(0).isBloqueado()).isEqualTo(false);
}

@Test
public void servicioComprobarActualizarUsuarioNoBloqueado() {
    // GIVEN
    // Un usuario en la BD no bloqueado

    Long idUsuario = addUsuarioBD();

    UsuarioData usuario = usuarioService.findById(idUsuario);

    // WHEN
    // cambiamos de estado su atributo "bloqueado",

    usuario.setBloqueado(true);
    usuarioService.actualizarUsuario(usuario);

    List<Usuario> usuarios = usuarioService.listadoCompleto();

    // THEN
    // los usuarios son devueltos correctamente por el método.

    assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    assertThat(usuarios.get(0).isBloqueado()).isEqualTo(true);
}
```



### LINKS

Trello público: https://trello.com/b/VQoo3uP6/todolist-mads  
Docker Hub: https://hub.docker.com/r/noelmartinnez/spring-boot-demoapp  
GitHub: https://github.com/mads-ua-23-24/mads-todolist-nmp50-ua  
