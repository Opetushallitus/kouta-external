create table koulutus (
  uuid uuid primary key default gen_random_uuid(),
  external_id text not null,
  kielivalinta jsonb not null,
  tila text not null,
  nimi jsonb not null,
  tarjoajat jsonb not null,
  metadata jsonb,
  owner_org text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz,
  unique(owner_org, external_id)
);