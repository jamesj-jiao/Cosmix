import numpy as np
from scipy.spatial import distance

#array implementation
#arr1 = ['id_1', 5, 5, 5, 5]
#arr2 = ['id_2', 1, 2, 2, 2]
#arr3 = ['id_3', 1, 1, 1, 1]
#arr4 = ['id_4', 1, 1.5, 1.5, 1.5]

#list = [arr1, arr2, arr3, arr4]

def similar_songs(song_list, num):
    def average_song(song_list):
        average_song = [0 for _ in range(len(song_list[0]) - 1)]
        for song in song_list:
            for i in range(len(average_song)):
                average_song[i] += song[i + 1]
        for i in range(len(average_song)):
            average_song[i] /= len(song_list)

        return average_song
    
    avg_song = average_song(song_list)

    def helper(song_list, avg_song, num):
        song_similarity_list = []
        similar_songs = []
        for i in range(len(song_list)):
            song_similarity_list.append([distance.euclidean(avg_song, song_list[i][1:]), i])
        song_similarity_list.sort(key = lambda song_pair: song_pair[0])
        for song_pair in song_similarity_list[:num]:
            similar_songs.append(song_list[song_pair[1]][0])
        return similar_songs
    
    return helper(song_list, avg_song, num)