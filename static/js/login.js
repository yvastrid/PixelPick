// Login Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Password Toggle
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');
    
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Update icon (simple toggle - you can enhance this with different icons)
            passwordToggle.style.opacity = type === 'password' ? '0.5' : '1';
        });
    }

    // Form Submission
    const registerForm = document.getElementById('registerForm');
    
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form values
            const formData = {
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value,
                terms: document.getElementById('terms').checked
            };
            
            // Basic validation
            if (!formData.firstName || !formData.lastName || !formData.email || 
                !formData.password || !formData.terms) {
                alert('Por favor, completa todos los campos y acepta los términos.');
                return;
            }
            
            // Email validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(formData.email)) {
                alert('Por favor, ingresa un correo electrónico válido.');
                return;
            }
            
            // Password validation (minimum 8 characters)
            if (formData.password.length < 8) {
                alert('La contraseña debe tener al menos 8 caracteres.');
                return;
            }
            
            // Enviar datos al backend
            fetch('/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Si el usuario tenía intención de suscribirse, redirigir al checkout
                    if (data.redirect_to_checkout) {
                        window.location.href = '/checkout';
                    } else {
                        // Registro exitoso, redirigir a welcome
                        window.location.href = '/welcome';
                    }
                } else {
                    alert(data.error || 'Error al registrar usuario');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al conectar con el servidor');
            });
        });
    }

    // Add input focus animations
    const inputs = document.querySelectorAll('.form-group input');
    
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.style.transform = 'scale(1.02)';
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.style.transform = 'scale(1)';
        });
    });
});

