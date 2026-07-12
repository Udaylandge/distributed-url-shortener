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
            if (targetId === '#') return;
            
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

    // 5. LIVE DEMO: URL Shortener Interactive Experience
    const liveForm = document.getElementById("liveShortenForm");
    const longUrlInput = document.getElementById("longUrlInput");
    const shortenBtn = document.getElementById("shortenBtn");
    const demoResultBox = document.getElementById("demoResultBox");
    const originalUrlDisplay = document.getElementById("originalUrlDisplay");
    const shortUrlDisplay = document.getElementById("shortUrlDisplay");
    const qrImage = document.getElementById("qrImage");

    // Array of realistic mock Base62 hashes for demo variety
    const mockHashes = ['a8Kd92', 'J8dk2L', 'X9vM3q', 'pL4z8W', 'mK9b2Y', 'R3xW1z'];

    if (liveForm) {
        liveForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const inputVal = longUrlInput.value.trim();
            
            if (inputVal !== "") {
                // UI Loading Feedback
                const originalBtnText = shortenBtn.innerHTML;
                shortenBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin me-2"></i>Shortening...`;
                shortenBtn.disabled = true;

                // Simulate Network latency (600ms) for realistic feel
                setTimeout(() => {
                    // Pick random hash
                    const randomHash = mockHashes[Math.floor(Math.random() * mockHashes.length)];
                    const generatedShortUrl = `https://shortify.live/${randomHash}`;

                    // Update DOM Elements
                    originalUrlDisplay.innerText = inputVal;
                    shortUrlDisplay.innerText = generatedShortUrl;
                    shortUrlDisplay.setAttribute("href", generatedShortUrl);
                    
                    // Update QR Code Image Dynamically
                    if (qrImage) {
                        qrImage.src = `https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(generatedShortUrl)}`;
                    }

                    // Show result box with fade animation
                    demoResultBox.classList.remove("d-none");

                    // Restore button state
                    shortenBtn.innerHTML = originalBtnText;
                    shortenBtn.disabled = false;

                    // Scroll slightly if on smaller screen to reveal result
                    if (window.innerWidth < 768) {
                        demoResultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                    }
                }, 600);
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