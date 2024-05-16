package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String email;
    private String nombre;
    private String password;
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;

    // Usamos un booleano para identificar al admin
    @Column(name = "admin")
    private boolean admin = false;

    @Column(name = "bloqueado")
    private boolean bloqueado = false;

    // La relación es lazy por defecto, es necesario acceder a la lista de tareas para que se carguen.
    // Un usuario tiene muchas tareas.
    /* El atributo mappedBy indica que la clave ajena se va a guardar
     en la columna correspondiente con el atributo "usuario" de la entidad Tarea. */
    @OneToMany(mappedBy = "usuario")
    Set<Tarea> tareas = new HashSet<>();

    @ManyToMany(mappedBy = "usuarios")
    Set<Equipo> equipos = new HashSet<>();



    @OneToMany(mappedBy = "usuario")
    Set<Comentario> comentarios = new HashSet<>();
    public Set<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(Set<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public void addComentarios(Comentario comentario){
        this.comentarios.add(comentario);
        if (comentario.getUsuario() != this){
            comentario.setUsuario(this);
        }
    }

    @OneToOne(mappedBy = "adminUsuario", cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_admin_equipo_id")
    private Equipo adminEquipo;

    public Equipo getAdminEquipo() {
        return adminEquipo;
    }

    public void setAdminEquipo(Equipo adminEquipo) {
        this.adminEquipo = adminEquipo;
    }

    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Usuario() {}

    // Constructor público con los atributos obligatorios. En este caso el correo electrónico.
    public Usuario(String email) {
        this.email = email;
    }

    // Getters y setters atributos básicos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters y setters de la relación

    public Set<Tarea> getTareas() {
        return tareas;
    }

    // Método helper para añadir una tarea a la lista y establecer la relación inversa
    public void addTarea(Tarea tarea) {
        // Si la tarea ya está en la lista, no la añadimos
        if (tareas.contains(tarea)) return;
        // Añadimos la tarea a la lista
        tareas.add(tarea);
        // Establecemos la relación inversa del usuario en la tarea
        if (tarea.getUsuario() != this) {
            tarea.setUsuario(this);
        }
    }

    public Set<Equipo> getEquipos() {
        return equipos;
    }

    public void removeEquipo(Equipo equipo) {
        if (!equipos.contains(equipo)) return;
        equipos.remove(equipo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        if (id != null && usuario.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, usuario.id);
        // si no comparamos por campos obligatorios
        return email.equals(usuario.email);
    }

    @Override
    public int hashCode() {
        // Generamos un hash basado en los campos obligatorios
        return Objects.hash(email);
    }

    // Setter y Getter para el nuevo atributo de admin
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean estado) {
        admin = estado;
    }


    // Setter y Getter para el nuevo atributo de bloqueado
    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean estado) {
        bloqueado = estado;
    }
}
