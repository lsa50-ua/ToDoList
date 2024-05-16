--
-- PostgreSQL database dump
--

-- Dumped from database version 13.12 (Debian 13.12-1.pgdg120+1)
-- Dumped by pg_dump version 13.12 (Debian 13.12-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE ONLY public.equipo_usuario DROP CONSTRAINT fksabsvjgvfuen6hmyg7gn7oq4v;
ALTER TABLE ONLY public.equipo_usuario DROP CONSTRAINT fkk4y9gec15ccnirp3r7w29o66p;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fk_tarea_quipo;
ALTER TABLE ONLY public.comentarios DROP CONSTRAINT fk_comentario_usuario;
ALTER TABLE ONLY public.comentarios DROP CONSTRAINT fk_comentario_tarea;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT fk_admin_usuario;
ALTER TABLE ONLY public.usuarios DROP CONSTRAINT fk_admin_equipo;
ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_pkey;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT tareas_pkey;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT equipos_pkey;
ALTER TABLE ONLY public.equipo_usuario DROP CONSTRAINT equipo_usuario_pkey;
ALTER TABLE ONLY public.comentarios DROP CONSTRAINT comentarios_pkey;
ALTER TABLE public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.tareas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.equipos ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE public.usuarios_id_seq;
DROP TABLE public.usuarios;
DROP SEQUENCE public.tareas_id_seq;
DROP TABLE public.tareas;
DROP SEQUENCE public.equipos_id_seq;
DROP TABLE public.equipo_usuario;
DROP TABLE public.comentarios;
DROP SEQUENCE public.comentarios_id_seq;
DROP TABLE public.equipos;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: equipos; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipos (
    id bigint NOT NULL,
    nombre character varying(255),
    descripcion character varying(255),
    fk_admin_usuario_id bigint
);


ALTER TABLE public.equipos OWNER TO mads;

--
-- Name: comentarios_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.comentarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.comentarios_id_seq OWNER TO mads;

--
-- Name: comentarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.comentarios_id_seq OWNED BY public.equipos.id;


--
-- Name: comentarios; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.comentarios (
    id bigint DEFAULT nextval('public.comentarios_id_seq'::regclass) NOT NULL,
    texto character varying(255) NOT NULL,
    fecha date,
    tarea_id bigint NOT NULL,
    usuario_id bigint NOT NULL
);


ALTER TABLE public.comentarios OWNER TO mads;

--
-- Name: equipo_usuario; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipo_usuario (
    fk_equipo bigint NOT NULL,
    fk_usuario bigint NOT NULL
);


ALTER TABLE public.equipo_usuario OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.equipos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.equipos_id_seq OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.equipos_id_seq OWNED BY public.equipos.id;


--
-- Name: tareas; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.tareas (
    id bigint NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL,
    destacada boolean,
    estado character varying(255),
    fecha_limite date,
    equipo_asignado_id bigint
);


ALTER TABLE public.tareas OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.tareas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tareas_id_seq OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.tareas_id_seq OWNED BY public.tareas.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean,
    bloqueado boolean,
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255),
    fk_admin_equipo_id bigint
);


ALTER TABLE public.usuarios OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usuarios_id_seq OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: equipos id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id SET DEFAULT nextval('public.equipos_id_seq'::regclass);


--
-- Name: tareas id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas ALTER COLUMN id SET DEFAULT nextval('public.tareas_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: comentarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.comentarios (id, texto, fecha, tarea_id, usuario_id) FROM stdin;
1	soy increible	2023-12-19	1	1
\.


--
-- Data for Name: equipo_usuario; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipo_usuario (fk_equipo, fk_usuario) FROM stdin;
1	1
\.


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos (id, nombre, descripcion, fk_admin_usuario_id) FROM stdin;
1	Equipo 4	\N	\N
\.


--
-- Data for Name: tareas; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.tareas (id, titulo, usuario_id, destacada, estado, fecha_limite, equipo_asignado_id) FROM stdin;
1	comer	1	t	EN_PROCESO	2023-12-29	\N
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.usuarios (id, admin, bloqueado, email, fecha_nacimiento, nombre, password, fk_admin_equipo_id) FROM stdin;
1	t	f	noelmartinezpomares@gmail.com	2000-12-01	noel	noel	\N
\.


--
-- Name: comentarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.comentarios_id_seq', 1, true);


--
-- Name: equipos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.equipos_id_seq', 1, true);


--
-- Name: tareas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.tareas_id_seq', 1, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 1, true);


--
-- Name: comentarios comentarios_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.comentarios
    ADD CONSTRAINT comentarios_pkey PRIMARY KEY (id);


--
-- Name: equipo_usuario equipo_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT equipo_usuario_pkey PRIMARY KEY (fk_equipo, fk_usuario);


--
-- Name: equipos equipos_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_pkey PRIMARY KEY (id);


--
-- Name: tareas tareas_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT tareas_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: usuarios fk_admin_equipo; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_admin_equipo FOREIGN KEY (fk_admin_equipo_id) REFERENCES public.equipos(id);


--
-- Name: equipos fk_admin_usuario; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT fk_admin_usuario FOREIGN KEY (fk_admin_usuario_id) REFERENCES public.usuarios(id);


--
-- Name: comentarios fk_comentario_tarea; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.comentarios
    ADD CONSTRAINT fk_comentario_tarea FOREIGN KEY (tarea_id) REFERENCES public.tareas(id);


--
-- Name: comentarios fk_comentario_usuario; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.comentarios
    ADD CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: tareas fk_tarea_quipo; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fk_tarea_quipo FOREIGN KEY (equipo_asignado_id) REFERENCES public.tareas(id);


--
-- Name: tareas fkdmoaxl7yv4q6vkc9h32wvbddr; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: equipo_usuario fkk4y9gec15ccnirp3r7w29o66p; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fkk4y9gec15ccnirp3r7w29o66p FOREIGN KEY (fk_equipo) REFERENCES public.equipos(id);


--
-- Name: equipo_usuario fksabsvjgvfuen6hmyg7gn7oq4v; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fksabsvjgvfuen6hmyg7gn7oq4v FOREIGN KEY (fk_usuario) REFERENCES public.usuarios(id);


--
-- PostgreSQL database dump complete
--

