package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class RegistradosWebTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // LISTADO DE REGISTRADOS

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

    // DESCRIPCIÓN DE REGISTRADOS

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
}
