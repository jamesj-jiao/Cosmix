from VectorGeneration import *
from SimilarSongs import *

def add_to_party_playlist(sp, isrc_list, num):
	song_list = vector_generation(sp, isrc_list)
	return similar_songs(song_list, num)

#print(vector_generation("9l4ypaf0dsx8vkjtxhu28hxhm", "4n0zdWzKBaM1wpAlv7kd0I"))
#print(add_to_party_playlist("9l4ypaf0dsx8vkjtxhu28hxhm", "4n0zdWzKBaM1wpAlv7kd0I", 2))