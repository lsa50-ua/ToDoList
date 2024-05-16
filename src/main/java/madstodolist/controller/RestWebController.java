package madstodolist.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import madstodolist.model.Event;


@RestController
@RequestMapping("/api/event")
public class RestWebController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping(value = "/all")
    public String getEvents() {
        String jsonMsg = null;
        try {
            Long idUsuario = managerUserSession.usuarioLogeado();
            List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);

            List<Event> events = new ArrayList<>();

            for (TareaData tarea : tareas) {
                Event event = new Event();
                event.setTitle(tarea.getTitulo());
                if (tarea.getFechaLimite() != null) {
                    event.setStart(tarea.getFechaLimite().toString());
                }
                events.add(event);
            }

            ObjectMapper mapper = new ObjectMapper();
            jsonMsg =  mapper.writerWithDefaultPrettyPrinter().writeValueAsString(events);
        } catch (IOException ioex) {
            System.out.println(ioex.getMessage());
        }
        return jsonMsg;
    }
}

