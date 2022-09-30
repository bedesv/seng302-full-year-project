let toggle1 = 0;

document.addEventListener('DOMContentLoaded', () => {
    if (theme) {
        if (theme === 'dark') {
            document.getElementById("flexSwitchCheckDefault").checked = true;
            document.getElementById("theme-toggle__label").innerText = 'Dark';
        } else {
            document.getElementById("flexSwitchCheckDefault").checked = false;
            document.getElementById("theme-toggle__label").innerText = 'Light';
        }
    }
    if (accent) {
        let radio;
        switch (accent) {
            case "berry":
                radio = document.getElementById("accent-color__radio-1");
                radio.classList.add('accent-color__active');
                break;
            case "forest":
                radio = document.getElementById("accent-color__radio-2");
                radio.classList.add('accent-color__active');
                break;
            case "peach":
                radio = document.getElementById("accent-color__radio-3");
                radio.classList.add('accent-color__active');
                break;
            case "cobalt":
                radio = document.getElementById("accent-color__radio-4");
                radio.classList.add('accent-color__active');
                break;
            case "steel":
                radio = document.getElementById("accent-color__radio-5");
                radio.classList.add('accent-color__active');
                break;
        }
    }
})

function expandNav() {
    document.getElementById("site-navigation").classList.remove("collapsed");
    document.getElementById("site-navigation").classList.add("expanded");
    document.getElementById("page-content").classList.remove("expanded");
    document.getElementById("page-content").classList.add("constrict");
    document.getElementById("page-header").classList.add("constrict")
    document.getElementById("page-header").classList.remove("expanded")

}

function collapseNav() {
    document.getElementById("site-navigation").classList.remove("expanded");
    document.getElementById("site-navigation").classList.add("collapsed");
    document.getElementById("page-content").classList.remove("constrict");
    document.getElementById("page-content").classList.add("expanded");
    document.getElementById("page-header").classList.add("expanded")
    document.getElementById("page-header").classList.remove("constrict")


}
function toggleNavbarVisibility() {
    if (document.getElementById("site-navigation").classList.contains("collapsed")) {
        expandNav();
    } else {
        collapseNav();
    }
}

/**
 * Function to handle dropdown when arrow clicked.
 */
function dropDown() {
    let dropdown = document.getElementById("js-dropdown__list");
    if (toggle1 === 0) {
        dropdown.style.display = 'block';
        toggle1 = 1;
    } else {
        dropdown.style.display = 'none';
        toggle1 = 0;
    }
}

/**
 * Event listeners and function for checking when clicked off dropdown to close it.
 */
document.addEventListener("touchend", eventHandle)
document.addEventListener("click", eventHandle)
function eventHandle(e) {
    let dropdown = document.getElementById("js-dropdown__list");
    let classList = e.target.classList;
    if (!(classList.contains("dropdown") || classList.contains("clear-decoration") || classList.contains("navigation_icon")
        || classList.contains("dropdown__container") || classList.contains("dropdown-content")
        || classList.contains("site-navigation__sublabel") || classList.contains("site-navigation__sublink"))) {
        dropdown.style.display = 'none';
        toggle1 = 0;
    }
}

/**
 * Function handles hovering over dropdown open and close.
 */
document.addEventListener("mouseover", (e) => {
    let dropdown = document.getElementById("js-dropdown__list");
    let classList = e.target.classList;
    if (classList.contains("site-navigation__dropdown") || classList.contains("site-navigation__clear-decoration")
        || classList.contains("site-navigation__navigation-icon")
        || classList.contains("site-navigation__dropdown-container") || classList.contains("site-navigation__dropdown-content")
        || classList.contains("site-navigation__sublabel") || classList.contains("site-navigation__sublink")) {
        dropdown.style.display = 'block';
    } else if (toggle1 === 0) {
        dropdown.style.display = 'none';
    }
})

document.addEventListener('change', (event) => {
    body.classList.add('theme__transition');
    let toggle = event.target;
    if (toggle.id === 'flexSwitchCheckDefault' && toggle.type === 'checkbox') {
        if (toggle.checked) {
            body.classList.replace('light', 'dark');
            localStorage.setItem('theme', 'dark');
            document.getElementById("theme-toggle__label").innerText = 'Dark';
        } else {
            body.classList.replace('dark', 'light');
            localStorage.setItem('theme', 'light');
            document.getElementById("theme-toggle__label").innerText = 'Light';
        }
    }
})

function updateRadioButton(radio) {
    let radios = document.getElementsByClassName("accent-color__radio");
    for (let i = 0; i< radios.length; i++) {
        if (radios[i] === radio) {
            radio.classList.add("accent-color__active")
            switch (radio.id) {
                case "accent-color__radio-1":
                    if (body.classList.contains('dark')) {
                        body.className = 'dark berry';
                    } else {
                        body.className = 'light berry';
                    }
                    localStorage.setItem('accent', 'berry');
                    break;
                case "accent-color__radio-2":
                    if (body.classList.contains('dark')) {
                        body.className = 'dark forest';
                    } else {
                        body.className = 'light forest';
                    }
                    localStorage.setItem('accent', 'forest');
                    break;
                case "accent-color__radio-3":
                    if (body.classList.contains('dark')) {
                        body.className = 'dark peach';
                    } else {
                        body.className = 'light peach';
                    }
                    localStorage.setItem('accent', 'peach');
                    break;
                case "accent-color__radio-4":
                    if (body.classList.contains('dark')) {
                        body.className = 'dark cobalt';
                    } else {
                        body.className = 'light cobalt';
                    }
                    localStorage.setItem('accent', 'cobalt');
                    break;
                case "accent-color__radio-5":
                    if (body.classList.contains('dark')) {
                        body.className = 'dark steel';
                    } else {
                        body.className = 'light steel';
                    }
                    localStorage.setItem('accent', 'steel');
                    break;
            }
        } else {
            radios[i].classList.remove("accent-color__active");
        }

    }
}