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
    initHudDashboard();
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

function initHudDashboard() {
    const page = document.querySelector(".dashboard-hud-page");
    const canvas = document.querySelector(".viewport-canvas");
    const airport = document.querySelector(".panoptic-airport");
    if (!page || !canvas || !airport) {
        return;
    }

    let scale = 1;
    let offsetX = 0;
    let offsetY = 0;
    let isDragging = false;
    let dragStart = { x: 0, y: 0 };

    const applyTransform = () => {
        airport.style.setProperty("--hud-scale", scale.toFixed(2));
        airport.style.setProperty("--hud-x", `${offsetX}px`);
        airport.style.setProperty("--hud-y", `${offsetY}px`);
        page.dataset.zoom = `${Math.round(scale * 100)}%`;
    };

    const setViewPreset = (preset) => {
        const presets = {
            global: { scale: 1, x: 0, y: 0 },
            terminal: { scale: 1.28, x: -92, y: -24 },
            runway: { scale: 1.36, x: 64, y: 36 }
        };
        const next = presets[preset] || presets.global;
        scale = next.scale;
        offsetX = next.x;
        offsetY = next.y;
        applyTransform();
    };

    canvas.addEventListener("wheel", (event) => {
        if (window.innerWidth <= 860) {
            return;
        }
        event.preventDefault();
        const delta = event.deltaY > 0 ? -0.08 : 0.08;
        scale = Math.min(1.8, Math.max(0.72, scale + delta));
        applyTransform();
    }, { passive: false });

    canvas.addEventListener("pointerdown", (event) => {
        if (event.button !== 1 && event.button !== 2) {
            return;
        }
        event.preventDefault();
        isDragging = true;
        dragStart = { x: event.clientX - offsetX, y: event.clientY - offsetY };
        canvas.setPointerCapture(event.pointerId);
        canvas.classList.add("is-panning");
    });

    canvas.addEventListener("pointermove", (event) => {
        if (!isDragging) {
            return;
        }
        offsetX = event.clientX - dragStart.x;
        offsetY = event.clientY - dragStart.y;
        applyTransform();
    });

    const stopDragging = () => {
        isDragging = false;
        canvas.classList.remove("is-panning");
    };
    canvas.addEventListener("pointerup", stopDragging);
    canvas.addEventListener("pointercancel", stopDragging);

    const contextMenu = document.querySelector("[data-context-menu]");
    canvas.addEventListener("contextmenu", (event) => {
        event.preventDefault();
        if (!contextMenu) {
            return;
        }
        const rect = canvas.getBoundingClientRect();
        contextMenu.hidden = false;
        contextMenu.style.left = `${event.clientX - rect.left}px`;
        contextMenu.style.top = `${event.clientY - rect.top}px`;
    });

    document.addEventListener("click", (event) => {
        if (contextMenu && !contextMenu.contains(event.target)) {
            contextMenu.hidden = true;
        }
    });

    document.querySelectorAll("[data-layer-toggle]").forEach((button) => {
        button.addEventListener("click", () => {
            const layerName = button.getAttribute("data-layer-toggle");
            const layer = document.querySelector(`[data-layer="${layerName}"]`);
            if (!layer) {
                return;
            }
            const visible = layer.classList.toggle("is-visible");
            button.classList.toggle("is-active", visible);
        });
    });

    document.querySelectorAll("[data-view-preset]").forEach((button) => {
        button.addEventListener("click", () => {
            setViewPreset(button.getAttribute("data-view-preset"));
        });
    });

    document.querySelectorAll("[data-time-warp] button").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll("[data-time-warp] button").forEach((item) => item.classList.remove("is-active"));
            button.classList.add("is-active");
            page.dataset.speed = button.getAttribute("data-speed");
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.target && ["INPUT", "SELECT", "TEXTAREA"].includes(event.target.tagName)) {
            return;
        }
        if (event.code === "Space") {
            event.preventDefault();
            page.classList.toggle("is-paused");
        }
        if (event.code === "ArrowRight") {
            event.preventDefault();
            document.querySelector(".console-actions .button.primary")?.classList.add("is-key-pulse");
            window.setTimeout(() => document.querySelector(".console-actions .button.primary")?.classList.remove("is-key-pulse"), 260);
        }
        if (event.key === "1") setViewPreset("global");
        if (event.key === "2") setViewPreset("terminal");
        if (event.key === "3") setViewPreset("runway");
        if (event.key.toLowerCase() === "f") {
            page.classList.toggle("is-inspector-hidden");
        }
    });

    applyTransform();
}
