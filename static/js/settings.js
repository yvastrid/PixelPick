// Settings Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Tab Functionality
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabPanes = document.querySelectorAll('.tab-pane');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');

            // Remove active class from all buttons and panes
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabPanes.forEach(pane => pane.classList.remove('active'));

            // Add active class to clicked button and corresponding pane
            this.classList.add('active');
            const targetPane = document.getElementById(`${targetTab}-tab`);
            if (targetPane) {
                targetPane.classList.add('active');
            }
        });
    });

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

    // Search Button (placeholder)
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            if (profileDropdown) {
                profileDropdown.classList.remove('active');
            }
            alert('Funcionalidad de búsqueda próximamente disponible');
        });
    }



    // Delete Account Button
    const deleteAccountButton = document.getElementById('deleteAccountButton');
    if (deleteAccountButton) {
        deleteAccountButton.addEventListener('click', function() {
            const confirmMessage = '¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.';
            const secondConfirm = 'Esta acción es permanente. Escribe "ELIMINAR" para confirmar:';
            
            if (confirm(confirmMessage)) {
                const userInput = prompt(secondConfirm);
                if (userInput === 'ELIMINAR') {
                    // Here you would handle account deletion
                    alert('Cuenta eliminada exitosamente');
                    // window.location.href = '/';
                } else if (userInput) {
                    alert('Confirmación incorrecta. La eliminación de cuenta ha sido cancelada.');
                }
            }
        });
    }

    // Form Validation
    const formInputs = document.querySelectorAll('.form-input, .form-textarea');
    formInputs.forEach(input => {
        input.addEventListener('blur', function() {
            if (this.value.trim() === '' && this.hasAttribute('required')) {
                this.style.borderColor = 'rgba(255, 100, 100, 0.5)';
            } else {
                this.style.borderColor = 'rgba(0, 212, 255, 0.3)';
            }
        });

        input.addEventListener('input', function() {
            if (this.style.borderColor.includes('255, 100, 100')) {
                this.style.borderColor = 'rgba(0, 212, 255, 0.3)';
            }
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

