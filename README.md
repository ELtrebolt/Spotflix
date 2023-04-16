# CS 122B Spring 2023 Project 1
For our project, we decided to work with a custom domain centered around displaying
a Spotify user's top liked songs. This was done by leveraging the endpoints of 
Spotify's Web API to construct the MySQL database around data specific to one our Spotify accounts.
 
### Youtube Demo URL:
#### TODO: URL

### Group Member Contributions:
#### Ethan Lee:

#### David Dao:

- Setup and configured details of AWS Instance.
- Managed the deployment of application.
- Implemented frontend interface and SQL queries for Single Artist Page.

### Custom Schema
#### Tables
- songs (movies):
    - id: varchar(22) (primary key)
    - Title: varchar(100)
    - Album: varchar(100)
    - DateLiked: date
- artists (stars):
    - id: varchar(22) (primary key)
    - NameL varchar(100)
- artists_in_songs (stars_in-movies):
    - ArtistId: varchar(22)
    - SongId: varchar(22)
- genres:
    - Name: varchar(100) (primary key)
    - id: int
- genres_in_songs (genres_in_movies):
    - Genre: varchar(100)
    - ArtistId: varchar(22)
- top_songs (ratings):
    - id: varchar(22) (primary key)
    - ShortRank: int
    - MedRank: int
    - LongRank: int