package madstodolist.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "equipo_usuario",
            joinColumns = { @JoinColumn(name = "fk_equipo") },
            inverseJoinColumns = {@JoinColumn(name = "fk_usuario")})
    Set<Usuario> usuarios = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "fk_admin_usuario_id")
    private Usuario adminUsuario;

    @OneToMany(mappedBy = "equipoAsignado")
    private Set<Tarea> tareasEquipo = new HashSet<>();

    public Usuario getAdminUsuario() {
        return adminUsuario;
    }

    public void setAdminUsuario(Usuario adminUsuario) {
        this.adminUsuario = adminUsuario;
    }

    public Equipo() {}

    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void addUsuario(Usuario usuario) {
        // Hay que actualiar ambas colecciones, porque
        // JPA/Hibernate no lo hace automáticamente
        this.getUsuarios().add(usuario);
        usuario.getEquipos().add(this);
    }

    public void removeUsuario(Usuario usuario) {
        if (!usuarios.contains(usuario)) return;
        usuarios.remove(usuario);
        usuario.removeEquipo(this); // Actualiza la relación inversa
    }

    public Set<Tarea> getTareas() {
        return tareasEquipo;
    }

    public void addTarea(Tarea tarea) {
        if(tarea.getUsuario() == this.adminUsuario) {
            if (tareasEquipo.contains(tarea)) return;
            if(tarea.getEquipo() == null){
                tarea.setEquipo(this);
                tareasEquipo.add(tarea);
            }
        }
    }

    public void removeTarea(Tarea tarea) {
        if (!tareasEquipo.contains(tarea)) return;
        tareasEquipo.remove(tarea);
        tarea.setEquipo(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if (id != null && equipo.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // si no comparamos por campos obligatorios
        return nombre.equals(equipo.nombre);
    }

    @Override
    public int hashCode() {
        // Generamos un hash basado en los campos obligatorios
        return Objects.hash(nombre);
    }
}
