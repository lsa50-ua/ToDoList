package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class RegistradosController {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    // Método que recupera al usuario que está logueado y comprueba si es admin o no
    // Si ni si quiera está logueado se lanza la excepción de no estar logueado
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

    @GetMapping("/registrados")
    public String listadoRegistrados(Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        comprobarAdmin(id);

        // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
        UsuarioData user = usuarioService.findById(id);
        // "usuario" lo usaremos en la vista html
        model.addAttribute("usuario", user);

        // Recuperamos la lista de objetos Usuario que hay creados en la base de datos y
        // se los pasamos como atributos al modelo que se le pasa al html "listaRegistrados"
        // Es necesario que sea Usuario pues en el método listadoCompleto() se llama al método .findAll()
        // que tiene un UsuarioRepository por defecto en la clase, y este devuelve una lista de Usuario
        // y no de UsuarioData.
        List<Usuario> registrados = usuarioService.listadoCompleto();

        model.addAttribute("registrados", registrados);
        return "listaRegistrados";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionRegistrado(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        comprobarAdmin(id);

        // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
        UsuarioData user = usuarioService.findById(id);
        // "logueado" lo usaremos en la vista html
        model.addAttribute("logueado", user);

        List<Usuario> registrados = usuarioService.listadoCompleto();
        // Necesito trabajar con un Usuario y no con un UsuarioData por que en el lsitado de registrados
        // trabajo con Usuario, entonces si intento usar un findById() del usuarioService, me devolverá
        // un UsuarioData y daría error.
        Usuario usuario = usuarioService.buscarUsuarioPorId(registrados, idUsuario);

        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/bloquear/{id}")
    public String bloquearUsuario(@PathVariable(value="id") Long idUsuario) {
        UsuarioData usuario = usuarioService.findById(idUsuario);

        if (usuario != null) {
            usuario.setBloqueado(true);
            usuarioService.actualizarUsuario(usuario);
        }

        return "redirect:/registrados";
    }

    @PostMapping("/registrados/desbloquear/{id}")
    public String desbloquearUsuario(@PathVariable(value="id") Long idUsuario) {
        UsuarioData usuario = usuarioService.findById(idUsuario);

        if (usuario != null) {
            usuario.setBloqueado(false);
            usuarioService.actualizarUsuario(usuario);
        }

        return "redirect:/registrados";
    }
}
