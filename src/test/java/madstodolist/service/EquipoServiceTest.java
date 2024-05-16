package madstodolist.service;

import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Tarea;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    /*
    * En el test no hay que añadir la anotación @Transactional
    * porque queremos probar el uso de los métodos de servicio en un contexto similar al que usaremos cuando los llamemos desde el controller.
    *
    * Cuando llamemos desde el controller a los métodos de servicio no se usará la anotación @Transactional
    * para evitar en el código del controller se pueda acceder a los objetos repository y modificar directamente la base de datos.
    * */
    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void excepcionesCrearEquipo() {
        equipoService.crearEquipo("Proyecto");

        assertThatThrownBy(() -> equipoService.crearEquipo(""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo("Proyecto"))
                .isInstanceOf(EquipoServiceException.class);
    }

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
        List<UsuarioData> usuariosEnEquipo = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuariosEnEquipo).isEmpty();
    }

    @Test
    public void cambiarNombreEquipo() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.cambiarNombreEquipo(equipo.getId(), "Nuevo Proyecto");

        // WHEN
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        assertThat(equipos).hasSize(1);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Nuevo Proyecto");
    }

    @Test
    public void añadirDescripcion() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirDescripcion(equipo.getId(), "esto es una descripcion");

        // WHEN
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        assertThat(equipos).hasSize(1);
        assertThat(equipos.get(0).getDescripcion()).isEqualTo("esto es una descripcion");
    }

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

    @Test
    public void usuariosSinEquipoAsignadoComoAdminTest() {
        // GIVEN
        // 2 usuarios, 2 equipos, en un equipo 2 usuarios, y en otro solo 1
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("user1@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("124");
        usuario2 = usuarioService.registrar(usuario2);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario1.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario2.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario2.getId());

        // WHEN
        usuarioService.asignarEquipo(usuario2.getId(),equipo2.getId());
        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipo1.getId());

        // THEN
        assertThat(usuarios).hasSize(1);
    }

    @Test
    public void eliminarUsuariosAdminDeEquipo() {
        // GIVEN
        // 2 usuarios, 2 equipos, en un equipo 2 usuarios, y en otro solo 1
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("user1@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("124");
        usuario2 = usuarioService.registrar(usuario2);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario1.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario2.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario2.getId());

        // WHEN
        usuarioService.asignarEquipo(usuario2.getId(),equipo2.getId());
        List<UsuarioData> usuarios1 = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipo1.getId());
        equipoService.eliminarUsuarioDeEquipo(usuario2.getId(),equipo2.getId());
        List<UsuarioData> usuarios2 = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipo1.getId());

        // THEN
        assertThat(usuarios1).hasSize(1);
        assertThat(usuarios2).hasSize(2);
    }

    @Test
    public void obtenerTareasEquipo() {
        // GIVEN
        // 2 usuarios, 2 equipos, en un equipo 2 usuarios, y en otro solo 1
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("user1@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("124");
        usuario2 = usuarioService.registrar(usuario2);

        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario1.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario2.getId());

        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 1");
        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 2");

        List<TareaData> tareasData = tareaService.allTareasUsuario(usuario2.getId());

        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());
        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().skip(1).findFirst().get().getId());
        // WHEN


        List<TareaData> tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(0);

        usuarioService.asignarEquipo(usuario2.getId(),equipo1.getId());

        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());
        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().skip(1).findFirst().get().getId());

        tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(2);
    }

    @Test
    public void eliminarTareasEquipo() {
        // GIVEN
        // 2 usuarios, 2 equipos, en un equipo 2 usuarios, y en otro solo 1
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("user1@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("124");
        usuario2 = usuarioService.registrar(usuario2);

        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario1.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario2.getId());

        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 1");
        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 2");

        List<TareaData> tareasData = tareaService.allTareasUsuario(usuario2.getId());

        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());
        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().skip(1).findFirst().get().getId());
        // WHEN


        List<TareaData> tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(0);

        usuarioService.asignarEquipo(usuario2.getId(),equipo1.getId());

        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());
        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().skip(1).findFirst().get().getId());

        tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(2);

        equipoService.eliminarTareaEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());

        tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(1);
    }

    @Test
    public void obtenerTareasSinEquipoAsignado() {
        // GIVEN
        // 2 usuarios, 2 equipos, en un equipo 2 usuarios, y en otro solo 1
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("user1@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("124");
        usuario2 = usuarioService.registrar(usuario2);

        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario1.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario2.getId());

        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 1");
        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 2");
        tareaService.nuevaTareaUsuario(usuario2.getId(), "Tarea 3");

        List<TareaData> tareasData = tareaService.allTareasUsuario(usuario2.getId());

        usuarioService.asignarEquipo(usuario2.getId(),equipo1.getId());

        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().findFirst().get().getId());
        equipoService.addTareaAlEquipo(equipo1.getId(), tareasData.stream().skip(1).findFirst().get().getId());

        List<TareaData> tareas = equipoService.obtenerTareasEquipo(equipo1.getId());

        assertThat(tareas).hasSize(2);

        tareas = equipoService.obtenerTareasUsuarioSinEquipo(usuario2.getId());

        assertThat(tareas).hasSize(1);
    }
}
