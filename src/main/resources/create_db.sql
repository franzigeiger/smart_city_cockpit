drop table if exists StopsInLineDB cascade;
drop table if exists TimeBetweenStopsDB cascade;
drop table if exists TourDB cascade;
drop table if exists NotificationDB cascade;
drop table if exists FeedbackDB cascade;
drop table if exists VehicleDB cascade;
drop table if exists StopDB cascade;
drop table if exists LineDB cascade;
drop table if exists NotificationDB cascade;
drop table if exists ServiceDB cascade;
drop table if exists EventDB cascade;


CREATE TABLE VehicleDB (
    id varchar primary key,
    type varchar not null,
    deleted boolean not null
);

CREATE TABLE StopDB (
    id varchar primary key,
    name varchar not null,
    latitude varchar not null,
    longitude varchar not null
);

CREATE TABLE LineDB (
    id varchar primary key,
    name varchar not null,
    color varchar not null, --hex code,
    type varchar not null
);

CREATE TABLE StopsInLineDB (
    line varchar references LineDB,
    stop varchar references StopDB,
    positionStopOnLine int not null CHECK(positionStopOnLine >= 0),
    primary key (line, stop)
);

CREATE TABLE TourDB (
    id int primary key,
    start_time timestamp not null,
    end_time timestamp not null,
    start_stop varchar not null references StopDB,
    end_stop varchar not null references StopDB,
    line varchar not null references LineDB,
    vehicle varchar references VehicleDB
);

CREATE TABLE NotificationDB (
    id int primary key,
    line varchar not null references LineDB,
    stop varchar references StopDB, -- when stop is null the notification is for the whole line
    description varchar not null
);

CREATE TABLE  ServiceDB (
    id int primary key,
    serviceType varchar not null,
    executionDate timestamp not null,
    description varchar not null,
    done bit not null
);

CREATE TABLE EventDB (
    id int primary key,
    title varchar not null,
    description varchar,
    start_timestamp timestamp not null,
    end_timestamp timestamp not null,
    location varchar
);

CREATE TABLE TimeBetweenStopsDB (
    startStop varchar references StopDB,
    nextStop varchar references StopDB,
    timeInMinutes int not null CHECK(timeInMinutes >= 0),
    primary key (startStop, nextStop)
);

CREATE OR REPLACE FUNCTION id_in_stop_vehicle_line(_id text)
    RETURNS bool AS
$func$
SELECT EXISTS (
    SELECT 1 FROM LineDB WHERE id=$1 UNION
    SELECT 1 FROM VehicleDB WHERE id=$1 UNION
    SELECT 1 FROM StopDB WHERE id=$1);
$func$  LANGUAGE sql STABLE;


CREATE TABLE FeedbackDB (
    id int primary key,
    content varchar not null,
    commit_time timestamp not null,
    finished boolean not null,
    reason varchar not null,
    placeType varchar not null,
    placeInstance varchar not null,
    CHECK(placeType != 'General' OR (placeType = 'General' AND placeinstance = '')),
    CHECK(placeinstance = '' or id_in_stop_vehicle_line(placeInstance))
);