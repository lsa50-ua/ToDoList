package madstodolist.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

// Data Transfer Object para la clase Tarea
public class ComentarioData implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long usuarioID;
    private Long tareaId;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date fecha= new Date();
    private String Texto;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioID() {
        return usuarioID;
    }

    public void setUsuarioID(Long usuarioID) {
        this.usuarioID = usuarioID;
    }

    public Long getTareaId() {
        return tareaId;
    }

    public void setTareaId(Long tareaid) {
        this.tareaId = tareaid;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTexto() {
        return Texto;
    }

    public void setTexto(String texto) {
        Texto = texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TareaData)) return false;
        ComentarioData comentarioData = (ComentarioData) o;
        return Objects.equals(id, comentarioData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
