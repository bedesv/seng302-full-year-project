let body = document.body;
let theme = localStorage.getItem('theme');
let accent = localStorage.getItem('accent');

if (theme) {
    body.classList.add(theme);
    body.classList.add(accent);
} else {
    body.classList.add('light');
    body.classList.add('cobalt');
    localStorage.setItem('theme', 'light');
    localStorage.setItem('accent', 'cobalt');
}