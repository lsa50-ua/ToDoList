package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TareaRepository tareaRepository;

    @Autowired
    private ModelMapper modelMapper;

    /*
    * La anotación @Transactional se usa en los métodos de servicio en los que se trabaja con objetos repository
    * para asegurarnos de que todo el código del método se ejecuta en la misma transacción y usando la misma conexión a la base de datos.
    * */
    @Transactional
    public EquipoData crearEquipo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        }

        List<Equipo> equipos = equipoRepository.findAll();

        // Buscar si ya existe un equipo con el mismo nombre
        for (Equipo equipoBD : equipos) {
            if (equipoBD.getNombre().equals(nombre)) {
                throw new EquipoServiceException("Ya existe un equipo con el mismo nombre.");
            }
        }

        Equipo equipoNuevo = new Equipo(nombre);
        equipoNuevo = equipoRepository.save(equipoNuevo);
        return modelMapper.map(equipoNuevo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null)
            throw new EquipoServiceException("No se ha encontrado el equipo dado el id.");
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<Equipo> equipos = equipoRepository.findAllByOrderByNombreAsc();

        return equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        Usuario usuario = usuarioRepository.findById(id1).orElse(null);

        if (equipo == null || usuario == null) {
            throw new EquipoServiceException("No se puede agregar el usuario al equipo. Equipo o usuario no encontrados.");
        }

        equipo.addUsuario(usuario);
    }

    @Transactional
    public void abandonarEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (equipo == null || usuario == null) {
            throw new EquipoServiceException("No se puede abandonar el equipo. Equipo o usuario no encontrados.");
        }

        equipo.removeUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("No se ha encontrado el equipo dado el ID.");
        }

        List<UsuarioData> usuariosData = equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());

        return usuariosData;
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosSinEquipoAsignadoComoAdmin(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("No se ha encontrado el equipo dado el ID.");
        }

        List<UsuarioData> usuariosData = equipo.getUsuarios().stream()
                .filter(usuario -> usuario.getAdminEquipo() == null)
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());

        return usuariosData;
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario == null) {
            throw new EquipoServiceException("No se ha encontrado el usuario dado el ID.");
        }

        Set<Equipo> equiposDelUsuario = usuario.getEquipos();

        if (equiposDelUsuario.isEmpty()) {
            throw new EquipoServiceException("El usuario no pertenece a ningún equipo.");
        }

        List<EquipoData> equiposData = new ArrayList<>();
        for (Equipo equipo : equiposDelUsuario) {
            equiposData.add(modelMapper.map(equipo, EquipoData.class));
        }

        return equiposData;
    }

    @Transactional
    public EquipoData cambiarNombreEquipo(Long equipoId, String nuevoNombre) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }

        List<Equipo> equipos = equipoRepository.findAllByOrderByNombreAsc().stream()
                .filter(e -> e.getNombre().equals(nuevoNombre))
                .collect(Collectors.toList());

        // La lista equipo es una lista donde están los equipos que tiene el mismo nombre que el nuevoNombre
        if (!equipos.isEmpty()) {
            throw new EquipoServiceException("Ya existe un equipo con el nombre " + nuevoNombre + ".");
        }

        equipo.setNombre(nuevoNombre);

        equipo = equipoRepository.save(equipo);

        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional
    public void eliminarEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }

        // Eliminamos el equipo de los usuarios asociados
        for (Usuario usuario : equipo.getUsuarios()) {
            usuario.removeEquipo(equipo);
        }

        equipoRepository.delete(equipo);
    }

    @Transactional
    public EquipoData añadirDescripcion(Long equipoId, String nuevaDescripcion) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }

        equipo.setDescripcion(nuevaDescripcion);

        equipo = equipoRepository.save(equipo);

        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public Equipo findByIdEquipoAdmin(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, Equipo.class);
        }
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long usuarioId, Long equipoId) {
        // Verificar si el usuario y el equipo existen en la base de datos
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
        Optional<Equipo> equipoOptional = equipoRepository.findById(equipoId);

        if (!usuarioOptional.isPresent() || !equipoOptional.isPresent()) {
            throw new UsuarioServiceException("El usuario o el equipo no existe en la base de datos");
        }

        Usuario usuario = usuarioOptional.get();
        Equipo equipo = equipoOptional.get();

        // Asignar a null en los dos casos
        usuario.setAdminEquipo(null);
        equipo.setAdminUsuario(null);

        // Guardar los cambios en la base de datos
        usuarioRepository.save(usuario);
        equipoRepository.save(equipo);
    }

    @Transactional(readOnly = true)
    public List<TareaData> obtenerTareasUsuarioSinEquipo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            throw new EquipoServiceException("El usuario con ID " + usuarioId + " no existe en la base de datos.");
        }
        Set<Tarea> tareasUsuario = usuario.getTareas();
        List<TareaData> tareasData = new ArrayList<>();
        for (Tarea tarea : tareasUsuario) {
            if (tarea.getEquipo() == null) {
                tareasData.add(modelMapper.map(tarea, TareaData.class));
            }
        }

        return tareasData;
    }
    @Transactional(readOnly = true)
    public List<TareaData> obtenerTareasEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }
        Set<Tarea> tareasEquipo = equipo.getTareas();
        List<TareaData> tareasData = new ArrayList<>();
        for (Tarea tarea : tareasEquipo) {
            tareasData.add(modelMapper.map(tarea, TareaData.class));
        }

        return tareasData;
    }

    @Transactional
    public void eliminarTareaEquipo(Long equipoId, Long tareaId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }

        Tarea tarea = equipo.getTareas().stream()
                .filter(t -> t.getId().equals(tareaId))
                .findFirst()
                .orElse(null);
        if (tarea == null) {
            throw new EquipoServiceException("La tarea con ID " + tareaId + " no existe en la base de datos.");
        }

        equipo.removeTarea(tarea);
    }

    @Transactional
    public void addTareaAlEquipo(Long equipoId, Long tareaId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);

        if (equipo == null) {
            throw new EquipoServiceException("El equipo con ID " + equipoId + " no existe en la base de datos.");
        }

        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) {
            throw new EquipoServiceException("La tarea con ID " + tareaId + " no existe en la base de datos.");
        }

        if(equipo.getTareas().contains(tarea)) {
            throw new EquipoServiceException("La tarea con ID " + tareaId + " ya está en el equipo con ID " + equipoId + ".");
        }
        equipo.addTarea(tarea);
    }
}