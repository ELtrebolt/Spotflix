CREATE DATABASE IF NOT EXISTS espotify;
USE espotify;

CREATE TABLE IF NOT EXISTS songs(
               Id varchar(22) primary key,
               Title varchar(200) CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci not null,
	   Album varchar(100),
               DateLiked date
           );

CREATE TABLE IF NOT EXISTS artists(
               Id varchar(22) primary key,
               Name varchar(100) CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci not null
           );

CREATE TABLE IF NOT EXISTS artists_in_songs(
               ArtistId varchar(22) DEFAULT '',
               SongId varchar(22) DEFAULT '',
	   FOREIGN KEY (ArtistId) REFERENCES artists(Id),
       	   FOREIGN KEY (SongId) REFERENCES songs(Id)
           );

CREATE TABLE IF NOT EXISTS genres(
               Name varchar(100) CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci primary key,
	   Id INT not null
           );

CREATE TABLE IF NOT EXISTS genres_of_artists(
               Genre varchar(100) CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '',
               ArtistId varchar(22) DEFAULT '',
	   FOREIGN KEY (Genre) REFERENCES genres(Name),
       	   FOREIGN KEY (ArtistId) REFERENCES artists(Id)
           );

CREATE TABLE IF NOT EXISTS top_songs(
               Id varchar(22) primary key,
	   ShortRank INT,
	   MedRank INT,
	   LongRank INT,
       FOREIGN KEY (Id) REFERENCES songs(Id)
           );
