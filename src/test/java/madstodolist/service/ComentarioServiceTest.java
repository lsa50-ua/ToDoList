package madstodolist.service;

import madstodolist.dto.ComentarioData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Comentario;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ComentarioServiceTest {
    @Autowired
    TareaService tareaService;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    ComentarioService comentarioService;
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        // Añadimos un usuario a la base de datos
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        ids.put("tareaId2", tarea2.getId());
        return ids;
    }
    @Test
    public void CrearyRecuperarComentTest(){
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId,"recoger suelo");
        ComentarioData comentario =  comentarioService.nuevoComentario(usuarioId,tarea.getId(),"me gusta");
        assertThat(comentario.getTexto().equals("me gusta"));
        assertThat(comentario.getTareaId().equals(tarea.getId()));
        assertThat(comentario.getUsuarioID().equals(usuarioId));
        ComentarioData comentariorecuperado= comentarioService.obtenerComentario(comentario.getId());
        assertThat(comentario.getTexto().equals(comentariorecuperado.getTexto()));
    }
    @Test
    public void ListadoComentTest(){
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId,"recoger suelo");
        ComentarioData comentario =  comentarioService.nuevoComentario(usuarioId,tarea.getId(),"me gusta");
        ComentarioData comentario2 =  comentarioService.nuevoComentario(usuarioId,tarea.getId(),"a mi no m gusta la verdad");
        List<ComentarioData> comentarios= comentarioService.obtenerComentariosDeTarea(tarea.getId());
        assertThat(comentarios.get(0).equals(comentario));
        assertThat(comentarios.get(0).equals(comentario2));

    }
    @Test
    public void BorrarComentario(){
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId,"recoger suelo");
        ComentarioData comentario =  comentarioService.nuevoComentario(usuarioId,tarea.getId(),"me gusta");
        comentarioService.BorrarComentario(comentario.getId(),usuarioId);
        List<ComentarioData> comentarios= comentarioService.obtenerComentariosDeTarea(tarea.getId());
        assertThat(comentarios).hasSize(0);


    }


}
