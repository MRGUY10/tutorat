create table roles
(
    id          bigserial
        primary key,
    nom         varchar(50) not null
        unique
        constraint roles_nom_check
            check ((nom)::text = ANY
        ((ARRAY ['STUDENT'::character varying, 'TUTOR'::character varying, 'ADMIN'::character varying])::text[])),
    description text,
    created_at  timestamp default CURRENT_TIMESTAMP,
    updated_at  timestamp default CURRENT_TIMESTAMP,
    version     bigint    default 0
);

alter table roles
    owner to admin;

create trigger update_roles_updated_at
    before update
    on roles
    for each row
    execute procedure update_updated_at_column();

create table users
(
    id               bigserial
        primary key,
    nom              varchar(50)  not null,
    prenom           varchar(50)  not null,
    email            varchar(255) not null
        unique,
    mot_de_passe     varchar(255) not null,
    telephone        varchar(20),
    date_inscription timestamp   default CURRENT_TIMESTAMP,
    statut           varchar(20) default 'ACTIVE'::character varying
        constraint users_statut_check
            check ((statut)::text = ANY
                   ((ARRAY ['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])),
    photo            varchar(500),
    created_at       timestamp   default CURRENT_TIMESTAMP,
    updated_at       timestamp   default CURRENT_TIMESTAMP,
    version          bigint      default 0
);

comment on table users is 'Base users table containing common information for all user types';

alter table users
    owner to admin;

create index idx_users_email
    on users (email);

create index idx_users_statut
    on users (statut);

create index idx_users_date_inscription
    on users (date_inscription);

create trigger update_users_updated_at
    before update
    on users
    for each row
    execute procedure update_updated_at_column();

create table matieres
(
    id          bigserial
        primary key,
    nom         varchar(100) not null,
    description text,
    niveau      varchar(20)
        constraint matieres_niveau_check
            check ((niveau)::text = ANY
        ((ARRAY ['debutant'::character varying, 'intermediaire'::character varying, 'avance'::character varying])::text[])),
    domaine     varchar(100),
    created_at  timestamp default CURRENT_TIMESTAMP,
    updated_at  timestamp default CURRENT_TIMESTAMP,
    version     bigint    default 0
);

alter table matieres
    owner to admin;

create index idx_matieres_domaine
    on matieres (domaine);

create index idx_matieres_niveau
    on matieres (niveau);

create trigger update_matieres_updated_at
    before update
    on matieres
    for each row
    execute procedure update_updated_at_column();

create table etudiants
(
    id      bigint       not null
        primary key
        references users
            on delete cascade,
    filiere varchar(100) not null,
    annee   integer      not null
        constraint etudiants_annee_check
            check (annee > 0),
    niveau  varchar(20) default 'debutant'::character varying
        constraint etudiants_niveau_check
            check ((niveau)::text = ANY
                   ((ARRAY ['debutant'::character varying, 'intermediaire'::character varying, 'avance'::character varying])::text[]))
);

comment on table etudiants is 'Students table with academic information';

alter table etudiants
    owner to admin;

create table admins
(
    id          bigint not null
        primary key
        references users
            on delete cascade,
    permissions text,
    departement varchar(100)
);

comment on table admins is 'Administrators table with permissions and department info';

alter table admins
    owner to admin;

create table user_roles
(
    id         bigserial
        primary key,
    user_id    bigint not null
        references users
            on delete cascade,
    role_id    bigint not null
        references roles
            on delete cascade,
    created_at timestamp default CURRENT_TIMESTAMP,
    version    bigint    default 0,
    unique (user_id, role_id)
);

alter table user_roles
    owner to admin;

create table demande_sessions
(
    id                   bigserial
        primary key,
    etudiant_id          bigint    not null
        references users
            on delete cascade,
    tuteur_id            bigint
                                   references users
                                       on delete set null,
    matiere_id           bigint    not null
        references matieres
            on delete cascade,
    date_heure_souhaitee timestamp not null,
    duree                integer   not null
        constraint demande_sessions_duree_check
            check (duree > 0),
    description          text,
    tarif_propose        numeric(10, 2)
        constraint demande_sessions_tarif_propose_check
            check (tarif_propose >= (0)::numeric),
    statut               varchar(20) default 'EN_ATTENTE'::character varying
        constraint demande_sessions_statut_check
            check ((statut)::text = ANY
                   ((ARRAY ['EN_ATTENTE'::character varying, 'ACCEPTEE'::character varying, 'REFUSEE'::character varying, 'ANNULEE'::character varying])::text[])),
    urgence              varchar(20) default 'NORMALE'::character varying
        constraint demande_sessions_urgence_check
            check ((urgence)::text = ANY
                   ((ARRAY ['BASSE'::character varying, 'NORMALE'::character varying, 'HAUTE'::character varying, 'URGENTE'::character varying])::text[])),
    date_creation        timestamp   default CURRENT_TIMESTAMP,
    date_reponse         timestamp,
    commentaire_tuteur   text
);

alter table demande_sessions
    owner to admin;

create table sessions
(
    id                 bigserial
        primary key,
    tuteur_id          bigint    not null
        references users
            on delete cascade,
    etudiant_id        bigint    not null
        references users
            on delete cascade,
    matiere_id         bigint    not null
        references matieres
            on delete cascade,
    demande_session_id bigint
                                 references demande_sessions
                                     on delete set null,
    date_heure         timestamp not null,
    duree              integer   not null
        constraint sessions_duree_check
            check (duree > 0),
    statut             varchar(20) default 'DEMANDEE'::character varying
        constraint sessions_statut_check
            check ((statut)::text = ANY
                   ((ARRAY ['demandee'::character varying, 'confirmee'::character varying, 'en_cours'::character varying, 'terminee'::character varying, 'annulee'::character varying])::text[])),
    prix               numeric(10, 2)
        constraint sessions_prix_check
            check (prix >= (0)::numeric),
    type_session       varchar(20) default 'INDIVIDUELLE'::character varying
        constraint sessions_type_session_check
            check ((type_session)::text = ANY
                   ((ARRAY ['en_ligne'::character varying, 'presentiel'::character varying])::text[])),
    mode_session       varchar(20) default 'EN_LIGNE'::character varying
        constraint sessions_mode_session_check
            check ((mode_session)::text = ANY
                   ((ARRAY ['EN_LIGNE'::character varying, 'PRESENTIEL'::character varying, 'HYBRIDE'::character varying])::text[])),
    lien_session       varchar(500),
    lieu_session       varchar(200),
    notes_tuteur       text,
    notes_etudiant     text,
    documents_partages text,
    date_creation      timestamp   default CURRENT_TIMESTAMP,
    date_modification  timestamp   default CURRENT_TIMESTAMP,
    lien_visio         varchar(500),
    salle              varchar(200),
    notes              text,
    created_at         timestamp   default CURRENT_TIMESTAMP,
    updated_at         timestamp   default CURRENT_TIMESTAMP,
    version            bigint      default 0
);

comment on table sessions is 'Tutoring sessions with scheduling and status information';

alter table sessions
    owner to admin;

create index idx_sessions_tuteur
    on sessions (tuteur_id);

create index idx_sessions_etudiant
    on sessions (etudiant_id);

create index idx_sessions_matiere
    on sessions (matiere_id);

create index idx_sessions_date
    on sessions (date_heure);

create index idx_sessions_statut
    on sessions (statut);

create index idx_sessions_tuteur_date
    on sessions (tuteur_id, date_heure);

create trigger update_sessions_updated_at
    before update
    on sessions
    for each row
    execute procedure update_updated_at_column();

create table evaluations
(
    id                   bigserial
        primary key,
    session_id           bigint      not null
        references sessions
            on delete cascade,
    evaluateur_id        bigint      not null
        references users
            on delete cascade,
    evalue_id            bigint      not null
        references users
            on delete cascade,
    note                 integer     not null
        constraint evaluations_note_check
            check ((note >= 1) AND (note <= 5)),
    commentaire          text,
    date                 timestamp default CURRENT_TIMESTAMP,
    type_evaluation      varchar(20) not null
        constraint evaluations_type_evaluation_check
            check ((type_evaluation)::text = ANY
        ((ARRAY ['TUTEUR_PAR_ETUDIANT'::character varying, 'ETUDIANT_PAR_TUTEUR'::character varying, 'SESSION'::character varying])::text[])),
    qualite_enseignement integer
        constraint evaluations_qualite_enseignement_check
            check ((qualite_enseignement IS NULL) OR ((qualite_enseignement >= 1) AND (qualite_enseignement <= 5))),
    communication        integer
        constraint evaluations_communication_check
            check ((communication IS NULL) OR ((communication >= 1) AND (communication <= 5))),
    ponctualite          integer
        constraint evaluations_ponctualite_check
            check ((ponctualite IS NULL) OR ((ponctualite >= 1) AND (ponctualite <= 5))),
    preparation          integer
        constraint evaluations_preparation_check
            check ((preparation IS NULL) OR ((preparation >= 1) AND (preparation <= 5))),
    patience             integer
        constraint evaluations_patience_check
            check ((patience IS NULL) OR ((patience >= 1) AND (patience <= 5))),
    recommanderais       boolean,
    created_at           timestamp default CURRENT_TIMESTAMP,
    updated_at           timestamp default CURRENT_TIMESTAMP,
    version              bigint    default 0
);

comment on table evaluations is 'Ratings and feedback for sessions and users';

comment on column evaluations.qualite_enseignement is 'Teaching quality rating (1-5)';

comment on column evaluations.communication is 'Communication skills rating (1-5)';

comment on column evaluations.ponctualite is 'Punctuality rating (1-5)';

comment on column evaluations.preparation is 'Preparation level rating (1-5)';

comment on column evaluations.patience is 'Patience rating (1-5)';

comment on column evaluations.recommanderais is 'Would recommend this person';

alter table evaluations
    owner to admin;

create index idx_evaluations_session
    on evaluations (session_id);

create index idx_evaluations_evaluateur
    on evaluations (evaluateur_id);

create index idx_evaluations_evalue
    on evaluations (evalue_id);

create index idx_evaluations_type
    on evaluations (type_evaluation);

create trigger trigger_evaluations_updated_at
    before update
    on evaluations
    for each row
    execute procedure update_evaluations_updated_at();

create table paiements
(
    id                    bigserial
        primary key,
    session_id            bigint         not null
        references sessions
            on delete cascade,
    montant               numeric(10, 2) not null
        constraint paiements_montant_check
            check (montant > (0)::numeric),
    methode_paiement      varchar(20)    not null
        constraint paiements_methode_paiement_check
            check ((methode_paiement)::text = ANY
                   ((ARRAY ['CARTE_CREDIT'::character varying, 'PAYPAL'::character varying, 'VIREMENT'::character varying, 'ESPECES'::character varying])::text[])),
    statut                varchar(20) default 'EN_ATTENTE'::character varying
        constraint paiements_statut_check
            check ((statut)::text = ANY
                   ((ARRAY ['EN_ATTENTE'::character varying, 'PAYE'::character varying, 'REMBOURSE'::character varying, 'ECHOUE'::character varying])::text[])),
    date_paiement         timestamp   default CURRENT_TIMESTAMP,
    date_traitement       timestamp,
    reference_transaction varchar(100),
    commentaires          text
);

comment on table paiements is 'Payment tracking for tutoring sessions';

alter table paiements
    owner to admin;

create table planning
(
    id                bigserial
        primary key,
    tuteur_id         bigint  not null
        references users
            on delete cascade,
    jour_semaine      integer not null
        constraint planning_jour_semaine_check
            check ((jour_semaine >= 1) AND (jour_semaine <= 7)),
    heure_debut       time    not null,
    heure_fin         time    not null,
    disponible        boolean   default true,
    date_creation     timestamp default CURRENT_TIMESTAMP,
    date_modification timestamp default CURRENT_TIMESTAMP,
    constraint chk_planning_time
        check (heure_debut < heure_fin)
);

comment on table planning is 'Weekly availability schedules for tutors';

alter table planning
    owner to admin;

create index idx_planning_tuteur
    on planning (tuteur_id);

create index idx_planning_jour
    on planning (jour_semaine);

create index idx_planning_disponible
    on planning (disponible);

create trigger update_planning_updated_at
    before update
    on planning
    for each row
    execute procedure update_updated_at_column();

create table creneaux_disponibles
(
    id            bigserial
        primary key,
    tuteur_id     bigint    not null
        references users
            on delete cascade,
    date_debut    timestamp not null,
    date_fin      timestamp not null,
    disponible    boolean   default true,
    prix_horaire  numeric(10, 2)
        constraint creneaux_disponibles_prix_horaire_check
            check (prix_horaire >= (0)::numeric),
    notes         text,
    date_creation timestamp default CURRENT_TIMESTAMP,
    constraint chk_creneau_time
        check (date_debut < date_fin)
);

comment on table creneaux_disponibles is 'Specific time slots available for booking';

alter table creneaux_disponibles
    owner to admin;

create table conversations
(
    id                     bigserial
        primary key,
    nom                    varchar(200),
    type                   varchar(20) default 'PRIVE'::character varying
        constraint conversations_type_check
            check ((type)::text = ANY
                   ((ARRAY ['PRIVE'::character varying, 'GROUPE'::character varying, 'SUPPORT'::character varying])::text[])),
    date_creation          timestamp   default CURRENT_TIMESTAMP,
    date_derniere_activite timestamp   default CURRENT_TIMESTAMP,
    archivee               boolean     default false
);

comment on table conversations is 'Chat conversations between users';

alter table conversations
    owner to admin;

create table conversation_participants
(
    id              bigserial
        primary key,
    conversation_id bigint not null
        references conversations
            on delete cascade,
    user_id         bigint not null
        references users
            on delete cascade,
    role            varchar(20) default 'PARTICIPANT'::character varying
        constraint conversation_participants_role_check
            check ((role)::text = ANY
                   ((ARRAY ['PARTICIPANT'::character varying, 'MODERATEUR'::character varying, 'ADMIN'::character varying])::text[])),
    date_adhesion   timestamp   default CURRENT_TIMESTAMP,
    active          boolean     default true,
    unique (conversation_id, user_id)
);

alter table conversation_participants
    owner to admin;

create table messages
(
    id                bigserial
        primary key,
    conversation_id   bigint not null
        references conversations
            on delete cascade,
    expediteur_id     bigint not null
        references users
            on delete cascade,
    contenu           text   not null,
    type              varchar(20) default 'TEXTE'::character varying
        constraint messages_type_check
            check ((type)::text = ANY
                   ((ARRAY ['TEXTE'::character varying, 'IMAGE'::character varying, 'FICHIER'::character varying, 'LIEN'::character varying, 'SYSTEM'::character varying])::text[])),
    date_envoi        timestamp   default CURRENT_TIMESTAMP,
    lu                boolean     default false,
    date_lecture      timestamp,
    modifie           boolean     default false,
    date_modification timestamp
);

comment on table messages is 'Individual messages within conversations';

alter table messages
    owner to admin;

create index idx_messages_conversation
    on messages (conversation_id);

create index idx_messages_expediteur
    on messages (expediteur_id);

create index idx_messages_date
    on messages (date_envoi);

create index idx_messages_lu
    on messages (lu);

create table notifications
(
    id            bigserial
        primary key,
    user_id       bigint       not null
        references users
            on delete cascade,
    titre         varchar(200) not null,
    message       text         not null,
    type          varchar(30) default 'INFO'::character varying
        constraint notifications_type_check
            check ((type)::text = ANY
                   ((ARRAY ['INFO'::character varying, 'SUCCESS'::character varying, 'WARNING'::character varying, 'ERROR'::character varying, 'SESSION_REMINDER'::character varying, 'PAYMENT_DUE'::character varying, 'NEW_MESSAGE'::character varying])::text[])),
    lue           boolean     default false,
    date_creation timestamp   default CURRENT_TIMESTAMP,
    date_lecture  timestamp,
    lien_action   varchar(500)
);

comment on table notifications is 'System notifications for users';

alter table notifications
    owner to admin;

create index idx_notifications_user
    on notifications (user_id);

create index idx_notifications_type
    on notifications (type);

create index idx_notifications_lue
    on notifications (lue);

create index idx_notifications_date
    on notifications (date_creation);

create table user_audit_logs
(
    id          bigserial
        primary key,
    user_id     bigint
                            references users
                                on delete set null,
    action      varchar(50) not null,
    table_name  varchar(50) not null,
    record_id   bigint,
    old_values  jsonb,
    new_values  jsonb,
    ip_address  inet,
    user_agent  text,
    date_action timestamp default CURRENT_TIMESTAMP
);

alter table user_audit_logs
    owner to admin;

create table tutors
(
    id                 bigint         not null
        primary key
        constraint fk_tutors_users
            references users
            on delete cascade,
    experience         text,
    tarif_horaire      numeric(10, 2) not null
        constraint tutors_tarif_horaire_check
            check (tarif_horaire >= (0)::numeric),
    diplomes           text,
    description        text,
    ville              varchar(100),
    pays               varchar(100)  default 'France'::character varying,
    cours_en_ligne     boolean       default true,
    cours_presentiel   boolean       default false,
    duree_session_min  integer       default 60
        constraint tutors_duree_session_min_check
            check (duree_session_min > 0),
    duree_session_max  integer       default 180
        constraint tutors_duree_session_max_check
            check (duree_session_max > 0),
    note_moyenne       numeric(3, 2) default 0.0
        constraint tutors_note_moyenne_check
            check ((note_moyenne >= (0)::numeric) AND (note_moyenne <= (5)::numeric)),
    nombre_evaluations integer       default 0
        constraint tutors_nombre_evaluations_check
            check (nombre_evaluations >= 0),
    total_sessions     integer       default 0
        constraint tutors_total_sessions_check
            check (total_sessions >= 0),
    verifie            boolean       default false,
    date_verification  timestamp,
    disponible         boolean       default true,
    created_at         timestamp     default CURRENT_TIMESTAMP,
    updated_at         timestamp     default CURRENT_TIMESTAMP,
    version            bigint        default 0,
    constraint chk_session_duration
        check (duree_session_min <= duree_session_max),
    constraint chk_teaching_method
        check (cours_en_ligne OR cours_presentiel)
);

comment on table tutors is 'Tutors table with teaching-specific information and ratings. ID references users table.';

alter table tutors
    owner to admin;

create table tutor_specialites
(
    id         bigserial
        primary key,
    tutor_id   bigint not null
        constraint fk_tutor_specialites_tutor
            references tutors
            on delete cascade,
    matiere_id bigint not null
        constraint fk_tutor_specialites_matiere
            references matieres
            on delete cascade,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP,
    constraint uk_tutor_matiere
        unique (tutor_id, matiere_id)
);

alter table tutor_specialites
    owner to admin;

create index idx_tutor_specialites_tutor_id
    on tutor_specialites (tutor_id);

create index idx_tutor_specialites_matiere_id
    on tutor_specialites (matiere_id);

create index idx_tutor_specialites_created_at
    on tutor_specialites (created_at);

create trigger tr_tutor_specialites_updated_at
    before update
    on tutor_specialites
    for each row
    execute procedure update_tutor_specialites_timestamp();

create index idx_tutors_ville
    on tutors (ville);

create index idx_tutors_tarif
    on tutors (tarif_horaire);

create index idx_tutors_note
    on tutors (note_moyenne);

create index idx_tutors_disponible
    on tutors (disponible);

create index idx_tutors_verifie
    on tutors (verifie);

create index idx_tutors_rating_price
    on tutors (note_moyenne desc, tarif_horaire asc);

create trigger update_tutors_updated_at
    before update
    on tutors
    for each row
    execute procedure update_updated_at_column();