package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.ComentarioData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.ComentarioService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
public class TareaController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ComentarioService comentarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value="id") Long idUsuario, @ModelAttribute TareaData tareaData,
                                 Model model,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value = "id") Long idUsuario,
                             @Valid @ModelAttribute TareaData tareaData,
                             BindingResult result,
                             Model model,
                             RedirectAttributes flash,
                             HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);


        if (result.hasErrors()) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuario", usuario);
            return "formNuevaTarea";
        }
        else {



            TareaData tarea = new TareaData();
            tarea.setTitulo(tareaData.getTitulo());
            tarea.setFechaLimite(tareaData.getFechaLimite());
            tarea.setUsuarioId(idUsuario);

            tareaService.nuevaTareaUsuario(tarea);
            flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
            return "redirect:/usuarios/" + idUsuario + "/tareas";
        }
     }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "listaTareas";
    }

    @GetMapping("/tarea/{id}")
    public String VerTarea(@PathVariable(value="id") Long idtarea, Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idtarea);
        Long idUsuario = tarea.getUsuarioId();
        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<ComentarioData> comentarios = comentarioService.obtenerComentariosDeTarea(idtarea);
        model.addAttribute("comentarios",comentarios);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tarea", tarea);
        return "VerTarea";
    }
    @PostMapping("/tareas/{id}/comentarios/nuevo")
    public String CrearComentario(@PathVariable(value = "id") Long idtarea,@RequestParam("texto") String texto,Model model, HttpSession session){
        Long idUsuario= managerUserSession.usuarioLogeado();
        TareaData tarea = tareaService.findById(idtarea);
        comentarioService.nuevoComentario(idUsuario,idtarea,texto);
        return "redirect:/tarea/" + tarea.getId();
    }
    @DeleteMapping("/comentario/{id}/borrar")
    @ResponseBody
    public String BorrarComentario(@PathVariable(value="id") Long idComent, RedirectAttributes flash, HttpSession session){
        ComentarioData comentarioData = comentarioService.obtenerComentario(idComent);
        if(comentarioData.getUsuarioID() != managerUserSession.usuarioLogeado()){

        }
        else{
            comentarioService.BorrarComentario(idComent, managerUserSession.usuarioLogeado());
        }

        return "";

    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());
        UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
        model.addAttribute("usuario", usuario);
        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        tareaData.setFechaLimite(tarea.getFechaLimite());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @Valid @ModelAttribute TareaData tareaData, BindingResult result,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();

        comprobarUsuarioLogeado(idUsuario);

        if (result.hasErrors()) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("tarea", tarea);
            model.addAttribute("usuario", usuario);
            return "formEditarTarea";
        }
       
        tareaService.modificaTarea(idTarea, tareaData.getTitulo(), tareaData.getEstado(),tareaData.getFechaLimite());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    // La anotación @ResponseBody sirve para que la cadena devuelta sea la resupuesta
    // de la petición HTTP, en lugar de una plantilla thymeleaf
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        return "";
    }
    @PostMapping("/tarea/{id}/estado/{estado}")
    @ResponseBody
    // La anotación @ResponseBody sirve para que la cadena devuelta sea la resupuesta
    // de la petición HTTP, en lugar de una plantilla thymeleaf
    public String EditarEstado(@PathVariable(value="id") Long idTarea, @PathVariable(value="estado") String estado, RedirectAttributes flash, HttpSession session) {
        System.out.println(estado);
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());


        tareaService.CambiarEstado(idTarea,estado);
        return "";
    }

    @PostMapping("/tareas/{id}/destacada")
    @ResponseBody
    public String toggleDestacada(@PathVariable(value = "id") Long idTarea) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();
        comprobarUsuarioLogeado(idUsuario);

        tareaService.destacarTarea(idTarea);
        return "";
    }

    @GetMapping("/tareas/calendario")
    public String calendario(Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "calendario";
    }
}

