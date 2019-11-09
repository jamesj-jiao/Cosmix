from google.cloud import firestore
import google.cloud.exceptions
import utils
#from utils import get_val_from_request
# import json
# import secrets
import string
from schema import Playlist
from GenerateFilter.GenerateFilterDB import generate_filter
from napster import *
from AddToPartyPlaylist import *

CODE_ALPHABET = string.ascii_letters + string.digits

db = firestore.Client()

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
    party_id = get_val_from_request(request, 'id')
    db.collection('parties').document(party_id).set({'allTracks': [], 'filtTracks': []})
    return 'Success'


def check_party(request):
    party_id = get_val_from_request(request, 'id')
    return json.dumps(dict(result=db.collection('parties').document(party_id).get().exists))

    #party_id = get_val_from_request(request, 'id')
    #party_ref = db.collection('parties').document(party_id)


    #try:
    #    party_ref.get().to_dict()
    #    return 'Found'
    #except google.cloud.exceptions.NotFound:
    #    return 'Not found'


def get_facts(request):
    isrc = get_val_from_request(request, 'isrc')
    if isrc:
        return json.dumps(isrc_to_facts(isrc))
    return "Problem with get_facts"

def gen_filter(request):
    filter_name = get_val_from_request(request, 'name')
    num_songs = get_val_from_request(request, 'numSongs')
    party_id = get_val_from_request(request, 'id')

    all_isrcs = db.collection('parties').document(party_id).get().to_dict()['allTracks']

    new_isrcs = generate_filter(create_genre_json(all_isrcs), filter_name, int(num_songs))

    return json.dumps(new_isrcs)

def save_isrcs(request):
    isrc_list = get_val_from_request(request, "isrcs")
    playlist_name = get_val_from_request(request, "name")
    token = get_val_from_request(request, "token")
    utils.new_playlist(playlist_name, isrc_list, token)

def playlists(request):
    """Return the user's playlists for a given token and service."""
    service = get_val_from_request(request, 'service')
    token = get_val_from_request(request, 'token')
    playlists = list(SERVICES[service]['playlists'](token))
    dict_playlists = [dict(id=service + '/' + p.id, name=p.name, image=p.image) for p in playlists]
    return json.dumps(dict_playlists)


def add(request):
    """Add all songs from a given playlist to the group's music."""
    party_id = get_val_from_request(request, 'id')
    combined_id = get_val_from_request(request, 'playlist')
    token = get_val_from_request(request, 'token')

    party_ref = db.collection('parties').document(party_id)

    service, playlist_id = combined_id.split('/')
    track_isrcs = SERVICES[service]['track_isrcs'](playlist_id, token)

    party_ref.update({'allTracks': firestore.ArrayUnion(track_isrcs)})
    party = db.collection('parties').document(party_id).get().to_dict()
    isrcs = party['allTracks']

    sp = spotipy.Spotify(auth=token)
    party_ref.update({'filtTracks': add_to_party_playlist(sp, isrcs, num=10)})

def save(request):
    """Add filtered songs to a playlist."""
    party_id = get_val_from_request(request, 'id')
    name = get_val_from_request(request, 'name')
    token = get_val_from_request(request, 'token')

    party = db.collection('parties').document(party_id).get().to_dict()
    isrcs = party['filtTracks']
    utils.new_playlist(name, isrcs, token)
