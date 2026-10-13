-- CB cloud sync schema (run in Supabase SQL Editor).
-- Never put the Supabase service_role key in frontend code.
create table if not exists public.user_state (
  user_id uuid primary key references auth.users(id) on delete cascade,
  state jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now(),
  constraint user_state_state_object check (jsonb_typeof(state) = 'object')
);

alter table public.user_state enable row level security;

drop policy if exists "Users can read their own CB state" on public.user_state;
create policy "Users can read their own CB state"
  on public.user_state for select
  to authenticated
  using ((select auth.uid()) = user_id);

drop policy if exists "Users can insert their own CB state" on public.user_state;
create policy "Users can insert their own CB state"
  on public.user_state for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

drop policy if exists "Users can update their own CB state" on public.user_state;
create policy "Users can update their own CB state"
  on public.user_state for update
  to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

-- No public/anonymous policies are created. Authenticated users can only
-- access the row whose user_id matches their verified Supabase auth identity.

-- Explicit least-privilege grants; RLS remains the row-level boundary.
revoke all on public.user_state from anon;
grant select, insert, update on public.user_state to authenticated;

-- Enable realtime row-change events for cross-device sync (safe to re-run).
do $$
begin
  if exists (select 1 from pg_publication where pubname = 'supabase_realtime')
     and not exists (
       select 1 from pg_publication_tables
       where pubname = 'supabase_realtime'
         and schemaname = 'public'
         and tablename = 'user_state'
     ) then
    execute 'alter publication supabase_realtime add table public.user_state';
  end if;
end $$;
