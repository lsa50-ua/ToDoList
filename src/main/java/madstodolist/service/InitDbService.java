package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
// Se ejecuta solo si el perfil activo es 'dev'
@Profile("dev")
public class InitDbService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;

    // Se ejecuta tras crear el contexto de la aplicació para inicializar la base de datos.
    // Los datos que introduzcamos van a estar presentes mientras que la aplicación esté funcionando.

    // Cuando matemos la aplicación y la volvamos a reiniciar sólo estarán los datos iniciales,
    // los datos que se cargan con el servicio InitDbService.
    @PostConstruct
    public void initDatabase() {
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Lavar coche", new Date());
        tareaRepository.save(tarea1);

        Tarea tarea2 = new Tarea(usuario, "Renovar DNI", new Date());
        tareaRepository.save(tarea2);
    }

}
