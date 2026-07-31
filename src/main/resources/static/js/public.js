/**
 * Shortify.live - Commercial SaaS Interactive Scripts
 * Handles Live Demo URL shortening, Copy to Clipboard toast, QR modal generation, and counters.
 */

document.addEventListener("DOMContentLoaded", () => {
    // 1. Initialize AOS Animation Library
    if (typeof AOS !== 'undefined') {
        AOS.init({
            duration: 800,
            easing: 'ease-out-cubic',
            once: true,
            offset: 50
        });
    }

    // 2. Navbar Scrolled Shadow Effect
    const navbar = document.querySelector(".glass-navbar");
    window.addEventListener("scroll", () => {
        if (window.scrollY > 40) {
            navbar.classList.add("scrolled");
        } else {
            navbar.classList.remove("scrolled");
        }
    });

    // 3. Smooth Scrolling for Anchor Links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const targetId = this.getAttribute('href');
            // Only process in-page anchor links — skip full URLs or bare '#'
            if (!targetId || targetId === '#' || !targetId.startsWith('#')) return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // 4. Animated Statistics Counter
    const counters = document.querySelectorAll(".counter");
    const speed = 150; 

    const animateCounter = (counter) => {
        const target = +counter.getAttribute("data-target");
        let count = 0;
        const inc = target / speed;

        const updateCount = () => {
            count += inc;
            if (count < target) {
                counter.innerText = Math.ceil(count).toLocaleString();
                requestAnimationFrame(updateCount);
            } else {
                counter.innerText = target.toLocaleString() + "+";
            }
        };
        updateCount();
    };

    const counterObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animateCounter(entry.target);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });

    counters.forEach(counter => {
        counterObserver.observe(counter);
    });

    // 5. LIVE DEMO: URL Shortener Interactive Experience (calls real backend API)
    const liveForm = document.getElementById("liveShortenForm");
    const longUrlInput = document.getElementById("longUrlInput");
    const shortenBtn = document.getElementById("shortenBtn");
    const demoResultBox = document.getElementById("demoResultBox");
    const originalUrlDisplay = document.getElementById("originalUrlDisplay");
    const shortUrlDisplay = document.getElementById("shortUrlDisplay");
    const qrImage = document.getElementById("qrImage");

    if (liveForm) {
        liveForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const inputVal = longUrlInput.value.trim();

            if (inputVal === "") return;

            // UI Loading Feedback
            const originalBtnText = shortenBtn.innerHTML;
            shortenBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin me-2"></i>Shortening...`;
            shortenBtn.disabled = true;

            try {
                // Call the real backend API to create a short URL in MongoDB
                const response = await fetch("/api/shorten", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ originalUrl: inputVal })
                });

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(data.error || "Failed to shorten URL");
                }

                const generatedShortUrl = data.shortUrl;

                // Update DOM Elements with REAL short URL
                originalUrlDisplay.innerText = data.originalUrl;
                shortUrlDisplay.innerText = generatedShortUrl;
                shortUrlDisplay.setAttribute("href", generatedShortUrl);
                shortUrlDisplay.setAttribute("target", "_blank");

                // Update QR Code Image Dynamically
                if (qrImage) {
                    qrImage.src = `https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(generatedShortUrl)}`;
                }

                // Show result box with fade animation
                demoResultBox.classList.remove("d-none");

                // Scroll slightly if on smaller screen to reveal result
                if (window.innerWidth < 768) {
                    demoResultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }

            } catch (err) {
                console.error("Shorten failed:", err);
                alert("Could not shorten URL: " + err.message);
            } finally {
                // Restore button state
                shortenBtn.innerHTML = originalBtnText;
                shortenBtn.disabled = false;
            }
        });
    }

    // 6. Copy to Clipboard & Toast Notification
    const copyBtn = document.getElementById("copyBtn");
    const toastElement = document.getElementById("copyToast");
    const toastMessage = document.getElementById("toastMessage");
    
    if (copyBtn && toastElement) {
        const toast = new bootstrap.Toast(toastElement, { delay: 3000 });

        copyBtn.addEventListener("click", () => {
            const textToCopy = shortUrlDisplay.innerText;
            
            navigator.clipboard.writeText(textToCopy).then(() => {
                // Change Button Icon temporarily
                const originalHtml = copyBtn.innerHTML;
                copyBtn.innerHTML = `<i class="fa-solid fa-check text-success me-2"></i><span>Copied!</span>`;
                copyBtn.classList.replace("btn-outline-primary", "btn-outline-success");

                // Show Toast
                toastMessage.innerText = `Copied ${textToCopy} to clipboard!`;
                toast.show();

                setTimeout(() => {
                    copyBtn.innerHTML = originalHtml;
                    copyBtn.classList.replace("btn-outline-success", "btn-outline-primary");
                }, 2000);
            }).catch(err => {
                console.error("Failed to copy text: ", err);
            });
        });
    }
});