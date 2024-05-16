package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class EquipoController {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/equipos")
    public String listaEquipos(Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(id);

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
            model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "equipos";
    }

    @GetMapping("/equipos/{equipoId}/usuarios")
    public String listaUsuariosEnEquipo(@PathVariable(value="equipoId") Long equipoId, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        List<TareaData> tareasEquipo = equipoService.obtenerTareasEquipo(equipoId);
        model.addAttribute("tareasEquipo", tareasEquipo);

        return "usuariosEnEquipo";
    }

    @PostMapping("/equipos/crear")
    public String crearEquipo(@RequestParam("nombreEquipo") String nombreEquipo, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        // Validación de nombre de equipo no vacío
        if (nombreEquipo == null || nombreEquipo.trim().isEmpty()) {
            model.addAttribute("errorCrearEquipo", "El nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        }
        else{
            try {
                // Intenta crear el equipo
                equipoService.crearEquipo(nombreEquipo);
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCrearEquipo", e.getMessage());
            }
        }

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(id);

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
            model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "equipos";
    }

    @PostMapping("/equipos/{equipoId}/unirse")
    public String unirseAEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
            model.addAttribute("errorUnirse", "Ya perteneces a este equipo.");
        } else {
            equipoService.añadirUsuarioAEquipo(equipoId, idUsuario);
            model.addAttribute("correcto", "Te has unido al equipo.");
        }

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
            model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @PostMapping("/equipos/{equipoId}/abandonar")
    public String abandonarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
            equipoService.abandonarEquipo(equipoId, idUsuario);

            Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
            if (equipoAdmin != null && equipoAdmin.getAdminUsuario() != null) {
                if (Objects.equals(equipoAdmin.getAdminUsuario().getId(), idUsuario)) {
                    equipoService.eliminarUsuarioDeEquipo(idUsuario, equipoId);
                }
            }

            model.addAttribute("correctoAbandono", "Has abandonado el equipo.");
        } else {
            model.addAttribute("errorAbandonar", "No perteneces a este equipo.");
        }

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
            model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @GetMapping("/equipos/{equipoId}/administrar")
    public String administrarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        // Verificar si el usuario es administrador antes de permitir la administración del equipo
        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
        model.addAttribute("usuarios", usuarios);

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
        if(equipoAdmin != null){
            model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
        }
        else{
            model.addAttribute("equipoAdmin", null);
        }

        try {
            if(equipoAdmin.getAdminUsuario() != null){
                model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
            }
            else{
                model.addAttribute("idAdmin", null);
            }
        } catch(NullPointerException e) {
            model.addAttribute("idAdmin", null);
        }

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/cambiarNombre")
    public String cambiarNombreEquipo(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevoNombre") String nuevoNombre, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            model.addAttribute("errorCambiarNombre", "El nuevo nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        } else {
            try {
                equipoService.cambiarNombreEquipo(equipoId, nuevoNombre);
                model.addAttribute("correctoCambioNombre", "El nombre del equipo se ha cambiado con éxito.");
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCambiarNombre", e.getMessage());
            }
        }

        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
        model.addAttribute("usuarios", usuarios);

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
        if(equipoAdmin != null){
            model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
        }
        else{
            model.addAttribute("equipoAdmin", null);
        }

        try {
            if(equipoAdmin.getAdminUsuario() != null){
                model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
            }
            else{
                model.addAttribute("idAdmin", null);
            }
        } catch(NullPointerException e) {
            model.addAttribute("idAdmin", null);
        }

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/eliminar")
    public String eliminarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        try {
            Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
            if (equipoAdmin != null && equipoAdmin.getAdminUsuario() != null) {
                equipoService.eliminarUsuarioDeEquipo(equipoAdmin.getAdminUsuario().getId(), equipoId);
            }
            equipoService.eliminarEquipo(equipoId);

            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);

            Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

            if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
                Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
                model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
            }

            List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
            model.addAttribute("equipos", equipos);

            return "redirect:/equipos";

        } catch (EquipoServiceException e) {
            model.addAttribute("errorEliminacion", e.getMessage());

            List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
            model.addAttribute("usuarios", usuarios);

            EquipoData equipo = equipoService.recuperarEquipo(equipoId);
            model.addAttribute("equipo", equipo);

            Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
            if(equipoAdmin != null){
                model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
            }
            else{
                model.addAttribute("equipoAdmin", null);
            }

            try {
                if(equipoAdmin.getAdminUsuario() != null){
                    model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
                }
                else{
                    model.addAttribute("idAdmin", null);
                }
            } catch(NullPointerException ex) {
                model.addAttribute("idAdmin", null);
            }

            return "administracionEquipo";
        }
    }

    @PostMapping("/equipos/{equipoId}/descripcion")
    public String añadirDescripcion(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevaDescripcion") String nuevaDescripcion, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        if (nuevaDescripcion == null || nuevaDescripcion.trim().isEmpty()) {
            model.addAttribute("errorCambiarDescripcion", "La descripción no puede estar vacía o compuesta por espacios en blanco.");
        } else {
            try {
                equipoService.añadirDescripcion(equipoId, nuevaDescripcion);
                model.addAttribute("correctaDescripcion", "La descripción se ha añadido con éxito.");
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCambiarDescripcion", e.getMessage());
            }
        }

        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
        model.addAttribute("usuarios", usuarios);

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
        if(equipoAdmin != null){
            model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
        }
        else{
            model.addAttribute("equipoAdmin", null);
        }

        try {
            if(equipoAdmin.getAdminUsuario() != null){
                model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
            }
            else{
                model.addAttribute("idAdmin", null);
            }
        } catch(NullPointerException e) {
            model.addAttribute("idAdmin", null);
        }

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/asignarUsuario")
    public String asignarUsuario(
            @PathVariable(value = "equipoId") Long equipoId,
            @RequestParam("usuarioId") Long usuarioIdSeleccionado,
            Model model, HttpSession session) {

        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        try{
            usuarioService.asignarEquipo(usuarioIdSeleccionado, equipoId);
            model.addAttribute("correctoAsignar", "Se ha asignado correctamente.");
        } catch (UsuarioServiceException e) {
            model.addAttribute("errorAsignar", e.getMessage());
        }

        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
        model.addAttribute("usuarios", usuarios);

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
        if(equipoAdmin != null){
            model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
        }
        else{
            model.addAttribute("equipoAdmin", null);
        }

        try {
            if(equipoAdmin.getAdminUsuario() != null){
                model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
            }
            else{
                model.addAttribute("idAdmin", null);
            }
        } catch(NullPointerException e) {
            model.addAttribute("idAdmin", null);
        }

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/eliminarAdmin/{idAdmin}")
    public String eliminarUsuarioDeEquipo(
            @PathVariable(value = "equipoId") Long equipoId,
            @PathVariable(value = "idAdmin") Long idAdmin,
            Model model, HttpSession session) {

        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        try{
            equipoService.eliminarUsuarioDeEquipo(idAdmin, equipoId);
            model.addAttribute("correctoEliminar", "Se ha eliminado correctamente.");
        } catch (UsuarioServiceException e) {
            model.addAttribute("errorEliminar", e.getMessage());
        }

        List<UsuarioData> usuarios = equipoService.usuariosSinEquipoAsignadoComoAdmin(equipoId);
        model.addAttribute("usuarios", usuarios);

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
        if(equipoAdmin != null){
            model.addAttribute("equipoAdmin", equipoAdmin.getAdminUsuario());
        }
        else{
            model.addAttribute("equipoAdmin", null);
        }

        try {
            if(equipoAdmin.getAdminUsuario() != null){
                model.addAttribute("idAdmin", equipoAdmin.getAdminUsuario().getId());
            }
            else{
                model.addAttribute("idAdmin", null);
            }
        } catch(NullPointerException e) {
            model.addAttribute("idAdmin", null);
        }

        return "administracionEquipo";
    }

    @GetMapping("/equipos/{equipoId}/noAdminAdministracion")
    public String administrarEquipoNoAdmin(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

        Equipo equipoAdministrado = null;

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
        }

        if (equipoAdministrado != null && !Objects.equals(equipoId, Objects.requireNonNull(equipoAdministrado).getId())) {
            throw new UsuarioNoAutorizadoException();
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        List<TareaData> tareasSinEquipoAsignado = equipoService.obtenerTareasUsuarioSinEquipo(idUsuario);
        model.addAttribute("tareasSinEquipoAsignado", tareasSinEquipoAsignado);

        List<TareaData> tareasEquipo = equipoService.obtenerTareasEquipo(equipoId);
        model.addAttribute("tareasEquipo", tareasEquipo);

        return "administracionEquipoNoAdmin";
    }

    @PostMapping("/equipos/{equipoId}/eliminar/{usuarioId}")
    public String eliminarDelEquipo(@PathVariable(value = "equipoId") Long equipoId,
                                    @PathVariable(value = "usuarioId") Long usuarioId,
                                    Model model,
                                    HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

        Equipo equipoAdministrado = null;

        if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
            equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
        }

        if (equipoAdministrado != null && !Objects.equals(equipoId, Objects.requireNonNull(equipoAdministrado).getId())) {
            throw new UsuarioNoAutorizadoException();
        }

        try{
            equipoService.abandonarEquipo(equipoId, usuarioId);

            Equipo equipoAdmin = equipoService.findByIdEquipoAdmin(equipoId);
            if (equipoAdmin != null && equipoAdmin.getAdminUsuario() != null &&
                    Objects.equals(equipoAdmin.getAdminUsuario().getId(), usuarioId)) {
                equipoService.eliminarUsuarioDeEquipo(usuarioId, equipoId);

                boolean esAdmin = usuario.isAdmin();
                model.addAttribute("esAdmin", esAdmin);

                Usuario usuarioEsAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

                if(usuarioEsAdminEquipo.getAdminEquipo() != null) {
                    Equipo equipoEsAdministrado = usuarioEsAdminEquipo.getAdminEquipo();
                    model.addAttribute("equipoAdministrado", equipoEsAdministrado.getId());
                }

                List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
                model.addAttribute("equipos", equipos);

                return "redirect:/equipos";
            }

            model.addAttribute("estadoCorrecto", "Todo correcto.");
        } catch(EquipoServiceException e) {
            model.addAttribute("estadoIncorrecto", "Ha ocurrido un error.");
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        return "administracionEquipoNoAdmin";
    }

    @PostMapping("/equipos/{equipoId}/cambiarNombreNoAdmin")
    public String cambiarNombreEquipoNoAdmin(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevoNombre") String nuevoNombre, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            model.addAttribute("errorCambiarNombre", "El nuevo nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        } else {
            try {
                equipoService.cambiarNombreEquipo(equipoId, nuevoNombre);
                model.addAttribute("correctoCambioNombre", "El nombre del equipo se ha cambiado con éxito.");
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCambiarNombre", e.getMessage());
            }
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        return "administracionEquipoNoAdmin";
    }

    @PostMapping("/equipos/{equipoId}/descripcionNoAdmin")
    public String añadirDescripcionNoAdmin(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevaDescripcion") String nuevaDescripcion, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (nuevaDescripcion == null || nuevaDescripcion.trim().isEmpty()) {
            model.addAttribute("errorCambiarDescripcion", "La descripción no puede estar vacía o compuesta por espacios en blanco.");
        } else {
            try {
                equipoService.añadirDescripcion(equipoId, nuevaDescripcion);
                model.addAttribute("correctaDescripcion", "La descripción se ha añadido con éxito.");
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCambiarDescripcion", e.getMessage());
            }
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        return "administracionEquipoNoAdmin";
    }

    @PostMapping("/equipos/{equipoId}/eliminarNoAdmin")
    public String eliminarEquipoNoAdmin(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        try {
            equipoService.eliminarUsuarioDeEquipo(idUsuario, equipoId);
            equipoService.eliminarEquipo(equipoId);

            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);

            Usuario usuarioAdminEquipo = usuarioService.findByIdAdminEquipo(idUsuario);

            if(usuarioAdminEquipo != null && usuarioAdminEquipo.getAdminEquipo() != null) {
                Equipo equipoAdministrado = usuarioAdminEquipo.getAdminEquipo();
                model.addAttribute("equipoAdministrado", equipoAdministrado.getId());
            }

            List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
            model.addAttribute("equipos", equipos);

            return "redirect:/equipos";

        } catch (EquipoServiceException e) {
            model.addAttribute("errorEliminacion", e.getMessage());

            EquipoData equipo = equipoService.recuperarEquipo(equipoId);
            model.addAttribute("equipo", equipo);

            List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
            model.addAttribute("usuarios", usuarios);

            return "administracionEquipoNoAdmin";
        }
    }

    @PostMapping("/equipos/{equipoId}/agregarTareaNoAsignada")
    public String agregarTareaNoAsignada(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session, RedirectAttributes flash, @RequestParam Long opcionSeleccionada) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }


        equipoService.addTareaAlEquipo(equipoId, opcionSeleccionada);


        return "redirect:/equipos/{equipoId}/noAdminAdministracion";

    }

    @PostMapping("/equipos/{equipoId}/eliminarTarea/{tareaId}")
    public String eliminarTarea(@PathVariable(value = "equipoId") Long equipoId, @PathVariable(value = "tareaId") Long tareaId, Model model, HttpSession session, RedirectAttributes flash) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        equipoService.eliminarTareaEquipo(equipoId, tareaId);

        return "redirect:/equipos/{equipoId}/noAdminAdministracion";
    }
}
