import requests
import spotipy
import secretstuff
from schema import Playlist

def next_or_null(it, key):
    if len(it) > 0:
        return it[0][key]
    return None

def spotify_playlists(token):
    sp = spotipy.Spotify(auth=token)
    playlists = sp.current_user_playlists()
    for playlist in playlists['items']:
        yield Playlist(id=playlist['id'],
                       name=playlist['name'],
                       image=next_or_null(playlist['images'], key='url')
        )

def id_to_isrc(sp, spotify_id):
    """Convert a spotify id to an isrc."""
    return sp.track(spotify_id)['external_ids']['isrc']

def isrc_to_id(sp, isrc):
    return sp.search(q='isrc:{}'.format(isrc), type='track')['tracks']['items'][0]['id']

def features_from_id(sp, spotify_id):
    return sp.audio_features(sp, spotify_id)[0]

def isrc_to_facts(sp, isrc):
    track = sp.track(isrc_to_id(sp, isrc))
    return {'name': track['name'], 'artist': track['artists'][0]['name']}

def get_audio_features(sp, isrc):
    #isrc = get_val_from_request(request, 'isrc')
    return features_from_id(isrc_to_id(sp, isrc))

def spotify_track_isrcs(playlist_id, token):
    sp = spotipy.Spotify(auth=token)
    isrc_list = []
    for item in get_playlist_tracks(sp, playlist_id)['items']:
        spotify_id = item['track']['id']
        isrc_list.append(id_to_isrc(sp, spotify_id))
    return isrc_list

def get_playlist_tracks(sp, playlist_id, limit=100, offset=0):
    """
    Get full details of the tracks of a playlist owned by a Spotify user. Link to api doc as of 08/17/2019:
        - https://developer.spotify.com/documentation/web-api/reference/playlists/get-playlists-tracks/
    Parameters:
        - playlist_id: the id of the playlist
        - limit: maximum number of tracks to return
        - offset: index of the first track to return
    """
    plid = sp._get_id('playlist', playlist_id)
    return sp._get("playlists/{}/tracks?limit={}&offset={}".format(plid, limit, offset))

def add_tracks(token, playlist_id, track_ids):
    if token:
        sp = spotipy.Spotify(auth=token)
        sp.trace = False
        # username = sp.me()
        # return username

        results = playlist_add_tracks(sp, playlist_id, track_ids)
        return results
    else:
        return "Can't get token"

def playlist_add_tracks(sp, playlist_id, tracks, position=None):
    plid = sp._get_id('playlist', playlist_id)
    ftracks = [sp._get_uri('track', tid) for tid in tracks]
    return sp._post("playlists/%s/tracks" % plid,
                      payload=ftracks, position=position)

def playlist_to_features_dict(sp, playlist_id):
    #user_id = get_val_from_request(request, 'user')
    #playlist_id = get_val_from_request(request, 'playlist')
    d = {}
    for item in get_playlist_tracks(sp, playlist_id)['items']:
        spotify_id = item['track']['id']
        d[id_to_isrc(sp, spotify_id)] = features_from_id(sp, spotify_id)
    return str(d)
