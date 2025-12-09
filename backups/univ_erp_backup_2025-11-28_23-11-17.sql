-- University ERP Complete Database Backup
-- Generated: 2025-11-28T23:11:17.204448800
-- Auth Database: univ_auth
-- ERP Database: univ_erp

-- ===== AUTH DATABASE BACKUP =====
--
-- PostgreSQL database dump
--

\restrict HEnXH9gVS36Nzw3m257waR5jQJS9RfOG0U9otl2ZYqlqzldSbdGyjVJRFHhYcdW

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users_auth; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users_auth (
    user_id integer NOT NULL,
    username character varying(50) NOT NULL,
    role character varying(20) NOT NULL,
    password_hash text NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    last_login timestamp without time zone,
    CONSTRAINT users_auth_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'INSTRUCTOR'::character varying, 'STUDENT'::character varying])::text[])))
);


ALTER TABLE public.users_auth OWNER TO postgres;

--
-- Data for Name: users_auth; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users_auth (user_id, username, role, password_hash, status, last_login) FROM stdin;
1	admin1	ADMIN	PBKDF2$120000$UheAsRJgdA4oA5Q5oWf0iA==$cog7UmHWKaGgOp3AzhVywx0vcStWpTWy4A2uXvhRlhI=	ACTIVE	2025-11-28 23:09:47.490166
11	stu6	STUDENT	PBKDF2$120000$Bw//4ZLzbQ+5L+Wpr6M7Pw==$hyJgMr/03sjyKP5kMOPvFL2hCf9ua8FlHn8IbRv0rUk=	ACTIVE	\N
6	inst3	STUDENT	PBKDF2$120000$a1Cgbz67dlWh2p/ZiLlVwQ==$UORle+4wfk4pSD8nKsFqdY9QpBtWgT5QOJ5wJKN8Dz0=	ACTIVE	\N
5	stu2	STUDENT	PBKDF2$120000$yVpkAxT2rR9f0bYoA0D5VA==$+a16Wvm/4seMfOW906QMADIsvTnIY4pseEpm3Y6YYPY=	ACTIVE	2025-11-25 19:03:30.772748
4	stu1	STUDENT	PBKDF2$120000$aXxEmxWVLgqibj9GQuXeRg==$rd7iekdAVv70kl/i5SH+C0uNQSKxIGkQWCkBw0Rs3R8=	ACTIVE	2025-11-28 19:33:34.902877
10	stu4	STUDENT	PBKDF2$120000$s01EXfAwp5j23nHXH0cDlQ==$qU6r5pGyZ2hhnGvhLyTnavFCmRul1T2y+O/9K0dW1EA=	ACTIVE	\N
7	inst5	INSTRUCTOR	PBKDF2$120000$+N2ba52z3kGGH1gJihzntw==$u62zAuR9JqU4xlNo7eRTvdfNIcGWSWPdGMpZaelJbpw=	ACTIVE	2025-11-28 22:57:55.136857
9	stu3	STUDENT	PBKDF2$120000$k6j8H34p8NR2cWRp8M+eaQ==$+f8AUdh/GaZ3QoNVn6yLIlJlOdV8ZmvyRj9BJt7+nKw=	ACTIVE	2025-11-28 23:00:15.451828
\.


--
-- Name: users_auth users_auth_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_auth
    ADD CONSTRAINT users_auth_pkey PRIMARY KEY (user_id);


--
-- Name: users_auth users_auth_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users_auth
    ADD CONSTRAINT users_auth_username_key UNIQUE (username);


--
-- PostgreSQL database dump complete
--

\unrestrict HEnXH9gVS36Nzw3m257waR5jQJS9RfOG0U9otl2ZYqlqzldSbdGyjVJRFHhYcdW


-- ===== ERP DATABASE BACKUP =====
--
-- PostgreSQL database dump
--

\restrict HDxGxUs9SWMqsB14m9BlJ5J6kywU1e49pUhg3SVJCUedIRjFnrJKB33jezWFeU6

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.courses (
    course_id integer NOT NULL,
    code character varying(20) NOT NULL,
    title character varying(100) NOT NULL,
    credits integer NOT NULL
);


ALTER TABLE public.courses OWNER TO postgres;

--
-- Name: courses_course_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.courses_course_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.courses_course_id_seq OWNER TO postgres;

--
-- Name: courses_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.courses_course_id_seq OWNED BY public.courses.course_id;


--
-- Name: enrollments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.enrollments (
    enrollment_id integer NOT NULL,
    student_user_id integer NOT NULL,
    section_id integer NOT NULL,
    status character varying(20) DEFAULT 'ENROLLED'::character varying NOT NULL
);


ALTER TABLE public.enrollments OWNER TO postgres;

--
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.enrollments_enrollment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.enrollments_enrollment_id_seq OWNER TO postgres;

--
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.enrollments_enrollment_id_seq OWNED BY public.enrollments.enrollment_id;


--
-- Name: grades; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.grades (
    grade_id integer NOT NULL,
    enrollment_id integer NOT NULL,
    component character varying(20) NOT NULL,
    score numeric(5,2),
    final_grade numeric(5,2)
);


ALTER TABLE public.grades OWNER TO postgres;

--
-- Name: grades_grade_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.grades_grade_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.grades_grade_id_seq OWNER TO postgres;

--
-- Name: grades_grade_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.grades_grade_id_seq OWNED BY public.grades.grade_id;


--
-- Name: instructors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.instructors (
    user_id integer NOT NULL,
    department character varying(50)
);


ALTER TABLE public.instructors OWNER TO postgres;

--
-- Name: sections; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sections (
    section_id integer NOT NULL,
    course_id integer NOT NULL,
    instructor_user_id integer,
    day_time character varying(50) NOT NULL,
    room character varying(50),
    capacity integer NOT NULL,
    semester character varying(10),
    year integer,
    CONSTRAINT sections_capacity_check CHECK ((capacity >= 0))
);


ALTER TABLE public.sections OWNER TO postgres;

--
-- Name: sections_section_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sections_section_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sections_section_id_seq OWNER TO postgres;

--
-- Name: sections_section_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sections_section_id_seq OWNED BY public.sections.section_id;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.settings (
    key character varying(50) NOT NULL,
    value character varying(100) NOT NULL
);


ALTER TABLE public.settings OWNER TO postgres;

--
-- Name: students; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.students (
    user_id integer NOT NULL,
    roll_no character varying(20) NOT NULL,
    program character varying(50),
    year integer
);


ALTER TABLE public.students OWNER TO postgres;

--
-- Name: courses course_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses ALTER COLUMN course_id SET DEFAULT nextval('public.courses_course_id_seq'::regclass);


--
-- Name: enrollments enrollment_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments ALTER COLUMN enrollment_id SET DEFAULT nextval('public.enrollments_enrollment_id_seq'::regclass);


--
-- Name: grades grade_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.grades ALTER COLUMN grade_id SET DEFAULT nextval('public.grades_grade_id_seq'::regclass);


--
-- Name: sections section_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sections ALTER COLUMN section_id SET DEFAULT nextval('public.sections_section_id_seq'::regclass);


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.courses (course_id, code, title, credits) FROM stdin;
18	MTH101	PnS	4
19	SSH202	SPP	4
20	ECE100	BE	4
21	ECE111	ELD	4
\.


--
-- Data for Name: enrollments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.enrollments (enrollment_id, student_user_id, section_id, status) FROM stdin;
16	9	18	ENROLLED
\.


--
-- Data for Name: grades; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.grades (grade_id, enrollment_id, component, score, final_grade) FROM stdin;
51	16	QUIZ	40.00	\N
52	16	MIDTERM	60.00	\N
53	16	ENDSEM	80.00	\N
54	16	FINAL	\N	66.00
\.


--
-- Data for Name: instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.instructors (user_id, department) FROM stdin;
7	MATH
\.


--
-- Data for Name: sections; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sections (section_id, course_id, instructor_user_id, day_time, room, capacity, semester, year) FROM stdin;
18	18	7	Tue 12:00-1:00 PM	C100	40	Winter	2025
20	20	7	Friday 4:00-5:00 PM	C202	40	Monsoon	2025
19	19	\N	Wed 4:00-5:00 PM	C201	40	Winter	2025
21	21	7	wednesday 12:00-1:00 PM	C201	40	Winter	2025
\.


--
-- Data for Name: settings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.settings (key, value) FROM stdin;
drop_deadline	2025-12-31
grading_scale	A: 90-100, B: 80-89, C: 70-79, D: 60-69, F: <60
maintenance_on	false
weights_section_20	30,30,40
\.


--
-- Data for Name: students; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.students (user_id, roll_no, program, year) FROM stdin;
4	CSE-2024-001	B.Tech Computer Science	2
5	ECE-2024-002	B.Tech Electronics & Communication	2
6	7	CSE	1
9	9	CSB	1
10	10	ECE	1
11	11	CSE	1
\.


--
-- Name: courses_course_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.courses_course_id_seq', 21, true);


--
-- Name: enrollments_enrollment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.enrollments_enrollment_id_seq', 17, true);


--
-- Name: grades_grade_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.grades_grade_id_seq', 59, true);


--
-- Name: sections_section_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sections_section_id_seq', 21, true);


--
-- Name: courses courses_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_code_key UNIQUE (code);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (course_id);


--
-- Name: enrollments enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (enrollment_id);


--
-- Name: enrollments enrollments_student_user_id_section_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_student_user_id_section_id_key UNIQUE (student_user_id, section_id);


--
-- Name: grades grades_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.grades
    ADD CONSTRAINT grades_pkey PRIMARY KEY (grade_id);


--
-- Name: instructors instructors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.instructors
    ADD CONSTRAINT instructors_pkey PRIMARY KEY (user_id);


--
-- Name: sections sections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_pkey PRIMARY KEY (section_id);


--
-- Name: settings settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (key);


--
-- Name: students students_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_pkey PRIMARY KEY (user_id);


--
-- Name: students students_roll_no_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_roll_no_key UNIQUE (roll_no);


--
-- Name: enrollments enrollments_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(section_id) ON DELETE CASCADE;


--
-- Name: grades grades_enrollment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.grades
    ADD CONSTRAINT grades_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES public.enrollments(enrollment_id) ON DELETE CASCADE;


--
-- Name: sections sections_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.courses(course_id);


--
-- PostgreSQL database dump complete
--

\unrestrict HDxGxUs9SWMqsB14m9BlJ5J6kywU1e49pUhg3SVJCUedIRjFnrJKB33jezWFeU6

