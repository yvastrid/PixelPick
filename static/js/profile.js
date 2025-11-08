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

    // Cargar datos del perfil desde el backend
    loadProfileData();

    // Handle menu item clicks in dropdown
    if (profileDropdown) {
        const menuItems = profileDropdown.querySelectorAll('.profile-menu-item');
        menuItems.forEach(item => {
            item.addEventListener('click', function(e) {
                const text = this.querySelector('span').textContent;
                
                if (text === 'Cerrar Sesión') {
                    e.preventDefault();
                    logout();
                }
            });
        });
    }
});

// Función para cargar datos del perfil
function loadProfileData() {
    fetch('/api/profile')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const user = data.user;
                const stats = data.stats;
                
                // Actualizar nombre
                const nameElements = document.querySelectorAll('.profile-name-large, .profile-name');
                nameElements.forEach(el => {
                    el.textContent = `${user.first_name} ${user.last_name}`;
                });
                
                // Actualizar email
                const emailElements = document.querySelectorAll('.profile-email');
                emailElements.forEach(el => {
                    el.textContent = user.email;
                });
                
                // Actualizar fecha de unión
                if (user.created_at) {
                    const date = new Date(user.created_at);
                    const monthNames = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
                        'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
                    const month = monthNames[date.getMonth()];
                    const year = date.getFullYear();
                    const joinedElements = document.querySelectorAll('.profile-joined span');
                    joinedElements.forEach(el => {
                        el.textContent = `Se unió en ${month} de ${year}`;
                    });
                }
                
                // Actualizar estadísticas
                const statNumbers = document.querySelectorAll('.stat-number');
                if (statNumbers.length >= 2) {
                    statNumbers[0].textContent = stats.completed || 0;
                    statNumbers[1].textContent = stats.playing || 0;
                }
                
                // Actualizar juegos recientes
                updateRecentGames(data.games);
            }
        })
        .catch(error => {
            console.error('Error al cargar perfil:', error);
        });
}

// Función para actualizar juegos recientes
function updateRecentGames(games) {
    const gamesGrid = document.getElementById('gamesGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!gamesGrid) return;
    
    if (games && games.length > 0) {
        gamesGrid.innerHTML = '';
        games.forEach(gameData => {
            if (gameData.game) {
                const game = gameData.game;
                const lastPlayed = gameData.last_played ? formatTimeAgo(new Date(gameData.last_played)) : 'Nunca';
                
                const gameCard = document.createElement('div');
                gameCard.className = 'game-card-recent';
                gameCard.innerHTML = `
                    <div class="game-image-recent">
                        <div class="game-placeholder-recent game-${(game.id % 3) + 1}">
                            <span>${game.name.substring(0, 4).toUpperCase()}</span>
                        </div>
                    </div>
                    <div class="game-info-recent">
                        <h3 class="game-title-recent">${game.name}</h3>
                        <p class="game-time-recent">Última vez: ${lastPlayed}</p>
                    </div>
                `;
                gamesGrid.appendChild(gameCard);
            }
        });
        
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        gamesGrid.style.display = 'grid';
    } else {
        if (emptyState) {
            emptyState.style.display = 'flex';
        }
        gamesGrid.style.display = 'none';
    }
}

// Función para formatear tiempo transcurrido
function formatTimeAgo(date) {
    const now = new Date();
    const diff = now - date;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (days > 0) {
        return `Hace ${days} ${days === 1 ? 'día' : 'días'}`;
    } else if (hours > 0) {
        return `Hace ${hours} ${hours === 1 ? 'hora' : 'horas'}`;
    } else if (minutes > 0) {
        return `Hace ${minutes} ${minutes === 1 ? 'minuto' : 'minutos'}`;
    } else {
        return 'Hace unos momentos';
    }
}

// Función para cerrar sesión
function logout() {
    fetch('/api/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.location.href = '/signin';
        } else {
            alert('Error al cerrar sesión');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        window.location.href = '/signin';
    });
}

