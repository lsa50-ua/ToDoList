package madstodolist.repository;

import madstodolist.model.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    // findByEmail hace que Spring construya automáticamente una consulta sobre la base de datos.
    Optional<Usuario> findByEmail(String s);
}
