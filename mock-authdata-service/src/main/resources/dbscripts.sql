create schema "mockauthdata";

create table "mockauthdata"."mock_identity" ("individual_id" varchar(36) not null, "identity_json" varchar(2048) not null);

alter table "mockauthdata"."mock_identity" add constraint "pk_mock_id_code" primary key ("individual_id");