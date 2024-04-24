function getCurrentLocation() {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
            position => {
                let coords = position.coords;
                resolve([coords.latitude, coords.longitude]);
            },
            error => {
                reject(error);
            }
        );
    });
}

getCurrentLocation().then(
    coords => $0.$server.receiveCoords(coords)
).catch(error => {
    $0.$server.receiveCoords([])
    console.error("Error getting location:", error);
});