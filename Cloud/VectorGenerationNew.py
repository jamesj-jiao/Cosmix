import json
from utils import *

song_attributes = {'acousticness', 'danceability', 'energy', 'instrumentalness', 'liveness', 'loudness', 'speechiness', 'valence', 'tempo'}
num_attributes = len(song_attributes)
feature_dict = playlist_to_features_dict(isrc_list)

# def check_attributes(dict):
#     for key in dict:
#         for attribute in song_attributes:
#             if attribute not in dict[key]:
#                 print(key, 'error')

def add_to_mix(new_isrcs, filt_isrcs, avg_vec, total_songs, num):

    all_isrcs = isrc_list[:].extend(filt_isrcs)
    feature_dict = playlist_to_features_dict(all_isrcs)

    #add to genre database

    new_isrcs_vec = []
    new_isrcs_avg_vec = [0]*num_attributes
    for isrc in new_isrcs:
        isrc_vec = [0]*num_attributes
        for index, attribute in enumerate(song_attributes):
            isrc_vec[index] = feature_dict[isrc][attribute]
            new_isrcs_avg_vec[index] += isrc_vec[index]
        new_isrcs.append(isrc_vec)

    for i in range(num_attributes):
        new_isrcs_avg_vec[i] /= num_attributes

    #new avg of mix
    new_avg_vec = []
    for i in range(num_attributes):
        new_avg_vec[i] = avg_vec[i] * (total_songs - len(new_isrcs)) / total_songs + new_isrcs_avg_vec[i] * len(new_isrcs) / total_songs

    def common_keys(json_data):
        ar = json_data[next(iter(json_data))]

        ar = {key: 0 for key in ar if type(ar[key]) == float and key != "duration_ms" and key != "key" and key != "mode" and key != "time_signature"}

        # print("thisisAR:", ar)
        for key in json_data:
            for sub_key in json_data[key]:
                #print("subkey: ", sub_key)
                if type(json_data[key][sub_key]) is float and key != "duration_ms" and key != "key" and key != "mode" and key != "time_signature":
                    ar[sub_key] = ar.get(sub_key, 0) + 1

        #for x in range(len(new_json[next(iter(new_json))])):
        for key in ar:
            if ar[key] < len(json_data):
                ar[key] = -1
                # print("this failed, ", key)
        # print("new ar: ", ar)
        return ar

    def not_common_keys(json_data):
        "None"


    def generate_arrays(new_json, array):
        final_array = []
        name_of_nums = []
        index = []
        for key in new_json:  # looking at each track's features
            # print("key:", key)
            ar = []
            ar.append(key)
            # print(new_json[key])
            #index.append(new_json[key][0])
            for smallkey in new_json[key]:
                # print("smallkey:", smallkey)
                if array.get(smallkey, 0) != -1:
                    value = new_json[key][smallkey]
                    if type(value) is float:
                        ar.append(value)
                        #if smallkey not in name_of_nums:
                        #    name_of_nums.append(smallkey)
                else:
                    ar.append("None")
            # print("ar: ", ar)
            final_array.append(ar)
        # print(finalArray)
        return final_array


    def normalize(final):
        for x in range(len(final[0])):  # len(array[0])
            # ar = [max(subArray[x]) for subArray in array if type(subArray[x]) is float]
            ar = []
            for subArray in final:
                # print("subArray: ", subArray[x])
                #print("subarray: ", subArray, " length: ", len(subArray))
                if type(subArray[x]) is float:
                    # print("subArray: ", subArray[x])
                    ar.append(subArray[x])
            # print("ar: ", ar)
            if ar:
                largest_val = max(ar)
                smallest_val = min(ar)
                for subArray in final:
                    if type(subArray[x]) is float:
                        subArray[x] = (subArray[x] - smallest_val) / (largest_val - smallest_val)


    ar = common_keys(make_dict(input_json))

    finalArray = generate_arrays(make_dict(input_json), ar)

    normalize(finalArray)

    return finalArray

#print(finalArray)

#print("FinalArray: ", finalArray, " namesArray: ", namesArray)