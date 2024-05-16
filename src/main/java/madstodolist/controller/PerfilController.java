package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class PerfilController {
    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    // Método que devuelve la página /perfil
    @GetMapping("/perfil/{id}")
    public String perfil(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        // Obtenemos el id del usuario en sesión para comprobar si está logueado o no
        Long id = managerUserSession.usuarioLogeado();

        if(id != null){
            // Si está logueado, lo buscamos en la base de datos y lo añadimos al atributo "usuario"
            UsuarioData user = usuarioService.findById(id);
            model.addAttribute("logueado", user);

            List<Usuario> usuarios = usuarioService.listadoCompleto();
            Usuario usuario = usuarioService.buscarUsuarioPorId(usuarios, idUsuario);

            model.addAttribute("usuario", usuario);
        }
        else {
            throw new UsuarioNoLogeadoException();
        }

        return "perfil";
    }

    // Método para mostrar la página de actualización de perfil
    @GetMapping("/perfil/{id}/actualizar")
    public String mostrarActualizarPerfil(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if(id != null){
            model.addAttribute("registroData", new RegistroData());

            UsuarioData user = usuarioService.findById(id);
            model.addAttribute("logueado", user);

            List<Usuario> usuarios = usuarioService.listadoCompleto();
            Usuario usuario = usuarioService.buscarUsuarioPorId(usuarios, idUsuario);

            model.addAttribute("usuario", usuario);
        }
        else {
            throw new UsuarioNoLogeadoException();
        }

        return "actualizarPerfil";
    }

    // Método para manejar la actualización del perfil
    @PostMapping("/perfil/{id}/actualizar")
    public String actualizarPerfil(@PathVariable(value="id") Long idUsuario, @Valid RegistroData registroData, BindingResult result, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (result.hasErrors()) {
            System.out.println("Ha ocurrido un error.");
        }
        else{
            if(id != null){
                try {
                    UsuarioData nuevoUsuarioData = usuarioService.findById(idUsuario);
                    if(registroData.getEmail() != null && registroData.getPassword() != null &&
                            registroData.getNombre() != null && registroData.getFechaNacimiento() != null) {
                        nuevoUsuarioData.setEmail(registroData.getEmail());
                        nuevoUsuarioData.setPassword(registroData.getPassword());
                        nuevoUsuarioData.setFechaNacimiento(registroData.getFechaNacimiento());
                        nuevoUsuarioData.setNombre(registroData.getNombre());

                        // Validar y actualizar los datos del usuario en el servicio
                        usuarioService.actualizarUsuarioPorId(idUsuario, nuevoUsuarioData);

                        // Redirigir al perfil del usuario
                        return "redirect:/perfil/" + idUsuario;
                    }
                    else{
                        model.addAttribute("errorActualizar", "Ninguno de los campos puede estar vacio.");
                    }

                } catch (UsuarioServiceException e) {
                    model.addAttribute("errorActualizar", e.getMessage());
                }
            }
            else {
                throw new UsuarioNoLogeadoException();
            }
        }

        model.addAttribute("registroData", registroData);

        UsuarioData user = usuarioService.findById(id);
        model.addAttribute("logueado", user);

        List<Usuario> usuarios = usuarioService.listadoCompleto();
        Usuario usuario = usuarioService.buscarUsuarioPorId(usuarios, idUsuario);

        model.addAttribute("usuario", usuario);

        return "actualizarPerfil";
    }
}
