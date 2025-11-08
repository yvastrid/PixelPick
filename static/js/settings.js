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



    // Cargar datos del usuario
    loadUserData();

    // Guardar cambios del perfil
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    
    if (firstNameInput && lastNameInput) {
        // Guardar cambios cuando se pierde el foco
        [firstNameInput, lastNameInput].forEach(input => {
            input.addEventListener('blur', function() {
                if (this.value.trim() !== '') {
                    saveProfileChanges();
                }
            });
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
                    deleteAccount();
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
                    logout();
                }
            });
        });
    }

});

// Función para cargar datos del usuario
function loadUserData() {
    fetch('/api/user')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const user = data.user;
                
                // Actualizar campos del formulario
                const firstNameInput = document.getElementById('firstName');
                const lastNameInput = document.getElementById('lastName');
                
                if (firstNameInput) firstNameInput.value = user.first_name || '';
                if (lastNameInput) lastNameInput.value = user.last_name || '';
                
                // Actualizar información en el dropdown
                const nameElements = document.querySelectorAll('.profile-name');
                const emailElements = document.querySelectorAll('.profile-email');
                
                nameElements.forEach(el => {
                    el.textContent = `${user.first_name} ${user.last_name}`;
                });
                
                emailElements.forEach(el => {
                    el.textContent = user.email;
                });
                
                // Actualizar email en la sección de emails
                const emailAddressElement = document.getElementById('userEmailAddress');
                if (emailAddressElement) {
                    emailAddressElement.textContent = user.email;
                }
            }
        })
        .catch(error => {
            console.error('Error al cargar datos del usuario:', error);
        });
}

// Función para guardar cambios del perfil
function saveProfileChanges() {
    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    
    if (!firstName || !lastName) {
        alert('El nombre y apellido son requeridos');
        return;
    }
    
    fetch('/api/profile/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            firstName: firstName,
            lastName: lastName
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Actualizar nombre en el dropdown también
            const nameElements = document.querySelectorAll('.profile-name');
            nameElements.forEach(el => {
                el.textContent = `${firstName} ${lastName}`;
            });
            // Mostrar mensaje de éxito (opcional)
            console.log('Perfil actualizado exitosamente');
        } else {
            alert(data.error || 'Error al actualizar perfil');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al conectar con el servidor');
    });
}

// Función para eliminar cuenta
function deleteAccount() {
    fetch('/api/profile/delete', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('Cuenta eliminada exitosamente');
            window.location.href = '/';
        } else {
            alert(data.error || 'Error al eliminar cuenta');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al conectar con el servidor');
    });
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

