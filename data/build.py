# Copyright (C) 2015 Jeffrey Meyers
#
# This program is released under the "MIT License".
# Please see the file COPYING in this distribution for license terms.

import csv, subprocess, os, itertools

OUTPUT_DIR = "output"
DATA_DIR = "source_data"
ROUTES = os.path.join(DATA_DIR, 'routes.shp')
STOPS = os.path.join(DATA_DIR, 'stops.shp')
ROUTE_IDS = ["9", "17", "193"]
DIRECTION_IDS = ["0", "1"]

# each "run" has route along with direction
# Powell Blvd - To Portland
#   Route: 9
#   Direction: 1

# units in map projection for how much to simplify line geojson

SIMPLIFY = 20

"""
file naming schema:

<route_id>_<direction>_stops.geojson
<route_id>_<direction>_routes.geojson

Base command: 
  ogr2ogr -f GeoJSON -t_srs EPSG:4326 <output geojson> <input shapefile>

Extra flags:
  -simplify <tolerance>
  -sql "SELECT <..> FROM <..> WHERE <..>
"""


def pretty_json(source):
    target = os.path.splitext(source)[0] + ".geojson"
    prettify = "cat {0} | python -m json.tool > {1}".format(source, target)
    remove = "rm -rf {0}".format(source)
    subprocess.call(prettify, shell=True)
    subprocess.call(remove, shell=True) 


def runner():
    # clear output directory
    subprocess.call("rm -f {0}/*".format(OUTPUT_DIR),  shell=True) 
    runs = itertools.product(ROUTE_IDS, DIRECTION_IDS)
    for run in runs:
        stops, stops_output = build_stops_command(*run)
        routes, routes_output = build_routes_command(*run)
        subprocess.call(stops, shell=True)
        subprocess.call(routes, shell=True) 
        pretty_json(stops_output)
        pretty_json(routes_output)
    print "view files in directoy: " + OUTPUT_DIR
def build_stops_command(route, direction):
    stops = os.path.splitext(os.path.basename(STOPS))[0]
    stops_file = "{0}_{1}_stops.temp"
    stops_command = """ogr2ogr \
        -f GeoJSON \
        -t_srs EPSG:4326 \
        -sql \"{0}\" \
        {1} {2}"""

    sql = "SELECT * FROM {0} WHERE rte={1} AND dir={2}"
    output = os.path.join(OUTPUT_DIR, stops_file.format(route,direction))
    sql_command = sql.format(stops, route, direction)
    command = stops_command.format(sql_command, output, STOPS) 
    return command, output

def build_routes_command(route, direction):
    routes = os.path.splitext(os.path.basename(ROUTES))[0]
    routes_file = "{0}_{1}_routes.temp"
    routes_command = """ogr2ogr \
        -f GeoJSON \
        -t_srs EPSG:4326 \
        -sql \"{0}\" \
        -simplify {1} \
        {2} {3}"""
    sql = "SELECT * FROM {0} WHERE rte={1} AND dir={2}"
    output = os.path.join(OUTPUT_DIR, routes_file.format(route,direction))
    sql_command = sql.format(routes, route, direction)
    command = routes_command.format(sql_command, SIMPLIFY, output, ROUTES) 
    return command, output

if __name__ == "__main__":
    runner()
    


