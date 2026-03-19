self.addEventListener("push", (event) => {
    console.log(event.data);
    const data = event.data.json();

    event.waitUntil(
        self.registration.showNotification(
            data.title, {body: data.message}
        )
    )
})