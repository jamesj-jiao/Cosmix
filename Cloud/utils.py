  
import spotipy
import requests
from spotipy.oauth2 import SpotifyClientCredentials

CLIENT_ID = '2fd46a7902e043e4bcb8ccda3d1381b2'
CLIENT_SECRET = 'c059fdd2c9aa40c9be2a740ddc6151f9'


def get_spotify():
    client_credentials_manager = SpotifyClientCredentials(CLIENT_ID, CLIENT_SECRET)
    return spotipy.Spotify(client_credentials_manager=client_credentials_manager)


spotify = get_spotify()


def get_val_from_request(request, key):
    request_json = request.get_json()
    if request.args and key in request.args:
        return request.args.get(key)
    elif request_json and key in request_json:
        return request_json[key]

def isrc_to_id(isrc):
    return spotify.search(q='isrc:{}'.format(isrc), type='track')['tracks']['items'][0]['id']


def id_to_isrc(spotify_id):
    return spotify.track(spotify_id)['external_ids']['isrc']


def features_from_id(spotify_id):
    return spotify.audio_features(spotify_id)[0]

def isrc_to_facts(isrc):
    track = spotify.track(isrc_to_id(isrc))
    return {'name': track['name'], 'artist': track['artists'][0]['name']}

def get_audio_features(isrc):
    #isrc = get_val_from_request(request, 'isrc')
    return features_from_id(isrc_to_id(isrc))

def apple_req(url, devkey, key, **kwargs):
    r = requests.get('https://api.music.apple.com' + url,
                     headers={
                         'Music-User-Token': key,
                         'Authorization': 'Bearer ' + devkey
                     }, **kwargs)
    resp = r.json()
    if 'data' not in resp:
        raise ValueError(resp)
    return resp

def apple_playlists(devkey, key):
    resp = apple_req('/v1/me/library/playlists', devkey, key)
    for playlist in resp['data']:
        yield parseplaylist(playlist, devkey, key)
    if 'next' in resp:
        raise Exception('More playlists')

def parseplaylist(playlist, devkey, key):
    p = {
        'id': playlist['id'],
        'name': playlist['attributes']['name'],
    }
    if 'description' in playlist['attributes']:
        p['description'] = playlist['attributes']['description']['standard']
    if 'artwork' in playlist['attributes']:

        p['artwork'] = playlist['attributes']['artwork']['url']
    if 'relationships' in playlist:
        p['tracks'] = []
        addTracks(playlist['relationships']['tracks']['data'], p['tracks'])
        if 'next' in playlist['relationships']['tracks']:
            print('adding more')
            addMoreTracks(playlist['relationships']['tracks']['next'], p['tracks'], devkey, key)
    return p

def addTracks(ptracks, otracks):
    for track in ptracks:
        if 'playParams' not in track['attributes']:
            print('Track does not have playParams', track)
            continue
        otracks.append({
            'id': track['id'],
            'artist': track['attributes']['artistName'],
            'genres': track['attributes']['genreNames'],
            'name': track['attributes']['name'],
            'album': track['attributes']['albumName'],
            'catalogId': track['attributes']['playParams']['catalogId']
        })

def addMoreTracks(url, tracks, devkey, key):
    resp = apple_req(url, devkey, key)
    addTracks(resp['data'], tracks)
    if 'next' in resp:
        print('adding more more')
        addMoreTracks(resp['next'], tracks, devkey, key)

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

def get_playlists(token):
    if token:
        sp = spotipy.Spotify(auth=token)
        playlists = sp.current_user_playlists()
        results = []
        for playlist in playlists['items']:
            results.append(playlist['id'])
        return results
    else:
        return "Can't get token"

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

def get_playlist(sp, playlist_id):
    """
    Get a playlist owned by a spotify user. Link to api doc as of 08/17/2019:
        - https://developer.spotify.com/documentation/web-api/reference/playlists/get-playlist/
    Parameters:
        - playlist_id: the id of the playlist
    """
    plid = sp._get_id('playlist', playlist_id)
    return sp._get("playlists/{}".format(plid))

def playlist_to_features_dict(sp, playlist_id):
    #user_id = get_val_from_request(request, 'user')
    #playlist_id = get_val_from_request(request, 'playlist')
    d = {}
    for item in get_playlist_tracks(sp, playlist_id)['items']:
        spotify_id = item['track']['id']
        d[id_to_isrc(spotify_id)] = features_from_id(spotify_id)
    return str(d)

def playlist_to_isrc(sp, playlist_id):
    isrc_list = []
    for item in get_playlist_tracks(sp, playlist_id)['items']:
        spotify_id = item['track']['id']
        isrc_list.append(id_to_isrc(spotify_id))
    return isrc_list

def playlist_add_tracks(sp, playlist_id, tracks, position=None):
    plid = sp._get_id('playlist', playlist_id)
    ftracks = [sp._get_uri('track', tid) for tid in tracks]
    return sp._post("playlists/%s/tracks" % plid,
                      payload=ftracks, position=position)