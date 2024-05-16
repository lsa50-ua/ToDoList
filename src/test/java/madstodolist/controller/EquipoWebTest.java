package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
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
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void navBarEquipos() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noel");
        usuario.setEmail("noel@gmail.com");
        usuario.setPassword("noel");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        this.mockMvc.perform(get("/equipos"))
                .andExpect(content().string(containsString("Equipos")));
    }

    @Test
    public void existeVistaEquipos() throws Exception {
        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"));
    }

    @Test
    public void listarEquipos() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noel");
        usuario.setEmail("noel@gmail.com");
        usuario.setPassword("noel");

        EquipoData equipo1 = new EquipoData();
        equipo1.setNombre("Equipo 1");
        EquipoData equipo2 = new EquipoData();
        equipo2.setNombre("Equipo 2");

        List<EquipoData> equipos = new ArrayList<>();
        equipos.add(equipo1);
        equipos.add(equipo2);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(content().string(containsString("Equipo 1")))
                .andExpect(content().string(containsString("Equipo 2")));
    }

    @Test
    public void usuariosEnEquipoExiste() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noel");
        usuario.setEmail("noel@gmail.com");
        usuario.setPassword("noel");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo 1");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);

        mockMvc.perform(get("/equipos/1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuariosEnEquipo"))
                .andExpect(content().string(containsString("Equipo 1")));
    }

    @Test
    public void listarUsuariosEnEquipo() throws Exception {
        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo 1");

        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(1L);
        usuario1.setNombre("Usuario 1");
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setNombre("Usuario 2");

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(usuario1);
        usuarios.add(usuario2);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario1.getId());
        when(usuarioService.findById(usuario1.getId())).thenReturn(usuario1);

        when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(1L)).thenReturn(usuarios);

        mockMvc.perform(get("/equipos/1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuariosEnEquipo"))
                .andExpect(content().string(containsString("Equipo 1")))
                .andExpect(content().string(containsString("Usuario 1")))
                .andExpect(content().string(containsString("Usuario 2")));
    }

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

    @Test
    public void añadirDescripcionEquipo() throws Exception {
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
        when(equipoService.añadirDescripcion(equipo.getId(), "esto es una descripcion")).thenReturn(equipo);

        mockMvc.perform(post("/equipos/1/descripcion")
                        .param("nuevaDescripcion", "esto es una descripcion"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipo"))
                .andExpect(content().string(containsString("La descripción se ha añadido con éxito.")));
    }

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

    @Test
    public void asignarUsuario() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setNombre("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin");
        admin.setAdmin(true);

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo Original");

        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setNombre("Usuario Nuevo");

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
        when(usuarioService.findById(admin.getId())).thenReturn(admin);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(usuarioService.findById(anyLong())).thenReturn(usuario);

        mockMvc.perform(post("/equipos/1/asignarUsuario")
                        .param("usuarioId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipo"))
                .andExpect(content().string(containsString("Se ha asignado correctamente.")));
    }

    @Test
    public void eliminarUsuarioDeEquipo() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setNombre("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin");
        admin.setAdmin(true);

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo Original");

        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setNombre("Usuario a Eliminar");

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
        when(usuarioService.findById(admin.getId())).thenReturn(admin);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(usuarioService.findById(anyLong())).thenReturn(usuario);
        doNothing().when(equipoService).eliminarUsuarioDeEquipo(anyLong(), anyLong());

        mockMvc.perform(post("/equipos/1/eliminarAdmin/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipo"))
                .andExpect(content().string(containsString("Se ha eliminado correctamente.")));
    }

    @Test
    public void administrarEquipoNoAdmin() throws Exception {
        // GET
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noadmin");
        usuario.setEmail("noadmin@gmail.com");
        usuario.setPassword("noadmin");

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("noadmin");
        usuarioAdmin.setEmail("noadmin@gmail.com");
        usuarioAdmin.setPassword("noadmin");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo NoAdmin");

        Equipo equipoAdmin = new Equipo();
        equipoAdmin.setId(1L);
        equipoAdmin.setNombre("Equipo NoAdmin");

        usuarioAdmin.setAdminEquipo(equipoAdmin);
        equipoAdmin.setAdminUsuario(usuarioAdmin);

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);
        when(usuarioService.findByIdAdminEquipo(usuario.getId())).thenReturn(usuarioAdmin);
        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(usuarios);

        // THEN
        mockMvc.perform(get("/equipos/" + equipo.getId() + "/noAdminAdministracion"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipoNoAdmin"))
                .andExpect(content().string(containsString("Administración de Equipo")));
    }

    @Test
    public void eliminarUsuarioDelEquipo() throws Exception {
        // GET
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noadmin");
        usuario.setEmail("noadmin@gmail.com");
        usuario.setPassword("noadmin");

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("noadmin");
        usuarioAdmin.setEmail("noadmin@gmail.com");
        usuarioAdmin.setPassword("noadmin");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo NoAdmin");

        Equipo equipoAdmin = new Equipo();
        equipoAdmin.setId(1L);
        equipoAdmin.setNombre("Equipo NoAdmin");

        usuarioAdmin.setAdminEquipo(equipoAdmin);
        equipoAdmin.setAdminUsuario(usuarioAdmin);

        UsuarioData usuarioAEliminar = new UsuarioData();
        usuarioAEliminar.setId(2L);
        usuarioAEliminar.setNombre("Usuario a Eliminar");

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);
        when(usuarioService.findByIdAdminEquipo(usuario.getId())).thenReturn(usuarioAdmin);

        when(equipoService.findByIdEquipoAdmin(equipo.getId())).thenReturn(equipoAdmin);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(usuarios);

        // THEN
        mockMvc.perform(post("/equipos/" + equipo.getId() + "/eliminar/" + usuarioAEliminar.getId()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("Todo correcto.")));
    }

    @Test
    void cambiarNombreEquipoNoAdmin() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setNombre("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo Original");

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(admin);

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
        when(usuarioService.findById(admin.getId())).thenReturn(admin);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(usuarios);

        mockMvc.perform(post("/equipos/{equipoId}/cambiarNombreNoAdmin", equipo.getId())
                        .param("nuevoNombre", "nuevoNombre"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipoNoAdmin"))
                .andExpect(content().string(containsString("nuevoNombre")));
    }

    @Test
    void añadirDescripcionEquipoNoAdmin() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setNombre("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo Original");

        List<UsuarioData> usuarios = new ArrayList<>();
        usuarios.add(admin);

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
        when(usuarioService.findById(admin.getId())).thenReturn(admin);

        when(equipoService.recuperarEquipo(equipo.getId())).thenReturn(equipo);
        when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(usuarios);

        mockMvc.perform(post("/equipos/{equipoId}/cambiarNombreNoAdmin", equipo.getId())
                        .param("nuevoNombre", "nuevoNombre"))
                .andExpect(status().isOk())
                .andExpect(view().name("administracionEquipoNoAdmin"))
                .andExpect(content().string(containsString("nuevoNombre")));
    }

    @Test
    public void eliminarEquipoNoAdmin() throws Exception {
        // GET
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noadmin");
        usuario.setEmail("noadmin@gmail.com");
        usuario.setPassword("noadmin");

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("noadmin");
        usuarioAdmin.setEmail("noadmin@gmail.com");
        usuarioAdmin.setPassword("noadmin");

        EquipoData equipo = new EquipoData();
        equipo.setId(1L);
        equipo.setNombre("Equipo NoAdmin");

        Equipo equipoAdmin = new Equipo();
        equipoAdmin.setId(1L);
        equipoAdmin.setNombre("Equipo NoAdmin");

        usuarioAdmin.setAdminEquipo(equipoAdmin);
        equipoAdmin.setAdminUsuario(usuarioAdmin);

        List<EquipoData> equipos = new ArrayList<>();
        equipos.add(equipo);

        // WHEN
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        when(usuarioService.findByIdAdminEquipo(usuario.getId())).thenReturn(usuarioAdmin);

        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);

        // THEN
        mockMvc.perform(post("/equipos/" + equipo.getId() + "/eliminarNoAdmin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(content().string(not(containsString("Equipo NoAdmin"))));
    }
}

