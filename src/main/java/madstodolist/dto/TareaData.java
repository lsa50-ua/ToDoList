package madstodolist.dto;


import madstodolist.model.EstadoTarea;

import org.springframework.format.annotation.DateTimeFormat;


import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// Data Transfer Object para la clase Tarea
public class TareaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private Long usuarioId;  // Esta es la ID del usuario asociado
    private boolean destacada; // Nuevo atributo para desctacar o no una tarea




    private EstadoTarea estado;


    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date fechaLimite;

    private Long equipoAsignadoId; // Esta es la ID del equipo asociado

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Setter y Getter para el nuevo atributo de destacada
    public boolean isDestacada() {
        return destacada;
    }
    public void setDestacada(boolean destacada) {
        this.destacada = destacada;
    }


    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }


    // Setter y Getter para el nuevo atributo de fechaLimite
    public Date getFechaLimite() {
        return fechaLimite;
    }
    public void setFechaLimite(Date fechaLimite) {
        this.fechaLimite = fechaLimite;

    }

    public Long getEquipoAsignadoId() {
        return equipoAsignadoId;
    }

    public void setEquipoAsignadoId(Long equipoAsignadoId) {
        this.equipoAsignadoId = equipoAsignadoId;
    }

    // Sobreescribimos equals y hashCode para que dos tareas sean iguales
    // si tienen el mismo ID (ignoramos el resto de atributos)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TareaData)) return false;
        TareaData tareaData = (TareaData) o;
        return Objects.equals(id, tareaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
