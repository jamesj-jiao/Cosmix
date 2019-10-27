import firebase_admin
from firebase_admin import credentials, firestore
import utils
from utils import get_val_from_request
import json
import secrets
import string
from Cloud.GeneratePlaylist.GeneratePlaylist import create_playlist
from Cloud.napster import *
from Cloud.AddToPartyPlaylist import *

CODE_ALPHABET = string.ascii_letters + string.digits

default_app = firebase_admin.initialize_app()
db = firestore.client()

SERVICES = {
    'apple': {
        'playlists': utils.apple_playlists,
        'track_isrcs': utils.apple_track_isrcs,
    },
    'spotify': {
        'playlists': utils.spotify_playlists,
        'track_isrcs': utils.spotify_track_isrcs,
    }
}

def new_party(request):
    pid = ''.join(secrets.choice(CODE_ALPHABET) for _ in range(10))
    db.collection('parties').doc(pid).set({
        'allTracks': [],
    })
    return pid

def check_party(request):
    pid = get_val_from_request(request, 'id')
    party_ref = db.collection('parties').document(pid)
    try:
        party_ref.get()
        return 'Found'
    except:
        return 'Not found'

def join_party(request):
    pid = get_val_from_request(request, 'id')
    # username = get_val_from_request(request, 'username')

    party_ref = db.collection('parties').document(pid)
    # party_ref.update({'members': firestore.ArrayUnion([username])})


def gen_playlist(request):
    name = get_val_from_request(request, 'name')
    num_songs = get_val_from_request(request, 'numSongs')
    group_isrc_list = get_val_from_request(request,'isrc')

    create_genre_json(group_isrc_list)

    create_playlist(name, num_songs)

    #FINISH

def playlists(request):
    """Return the user's playlists for a given token and service."""
    service = get_val_from_request(request, 'service')
    token = get_val_from_request(request, 'token')
    playlists = list(SERVICES[service]['playlists'](token))
    for p in playlists:
        p.id = service + '/' + p.id
    return json.dumps(playlists)

def add(request):
    """Add all songs from a given playlist to the group's music."""
    group_id = get_val_from_request(request, 'group')
    combined_id = get_val_from_request(request, 'playlist')
    token = get_val_from_request(request, 'token')

    service, playlist_id = combined_id.split('/')

    track_isrcs = SERVICES[service]['track_isrcs'](playlist_id, token)
    features = utils.features(track_isrcs)
    utils.add_features(group_id, features)
