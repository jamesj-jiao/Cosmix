import firebase_admin
from firebase_admin import credentials, firestore
from utils import isrc_to_id, id_to_isrc, isrc_to_facts, spotify, features_from_id #, get_val_from_request

default_app = firebase_admin.initialize_app()
db = firestore.client()

from utils import get_val_from_request, isrc_to_id, id_to_isrc, isrc_to_facts, spotify, features_from_id, apple_playlists
import utils
import json

#insert cloud functions