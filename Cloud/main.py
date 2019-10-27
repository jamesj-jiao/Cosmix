import firebase_admin
from firebase_admin import credentials, firestore
import utils.
from utils import get_val_from_request
import json
import secrets
import string
from GeneratePlaylist.GeneratePlaylist import create_playlist
from napster import *
from AddToPartyPlaylist import *

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
    party_id = get_val_from_request(request, 'codeName')
    db.collection('parties').doc(party_id).set({
        'allTracks': [],
        'filtTracks': [],
    })

def check_party(request):
    party_id = get_val_from_request(request, 'id')
    party_ref = db.collection('parties').doc(party_id)
    try:
        party_ref.get()
        return 'Found'
    except:
        return 'Not found'

def gen_playlist(request):
    name = get_val_from_request(request, 'name')
    num_songs = get_val_from_request(request, 'numSongs')
    party_id = get_val_from_request(request, 'partyID')

    isrc_list = db.collection('parties').doc(party_id).get()['filtTracks']

    create_genre_json(isrc_list)

    create_playlist(name, num_songs)

    #FINISH!!

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
    party_id = get_val_from_request(request, 'id')
    combined_id = get_val_from_request(request, 'playlist')
    token = get_val_from_request(request, 'token')

    party_ref = db.collection('parties').doc(party_id)
    sp = spotipy.Spotify(auth=token)

    service, playlist_id = combined_id.split('/')
    track_isrcs = SERVICES[service]['track_isrcs'](playlist_id, token)

    party_ref.update({'allTracks': firestore.ArrayUnion(track_isrcs)})

    party_ref.update({'filtTracks': add_to_party_playlist(sp, track_isrcs, num = 10)})
