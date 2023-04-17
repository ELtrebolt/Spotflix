LOAD DATA LOCAL INFILE '~/load-queries/AllSavedTracks.csv'
INTO TABLE songs
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(Id,Title,Album,@DateLiked)
SET DateLiked = STR_TO_DATE(@DateLiked, '%m/%d/%Y');

LOAD DATA LOCAL INFILE '~/load-queries/Artists.csv'
INTO TABLE artists
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

LOAD DATA LOCAL INFILE '~/load-queries/ArtistsInSongs.csv'
INTO TABLE artists_in_songs
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

LOAD DATA LOCAL INFILE '~/load-queries/Genres.csv'
INTO TABLE genres
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

LOAD DATA LOCAL INFILE '~/load-queries/GenresOfArtists.csv'
INTO TABLE genres_of_artists
FIELDS TERMINATED BY ',' ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;

-- Data must not have header AND you must PreProcess every Null to 0
LOAD DATA LOCAL INFILE '~/load-queries/TopSongs.csv'
INTO TABLE top_songs
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(id, @vtwo, @vthree, @vfour)
SET 
ShortRank = NULLIF(@vtwo,0),
MedRank = NULLIF(@vthree,0),
LongRank = NULLIF(@vfour,0);
