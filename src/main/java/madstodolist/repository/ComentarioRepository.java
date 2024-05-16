package madstodolist.repository;

import madstodolist.model.Comentario;

import madstodolist.model.Tarea;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ComentarioRepository extends CrudRepository<Comentario, Long> {
    public List<Comentario> findAll();

    List<Comentario> findByTarea(Tarea tarea);

}
