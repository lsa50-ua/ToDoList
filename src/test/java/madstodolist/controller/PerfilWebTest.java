package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PerfilWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void usuarioVerificaPerfil() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("noel");
        usuario.setEmail("noel@gmail.com");
        usuario.setPassword("noel");

        Usuario usuarioPerfil = new Usuario();
        usuarioPerfil.setId(1L);
        usuarioPerfil.setNombre("noel");
        usuarioPerfil.setEmail("noel@gmail.com");
        usuarioPerfil.setPassword("noel");

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuarioPerfil);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        when(usuarioService.listadoCompleto()).thenReturn(usuarios);
        when(usuarioService.buscarUsuarioPorId(usuarios,1L)).thenReturn(usuarioPerfil);

        mockMvc.perform(get("/perfil/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("noel@gmail.com")));
    }

    @Test
    public void usuarioActualizaPerfilExitosamente() throws Exception {
        // Datos para el usuario logeado
        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setId(1L);
        usuarioLogeado.setNombre("noel");
        usuarioLogeado.setEmail("noel@gmail.com");
        usuarioLogeado.setPassword("noel");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioLogeado.getId());
        when(usuarioService.findById(usuarioLogeado.getId())).thenReturn(usuarioLogeado);

        // Datos para el usuario a actualizar
        Usuario usuarioActualizar = new Usuario();
        usuarioActualizar.setId(1L);
        usuarioActualizar.setNombre("nuevoNombre");
        usuarioActualizar.setEmail("nuevoEmail@gmail.com");
        usuarioActualizar.setPassword("nuevaContraseña");

        // Configurar el servicio para devolver el usuario a actualizar
        when(usuarioService.buscarUsuarioPorId(Mockito.anyList(), Mockito.eq(1L))).thenReturn(usuarioActualizar);

        // Formatear la fecha actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fechaNacimiento = dateFormat.format(new Date());

        // Solicitud POST para actualizar el perfil
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/perfil/1/actualizar")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "nuevoEmail@gmail.com")
                .param("nombre", "nuevoNombre")
                .param("password", "nuevaContraseña")
                .param("fechaNacimiento", fechaNacimiento)
        );

        // Verificar que la actualización fue exitosa y redirige al perfil del usuario
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/1"));

        // Verificar que no hay errores en el modelo (puedes ajustar esto según tu implementación)
        resultActions.andExpect(model().attributeDoesNotExist("errorActualizar"));
    }
}
