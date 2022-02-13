--changeset nutkin:1
create schema calendar;

create sequence calendar.users_id_seq;

create table calendar.users
(
    id         int8 primary key default nextval('calendar.users_id_seq'),
    first_name varchar(50) not null,
    last_name  varchar(50) not null,
    version    int8             default 0
);

-- meeting
create sequence calendar.meeting_id_seq;

create table calendar.meetings
(
    id              int8 primary key                  default nextval('calendar.meeting_id_seq'),
    title           varchar(155)             not null,
    start_time      timestamp with time zone not null,
    end_time        timestamp with time zone not null,
    status          varchar(10)              not null default 'OPEN',
    creator_user_id int8 references calendar.users (id),
    version         int8                              default 0
);

create index meetings_user_id_index on calendar.meetings (creator_user_id);
create index meetings_start_date_index on calendar.meetings (start_time);
create index meetings_end_date_index on calendar.meetings (end_time);


-- members
create table calendar.meeting_members
(
    user_id       int8 references calendar.users (id),
    meeting_id    int8 references calendar.meetings (id),
    member_status varchar(15) not null default 'NONE',
    primary key (user_id, meeting_id)
);

create index meeting_members_reverse_index on calendar.meeting_members (meeting_id, user_id);