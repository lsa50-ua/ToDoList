package madstodolist.repository;
import madstodolist.model.Comentario;
import madstodolist.model.Equipo;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ComentarioTest {
    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void crearComentario(){
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");
        Comentario comentario = new Comentario(usuario,tarea,"hola");
        assertThat(comentario.getTexto().equals("hola"));
        assertThat(comentario.getTarea().equals(tarea));
        assertThat(comentario.getUsuario().equals(usuario));

    }
    @Test
    public void listadoTest(){
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS");
        usuarioRepository.save(usuario);
        tareaRepository.save(tarea);
        Comentario comentario = new Comentario(usuario,tarea,"hola");
        Comentario newComent= new Comentario(usuario,tarea,"adios");
        comentarioRepository.save(comentario);
        comentarioRepository.save(newComent);
        List< Comentario> comentarios = comentarioRepository.findByTarea(tarea);
        assertThat(comentarios).hasSize(2);

    }
}
