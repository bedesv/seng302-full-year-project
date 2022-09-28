function makeActive(el) {
    el.classList.add("active-carousel-tab");
    let els = document.getElementsByClassName("user-tab__button");
    for (let i = 0; i < els.length; i++) {
        if (els[i] !== el) {
            console.log("HERE");
            els[i].classList.remove("active-carousel-tab");
        }
    }
}