// Profile Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Profile Dropdown Functionality
    const profileButton = document.getElementById('profileButton');
    const profileDropdown = document.getElementById('profileDropdown');
    const searchButton = document.getElementById('searchButton');

    if (profileButton && profileDropdown) {
        // Toggle dropdown on click
        profileButton.addEventListener('click', function(e) {
            e.stopPropagation();
            profileDropdown.classList.toggle('active');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!profileButton.contains(e.target) && !profileDropdown.contains(e.target)) {
                profileDropdown.classList.remove('active');
            }
        });

        // Close dropdown on escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && profileDropdown.classList.contains('active')) {
                profileDropdown.classList.remove('active');
            }
        });
    }

    // Search Button (placeholder for future functionality)
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            // Close profile dropdown if open
            if (profileDropdown) {
                profileDropdown.classList.remove('active');
            }
            // Add search functionality here
            alert('Funcionalidad de búsqueda próximamente disponible');
        });
    }

    // Animate game cards on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
    }, observerOptions);

    const gameCards = document.querySelectorAll('.game-card-recent');
    gameCards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(card);
    });

    // Check if games grid is empty and show empty state
    const gamesGrid = document.getElementById('gamesGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (gamesGrid && emptyState) {
        const gameCards = gamesGrid.querySelectorAll('.game-card-recent');
        if (gameCards.length === 0) {
            gamesGrid.style.display = 'none';
            emptyState.style.display = 'flex';
        }
    }

    // Add hover effects to game cards
    gameCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // Handle menu item clicks in dropdown
    if (profileDropdown) {
        const menuItems = profileDropdown.querySelectorAll('.profile-menu-item');
        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                const text = this.querySelector('span').textContent;
                
                if (text === 'Cerrar Sesión') {
                    e.preventDefault();
                    window.location.href = '/signin';
                }
            });
        });
    }
});

