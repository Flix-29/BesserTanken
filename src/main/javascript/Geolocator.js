function getCurrentLocation() {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
            position => {
                let coords = position.coords;
                resolve({ latitude: coords.latitude, longitude: coords.longitude });
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
    console.error("Error getting location:", error);
    return null;
});