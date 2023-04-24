LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/AllSavedTracks-sample.csv'
INTO TABLE songs
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS
(Id,Title,Album,@DateLiked)
SET DateLiked = STR_TO_DATE(@DateLiked, '%m/%d/%Y');

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/Artists-sample.csv'
INTO TABLE artists
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/ArtistsInSongs-sample.csv'
INTO TABLE artists_in_songs
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/Genres-sample.csv'
INTO TABLE genres
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/GenresOfArtists-sample.csv'
INTO TABLE genres_of_artists
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/TopSongs-sample.csv'
INTO TABLE top_songs
FIELDS TERMINATED BY ',' ENCLOSED BY '\"'
LINES TERMINATED BY '\r\n'
(id, @vtwo, @vthree, @vfour)
SET
ShortRank = NULLIF(@vtwo,0),
MedRank = NULLIF(@vthree,0),
LongRank = NULLIF(@vfour,0);