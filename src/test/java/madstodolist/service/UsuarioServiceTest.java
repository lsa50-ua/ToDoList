package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EquipoRepository equipoRepository;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    // Método para inicializar los datos de prueba en la BD de un usuario bloqueado
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBloqueadoBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("noel@gmail.com");
        usuario.setNombre("noel");
        usuario.setPassword("noel");
        usuario.setBloqueado(true);
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    // Método para inicializar los datos de prueba en la BD de un equipo
    // Devuelve el identificador del equipo en la BD
    Long addEquipoBD() {
        Equipo equipo = new Equipo();
        equipo.setNombre("Equipo de Prueba");
        equipoRepository.save(equipo);
        return equipo.getId();
    }

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

    @Test
    public void servicioRegistroUsuario() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema.

        UsuarioData usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getEmail()).isEqualTo("usuario.prueba2@gmail.com");
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // WHEN, THEN
        // Si intentamos registrar un usuario con un password null,
        // se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }


    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos,
        // , se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("12345678");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {

        // WHEN
        // Si registramos en el sistema un usuario con un e-mail no existente en la base de datos,
        // y un password no nulo,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario

        assertThat(usuarioNuevo.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD.

        UsuarioData usuarioBD = usuarioService.findById(usuarioNuevo.getId());
        assertThat(usuarioBD).isEqualTo(usuarioNuevo);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail,

        UsuarioData usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto.

        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

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

    @Test
    public void comprobarActualizarUsuarioPorId() {
        // GIVEN
        // Un usuario en la BD
        Long idUsuario = addUsuarioBD();

        // WHEN
        // Actualizamos el usuario con nuevos datos
        UsuarioData nuevosDatos = new UsuarioData();
        nuevosDatos.setNombre("Nuevo Nombre");
        nuevosDatos.setPassword("nuevacontraseña");
        nuevosDatos.setEmail("nuevoemail@gmail.com");
        nuevosDatos.setFechaNacimiento(new Date());

        UsuarioData usuarioActualizado = usuarioService.actualizarUsuarioPorId(idUsuario, nuevosDatos);

        // THEN
        // Verificamos que los datos se han actualizado correctamente
        assertThat(usuarioActualizado).isNotNull();
        assertThat(usuarioActualizado.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(usuarioActualizado.getPassword()).isEqualTo("nuevacontraseña");
        assertThat(usuarioActualizado.getEmail()).isEqualTo("nuevoemail@gmail.com");
    }

    @Test
    public void servicioAsignarEquipo() {
        // GIVEN
        // Un usuario y un equipo en la BD

        Long usuarioId = addUsuarioBD();
        Long equipoId = addEquipoBD();

        // WHEN
        // Asignamos el equipo al usuario y viceversa
        usuarioService.asignarEquipo(usuarioId, equipoId);

        // THEN
        // Verificamos que la asignación se haya realizado correctamente

        Usuario usuarioConEquipo = usuarioService.findByIdAdminEquipo(usuarioId);
        assertThat(usuarioConEquipo.getAdminEquipo()).isNotNull();

        Equipo equipoConUsuario = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoConUsuario.getAdminUsuario()).isNotNull();
    }
}