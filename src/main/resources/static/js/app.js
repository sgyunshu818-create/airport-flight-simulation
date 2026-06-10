function updateClock() {
    const now = new Date();
    const clock = document.querySelector("[data-live-clock]");
    const date = document.querySelector("[data-live-date]");
    if (clock) {
        clock.textContent = now.toLocaleTimeString("zh-CN", { hour12: false });
    }
    if (date) {
        date.textContent = now.toLocaleDateString("zh-CN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            weekday: "short"
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    updateClock();
    window.setInterval(updateClock, 1000);

    document.querySelectorAll("[data-confirm]").forEach((element) => {
        element.addEventListener("click", (event) => {
            if (!window.confirm(element.getAttribute("data-confirm"))) {
                event.preventDefault();
            }
        });
    });

    initMissionLogin();
    initActiveNavigation();
    initInterfaceMotion();
});

function initMissionLogin() {
    const page = document.querySelector(".login-page");
    if (!page) {
        return;
    }

    const enterButton = document.querySelector("[data-login-enter]");
    const usernameInput = document.querySelector(".login-form input[name='username']");
    if (enterButton) {
        enterButton.addEventListener("click", () => {
            if (page.classList.contains("is-auth-visible")) {
                return;
            }
            page.classList.add("is-launching");
            window.setTimeout(() => {
                page.classList.remove("login-intro-mode");
                page.classList.add("is-auth-visible");
                window.setTimeout(() => usernameInput?.focus(), 620);
            }, 260);
        });
    }

    initLoginParticles();
}

function initLoginParticles() {
    const canvas = document.querySelector("[data-login-particles]");
    if (!canvas || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return;
    }

    const ctx = canvas.getContext("2d");
    const particles = [];
    const particleCount = 82;
    let width = 0;
    let height = 0;
    let animationFrame = null;

    const resize = () => {
        width = canvas.width = window.innerWidth * window.devicePixelRatio;
        height = canvas.height = window.innerHeight * window.devicePixelRatio;
        canvas.style.width = `${window.innerWidth}px`;
        canvas.style.height = `${window.innerHeight}px`;
    };

    const createParticle = () => ({
        x: Math.random() * width,
        y: Math.random() * height,
        radius: (Math.random() * 1.7 + 0.4) * window.devicePixelRatio,
        speed: (Math.random() * 0.18 + 0.05) * window.devicePixelRatio,
        alpha: Math.random() * 0.55 + 0.18
    });

    const seed = () => {
        particles.length = 0;
        for (let i = 0; i < particleCount; i++) {
            particles.push(createParticle());
        }
    };

    const draw = () => {
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = "rgba(255,255,255,0.9)";

        particles.forEach((particle) => {
            particle.x += particle.speed;
            particle.y -= particle.speed * 0.24;
            if (particle.x > width + 10 || particle.y < -10) {
                Object.assign(particle, createParticle(), { x: -10, y: Math.random() * height });
            }

            ctx.globalAlpha = particle.alpha;
            ctx.beginPath();
            ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
            ctx.fill();
        });

        ctx.globalAlpha = 1;
        animationFrame = window.requestAnimationFrame(draw);
    };

    resize();
    seed();
    draw();
    window.addEventListener("resize", () => {
        window.cancelAnimationFrame(animationFrame);
        resize();
        seed();
        draw();
    });
}

function initActiveNavigation() {
    const currentPath = window.location.pathname.replace(/\/$/, "") || "/dashboard";
    document.querySelectorAll(".side-nav a[href]").forEach((link) => {
        const linkPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, "");
        const isActive = currentPath === linkPath || (linkPath !== "/dashboard" && currentPath.startsWith(linkPath));
        if (isActive) {
            link.classList.add("is-active");
        }
    });
}

function initInterfaceMotion() {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return;
    }

    document.querySelectorAll(".panel").forEach((panel, index) => {
        panel.style.animationDelay = `${Math.min(index * 36, 260)}ms`;
    });

    document.querySelectorAll(".metric").forEach((metric, index) => {
        metric.style.animationDelay = `${Math.min(index * 42, 300)}ms`;
        metric.classList.add("metric-ready");
    });
}
