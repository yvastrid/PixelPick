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

    // Botón de guardar
    const saveButton = document.getElementById('saveProfileButton');
    if (saveButton) {
        saveButton.addEventListener('click', function() {
            saveProfileChanges();
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
                
                // Actualizar información de cambios disponibles
                updateNameChangeInfo(user);
            }
        })
        .catch(error => {
            console.error('Error al cargar datos del usuario:', error);
        });
}

// Función para actualizar información de cambios disponibles
function updateNameChangeInfo(user) {
    const changesRemainingText = document.getElementById('changesRemainingText');
    const saveButton = document.getElementById('saveProfileButton');
    
    if (!changesRemainingText) return;
    
    const changesCount = user.name_change_count || 0;
    const changesRemaining = Math.max(0, 3 - changesCount);
    
    if (changesCount >= 3) {
        // Verificar si han pasado 60 días
        if (user.last_name_change_date) {
            const lastChangeDate = new Date(user.last_name_change_date);
            const now = new Date();
            const daysSinceChange = Math.floor((now - lastChangeDate) / (1000 * 60 * 60 * 24));
            const daysRemaining = 60 - daysSinceChange;
            
            if (daysRemaining > 0) {
                changesRemainingText.textContent = `Has excedido el límite de 3 cambios. Debes esperar ${daysRemaining} días más para poder cambiar tu nombre/apellido.`;
                changesRemainingText.style.color = 'rgba(255, 100, 100, 0.9)';
                if (saveButton) {
                    saveButton.disabled = true;
                    saveButton.style.opacity = '0.5';
                    saveButton.style.cursor = 'not-allowed';
                }
                return;
            } else {
                // Han pasado 60 días, puede cambiar de nuevo
                changesRemainingText.textContent = 'Puedes cambiar tu nombre/apellido nuevamente.';
                changesRemainingText.style.color = 'rgba(0, 212, 255, 0.9)';
            }
        }
    } else {
        changesRemainingText.textContent = `Tienes ${changesRemaining} ${changesRemaining === 1 ? 'oportunidad' : 'oportunidades'} restante${changesRemaining !== 1 ? 's' : ''} para cambiar tu nombre/apellido.`;
        changesRemainingText.style.color = 'rgba(255, 255, 255, 0.7)';
    }
    
    if (saveButton) {
        saveButton.disabled = false;
        saveButton.style.opacity = '1';
        saveButton.style.cursor = 'pointer';
    }
}

// Función para guardar cambios del perfil
function saveProfileChanges() {
    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    const saveButton = document.getElementById('saveProfileButton');
    const saveMessage = document.getElementById('saveMessage');
    
    if (!firstName || !lastName) {
        showMessage('El nombre y apellido son requeridos', 'error');
        return;
    }
    
    // Deshabilitar botón mientras se guarda
    if (saveButton) {
        saveButton.disabled = true;
        saveButton.innerHTML = '<span>Guardando...</span>';
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
            
            // Mostrar mensaje de éxito
            showMessage(data.message || 'Nombre y apellido actualizados correctamente', 'success');
            
            // Actualizar información de cambios disponibles
            if (data.user) {
                updateNameChangeInfo(data.user);
            }
        } else {
            showMessage(data.error || 'Error al actualizar perfil', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('Error al conectar con el servidor', 'error');
    })
    .finally(() => {
        // Re-habilitar botón
        if (saveButton) {
            saveButton.disabled = false;
            saveButton.innerHTML = '<span>Guardar Cambios</span>';
        }
    });
}

// Función para mostrar mensajes
function showMessage(message, type) {
    const saveMessage = document.getElementById('saveMessage');
    if (!saveMessage) return;
    
    saveMessage.style.display = 'block';
    saveMessage.textContent = message;
    
    if (type === 'success') {
        saveMessage.style.background = 'rgba(0, 212, 255, 0.2)';
        saveMessage.style.border = '1px solid rgba(0, 212, 255, 0.5)';
        saveMessage.style.color = '#00d4ff';
    } else {
        saveMessage.style.background = 'rgba(255, 100, 100, 0.2)';
        saveMessage.style.border = '1px solid rgba(255, 100, 100, 0.5)';
        saveMessage.style.color = '#ff6464';
    }
    
    // Ocultar mensaje después de 5 segundos
    setTimeout(() => {
        saveMessage.style.display = 'none';
    }, 5000);
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

