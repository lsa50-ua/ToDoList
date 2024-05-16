package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    // Método que devuelve la página /about
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
}