
CREATE TABLE users (
--record identifier
  id  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email varchar(254) NOT NULL,
  password_hash varchar(100) NOT NULL,
  display_name    varchar(50),
  daily_calorie_goal int check(daily_calorie_goal>0),
  timezone    varchar(100) not null default 'UTC',    -- "today" is user-local!
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now()
);


CREATE UNIQUE INDEX ux_users_email_lower ON users (lower(email));
