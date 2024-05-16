package madstodolist.service;

import madstodolist.dto.ComentarioData;
import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.model.Comentario;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.ComentarioRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {
    Logger logger = LoggerFactory.getLogger(ComentarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public ComentarioData nuevoComentario(Long idUsuario, Long idTarea, String texto) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new ComentarioServiceException("Este usuario no existe");
        }
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea== null){
            throw new ComentarioServiceException("Esta tarea no existe");
        }
        Comentario comentario = new Comentario(usuario,tarea,texto);
        comentarioRepository.save(comentario);

        return modelMapper.map(comentario, ComentarioData.class);
    }
    @Transactional
    public void BorrarComentario(Long idComent, Long IdUSer){
        Usuario usuario = usuarioRepository.findById(IdUSer).orElse(null);
        if (usuario == null) {
            throw new ComentarioServiceException("Este usuario no existe");
        }
        Comentario comentario = comentarioRepository.findById(idComent).orElse(null);
        if(comentario == null){
            throw new ComentarioServiceException("Esta tarea no existe");
        }
        comentarioRepository.delete(comentario);
    }
    @Transactional(readOnly = true)
    public List<ComentarioData> obtenerComentariosDeTarea(Long idTarea) {
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if(tarea==null){
            throw new ComentarioServiceException("Esta tarea no existe");
        }
        List<Comentario> comentarios= comentarioRepository.findByTarea(tarea);
        return comentarios.stream()
                .map(comentario -> {
                    ComentarioData comentarioData = modelMapper.map(comentario, ComentarioData.class);
                    comentarioData.setUsername(comentario.getUsuario().getNombre()); // Ajusta esto según tu modelo de datos
                    return comentarioData;
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ComentarioData obtenerComentario(Long idComent){
        Comentario comentario = comentarioRepository.findById(idComent).orElse(null);
        if(comentario==null){
            throw new ComentarioServiceException("Este comentario no existe");
        }
        return modelMapper.map(comentario, ComentarioData.class);
    }




}
