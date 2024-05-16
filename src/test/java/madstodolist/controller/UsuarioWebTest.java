package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

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
}
