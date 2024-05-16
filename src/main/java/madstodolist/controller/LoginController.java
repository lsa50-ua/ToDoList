package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioBloqueadoException;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        // Llamada al servicio para comprobar si el login es correcto
        UsuarioService.LoginStatus loginStatus = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

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
        } else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.USUARIO_BLOQUEADO) {
            model.addAttribute("error", "Usuario Bloqueado.");
            return "formLogin";
        }

        return "formLogin";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroData", new RegistroData());

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

        return "formRegistro";
    }

   @PostMapping("/registro")
   public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }

       UsuarioData usuarioData = new UsuarioData();
       usuarioData.setEmail(registroData.getEmail());
       usuarioData.setPassword(registroData.getPassword());
       usuarioData.setFechaNacimiento(registroData.getFechaNacimiento());
       usuarioData.setNombre(registroData.getNombre());

       usuarioData.setAdmin(registroData.isAdmin());
       usuarioData.setBloqueado(false);

        usuarioService.registrar(usuarioData);
        return "redirect:/login";
   }

   @GetMapping("/logout")
   public String logout(HttpSession session) {
        managerUserSession.logout();
        return "redirect:/login";
   }
}
