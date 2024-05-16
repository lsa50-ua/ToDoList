ALTER TABLE public.tareas
ADD COLUMN destacada BOOLEAN;


ALTER TABLE tareas
    ADD COLUMN estado VARCHAR(255);

Alter TABLE tareas
    add column fecha_limite DATE;

ALTER TABLE public.usuarios
    ADD COLUMN fk_admin_equipo_id bigint;

-- Añadir la restricción de clave foránea (foreign key) para la relación
ALTER TABLE public.usuarios
    ADD CONSTRAINT fk_admin_equipo
        FOREIGN KEY (fk_admin_equipo_id)
            REFERENCES public.equipos(id);

ALTER TABLE public.equipos
    ADD COLUMN fk_admin_usuario_id bigint;

-- Añadir la restricción de clave foránea (foreign key) para la relación inversa
ALTER TABLE public.equipos
    ADD CONSTRAINT fk_admin_usuario
        FOREIGN KEY (fk_admin_usuario_id)
            REFERENCES public.usuarios(id);

CREATE TABLE public.comentarios (
     id bigint PRIMARY KEY ,
     Texto VARCHAR(255) NOT NULL,
     fecha DATE,
     tarea_id bigint NOT NULL,
     usuario_id bigint NOT NULL
);



ALTER TABLE public.comentarios OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.comentarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.comentarios_id_seq OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.comentarios_id_seq OWNED BY public.equipos.id;

Alter table public.comentarios
    add CONSTRAINT fk_comentario_tarea FOREIGN KEY (tarea_id) REFERENCES public.tareas(id);

Alter table public.comentarios
    add CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);

ALTER TABLE ONLY public.comentarios ALTER COLUMN id SET DEFAULT nextval('public.comentarios_id_seq'::regclass);

ALTER TABLE public.tareas
    ADD COLUMN equipo_Asignado_id bigint;

-- Añadir la restricción de clave foránea (foreign key) para la relación inversa
ALTER TABLE public.tareas
    ADD CONSTRAINT fk_tarea_quipo
        FOREIGN KEY (equipo_Asignado_id)
            REFERENCES public.tareas(id);



