// Parallax effect for galaxy shapes, orbs, and glassmorphism container
document.addEventListener('DOMContentLoaded', function() {
    const galaxyShapes = document.querySelectorAll('.galaxy-shape');
    const floatingOrbs = document.querySelectorAll('.floating-orb');
    const glassContainer = document.querySelector('.glassmorphism-container');
    
    // Mouse parallax effect
    let mouseX = 0.5;
    let mouseY = 0.5;
    
    document.addEventListener('mousemove', (e) => {
        mouseX = e.clientX / window.innerWidth;
        mouseY = e.clientY / window.innerHeight;
    });
    
    // Smooth parallax animation
    function updateParallax() {
        // Move galaxy shapes based on mouse position (subtle effect)
        galaxyShapes.forEach((shape, index) => {
            const speed = (index + 1) * 0.1;
            const x = (mouseX - 0.5) * 20 * speed;
            const y = (mouseY - 0.5) * 20 * speed;
            shape.style.setProperty('--parallax-x', `${x}px`);
            shape.style.setProperty('--parallax-y', `${y}px`);
        });
        
        // Move floating orbs based on mouse position
        floatingOrbs.forEach((orb, index) => {
            const speed = (index + 1) * 0.12;
            const x = (mouseX - 0.5) * 15 * speed;
            const y = (mouseY - 0.5) * 15 * speed;
            orb.style.setProperty('--parallax-x', `${x}px`);
            orb.style.setProperty('--parallax-y', `${y}px`);
        });
        
        // Move glassmorphism container based on mouse position
        if (glassContainer) {
            const x = (mouseX - 0.5) * 20;
            const y = (mouseY - 0.5) * 20;
            glassContainer.style.setProperty('--parallax-x', `${x}px`);
            glassContainer.style.setProperty('--parallax-y', `${y}px`);
        }
        
        requestAnimationFrame(updateParallax);
    }
    
    updateParallax();
    
    // Gradient text animation enhancement
    const gradientText = document.querySelector('.gradient-text');
    if (gradientText) {
        let hue = 0;
        setInterval(() => {
            hue = (hue + 1) % 360;
            gradientText.style.filter = `hue-rotate(${hue}deg)`;
        }, 100);
    }
    
    // Button hover effects
    const ctaButton = document.querySelector('.cta-button');
    if (ctaButton) {
        ctaButton.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px) scale(1.05)';
        });
        
        ctaButton.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    }
    
    // Add sparkle effect on click
    document.addEventListener('click', function(e) {
        createSparkle(e.clientX, e.clientY);
    });
    
    function createSparkle(x, y) {
        const sparkle = document.createElement('div');
        sparkle.style.position = 'fixed';
        sparkle.style.left = x + 'px';
        sparkle.style.top = y + 'px';
        sparkle.style.width = '4px';
        sparkle.style.height = '4px';
        sparkle.style.background = '#00d4ff';
        sparkle.style.borderRadius = '50%';
        sparkle.style.pointerEvents = 'none';
        sparkle.style.zIndex = '9999';
        sparkle.style.boxShadow = '0 0 10px #00d4ff';
        sparkle.style.animation = 'sparkleFade 0.6s ease-out forwards';
        
        document.body.appendChild(sparkle);
        
        setTimeout(() => {
            sparkle.remove();
        }, 600);
    }
    
    // Add CSS for sparkle animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes sparkleFade {
            0% {
                transform: scale(0) translate(0, 0);
                opacity: 1;
            }
            100% {
                transform: scale(3) translate(${(Math.random() - 0.5) * 50}px, ${(Math.random() - 0.5) * 50}px);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
    
    // Intersection Observer for fade-in animations on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Observe elements for scroll animations
    const animatedElements = document.querySelectorAll('.left-section, .right-section');
    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
    
    // Add pulsing effect to logo star
    const logoStar = document.querySelector('.logo-star');
    if (logoStar) {
        setInterval(() => {
            logoStar.style.transform = 'scale(1.3)';
            setTimeout(() => {
                logoStar.style.transform = 'scale(1)';
            }, 200);
        }, 2000);
    }
});

