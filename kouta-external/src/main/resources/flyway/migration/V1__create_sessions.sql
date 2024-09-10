create table sessions (
  id         uuid primary key,
  cas_ticket character varying,
  person     character varying not null,
  last_read  timestamptz       not null default now()
);

create table authorities (
  session uuid references sessions (id) on delete cascade,
  authority    character varying not null
);

create index authorities_session_idx ON authorities (session);
