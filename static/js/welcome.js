// Welcome Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Smooth scroll for anchor links
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#' && href !== '') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });

    // Parallax effect for background elements
    let lastScrollY = window.scrollY;
    
    function updateParallax() {
        const scrollY = window.scrollY;
        const parallaxElements = document.querySelectorAll('.floating-shapes .shape');
        
        parallaxElements.forEach((element, index) => {
            const speed = 0.5 + (index * 0.1);
            const yPos = -(scrollY * speed);
            element.style.transform = `translateY(${yPos}px)`;
        });
        
        lastScrollY = scrollY;
    }
    
    window.addEventListener('scroll', updateParallax);
    updateParallax();

    // Animate cards on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    const cards = document.querySelectorAll('.game-card, .catalog-item');
    cards.forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(card);
    });

    // Add hover effects to game cards
    const gameCards = document.querySelectorAll('.game-card');
    gameCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // Search Modal Functionality
    const searchButton = document.getElementById('searchButton');
    const searchModal = document.getElementById('searchModal');
    const searchClose = document.getElementById('searchClose');
    const searchInput = document.getElementById('searchInput');
    const searchResults = document.getElementById('searchResults');
    const profileDropdown = document.getElementById('profileDropdown');

    if (searchButton && searchModal) {
        // Open search modal
        searchButton.addEventListener('click', function() {
            // Close profile dropdown if open
            if (profileDropdown) {
                profileDropdown.classList.remove('active');
            }
            searchModal.classList.add('active');
            setTimeout(() => {
                searchInput.focus();
            }, 100);
        });

        // Close search modal
        if (searchClose) {
            searchClose.addEventListener('click', function() {
                searchModal.classList.remove('active');
                searchInput.value = '';
                searchResults.innerHTML = '';
            });
        }

        // Close on escape key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && searchModal.classList.contains('active')) {
                searchModal.classList.remove('active');
                searchInput.value = '';
                searchResults.innerHTML = '';
            }
        });

        // Close on background click
        searchModal.addEventListener('click', function(e) {
            if (e.target === searchModal) {
                searchModal.classList.remove('active');
                searchInput.value = '';
                searchResults.innerHTML = '';
            }
        });

        // Search functionality
        if (searchInput && searchResults) {
            searchInput.addEventListener('input', function(e) {
                const query = e.target.value.toLowerCase().trim();
                
                if (query.length === 0) {
                    searchResults.innerHTML = '';
                    return;
                }

                // Mock search results (you can replace this with actual API call)
                const mockGames = [
                    { name: 'Mario Kart', category: 'Carreras' },
                    { name: 'Roblox', category: 'Mundo Abierto' },
                    { name: 'Call of Duty', category: 'Acción' },
                    { name: 'Space Wars', category: 'Sci-Fi' },
                    { name: 'Return of the Cars', category: 'Carreras' },
                    { name: 'Planes of Gloria', category: 'Aviación' },
                    { name: 'Earth Wars', category: 'Estrategia' }
                ];

                const filteredGames = mockGames.filter(game => 
                    game.name.toLowerCase().includes(query) || 
                    game.category.toLowerCase().includes(query)
                );

                if (filteredGames.length > 0) {
                    searchResults.innerHTML = filteredGames.map(game => `
                        <div class="search-result-item">
                            <div class="search-result-content">
                                <h4 class="search-result-title">${game.name}</h4>
                                <p class="search-result-category">${game.category}</p>
                            </div>
                        </div>
                    `).join('');
                } else {
                    searchResults.innerHTML = `
                        <div class="search-result-empty">
                            <p>No se encontraron juegos para "${query}"</p>
                        </div>
                    `;
                }
            });
        }
    }

    // Profile Button Functionality
    const profileButton = document.getElementById('profileButton');
    
    if (profileButton && profileDropdown) {
        // Toggle dropdown on click
        profileButton.addEventListener('click', function(e) {
            e.stopPropagation();
            // Close search modal if open
            if (searchModal && searchModal.classList.contains('active')) {
                searchModal.classList.remove('active');
                if (searchInput) searchInput.value = '';
                if (searchResults) searchResults.innerHTML = '';
            }
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

        // Handle menu item clicks
        const menuItems = profileDropdown.querySelectorAll('.profile-menu-item');
        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                const text = this.querySelector('span').textContent;
                
                if (text === 'Cerrar Sesión') {
                    window.location.href = '/signin';
                } else if (text === 'Mi Perfil') {
                    window.location.href = '/profile';
                } else if (text === 'Configuración') {
                    window.location.href = '/settings';
                } else {
                    // Handle other menu items
                    console.log(`Clicked: ${text}`);
                    // You can add navigation here for other items
                }
                
                profileDropdown.classList.remove('active');
            });
        });
    }
});

