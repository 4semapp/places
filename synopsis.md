# Synopsis

We wish to create an application where users can post images of various locations on the planet. The user is authenticated using their Google Account. The user can then take pictures using their camera. The images are stored on the phone storage.

On the homepage the user can view interesting places submitted by the community.

The user can submit _places_ to our server, with information about the place, and a number of images.

Other users can search for submitted _places_, using our search feature. When the user searches for places, the results can be viewed on a map, or in a list. When the user views the results on the map, the user can click markers, to view details about the place.


```
Technologies

Frontend:
    Google Authentication
    Backend Authentication (Maybe)
    Responsive (Extra)

    Home
        HTTP REST

    Camera
        Camera API
        Filesystem

    Search
        HTTP REST
        Google Maps API
        Master-detail

    Contribute
        HTTP REST
        Location API
        Filesystem

Backend Endpoints
    HTTP
    SQLLite w. Anko
    Ktor (RESTful clients)

```