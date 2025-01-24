select g.name,
       gpg.name,
       pg.name_property,
       pg.description
from games g
         left outer join games_properties gp on g.id = gp.id_game
         left outer join properties_games pg on gp.id_property = pg.id
         left outer join groups_properties_games gpg on pg.id_group = gpg.id
order by gp.id;

select g.link_id,
       g.name,
       gf.name,
       gf.approx_size,
       gpf.name,
       pf.name_property,
       pf.description
from files_properties fp
         left outer join games_files gf on fp.id_file = gf.id
         left outer join games g on gf.id_game = g.id
         left outer join properties_files pf on fp.id_property = pf.id
         left outer join groups_properties_files gpf on pf.id_group = gpf.id
order by g.id;

select g.name,
       g.link_id,
       tf.name,
       gf.name,
       gf.approx_size,
       gf.size,
       gf.date,
       gf.description,
       gf.provided,
       gf.link,
       gf.cause_load,
       gf.cause_unload
from games g
         left outer join games_files gf on g.id = gf.id_game
         left outer join types_files tf on gf.id_type = tf.id
where gf.cause_unload != ''
    and gf.cause_unload != 'only documents'
   or gf.cause_load != ''
    and gf.cause_load != 'not exist'
order by gf.date desc, g.id;

select g.name,
       g.link_id,
       gs.name         as "name scrn",
       gs.nn,
       gs.provided     as "provided",
       gs.description  as "description",
       gs.size         as "size",
       gs.cause_load   as "cause_load",
       gs.cause_unload as "cause_unload"
from games_screenshots gs
         left outer join games g on gs.id_game = g.id
where gs.cause_unload != ''
   or gs.cause_load != ''
    and gs.cause_load != 'not exist'
order by g.link_id, gs.nn
;

select sum(gs.size) / 1024 / 1024,
       count(1),
       max(gs.size)
from games_screenshots gs
         left outer join games g on gs.id_game = g.id
;

select sum(gf.size) / 1024 / 1024,
       count(1),
       max(gf.size)
from games_files gf
         left outer join games g on gf.id_game = g.id
;

select gw.link_id  as "link_id",
       gw.name     as "name",
       gw.released as "released"
from games_wasted gw
;

select gw.link_id  as "link_id",
       gw.name     as "name",
       gw.released as "released"
from games_saved gw
;

select g.link_id                                                as "link_id",
       '=ГИПЕРССЫЛКА(I' || row_number() over (order by link_id desc) + 1 || ';' || 'H' ||
       row_number() over (order by link_id desc) + 1 || ')'        as "hyper",
        g.released                                               as "released",
       case
           when g.wasted then '*'
           else ''
           end                                                  as "wasted",
       case
           when g.saved then '*'
           else ''
           end                                                  as "saved",
       case
           when g.documented then '*'
           else ''
           end                                                  as "documented",
       ''                                                       as "to check",
       g.name                                                   as "name",
       'https://www.old-games.ru/game/' || g.link_id || '.html' as "link",
       gnr.name                                                 as "genre",
       round(gfs.size / 1024 / 1024)                            as "size",
       gd.name                                                  as "developers",
       gp.name                                                  as "publishers",
       p.name                                                   as "platform",
       g.favorites                                              as "favorites",
       gl.language                                              as "language"
from games g
         left outer join genres gnr on g.id_genre = gnr.id
         left outer join platforms p on g.id_platform = p.id
         left outer join (select gan.id_game,
                                 string_agg(gan.name, ', ') as "name"
                          from games_alt_names gan
                          group by gan.id_game) gan on g.id = gan.id_game
         left outer join (select gd.id_game,
                                 string_agg(c.name, ', ') as "name"
                          from games_developers gd
                                   left outer join companies c on gd.id_company = c.id
                          group by gd.id_game) gd on g.id = gd.id_game
         left outer join (select gp.id_game,
                                 string_agg(c.name, ', ') as "name"
                          from games_publishers gp
                                   left outer join companies c on gp.id_company = c.id
                          group by gp.id_game) gp on g.id = gp.id_game
         left outer join (select gf.id_game,
                                 sum(gf.size) as "size"
                          from games_files gf
                          group by gf.id_game) gfs on g.id = gfs.id_game
         left outer join (select g.id,
                                 string_agg(pg.name_property, ', ') as "language"
                          from games g
                                   left outer join games_properties gp on g.id = gp.id_game
                                   left outer join properties_games pg on gp.id_property = pg.id
                                   left outer join groups_properties_games gpg on pg.id_group = gpg.id
                          where gpg.name = 'Язык'
                          group by g.id) gl on g.id = gl.id
order by g.id desc;
