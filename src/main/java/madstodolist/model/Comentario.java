package madstodolist.model;



import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "comentarios")
public class Comentario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha = new Date();
    @Column(name ="Texto")
    private String Texto;

    public Comentario(Usuario usuario, Tarea tarea,String texto) {
        Texto = texto;
        setUsuario(usuario);
        setTarea(tarea);


    }

    public Comentario() {

    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if(this.usuario!=usuario) {
            this.usuario = usuario;
            usuario.addComentarios(this);
        }
    }

    public Tarea getTarea() {
        return tarea;
    }

    public void setTarea(Tarea tarea) {
        if(this.tarea!=tarea) {
            this.tarea = tarea;
            tarea.addComentarios(this);
        }
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (o == null || getClass() != o.getClass()) return false;
        Comentario comentario = (Comentario) o;
        if (id != null && comentario.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, comentario.id);
        // si no comparamos por campos obligatorios
        return Texto.equals(comentario.Texto) &&
                usuario.equals(comentario.usuario) && tarea.equals(comentario.tarea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Texto, usuario, tarea);
    }
}